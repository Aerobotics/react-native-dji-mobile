//
//  DJIMissionControl.swift
//  ReactNativeDjiMobile
//
//  Created by Adam Rosendorff on 2019/02/20.
//

import Foundation
import DJISDK

// TODO: (Adam) Replace this with an enum
let timelineElements = [
  "GimbalAttitudeAction": "GimbalAttitudeAction",
  "ShootPhotoAction": "ShootPhotoAction",
  "RecordVideoAction": "RecordVideoAction",
  
  "WaypointMissionTimelineElement": "WaypointMissionTimelineElement",
  "VirtualStickTimelineElement": "VirtualStickTimelineElement",
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
      let waypointMission = WaypointMissionTimelineElement(parameters)
      newElement = DJIWaypointMission(mission: waypointMission)
      
    case timelineElements["GimbalAttitudeAction"]:
      newElement = buildGimbalAttitudeAction(parameters)
      
    case timelineElements["ShootPhotoAction"]:
      newElement = buildShootPhotoAction(parameters)
      
    case timelineElements["RecordVideoAction"]:
      newElement = buildRecordVideoAction(parameters)
      
    case timelineElements["VirtualStickTimelineElement"]:
      newElement = VirtualStickTimelineElement(parameters)
      
    default:
      break
    }
    
    if newElement != nil {
      let validError: Error? = newElement?.checkValidity()
      if (validError != nil) {
        reject("Schedule Element Error", validError!.localizedDescription, validError!)
        return
      }
      let error: Error? = missionControl.scheduleElement(newElement!)
      if (error != nil) {
        reject("Schedule Element Error", error!.localizedDescription, error!)
        return
      }
      resolve("DJI Mission Control: Scheduled Element")
      return
    }
  }
  
  func buildGimbalAttitudeAction(_ parameters: NSDictionary) -> DJIGimbalAttitudeAction? {
    let pitch = parameters["pitch"] as! Float
    let roll = parameters["roll"] as! Float
    let yaw = parameters["yaw"] as! Float
    let completionTime = parameters["completionTime"] as? Double
    
    let attitude = DJIGimbalAttitude(pitch: pitch, roll: roll, yaw: yaw)
    let gimbalAttitudeAction = DJIGimbalAttitudeAction(attitude: attitude)
    if (completionTime != nil) {
      gimbalAttitudeAction?.completionTime = completionTime!
    }
    
    return gimbalAttitudeAction
  }
  
  func buildShootPhotoAction(_ parameters: NSDictionary) -> DJIShootPhotoAction? {
    let count = parameters["count"] as? Int32
    let interval = parameters["interval"] as? Double
    let wait = parameters["wait"] as? Bool
    let stopShoot = parameters["stopShoot"] as? Bool
    
    let shootPhotoAction: DJIShootPhotoAction?;
    
    if (stopShoot != nil) {
      shootPhotoAction = DJIShootPhotoAction.init(stopShootPhoto: ())
    } else if (count != nil && interval != nil && wait != nil) {
      shootPhotoAction = DJIShootPhotoAction.init(photoCount: count!, timeInterval: interval!, waitUntilFinish: wait!)
    } else {
      shootPhotoAction = DJIShootPhotoAction.init(singleShootPhoto: ())
    }
    
    return shootPhotoAction;
    
  }
  
  func buildRecordVideoAction(_ parameters: NSDictionary) -> DJIRecordVideoAction? {
    let duration = parameters["duration"] as? Double
    let stopRecord = parameters["stopRecord"] as? Bool
    
    if (stopRecord == true) {
      return DJIRecordVideoAction(stopRecordVideo: ())
    } else {
      if (duration != nil) {
        return DJIRecordVideoAction(duration: duration!)
      } else {
        return DJIRecordVideoAction(startRecordVideo: ())
      }
    }
  }
  
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
    let waypointMission = WaypointMissionTimelineElement(parameters)
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
