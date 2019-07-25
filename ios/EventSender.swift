//
//  DJISDK.swift
//  ReactNativeDjiMobile
//
//  Created by Adam Rosendorff on 2019/02/22.
//

import Foundation
import DJISDK

@objc(EventSender)
public class EventSender: RCTEventEmitter, RCTInvalidating {
  
  private static var eventSendFrequency = 1.0
  private var eventSendLimiterTimer: Timer
  
  // The event queue only holds the most recent event for each event type received, discarding older events of the same type
  private var queuedEvents = [String: Any]()
  
  override init() {
    eventSendLimiterTimer = Timer.init()
    super.init()
    
    self.eventSendLimiterTimer = Timer.scheduledTimer(timeInterval: 1.0/EventSender.eventSendFrequency, target: self, selector: #selector(self.sendQueuedEvents), userInfo: nil, repeats: true)
    NotificationCenter.default.addObserver(self, selector: #selector(processEvent), name: NSNotification.Name("DJIEvent"), object: nil)
    NotificationCenter.default.addObserver(self, selector: #selector(setNewEventSendFrequency), name: NSNotification.Name("limitEventSendFrequency"), object: nil)
  }
  
  public static func getEventSendFrequency() -> Double {
    return eventSendFrequency
  }
  
  public func invalidate() {
    self.eventSendLimiterTimer.invalidate()
    NotificationCenter.default.removeObserver(self)
  }
  
  static func limitEventSendFrequency(frequency: Double) {
    NotificationCenter.default.post(name: Notification.Name("limitEventSendFrequency"), object: nil, userInfo: ["frequency": frequency])
  }
  
  static func sendReactEvent(type: String, value: Any, realtime: Bool = false) {
    NotificationCenter.default.post(name: Notification.Name("DJIEvent"), object: nil, userInfo: ["type": type, "value": value, "realtime": realtime])
  }
  
  @objc private func setNewEventSendFrequency(payload: NSNotification) {
    EventSender.eventSendFrequency = payload.userInfo!["frequency"] as! Double
    self.eventSendLimiterTimer.invalidate()
    self.eventSendLimiterTimer = Timer.init(timeInterval: 1.0/EventSender.eventSendFrequency, target: self, selector: #selector(self.sendQueuedEvents), userInfo: nil, repeats: true)
    RunLoop.main.add(self.eventSendLimiterTimer, forMode: .common)
  }
  
  @objc private func sendQueuedEvents() {
    if (queuedEvents.count > 0) {
      let queuedEventsSnapshot = queuedEvents
      queuedEvents = [:]
      for (type, value) in queuedEventsSnapshot {
        sendEventThroughBridge(type, value)
      }
    }
  }
  
  @objc private func processEvent(payload: NSNotification) {
    let type = payload.userInfo!["type"] as! String
    let value = payload.userInfo!["value"]
    let realtime = payload.userInfo!["realtime"] as! Bool
    
    if (realtime == true) {
      sendEventThroughBridge(type, value)
    } else {
      queueEvent(type, value)
    }
  }
  
  private func queueEvent(_ type: String, _ value: Any?) {
    queuedEvents[type] = value
  }
  
  private func sendEventThroughBridge(_ type: String, _ value: Any?) {
    // Only send events if the JS bridge has loaded
    if (self.bridge != nil) {
      self.sendEvent(withName: "DJIEvent", body: [
        "type": type,
        "value": value,
        ])
    }
  }
  
  override public func supportedEvents() -> [String]! {
    return ["DJIEvent"]
  }
  
  override public static func requiresMainQueueSetup() -> Bool {
    return true
  }
}
