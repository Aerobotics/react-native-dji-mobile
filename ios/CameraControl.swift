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

    "ISOs": [
      "AUTO": DJICameraISO.isoAuto.rawValue,
      "ISO_100": DJICameraISO.ISO100.rawValue,
      "ISO_200": DJICameraISO.ISO200.rawValue,
      "ISO_400": DJICameraISO.ISO400.rawValue,
      "ISO_800": DJICameraISO.ISO800.rawValue,
      "ISO_1600": DJICameraISO.ISO1600.rawValue,
      "ISO_3200": DJICameraISO.ISO3200.rawValue,
      "ISO_6400": DJICameraISO.ISO6400.rawValue,
      "ISO_12800": DJICameraISO.ISO12800.rawValue,
      "ISO_25600": DJICameraISO.ISO25600.rawValue,
      "FIXED": DJICameraISO.isoFixed.rawValue,
      "UNKNOWN": DJICameraISO.isoUnknown.rawValue,
    ],

    "ShutterSpeeds": [
      "SHUTTER_SPEED_1_8000": DJICameraShutterSpeed.speed1_8000.rawValue,
      "SHUTTER_SPEED_1_6400": DJICameraShutterSpeed.speed1_6400.rawValue,
      "SHUTTER_SPEED_1_6000": DJICameraShutterSpeed.speed1_6000.rawValue,
      "SHUTTER_SPEED_1_5000": DJICameraShutterSpeed.speed1_5000.rawValue,
      "SHUTTER_SPEED_1_4000": DJICameraShutterSpeed.speed1_4000.rawValue,
      "SHUTTER_SPEED_1_3200": DJICameraShutterSpeed.speed1_3200.rawValue,
      "SHUTTER_SPEED_1_3000": DJICameraShutterSpeed.speed1_3000.rawValue,
      "SHUTTER_SPEED_1_2500": DJICameraShutterSpeed.speed1_2500.rawValue,
      "SHUTTER_SPEED_1_2000": DJICameraShutterSpeed.speed1_2000.rawValue,
      "SHUTTER_SPEED_1_1600": DJICameraShutterSpeed.speed1_1600.rawValue,
      "SHUTTER_SPEED_1_1250": DJICameraShutterSpeed.speed1_1250.rawValue,
      "SHUTTER_SPEED_1_1000": DJICameraShutterSpeed.speed1_1000.rawValue,
      "SHUTTER_SPEED_1_800": DJICameraShutterSpeed.speed1_800.rawValue,
      "SHUTTER_SPEED_1_725": DJICameraShutterSpeed.speed1_725.rawValue,
      "SHUTTER_SPEED_1_640": DJICameraShutterSpeed.speed1_640.rawValue,
      "SHUTTER_SPEED_1_500": DJICameraShutterSpeed.speed1_500.rawValue,
      "SHUTTER_SPEED_1_400": DJICameraShutterSpeed.speed1_400.rawValue,
      "SHUTTER_SPEED_1_350": DJICameraShutterSpeed.speed1_350.rawValue,
      "SHUTTER_SPEED_1_320": DJICameraShutterSpeed.speed1_320.rawValue,
      "SHUTTER_SPEED_1_250": DJICameraShutterSpeed.speed1_250.rawValue,
      "SHUTTER_SPEED_1_240": DJICameraShutterSpeed.speed1_240.rawValue,
      "SHUTTER_SPEED_1_200": DJICameraShutterSpeed.speed1_200.rawValue,
      "SHUTTER_SPEED_1_180": DJICameraShutterSpeed.speed1_180.rawValue,
      "SHUTTER_SPEED_1_160": DJICameraShutterSpeed.speed1_160.rawValue,
      "SHUTTER_SPEED_1_125": DJICameraShutterSpeed.speed1_125.rawValue,
      "SHUTTER_SPEED_1_120": DJICameraShutterSpeed.speed1_120.rawValue,
      "SHUTTER_SPEED_1_100": DJICameraShutterSpeed.speed1_100.rawValue,
      "SHUTTER_SPEED_1_90": DJICameraShutterSpeed.speed1_90.rawValue,
      "SHUTTER_SPEED_1_80": DJICameraShutterSpeed.speed1_80.rawValue,
      "SHUTTER_SPEED_1_60": DJICameraShutterSpeed.speed1_60.rawValue,
      "SHUTTER_SPEED_1_50": DJICameraShutterSpeed.speed1_50.rawValue,
      "SHUTTER_SPEED_1_40": DJICameraShutterSpeed.speed1_40.rawValue,
      "SHUTTER_SPEED_1_30": DJICameraShutterSpeed.speed1_30.rawValue,
      "SHUTTER_SPEED_1_25": DJICameraShutterSpeed.speed1_25.rawValue,
      "SHUTTER_SPEED_1_20": DJICameraShutterSpeed.speed1_20.rawValue,
      "SHUTTER_SPEED_1_15": DJICameraShutterSpeed.speed1_15.rawValue,
      "SHUTTER_SPEED_1_12_DOT_5": DJICameraShutterSpeed.speed1_12Dot5.rawValue,
      "SHUTTER_SPEED_1_10": DJICameraShutterSpeed.speed1_10.rawValue,
      "SHUTTER_SPEED_1_8": DJICameraShutterSpeed.speed1_8.rawValue,
      "SHUTTER_SPEED_1_6_DOT_25": DJICameraShutterSpeed.speed1_6Dot25.rawValue,
      "SHUTTER_SPEED_1_5": DJICameraShutterSpeed.speed1_5.rawValue,
      "SHUTTER_SPEED_1_4": DJICameraShutterSpeed.speed1_4.rawValue,
      "SHUTTER_SPEED_1_3": DJICameraShutterSpeed.speed1_3.rawValue,
      "SHUTTER_SPEED_1_2_DOT_5": DJICameraShutterSpeed.speed1_2Dot5.rawValue,
      "SHUTTER_SPEED_1_2": DJICameraShutterSpeed.speed1_2.rawValue,
      "SHUTTER_SPEED_1_1_DOT_67": DJICameraShutterSpeed.speed1_1Dot67.rawValue,
      "SHUTTER_SPEED_1_1_DOT_25": DJICameraShutterSpeed.speed1_1Dot25.rawValue,
      "SHUTTER_SPEED_1": DJICameraShutterSpeed.speed1.rawValue,
      "SHUTTER_SPEED_1_DOT_3": DJICameraShutterSpeed.speed1Dot3.rawValue,
      "SHUTTER_SPEED_1_DOT_6": DJICameraShutterSpeed.speed1Dot6.rawValue,
      "SHUTTER_SPEED_2": DJICameraShutterSpeed.speed2.rawValue,
      "SHUTTER_SPEED_2_DOT_5": DJICameraShutterSpeed.speed2Dot5.rawValue,
      "SHUTTER_SPEED_3": DJICameraShutterSpeed.speed3.rawValue,
      "SHUTTER_SPEED_3_DOT_2": DJICameraShutterSpeed.speed3Dot2.rawValue,
      "SHUTTER_SPEED_4": DJICameraShutterSpeed.speed4.rawValue,
      "SHUTTER_SPEED_5": DJICameraShutterSpeed.speed5.rawValue,
      "SHUTTER_SPEED_6": DJICameraShutterSpeed.speed6.rawValue,
      "SHUTTER_SPEED_7": DJICameraShutterSpeed.speed7.rawValue,
      "SHUTTER_SPEED_8": DJICameraShutterSpeed.speed8.rawValue,
      "SHUTTER_SPEED_9": DJICameraShutterSpeed.speed9.rawValue,
      "SHUTTER_SPEED_10": DJICameraShutterSpeed.speed10.rawValue,
      "SHUTTER_SPEED_13": DJICameraShutterSpeed.speed13.rawValue,
      "SHUTTER_SPEED_15": DJICameraShutterSpeed.speed15.rawValue,
      "SHUTTER_SPEED_20": DJICameraShutterSpeed.speed20.rawValue,
      "SHUTTER_SPEED_25": DJICameraShutterSpeed.speed25.rawValue,
      "SHUTTER_SPEED_30": DJICameraShutterSpeed.speed30.rawValue,
      "UNKNOWN": DJICameraShutterSpeed.speedUnknown.rawValue,
    ],

    "CameraModes": [
      "SHOOT_PHOTO": DJICameraMode.shootPhoto.rawValue,
      "RECORD_VIDEO": DJICameraMode.recordVideo.rawValue,
      "PLAYBACK": DJICameraMode.playback.rawValue,
      "MEDIA_DOWNLOAD": DJICameraMode.mediaDownload.rawValue,
    ],

  ]

  @objc(setCameraMode:resolve:reject:)
  func setCameraMode(cameraMode: String, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    guard let cameraModeParam = cameraParameters["CameraModes"]![cameraMode] else {
      reject("CameraControl: Unknown camera mode", "An unknown camera mode of \"\(cameraMode)\" was provided", nil)
      return
    }
    DJISDKManager.keyManager()?.setValue(cameraModeParam, for: DJICameraKey(param: DJICameraParamMode)!, withCompletion: { (error: Error?) in
      if (error == nil) {
        resolve("CameraControl: Camera mode set successfully")
      } else {
        reject("CameraControl: Camera mode error", error?.localizedDescription, error)
      }
    })
  }

  @objc(setPhotoAspectRatio:resolve:reject:)
  func setPhotoAspectRatio(photoAspectRatio: String, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    guard let photoAspectRatioParam = cameraParameters["AspectRatios"]![photoAspectRatio] else {
      reject("CameraControl: Unknown Photo aspect ratio", "An unknown photo aspect ratio of \"\(photoAspectRatio)\" was provided", nil)
      return
    }
    DJISDKManager.keyManager()?.setValue(photoAspectRatioParam, for: DJICameraKey(param: DJICameraParamPhotoAspectRatio)!, withCompletion: { (error: Error?) in
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
      whiteBalance = DJICameraWhiteBalance.init(preset: DJICameraWhiteBalancePreset.init(rawValue: cameraParameters["WhiteBalancePresets"]![preset]! as! UInt)!)
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
    guard let exposureModeParam = cameraParameters["ExposureModes"]![exposureMode] else {
      reject("CameraControl: Unknown exposure mode", "An unknown exposure mode of \"\(exposureMode)\" was provided", nil)
      return
    }
    DJISDKManager.keyManager()?.setValue(exposureModeParam, for: DJICameraKey(param: DJICameraParamExposureMode)!, withCompletion: { (error: Error?) in
      if (error == nil) {
        resolve("CameraControl: Exposure mode set successfully")
      } else {
        reject("CameraControl: Exposure mode error", error?.localizedDescription, error)
      }
    })
  }

  @objc(setISO:resolve:reject:)
  func setISO(iso: String, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    guard let isoParam = cameraParameters["ISOs"]![iso] else {
      reject("CameraControl: Unknown ISO", "An unknown ISO value of of \"\(iso)\" was provided", nil)
      return
    }
    DJISDKManager.keyManager()?.setValue(isoParam, for: DJICameraKey(param: DJICameraParamISO)!, withCompletion: { (error: Error?) in
      if (error == nil) {
        resolve("CameraControl: ISO set successfully")
      } else {
        reject("CameraControl: ISO error", error?.localizedDescription, error)
      }
    })
  }

  @objc(setShutterSpeed:resolve:reject:)
  func setShutterSpeed(shutterSpeed: String, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    guard let shutterSpeedParam = cameraParameters["ShutterSpeeds"]![shutterSpeed] else {
      reject("CameraControl: Unknown shutter speed", "An unknown shutter speed of \"\(shutterSpeed)\" was provided", nil)
      return
    }
    DJISDKManager.keyManager()?.setValue(shutterSpeedParam, for: DJICameraKey(param: DJICameraParamShutterSpeed)!, withCompletion: { (error: Error?) in
      if (error == nil) {
        resolve("CameraControl: Shutter speed set successfully")
      } else {
        reject("CameraControl: Shutter speed error", error?.localizedDescription, error)
      }
    })
  }

  @objc(setVideoFileFormat:resolve:reject:)
  func setVideoFileFormat(videoFileFormat: String, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    guard let videoFileFormatParam = cameraParameters["VideoFileFormats"]![videoFileFormat] else {
      reject("CameraControl: Unknown video file format", "An unknown video file format of \"\(videoFileFormat)\" was provided", nil)
      return
    }
    DJISDKManager.keyManager()?.setValue(videoFileFormatParam, for: DJICameraKey(param: DJICameraParamVideoFileFormat)!, withCompletion: { (error: Error?) in
      if (error == nil) {
        resolve("CameraControl: Video file format set successfully")
      } else {
        reject("CameraControl: Video file format error", error?.localizedDescription, error)
      }
    })
  }

  @objc(setVideoFileCompressionStandard:resolve:reject:)
  func setVideoFileCompressionStandard(videoCompressionStandard: String, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    guard let videoFileCompressionStandardParam = cameraParameters["VideoFileCompressionStandards"]![videoCompressionStandard] else {
      reject("CameraControl: Unknown video file compression standard", "An unknown video file compression standard of \"\(videoCompressionStandard)\" was provided", nil)
      return
    }
    DJISDKManager.keyManager()?.setValue(videoFileCompressionStandardParam, for: DJICameraKey(param: DJICameraParamVideoFileCompressionStandard)!, withCompletion: { (error: Error?) in
      if (error == nil) {
        resolve("CameraControl: Video file compression standard set successfully")
      } else {
        reject("CameraControl: Video file compression standard error", error?.localizedDescription, error)
      }
    })
  }

  @objc(setVideoResolutionAndFrameRate:frameRate:resolve:reject:)
  func setVideoResolutionAndFrameRate(resolution: String, frameRate: String, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {

    guard let videoResolutionParam = cameraParameters["VideoResolutions"]![resolution] else {
      reject("CameraControl: Unknown video resolution", "An unknown or invalid video resolution of \"\(resolution)\" was provided", nil)
      return
    }
    guard let videoFrameRateParam = cameraParameters["VideoFrameRates"]![frameRate] else {
      reject("CameraControl: Unknown video frame rate", "An unknown video frame rate of \"\(frameRate)\" was provided", nil)
      return
    }

    let resolutionAndFramerate = DJICameraVideoResolutionAndFrameRate(resolution: DJICameraVideoResolution(rawValue: videoResolutionParam)!, frameRate: DJICameraVideoFrameRate(rawValue: videoFrameRateParam)!)
    DJISDKManager.keyManager()?.setValue(resolutionAndFramerate, for: DJICameraKey(param: DJICameraParamVideoResolutionAndFrameRate)!, withCompletion: { (error: Error?) in
      if (error == nil) {
        resolve("CameraControl: Set video resolution & frame rate successfully")
      } else {
        reject("CameraControl: Set video resolution & frame rate error", error?.localizedDescription, error)
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

  @objc(isSDCardInserted:reject:)
  func isSDCardInserted(resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    DJISDKManager.keyManager()?.getValueFor(DJICameraKey(param: DJICameraParamSDCardIsInserted)!, withCompletion: { (value: DJIKeyedValue?, error: Error?) in
      if (error == nil) {
        if let isCardInserted = value?.boolValue {
          resolve(isCardInserted)
          return
        }
      }
      reject("CameraControl: isSDCardInserted failed", nil, error)
    })
  }

  @objc static func requiresMainQueueSetup() -> Bool {
    return true
  }

  @objc func constantsToExport() -> [String: Any] {
    return cameraParameters
  }

}
