//
//  DroneVideoView.swift
//  ReactNativeDjiMobile
//
//  Created by Adam Rosendorff on 2019/02/13.
//

import Foundation

@objc(DroneVideo)
class DroneVideoViewManager: RCTViewManager {
  
  override func view() -> UIView! {
    return DroneVideoView()
  }
}
