
package com.aerobotics.DjiMobile;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;
import java.util.SplittableRandom;

import dji.common.error.DJIError;
import dji.common.error.DJISDKError;
import dji.common.flightcontroller.LocationCoordinate3D;
import dji.keysdk.DJIKey;
import dji.keysdk.FlightControllerKey;
import dji.keysdk.KeyManager;
import dji.keysdk.ProductKey;
import dji.keysdk.BatteryKey;
import dji.keysdk.callback.GetCallback;
import dji.keysdk.callback.KeyListener;
import dji.sdk.base.BaseComponent;
import dji.sdk.base.BaseProduct;
import dji.sdk.media.MediaFile;
import dji.sdk.sdkmanager.DJISDKManager;

class ValidKeyInfo {
  String keyParam;
  Class keyClass;
  Method createMethod;

  public ValidKeyInfo(String keyParam, Class keyClass) {
    this.keyParam = keyParam;
    this.keyClass = keyClass;
    try {
      this.createMethod = keyClass.getMethod("create", String.class);
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    }
  }

  public DJIKey createDJIKey() {
    try {
//      Object KeyClass = this.keyClass.newInstance();
//      String args[] = {this.keyParam};
      // As the .create() method is a static method, no object instance needs to be passed to .invoke(), hence the null value
      DJIKey createdKey = (DJIKey)this.createMethod.invoke(null, this.keyParam);
      return createdKey;
    } catch (Exception e) {
      Log.i("EXCEPTION", e.getLocalizedMessage());
      return null;
    }
  }
}

public class DJIMobile extends ReactContextBaseJavaModule {

  private final ReactApplicationContext reactContext;

  private HashMap eventListeners = new HashMap();

  ; // This must only be initialized once the SDK has registered, as it uses the SDK
  private SdkEventHandler sdkEventHandler;

//  private Observer newMediaFileObserver = new Observer() {
//    @Override
//    public void update(Observable observable, Object arg) {
//      if (arg instanceof HashMap) {
//        HashMap payload = (HashMap) arg;
//        if (payload.get("name") == CameraEvent.DID_GENERATE_NEW_MEDIA_FILE.getEventName()) {
//          MediaFile mediaFile = (MediaFile) payload.get("value");
//          WritableMap params = Arguments.createMap();
//          params.putString("fileName", mediaFile.getFileName());
//          sendEvent("newMediaFile", params);
//        }
//      }
//    }
//  };

  public DJIMobile(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
  }

  @ReactMethod
  public void registerApp(final Promise promise) {
    registerAppInternal(null, promise);
  }

  @ReactMethod
  public void registerAppAndUseBridge(final String bridgeIp, final Promise promise) {
    registerAppInternal(bridgeIp, promise);
  }

  public void registerAppInternal(final String bridgeIp, final Promise promise) {
    final DJISDKManager djisdkManager = DJISDKManager.getInstance();
    djisdkManager.registerApp(reactContext, new DJISDKManager.SDKManagerCallback() {
      @Override
      public void onRegister(DJIError djiError) {
        if (djiError == DJISDKError.REGISTRATION_SUCCESS) {
          // Only create a single instance of the sdkEventHandler, as multiple instances cause conflicts (duplicate camera callbacks, etc.)
          if (sdkEventHandler == null) {
            sdkEventHandler = new SdkEventHandler();
          }
          promise.resolve("DJI SDK: Registration Successful");
          if (bridgeIp != null) {
            djisdkManager.enableBridgeModeWithBridgeAppIP(bridgeIp);
          } else {
            djisdkManager.startConnectionToProduct();
          }
        } else {
          promise.reject(djiError.toString(), djiError.getDescription());
        }
      }

      @Override
      public void onProductDisconnect() {
        // TODO
      }

      @Override
      public void onProductConnect(BaseProduct baseProduct) {
        // TODO
      }

      @Override
      public void onComponentChange(BaseProduct.ComponentKey componentKey, BaseComponent baseComponent, BaseComponent baseComponent1) {
        // TODO
      }
    });
  }

