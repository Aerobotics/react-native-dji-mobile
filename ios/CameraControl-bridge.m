//
//  CameraControl-bridge.m
//  ReactNativeDjiMobile
//
//  Created by Adam Rosendorff on 2019/06/11.
//  Copyright © 2019 Aerobotics. All rights reserved.
//


#import <Foundation/Foundation.h>
#import <React/RCTBridgeModule.h>

@interface RCT_EXTERN_MODULE(CameraControlNative, NSObject)

RCT_EXTERN_METHOD(
                  setCameraMode: (NSString)cameraMode
                  resolve:       (RCTPromiseResolveBlock)resolve
                  reject:        (RCTPromiseRejectBlock)reject
                  )

RCT_EXTERN_METHOD(
                  setPhotoAspectRatio: (NSString)photoAspectRatio
                  resolve:             (RCTPromiseResolveBlock)resolve
                  reject:              (RCTPromiseRejectBlock)reject
                  )

RCT_EXTERN_METHOD(
                  setWhiteBalance: (NSDictionary)parameters
                  resolve:         (RCTPromiseResolveBlock)resolve
                  reject:          (RCTPromiseRejectBlock)reject
                  )

RCT_EXTERN_METHOD(
                  setExposureMode: (NSString)exposureMode
                  resolve:         (RCTPromiseResolveBlock)resolve
                  reject:          (RCTPromiseRejectBlock)reject
                  )

RCT_EXTERN_METHOD(
                  setISO:  (NSString)iso
                  resolve: (RCTPromiseResolveBlock)resolve
                  reject:  (RCTPromiseRejectBlock)reject
)

RCT_EXTERN_METHOD(
                  setShutterSpeed: (NSString)shutterSpeed
                  resolve:         (RCTPromiseResolveBlock)resolve
                  reject:          (RCTPromiseRejectBlock)reject
)

RCT_EXTERN_METHOD(
                  setVideoFileFormat: (NSString)videoFileFormat
                  resolve:            (RCTPromiseResolveBlock)resolve
                  reject:             (RCTPromiseRejectBlock)reject
                  )

RCT_EXTERN_METHOD(
                  setVideoFileCompressionStandard: (NSString)videoFileCompressionStandard
                  resolve:                         (RCTPromiseResolveBlock)resolve
                  reject:                          (RCTPromiseRejectBlock)reject
                  )

RCT_EXTERN_METHOD(
                  setVideoResolutionAndFrameRate: (NSString)resolution
                  frameRate:                      (NSString)frameRate
                  resolve:                        (RCTPromiseResolveBlock)resolve
                  reject:                         (RCTPromiseRejectBlock)reject
                  )

RCT_EXTERN_METHOD(
                  stopRecording: (RCTPromiseResolveBlock)resolve
                  reject:        (RCTPromiseRejectBlock)reject
                  )

@end
