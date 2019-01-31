//
//  test-bridge.m
//  ReactNativeDjiMobile
//
//  Created by Adam Rosendorff on 2019/01/29.
//  Copyright © 2019 Facebook. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <React/RCTBridgeModule.h>

@interface RCT_EXTERN_MODULE(DJISDKManagerWrapper, NSObject)

RCT_EXTERN_METHOD(
  getSDKVersion:  (RCTPromiseResolveBlock)resolve
  reject:         (RCTPromiseRejectBlock)reject
)

@end
