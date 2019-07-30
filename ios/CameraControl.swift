//
//  CameraControl.swift
//  ReactNativeDjiMobile
//
//  Created by Adam Rosendorff on 2019/06/11.
//  Copyright © 2019 Aerobotics. All rights reserved.
//

import Foundation
import DJISDK

@objc(CameraControlNative)
class CameraControlNative: NSObject {
  
  @objc(setPhotoAspectRatio:resolve:reject:)
  func setPhotoAspectRatio(photoAspectRatio: UInt, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    DJISDKManager.keyManager()?.setValue(photoAspectRatio, for: DJICameraKey(param: DJICameraParamPhotoAspectRatio)!, withCompletion: { (error: Error?) in
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
    
    DJISDKManager.keyManager()?.setValue(whiteBalance, for: DJICameraKey(param: DJICameraParamWhiteBalance)!, withCompletion: { (error: Error?) in
      if (error == nil) {
        resolve("CameraControl: White balance set successfully")
      } else {
        reject("CameraControl: White balance error", error?.localizedDescription, error)
      }
    })
  }
  
  @objc(setExposureMode:resolve:reject:)
  func setExposureMode(exposureModeIndex: UInt, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    DJISDKManager.keyManager()?.setValue(exposureModeIndex, for: DJICameraKey(param: DJICameraParamExposureMode)!, withCompletion: { (error: Error?) in
      if (error == nil) {
        resolve("CameraControl: Exposure mode set successfully")
      } else {
        reject("CameraControl: Exposure mode error", error?.localizedDescription, error)
      }
    })
  }
  
  @objc(setVideoFileFormat:resolve:reject:)
  func setVideoFileFormat(fileFormatIndex: UInt, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    DJISDKManager.keyManager()?.setValue(fileFormatIndex, for: DJICameraKey(param: DJICameraParamVideoFileFormat)!, withCompletion: { (error: Error?) in
      if (error == nil) {
        resolve("CameraControl: Video file format set successfully")
      } else {
        reject("CameraControl: Video file format error", error?.localizedDescription, error)
      }
    })
  }
  
  @objc(setVideoFileCompressionStandard:resolve:reject:)
  func setVideoFileCompressionStandard(compressionStandardIndex: UInt, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    DJISDKManager.keyManager()?.setValue(compressionStandardIndex, for: DJICameraKey(param: DJICameraParamVideoFileCompressionStandard)!, withCompletion: { (error: Error?) in
      if (error == nil) {
        resolve("CameraControl: Video file compression standard set successfully")
      } else {
        reject("CameraControl: Video file compression standard error", error?.localizedDescription, error)
      }
    })
  }
  
  @objc static func requiresMainQueueSetup() -> Bool {
    return true
  }
  
}
