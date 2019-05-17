//
//  VirtualStickTimelineElement.swift
//  ReactNativeDjiMobile
//
//  Created by Adam Rosendorff on 2019/05/06.
//

import Foundation
import DJISDK

//enum Parameters: String {
//  case stopVirtualStick
//  case endTrigger
//  case timerEndTime
//  case ultrasonicEndDistance
//  case ultrasonicDecreaseVerticalThrottleWithDistance
//  case enableObstacleAvoidance
//  case pitchControllerStickAdjustment
//  case rollControllerStickAdjustment
//  case verticalThrottleControllerStickAdjustment
//  case yawControllerStickAdjustment
//}

enum ControllerStickAxis: String {
  case leftHorizontal
  case leftVertical
  case rightHorizontal
  case rightVertical
}

enum AdjustmentStickMode: String {
  case pitchControllerStickAdjustment
  case rollControllerStickAdjustment
  case verticalThrottleControllerStickAdjustment
  case yawControllerStickAdjustment
}

let adjustmentStickModeNames: [AdjustmentStickMode] = [
  .pitchControllerStickAdjustment,
  .rollControllerStickAdjustment,
  .verticalThrottleControllerStickAdjustment,
  .yawControllerStickAdjustment,
]

let CONTROLLER_STICK_LIMIT = 660.0

public class VirtualStickTimelineElement: NSObject, DJIMissionControlTimelineElement {
  
  var parameters: NSDictionary
  
  var sendVirtualStickDataTimer: Timer
  var endTriggerTimer: Timer
  var waitForControlsResetTimer: Timer
  var secondsUntilEndTrigger: TimeInterval?
  
  var doNotStopVirtualStickOnEnd = false
  var stopExistingVirtualStick = false
  var waitForControlSticksReleaseOnEnd = false
  
  var adjustmentStickValues = [
    AdjustmentStickMode.pitchControllerStickAdjustment: 0.0,
    AdjustmentStickMode.rollControllerStickAdjustment: 0.0,
    AdjustmentStickMode.yawControllerStickAdjustment: 0.0,
    AdjustmentStickMode.verticalThrottleControllerStickAdjustment: 0.0,
  ]
  
  var virtualStickData = [
    "pitch": 0.0,
    "roll": 0.0,
    "yaw": 0.0,
    "verticalThrottle": 0.0,
  ]
  
  init(_ parameters: NSDictionary) {
    
    self.parameters = parameters
    // Create empty timers for now
    self.sendVirtualStickDataTimer = Timer.init()
    self.endTriggerTimer = Timer.init()
    self.waitForControlsResetTimer = Timer.init()
    super.init()
    
    if let virtualStickData = parameters["virtualStickData"] as? NSDictionary {
      print(virtualStickData)
      if let pitch = virtualStickData["pitch"] as? Double {
        self.virtualStickData["pitch"] = pitch
      }
      if let roll = virtualStickData["roll"] as? Double {
        self.virtualStickData["roll"] = roll
      }
      if let yaw = virtualStickData["yaw"] as? Double {
        self.virtualStickData["yaw"] = yaw
      }
      if let verticalThrottle = virtualStickData["verticalThrottle"] as? Double {
        self.virtualStickData["verticalThrottle"] = verticalThrottle
      }
    }
    
    if let doNotStopVirtualStickOnEnd = parameters["doNotStopVirtualStickOnEnd"] as? Bool {
      self.doNotStopVirtualStickOnEnd = doNotStopVirtualStickOnEnd
    }
    
    if let waitForControlSticksReleaseOnEnd = parameters["waitForControlSticksReleaseOnEnd"] as? Bool {
      self.waitForControlSticksReleaseOnEnd = waitForControlSticksReleaseOnEnd
    }
    
    if let stopExistingVirtualStick = parameters["stopExistingVirtualStick"] as? Bool {
      self.stopExistingVirtualStick = stopExistingVirtualStick
      
    } else {
      for adjustmentStick in adjustmentStickModeNames {
        if let adjustmentStickParameters = parameters[adjustmentStick.rawValue] as? [String: Any] {
          let axis = adjustmentStickParameters["axis"] as! String
          var maxValue: Double
          var minValue: Double
          
          if (adjustmentStick == .yawControllerStickAdjustment) { // For yaw the max rotation speed is defined, instead of a min max value
            maxValue = adjustmentStickParameters["maxYawSpeed"] as! Double
            minValue = -maxValue // Opposite direction rotation
          } else {
            maxValue = adjustmentStickParameters["maxValue"] as! Double
            minValue = adjustmentStickParameters["minValue"] as! Double
          }
          self.implementControllerStickAdjustment(mode: adjustmentStick, stick: ControllerStickAxis(rawValue: axis)!, minValue: minValue, maxValue: maxValue)
        }
      }
    }
    
    
    
  }
  
