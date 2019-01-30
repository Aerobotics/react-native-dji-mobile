//
//  DJISDK.swift
//  ReactNativeDjiMobile
//
//  Created by Adam Rosendorff on 2019/01/29.
//  Copyright © 2019 Facebook. All rights reserved.

//import Foundation
import DJISDK

@objc(test)
class test: NSObject {
  @objc(version)
  func version() -> Void {
    NSLog("TEST VERSION: %@", DJISDKManager.sdkVersion());
  }
}
