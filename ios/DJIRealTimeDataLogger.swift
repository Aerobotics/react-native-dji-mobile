//
//  DroneRealTimeDataLogger.swift
//  ReactNativeDjiMobile
//
//  Created by Adam Rosendorff on 2019/05/15.
//  Copyright © 2019 Facebook. All rights reserved.
//

import Foundation
import DJISDK

enum ErrorsToThrow: Error {
  case noKeyManager
}

class DJIRealTimeDataLogger {
  
  public func startLogging(fileName: String, withCompletion: (Error?) -> ()) {
    guard let keyManager = DJISDKManager.keyManager() else {
      withCompletion(ErrorsToThrow.noKeyManager)
      return
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
          "velocity_x": self.roundDecimalPlaces(number: velocity.x, decimalPlaces: 2),
          "velocity_y": self.roundDecimalPlaces(number: velocity.y, decimalPlaces: 2),
          "velocity_z": self.roundDecimalPlaces(number: velocity.z, decimalPlaces: 2),
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
    
    withCompletion(nil)
  }
  
  public func stopLogging(withCompletion: (Error?) -> ()) {
    if let keyManager = DJISDKManager.keyManager() {
      keyManager.stopAllListening(ofListeners: self)
      withCompletion(nil)
    } else {
      withCompletion(ErrorsToThrow.noKeyManager)
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
      df.dateFormat =  "yyyy-MM-dd'T'HH:mm:ss.SSSS"
      
      for (key, value) in data {
        var fileEntry = df.string(from: currentDate)
        fileEntry += " " + key + ":" + String(describing: value)
        fileEntry += "\n"
        file.write(fileEntry.data(using: .utf8)!)
      }
      file.closeFile()
    } catch {
      print(error)
    }
  }
}
