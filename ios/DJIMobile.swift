//
//  DJISDK.swift
//  ReactNativeDjiMobile
//
//  Created by Adam Rosendorff on 2019/01/29.

import Foundation
import DJISDK

@objc(DJIMobile)
class DJIMobile: NSObject, RCTInvalidating {
  
  let realTimeDataRecorder = RealTimeDataRecorder()
  
  func invalidate() {
    // For debugging, when the Javascript side reloads, we want to remove all DJI event listeners
    if (DJISDKManager.hasSDKRegistered()) {
      DJISDKManager.keyManager()?.stopAllListening(ofListeners: self)
    }
  }
  
  // This allows us to use a single function to stop key listeners, as it can find the key string & key type from here
  let implementedKeys: [String: [Any]] = [
    "DJIParamConnection": [DJIParamConnection, DJIProductKey()],
    "DJIBatteryParamChargeRemainingInPercent": [DJIBatteryParamChargeRemainingInPercent, DJIBatteryKey()],
    "DJIFlightControllerParamAircraftLocation": [DJIFlightControllerParamAircraftLocation, DJIFlightControllerKey()],
    "DJIFlightControllerParamVelocity": [DJIFlightControllerParamVelocity, DJIFlightControllerKey()],
    "DJIFlightControllerParamCompassHeading": [DJIFlightControllerParamCompassHeading, DJIFlightControllerKey()],
  ]
  
  var keyListeners: [String] = []
  
  @objc(registerApp:reject:)
  func registerApp(resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    registerAppInternal(nil, resolve, reject)
  }
  
  @objc(registerAppAndUseBridge:resolve:reject:)
  func registerAppAndUseBridge(bridgeIp: String, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    registerAppInternal(bridgeIp, resolve, reject)
  }
  
  func registerAppInternal(_ bridgeIp: String?, _ resolve: @escaping RCTPromiseResolveBlock, _ reject: @escaping RCTPromiseRejectBlock) {
    var sentRegistration = false
    
    DJISDKManager.startListeningOnRegistrationUpdates(withListener: self) { (registered: Bool, registrationError: Error) in
      if (DJISDKManager.hasSDKRegistered() == true) {
        if (bridgeIp != nil) {
          DJISDKManager.enableBridgeMode(withBridgeAppIP: bridgeIp!)
        } else {
          DJISDKManager.startConnectionToProduct()
        }
        if (!sentRegistration) {
          resolve("DJI SDK: Registration Successful")
          sentRegistration = true
        }
      } else if (registrationError != nil) {
        if (!sentRegistration) {
          self.sendReject(reject, "Registration Error", registrationError as NSError)
          sentRegistration = true
        }
      }
    }
    
    DJISDKManager.beginAppRegistration()
  }
  
  @objc(startRecordRealTimeData:reject:)
  func startRecordRealTimeData(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
    //    DJISDKManager.keyManager()?.startListeningForChanges(on: key, withListener: self, andUpdate: updateBlock)
    guard let keyManager = DJISDKManager.keyManager() else {
      // TODO: Could not get the keyManager
      self.sendReject(reject, "startRecordRealTimeData Error", nil)
      return
    }
    
    let fileName = "testfile"
    
//    self.writeDataToLogFile(fileName: fileName, data: [
//      "START RECORD": "TRUE",
//      ])
    
    keyManager.startListeningForChanges(on: DJIFlightControllerKey(param: DJIFlightControllerParamAircraftLocation)!, withListener: self.realTimeDataRecorder) { (oldValue: DJIKeyedValue?, newValue: DJIKeyedValue?) in
      if let location = newValue?.value as? CLLocation {
        let longitude = location.coordinate.longitude
        let latitude = location.coordinate.latitude
        let altitude = location.altitude
        self.writeDataToLogFile(fileName: fileName, data: [
          "longitude": longitude,
          "latitude": latitude,
          "altitude": altitude,
          ])
      }
    }
    
    keyManager.startListeningForChanges(on: DJIFlightControllerKey(param: DJIFlightControllerParamAttitude)!, withListener: self.realTimeDataRecorder) { (oldValue: DJIKeyedValue?, newValue: DJIKeyedValue?) in
      if let attitude = newValue?.value as? DJIAttitude {
        self.writeDataToLogFile(fileName: fileName, data: [
          "drone_pitch": attitude.pitch,
          "drone_roll": attitude.roll,
          "drone_yaw": attitude.yaw,
          ])
      }
    }
    
    keyManager.startListeningForChanges(on: DJIFlightControllerKey(param: DJIFlightControllerParamVelocity)!, withListener: self.realTimeDataRecorder) { (oldValue: DJIKeyedValue?, newValue: DJIKeyedValue?) in
      if let velocity = newValue?.value as? DJISDKVector3D {
        self.writeDataToLogFile(fileName: fileName, data: [
          "velocity_x": velocity.x,
          "velocity_y": velocity.y,
          "velocity_z": velocity.z,
          ])
      }
    }
    
    keyManager.startListeningForChanges(on: DJIGimbalKey(param: DJIGimbalParamAttitudeInDegrees)!, withListener: self.realTimeDataRecorder) { (oldValue: DJIKeyedValue?, newValue: DJIKeyedValue?) in
      if let gimbalAttitude = newValue?.value as? DJIGimbalAttitude {
        self.writeDataToLogFile(fileName: fileName, data: [
          "gimbal_pitch": gimbalAttitude.pitch,
          "gimbal_roll": gimbalAttitude.roll,
          "gimbal_yaw": gimbalAttitude.yaw,
          ])
      }
    }
    
    resolve("startRecordRealTimeData Successful")
    return
    
  }
  
