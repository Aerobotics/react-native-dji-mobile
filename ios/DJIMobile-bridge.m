//
//  test-bridge.m
//  ReactNativeDjiMobile
//
//  Created by Adam Rosendorff on 2019/01/29.
//

#import <Foundation/Foundation.h>
#import <React/RCTBridgeModule.h>

@interface RCT_EXTERN_MODULE(DJIMobile, NSObject)

RCT_EXTERN_METHOD(
                  registerApp: (RCTPromiseResolveBlock)resolve
                  reject:      (RCTPromiseRejectBlock)reject
                  )

RCT_EXTERN_METHOD(
                  registerAppAndUseBridge: (NSString)bridgeIp
                  resolve:                 (RCTPromiseResolveBlock)resolve
                  reject:                  (RCTPromiseRejectBlock)reject
                  )

RCT_EXTERN_METHOD(
                  limitEventFrequency: (nonnull NSNumber)frequency
                  resolve:             (RCTPromiseResolveBlock)resolve
                  reject:              (RCTPromiseRejectBlock)reject
                  )

RCT_EXTERN_METHOD(
                  startEventListener: (NSString)eventName
                  resolve:            (RCTPromiseResolveBlock)resolve
                  reject:             (RCTPromiseRejectBlock)reject
                  )

RCT_EXTERN_METHOD(
                  startRecordFlightData: (NSString)fileName
                  resolve:               (RCTPromiseResolveBlock)resolve
                  reject:                (RCTPromiseRejectBlock)reject
                  )

RCT_EXTERN_METHOD(
                  stopRecordFlightData: (RCTPromiseResolveBlock)resolve
                  reject:               (RCTPromiseRejectBlock)reject
                  )

RCT_EXTERN_METHOD(
                  getAircraftLocation: (RCTPromiseResolveBlock)resolve
                  reject:              (RCTPromiseRejectBlock)reject
                  )

RCT_EXTERN_METHOD(
                  startAircraftVelocityListener: (RCTPromiseResolveBlock)resolve
                  reject:                        (RCTPromiseRejectBlock)reject
                  )

RCT_EXTERN_METHOD(
                  getFlightLogPath: (RCTPromiseResolveBlock)resolve
                  reject:           (RCTPromiseRejectBlock)reject
                  )

RCT_EXTERN_METHOD(
                  startFlightLogListener: (RCTPromiseResolveBlock)resolve
                  reject:                 (RCTPromiseRejectBlock)reject
                  )

RCT_EXTERN_METHOD(
                  stopFlightLogListener: (RCTPromiseResolveBlock)resolve
                  reject:                (RCTPromiseRejectBlock)reject
                  )

RCT_EXTERN_METHOD(
                  setCollisionAvoidanceEnabled: (BOOL)enabled
                  resolve:                      (RCTPromiseResolveBlock)resolve
                  reject:                       (RCTPromiseRejectBlock)reject
                  )

RCT_EXTERN_METHOD(
                  setVirtualStickAdvancedModeEnabled: (BOOL)enabled
                  resolve:                            (RCTPromiseResolveBlock)resolve
                  reject:                             (RCTPromiseRejectBlock)reject
                  )

RCT_EXTERN_METHOD(
                  setLandingProtectionEnabled: (BOOL)enabled
                  resolve:                     (RCTPromiseResolveBlock)resolve
                  reject:                      (RCTPromiseRejectBlock)reject
                  )

RCT_EXTERN_METHOD(
                  setVisionAssistedPositioningEnabled: (BOOL)enabled
                  resolve:                             (RCTPromiseResolveBlock)resolve
                  reject:                              (RCTPromiseRejectBlock)reject
                  )

RCT_EXTERN_METHOD(
                  startNewMediaFileListener: (RCTPromiseResolveBlock)resolve
                  reject:                    (RCTPromiseRejectBlock)reject
                  )

RCT_EXTERN_METHOD(
                  stopEventListener: (NSString)keyString
                  resolve:           (RCTPromiseResolveBlock)resolve
                  reject:            (RCTPromiseRejectBlock)reject
                  )

RCT_EXTERN_METHOD(
                  stopNotificationCenterListener: (NSString)name
                  resolve:                        (RCTPromiseResolveBlock)resolve
                  reject:                         (RCTPromiseRejectBlock)reject
                  )

@end
