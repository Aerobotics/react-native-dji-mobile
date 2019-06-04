//
//  VirtualStickTimelineElement.swift
//  ReactNativeDjiMobile
//
//  Created by Adam Rosendorff on 2019/05/06.
//

import Foundation
import DJISDK

enum VirtualStickControl: String, CaseIterable {
  case pitch
  case roll
  case yaw
  case verticalThrottle
}

enum ControllerStickAxis: String {
  case leftHorizontal
  case leftVertical
  case rightHorizontal
  case rightVertical
}

enum Parameters: String {
  case baseVirtualStickControlValues
  case doNotStopVirtualStickOnEnd
  case stopExistingVirtualStick
  case waitForControlSticksReleaseOnEnd
  case endTrigger
  case timerEndTime
  case ultrasonicEndDistance
  case ultrasonicDecreaseVerticalThrottleWithDistance
  case enableObstacleAvoidance
  case controlStickAdjustments
}

enum EndTrigger: String {
  case timer
  case ultrasonic
}

let adjustmentStickModeNames: [AdjustmentStickMode] = [
  .pitchControllerStickAdjustment,
  .rollControllerStickAdjustment,
  .verticalThrottleControllerStickAdjustment,
  .yawControllerStickAdjustment,
]

let CONTROLLER_STICK_LIMIT = 660.0
let sendVirtualStickDataTimerPeriod = 0.05

public class VirtualStickTimelineElement: NSObject, DJIMissionControlTimelineElement {
  
  var parameters: NSDictionary
  
  var sendVirtualStickDataTimer: Timer
  var endTriggerTimer: Timer
  var waitForControlsResetTimer: Timer
  var secondsUntilEndTrigger: TimeInterval?
  
  var endTrigger: EndTrigger
  var timerEndTime: Double
  
  var stopExistingVirtualStick = false
  var doNotStopVirtualStickOnEnd = false
  var waitForControlSticksReleaseOnEnd = false
  
  var virtualStickAdjustmentValues = [
    VirtualStickControl.pitch: 0.0,
    VirtualStickControl.roll: 0.0,
    VirtualStickControl.yaw: 0.0,
    VirtualStickControl.verticalThrottle: 0.0,
  ]
  
  var baseVirtualStickControlValues = [
    VirtualStickControl.pitch: 0.0,
    VirtualStickControl.roll: 0.0,
    VirtualStickControl.yaw: 0.0,
    VirtualStickControl.verticalThrottle: 0.0,
  ]
  
  init(_ parameters: NSDictionary) {
    
    // Create empty timers for now
    self.sendVirtualStickDataTimer = Timer.init()
    self.endTriggerTimer = Timer.init()
    self.waitForControlsResetTimer = Timer.init()
    super.init()
    
    if let stopExistingVirtualStick = parameters[Parameters.stopExistingVirtualStick] as? Bool {
      self.stopExistingVirtualStick = stopExistingVirtualStick
    }
    
    if (self.stopExistingVirtualStick != true) {
      
      if let baseVirtualStickControlValuesInput = parameters[Parameters.baseVirtualStickControlValues] as? NSDictionary {
        for (key, value) in baseVirtualStickControlValuesInput {
          baseVirtualStickControlValues[VirtualStickControl.init(rawValue: key as! String)!] = (value as! Double)
        }
      }
      
      if let doNotStopVirtualStickOnEnd = parameters[Parameters.doNotStopVirtualStickOnEnd] as? Bool {
        self.doNotStopVirtualStickOnEnd = doNotStopVirtualStickOnEnd
      }
      
      if let waitForControlSticksReleaseOnEnd = parameters[Parameters.waitForControlSticksReleaseOnEnd] as? Bool {
        self.waitForControlSticksReleaseOnEnd = waitForControlSticksReleaseOnEnd
      }
      
      if let endTrigger = parameters[Parameters.endTrigger] as? String {
        self.endTrigger = EndTrigger.init(rawValue: endTrigger)
      }
      
      if let timerEndTime = parameters[Parameters.timerEndTime] as? Double {
        self.timerEndTime = timerEndTime
      }
      
      if let controlStickAdjustments = parameters[Parameters.controlStickAdjustments] as? NSDictionary {
        
        for virtualStickControl in VirtualStickControl.allCases {
          if let adjustmentStickParameters = controlStickAdjustments[virtualStickControl] as? NSDictionary {
            let controllerStickAxis = ControllerStickAxis.init(rawValue: adjustmentStickParameters["axis"] as! String)
            var minSpeed: Double
            let maxSpeed = adjustmentStickParameters["maxSpeed"] as! Double
            if (virtualStickControl == .yaw) { // For yaw the max (cw & ccw) rotation speed is defined, instead of a min max value
              minSpeed = -maxSpeed
            } else {
              minSpeed = adjustmentStickParameters["minSpeed"] as! Double
            }
          }
        }
      }
      
    }
  }
  
