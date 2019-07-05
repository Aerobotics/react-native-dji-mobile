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
                  startRecordFlightData: (NString)fileName
                  resolve:               (RCTPromiseResolveBlock)resolve
                  reject:                (RCTPromiseRejectBlock)reject
                  )

RCT_EXTERN_METHOD(
                  stopRecordFlightData: (RCTPromiseResolveBlock)resolve
                  reject:               (RCTPromiseRejectBlock)reject
                  )

RCT_EXTERN_METHOD(
                  startProductConnectionListener: (RCTPromiseResolveBlock)resolve
                  reject:                         (RCTPromiseRejectBlock)reject
                  )

RCT_EXTERN_METHOD(
                  startBatteryPercentChargeRemainingListener: (RCTPromiseResolveBlock)resolve
                  reject:                                     (RCTPromiseRejectBlock)reject
                  )

RCT_EXTERN_METHOD(
                  startAircraftLocationListener: (RCTPromiseResolveBlock)resolve
                  reject:                        (RCTPromiseRejectBlock)reject
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
                  startAircraftCompassHeadingListener: (RCTPromiseResolveBlock)resolve
                  reject:                              (RCTPromiseRejectBlock)reject
                  )

RCT_EXTERN_METHOD(
                  startNewMediaFileListener: (RCTPromiseResolveBlock)resolve
                  reject:                    (RCTPromiseRejectBlock)reject
                  )

RCT_EXTERN_METHOD(
                  stopKeyListener: (NSString)keyString
                  resolve:         (RCTPromiseResolveBlock)resolve
                  reject:          (RCTPromiseRejectBlock)reject
                  )

RCT_EXTERN_METHOD(
                  stopNotificationCenterListener: (NSString)name
                  resolve:                        (RCTPromiseResolveBlock)resolve
                  reject:                         (RCTPromiseRejectBlock)reject
                  )

@end
