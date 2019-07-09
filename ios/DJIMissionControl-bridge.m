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
                  scheduleElement: (NSString)timelineElementName
                  parameters:      (NSDictionary)parameters
                  resolve:         (RCTPromiseResolveBlock)resolve
                  reject:          (RCTPromiseRejectBlock)reject
                  )

//RCT_EXTERN_METHOD(
//                  createWaypointMission: (NSArray)coordinates
//                  parameters:            (NSDictionary)parameters
//                  resolve:               (RCTPromiseResolveBlock)resolve
//                  reject:                (RCTPromiseRejectBlock)reject
//                  )

RCT_EXTERN_METHOD(
                  unscheduleEverything: (RCTPromiseResolveBlock)resolve
                  reject:               (RCTPromiseRejectBlock)reject
                  )

RCT_EXTERN_METHOD(
                  startTimeline: (RCTPromiseResolveBlock)resolve
                  reject:        (RCTPromiseRejectBlock)reject
                  )

RCT_EXTERN_METHOD(
                  pauseTimeline: (RCTPromiseResolveBlock)resolve
                  reject:        (RCTPromiseRejectBlock)reject
                  )

RCT_EXTERN_METHOD(
                  resumeTimeline: (RCTPromiseResolveBlock)resolve
                  reject:        (RCTPromiseRejectBlock)reject
                  )

RCT_EXTERN_METHOD(
                  stopTimeline: (RCTPromiseResolveBlock)resolve
                  reject:       (RCTPromiseRejectBlock)reject
                  )

RCT_EXTERN_METHOD(
                  setCurrentTimelineMarker: (NSUInteger)currentTimelineMarker
                  resolve:                  (RCTPromiseResolveBlock)resolve
                  reject:                   (RCTPromiseRejectBlock)reject
                  )

RCT_EXTERN_METHOD(
                  startGoHome: (RCTPromiseResolveBlock)resolve
                  reject:      (RCTPromiseRejectBlock)reject
                  )

RCT_EXTERN_METHOD(
                  startTimelineListener: (RCTPromiseResolveBlock)resolve
                  reject:                (RCTPromiseRejectBlock)reject
                  )

RCT_EXTERN_METHOD(
                  stopTimelineListener: (RCTPromiseResolveBlock)resolve
                  reject:               (RCTPromiseRejectBlock)reject
                  )

RCT_EXTERN_METHOD(
                  checkWaypointMissionValidity: (NSDictionary)parameters
                  resolve:                      (RCTPromiseResolveBlock)resolve
                  reject:                       (RCTPromiseRejectBlock)reject
                  )

@end
