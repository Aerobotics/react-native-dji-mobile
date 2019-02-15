//
//  DJISDK.swift
//  ReactNativeDjiMobile
//
//  Created by Adam Rosendorff on 2019/01/29.
//  Copyright © 2019 Facebook. All rights reserved.

import Foundation
import DJISDK

@objc(DJIMobile)
class DJIMobile: RCTEventEmitter {
  
  // This allows us to use a single function to stop key listeners, as it can find the key string & key type from here
  let implementedKeys: [String: [Any]] = [
    "DJIParamConnection": [DJIParamConnection, DJIProductKey()],
    "DJIBatteryParamChargeRemainingInPercent": [DJIBatteryParamChargeRemainingInPercent, DJIBatteryKey()],
    "DJIFlightControllerParamAircraftLocation": [DJIFlightControllerParamAircraftLocation, DJIFlightControllerKey()],
    ]
  
  var keyListeners: [String] = []
  
  @objc(registerApp:reject:)
  func registerApp(resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    var sentRegistration = false
    DJISDKManager.startListeningOnRegistrationUpdates(withListener: self) { (registered, registrationError) in
      if (sentRegistration) {
        DJISDKManager.stopListening(onRegistrationUpdatesOfListener: self)
      } else {
        if (registrationError != nil) {
          reject("Registration Error", registrationError.localizedDescription, nil)
          sentRegistration = true
        } else if (registered == true) {
          sentRegistration = true
          DispatchQueue.main.asyncAfter(deadline: .now() + 1, execute: {
            DJISDKManager.startConnectionToProduct()
            resolve(nil)
          })
        }
      }
    }
    DJISDKManager.beginAppRegistration()
  }
  
  @objc(startProductConnectionListener:reject:)
  func startProductConnectionListener(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
    let key = DJIProductKey(param: DJIParamConnection)!
    self.startKeyListener(key: key) { (oldValue: DJIKeyedValue?, newValue: DJIKeyedValue?) in
      if let connected = newValue?.boolValue {
        self.sendKeyEvent(type: "connectionStatus", value: connected ? "connected" : "disconnected")
      }
    }
    resolve(nil)
  }
  
  @objc(startBatteryPercentChargeRemainingListener:reject:)
  func startBatteryPercentChargeRemainingListener(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
    let key = DJIBatteryKey(param: DJIBatteryParamChargeRemainingInPercent)!
    self.startKeyListener(key: key) { (oldValue: DJIKeyedValue?, newValue: DJIKeyedValue?) in
      if let chargePercent = newValue?.integerValue {
        self.sendKeyEvent(type: "chargeRemaining", value: chargePercent)
      }
    }
    resolve(nil)
  }
  
  @objc(startAircraftLocationListener:reject:)
  func startAircraftLocationListener(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
    let key = DJIFlightControllerKey(param: DJIFlightControllerParamAircraftLocation)!
    self.startKeyListener(key: key) { (oldValue: DJIKeyedValue?, newValue: DJIKeyedValue?) in
      if let location = newValue?.value as? CLLocation {
        let longitude = location.coordinate.longitude
        let latitude = location.coordinate.latitude
        let altitude = location.altitude
        self.sendKeyEvent(type: "aircraftLocation", value: [
          "longitude": longitude,
          "latitude": latitude,
          "altitude": altitude,
          ])
      }
    }
    resolve(nil)
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
  
  //  func stopKeyListener(key: DJIKey) {
  //    self.keyListeners.removeAll(where: { $0 == key.param! })
  //    DJISDKManager.keyManager()?.stopListening(on: key, ofListener: self)
  //  }
  
  func sendKeyEvent(type: String, value: Any) {
    self.sendEvent(withName: "DJIEvent", body: [
      "type": type,
      "value": value,
      ])
  }
  
  override func supportedEvents() -> [String]! {
    return ["DJIEvent"]
  }
  
  //  override func constantsToExport() -> [AnyHashable : Any]! {
  //    var keyStrings: [String] = []
  //    for (key, _) in self.implementedKeys {
  //      keyStrings.append(key)
  //    }
  //    return ["keys": keyStrings]
  //  }
  
  override static func requiresMainQueueSetup() -> Bool {
    return true
  }
}
