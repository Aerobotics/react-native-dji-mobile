//
//  DJISDK.swift
//  ReactNativeDjiMobile
//
//  Created by Adam Rosendorff on 2019/01/29.

import Foundation
import DJISDK

@objc(DJIMobile)
class DJIMobile: NSObject, RCTInvalidating {

  var cameraDelegateEventSender: DJICameraDelegateSender?
  var folderMonitor = FolderMonitor()
  //  var productConnectionListener

  func invalidate() {
    // For debugging, when the Javascript side reloads, we want to remove all DJI event listeners
    if (DJISDKManager.hasSDKRegistered()) {
      DJISDKManager.keyManager()?.stopAllListening(ofListeners: self)
    }
  }

  private enum SdkEventName: String, CaseIterable {
    case ProductConnection
    case BatteryChargeRemaining
    case AircraftCompassHeading
    case AircraftLocation
    case AircraftVelocity
    case AircraftAttitude
    case AircraftGpsSignalLevel
    case AirLinkUplinkSignalQuality
    case AircraftHomeLocation
    case AircraftUltrasonicHeight
    case CompassHasError

    case CameraIsRecording
    case SDCardIsInserted
    case SDCardIsReadOnly

    case CameraDidUpdateSystemState
    case CameraDidGenerateNewMediaFile

    case DJIFlightLogEvent
  }

  private let implementedEvents: [SdkEventName: [Any]] = [
    .ProductConnection: [DJIParamConnection, DJIProductKey.self],
    .BatteryChargeRemaining: [DJIBatteryParamChargeRemainingInPercent, DJIBatteryKey.self],
    .AircraftCompassHeading: [DJIFlightControllerParamCompassHeading, DJIFlightControllerKey.self],
    .AircraftLocation: [DJIFlightControllerParamAircraftLocation, DJIFlightControllerKey.self],
    .AircraftVelocity: [DJIFlightControllerParamVelocity, DJIFlightControllerKey.self],
    .AircraftAttitude: [DJIFlightControllerParamAttitude, DJIFlightControllerKey.self],
    .AircraftGpsSignalLevel: [DJIFlightControllerParamGPSSignalStatus, DJIFlightControllerKey.self],
    .AirLinkUplinkSignalQuality: [DJIAirLinkParamUplinkSignalQuality, DJIAirLinkKey.self],
    .AircraftHomeLocation: [DJIFlightControllerParamHomeLocation, DJIFlightControllerKey.self],
    .AircraftUltrasonicHeight: [DJIFlightControllerParamUltrasonicHeightInMeters, DJIFlightControllerKey.self],
    .CompassHasError: [DJIFlightControllerParamCompassHasError, DJIFlightControllerKey.self],
    .CameraIsRecording: [DJICameraParamIsRecording, DJICameraKey.self],
    .SDCardIsInserted: [DJICameraParamSDCardIsInserted, DJICameraKey.self],
    .SDCardIsReadOnly: [DJICameraParamSDCardIsReadOnly, DJICameraKey.self],
  ]

  private var eventsBeingListenedTo: [SdkEventName] = []

  @objc(registerApp:reject:)
  func registerApp(resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    registerAppInternal(nil, resolve, reject)
  }

  @objc(registerAppAndUseBridge:resolve:reject:)
  func registerAppAndUseBridge(bridgeIp: String, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    registerAppInternal(bridgeIp, resolve, reject)
  }

  func registerAppInternal(_ bridgeIp: String?, _ resolve: @escaping RCTPromiseResolveBlock, _ reject: @escaping RCTPromiseRejectBlock) {
    var sentRegistration = false

    DJISDKManager.startListeningOnRegistrationUpdates(withListener: self) { (registered: Bool, registrationError: Error) in
      if (DJISDKManager.hasSDKRegistered() == true) {
        if (bridgeIp != nil) {
          DJISDKManager.enableBridgeMode(withBridgeAppIP: bridgeIp!)
        } else {
          DJISDKManager.startConnectionToProduct()
        }
        if (!sentRegistration) {
          resolve("DJI SDK: Registration Successful")
          sentRegistration = true
          self.cameraDelegateEventSender = DJICameraDelegateSender()

        }
      } else if (registrationError != nil) {
        if (!sentRegistration) {
          self.sendReject(reject, "Registration Error", registrationError as NSError)
          sentRegistration = true
        }
      }
    }

    DJISDKManager.beginAppRegistration()

  }