  @objc(stopRecordRealTimeData:reject:)
  func stopRecordRealTimeData(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
    guard let keyManager = DJISDKManager.keyManager() else {
      // TODO: Could not get the keyManager
      self.sendReject(reject, "stopRecordRealTimeData Error", nil)
      return
    }
    keyManager.stopAllListening(ofListeners: self.realTimeDataRecorder)
    resolve("stopRecordRealTimeData Successful")
    return
  }
  
  private func writeDataToLogFile(fileName: String, data: [String: Any]) {
    let fileManager = FileManager.default
    
    let documentUrl = try! fileManager.url(
      for: .documentDirectory,
      in: .userDomainMask,
      appropriateFor: nil,
      create: true
    )
    
    let fileUrl = documentUrl.appendingPathComponent(fileName).appendingPathExtension("txt")
    
    do {
      
      let fileExists = try? fileUrl.checkResourceIsReachable()
      if (fileExists != true) {
        try "".write(to: fileUrl, atomically: true, encoding: .utf8)
      }
      let file = try FileHandle(forUpdating: fileUrl)
      file.seekToEndOfFile()
      
      let currentDate = Date()
      let df = DateFormatter()
      df.dateFormat =  "yyyy-MM-dd'T'HH:mm:ss.SSSS"
      
      for (key, value) in data {
        var fileEntry = "\n"
        fileEntry += df.string(from: currentDate)
        fileEntry += " " + key + ":" + String(describing: value)
        file.write(fileEntry.data(using: .utf8)!)
      }
      file.closeFile()
    } catch {
      print(error)
    }
  }
  
  @objc(startProductConnectionListener:reject:)
  func startProductConnectionListener(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
    let key = DJIProductKey(param: DJIParamConnection)!
    resolve(nil)
    self.startKeyListener(key: key) { (oldValue: DJIKeyedValue?, newValue: DJIKeyedValue?) in
      if let connected = newValue?.boolValue {
        EventSender.sendReactEvent(type: "connectionStatus", value: connected ? "connected" : "disconnected")
      }
    }
  }
  
  @objc(startBatteryPercentChargeRemainingListener:reject:)
  func startBatteryPercentChargeRemainingListener(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
    let key = DJIBatteryKey(param: DJIBatteryParamChargeRemainingInPercent)!
    resolve(nil)
    self.startKeyListener(key: key) { (oldValue: DJIKeyedValue?, newValue: DJIKeyedValue?) in
      if let chargePercent = newValue?.integerValue {
        EventSender.sendReactEvent(type: "chargeRemaining", value: chargePercent)
      }
    }
  }
  
  @objc(startAircraftLocationListener:reject:)
  func startAircraftLocationListener(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
    let key = DJIFlightControllerKey(param: DJIFlightControllerParamAircraftLocation)!
    resolve(nil)
    self.startKeyListener(key: key) { (oldValue: DJIKeyedValue?, newValue: DJIKeyedValue?) in
      if let location = newValue?.value as? CLLocation {
        let longitude = location.coordinate.longitude
        let latitude = location.coordinate.latitude
        let altitude = location.altitude
        EventSender.sendReactEvent(type: "aircraftLocation", value: [
          "longitude": longitude,
          "latitude": latitude,
          "altitude": altitude,
          ])
      }
    }
  }
  
