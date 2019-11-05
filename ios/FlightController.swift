//
//  FlightController.swift
//  ReactNativeDjiMobile
//
//  Created by Adam Rosendorff on 2019/10/16.
//  Copyright © 2019 Facebook. All rights reserved.
//

import Foundation
import DJISDK

//enum TimelineElement: String {
//  case TakeOffAction
//  case GoToAction
//  case GoHomeAction
//  case GimbalAttitudeAction
//  case RecordVideoAction
//  case ShootPhotoAction
//
//  case WaypointMissionTimelineElement
//  case VirtualStickTimelineElement
//  case RecordFlightData
//  case RunJSElement
//
//}

@objc(FlightControllerWrapper)
class FlightControllerWrapper: NSObject {

  var virtualStickTimelineElement: VirtualStickTimelineElement?

  @objc(startVirtualStick:resolve:reject:)
  func startVirtualStick(parameters: NSDictionary, resolve: @escaping RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
//    if (resolveOnFinish == true) {
//      parameters.setValue({ (error: Error?) in
//        if (error != nil) {
//
//        } else {
//          resolve("FlightController: Virtual stick complete")
//        }
//      }, forKey: "onFinish")
//    }
    self.virtualStickTimelineElement = VirtualStickTimelineElement(parameters)
    virtualStickTimelineElement?.run()
//    if (resolveOnFinish == false) {
      resolve("FlightController: Start virtual stick")
//    }

  }

  @objc(stopVirtualStick:reject:)
  func stopVirtualStick(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
    self.virtualStickTimelineElement?.stopRun()
    self.virtualStickTimelineElement = nil
    resolve("FlightController: Stop virtual stick")
  }

  @objc(startWaypointMission:resolve:reject:)
  func startWaypointMission(parameters: NSDictionary, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    guard let waypointMissionOperator = DJISDKManager.missionControl()?.waypointMissionOperator() else {
      reject("startWaypointMission Error", "Could not load DJIWaypointMissionOperator", nil);
      return
    }

    if (waypointMissionOperator.currentState == .executing) {
      waypointMissionOperator.stopMission(completion: nil)
    }

    let waypointMission = WaypointMissionTimelineElement(parameters)
    let loadError = waypointMissionOperator.load(DJIWaypointMission(mission: waypointMission))
    if (loadError != nil) {
      reject("startWaypointMission Error", "Load mission error", loadError);
      return
    }

    waypointMissionOperator.uploadMission { (error: Error?) in
      if (error != nil) {
        reject("startWaypointMission Error", "Upload mission error", error);

      } else {

        let queue = DispatchQueue(label: "waitForUpload", attributes: .concurrent)
        let semaphore = DispatchSemaphore(value: 0)
        var timedOut = false

        queue.async {
          semaphore.wait(timeout: .now() + .seconds(4))
          timedOut = true
        }

        queue.async {
          while (timedOut == false) {
            if (waypointMissionOperator.currentState == .readyToExecute) {
              timedOut = true
              semaphore.signal()
            }
          }
          waypointMissionOperator.startMission { (error: Error?) in
            if (error != nil) {
              reject("startWaypointMission Error", "Start mission error", error);

            } else {
              resolve("startWaypointMission")
            }
          }
        }

      }
    }

  }

  @objc(stopWaypointMission:reject:)
  func stopWaypointMission(resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    guard let waypointMissionOperator = DJISDKManager.missionControl()?.waypointMissionOperator() else {
      reject("stopWaypointMission Error", "Could not load DJIWaypointMissionOperator", nil);
      return
    }

    if (waypointMissionOperator.currentState == .executing) {
      waypointMissionOperator.stopMission(completion: nil)
    }
    resolve("stopWaypointMission")
  }

  @objc(startWaypointMissionFinishedListener:reject:)
  func startWaypointMissionFinishedListener(resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    guard let waypointMissionOperator = DJISDKManager.missionControl()?.waypointMissionOperator() else {
      reject("startWaypointMissionFinishedListener Error", "Could not load DJIWaypointMissionOperator", nil);
      return
    }
    waypointMissionOperator.addListener(toFinished: self, with: nil) { (error: Error?) in
      EventSender.sendReactEvent(type: "WaypointMissionFinished", value: true, realtime: true)
    }
    resolve("startWaypointMissionFinishedListener")
  }

  @objc(stopAllWaypointMissionListeners:reject:)
  func stopAllWaypointMissionListeners(resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    guard let waypointMissionOperator = DJISDKManager.missionControl()?.waypointMissionOperator() else {
      reject("stopAllWaypointMissionListeners Error", "Could not load DJIWaypointMissionOperator", nil);
      return
    }
    waypointMissionOperator.removeAllListeners()
    resolve("stopAllWaypointMissionListeners")
  }

  @objc(startRecordFlightData:resolve:reject:)
  func startRecordFlightData(fileName: String, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    DJIRealTimeDataLogger.startLogging(fileName: fileName)
    resolve("startRecordFlightData")
  }

  @objc(stopRecordFlightData:reject:)
  func stopRecordFlightData(resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    DJIRealTimeDataLogger.stopLogging()
    resolve("stopRecordFlightData")
  }

  @objc static func requiresMainQueueSetup() -> Bool {
    return true
  }

}

