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
  
  override init() {
    super.init()
    NotificationCenter.default.addObserver(self, selector: #selector(sendEventToJS), name: NSNotification.Name("DJIEvent"), object: nil)
  }

  static func sendReactEvent(type: String, value: Any) {
    NotificationCenter.default.post(name: Notification.Name("DJIEvent"), object: nil, userInfo: ["type": type, "value": value])
  }
  
  @objc private func sendEventToJS(payload: NSNotification) {
    // Only send events if the JS bridge has loaded
    let type = payload.userInfo!["type"] as! String
    let value = payload.userInfo!["value"]
    print("SENDING EVENT")
    if (self.bridge != nil) {
      print("2. SENDING EVENT: " + type)
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
