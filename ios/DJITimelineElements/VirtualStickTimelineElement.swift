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

let CONTROLLER_STICK_LIMIT = 660.0
let sendVirtualStickDataTimerPeriod = 0.05

public class VirtualStickTimelineElement: NSObject, DJIMissionControlTimelineElement {
  
  var sendVirtualStickDataTimer: Timer
  var endTriggerTimer: Timer
  var waitForControlSticksReleaseTimer: Timer
  var secondsUntilEndTrigger: TimeInterval?
  
  var endTrigger: EndTrigger?
  var timerEndTime: Double?
  
  var stopExistingVirtualStick = false
  var doNotStopVirtualStickOnEnd = false
  var waitForControlSticksReleaseOnEnd = false
  
  var ultrasonicEndDistance: Double?
  
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
    
    // Initialize all required values
    self.sendVirtualStickDataTimer = Timer.init()
    self.endTriggerTimer = Timer.init()
    self.waitForControlSticksReleaseTimer = Timer.init()
    super.init()
    
    if let stopExistingVirtualStick = parameters[Parameters.stopExistingVirtualStick.rawValue] as? Bool {
      self.stopExistingVirtualStick = stopExistingVirtualStick
    }
    
    if (self.stopExistingVirtualStick != true) {
      
      if let baseVirtualStickControlValuesInput = parameters[Parameters.baseVirtualStickControlValues.rawValue] as? NSDictionary {
        for (key, value) in baseVirtualStickControlValuesInput {
          baseVirtualStickControlValues[VirtualStickControl.init(rawValue: key as! String)!] = (value as! Double)
        }
      }
      
      if let doNotStopVirtualStickOnEnd = parameters[Parameters.doNotStopVirtualStickOnEnd.rawValue] as? Bool {
        self.doNotStopVirtualStickOnEnd = doNotStopVirtualStickOnEnd
      }
      
      if let waitForControlSticksReleaseOnEnd = parameters[Parameters.waitForControlSticksReleaseOnEnd.rawValue] as? Bool {
        self.waitForControlSticksReleaseOnEnd = waitForControlSticksReleaseOnEnd
      }
      
      if let endTrigger = parameters[Parameters.endTrigger.rawValue] as? String {
        self.endTrigger = EndTrigger.init(rawValue: endTrigger)!
      }
      
      if let timerEndTime = parameters[Parameters.timerEndTime.rawValue] as? Double {
        self.timerEndTime = timerEndTime
      }
      
      if let ultrasonicEndDistance = parameters[Parameters.ultrasonicEndDistance.rawValue] as? Double {
        self.ultrasonicEndDistance = ultrasonicEndDistance
      }
      
      if let controlStickAdjustments = parameters[Parameters.controlStickAdjustments.rawValue] as? NSDictionary {
        
        for virtualStickControl in VirtualStickControl.allCases {
          if let adjustmentStickParameters = controlStickAdjustments[virtualStickControl.rawValue] as? NSDictionary {
            let controllerStickAxis = ControllerStickAxis.init(rawValue: adjustmentStickParameters["axis"] as! String)!
            var minSpeed: Double
            let maxSpeed = adjustmentStickParameters["maxSpeed"] as! Double
            if (virtualStickControl == .yaw) { // For yaw the max (cw & ccw) rotation speed is defined, instead of a min max value
              minSpeed = -maxSpeed
            } else {
              minSpeed = adjustmentStickParameters["minSpeed"] as! Double
            }
            
            implementControlStickAdjustment(virtualStickControl, controllerStickAxis, minSpeed, maxSpeed)
            
          }
        }
      }
      
    }
  }
  
  private func implementControlStickAdjustment(_ virtualStickControl: VirtualStickControl, _ controllerStickAxis: ControllerStickAxis, _ minSpeed: Double, _ maxSpeed: Double) {
    
    var controllerStickKeyParam: String
    
    switch controllerStickAxis {
    case .leftHorizontal:
      controllerStickKeyParam = DJIRemoteControllerParamLeftHorizontalValue
    case .leftVertical:
      controllerStickKeyParam = DJIRemoteControllerParamLeftVerticalValue
    case .rightHorizontal:
      controllerStickKeyParam = DJIRemoteControllerParamRightHorizontalValue
    case .rightVertical:
      controllerStickKeyParam = DJIRemoteControllerParamRightVerticalValue
    }
    
    DJISDKManager.keyManager()?.startListeningForChanges(on: DJIRemoteControllerKey(param: controllerStickKeyParam)!, withListener: self, andUpdate: { (oldValue: DJIKeyedValue?, newValue: DJIKeyedValue?) in
      if (newValue != nil) {
        let stickValue = newValue!.integerValue
        let rescaledStickValue = self.rescaleControllerStickValue(stickValue, minSpeed, maxSpeed)
        self.virtualStickAdjustmentValues[virtualStickControl] = rescaledStickValue
      }
    })
  }
  
  private func rescaleControllerStickValue(_ controllerStickValue: Int, _ minSpeed: Double, _ maxSpeed: Double) -> Double {
    if (controllerStickValue < 0) {
      return Double(-controllerStickValue) / (CONTROLLER_STICK_LIMIT / minSpeed)
    } else {
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
  
  // TODO: Return the reason of error if there is one!
  private func isUltrasonicEnabled(withCompletion: @escaping (Bool) -> ()) {
    let keyManager = DJISDKManager.keyManager()
    keyManager?.getValueFor(DJIFlightControllerKey.init(param: DJIFlightControllerParamIsUltrasonicBeingUsed)!, withCompletion: { (value: DJIKeyedValue?, error: Error?) in
      if (error == nil && value != nil && value!.boolValue == true) {
        keyManager?.getValueFor(DJIFlightControllerKey.init(param: DJIFlightControllerParamDoesUltrasonicHaveError)!, withCompletion: { (value: DJIKeyedValue?, error: Error?) in
          if (error == nil && value != nil && value!.boolValue == false) {
            withCompletion(true)
            return
          } else {
            withCompletion(false)
            return
          }
        })
        
      } else {
        withCompletion(false)
        return
      }
      
    })
  }
  
  private func stopAtUltrasonicHeight(stopHeight: Double) {
    DJISDKManager.keyManager()?.startListeningForChanges(on: DJIFlightControllerKey.init(param: DJIFlightControllerParamUltrasonicHeightInMeters)!, withListener: self, andUpdate: { (oldValue: DJIKeyedValue?, newValue: DJIKeyedValue?) in
      let ultrasonicHeight = newValue!.doubleValue
      if (ultrasonicHeight <= stopHeight) {
        self.sendVirtualStickDataTimer.invalidate()
        self.cleanUp { (error: Error?) in
          DJISDKManager.missionControl()?.element(self, didFinishRunningWithError: error)
        }
      }
    })
  }
  
  @objc
  private func sendVirtualStickData() {
    let flightController = (DJISDKManager.product() as! DJIAircraft).flightController
    
    var virtualStickData: [VirtualStickControl: Double] = [:]
    
    for virtualStickControl in VirtualStickControl.allCases {
      virtualStickData[virtualStickControl] = self.baseVirtualStickControlValues[virtualStickControl]! + self.virtualStickAdjustmentValues[virtualStickControl]!
    }
    
    flightController?.send(
      DJIVirtualStickFlightControlData(
        // In the coordinate system used for the drone, roll and pitch are swapped
        pitch: Float(virtualStickData[VirtualStickControl.roll]!),
        roll: Float(virtualStickData[VirtualStickControl.pitch]!),
        yaw: Float(virtualStickData[VirtualStickControl.yaw]!),
        verticalThrottle: Float(virtualStickData[VirtualStickControl.verticalThrottle]!)
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
    flightController?.verticalControlMode = DJIVirtualStickVerticalControlMode.velocity
    flightController?.rollPitchCoordinateSystem = DJIVirtualStickFlightCoordinateSystem.body
    
    if (self.stopExistingVirtualStick == true) {
      flightController?.setVirtualStickModeEnabled(false, withCompletion: { (error: Error?) in
        self.cleanUp { (error: Error?) in
          DJISDKManager.missionControl()?.element(self, didFinishRunningWithError: error)
        }
      })
      
    } else {
      
      if (self.endTrigger == .ultrasonic) {
        
        self.isUltrasonicEnabled { (isUltrasonicEnabled: Bool) in
          print("isUltrasonicEnabled")
          print(isUltrasonicEnabled)
          if (isUltrasonicEnabled == true) {
            
            self.stopAtUltrasonicHeight(stopHeight: self.ultrasonicEndDistance!)
            
            flightController?.setVirtualStickModeEnabled(true, withCompletion: { (error: Error?) in
              if (error != nil) {
                missionControl?.element(self, failedStartingWithError: error!)
                return
              }
              
              self.sendVirtualStickDataTimer = Timer.scheduledTimer(timeInterval: sendVirtualStickDataTimerPeriod, target: self, selector: #selector(self.sendVirtualStickData), userInfo: nil, repeats: true)
              
              if (self.endTrigger == .timer) {
                self.endTriggerTimer = Timer.scheduledTimer(timeInterval: self.timerEndTime!, target: self, selector: #selector(self.endTriggerTimerDidTrigger), userInfo: nil, repeats: false)
              }
              
            })
            
          } else {
            enum UltrasonicSensorError: Error {
              case UltrasonicSensorUnavailableError(String)
            }
            missionControl?.element(self, failedStartingWithError: UltrasonicSensorError.UltrasonicSensorUnavailableError("UltrasonicSensorUnavailable"))
            
          }
          
        }
        
        
      }
      
      
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
  
}
