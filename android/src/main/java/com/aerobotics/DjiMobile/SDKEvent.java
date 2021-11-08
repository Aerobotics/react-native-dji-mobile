package com.aerobotics.DjiMobile;

import dji.keysdk.AirLinkKey;
import dji.keysdk.BatteryKey;
import dji.keysdk.CameraKey;
import dji.keysdk.FlightControllerKey;
import dji.keysdk.GimbalKey;
import dji.keysdk.ProductKey;

public enum SDKEvent {
  ProductConnection(ProductKey.create(ProductKey.CONNECTION), EventType.DJI_KEY_MANAGER_EVENT),
  BatteryChargeRemaining(BatteryKey.create(BatteryKey.CHARGE_REMAINING_IN_PERCENT), EventType.DJI_KEY_MANAGER_EVENT),
  AircraftCompassHeading(FlightControllerKey.create(FlightControllerKey.COMPASS_HEADING), EventType.DJI_KEY_MANAGER_EVENT),
  AircraftLocation(FlightControllerKey.create(FlightControllerKey.AIRCRAFT_LOCATION), EventType.DJI_KEY_MANAGER_EVENT),

  // This is not a real DJI Key event, it is used to start listening to the x, y, and z velocity events
  AircraftVelocity(null, EventType.DJI_KEY_MANAGER_EVENT),
  AircraftVelocityX(FlightControllerKey.create(FlightControllerKey.VELOCITY_X), EventType.DJI_KEY_MANAGER_EVENT),
  AircraftVelocityY(FlightControllerKey.create(FlightControllerKey.VELOCITY_Y), EventType.DJI_KEY_MANAGER_EVENT),
  AircraftVelocityZ(FlightControllerKey.create(FlightControllerKey.VELOCITY_Z), EventType.DJI_KEY_MANAGER_EVENT),

  // This is not a real DJI Key event, it is used to start listening to the yaw, pitch, and roll attitude events
  AircraftAttitude(null, EventType.DJI_KEY_MANAGER_EVENT),
  AircraftAttitudeYaw(FlightControllerKey.create(FlightControllerKey.ATTITUDE_YAW), EventType.DJI_KEY_MANAGER_EVENT),
  AircraftAttitudePitch(FlightControllerKey.create(FlightControllerKey.ATTITUDE_PITCH), EventType.DJI_KEY_MANAGER_EVENT),
  AircraftAttitudeRoll(FlightControllerKey.create(FlightControllerKey.ATTITUDE_ROLL), EventType.DJI_KEY_MANAGER_EVENT),

  AircraftGpsSignalLevel(FlightControllerKey.create(FlightControllerKey.GPS_SIGNAL_LEVEL), EventType.DJI_KEY_MANAGER_EVENT),
  AirLinkUplinkSignalQuality(null, EventType.DJI_KEY_MANAGER_EVENT),
  AirLinkLightbridgeUplinkSignalQuality(AirLinkKey.createLightbridgeLinkKey(AirLinkKey.UPLINK_SIGNAL_QUALITY), EventType.DJI_KEY_MANAGER_EVENT),
  AirLinkOcuSyncUplinkSignalQuality(AirLinkKey.createOcuSyncLinkKey(AirLinkKey.UPLINK_SIGNAL_QUALITY), EventType.DJI_KEY_MANAGER_EVENT),
  AirLinkDownlinkSignalQuality(null, EventType.DJI_KEY_MANAGER_EVENT),
  AirLinkLightbridgeDownlinkSignalQuality(AirLinkKey.createLightbridgeLinkKey(AirLinkKey.DOWNLINK_SIGNAL_QUALITY), EventType.DJI_KEY_MANAGER_EVENT),
  AirLinkOcuSyncDownlinkSignalQuality(AirLinkKey.createOcuSyncLinkKey(AirLinkKey.DOWNLINK_SIGNAL_QUALITY), EventType.DJI_KEY_MANAGER_EVENT),
  AircraftHomeLocation(FlightControllerKey.create(FlightControllerKey.HOME_LOCATION), EventType.DJI_KEY_MANAGER_EVENT),
  TakeoffLocationAltitude(FlightControllerKey.create(FlightControllerKey.TAKEOFF_LOCATION_ALTITUDE), EventType.DJI_KEY_MANAGER_EVENT),
  AircraftUltrasonicHeight(FlightControllerKey.create(FlightControllerKey.ULTRASONIC_HEIGHT_IN_METERS), EventType.DJI_KEY_MANAGER_EVENT),
  CompassHasError(FlightControllerKey.create(FlightControllerKey.COMPASS_HAS_ERROR), EventType.DJI_KEY_MANAGER_EVENT),

  LandingProtectionEnabled(FlightControllerKey.create(FlightControllerKey.LANDING_PROTECTION_ENABLED), EventType.DJI_KEY_MANAGER_EVENT),
  VisionAssistedPositioningEnabled(FlightControllerKey.create(FlightControllerKey.VISION_ASSISTED_POSITIONING_ENABLED), EventType.DJI_KEY_MANAGER_EVENT),

  CameraIsRecording(CameraKey.create(CameraKey.IS_RECORDING), EventType.DJI_KEY_MANAGER_EVENT),
  SDCardIsInserted(CameraKey.create(CameraKey.SDCARD_IS_INSERTED), EventType.DJI_KEY_MANAGER_EVENT),
  SDCardIsReadOnly(CameraKey.create(CameraKey.SDCARD_IS_READ_ONLY), EventType.DJI_KEY_MANAGER_EVENT),

  GimbalIsAtYawStop(GimbalKey.create(GimbalKey.IS_YAW_AT_STOP), EventType.DJI_KEY_MANAGER_EVENT),

  WaypointMissionFinished(null, EventType.DJI_KEY_MANAGER_EVENT),
  WaypointMissionStarted(null, EventType.DJI_KEY_MANAGER_EVENT),
  WaypointMissionExecutionProgress(null, EventType.DJI_KEY_MANAGER_EVENT),

  VirtualStickTimelineElementEvent(null, EventType.DJI_KEY_MANAGER_EVENT),

  AircraftVirtualStickEnabled(FlightControllerKey.create(FlightControllerKey.VIRTUAL_STICK_CONTROL_MODE_ENABLED), EventType.DJI_KEY_MANAGER_EVENT),

  OnboardSDKDeviceData(null, EventType.DJI_ONBOARD_SDK_DEVICE_DATA_EVENT),

  CameraDidUpdateSystemState(null, EventType.DJI_CAMERA_DELEGATE_EVENT),
  CameraDidGenerateNewMediaFile(null, EventType.DJI_CAMERA_DELEGATE_EVENT),

  VisionDetectionState(FlightControllerKey.createFlightAssistantKey(FlightControllerKey.VISION_DETECTION_STATE), EventType.DJI_KEY_MANAGER_EVENT),
  VisionControlState(null, EventType.DJI_CALLBACK_EVENT),

  DJIDiagnostics(null, EventType.DJI_CALLBACK_EVENT);

  private Object key;
  private EventType eventType;

  SDKEvent(Object key, EventType eventType) {
    this.key = key;
    this.eventType = eventType;
  }

  public Object getKey() {
    return key;
  }

  public EventType getEventType() {
    return eventType;
  }

}
