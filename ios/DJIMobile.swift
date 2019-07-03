//
//  DJISDK.swift
//  ReactNativeDjiMobile
//
//  Created by Adam Rosendorff on 2019/01/29.

import Foundation
import DJISDK

@objc(DJIMobile)
class DJIMobile: NSObject, RCTInvalidating {
  
  var cameraDelegateEventSender: DJICameraDelegateSender?
  //  var productConnectionListener
  
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
          self.cameraDelegateEventSender = DJICameraDelegateSender()
          
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
  
  @objc(limitEventFrequency:resolve:reject:)
  func limitEventFrequency(frequency: NSNumber, resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
    EventSender.limitEventSendFrequency(frequency: frequency.intValue)
    resolve("limitEventFrequency Successful")
  }
  
//  @objc(startRecordRealTimeData:resolve:reject:)
//  func startRecordRealTimeData(fileName: String, resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
//    self.realTimeDataLogger.startLogging(fileName: fileName) { (error: Error?) in
//      if (error != nil) {
//        self.sendReject(reject, "startRecordRealTimeData Error", nil)
//        return
//      } else {
//        resolve("startRecordRealTimeData Successful")
//        return
//      }
//    }
//  }
//
//  @objc(stopRecordRealTimeData:reject:)
//  func stopRecordRealTimeData(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
//    self.realTimeDataLogger.stopLogging { (error: Error?) in
//      if (error != nil) {
//        self.sendReject(reject, "stopRecordRealTimeData Error", nil)
//        return
//      } else {
//        resolve("stopRecordRealTimeData Successful")
//        return
//      }
//    }
//  }
  
  @objc(startProductConnectionListener:reject:)
  func startProductConnectionListener(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
    let key = DJIProductKey(param: DJIParamConnection)!
    resolve(nil)
    self.startKeyListener(key) { (oldValue: DJIKeyedValue?, newValue: DJIKeyedValue?) in
      if let connected = newValue?.boolValue {
        EventSender.sendReactEvent(type: "ProductConnection", value: connected ? "connected" : "disconnected")
      }
    }
  }
  
  @objc(startBatteryPercentChargeRemainingListener:reject:)
  func startBatteryPercentChargeRemainingListener(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
    let key = DJIBatteryKey(param: DJIBatteryParamChargeRemainingInPercent)!
    resolve(nil)
    self.startKeyListener(key) { (oldValue: DJIKeyedValue?, newValue: DJIKeyedValue?) in
      if let chargePercent = newValue?.integerValue {
        EventSender.sendReactEvent(type: "BatteryChargeRemaining", value: chargePercent)
      }
    }
  }
  
  @objc(startAircraftLocationListener:reject:)
  func startAircraftLocationListener(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
    let key = DJIFlightControllerKey(param: DJIFlightControllerParamAircraftLocation)!
    resolve(nil)
    self.startKeyListener(key) { (oldValue: DJIKeyedValue?, newValue: DJIKeyedValue?) in
      if let location = newValue?.value as? CLLocation {
        let longitude = location.coordinate.longitude
        let latitude = location.coordinate.latitude
        let altitude = location.altitude
        EventSender.sendReactEvent(type: "AircraftLocation", value: [
          "longitude": longitude,
          "latitude": latitude,
          "altitude": altitude,
          ])
      }
    }
  }
  
  @objc(getAircraftLocation:reject:)
  func getAircraftLocation(resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    let key = DJIFlightControllerKey(param: DJIFlightControllerParamAircraftLocation)!
    self.getKeyValue(key) { (value: DJIKeyedValue?, error: Error?) in
      if (error != nil) {
        self.sendReject(reject, "getAircraftLocation Error", error! as NSError)
      } else {
        if let location = value?.value as? CLLocation {
          let longitude = location.coordinate.longitude
          let latitude = location.coordinate.latitude
          let altitude = location.altitude
          resolve([
            "longitude": longitude,
            "latitude": latitude,
            "altitude": altitude,
            ])
        }
      }
    }
    
  }
  
  @objc(startAircraftVelocityListener:reject:)
  func startAircraftVelocityListener(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
    let key = DJIFlightControllerKey(param: DJIFlightControllerParamVelocity)!
    resolve(nil)
    self.startKeyListener(key) { (oldValue: DJIKeyedValue?, newValue: DJIKeyedValue?) in
      if let velocity = newValue?.value as? DJISDKVector3D {
        let x = velocity.x
        let y = velocity.y
        let z = velocity.z
        EventSender.sendReactEvent(type: "AircraftVelocity", value: [
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
    self.startKeyListener(key) { (oldValue: DJIKeyedValue?, newValue: DJIKeyedValue?) in
      if let heading = newValue?.doubleValue {
        EventSender.sendReactEvent(type: "AircraftCompassHeading", value: [
          "heading": heading,
          ])
      }
    }
  }
  
  @objc(startNewMediaFileListener:reject:)
  func startNewMediaFileListener(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
    NotificationCenter.default.addObserver(self, selector: #selector(newMediaFileUpdate), name: CameraEvent.didGenerateNewMediaFile.notification, object: nil)
    resolve("startNewMediaFileListener")
  }
  
  @objc private func newMediaFileUpdate(payload: NSNotification) {
    let newMedia = payload.userInfo!["value"] as! DJIMediaFile
    EventSender.sendReactEvent(type: "CameraDidGenerateNewMediaFile", value: [
      "fileName": newMedia.fileName
      ])
  }
  
  @objc(stopNotificationCenterListener:resolve:reject:)
  func stopNotificationServiceListener(name: String, resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
    NotificationCenter.default.removeObserver(self, name: NSNotification.Name(name), object: nil)
    resolve("stopNotificationCenterListener")
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
  
  private func startKeyListener(_ key: DJIKey, updateBlock: @escaping DJIKeyedListenerUpdateBlock) {
    let existingKeyIndex = self.keyListeners.firstIndex(of: key.param!)
    if (existingKeyIndex == nil) {
      self.keyListeners.append(key.param!)
      DJISDKManager.keyManager()?.startListeningForChanges(on: key, withListener: self, andUpdate: updateBlock)
    } else {
      // If there is an existing listener, don't create a new one
      return
    }
    
  }
  
  private func getKeyValue(_ key: DJIKey, updateBlock: @escaping DJIKeyedGetCompletionBlock) {
    DJISDKManager.keyManager()?.getValueFor(key, withCompletion: updateBlock)
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
