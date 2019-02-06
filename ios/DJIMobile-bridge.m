//
//  test-bridge.m
//  ReactNativeDjiMobile
//
//  Created by Adam Rosendorff on 2019/01/29.
//  Copyright © 2019 Facebook. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>


@interface RCT_EXTERN_MODULE(DJIMobile, RCTEventEmitter)

RCT_EXTERN_METHOD(
                  registerApp: (RCTPromiseResolveBlock)resolve
                  reject:      (RCTPromiseRejectBlock)reject
                  )

RCT_EXTERN_METHOD(
                  startProductConnectionListener: (RCTPromiseResolveBlock)resolve
                  reject:                         (RCTPromiseRejectBlock)reject
                  )

RCT_EXTERN_METHOD(
                  stopProductConnectionListener:  (RCTPromiseResolveBlock)resolve
                  reject:                         (RCTPromiseRejectBlock)reject
                  )

@end
