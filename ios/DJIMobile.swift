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
          DispatchQueue.main.asyncAfter(deadline: .now(), execute: {
            DJISDKManager.startConnectionToProduct()
          })
          
          resolve(nil)
        }
      }
    }
    DJISDKManager.beginAppRegistration()
  }
  
  @objc(startProductConnectionListener:reject:)
  func startProductConnectionListener(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
    let key = DJIProductKey(param: DJIParamConnection)!
    self.startKeyListener(key: key) { (oldValue: DJIKeyedValue?, newValue: DJIKeyedValue?) in
      if (newValue != nil) {
        let connected = (newValue != nil) as Bool
        NSLog("KEYLISTENER: %@", newValue!.stringValue!)
        self.sendKeyEvent(type: "connectionStatus", value: connected ? "connected" : "disconnected")
      }
    }
    resolve(nil)
  }
  
  @objc(stopProductConnectionListener:reject:)
  func stopProductConnectionListener(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
    self.stopKeyListener(key: DJIProductKey(param: DJIParamConnection)!)
    resolve(nil)
  }
  
  func startKeyListener(key: DJIKey, updateBlock: @escaping DJIKeyedListenerUpdateBlock) {
    let existingKeyIndex = self.keyListeners.firstIndex(of: key.param!)
    NSLog("KEYLISTENER INDEX: %d", existingKeyIndex ?? -1);
    if (existingKeyIndex == nil) {
      self.keyListeners.append(key.param!)
      DJISDKManager.keyManager()?.startListeningForChanges(on: key, withListener: self, andUpdate: updateBlock)
    } else {
      // If there is an existing listener, don't create a new one
      return
    }
    
  }
  
  func stopKeyListener(key: DJIKey) {
    self.keyListeners.removeAll(where: { $0 == key.param! })
    DJISDKManager.keyManager()?.stopListening(on: key, ofListener: self)
  }
  
  func sendKeyEvent(type: String, value: Any) {
    NSLog("KEYLISTENER SEND EVENT: %@", type)
    self.sendEvent(withName: "DJIEvent", body: [
      "type": type,
      "value": value,
      ])
  }
  
  override func supportedEvents() -> [String]! {
    return ["DJIEvent"]
  }
}
