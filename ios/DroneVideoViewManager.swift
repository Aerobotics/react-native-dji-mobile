//
//  DroneVideoView.swift
//  ReactNativeDjiMobile
//
//  Created by Adam Rosendorff on 2019/02/13.
//  Copyright © 2019 Facebook. All rights reserved.
//

import Foundation

@objc(DroneVideo)
class DroneVideoViewManager: RCTViewManager {
  
  override func view() -> UIView! {
    DJIVideoPreviewer.instance()
//    DJIVideoPreviewer.instance().setView();
    let label = UILabel()
    label.text = "Swift Counter"
    label.textAlignment = .center
    return label
  }
}
