package com.aerobotics.DjiMobile;


import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import dji.common.error.DJIError;
import dji.keysdk.AirLinkKey;
import dji.keysdk.BatteryKey;
import dji.keysdk.CameraKey;
import dji.keysdk.DJIKey;
import dji.keysdk.FlightControllerKey;
import dji.keysdk.KeyManager;
import dji.keysdk.ProductKey;
import dji.keysdk.callback.GetCallback;
import dji.keysdk.callback.KeyListener;
import dji.sdk.sdkmanager.DJISDKManager;

enum EventType {
  DJI_KEY_MANAGER_EVENT,
  DJI_CAMERA_DELEGATE_EVENT,
}

interface EventListener {
  void onValueChange(@Nullable Object oldValue, @Nullable Object newValue);
}

public class SdkEventHandler {

  private CameraEventDelegate cameraEventDelegate = new CameraEventDelegate();
  private Handler handler;

  private class EventInfo {
    String key;
    EventType eventType;
  }

  public SdkEventHandler() {
    handler = new Handler(Looper.getMainLooper());
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
      getInitialDJIKeyValue((DJIKey)key, eventListener);
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

  public void stopEventListener(SDKEvent sdkEvent, @Nullable Object eventSubscriptionObject) {
    EventType eventType = sdkEvent.getEventType();
    Object key = sdkEvent.getKey();

    if (eventType == EventType.DJI_KEY_MANAGER_EVENT) {
      DJISDKManager.getInstance().getKeyManager().removeListener((KeyListener)eventSubscriptionObject);

    } else if (eventType == EventType.DJI_CAMERA_DELEGATE_EVENT) {
      cameraEventDelegate.removeObserver((Observer)eventSubscriptionObject);
    }
  }

  /**
   * This allows us to get the initial value for a DJI key when starting a listener, as sometimes the initial value is only sent through
   * when a new event occurs and calls the listener.
   */
  private void getInitialDJIKeyValue(final DJIKey key, final EventListener eventListener) {
    KeyManager.getInstance().getValue((DJIKey)key, new GetCallback() {
      @Override
      public void onSuccess(@NonNull final Object value) {
        handler.postDelayed(new Runnable() {
          @Override
          public void run() {
            eventListener.onValueChange(null, value);
          }
        }, 500);
      }

      @Override
      public void onFailure(@NonNull DJIError djiError) {
          // An initial value couldn't be gotten
      }
    });
  }


}
