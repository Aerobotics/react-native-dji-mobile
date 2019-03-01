//
//  DJIMissionControl-bridge.m
//  ReactNativeDjiMobile
//
//  Created by Adam Rosendorff on 2019/02/20.
//

#import <Foundation/Foundation.h>
#import <React/RCTBridgeModule.h>
//#import <React/RCTEventEmitter.h>

@interface RCT_EXTERN_MODULE(DJIMissionControlWrapper, NSObject)

RCT_EXTERN_METHOD(
                  createWaypointMission: (NSArray)coordinates
                  parameters:            (NSDictionary)parameters
                  resolve:               (RCTPromiseResolveBlock)resolve
                  reject:                (RCTPromiseRejectBlock)reject
                  )

RCT_EXTERN_METHOD(
                  scheduleElement: (nonnull NSNumber)elementId
                  resolve:         (RCTPromiseResolveBlock)resolve
                  reject:          (RCTPromiseRejectBlock)reject
                  )

RCT_EXTERN_METHOD(
                  unscheduleEverything: (RCTPromiseResolveBlock)resolve
                  reject:               (RCTPromiseRejectBlock)reject
                  )

RCT_EXTERN_METHOD(
                  startTimeline: (RCTPromiseResolveBlock)resolve
                  reject:        (RCTPromiseRejectBlock)reject
                  )

RCT_EXTERN_METHOD(
                  startListener: (RCTPromiseResolveBlock)resolve
                  reject:        (RCTPromiseRejectBlock)reject
                  )

RCT_EXTERN_METHOD(
                  stopListener: (RCTPromiseResolveBlock)resolve
                  reject:       (RCTPromiseRejectBlock)reject
                  )

@end
