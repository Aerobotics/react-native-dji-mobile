//
//  DJIMissionControl.swift
//  ReactNativeDjiMobile
//
//  Created by Adam Rosendorff on 2019/02/20.
//

import Foundation
import DJISDK

@objc(DJIMissionControlWrapper)
class DJIMissionControlWrapper: NSObject {
  
  var timelineElementIndex = 0
  var timelineElements: [Int: DJIMissionControlTimelineElement] = [:]
  var scheduledElementIndexOrder: [Int] = [];
  
  @objc(createWaypointMission:parameters:resolve:reject:)
  func createWaypointMission(coordinates: NSArray, parameters: NSDictionary, resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
    
    let waypointMission = DJIMutableWaypointMission.init()
    
    waypointMission.autoFlightSpeed = (parameters["autoFlightSpeed"] as? Float) ?? 2.0
    waypointMission.maxFlightSpeed = (parameters["maxFlightSpeed"] as? Float) ?? waypointMission.autoFlightSpeed
    
    var waypoints: [DJIWaypoint] = []
    for case let item as [String: Double] in coordinates {
      let waypointCoordinate = CLLocationCoordinate2D.init(latitude: item["latitude"]!, longitude: item["longitude"]!)
      let waypoint = DJIWaypoint.init(coordinate: waypointCoordinate)
      waypoint.altitude = Float(item["altitude"]!)
      waypoints.append(waypoint)
    }
    
    waypointMission.addWaypoints(waypoints)
    let error = waypointMission.checkParameters()
    if (error != nil) {
      reject("Waypoint mission invalid", (error! as NSError).localizedDescription, error! as NSError)
      return
    } else {
      timelineElements[timelineElementIndex] = waypointMission
      resolve(timelineElementIndex)
      timelineElementIndex += 1
    }
    
  }
  
  //  @objc(destroyWaypointMission:resolve:reject:)
  //  func destroyWaypointMission(missionId: NSNumber, resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
  //    let removedMission = timelineElements.removeValue(forKey: missionId.intValue)
  //    // TODO: (Adam) Should this reject if no valid mission was found?
  //    resolve(nil)
  //  }
  
  @objc(scheduleElement:resolve:reject:)
  func scheduleElement(elementId: NSNumber, resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
    guard let missionControl = DJISDKManager.missionControl() else {
      reject("Schedule Element Error", "Could not schedule element as mission control could not be loaded", nil);
      return;
    }
    
    let element = timelineElements.first { $0.key == elementId.intValue }
    if (element != nil) {
      missionControl.scheduleElement(element!.value)
      scheduledElementIndexOrder.append(elementId.intValue)
    }
    resolve(nil)
  }
  
  @objc(unscheduleEverything:reject:)
  func unscheduleEverything(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
    guard let missionControl = DJISDKManager.missionControl() else {
      reject("Unschedule Everything Error", "Could not unschedule everything as mission control could not be loaded", nil);
      return;
    }
    
    missionControl.unscheduleEverything();
    scheduledElementIndexOrder.removeAll()
    resolve(nil);
  }
  
  @objc(startTimeline:reject:)
  func startTimeline(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
    guard let missionControl = DJISDKManager.missionControl() else {
      reject("Start Timeline Error", "Could not start timeline as mission control could not be loaded", nil);
      return;
    }
    
    missionControl.stopTimeline()
    missionControl.startTimeline()
    resolve(nil)
  }
  
  @objc(startListener:reject:)
  func startListener(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
    guard let missionControl = DJISDKManager.missionControl() else {
      reject("Start Listener Error", "Could not start mission control listener as mission control could not be loaded", nil);
      return;
    }
    
    missionControl.removeAllListeners()
    missionControl.addListener(self) { (timelineEvent: DJIMissionControlTimelineEvent, timelineElement: DJIMissionControlTimelineElement?, error: Error?, info: Any?) in
      var eventInfo: [String:Any] = [:]
      var timelineIndex = Int(missionControl.currentTimelineMarker)
      
      if (timelineElement == nil) { // This is a general timeline event (timeline start/stop, etc.)
        timelineIndex = -1
        eventInfo["elementId"] = -1
      } else {
        eventInfo["elementId"] = self.scheduledElementIndexOrder[Int(timelineIndex)]
      }
      eventInfo["eventType"] = timelineEvent.rawValue
      eventInfo["timelineIndex"] = timelineIndex
      
      if (error != nil) {
        eventInfo["error"] = error!.localizedDescription
      }
      
      EventSender.sendReactEvent(type: "missionControlEvent", value: eventInfo)
      
    }
    
    resolve(nil)
  }
  
  @objc(stopListener:reject:)
  func stopListener(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
    guard let missionControl = DJISDKManager.missionControl() else {
      reject("Stop Listener Error", "Could not stop mission control listener as mission control could not be loaded", nil);
      return;
    }
    
    missionControl.removeAllListeners()
    resolve(nil)
    
  }
  
}