//
//  DJIMissionControl.swift
//  ReactNativeDjiMobile
//
//  Created by Adam Rosendorff on 2019/02/20.
//

import Foundation
import DJISDK

enum TimelineElement: String {
  case TakeOffAction
  case GoToAction
  case GoHomeAction
  case GimbalAttitudeAction
  case RecordVideoAction
  case ShootPhotoAction
  
  case WaypointMissionTimelineElement
  case VirtualStickTimelineElement
  
}

@objc(DJIMissionControlWrapper)
class DJIMissionControlWrapper: NSObject {
  
  //  var timelineElementIndex = 0
  //  var timelineElements: [Int: DJIMissionControlTimelineElement] = [:]
  //  var scheduledElementIndexOrder: [Int] = [];
  
  @objc(scheduleElement:parameters:resolve:reject:)
  func scheduleElement(timelineElementName: String, parameters: NSDictionary, resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
    
    guard let missionControl = DJISDKManager.missionControl() else {
      reject("Schedule Element Error", "Could not schedule element as mission control could not be loaded", nil);
      return
    }
    
    let timelineElementToSchedule = TimelineElement.init(rawValue: timelineElementName)!
    
    var newElement: DJIMissionControlTimelineElement?
    
    switch timelineElementToSchedule {
      
    case .TakeOffAction:
      newElement = buildTakeOffAction()
      
    case .GoToAction:
      newElement = buildGoToAction(parameters)
      
    case .GoHomeAction:
      newElement = buildGoHomeAction(parameters)
      
    case .GimbalAttitudeAction:
      newElement = buildGimbalAttitudeAction(parameters)
      
    case .RecordVideoAction:
      newElement = buildRecordVideoAction(parameters)
      
    case .ShootPhotoAction:
      newElement = buildShootPhotoAction(parameters)
      
    case .WaypointMissionTimelineElement:
      let waypointMission = WaypointMissionTimelineElement(parameters)
      newElement = DJIWaypointMission(mission: waypointMission)
      
    case .VirtualStickTimelineElement:
      newElement = VirtualStickTimelineElement(parameters)
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
  
  private func buildTakeOffAction() -> DJITakeOffAction? {
    return DJITakeOffAction()
  }
  
  private func buildGoToAction(_ parameters: NSDictionary) -> DJIGoToAction? {
    let altitude = parameters["altitude"] as? Double
    let flightSpeed = parameters["flightSpeed"] as? Double
    var coordinate: CLLocationCoordinate2D? = nil
    if let coordinateParam = parameters["coordinate"] as? NSDictionary {
      coordinate = CLLocationCoordinate2D(
        latitude: coordinateParam["latitude"] as! Double,
        longitude: coordinateParam["longitude"] as! Double
      )
    }
    
    let goToAction: DJIGoToAction?
    
    if (coordinate != nil) {
      if (altitude != nil) {
        goToAction = DJIGoToAction(coordinate: coordinate!, altitude: altitude!)
      } else {
        goToAction = DJIGoToAction(coordinate: coordinate!)
      }
      
    } else { // Altitude will be provided only
      goToAction = DJIGoToAction(altitude: altitude!)
    }
    
    if (goToAction != nil && flightSpeed != nil) {
      goToAction!.flightSpeed = Float(flightSpeed!)
    }
    
    return goToAction
  }
  
  private func buildGoHomeAction(_ parameters: NSDictionary) -> DJIGoHomeAction? {
    let goHomeAction = DJIGoHomeAction()
    if let autoConfirmLandingEnabled = parameters["autoConfirmLandingEnabled"] as? Bool {
      goHomeAction.autoConfirmLandingEnabled = autoConfirmLandingEnabled
    }
    return goHomeAction
  }
  
  private func buildGimbalAttitudeAction(_ parameters: NSDictionary) -> DJIGimbalAttitudeAction? {
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
  
  private func buildShootPhotoAction(_ parameters: NSDictionary) -> DJIShootPhotoAction? {
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
  
  private func buildRecordVideoAction(_ parameters: NSDictionary) -> DJIRecordVideoAction? {
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
  
  //  @objc func constantsToExport() -> [AnyHashable : Any]! {
  //    return [
  //      "timelineElements": timelineElements,
  //    ]
  //  }
  
  @objc static func requiresMainQueueSetup() -> Bool {
    return true
  }
  
}
