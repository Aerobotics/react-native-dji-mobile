//
//  RunJSElement
//  ReactNativeDjiMobile
//
//  Created by Aerobotics on 2019/06/03.
//

import Foundation
import DJISDK

private enum Parameters: String {
  case callbackFuncId
}

public class RunJSElement: NSObject, DJIMissionControlTimelineElement {
  
  private var callbackFuncId: NSNumber?
  
  init(_ parameters: NSDictionary) {
    super.init()
    
    if let callbackFuncId = parameters[Parameters.callbackFuncId.rawValue] as? NSNumber {
      self.callbackFuncId = callbackFuncId
    }
  }
  
  public func run() {
    EventSender.sendReactEvent(type: "RunJSElementEvent", value: ["callbackFuncId": self.callbackFuncId], realtime: true)
    DJISDKManager.missionControl()?.element(self, didFinishRunningWithError: nil)
  }
  
  public func isPausable() -> Bool {
    return false
  }
  
  public func stopRun() {
    //    EventSender.sendReactEvent(type: "RunJSElementEvent", value: ["id": self.callbackFuncId, "stop": true], realtime: true)
    DJISDKManager.missionControl()?.elementDidStopRunning(self)
  }
  
  public func checkValidity() -> Error? {
    return nil
  }
  
}
