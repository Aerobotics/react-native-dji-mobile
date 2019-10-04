//
//  DJICameraDelegateSender.swift
//  ReactNativeDjiMobile
//
//  Created by Adam Rosendorff on 2019/05/17.
//  Copyright © 2019 Aerobotics. All rights reserved.
//

import Foundation
import DJISDK

//public enum CameraEventTypes {
//  case didUpdateSystemState
//  case didUpdateFocusState
//  case didGenerateNewMediaFile
//  case didGenerateTimeLapsePreview
//  case didUpdateSDCardState
//  case didUpdateStorageState
//  case didUpdateSSDState
//}

public enum CameraEvent: String {
  case didUpdateSystemState = "DJICameraEvent.didUpdateSystemState"
  case didGenerateNewMediaFile = "DJICameraEvent.didGenerateNewMediaFile"
  
  var notification: Notification.Name {
    return Notification.Name(self.rawValue)
  }
}

class DJICameraDelegateSender: NSObject, DJICameraDelegate {
  
  override init() {
    super.init()
    
    let cameraConnectedKey = DJICameraKey(param: DJIParamConnection)!
        
    DispatchQueue.main.asyncAfter(deadline: .now() + 1) {
      // First check if the camera is already connected
      DJISDKManager.keyManager()?.getValueFor(cameraConnectedKey, withCompletion: { (value: DJIKeyedValue?, error: Error?) in
        if (error != nil) {
          NSLog("Camera delegate error: %@", error?.localizedDescription ?? "No error" )
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
            DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
              guard let camera = DJISDKManager.product()?.camera else {
                NSLog("Camera connected but cannot access!")
                return
              }
              camera.delegate = self
            }
          }
        }
      }
    }
    
  }
  
  public func camera(_ camera: DJICamera, didUpdate systemState: DJICameraSystemState) {
    NotificationCenter.default.post(name: CameraEvent.didUpdateSystemState.notification, object: nil, userInfo: ["value": systemState])
  }
  
  public func camera(_ camera: DJICamera, didGenerateNewMediaFile newMedia: DJIMediaFile) {
    NotificationCenter.default.post(name: CameraEvent.didGenerateNewMediaFile.notification, object: nil, userInfo: ["value": newMedia])
  }
  
}