  public func run() {
    //    print("run")
    
    let flightController = (DJISDKManager.product() as! DJIAircraft).flightController
    let missionControl = DJISDKManager.missionControl()
    
    // Using velocity and body for controlMode and coordinateSystem respectively means that a positive pitch corresponds to a roll to the right,
    // and a positive roll corresponds to a pitch forwards, THIS IS THE DJI SDK AND WE HAVE TO LIVE WITH IT
    flightController?.rollPitchControlMode = DJIVirtualStickRollPitchControlMode.velocity
    flightController?.yawControlMode = DJIVirtualStickYawControlMode.angularVelocity
    flightController?.rollPitchCoordinateSystem = DJIVirtualStickFlightCoordinateSystem.body
    
    if (self.stopExistingVirtualStick == true) {
      flightController?.setVirtualStickModeEnabled(false, withCompletion: { (error: Error?) in
        self.cleanUp { (error: Error?) in
          DJISDKManager.missionControl()?.element(self, didFinishRunningWithError: error)
        }
      })
      
    } else {
      
      flightController?.setVirtualStickModeEnabled(true, withCompletion: { (error: Error?) in
        if (error != nil) {
          missionControl?.element(self, failedStartingWithError: error!)
          return
        }
        // TODO: (Adam) If setVirtualStickModeEnabled returns without an error, does that guarantee virtual stick is enabled, and when is it enabled?
        // isVirtualStickControlModeAvailable() fails the first time this is run
        
        //        if (flightController?.isVirtualStickControlModeAvailable() == false) {
        //          enum VirtualStickError: Error {
        //            case VirtualStickUnavailableError(String)
        //          }
        //          missionControl?.element(self, failedStartingWithError: VirtualStickError.VirtualStickUnavailableError("VirtualStickUnavailableError"))
        //          return
        //        }
        
        self.sendVirtualStickDataTimer = Timer.scheduledTimer(timeInterval: 0.1, target: self, selector: #selector(self.sendVirtualStickData), userInfo: nil, repeats: true)
        
        let endTrigger = self.parameters["endTrigger"] as! String
        
        if (endTrigger == "timer") {
          let timerEndTime = self.parameters["timerEndTime"] as! Double
          self.endTriggerTimer = Timer.scheduledTimer(timeInterval: timerEndTime, target: self, selector: #selector(self.endTriggerTimerDidTrigger), userInfo: nil, repeats: false)
        }
        
      })
      
    }
    
  }
  
  public func isPausable() -> Bool {
    return true
  }
  
  public func pauseRun() {
    self.sendVirtualStickDataTimer.invalidate()
    if (self.endTriggerTimer.isValid) {
      self.secondsUntilEndTrigger = self.endTriggerTimer.fireDate.timeIntervalSinceNow
      self.endTriggerTimer.invalidate()
    }
  }
  
  public func resumeRun() {
    self.sendVirtualStickDataTimer = Timer.scheduledTimer(timeInterval: 0.1, target: self, selector: #selector(self.sendVirtualStickData), userInfo: nil, repeats: true)
    if (self.secondsUntilEndTrigger != nil) {
      self.endTriggerTimer = Timer.scheduledTimer(timeInterval: self.secondsUntilEndTrigger!, target: self, selector: #selector(self.endTriggerTimerDidTrigger), userInfo: nil, repeats: false)
    }
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
  
  private func implementControllerStickAdjustment(mode: AdjustmentStickMode, stick: ControllerStickAxis, minValue: Double, maxValue: Double) {
    
    var adjustmentStickKey: String
    
    switch stick {
    case .leftHorizontal:
      adjustmentStickKey = DJIRemoteControllerParamLeftHorizontalValue
    case .leftVertical:
      adjustmentStickKey = DJIRemoteControllerParamLeftVerticalValue
    case .rightHorizontal:
      adjustmentStickKey = DJIRemoteControllerParamRightHorizontalValue
    case .rightVertical:
      adjustmentStickKey = DJIRemoteControllerParamRightVerticalValue
    default:
      break
    }
    
    DJISDKManager.keyManager()?.startListeningForChanges(on: DJIRemoteControllerKey(param: adjustmentStickKey)!, withListener: self, andUpdate: { (oldValue: DJIKeyedValue?, newValue: DJIKeyedValue?) in
      if (newValue != nil) {
        var stickValue = Double(newValue!.integerValue)
        if (stickValue < 0) {
          stickValue = Double(stickValue) / (-CONTROLLER_STICK_LIMIT / minValue)
        } else if (stickValue > 0) {
          stickValue = Double(stickValue) / (CONTROLLER_STICK_LIMIT / maxValue)
        }
        
        self.adjustmentStickValues[mode] = stickValue
      }
    })
    
  }
  
  private func cleanUp(withCompletion: @escaping (Error?) -> ()) {
    self.sendVirtualStickDataTimer.invalidate()
    self.endTriggerTimer.invalidate()
    DJISDKManager.keyManager()?.stopAllListening(ofListeners: self)
    
    if (self.doNotStopVirtualStickOnEnd == true) {
      withCompletion(nil)
    } else {
      self.stopVirtualStick { (error: Error?) in
        withCompletion(error)
      }
    }
    
  }
  
  @objc
  private func sendVirtualStickData() {
    let flightController = (DJISDKManager.product() as! DJIAircraft).flightController
    
    flightController?.send(
      DJIVirtualStickFlightControlData(
        // In the coordinate system used for the drone, roll and pitch are swapped
        pitch: Float(self.virtualStickData["roll"]! + self.adjustmentStickValues[.rollControllerStickAdjustment]!),
        roll: Float(self.virtualStickData["pitch"]! + self.adjustmentStickValues[.pitchControllerStickAdjustment]!),
        yaw: Float(self.virtualStickData["yaw"]! + self.adjustmentStickValues[.yawControllerStickAdjustment]!),
        verticalThrottle: Float(self.virtualStickData["verticalThrottle"]! + self.adjustmentStickValues[.verticalThrottleControllerStickAdjustment]!)
      ),
      withCompletion: nil)
    
  }
  
  @objc
  private func endTriggerTimerDidTrigger() {
    self.sendVirtualStickDataTimer.invalidate()
    self.cleanUp { (error: Error?) in
      DJISDKManager.missionControl()?.element(self, didFinishRunningWithError: error)
    }
  }
  
  private func stopVirtualStick(withCompletion: @escaping (Error?) -> ()) {
    let flightController = (DJISDKManager.product() as! DJIAircraft).flightController
    flightController?.setVirtualStickModeEnabled(false, withCompletion: { (error: Error?) in
      withCompletion(error)
    })
  }
  
}
