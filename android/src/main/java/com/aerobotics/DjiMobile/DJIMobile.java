
package com.aerobotics.DjiMobile;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;


import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.HashMap;

import dji.common.error.DJIError;
import dji.common.error.DJISDKError;
import dji.common.flightcontroller.GPSSignalLevel;
import dji.common.flightcontroller.LocationCoordinate3D;
import dji.keysdk.BatteryKey;
import dji.keysdk.DJIKey;
import dji.keysdk.FlightControllerKey;
import dji.keysdk.KeyManager;
import dji.keysdk.callback.GetCallback;
import dji.keysdk.callback.SetCallback;
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
  private BaseProduct product;

  private DJIRealTimeDataLogger djiRealTimeDataLogger;
  private Handler handler;

  public DJIMobile(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
    this.handler = new Handler(Looper.getMainLooper());
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
        product = null;
      }

      @Override
      public void onProductConnect(BaseProduct baseProduct) {
        product = baseProduct;
        Log.i("REACT", "connected");
      }

      @Override
      public void onComponentChange(BaseProduct.ComponentKey componentKey, BaseComponent baseComponent, BaseComponent baseComponent1) {
        // TODO
      }
    });
  }

  @ReactMethod
  public void getFileList(final Promise promise) {
    DJIMedia m = new DJIMedia(reactContext);
    if (product == null) {
        product = DJISDKManager.getInstance().getProduct();
        if (product == null) {
            promise.reject("No product connected");
        } else {
            m.getFileList(promise, product);
        }
    } else {
      m.getFileList(promise, product);
    }
  }

  @ReactMethod
  public void setCollisionAvoidanceEnabled(final Boolean enabled, final Promise promise) {
    DJIKey collisionAvoidanceKey = FlightControllerKey.createFlightAssistantKey(FlightControllerKey.COLLISION_AVOIDANCE_ENABLED);
    DJISDKManager.getInstance().getKeyManager().setValue(collisionAvoidanceKey, enabled, new SetCallback() {
      @Override
      public void onSuccess() {
        if (enabled) {
          promise.resolve("DJIMobile: Set collision avoidance enabled successfully");
        } else {
          promise.resolve("DJIMobile: Set collision avoidance disabled successfully");
        }
      }

      @Override
      public void onFailure(@NonNull DJIError djiError) {
        promise.reject(new Throwable(djiError.getDescription()));
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
  public void startBatteryPercentChargeRemainingListener(final Promise promise) {
      promise.resolve(null);
      startEventListener(SDKEvent.BatteryChargeRemaining, new EventListener() {
      @Override
      public void onValueChange(@Nullable Object oldValue, @Nullable Object newValue) {
        if (newValue != null && newValue instanceof Integer) {
          sendEvent(SDKEvent.BatteryChargeRemaining, newValue);
        }
      }
    });
    BatteryKey batteryKey = BatteryKey.create(BatteryKey.CHARGE_REMAINING_IN_PERCENT);
      // Send initial value
      KeyManager.getInstance().getValue(batteryKey, new GetCallback() {
        @Override
        public void onSuccess(@NonNull final Object newValue) {
            if (newValue != null && newValue instanceof Integer) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        sendEvent(SDKEvent.BatteryChargeRemaining, newValue);
                    }
                }, 300);
            }
        }

        @Override
        public void onFailure(@NonNull DJIError djiError) {
        }
    });
  }

  @ReactMethod
  public void startGPSSignalLevelListener(Promise promise) {
      promise.resolve(null);
      startEventListener(SDKEvent.GPSSignalLevel, new EventListener() {
          @Override
          public void onValueChange(@Nullable Object oldValue, @Nullable Object newValue) {
              if (newValue != null && newValue instanceof GPSSignalLevel) {
                  GPSSignalLevel gpsSignalLevel = (GPSSignalLevel) newValue;
                WritableMap params = Arguments.createMap();
                switch (gpsSignalLevel) {
                      case LEVEL_0:
                        params.putInt("gpsSignalLevel", 0);
                          break;
                      case LEVEL_1:
                        params.putInt("gpsSignalLevel", 1);
                          break;
                      case LEVEL_2:
                        params.putInt("gpsSignalLevel", 2);
                        break;
                      case LEVEL_3:
                        params.putInt("gpsSignalLevel", 3);
                        break;
                      case LEVEL_4:
                        params.putInt("gpsSignalLevel", 4);
                        break;
                      case LEVEL_5:
                        params.putInt("gpsSignalLevel", 5);

                        break;
                      case NONE:
                        params.putNull("gpsSignalLevel");
                        break;
                      default:
                          break;
                  }
                sendEvent(SDKEvent.GPSSignalLevel, params);
              }
          }
      });
      // Send initial value
      KeyManager.getInstance().getValue((DJIKey) SDKEvent.GPSSignalLevel.getKey(), new GetCallback() {
          @Override
          public void onSuccess(@NonNull final Object newValue) {
              if (newValue instanceof GPSSignalLevel) {
                  handler.postDelayed(new Runnable() {
                      @Override
                      public void run() {
                          WritableMap params = Arguments.createMap();
                          params.putInt("gpsSignalLevel", ((GPSSignalLevel) newValue).value());
                          sendEvent(SDKEvent.GPSSignalLevel, params);
                      }
                  }, 300);
              }
          }

          @Override
          public void onFailure(@NonNull DJIError djiError) {

          }
      });
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
  public void startAircraftAttitudeListener(Promise promise) {
    SDKEvent[] attitudeEvents = {
      SDKEvent.AircraftAttitudeYaw,
      SDKEvent.AircraftAttitudePitch,
      SDKEvent.AircraftAttitudeRoll,
    };
    final double[] attitudeVector = {0.0, 0.0, 0.0};

    for (int i = 0; i < 3; i++) {
      final int finalI = i;
      startEventListener(attitudeEvents[i], new EventListener() {
        @Override
        public void onValueChange(@Nullable Object oldValue, @Nullable Object newValue) {
          if (newValue != null && newValue instanceof Double) {
            attitudeVector[finalI] = (double)newValue;
            WritableMap params = Arguments.createMap();
            params.putDouble("yaw", attitudeVector[0]);
            params.putDouble("pitch", attitudeVector[1]);
            params.putDouble("roll", attitudeVector[2]);
            sendEvent("AircraftAttitude", params);
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
    promise.resolve(null);
  }

  @ReactMethod
  public void startUltrasonicHeightListener(Promise promise) {
    startEventListener(SDKEvent.UltrasonicHeight, new EventListener() {
      @Override
      public void onValueChange(@Nullable Object oldValue, @Nullable Object newValue) {
        if (newValue != null && newValue instanceof Float) {
          Float height = (Float) newValue;
          WritableMap params = Arguments.createMap();
          params.putDouble("height", height);
          sendEvent(SDKEvent.UltrasonicHeight, params);
        }
      }
    });
    promise.resolve(null);
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
  public void stopEventListener(String eventName, Promise promise) {
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
    promise.resolve(null);
  }

  private void startEventListener(SDKEvent SDKEvent, EventListener eventListener) {

    Object existingEventListener = eventListeners.get(SDKEvent);

    if (sdkEventHandler == null) {
        sdkEventHandler = new SdkEventHandler();
    }
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
      eventListeners.remove(SDKEvent);
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

    @ReactMethod
    public void startRecordRealTimeData(String fileName){
        if (djiRealTimeDataLogger == null) {
            djiRealTimeDataLogger = new DJIRealTimeDataLogger(reactContext);
        }
        djiRealTimeDataLogger.startLogging(fileName);
    }

    @ReactMethod
    public void stopRecordRealTimeData() {
        if (djiRealTimeDataLogger != null) {
            djiRealTimeDataLogger.stopLogging();
        }
    }

  @Override
  public String getName() {
    return "DJIMobile";
  }
}