  @objc(limitEventFrequency:resolve:reject:)
  func limitEventFrequency(frequency: NSNumber, resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
    EventSender.limitEventSendFrequency(frequency: frequency.doubleValue)
    resolve("limitEventFrequency Successful")
  }

  @objc(startRecordFlightData:resolve:reject:)
  func startRecordFlightData(fileName: String, resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
    DJIRealTimeDataLogger.startLogging(fileName: fileName)
  }
  //
  @objc(stopRecordFlightData:reject:)
  func stopRecordFlightData(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
    DJIRealTimeDataLogger.stopLogging()
  }

  @objc(startEventListener:resolve:reject:)
  func startEventListener(eventName: String, resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {

    guard let sdkEventName = SdkEventName(rawValue: eventName) else {
      reject("Invalid Key", nil, nil)
      return
    }

    switch (sdkEventName) {
    case .ProductConnection:
      startProductConnectionListener()

    case .BatteryChargeRemaining:
      startBatteryPercentChargeRemainingListener()

    case .AircraftCompassHeading:
      startAircraftCompassHeadingListener()

    case .AircraftLocation:
      startAircraftLocationListener()

    case .AircraftVelocity:
      startAircraftVelocityListener()

    case .AircraftAttitude:
      startAircraftAttitudeListener()

    case .AircraftGpsSignalLevel:
      startAircraftGpsSignalLevelListener()

    case .AirLinkUplinkSignalQuality:
      startAirLinkUplinkSignalQualityListener()

    case .AircraftHomeLocation:
      startAircraftHomeLocationListener()

    case .AircraftUltrasonicHeight:
      startAircraftUltrasonicHeightListener()

    case .CompassHasError:
      startCompassHasErrorListener()

    case .CameraIsRecording:
      startCameraIsRecordingListener()

    case .SDCardIsInserted:
      startSDCardIsInsertedListener()

    case .SDCardIsReadOnly:
      startSDCardIsReadOnlyListener()

    default:
      reject("Invalid Key", nil, nil)
      return
    }

    resolve(nil)
  }

  func startProductConnectionListener() {
    let event = SdkEventName.ProductConnection
    startKeyListener(event) { (oldValue: DJIKeyedValue?, newValue: DJIKeyedValue?) in
      if let connected = newValue?.boolValue {
        EventSender.sendReactEvent(type: event.rawValue, value: connected ? "connected" : "disconnected")
      }
    }
  }

  func startBatteryPercentChargeRemainingListener() {
    let event = SdkEventName.BatteryChargeRemaining
    startKeyListener(event) { (oldValue: DJIKeyedValue?, newValue: DJIKeyedValue?) in
      if let chargePercent = newValue?.integerValue {
        EventSender.sendReactEvent(type: event.rawValue, value: chargePercent)
      }
    }
  }

  func startAircraftCompassHeadingListener() {
    startKeyListener(.AircraftCompassHeading) { (oldValue: DJIKeyedValue?, newValue: DJIKeyedValue?) in
      if let heading = newValue?.doubleValue {
        EventSender.sendReactEvent(type: SdkEventName.AircraftCompassHeading.rawValue, value: heading)
      }
    }
  }

  func startAircraftLocationListener() {
    let event = SdkEventName.AircraftLocation
    startKeyListener(event) { (oldValue: DJIKeyedValue?, newValue: DJIKeyedValue?) in
      if let location = newValue?.value as? CLLocation {
        EventSender.sendReactEvent(type: event.rawValue, value: [
          "longitude": location.coordinate.longitude,
          "latitude": location.coordinate.latitude,
          "altitude": location.altitude,
          ])
      }
    }
  }

  func startAircraftVelocityListener() {
    let event = SdkEventName.AircraftVelocity
    startKeyListener(event) { (oldValue: DJIKeyedValue?, newValue: DJIKeyedValue?) in
      if let velocity = newValue?.value as? DJISDKVector3D {
        EventSender.sendReactEvent(type: event.rawValue, value: [
          "x": velocity.x,
          "y": velocity.y,
          "z": velocity.z,
          ])
      }
    }
  }

  func startAircraftAttitudeListener() {
    let event = SdkEventName.AircraftAttitude
    startKeyListener(event) { (oldValue: DJIKeyedValue?, newValue: DJIKeyedValue?) in
      if let attitude = newValue?.value as? DJISDKVector3D {
        EventSender.sendReactEvent(type: event.rawValue, value: [
          "pitch": attitude.x,
          "roll": attitude.y,
          "yaw": attitude.z,
          ])
      }
    }
  }

  func startAircraftGpsSignalLevelListener() {
    let event = SdkEventName.AircraftGpsSignalLevel
    startKeyListener(event) { (oldValue: DJIKeyedValue?, newValue: DJIKeyedValue?) in
      if var signalLevel = newValue?.value as? UInt8? { // DJI doesn't set the value as a DJIGPSSignalLevel enum!
        if (signalLevel != nil) {
          let GpsSignal = DJIGPSSignalLevel(rawValue: signalLevel!)
          if (GpsSignal == .levelNone) {
            signalLevel = nil
          }
        }
        EventSender.sendReactEvent(type: event.rawValue, value: signalLevel)
      }
    }
  }

  func startAirLinkUplinkSignalQualityListener() {
    let event = SdkEventName.AirLinkUplinkSignalQuality
    startKeyListener(event) { (oldValue: DJIKeyedValue?, newValue: DJIKeyedValue?) in
      if let qualityPercent = newValue?.value as? UInt {
        EventSender.sendReactEvent(type: event.rawValue, value: qualityPercent)
      }
    }
  }

  func startAircraftHomeLocationListener() {
    let event = SdkEventName.AircraftHomeLocation
    startKeyListener(event) { (oldValue: DJIKeyedValue?, newValue: DJIKeyedValue?) in
      var homeCoordinate: [String: CLLocationDegrees?] = [
        "longitude": nil,
        "latitude": nil,
      ]
      if let homeLocation = newValue?.value as? CLLocation {
        homeCoordinate = [
          "longitude": homeLocation.coordinate.longitude,
          "latitude": homeLocation.coordinate.latitude,
        ]
      }
      EventSender.sendReactEvent(type: event.rawValue, value: homeCoordinate)
    }
  }

  func startAircraftUltrasonicHeightListener() {
    let event = SdkEventName.AircraftUltrasonicHeight
    startKeyListener(event) { (oldValue: DJIKeyedValue?, newValue: DJIKeyedValue?) in
      if let ultrasonicHeight = newValue?.value as? Double {
        EventSender.sendReactEvent(type: event.rawValue, value: ultrasonicHeight)
      }
    }
  }

  func startCompassHasErrorListener() {
    let event = SdkEventName.CompassHasError
    startKeyListener(event) { (oldValue: DJIKeyedValue?, newValue: DJIKeyedValue?) in
      if let hasError = newValue?.boolValue {
        EventSender.sendReactEvent(type: event.rawValue, value: hasError)
      }
    }
  }

  func startCameraIsRecordingListener() {
    let event = SdkEventName.CameraIsRecording
    startKeyListener(event) { (oldValue: DJIKeyedValue?, newValue: DJIKeyedValue?) in
      if let isRecording = newValue?.boolValue {
        EventSender.sendReactEvent(type: event.rawValue, value: isRecording)
      }
    }
  }

  func startSDCardIsInsertedListener() {
    let event = SdkEventName.SDCardIsInserted
    startKeyListener(event) { (oldValue: DJIKeyedValue?, newValue: DJIKeyedValue?) in
      if let isInserted = newValue?.boolValue {
        EventSender.sendReactEvent(type: event.rawValue, value: isInserted)
      }
    }
  }

  func startSDCardIsReadOnlyListener() {
    let event = SdkEventName.SDCardIsReadOnly
    startKeyListener(event) { (oldValue: DJIKeyedValue?, newValue: DJIKeyedValue?) in
      if let isReadOnly = newValue?.boolValue {
        EventSender.sendReactEvent(type: event.rawValue, value: isReadOnly)
      }
    }
  }

  @objc(getAircraftLocation:reject:)
  func getAircraftLocation(resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    let key = DJIFlightControllerKey(param: DJIFlightControllerParamAircraftLocation)!
    self.getKeyValue(key) { (value: DJIKeyedValue?, error: Error?) in
      if (error != nil) {
        self.sendReject(reject, "getAircraftLocation Error", error! as NSError)
      } else {
        if let location = value?.value as? CLLocation {
          resolve([
            "longitude": location.coordinate.longitude,
            "latitude": location.coordinate.latitude,
            "altitude": location.altitude,
            ])
        }
      }
    }
  }

  @objc(getAircraftHomeLocation:reject:)
  func getAircraftHomeLocation(resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    let key = DJIFlightControllerKey(param: DJIFlightControllerParamHomeLocation)!
    self.getKeyValue(key) { (value: DJIKeyedValue?, error: Error?) in
      if (error != nil) {
        self.sendReject(reject, "getAircraftHomeLocation Error", error! as NSError)
      } else {
        if let homeLocation = value?.value as? CLLocationCoordinate2D {
          resolve([
            "longitude": homeLocation.longitude,
            "latitude": homeLocation.latitude,
            ])
        }
      }
    }
  }

  @objc(getFlightLogPath:reject:)
  func getFlightLogPath(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
    resolve(DJISDKManager.getLogPath())
  }

  @objc(startFlightLogListener:reject:)
  func startFlightLogListener(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
    self.folderMonitor.startMonitoring(pathToMonitor: DJISDKManager.getLogPath()) { (newFileName: String) in
      EventSender.sendReactEvent(type: SdkEventName.DJIFlightLogEvent.rawValue, value: [
        "eventName": "create",
        "filePath": newFileName,
        ])
    }
    resolve(nil)
  }

  @objc(stopFlightLogListener:reject:)
  func stopFlightLogListener(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
    self.folderMonitor.stopMonitoring()
    resolve(nil)
  }

  @objc(setCollisionAvoidanceEnabled:resolve:reject:)
  func setCollisionAvoidanceEnabled(enabled: Bool, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    DJISDKManager.keyManager()?.setValue(enabled, for: DJIFlightControllerKey(param: DJIFlightAssistantParamCollisionAvoidanceEnabled)!, withCompletion: { (error: Error?) in
      if (error == nil) {
        reject("Could not change collision avoidance state", nil, error)
      } else {
        resolve(nil)
      }
    })
  }

  @objc(setVirtualStickAdvancedModeEnabled:resolve:reject:)
  func setVirtualStickAdvancedModeEnabled(enabled: Bool, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    DJISDKManager.keyManager()?.setValue(enabled, for: DJIFlightControllerKey(param: DJIFlightControllerParamVirtualStickAdvancedControlModeEnabled)!, withCompletion: { (error: Error?) in
      if (error == nil) {
        reject("Could not change virtual stick advanced mode state", nil, error)
      } else {
        resolve(nil)
      }
    })
  }

  @objc(setLandingProtectionEnabled:resolve:reject:)
  func setLandingProtectionEnabled(enabled: Bool, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    DJISDKManager.keyManager()?.setValue(enabled, for: DJIFlightControllerKey(param: DJIFlightAssistantParamLandingProtectionEnabled)!, withCompletion: { (error: Error?) in
      if (error == nil) {
        reject("Could not change landing protection state", nil, error)
      } else {
        resolve(nil)
      }
    })
  }

  @objc(setVisionAssistedPositioningEnabled:resolve:reject:)
  func setVisionAssistedPositioningEnabled(enabled: Bool, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    DJISDKManager.keyManager()?.setValue(enabled, for: DJIFlightControllerKey(param: DJIFlightControllerParamVisionAssistedPositioningEnabled)!, withCompletion: { (error: Error?) in
      if (error == nil) {
        reject("Could not change vision assisted positioning state", nil, error)
      } else {
        resolve(nil)
      }
    })
  }

  @objc(startNewMediaFileListener:reject:)
  func startNewMediaFileListener(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
    NotificationCenter.default.addObserver(self, selector: #selector(newMediaFileUpdate), name: CameraEvent.didGenerateNewMediaFile.notification, object: nil)
    resolve("startNewMediaFileListener")
  }

  @objc private func newMediaFileUpdate(payload: NSNotification) {
    let newMedia = payload.userInfo!["value"] as! DJIMediaFile
    EventSender.sendReactEvent(type: "CameraDidGenerateNewMediaFile", value: [
      "fileName": newMedia.fileName
      ])
  }

  @objc(stopNotificationCenterListener:resolve:reject:)
  func stopNotificationServiceListener(name: String, resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
    NotificationCenter.default.removeObserver(self, name: NSNotification.Name(name), object: nil)
    resolve("stopNotificationCenterListener")
  }

  @objc(stopEventListener:resolve:reject:)
  func stopEventListener(keyString: String, resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {

    guard let sdkEventName = SdkEventName.init(rawValue: keyString) else {
      reject("Invalid Key", nil, nil)
      return
    }

    if let validKeyInfo = implementedEvents[sdkEventName] {
      let keyType = validKeyInfo[1] as! DJIKey.Type
      let keyParam = validKeyInfo[0] as! String
      let key = keyType.init(param: keyParam)!

      DJISDKManager.keyManager()?.stopListening(on: key, ofListener: self)
      eventsBeingListenedTo.removeAll(where: { $0 == sdkEventName } )
      resolve(nil)

    } else {
      reject("Invalid Key", nil, nil)
      return
    }
  }

  private func startKeyListener(_ eventName: SdkEventName, updateBlock: @escaping DJIKeyedListenerUpdateBlock) {
    let validKeyInfo = implementedEvents[eventName]!
    let keyType = validKeyInfo[1] as! DJIKey.Type
    let keyParam = validKeyInfo[0] as! String
    let key = keyType.init(param: keyParam)!

    // Check if there is already an event listener sending events across the bridge
    if (eventsBeingListenedTo.contains(where: { $0 == eventName } ) == false) {
      DJISDKManager.keyManager()?.startListeningForChanges(on: key, withListener: self, andUpdate: updateBlock)
      eventsBeingListenedTo.append(eventName)
    } else {
      // If there is an existing listener, don't create a new one
    }
    // Get an initial value for the listener to send

    if (eventName == .ProductConnection) { // The product connection key cannot be used with getValueFor
      EventSender.sendReactEvent(type: eventName.rawValue, value: DJISDKManager.product() != nil ? "connected" : "disconnected")
    } else {
      DJISDKManager.keyManager()?.getValueFor(key, withCompletion: { (value: DJIKeyedValue?, error: Error?) in
        if (error != nil) {
          print(error)
          // Could not get initial value for some reason
        } else {
          updateBlock(nil, value)
        }
      })
    }
  }

  private func getKeyValue(_ key: DJIKey, updateBlock: @escaping DJIKeyedGetCompletionBlock) {
    DJISDKManager.keyManager()?.getValueFor(key, withCompletion: updateBlock)
  }

  func sendReject(_ reject: RCTPromiseRejectBlock,
                  _ code: String,
                  _ error: NSError?
    ) {
    if (error != nil) {
      reject(
        code,
        error!.localizedDescription,
        error!
      )
    } else {
      reject(
        code,
        code,
        nil
      )
    }
  }

  @objc static func requiresMainQueueSetup() -> Bool {
    return true
  }

  @objc func constantsToExport() -> [AnyHashable : Any]! {

    var constants: [String: String] = [:]

    for eventName in SdkEventName.allCases {
      let eventNameString = eventName.rawValue
      constants[eventNameString] = eventNameString
    }

    return constants
  }

}
