//
//  DJIMissionControl-bridge.m
//  ReactNativeDjiMobile
//
//  Created by Adam Rosendorff on 2019/02/20.
//

#import <Foundation/Foundation.h>
#import <React/RCTEventEmitter.h>

@interface RCT_EXTERN_MODULE(DJIMissionControlWrapper, NSObject)

RCT_EXTERN_METHOD(
                  createWaypointMission: (RCTPromiseResolveBlock)resolve
                  reject:                (RCTPromiseRejectBlock)reject
                  )

@end