  @objc(startAircraftVelocityListener:reject:)
  func startAircraftVelocityListener(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
    let key = DJIFlightControllerKey(param: DJIFlightControllerParamVelocity)!
    resolve(nil)
    self.startKeyListener(key: key) { (oldValue: DJIKeyedValue?, newValue: DJIKeyedValue?) in
      if let velocity = newValue?.value as? DJISDKVector3D {
        let x = velocity.x
        let y = velocity.y
        let z = velocity.z
        EventSender.sendReactEvent(type: "aircraftVelocity", value: [
          "x": x,
          "y": y,
          "z": z,
          ])
      }
    }
  }
  
  @objc(startAircraftCompassHeadingListener:reject:)
  func startAircraftCompassHeadingListener(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
    let key = DJIFlightControllerKey(param: DJIFlightControllerParamCompassHeading)!
    resolve(nil)
    self.startKeyListener(key: key) { (oldValue: DJIKeyedValue?, newValue: DJIKeyedValue?) in
      if let heading = newValue?.doubleValue {
        EventSender.sendReactEvent(type: "aircraftCompassHeading", value: [
          "heading": heading,
          ])
      }
    }
  }
  
  @objc(stopKeyListener:resolve:reject:)
  func stopKeyListener(keyString: String, resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
    let validKeyInfo = self.implementedKeys.first { $0.key == keyString }
    if (validKeyInfo != nil) {
      let validKeyValues = validKeyInfo!.value
      let keyParamString = validKeyValues.first as! String
      let keyType = String(describing: type(of: validKeyValues.last!))
      
      var key: DJIKey
      
      switch keyType {
      case String(describing: type(of: DJIBatteryKey())) :
        key = DJIBatteryKey.init(param: keyParamString)!
        
      case String(describing: type(of: DJICameraKey())):
        key = DJICameraKey.init(param: keyParamString)!
        
      case String(describing: type(of: DJIFlightControllerKey())):
        key = DJIFlightControllerKey.init(param: keyParamString)!
        
      case String(describing: type(of: DJIPayloadKey())):
        key = DJIPayloadKey.init(param: keyParamString)!
        
      case String(describing: type(of: DJIGimbalKey())):
        key = DJIGimbalKey.init(param: keyParamString)!
        
      case String(describing: type(of: DJIProductKey())):
        key = DJIProductKey.init(param: keyParamString)!
        
      case String(describing: type(of: DJIRemoteControllerKey())):
        key = DJIRemoteControllerKey.init(param: keyParamString)!
        
      case String(describing: type(of: DJIHandheldControllerKey())):
        key = DJIHandheldControllerKey.init(param: keyParamString)!
        
      case String(describing: type(of: DJIMissionKey())):
        key = DJIMissionKey.init(param: keyParamString)!
        
      case String(describing: type(of: DJIAirLinkKey())):
        key = DJIAirLinkKey.init(param: keyParamString)!
        
      case String(describing: type(of: DJIAccessoryKey())):
        key = DJIAccessoryKey.init(param: keyParamString)!
        
      case String(describing: type(of: DJIFlightControllerKey())):
        key = DJIFlightControllerKey.init(param: keyParamString)!
        
      default:
        reject("Invalid Key", nil, nil)
        return
      }
      
      
      
      DJISDKManager.keyManager()?.stopListening(on: key, ofListener: self)
      self.keyListeners.removeAll(where: { $0 == keyString })
      resolve(nil)
    }
  }
  
  func startKeyListener(key: DJIKey, updateBlock: @escaping DJIKeyedListenerUpdateBlock) {
    let existingKeyIndex = self.keyListeners.firstIndex(of: key.param!)
    if (existingKeyIndex == nil) {
      self.keyListeners.append(key.param!)
      DJISDKManager.keyManager()?.startListeningForChanges(on: key, withListener: self, andUpdate: updateBlock)
    } else {
      // If there is an existing listener, don't create a new one
      return
    }
    
  }
  
  func sendReject(_ reject: RCTPromiseRejectBlock,
                  _ code: String,
                  _ error: NSError?
    ) {
    if (error != nil) {
      reject(
        code,
        error!.localizedDescription,
        error!
      )
    } else {
      reject(
        code,
        code,
        nil
      )
    }
  }
  
  @objc static func requiresMainQueueSetup() -> Bool {
    return true
  }
}

class RealTimeDataRecorder: NSObject {
  
}
