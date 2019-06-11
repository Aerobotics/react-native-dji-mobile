//
//  CameraControl.swift
//  ReactNativeDjiMobile
//
//  Created by Adam Rosendorff on 2019/06/11.
//  Copyright © 2019 Aerobotics. All rights reserved.
//

import Foundation
import DJISDK


class CameraControl: NSObject {
  
  @objc(setPhotoAspectRatioreso:resolve:reject:)
  func setPhotoAspectRatio(photoAspectRatio: UInt, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    DJISDKManager.keyManager()?.performAction(for: DJICameraKey(param: DJICameraParamPhotoAspectRatio)!, withArguments: [DJICameraPhotoAspectRatio.init(rawValue: photoAspectRatio)!], andCompletion: { (finished: Bool, value: DJIKeyedValue?, error: Error?) in
      if (error == nil) {
        resolve("CameraControl: Photo aspect ratio set successfully")
      } else {
        reject("CameraControl: Photo aspect ratio error", error?.localizedDescription, error)
      }
    })
  }
  
  @objc(setWhiteBalance:resolve:reject:)
  func setWhiteBalance(parameters: NSDictionary, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    var whiteBalance: DJICameraWhiteBalance?
    if let preset = parameters["preset"] as? UInt {
      whiteBalance = DJICameraWhiteBalance.init(preset:  DJICameraWhiteBalancePreset.init(rawValue: preset)!)
    }
    if let colorTemperature = parameters["colorTemperature"] as? UInt8 {
      whiteBalance = DJICameraWhiteBalance.init(customColorTemperature: colorTemperature)
    }
    
    DJISDKManager.keyManager()?.performAction(for: DJICameraKey(param: DJICameraParamWhiteBalance)!, withArguments: [whiteBalance!], andCompletion: { (finished: Bool, value: DJIKeyedValue?, error: Error?) in
      if (error == nil) {
        resolve("CameraControl: White balance set successfully")
      } else {
        reject("CameraControl: White balance error", error?.localizedDescription, error)
      }
    })
  }
  
  @objc(setExposureMode:resolve:reject:)
  func setExposureMode(exposureMode: UInt, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    DJISDKManager.keyManager()?.performAction(for: DJICameraKey(param: DJICameraParamExposureMode)!, withArguments: [DJICameraExposureMode.init(rawValue: exposureMode)!], andCompletion: { (finished: Bool, value: DJIKeyedValue?, error: Error?) in
      if (error == nil) {
        resolve("CameraControl: Exposure mode set successfully")
      } else {
        reject("CameraControl: Exposure mode error", error?.localizedDescription, error)
      }
    })
  }
  
}
