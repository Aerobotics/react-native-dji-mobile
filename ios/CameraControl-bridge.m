//
//  CameraControl-bridge.m
//  ReactNativeDjiMobile
//
//  Created by Adam Rosendorff on 2019/06/11.
//  Copyright © 2019 Aerobotics. All rights reserved.
//


#import <Foundation/Foundation.h>
#import <React/RCTBridgeModule.h>

@interface RCT_EXTERN_MODULE(CameraControl, NSObject)

RCT_EXTERN_METHOD(
                  setPhotoAspectRatio: (NSUInteger)photoAspectRatio
                  resolve:             (RCTPromiseResolveBlock)resolve
                  reject:              (RCTPromiseRejectBlock)reject
                  )

RCT_EXTERN_METHOD(
                  setWhiteBalance: (NSDictionary)parameters
                  resolve:         (RCTPromiseResolveBlock)resolve
                  reject:          (RCTPromiseRejectBlock)reject
                  )

RCT_EXTERN_METHOD(
                  setExposureMode: (NSUInteger)exposureMode
                  resolve:         (RCTPromiseResolveBlock)resolve
                  reject:          (RCTPromiseRejectBlock)reject
                  )

@end
