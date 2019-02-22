//
//  DJISDK.swift
//  ReactNativeDjiMobile
//
//  Created by Adam Rosendorff on 2019/02/22.
//

import Foundation
import DJISDK

@objc(EventSender)
class EventSender: RCTEventEmitter {
  
  func sendReactEvent(type: String, value: Any) {
    // FIXME: (Adam) How do we get the new bridge (if the javascript side is reloaded)???
    if (self.bridge != nil) {
      print("SENDING EVENT: " + type)
      self.sendEvent(withName: "DJIEvent", body: [
        "type": type,
        "value": value,
        ])
    }
  }
  
  
  override func supportedEvents() -> [String]! {
    return ["DJIEvent"]
  }
  
  override static func requiresMainQueueSetup() -> Bool {
    return true
  }
}
