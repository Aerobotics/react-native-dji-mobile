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
  
  private var cameraParameters = [
    "AspectRatios": [
      "RATIO_4_3": DJICameraPhotoAspectRatio.ratio4_3.rawValue,
      "RATIO_16_9":  DJICameraPhotoAspectRatio.ratio16_9.rawValue,
      "RATIO_3_2":  DJICameraPhotoAspectRatio.ratio3_2.rawValue,
      "UNKNOWN":  DJICameraPhotoAspectRatio.ratioUnknown.rawValue,
    ],
    
    "WhiteBalancePresets": [
      "AUTO": DJICameraWhiteBalancePreset.auto.rawValue,
      "SUNNY": DJICameraWhiteBalancePreset.sunny.rawValue,
      "CLOUDY": DJICameraWhiteBalancePreset.cloudy.rawValue,
      "WATER_SURFACE": DJICameraWhiteBalancePreset.waterSurface.rawValue,
      "INDOOR_INCANDESCENT": DJICameraWhiteBalancePreset.indoorIncandescent.rawValue,
      "INDOOR_FLUORESCENT": DJICameraWhiteBalancePreset.indoorFluorescent.rawValue,
      "CUSTOM": DJICameraWhiteBalancePreset.custom.rawValue,
      "UNKNOWN": DJICameraWhiteBalancePreset.unknown.rawValue,
      "PRESET_NEUTRAL": DJICameraWhiteBalancePreset.neutral.rawValue,
    ],
    
    "ExposureModes": [
      "PROGRAM": DJICameraExposureMode.program.rawValue,
      "SHUTTER_PRIORITY": DJICameraExposureMode.shutterPriority.rawValue,
      "APERTURE_PRIORITY": DJICameraExposureMode.aperturePriority.rawValue,
      "MANUAL": DJICameraExposureMode.manual.rawValue,
      "UNKNOWN": DJICameraExposureMode.unknown.rawValue,
    ],
    
    "VideoFileFormats": [
      "MOV": DJICameraVideoFileFormat.MOV.rawValue,
      "MP4": DJICameraVideoFileFormat.MP4.rawValue,
      "TIFF_SEQ": DJICameraVideoFileFormat.tiffSequence.rawValue,
      "SEQ": DJICameraVideoFileFormat.SEQ.rawValue,
      "UNKNOWN": DJICameraVideoFileFormat.unknown.rawValue,
    ],
    
    "VideoFileCompressionStandards": [
      "H264": DJIVideoFileCompressionStandard.H264.rawValue,
      "H265": DJIVideoFileCompressionStandard.H265.rawValue,
      "Unknown": DJIVideoFileCompressionStandard.unknown.rawValue,
    ],
    
    "VideoResolutions": [
      "RESOLUTION_336x256": DJICameraVideoResolution.resolution336x256.rawValue,
      "RESOLUTION_640x360": DJICameraVideoResolution.resolution640x360.rawValue,
      "RESOLUTION_640x480": DJICameraVideoResolution.resolution640x480.rawValue,
      "RESOLUTION_640x512": DJICameraVideoResolution.resolution640x512.rawValue,
      "RESOLUTION_1280x720": DJICameraVideoResolution.resolution1280x720.rawValue,
      "RESOLUTION_1920x1080": DJICameraVideoResolution.resolution1920x1080.rawValue,
      "RESOLUTION_2704x1520": DJICameraVideoResolution.resolution2704x1520.rawValue,
      "RESOLUTION_2720x1530": DJICameraVideoResolution.resolution2720x1530.rawValue,
      "RESOLUTION_3712x2088": DJICameraVideoResolution.resolution3712x2088.rawValue,
      "RESOLUTION_3840x1572": DJICameraVideoResolution.resolution3840x1572.rawValue,
      "RESOLUTION_3840x2160": DJICameraVideoResolution.resolution3840x2160.rawValue,
      "RESOLUTION_3944x2088": DJICameraVideoResolution.resolution3944x2088.rawValue,
      "RESOLUTION_4096x2160": DJICameraVideoResolution.resolution4096x2160.rawValue,
      "RESOLUTION_4608x2160": DJICameraVideoResolution.resolution4608x2160.rawValue,
      "RESOLUTION_4608x2592": DJICameraVideoResolution.resolution4608x2592.rawValue,
      "RESOLUTION_5280x2160": DJICameraVideoResolution.resolution5280x2160.rawValue,
      "RESOLUTION_5760x3240": DJICameraVideoResolution.resolution5760x3240.rawValue,
      "RESOLUTION_6016x3200": DJICameraVideoResolution.resolution6016x3200.rawValue,
      "RESOLUTION_MAX": DJICameraVideoResolution.resolutionMax.rawValue,
      "NO_SSD_VIDEO": DJICameraVideoResolution.resolutionNoSSDVideo.rawValue,
      "RESOLUTION_2048x1080": DJICameraVideoResolution.resolution2048x1080.rawValue,
      "RESOLUTION_2688x1512": DJICameraVideoResolution.resolution2688x1512.rawValue,
      "RESOLUTION_5280x2927": DJICameraVideoResolution.resolution5280x2972.rawValue,
      "UNKNOWN": DJICameraVideoResolution.resolutionUnknown.rawValue,
    ],
    
    "VideoFrameRates": [
      "FRAME_RATE_23_DOT_976_FPS": DJICameraVideoFrameRate.rate23dot976FPS.rawValue,
      "FRAME_RATE_24_FPS": DJICameraVideoFrameRate.rate24FPS.rawValue,
      "FRAME_RATE_25_FPS": DJICameraVideoFrameRate.rate25FPS.rawValue,
      "FRAME_RATE_29_DOT_970_FPS": DJICameraVideoFrameRate.rate29dot970FPS.rawValue,
      "FRAME_RATE_30_FPS": DJICameraVideoFrameRate.rate30FPS.rawValue,
      "FRAME_RATE_47_DOT_950_FPS": DJICameraVideoFrameRate.rate47dot950FPS.rawValue,
      "FRAME_RATE_48_FPS": DJICameraVideoFrameRate.rate48FPS.rawValue,
      "FRAME_RATE_50_FPS": DJICameraVideoFrameRate.rate50FPS.rawValue,
      "FRAME_RATE_59_DOT_940_FPS": DJICameraVideoFrameRate.rate59dot940FPS.rawValue,
      "FRAME_RATE_60_FPS": DJICameraVideoFrameRate.rate60FPS.rawValue,
      "FRAME_RATE_90_FPS": DJICameraVideoFrameRate.rate90FPS.rawValue,
      "FRAME_RATE_96_FPS": DJICameraVideoFrameRate.rate96FPS.rawValue,
      "FRAME_RATE_100_FPS": DJICameraVideoFrameRate.rate100FPS.rawValue,
      "FRAME_RATE_120_FPS": DJICameraVideoFrameRate.rate120FPS.rawValue,
      "FRAME_RATE_8_DOT_7_FPS": DJICameraVideoFrameRate.rate8dot7FPS.rawValue,
      "UNKNOWN": DJICameraVideoFrameRate.rateUnknown.rawValue,
    ],
    
    ]
  
  @objc(setPhotoAspectRatio:resolve:reject:)
  func setPhotoAspectRatio(photoAspectRatio: String, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    DJISDKManager.keyManager()?.setValue(cameraParameters["AspectRatios"]![photoAspectRatio]!, for: DJICameraKey(param: DJICameraParamPhotoAspectRatio)!, withCompletion: { (error: Error?) in
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
    if let preset = parameters["preset"] as? String {
      whiteBalance = DJICameraWhiteBalance.init(preset: DJICameraWhiteBalancePreset.init(rawValue: cameraParameters["WhiteBalancePresets"]![preset]!)!)
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
  func setExposureMode(exposureMode: String, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    DJISDKManager.keyManager()?.setValue(cameraParameters["ExposureModes"]![exposureMode]!, for: DJICameraKey(param: DJICameraParamExposureMode)!, withCompletion: { (error: Error?) in
      if (error == nil) {
        resolve("CameraControl: Exposure mode set successfully")
      } else {
        reject("CameraControl: Exposure mode error", error?.localizedDescription, error)
      }
    })
  }
  
  @objc(setVideoFileFormat:resolve:reject:)
  func setVideoFileFormat(videoFileFormat: String, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    DJISDKManager.keyManager()?.setValue(cameraParameters["VideoFileFormats"]![videoFileFormat]!, for: DJICameraKey(param: DJICameraParamVideoFileFormat)!, withCompletion: { (error: Error?) in
      if (error == nil) {
        resolve("CameraControl: Video file format set successfully")
      } else {
        reject("CameraControl: Video file format error", error?.localizedDescription, error)
      }
    })
  }
  
  @objc(setVideoFileCompressionStandard:resolve:reject:)
  func setVideoFileCompressionStandard(videoCompressionStandard: String, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    DJISDKManager.keyManager()?.setValue(cameraParameters["VideoFileCompressionStandards"]![videoCompressionStandard]!, for: DJICameraKey(param: DJICameraParamVideoFileCompressionStandard)!, withCompletion: { (error: Error?) in
      if (error == nil) {
        resolve("CameraControl: Video file compression standard set successfully")
      } else {
        reject("CameraControl: Video file compression standard error", error?.localizedDescription, error)
      }
    })
  }
  
  @objc(setVideoResolutionAndFrameRate:frameRate:resolve:reject:)
  func setVideoResolutionAndFrameRate(resolution: String, frameRate: String, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    let resolutionAndFramerate = DJICameraVideoResolutionAndFrameRate(resolution: DJICameraVideoResolution(rawValue: cameraParameters["VideoResolutions"]![resolution]!)!, frameRate: DJICameraVideoFrameRate(rawValue: cameraParameters["VideoFrameRates"]![frameRate]!)!)
    DJISDKManager.keyManager()?.setValue(resolutionAndFramerate, for: DJICameraKey(param: DJICameraParamVideoResolutionAndFrameRate)!, withCompletion: { (error: Error?) in
      if (error == nil) {
        resolve("CameraControl: Set video resolution & framerate successfully")
      } else {
        reject("CameraControl: Set video resolution & framerate error", error?.localizedDescription, error)
      }
    })
  }
  
  @objc(stopRecording:reject:)
  func stopRecording(resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    DJISDKManager.keyManager()?.performAction(for: DJICameraKey(param: DJICameraParamStopRecordVideo)!, withArguments: nil, andCompletion: { (finished: Bool, response: DJIKeyedValue?, error: Error?) in
      if (error == nil) {
        resolve(nil)
      } else {
        reject("CameraControl: stop recording failed", nil, error)
      }
    })
  }
  
  @objc static func requiresMainQueueSetup() -> Bool {
    return true
  }
  
  @objc func constantsToExport() -> [String: Any] {
    return cameraParameters
  }
  
}
