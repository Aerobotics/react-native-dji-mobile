//
//  PictureTimelineElement.swift
//  ReactNativeDjiMobile
//
//  Created by Adam Rosendorff on 2019/03/05.
//

import Foundation
import DJISDK

public class CapturePictureTimelineElement: NSObject, DJIMissionControlTimelineElement {
  
  public func run() {
    print("RUNNING CapturePictureTimelineElement")
  }
  
  public func isPausable() -> Bool {
    return false
  }
  
  public func stopRun() {
    print("STOPPING CapturePictureTimelineElement")
  }
  
  public func checkValidity() -> Error? {
    return nil
  }
  
}
