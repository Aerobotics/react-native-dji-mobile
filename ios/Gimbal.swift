//
//  Gimbal.swift
//  ReactNativeDjiMobile
//
//  Created by Adam Rosendorff on 2019/10/16.
//  Copyright © 2019 Aerobotics. All rights reserved.
//

import Foundation
import DJISDK

@objc(GimbalWrapper)
class GimbalWrapper: NSObject {
  
  @objc(rotate:resolve:reject:)
  func rotate(parameters: NSDictionary, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    guard let gimbal = DJISDKManager.product()?.gimbal else {
      reject("rotate error",  "Could not access gimbal", nil)
      return
    }
    
    guard let time = parameters["time"] as? Double else {
      reject("rotate error", "time value must be supplied", nil)
      return
    }
        
    let gimbalRotation = DJIGimbalRotation(pitchValue: parameters["pitch"] as? NSNumber, rollValue: parameters["roll"] as? NSNumber, yawValue: parameters["yaw"] as? NSNumber, time: time, mode: .absoluteAngle)

    gimbal.rotate(with: gimbalRotation) { (error: Error?) in
      if (error != nil) {
        reject("rotate error", error?.localizedDescription, error)
      } else {
        resolve("rotate")
      }
    }
    
  }
  
  
}