  @ReactMethod
  public void startProductConnectionListener(Promise promise) {
    startEventListener(SDKEvent.ProductConnection, new EventListener() {
      @Override
      public void onValueChange(@Nullable Object oldValue, @Nullable Object newValue) {
        if (newValue != null && newValue instanceof Boolean) {
          sendEvent(SDKEvent.ProductConnection, (boolean) newValue ? "connected" : "disconnected");
        }
      }
    });
    promise.resolve(null);
  }

  @ReactMethod
  public void startBatteryPercentChargeRemainingListener(Promise promise) {
    startEventListener(SDKEvent.BatteryChargeRemaining, new EventListener() {
      @Override
      public void onValueChange(@Nullable Object oldValue, @Nullable Object newValue) {
        if (newValue != null && newValue instanceof Integer) {
          sendEvent(SDKEvent.BatteryChargeRemaining, newValue);
        }
      }
    });
    promise.resolve(null);
  }

  @ReactMethod
  public void startAircraftLocationListener(Promise promise) {
    startEventListener(SDKEvent.AircraftLocation, new EventListener() {
      @Override
      public void onValueChange(@Nullable Object oldValue, @Nullable Object newValue) {
        if (newValue != null && newValue instanceof LocationCoordinate3D) {
          LocationCoordinate3D location = (LocationCoordinate3D) newValue;
          double longitude = location.getLongitude();
          double latitude = location.getLatitude();
          double altitude = location.getAltitude();
          if (!Double.isNaN(longitude) && !Double.isNaN(latitude)) {
            WritableMap params = Arguments.createMap();
            params.putDouble("longitude", longitude);
            params.putDouble("latitude", latitude);
            params.putDouble("altitude", altitude);
            sendEvent(SDKEvent.AircraftLocation, params);
          }
        }
      }
    });
    promise.resolve(null);
  }

  // TODO: (Adam) Update to new method!
  @ReactMethod
  public void getAircraftLocation(final Promise promise) {
    DJIKey key = FlightControllerKey.create(FlightControllerKey.AIRCRAFT_LOCATION);
    DJISDKManager.getInstance().getKeyManager().getValue(key, new GetCallback() {
      @Override
      public void onSuccess(@NonNull Object value) {
        LocationCoordinate3D location = (LocationCoordinate3D) value;
        double longitude = location.getLongitude();
        double latitude = location.getLatitude();
        double altitude = location.getAltitude();
        if (!Double.isNaN(longitude) && !Double.isNaN(latitude)) {
          WritableMap params = Arguments.createMap();
          params.putDouble("longitude", longitude);
          params.putDouble("latitude", latitude);
          params.putDouble("altitude", altitude);
          promise.resolve(params);
        }
      }

      @Override
      public void onFailure(@NonNull DJIError djiError) {
        promise.reject("getAircraftLocation Error", djiError.getDescription());
      }
    });
  }

  @ReactMethod
  public void startAircraftVelocityListener(Promise promise) {
    SDKEvent[] velocityEvents = {
      SDKEvent.AircraftVelocityX,
      SDKEvent.AircraftVelocityY,
      SDKEvent.AircraftVelocityZ,
    };
    final double[] velocityVector = {0.0, 0.0, 0.0};

    for (int i = 0; i < 3; i++) {
      final int finalI = i;
      startEventListener(velocityEvents[i], new EventListener() {
        @Override
        public void onValueChange(@Nullable Object oldValue, @Nullable Object newValue) {
          if (newValue != null && newValue instanceof Float) {
            velocityVector[finalI] = (float)newValue;
            WritableMap params = Arguments.createMap();
            params.putDouble("x", velocityVector[0]);
            params.putDouble("y", velocityVector[1]);
            params.putDouble("z", velocityVector[2]);
            sendEvent("AircraftVelocity", params);
          }
        }
      });
    }

    promise.resolve(null);
  }

