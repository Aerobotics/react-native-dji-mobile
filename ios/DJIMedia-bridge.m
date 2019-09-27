//
//  DJIMedia-bridge.m
//  ReactNativeDjiMobile
//
//  Created by Adam Rosendorff on 2019/07/25.
//  Copyright © 2019 Aerobotics. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <React/RCTBridgeModule.h>

@interface RCT_EXTERN_MODULE(DJIMedia, NSObject)

RCT_EXTERN_METHOD(
                  startFullResMediaFileDownload: (NSString)nameOfFileToDownload
                  newFileName:                   (NSString)newFileName
                  resolve:                       (RCTPromiseResolveBlock)resolve
                  reject:                        (RCTPromiseRejectBlock)reject
                  )

@end