  private func implementControlStickAdjustment(virtualStickControl: VirtualStickControl, controllerStickAxis: ControllerStickAxis, minSpeed: Double, maxSpeed: Double) {
    
    var controllerStickKeyParam: String
    
    switch controllerStickAxis {
    case .leftHorizontal:
      adjustmentStickKey = DJIRemoteControllerParamLeftHorizontalValue
    case .leftVertical:
      adjustmentStickKey = DJIRemoteControllerParamLeftVerticalValue
    case .rightHorizontal:
      adjustmentStickKey = DJIRemoteControllerParamRightHorizontalValue
    case .rightVertical:
      adjustmentStickKey = DJIRemoteControllerParamRightVerticalValue
    }
    
    DJISDKManager.keyManager()?.startListeningForChanges(on: DJIRemoteControllerKey(param: controllerStickKeyParam)!, withListener: self, andUpdate: { (oldValue: DJIKeyedValue?, newValue: DJIKeyedValue?) in
      if (newValue != nil) {
        let stickValue = newValue!.integerValue
        let rescaledStickValue = self.rescaleControllerStickValue(stickValue, minSpeed, maxSpeed)
        self.virtualStickAdjustmentValues[virtualStickControl] = rescaledStickValue
      }
    })
  }
  
  private func rescaleControllerStickValue(controllerStickValue: Integer, minSpeed: Double, maxSpeed: Double) -> Double {
    if (controllerStickValue < 0) {
      return Double(controllerStickValue) / (-CONTROLLER_STICK_LIMIT / minSpeed)
    } else if (stickValue > 0) {
      return Double(controllerStickValue) / (CONTROLLER_STICK_LIMIT / maxSpeed)
    }
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
  
  private func stopVirtualStick(withCompletion: @escaping (Error?) -> ()) {
    let flightController = (DJISDKManager.product() as! DJIAircraft).flightController
    flightController?.setVirtualStickModeEnabled(false, withCompletion: { (error: Error?) in
      withCompletion(error)
    })
  }
  
  @objc
  private func sendVirtualStickData() {
    let flightController = (DJISDKManager.product() as! DJIAircraft).flightController
    
    var virtualStickData = [VirtualStickControl: Double]
    
    for virtualStickControl in VirtualStickControl.allCases {
      virtualStickData[virtualStickControl] = self.baseVirtualStickControlValues[virtualStickControl]! + self.virtualStickAdjustmentValues[virtualStickControl]!
    }
    
    flightController?.send(
      DJIVirtualStickFlightControlData(
        // In the coordinate system used for the drone, roll and pitch are swapped
        pitch: virtualStickData[.roll],
        roll: virtualStickData[.pitch],
        yaw: virtualStickData[.yaw],
        verticalThrottle: virtualStickData[.verticalThrottle]
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
  
  
  
  public func run() {
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
        
        self.sendVirtualStickDataTimer = Timer.scheduledTimer(timeInterval: sendVirtualStickDataTimerPeriod, target: self, selector: #selector(self.sendVirtualStickData), userInfo: nil, repeats: true)
        
        if (self.endTrigger == .timer) {
          self.endTriggerTimer = Timer.scheduledTimer(timeInterval: self.timerEndTime, target: self, selector: #selector(self.endTriggerTimerDidTrigger), userInfo: nil, repeats: false)
        }
        
      })
    }
  }
  
  public func isPausable() -> Bool {
    return true
  }
  
  public func pauseRun() {
    self.sendVirtualStickDataTimer.invalidate()
    if (self.endTrigger == .timer) {
      self.secondsUntilEndTrigger = self.endTriggerTimer.fireDate.timeIntervalSinceNow
      self.endTriggerTimer.invalidate()
    }
  }
  
  public func resumeRun() {
    self.sendVirtualStickDataTimer = Timer.scheduledTimer(timeInterval: sendVirtualStickDataTimerPeriod, target: self, selector: #selector(self.sendVirtualStickData), userInfo: nil, repeats: true)
    if (self.endTrigger == .timer) {
      self.endTriggerTimer = Timer.scheduledTimer(timeInterval: self.secondsUntilEndTrigger!, target: self, selector: #selector(self.endTriggerTimerDidTrigger), userInfo: nil, repeats: false)
    }
  }
  
  public func stopRun() {
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
  
}
