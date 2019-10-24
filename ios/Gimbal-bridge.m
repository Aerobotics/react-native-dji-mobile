//
//  Gimbal-bridge.m
//  ReactNativeDjiMobile
//
//  Created by Adam Rosendorff on 2019/10/16.
//  Copyright © 2019 Aerobotics. All rights reserved.
//

#import <Foundation/Foundation.h>

//
//  FlightController-bridge.m
//  ReactNativeDjiMobile
//
//  Created by Adam Rosendorff on 2019/10/16.
//  Copyright © 2019 Facebook. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <React/RCTBridgeModule.h>

@interface RCT_EXTERN_MODULE(GimbalWrapper, NSObject)

RCT_EXTERN_METHOD(
                  rotate:  (NSDictionary)parameters
                  resolve: (RCTPromiseResolveBlock)resolve
                  reject:  (RCTPromiseRejectBlock)reject
                  )

@end
