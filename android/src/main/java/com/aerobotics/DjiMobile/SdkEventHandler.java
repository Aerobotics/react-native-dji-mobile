package com.aerobotics.DjiMobile;


import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import dji.keysdk.BatteryKey;
import dji.keysdk.DJIKey;
import dji.keysdk.FlightControllerKey;
import dji.keysdk.ProductKey;
import dji.keysdk.callback.KeyListener;
import dji.sdk.sdkmanager.DJISDKManager;

enum EventType {
  DJI_KEY_MANAGER_EVENT,
  DJI_CAMERA_DELEGATE_EVENT,
}

enum SDKEvent {
  ProductConnection(ProductKey.create(ProductKey.CONNECTION), EventType.DJI_KEY_MANAGER_EVENT),
  BatteryChargeRemaining(BatteryKey.create(BatteryKey.CHARGE_REMAINING_IN_PERCENT), EventType.DJI_KEY_MANAGER_EVENT),
  AircraftCompassHeading(FlightControllerKey.create(FlightControllerKey.COMPASS_HEADING), EventType.DJI_KEY_MANAGER_EVENT),
  AircraftLocation(FlightControllerKey.create(FlightControllerKey.AIRCRAFT_LOCATION), EventType.DJI_KEY_MANAGER_EVENT),
  AircraftVelocityX(FlightControllerKey.create(FlightControllerKey.VELOCITY_X), EventType.DJI_KEY_MANAGER_EVENT),
  AircraftVelocityY(FlightControllerKey.create(FlightControllerKey.VELOCITY_Y), EventType.DJI_KEY_MANAGER_EVENT),
  AircraftVelocityZ(FlightControllerKey.create(FlightControllerKey.VELOCITY_Z), EventType.DJI_KEY_MANAGER_EVENT),
  AircraftAttitudeYaw(FlightControllerKey.create(FlightControllerKey.ATTITUDE_YAW), EventType.DJI_KEY_MANAGER_EVENT),
  AircraftAttitudePitch(FlightControllerKey.create(FlightControllerKey.ATTITUDE_PITCH), EventType.DJI_KEY_MANAGER_EVENT),
  AircraftAttitudeRoll(FlightControllerKey.create(FlightControllerKey.ATTITUDE_ROLL), EventType.DJI_KEY_MANAGER_EVENT),
  GPSSignalLevel(FlightControllerKey.create(FlightControllerKey.GPS_SIGNAL_LEVEL), EventType.DJI_KEY_MANAGER_EVENT),
  IsHomeLocationSet(FlightControllerKey.create(FlightControllerKey.IS_HOME_LOCATION_SET), EventType.DJI_KEY_MANAGER_EVENT),
  HomeLocation(FlightControllerKey.create(FlightControllerKey.HOME_LOCATION), EventType.DJI_KEY_MANAGER_EVENT),
  UltrasonicHeight(FlightControllerKey.create(FlightControllerKey.ULTRASONIC_HEIGHT_IN_METERS), EventType.DJI_KEY_MANAGER_EVENT),

  CameraDidUpdateSystemState(null, EventType.DJI_CAMERA_DELEGATE_EVENT),
  CameraDidGenerateNewMediaFile(null, EventType.DJI_CAMERA_DELEGATE_EVENT);

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

interface EventListener {
  void onValueChange(@Nullable Object oldValue, @Nullable Object newValue);
}

public class SdkEventHandler {

  private CameraEventDelegate cameraEventDelegate = new CameraEventDelegate();

  private class EventInfo {
    String key;
    EventType eventType;
  }

  public Object startEventListener(final SDKEvent sdkEvent, final EventListener eventListener) {
    EventType eventType = sdkEvent.getEventType();
    Object key = sdkEvent.getKey();

    if (eventType == EventType.DJI_KEY_MANAGER_EVENT) {
      KeyListener keyListener = new KeyListener() {
        @Override
        public void onValueChange(@Nullable Object oldValue, @Nullable Object newValue) {
          eventListener.onValueChange(oldValue, newValue);
        }
      };
      DJISDKManager.getInstance().getKeyManager().addListener((DJIKey)key, keyListener);
      return keyListener;

    } else if (eventType == EventType.DJI_CAMERA_DELEGATE_EVENT) {
      Observer observer = new Observer() {
        @Override
        public void update(Observable observable, Object newValue) {
          if (newValue instanceof HashMap) {
            HashMap payload = (HashMap) newValue;
            // CameraEventDelegate sends an update for all camera events, so only send update if eventType matches sdkEvent we want
            if (payload.get("eventType") == sdkEvent) {
              eventListener.onValueChange(null, payload);
            }
          }

        }
      };
      cameraEventDelegate.addObserver(observer);
      return observer;
    }

    return null;
  }

  public void stopEventListener(SDKEvent SDKEvent, @Nullable Object eventSubscriptionObject) {
    EventType eventType = SDKEvent.getEventType();
    Object key = SDKEvent.getKey();

    if (eventType == EventType.DJI_KEY_MANAGER_EVENT) {
      DJISDKManager.getInstance().getKeyManager().removeListener((KeyListener)eventSubscriptionObject);

    } else if (eventType == EventType.DJI_CAMERA_DELEGATE_EVENT) {
      cameraEventDelegate.removeObserver((Observer)eventSubscriptionObject);
    }
  }


}
