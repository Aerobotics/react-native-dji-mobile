//
//  VirtualStickTimelineElement.swift
//  ReactNativeDjiMobile
//
//  Created by Adam Rosendorff on 2019/05/06.
//

import Foundation
import DJISDK

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
  
  var test = 0
  var timer: Timer
  
  var parameters: NSDictionary
  
  var sendVirtualStickDataTimer: Timer
  var endTriggerTimer: Timer
  
  var adjustmentStickValues = [
    AdjustmentStickMode.pitchControllerStickAdjustment: 0.0,
    AdjustmentStickMode.rollControllerStickAdjustment: 0.0,
    AdjustmentStickMode.yawControllerStickAdjustment: 0.0,
    AdjustmentStickMode.verticalThrottleControllerStickAdjustment: 0.0,
  ]
  
  init(_ parameters: NSDictionary) {
    
    self.parameters = parameters
    self.sendVirtualStickDataTimer = Timer.init()
    self.endTriggerTimer = Timer.init()
    self.timer = Timer.init() // Create an empty timer for now
    super.init()
    
    for adjustmentStick in adjustmentStickModeNames {
      if let adjustmentStickParameters = parameters[adjustmentStick.rawValue] as? [String: Any] {
        print(adjustmentStickParameters)
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
  
  public func run() {
    print("run")
    
    let flightController = (DJISDKManager.product() as! DJIAircraft).flightController
    let missionControl = DJISDKManager.missionControl()
    
    // Using velocity and body for controlMode and coordinateSystem respectively means that a positive pitch corresponds to a roll to the right,
    // and a positive roll corresponds to a pitch forwards, THIS IS THE DJI SDK AND WE HAVE TO LIVE WITH IT
    flightController?.rollPitchControlMode = DJIVirtualStickRollPitchControlMode.velocity
    flightController?.yawControlMode = DJIVirtualStickYawControlMode.angularVelocity
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
  
  private func implementControllerStickAdjustment(mode: AdjustmentStickMode, stick: ControllerStickAxis, minValue: Double, maxValue: Double) {
    
    print(mode)
    print(stick)
    print(minValue)
    print(maxValue)
    
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
        
        print(stickValue)
        
        self.adjustmentStickValues[mode] = stickValue
      }
    })
    
  }
  
  private func cleanUp(withCompletion: (Error?) -> ()) {
    self.sendVirtualStickDataTimer.invalidate()
    self.endTriggerTimer.invalidate()
    let flightController = (DJISDKManager.product() as! DJIAircraft).flightController
    let missionControl = DJISDKManager.missionControl()
    flightController?.setVirtualStickModeEnabled(false, withCompletion: { (error: Error?) in
      missionControl?.element(self, didFinishRunningWithError: error)
    })
    
    DJISDKManager.keyManager()?.stopAllListening(ofListeners: self)
  }
  
  @objc
  private func sendVirtualStickData() {
    let flightController = (DJISDKManager.product() as! DJIAircraft).flightController
    
    flightController?.send(
      DJIVirtualStickFlightControlData(
        pitch: Float(self.adjustmentStickValues[.rollControllerStickAdjustment]!),
        roll: Float(self.adjustmentStickValues[.pitchControllerStickAdjustment]!) + 1,
        yaw: Float(self.adjustmentStickValues[.yawControllerStickAdjustment]!),
        verticalThrottle: Float(self.adjustmentStickValues[.verticalThrottleControllerStickAdjustment]!)
      ),
      withCompletion: nil)
    
  }
  
  @objc
  private func endTriggerTimerDidTrigger() {
    self.sendVirtualStickDataTimer.invalidate()
    self.cleanUp { (erorr: Error?) in
      DJISDKManager.missionControl()?.element(self, didFinishRunningWithError: erorr)
    }
  }
  
}