  @ReactMethod
  public void startAircraftCompassHeadingListener(Promise promise) {
    startEventListener(SDKEvent.AircraftCompassHeading, new EventListener() {
      @Override
      public void onValueChange(@Nullable Object oldValue, @Nullable Object newValue) {
        if (newValue != null && newValue instanceof Float) {
          WritableMap params = Arguments.createMap();
          params.putDouble("heading", (float)newValue);
          sendEvent(SDKEvent.AircraftCompassHeading, params);
        }
      }
    });
    promise.resolve(null);
  }


//  @ReactMethod
//  public void startNewMediaFileListener(Promise promise) {
//    cameraDelegateSender.addObserver(newMediaFileObserver);
//    promise.resolve("startNewMediaFileListener");
//  }

  @ReactMethod
  public void startNewMediaFileListener(Promise promise) {
    startEventListener(SDKEvent.CameraDidGenerateNewMediaFile, new EventListener() {
      @Override
      public void onValueChange(@Nullable Object oldValue, @Nullable Object newValue) {
        if (newValue != null && newValue instanceof HashMap) {
          HashMap payload = (HashMap) newValue;
          MediaFile mediaFile = (MediaFile) payload.get("value");
          WritableMap params = Arguments.createMap();
          params.putString("fileName", mediaFile.getFileName());
          sendEvent(SDKEvent.CameraDidGenerateNewMediaFile, params);
        }
      }
    });
  }

//  @ReactMethod
//  public void stopCameraDelegateListener(String eventName, Promise promise) {
//    switch (eventName) {
//      case "DJICameraEvent.didGenerateNewMediaFile":
//        cameraDelegateSender.removeObserver(newMediaFileObserver);
//        break;
//
//      default:
//        break;
//    }
//    promise.resolve("stopCameraDelegateListener");
//  }

  @ReactMethod
  public void stopEventListener(String eventName) {
    if (eventName.equals("AircraftVelocity")) {
      SDKEvent[] velocityEvents = {
        SDKEvent.AircraftVelocityX,
        SDKEvent.AircraftVelocityY,
        SDKEvent.AircraftVelocityZ,
      };
      for (int i = 0; i < 3; i++) {
        final int finalI = i;
        stopEventListenerInternal(velocityEvents[i]);
      }

    } else {
      SDKEvent sdkEvent = SDKEvent.valueOf(eventName);
      if (sdkEvent != null) {
        stopEventListenerInternal(sdkEvent);
      }
    }
  }

  private void startEventListener(SDKEvent SDKEvent, EventListener eventListener) {

    Object existingEventListener = eventListeners.get(SDKEvent);

    if (existingEventListener == null) {
      Object eventSubscriptionObject = sdkEventHandler.startEventListener(SDKEvent, eventListener);
      eventListeners.put(SDKEvent, eventSubscriptionObject);
    } else {
      // If there is an existing listener, don't create a new one (the existing one will be sending events across the bridge already)
      return;
    }
  }

  private void stopEventListenerInternal(SDKEvent SDKEvent) {
    Object eventSubscriptionObject = eventListeners.get(SDKEvent);
    if (eventSubscriptionObject != null) {
      sdkEventHandler.stopEventListener(SDKEvent, eventSubscriptionObject);
    }
  }

  private void sendEvent(SDKEvent SDKEvent, Object value) {
    WritableMap params = buildEventParams(value);
    params.putString("type", SDKEvent.toString());
    reactContext
      .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
      .emit("DJIEvent", params);
  }

  private void sendEvent(String eventName, Object value) {
    WritableMap params = buildEventParams(value);
    params.putString("type", eventName);
    reactContext
      .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
      .emit("DJIEvent", params);
  }

  private WritableMap buildEventParams(Object value) {
    WritableMap params = Arguments.createMap();
    if (value instanceof Integer) {
      params.putInt("value", (Integer)value);
    } else if (value instanceof Double) {
      params.putDouble("value", (Double)value);
    } else if (value instanceof String) {
      params.putString("value", (String)value);
    } else if (value instanceof Boolean) {
      params.putBoolean("value", (Boolean)value);
    } else if (value instanceof WritableMap) {
      params.putMap("value", (WritableMap)value);
    } else if (value instanceof WritableArray) {
      params.putArray("value", (WritableArray) value);
    }
    return params;
  }

  @Override
  public String getName() {
    return "DJIMobile";
  }
}