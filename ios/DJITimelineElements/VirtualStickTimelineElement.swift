//
//  VirtualStickTimelineElement.swift
//  ReactNativeDjiMobile
//
//  Created by Adam Rosendorff on 2019/05/06.
//

import Foundation
import DJISDK

public class VirtualStickTimelineElement: NSObject, DJIMissionControlTimelineElement {
  
  var test = 0
  var timer: Timer
  
  init(_ parameters: NSDictionary) {
    self.timer = Timer.init() // Create an empty timer for now
    super.init()
  }
  
  public func run() {
    print("run")
    
    let flightController = (DJISDKManager.product() as! DJIAircraft).flightController
    let missionControl = DJISDKManager.missionControl()
    
    flightController?.setVirtualStickModeEnabled(true, withCompletion: { (error: Error?) in
      if (error != nil) {
        missionControl?.element(self, failedStartingWithError: error!)
        return
      }
      if (flightController?.isVirtualStickControlModeAvailable() == false) {
        enum VirtualStickError: Error {
          case VirtualStickUnavailableError(String)
        }
        missionControl?.element(self, failedStartingWithError: VirtualStickError.VirtualStickUnavailableError("VirtualStickUnavailableError"))
        return
      }
      
      missionControl?.elementDidStartRunning(self)
      
      self.timer = Timer.scheduledTimer(timeInterval: 0.1, target: self, selector: #selector(self.timerBlock), userInfo: nil, repeats: true)
      
//      missionControl?.element(self, didFinishRunningWithError: nil)
    })

  }
  
  @objc
  private func timerBlock() {
//    print("timerBlock!")
    
    let flightController = (DJISDKManager.product() as! DJIAircraft).flightController
    
    flightController?.send(DJIVirtualStickFlightControlData(pitch: 0, roll: 0, yaw: 0, verticalThrottle: 5), withCompletion: nil)
    
    test += 1
    if (test == 50) {
      timer.invalidate()
      DJISDKManager.missionControl()?.element(self, didFinishRunningWithError: nil)
    }
  }
  
  public func isPausable() -> Bool {
    return true
  }
  
  public func pauseRun() {
    print("pauseRun")
  }
  
  public func resumeRun() {
    print("resumeRun")
  }
  
  public func stopRun() {
    print("stopRun")
  }
  
  public func checkValidity() -> Error? {
    return nil
  }
}

//public class WaypointMissionTimelineElement: DJIMutableWaypointMission {
//
//  init(parameters: NSDictionary) {
//    super.init()
//
//    self.finishedAction = DJIWaypointMissionFinishedAction.noAction
//
//    guard let autoFlightSpeedParameter = parameters["autoFlightSpeed"] as? Float else {
//      // TODO: (Adam) Throw an error here!
//      return
//    }
//
//    guard let maxFlightSpeedParameter = parameters["maxFlightSpeed"] as? Float else {
//      // TODO: (Adam) Throw an error here!
//      return
//    }
//
//    guard let waypointsParameter = parameters["waypoints"] as? [Any] else {
//      // TODO: (Adam) Throw an error here!
//      return
//    }
//
//
//
//    self.autoFlightSpeed = autoFlightSpeedParameter
//    self.maxFlightSpeed = maxFlightSpeedParameter
//
//    // TODO: (Adam) Validate each waypoint!
//    for case let waypointData as [String: Double] in waypointsParameter {
//      let waypointCoordinate = CLLocationCoordinate2D.init(latitude: waypointData["latitude"]!, longitude: waypointData["longitude"]!)
//      let waypoint = DJIWaypoint.init(coordinate: waypointCoordinate)
//      waypoint.altitude = Float(waypointData["altitude"]!)
//      self.add(waypoint)
//    }
//
//  }
//}
