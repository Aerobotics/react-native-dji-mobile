//
//  DJISDK.swift
//  ReactNativeDjiMobile
//
//  Created by Adam Rosendorff on 2019/01/29.
//  Copyright © 2019 Facebook. All rights reserved.

import Foundation
import DJISDK

@objc(DJISDKManagerWrapper)
class DJISDKManagerWrapper: NSObject {
  
  @objc(getSDKVersion:reject:)
  func getSDKVersion(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
    resolve(DJISDKManager.sdkVersion())
  }
  
  @objc(registerApp:reject:)
  func registerApp(resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    var sentRegistration = false
    DJISDKManager.startListeningOnRegistrationUpdates(withListener: self) { (registered, registrationError) in
      if (sentRegistration) {
        DJISDKManager.stopListening(onRegistrationUpdatesOfListener: self)
      } else {
        if (registrationError != nil) {
          reject("Register Error", "App unable to register", nil)
          sentRegistration = true
        } else if (registered == true) {
          resolve(nil)
          sentRegistration = true
        }
      }
    }
    DJISDKManager.beginAppRegistration()
  }
  
  @objc(startConnectionToProduct:reject:)
  func startConnectionToProduct(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
    let returnValue = DJISDKManager.startConnectionToProduct()
    NSLog("START CONNECTION TO PRODUCT: %d", returnValue)
    resolve(returnValue)
  }
}
