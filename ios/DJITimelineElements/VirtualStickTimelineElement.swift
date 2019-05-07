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
  
  var parameters: NSDictionary
  
  var sendVirtualStickDataTimer: Timer
  var endTriggerTimer: Timer
  
  var pitchAdjustmentStick = 0
  var rollAdjustmentStick = 0
  var verticalAdjustmentStick = 0
  
  init(_ parameters: NSDictionary) {
    
    self.parameters = parameters
    
    self.sendVirtualStickDataTimer = Timer.init()
    self.endTriggerTimer = Timer.init()
    self.timer = Timer.init() // Create an empty timer for now
    super.init()
  }
  
  public func run() {
    print("run")
    
    let flightController = (DJISDKManager.product() as! DJIAircraft).flightController
    let missionControl = DJISDKManager.missionControl()
    
    // Using velocity and body for controlMode and coordinateSystem respectively means that a positive pitch corresponds to a roll to the right,
    // and a positive roll corresponds to a pitch forwards, THIS IS THE CRAZY DJI SDK AND WE HAVE TO LIVE WITH IT
    flightController?.rollPitchControlMode = DJIVirtualStickRollPitchControlMode.velocity
    flightController?.rollPitchCoordinateSystem = DJIVirtualStickFlightCoordinateSystem.body
    
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
      
      self.sendVirtualStickDataTimer = Timer.scheduledTimer(timeInterval: 0.1, target: self, selector: #selector(self.sendVirtualStickData), userInfo: nil, repeats: true)
      
      let endTrigger = self.parameters["endTrigger"] as! String
      print(endTrigger)
      
      if (endTrigger == "timer") {
        let timerEndTime = self.parameters["timerEndTime"] as! Double
        self.endTriggerTimer = Timer.scheduledTimer(timeInterval: timerEndTime, target: self, selector: #selector(self.endTriggerTimerDidTrigger), userInfo: nil, repeats: false)
      }
      
      
      
      
//      DJISDKManager.keyManager()?.startListeningForChanges(on: DJIRemoteControllerKey(param: DJIRemoteControllerParamRightHorizontalValue)!, withListener: self, andUpdate: { (oldValue: DJIKeyedValue?, newValue: DJIKeyedValue?) in
//        if (newValue != nil) {
//          self.rollAdjustmentStick = newValue!.integerValue
//        }
//      })
//
//      DJISDKManager.keyManager()?.startListeningForChanges(on: DJIRemoteControllerKey(param: DJIRemoteControllerParamRightVerticalValue)!, withListener: self, andUpdate: { (oldValue: DJIKeyedValue?, newValue: DJIKeyedValue?) in
//        if (newValue != nil) {
//          self.pitchAdjustmentStick = newValue!.integerValue
//        }
//      })
//
//      missionControl?.elementDidStartRunning(self)
//
//      self.timer = Timer.scheduledTimer(timeInterval: 0.1, target: self, selector: #selector(self.timerBlock), userInfo: nil, repeats: true)
      
    })
    
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
//    print("stopRun")
    let missionControl = DJISDKManager.missionControl()
    self.cleanUp { (error: Error?) in
      if (error != nil) {
        missionControl?.element(self, failedStoppingWithError: error!)
      } else {
        missionControl?.elementDidStopRunning(self)
      }
      
    }
  }
  
  public func checkValidity() -> Error? {
    return nil
  }
  
  private func cleanUp(withCompletion: (Error?) -> ()) {
    self.sendVirtualStickDataTimer.invalidate()
    self.endTriggerTimer.invalidate()
    let flightController = (DJISDKManager.product() as! DJIAircraft).flightController
    let missionControl = DJISDKManager.missionControl()
    flightController?.setVirtualStickModeEnabled(false, withCompletion: { (error: Error?) in
      missionControl?.element(self, didFinishRunningWithError: error)
    })
  }
  
  @objc
  private func sendVirtualStickData() {
    let flightController = (DJISDKManager.product() as! DJIAircraft).flightController
    
    flightController?.send(DJIVirtualStickFlightControlData(pitch: Float(self.rollAdjustmentStick) / 330, roll: Float(self.pitchAdjustmentStick) / 330, yaw: 0, verticalThrottle: 1), withCompletion: nil)
    
  }
  
  @objc
  private func endTriggerTimerDidTrigger() {
    self.sendVirtualStickDataTimer.invalidate()
    self.cleanUp { (erorr: Error?) in
      DJISDKManager.missionControl()?.element(self, didFinishRunningWithError: erorr)
    }
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
