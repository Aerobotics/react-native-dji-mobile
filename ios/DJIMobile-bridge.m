//
//  test-bridge.m
//  ReactNativeDjiMobile
//
//  Created by Adam Rosendorff on 2019/01/29.
//

#import <Foundation/Foundation.h>
#import <React/RCTBridgeModule.h>
//#import <React/RCTEventEmitter.h>


@interface RCT_EXTERN_MODULE(DJIMobile, NSObject)

RCT_EXTERN_METHOD(
                  registerApp: (RCTPromiseResolveBlock)resolve
                  reject:      (RCTPromiseRejectBlock)reject
                  )

RCT_EXTERN_METHOD(
                  registerAppAndUseBridge:  (NSString)bridgeIp
                  resolve:                  (RCTPromiseResolveBlock)resolve
                  reject:                   (RCTPromiseRejectBlock)reject
                  )

RCT_EXTERN_METHOD(
                  startRecordRealTimeData: (RCTPromiseResolveBlock)resolve
                  reject:                  (RCTPromiseRejectBlock)reject
                  )

RCT_EXTERN_METHOD(
                  stopRecordRealTimeData: (RCTPromiseResolveBlock)resolve
                  reject:                 (RCTPromiseRejectBlock)reject
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
                  startAircraftVelocityListener: (RCTPromiseResolveBlock)resolve
                  reject:                        (RCTPromiseRejectBlock)reject
                  )

RCT_EXTERN_METHOD(
                  startAircraftCompassHeadingListener: (RCTPromiseResolveBlock)resolve
                  reject:                              (RCTPromiseRejectBlock)reject
                  )

RCT_EXTERN_METHOD(
                  stopKeyListener: (NSString)keyString
                  resolve:         (RCTPromiseResolveBlock)resolve
                  reject:          (RCTPromiseRejectBlock)reject
                  )

@end
