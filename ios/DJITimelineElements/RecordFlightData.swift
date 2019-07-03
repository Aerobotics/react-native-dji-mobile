//
//  RecordFlightData.swift
//  ReactNativeDjiMobile
//
//  Created by Aerobotics on 2019/06/03.
//

import Foundation
import DJISDK

private enum Parameters: String {
  case stopRecordFlightData
  case fileName
}

public class RecordFlightData: NSObject, DJIMissionControlTimelineElement {
  
  private var stopRecordFlightData = false
  private var fileName: String?
  
  init(_ parameters: NSDictionary) {
    super.init()
    
    if let fileName = parameters[Parameters.fileName.rawValue] as? String {
      self.fileName = fileName
    }
    
    if let stopRecordFlightData = parameters[Parameters.stopRecordFlightData.rawValue] as? Bool {
      self.stopRecordFlightData = stopRecordFlightData
    }
  }
  
  public func run() {
    if (self.stopRecordFlightData == true) {
      DJIRealTimeDataLogger.stopLogging()
    } else {
      DJIRealTimeDataLogger.startLogging(fileName: fileName!)
    }
    DJISDKManager.missionControl()?.element(self, didFinishRunningWithError: nil)
  }
  
  public func isPausable() -> Bool {
    return false
  }
  
  public func stopRun() {
    DJIRealTimeDataLogger.stopLogging()
    DJISDKManager.missionControl()?.elementDidStopRunning(self)
  }
  
  public func checkValidity() -> Error? {
    return nil
  }
  
}
