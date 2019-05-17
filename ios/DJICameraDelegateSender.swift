//
//  DJICameraDelegateSender.swift
//  ReactNativeDjiMobile
//
//  Created by Adam Rosendorff on 2019/05/17.
//  Copyright © 2019 Aerobotics. All rights reserved.
//

import Foundation
import DJISDK

public enum CameraEventTypes {
  case didUpdateSystemState
  case didUpdateFocusState
  case didGenerateNewMediaFile
  case didGenerateTimeLapsePreview
  case didUpdateSDCardState
  case didUpdateStorageState
  case didUpdateSSDState
}

class DJICameraDelegateSender: NSObject, DJICameraDelegate {
  
  override init() {
    super.init()
    
    let cameraConnectedKey = DJICameraKey(param: DJIParamConnection)!
    
    DispatchQueue.main.asyncAfter(deadline: .now() + 1) {
      // First check if the camera is already connected
      DJISDKManager.keyManager()?.getValueFor(cameraConnectedKey, withCompletion: { (value: DJIKeyedValue?, error: Error?) in
        if (error != nil) {
          print(error!.localizedDescription)
        }
        if let isCameraConnected = value?.boolValue {
          if (isCameraConnected) {
            guard let camera = DJISDKManager.product()?.camera else {
              return
            }
            camera.delegate = self
          }
        }
      })
      
      // Set up an event to listen to camera connection changes, in case of a disconnect and reconnect
      DJISDKManager.keyManager()?.startListeningForChanges(on: cameraConnectedKey, withListener: self) { (oldValue: DJIKeyedValue?, newValue: DJIKeyedValue?) in
        if let connected = newValue?.boolValue {
          if (connected == true) {
            guard let camera = DJISDKManager.product()?.camera else {
              return
            }
            camera.delegate = self
          }
        }
      }
    }
    
//    NotificationCenter.default.addObserver(self, selector: #selector(test), name: NSNotification.Name("DJICameraEvent"), object: nil)
  }
  
  public func camera(_ camera: DJICamera, didUpdate systemState: DJICameraSystemState) {
    NotificationCenter.default.post(name: Notification.Name("DJICameraEvent"), object: nil, userInfo: ["type": CameraEventTypes.didUpdateSystemState, "value": systemState])
  }
//
//  @objc private func test(payload: NSNotification) {
//    // Only send events if the JS bridge has loaded
//    let type = payload.userInfo!["type"] as! String
//    let value = payload.userInfo!["value"]!
//    print(type)
//    print(value)
//  }
}
