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
  
  @objc(setVideoResolutionAndFrameRate:frameRate:resolve:reject:)
  func setVideoResolutionAndFrameRate(resolution: UInt, frameRate: UInt, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    let resolutionAndFramerate = DJICameraVideoResolutionAndFrameRate(resolution: DJICameraVideoResolution(rawValue: resolution)!, frameRate: DJICameraVideoFrameRate(rawValue: frameRate)!)
    DJISDKManager.keyManager()?.setValue(resolutionAndFramerate, for: DJICameraKey(param: DJICameraParamVideoResolutionAndFrameRate)!, withCompletion: { (error: Error?) in
      if (error == nil) {
        resolve("CameraControl: Set video resolution & framerate successfully")
      } else {
        reject("CameraControl: Set video resolution & framerate error", error?.localizedDescription, error)
      }
    })
  }
  
  @objc static func requiresMainQueueSetup() -> Bool {
    return true
  }
  
  @objc func constantsToExport() -> [String: Any] {
    return [
      "AspectRatios": [
        "4_3": DJICameraPhotoAspectRatio.ratio4_3.rawValue,
        "16_9":  DJICameraPhotoAspectRatio.ratio16_9.rawValue,
        "3_2":  DJICameraPhotoAspectRatio.ratio3_2.rawValue,
        "unknown":  DJICameraPhotoAspectRatio.ratioUnknown.rawValue,
      ],
      
      "WhiteBalancePresets": [
        "auto": DJICameraWhiteBalancePreset.auto.rawValue,
        "sunny": DJICameraWhiteBalancePreset.sunny.rawValue,
        "cloudy": DJICameraWhiteBalancePreset.cloudy.rawValue,
        "waterSurface": DJICameraWhiteBalancePreset.waterSurface.rawValue,
        "indoorIncandescent": DJICameraWhiteBalancePreset.indoorIncandescent.rawValue,
        "indoorFluorescent": DJICameraWhiteBalancePreset.indoorFluorescent.rawValue,
        "custom": DJICameraWhiteBalancePreset.custom.rawValue,
        "unknown": DJICameraWhiteBalancePreset.unknown.rawValue,
        "neutral": DJICameraWhiteBalancePreset.neutral.rawValue,
      ],
      
      "ExposureModes": [
        "program": DJICameraExposureMode.program.rawValue,
        "shutterPriority": DJICameraExposureMode.shutterPriority.rawValue,
        "aperturePriority": DJICameraExposureMode.aperturePriority.rawValue,
        "manual": DJICameraExposureMode.manual.rawValue,
        "unknown": DJICameraExposureMode.unknown.rawValue,
      ],
      
      "VideoFileFormats": [
        "mov": DJICameraVideoFileFormat.MOV.rawValue,
        "mp4": DJICameraVideoFileFormat.MP4.rawValue,
        "tiffSequence": DJICameraVideoFileFormat.tiffSequence.rawValue,
        "seq": DJICameraVideoFileFormat.SEQ.rawValue,
        "unknown": DJICameraVideoFileFormat.unknown.rawValue,
      ],
      
      "VideoFileCompressionStandards": [
        "h264": DJIVideoFileCompressionStandard.H264.rawValue,
        "h265": DJIVideoFileCompressionStandard.H265.rawValue,
        "unknown": DJIVideoFileCompressionStandard.unknown.rawValue,
      ],
      
      "VideoResolutions": [
        "336x256": DJICameraVideoResolution.resolution336x256.rawValue,
        "640x360": DJICameraVideoResolution.resolution640x360.rawValue,
        "640x480": DJICameraVideoResolution.resolution640x480.rawValue,
        "640x512": DJICameraVideoResolution.resolution640x512.rawValue,
        "1280x720": DJICameraVideoResolution.resolution1280x720.rawValue,
        "1920x1080": DJICameraVideoResolution.resolution1920x1080.rawValue,
        "2704x1520": DJICameraVideoResolution.resolution2704x1520.rawValue,
        "2720x1530": DJICameraVideoResolution.resolution2720x1530.rawValue,
        "3712x2088": DJICameraVideoResolution.resolution3712x2088.rawValue,
        "3840x1572": DJICameraVideoResolution.resolution3840x1572.rawValue,
        "3840x2160": DJICameraVideoResolution.resolution3840x2160.rawValue,
        "3944x2088": DJICameraVideoResolution.resolution3944x2088.rawValue,
        "4096x2160": DJICameraVideoResolution.resolution4096x2160.rawValue,
        "4608x2160": DJICameraVideoResolution.resolution4608x2160.rawValue,
        "4608x2592": DJICameraVideoResolution.resolution4608x2592.rawValue,
        "5280x2160": DJICameraVideoResolution.resolution5280x2160.rawValue,
        "5760x3240": DJICameraVideoResolution.resolution5760x3240.rawValue,
        "6016x3200": DJICameraVideoResolution.resolution6016x3200.rawValue,
        "max": DJICameraVideoResolution.resolutionMax.rawValue,
        "noSsdVideo": DJICameraVideoResolution.resolutionNoSSDVideo.rawValue,
        "2048x1080": DJICameraVideoResolution.resolution2048x1080.rawValue,
        "2688x1512": DJICameraVideoResolution.resolution2688x1512.rawValue,
        "5280x2927": DJICameraVideoResolution.resolution5280x2972.rawValue,
        "unknown": DJICameraVideoResolution.resolutionUnknown.rawValue,
      ],
      
      "VideoFrameRates": [
        "23.976": DJICameraVideoFrameRate.rate23dot976FPS.rawValue,
        "24": DJICameraVideoFrameRate.rate24FPS.rawValue,
        "25": DJICameraVideoFrameRate.rate25FPS.rawValue,
        "29.970": DJICameraVideoFrameRate.rate29dot970FPS.rawValue,
        "30": DJICameraVideoFrameRate.rate30FPS.rawValue,
        "47.950": DJICameraVideoFrameRate.rate47dot950FPS.rawValue,
        "48": DJICameraVideoFrameRate.rate48FPS.rawValue,
        "50": DJICameraVideoFrameRate.rate50FPS.rawValue,
        "59.940": DJICameraVideoFrameRate.rate59dot940FPS.rawValue,
        "60": DJICameraVideoFrameRate.rate60FPS.rawValue,
        "90": DJICameraVideoFrameRate.rate90FPS.rawValue,
        "96": DJICameraVideoFrameRate.rate96FPS.rawValue,
        "100": DJICameraVideoFrameRate.rate100FPS.rawValue,
        "120": DJICameraVideoFrameRate.rate120FPS.rawValue,
        "8.7": DJICameraVideoFrameRate.rate8dot7FPS.rawValue,
        "unknown": DJICameraVideoFrameRate.rateUnknown.rawValue,
      ],
      
    ]
  }
  
}
