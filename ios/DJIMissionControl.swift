//
//  DJIMissionControl.swift
//  ReactNativeDjiMobile
//
//  Created by Adam Rosendorff on 2019/02/20.
//

import Foundation
import DJISDK

let timelineElements = [
  "WaypointMissionTimelineElement": "WaypointMissionTimelineElement",
  "CapturePictureTimelineElement": "CapturePictureTimelineElement",
]

@objc(DJIMissionControlWrapper)
class DJIMissionControlWrapper: NSObject {
  
  //  var timelineElementIndex = 0
  //  var timelineElements: [Int: DJIMissionControlTimelineElement] = [:]
  //  var scheduledElementIndexOrder: [Int] = [];
  
  @objc(scheduleElement:parameters:resolve:reject:)
  func scheduleElement(timelineElementType: String, parameters: NSDictionary, resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
    
    guard let missionControl = DJISDKManager.missionControl() else {
      reject("Schedule Element Error", "Could not schedule element as mission control could not be loaded", nil);
      return
    }
    
    var newElement: DJIMissionControlTimelineElement?
    
    switch timelineElementType {
    case timelineElements["WaypointMissionTimelineElement"]:
      let waypointMission = WaypointMissionTimelineElement(parameters: parameters)
      newElement = DJIWaypointMission(mission: waypointMission)
    default:
      break
    }
    
    if newElement != nil {
      let validError: Error? = newElement?.checkValidity()
      if (validError != nil) {
        print(validError!.localizedDescription)
      }
      let error: Error? = missionControl.scheduleElement(newElement!)
      if (error != nil) {
        print(error!.localizedDescription)
      }
      resolve("DJI Mission Control: Schedule Element")
      return
    }
  }
  
  //  @objc(createWaypointMission:parameters:resolve:reject:)
  //  func createWaypointMission(coordinates: NSArray, parameters: NSDictionary, resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
  //
  //    let waypointMission = DJIMutableWaypointMission.init()
  //
  //    waypointMission.autoFlightSpeed = (parameters["autoFlightSpeed"] as? Float) ?? 2.0
  //    waypointMission.maxFlightSpeed = (parameters["maxFlightSpeed"] as? Float) ?? waypointMission.autoFlightSpeed
  //
  //    var waypoints: [DJIWaypoint] = []
  //    for case let item as [String: Double] in coordinates {
  //      let waypointCoordinate = CLLocationCoordinate2D.init(latitude: item["latitude"]!, longitude: item["longitude"]!)
  //      let waypoint = DJIWaypoint.init(coordinate: waypointCoordinate)
  //      waypoint.altitude = Float(item["altitude"]!)
  //      waypoints.append(waypoint)
  //    }
  //
  //    waypointMission.addWaypoints(waypoints)
  //    let error = waypointMission.checkParameters()
  //    if (error != nil) {
  //      reject("Waypoint mission invalid", (error! as NSError).localizedDescription, error! as NSError)
  //      return
  //    } else {
  //      timelineElements[timelineElementIndex] = waypointMission
  //      resolve(timelineElementIndex)
  //      timelineElementIndex += 1
  //    }
  //
  //  }
  
  //  @objc(destroyWaypointMission:resolve:reject:)
  //  func destroyWaypointMission(missionId: NSNumber, resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
  //    let removedMission = timelineElements.removeValue(forKey: missionId.intValue)
  //    // TODO: (Adam) Should this reject if no valid mission was found?
  //    resolve(nil)
  //  }
  
  //  @objc(scheduleElement:resolve:reject:)
  //  func scheduleElement(elementId: NSNumber, resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
  //    guard let missionControl = DJISDKManager.missionControl() else {
  //      reject("Schedule Element Error", "Could not schedule element as mission control could not be loaded", nil);
  //      return;
  //    }
  //
  //    let element = timelineElements.first { $0.key == elementId.intValue }
  //    if (element != nil) {
  //      missionControl.scheduleElement(element!.value)
  //      scheduledElementIndexOrder.append(elementId.intValue)
  //    }
  //    resolve(nil)
  //  }
  
  @objc(unscheduleEverything:reject:)
  func unscheduleEverything(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
    guard let missionControl = DJISDKManager.missionControl() else {
      reject("Unschedule Everything Error", "Could not unschedule everything as mission control could not be loaded", nil);
      return;
    }
    
    missionControl.unscheduleEverything();
    resolve("DJI Mission Control: Unschedule Everything");
  }
  
  @objc(startTimeline:reject:)
  func startTimeline(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
    guard let missionControl = DJISDKManager.missionControl() else {
      reject("Start Timeline Error", "Could not start timeline as mission control could not be loaded", nil);
      return
    }
    
    missionControl.stopTimeline()
    missionControl.startTimeline()
    resolve("DJI Mission Control: Start Timeline")
    return
  }
  
  @objc(stopTimeline:reject:)
  func stopTimeline(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
    guard let missionControl = DJISDKManager.missionControl() else {
      reject("Stop Timeline Error", "Could not stop timeline as mission control could not be loaded", nil);
      return
    }
    
    missionControl.stopTimeline()
    resolve("DJI Mission Control: Stop Timeline")
    return
  }
  
  @objc(startTimelineListener:reject:)
  func startTimelineListener(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
    guard let missionControl = DJISDKManager.missionControl() else {
      reject("Start Listener Error", "Could not start mission control listener as mission control could not be loaded", nil);
      return
    }
    
    missionControl.removeAllListeners()
    missionControl.addListener(self) { (timelineEvent: DJIMissionControlTimelineEvent, timelineElement: DJIMissionControlTimelineElement?, error: Error?, info: Any?) in
      var eventInfo: [String:Any] = [:]
      var timelineIndex = Int(missionControl.currentTimelineMarker)
      
      if (timelineElement == nil) { // This is a general timeline event (timeline start/stop, etc.)
        timelineIndex = -1
        eventInfo["elementId"] = -1
      } else {
        //          eventInfo["elementId"] = self.scheduledElementIndexOrder[Int(timelineIndex)]
      }
      eventInfo["eventType"] = timelineEvent.rawValue
      eventInfo["timelineIndex"] = timelineIndex
      
      if (error != nil) {
        eventInfo["error"] = error!.localizedDescription
      }
      
      EventSender.sendReactEvent(type: "missionControlEvent", value: eventInfo)
      
    }
    
    resolve("DJI Mission Control: Start Listener")
    return
  }
  
  @objc(stopTimelineListener:reject:)
  func stopTimelineListener(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
    guard let missionControl = DJISDKManager.missionControl() else {
      reject("Stop Listener Error", "Could not stop mission control listener as mission control could not be loaded", nil);
      return
    }
    
    missionControl.removeAllListeners()
    resolve("DJI Mission Control: Stop Listener")
    return
    
  }
  
  @objc(checkWaypointMissionValidity:resolve:reject:)
  func checkWaypointMissionValidity(parameters: NSDictionary, resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
    let waypointMission = WaypointMissionTimelineElement(parameters: parameters)
    let error = DJIWaypointMission.init(mission: waypointMission).checkParameters()
    if (error != nil) {
      reject("Waypoint Mission Invalid", error!.localizedDescription, nil)
      return
    } else {
      resolve("Waypoint Mission Valid")
      return
    }
  }
  
  @objc func constantsToExport() -> [AnyHashable : Any]! {
    return [
      "timelineElements": timelineElements,
    ]
  }
  
  @objc static func requiresMainQueueSetup() -> Bool {
    return true
  }
  
}
