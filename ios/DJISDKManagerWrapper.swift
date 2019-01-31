//
//  DJISDK.swift
//  ReactNativeDjiMobile
//
//  Created by Adam Rosendorff on 2019/01/29.
//  Copyright © 2019 Facebook. All rights reserved.

import Foundation
import DJISDK

@objc(DJISDKManagerWrapper)
class DJISDKManagerWrapper: NSObject {
  @objc(getSDKVersion:reject:)
  func getSDKVersion(_ resolve: RCTPromiseResolveBlock, reject reject: RCTPromiseRejectBlock) -> Void {
    resolve(DJISDKManager.sdkVersion());
  }
}
