//
//  DroneRealTimeDataLogger.swift
//  ReactNativeDjiMobile
//
//  Created by Adam Rosendorff on 2019/05/15.
//  Copyright © 2019 Facebook. All rights reserved.
//

import Foundation
import DJISDK

private enum ErrorsToThrow: Error {
  case noKeyManager
  case noCamera
  case alreadyLogging
}

private struct PreviousCameraState {
  var isShootingSinglePhoto = false
  var isRecording = false
}

@objc(DJIRealTimeDataLogger)
class DJIRealTimeDataLogger: NSObject, RCTInvalidating {
  
  private var previousCameraState = PreviousCameraState()
  private var fileName = ""
  private var isLogging = false
  
  override init() {
    super.init()
    
    NotificationCenter.default.addObserver(self, selector: #selector(processEvent), name: NSNotification.Name("DJIRealTimeDataLoggerEvent"), object: nil)
  }
  
  public func invalidate() {
    if (self.isLogging) {
      self.stopLoggingInternal()
      NotificationCenter.default.removeObserver(self)
    }
  }
  
  static func startLogging(fileName: String) {
    NotificationCenter.default.post(name: Notification.Name("DJIRealTimeDataLoggerEvent"), object: nil, userInfo: ["fileName": fileName])
  }
  
  static func stopLogging() {
    NotificationCenter.default.post(name: Notification.Name("DJIRealTimeDataLoggerEvent"), object: nil, userInfo: ["stopLogging": true])
  }
  
  @objc private func processEvent(payload: NSNotification) {
    if let stopLogging = payload.userInfo!["stopLogging"] as? Bool {
      self.stopLoggingInternal()
    } else if let fileName = payload.userInfo!["fileName"] as? String {
      self.startLoggingInternal(fileName)
    }
  }
  
  private func startLoggingInternal(_ fileName: String) {
    
    if (self.isLogging == true) {
      //      withCompletion(ErrorsToThrow.alreadyLogging)
      return
    }
    
    guard let keyManager = DJISDKManager.keyManager() else {
      //      withCompletion(ErrorsToThrow.noKeyManager)
      return
    }
    
    NotificationCenter.default.addObserver(self, selector: #selector(cameraSystemStateUpdate), name: CameraEvent.didUpdateSystemState.notification, object: nil)
    
    self.fileName = fileName
    self.isLogging = true
    
    // First get the aircraft model type and write it to the file
    if let modelName = DJISDKManager.product()?.model {
      self.writeDataToLogFile(fileName: fileName, data: [
        "modelName": modelName,
        ])
    } else {
      self.writeDataToLogFile(fileName: fileName, data: [
        "modelName": "undefined",
        ])
    }
    
    keyManager.startListeningForChanges(on: DJIFlightControllerKey(param: DJIFlightControllerParamAircraftLocation)!, withListener: self) { (oldValue: DJIKeyedValue?, newValue: DJIKeyedValue?) in
      if let location = newValue?.value as? CLLocation {
        let longitude = self.roundDecimalPlaces(number: location.coordinate.longitude, decimalPlaces: 7)
        let latitude = self.roundDecimalPlaces(number: location.coordinate.latitude, decimalPlaces: 7)
        let altitude = self.roundDecimalPlaces(number: location.altitude, decimalPlaces: 2)
        self.writeDataToLogFile(fileName: fileName, data: [
          "longitude": longitude,
          "latitude": latitude,
          "altitude": altitude,
          ])
      }
    }
    
    keyManager.startListeningForChanges(on: DJIFlightControllerKey(param: DJIFlightControllerParamUltrasonicHeightInMeters)!, withListener: self) { (oldValue: DJIKeyedValue?, newValue: DJIKeyedValue?) in
      if let ultrasonicHeight = newValue?.doubleValue {
        self.writeDataToLogFile(fileName: fileName, data: [
          "ultrasonic_height": self.roundDecimalPlaces(number: ultrasonicHeight, decimalPlaces: 2),
          ])
      }
    }
    
    keyManager.startListeningForChanges(on: DJIFlightControllerKey(param: DJIFlightControllerParamAttitude)!, withListener: self) { (oldValue: DJIKeyedValue?, newValue: DJIKeyedValue?) in
      if let attitude = newValue?.value as? DJISDKVector3D {
        // attitude.x = aircraft roll (left is -ve)
        // attitude.y = aircraft pitch (forwards is -ve)
        // altitude.z = aircraft yaw
        self.writeDataToLogFile(fileName: fileName, data: [
          "drone_pitch": self.roundDecimalPlaces(number: -attitude.y, decimalPlaces: 2),
          "drone_roll": self.roundDecimalPlaces(number: attitude.x, decimalPlaces: 2),
          "drone_yaw": self.roundDecimalPlaces(number: attitude.z, decimalPlaces: 2),
          ])
      }
    }
    
    keyManager.startListeningForChanges(on: DJIFlightControllerKey(param: DJIFlightControllerParamVelocity)!, withListener: self) { (oldValue: DJIKeyedValue?, newValue: DJIKeyedValue?) in
      if let velocity = newValue?.value as? DJISDKVector3D {
        self.writeDataToLogFile(fileName: fileName, data: [
          "velocity_n": self.roundDecimalPlaces(number: velocity.x, decimalPlaces: 4),
          "velocity_e": self.roundDecimalPlaces(number: velocity.y, decimalPlaces: 4),
          "velocity_d": self.roundDecimalPlaces(number: velocity.z, decimalPlaces: 4),
          ])
      }
    }
    
    // As the gimbal may not move for the duration of the flight (it may already be in position), we need to record its initial value
    keyManager.getValueFor(DJIGimbalKey(param: DJIGimbalParamAttitudeInDegrees)!) { (value: DJIKeyedValue?, error: Error?) in
      if (error == nil) {
        if let value = value?.value as? NSValue {
          var gimbalAttitude = DJIGimbalAttitude(pitch: 0, roll: 0, yaw: 0)
          value.getValue(&gimbalAttitude)
          self.writeDataToLogFile(fileName: fileName, data: [
            "gimbal_pitch": self.roundDecimalPlaces(number: Double(gimbalAttitude.pitch), decimalPlaces: 2),
            "gimbal_roll": self.roundDecimalPlaces(number: Double(gimbalAttitude.roll), decimalPlaces: 2),
            "gimbal_yaw": self.roundDecimalPlaces(number: Double(gimbalAttitude.yaw), decimalPlaces: 2),
            ])
        }
      }
    }
    
    keyManager.startListeningForChanges(on: DJIGimbalKey(param: DJIGimbalParamAttitudeInDegrees)!, withListener: self) { (oldValue: DJIKeyedValue?, newValue: DJIKeyedValue?) in
      if let value = newValue?.value as? NSValue {
        var gimbalAttitude = DJIGimbalAttitude(pitch: 0, roll: 0, yaw: 0)
        value.getValue(&gimbalAttitude)
        self.writeDataToLogFile(fileName: fileName, data: [
          "gimbal_pitch": self.roundDecimalPlaces(number: Double(gimbalAttitude.pitch), decimalPlaces: 2),
          "gimbal_roll": self.roundDecimalPlaces(number: Double(gimbalAttitude.roll), decimalPlaces: 2),
          "gimbal_yaw": self.roundDecimalPlaces(number: Double(gimbalAttitude.yaw), decimalPlaces: 2),
          ])
      }
    }
    
    // Get initial compass heading for same reason as gimbal attitude
    keyManager.getValueFor(DJIFlightControllerKey(param: DJIFlightControllerParamCompassHeading)!) { (value: DJIKeyedValue?, error: Error?) in
      if (error == nil) {
        if let heading = value?.doubleValue {
          self.writeDataToLogFile(fileName: fileName, data: [
            "compass_heading": self.roundDecimalPlaces(number: heading, decimalPlaces: 2),
            ])
        }
      }
    }
    
    keyManager.startListeningForChanges(on: DJIFlightControllerKey(param: DJIFlightControllerParamCompassHeading)!, withListener: self) { (oldValue: DJIKeyedValue?, newValue: DJIKeyedValue?) in
      if let heading = newValue?.doubleValue {
        self.writeDataToLogFile(fileName: fileName, data: [
          "compass_heading": self.roundDecimalPlaces(number: heading, decimalPlaces: 2),
          ])
      }
    }
    
    //    withCompletion(nil)
  }
  
  private func stopLoggingInternal() {
    self.isLogging = false
    
    if let keyManager = DJISDKManager.keyManager() {
      keyManager.stopAllListening(ofListeners: self)
    } else {
      //      withCompletion(ErrorsToThrow.noKeyManager)
      return
    }
    
    NotificationCenter.default.removeObserver(self, name: NSNotification.Name("DJICameraEvent.didUpdateSystemState"), object: nil)
    
    //    withCompletion(nil)
  }
  
  @objc private func cameraSystemStateUpdate(payload: NSNotification) {
    let systemState = payload.userInfo!["value"] as! DJICameraSystemState
    let isShootingSinglePhoto = systemState.isShootingSinglePhoto
    let isRecording = systemState.isRecording
    
    if (isShootingSinglePhoto != self.previousCameraState.isShootingSinglePhoto) {
      self.previousCameraState.isShootingSinglePhoto = isShootingSinglePhoto
      if (isShootingSinglePhoto == true && self.isLogging) {
        self.writeDataToLogFile(fileName: self.fileName, data: [
          "camera": "startCapturePhoto"
          ])
      }
    }
    
    if (isRecording != self.previousCameraState.isRecording) {
      self.previousCameraState.isRecording = isRecording
      if (self.isLogging) {
        if (isRecording == true) {
          self.writeDataToLogFile(fileName: self.fileName, data: [
            "camera": "startCaptureVideo"
            ])
        } else {
          self.writeDataToLogFile(fileName: self.fileName, data: [
            "camera": "stopCaptureVideo"
            ])
        }
      }
    }
  }
  
  private func roundDecimalPlaces(number: Double, decimalPlaces: Int) -> Double {
    let multiple = pow(Double(10), Double(decimalPlaces))
    return round(number * multiple) / multiple
  }
  
  private func writeDataToLogFile(fileName: String, data: [String: Any]) {
    let fileManager = FileManager.default
    
    let documentUrl = try! fileManager.url(
      for: .documentDirectory,
      in: .userDomainMask,
      appropriateFor: nil,
      create: true
    )
    
    let fileUrl = documentUrl.appendingPathComponent(fileName).appendingPathExtension("txt")
    
    do {
      
      let fileExists = try? fileUrl.checkResourceIsReachable()
      if (fileExists != true) {
        try "".write(to: fileUrl, atomically: true, encoding: .utf8)
      }
      let file = try FileHandle(forUpdating: fileUrl)
      //      if (fileExists == true) { // Start a new line if the file already exists
      file.seekToEndOfFile()
      //      }
      
      let currentDate = Date()
      let df = DateFormatter()
      df.locale = Locale(identifier: "en_US_POSIX")
      df.dateFormat =  "yyyy-MM-dd'T'HH:mm:ss.SSSS"
      
      for (key, value) in data {
        var fileEntry = df.string(from: currentDate)
        fileEntry += " " + key + ":" + String(describing: value)
        fileEntry += "\n"
        file.write(fileEntry.data(using: .utf8)!)
      }
      file.closeFile()
    } catch {
      print("Real Time Data Logger Error:")
      print(error)
    }
  }
  
  @objc static func requiresMainQueueSetup() -> Bool {
    return true
  }
}
