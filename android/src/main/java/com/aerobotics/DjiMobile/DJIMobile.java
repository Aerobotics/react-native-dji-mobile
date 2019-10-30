
package com.aerobotics.DjiMobile;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;


import android.os.FileObserver;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import dji.common.error.DJIError;
import dji.common.error.DJISDKError;
import dji.common.flightcontroller.GPSSignalLevel;
import dji.common.flightcontroller.LocationCoordinate3D;
import dji.common.model.LocationCoordinate2D;
import dji.keysdk.AirLinkKey;
import dji.keysdk.DJIKey;
import dji.keysdk.FlightControllerKey;
import dji.keysdk.KeyManager;
import dji.keysdk.callback.GetCallback;
import dji.keysdk.callback.SetCallback;
import dji.sdk.base.BaseComponent;
import dji.sdk.base.BaseProduct;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.media.MediaFile;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKInitEvent;
import dji.sdk.sdkmanager.DJISDKManager;


public class DJIMobile extends ReactContextBaseJavaModule {

  private final ReactApplicationContext reactContext;

  private HashMap eventListeners = new HashMap();

  // This must only be initialized once the SDK has registered, as it uses the SDK
  private SdkEventHandler sdkEventHandler;
  private BaseProduct product;

  private DJIRealTimeDataLogger djiRealTimeDataLogger;
  private Handler handler;
  private FileObserver flightLogObserver;
  private EventSender eventSender;

  public DJIMobile(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
    this.handler = new Handler(Looper.getMainLooper());
    this.eventSender = new EventSender(reactContext);
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

      @Override
      public void onInitProcess(DJISDKInitEvent djisdkInitEvent, int i) {

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
  public void setVirtualStickAdvancedModeEnabled(Boolean enabled, Promise promise) {
    BaseProduct baseProduct = DJISDKManager.getInstance().getProduct();
    if (baseProduct instanceof Aircraft) {
      Aircraft aircraft = (Aircraft) baseProduct;
      FlightController flightController = aircraft.getFlightController();
      flightController.setVirtualStickAdvancedModeEnabled(enabled);
      promise.resolve(null);
    } else {
      promise.reject("Error: Failed to get flight controller instance");
    }
  }

  @ReactMethod
  public void setLandingProtectionEnabled(final Boolean enabled, final Promise promise) {
    DJIKey setLandingProtectionEnabledKey = (DJIKey) SDKEvent.LandingProtectionEnabled.getKey();
    DJISDKManager.getInstance().getKeyManager().setValue(setLandingProtectionEnabledKey, enabled, new SetCallback() {
      @Override
      public void onSuccess() {
        if (enabled) {
          promise.resolve("DJIMobile: Set landing protection enabled successfully");
        } else {
          promise.resolve("DJIMobile: Set landing protection disabled successfully");
        }
      }

      @Override
      public void onFailure(@NonNull DJIError djiError) {
        promise.reject(new Throwable(djiError.getDescription()));
      }
    });
  }

  @ReactMethod
  public void setVisionAssistedPositioningEnabled(final Boolean enabled, final Promise promise) {
    DJISDKManager.getInstance().getKeyManager().setValue((DJIKey) SDKEvent.VisionAssistedPositioningEnabled.getKey(), enabled, new SetCallback() {
      @Override
      public void onSuccess() {
        if (enabled) {
          promise.resolve("DJIMobile: Set vision assisted positioning enabled successfully");
        } else {
          promise.resolve("DJIMobile: Set vision assisted positioning disabled successfully");
        }
      }

      @Override
      public void onFailure(@NonNull DJIError djiError) {
        promise.reject(new Throwable(djiError.getDescription()));
      }
    });
  }


  @ReactMethod
  public void startEventListener(String eventName, Promise promise) {
    try {
      SDKEvent validEvent = SDKEvent.valueOf(eventName);

      switch (validEvent) {
        case ProductConnection:
          startProductConnectionListener();
          break;

        case BatteryChargeRemaining:
          startBatteryPercentChargeRemainingListener();
          break;

        case AircraftCompassHeading:
          startAircraftCompassHeadingListener();
          break;

        case AircraftLocation:
          startAircraftLocationListener();
          break;

        case AircraftVelocity:
          startAircraftVelocityListener();
          break;

        case AircraftAttitude:
          startAircraftAttitudeListener();
          break;

        case AircraftGpsSignalLevel:
          startGPSSignalLevelListener();
          break;

        case AirLinkUplinkSignalQuality:
          startAirlinkUplinkSignalQualityListener();
          break;

        case AircraftHomeLocation:
          startAircraftHomeLocationListener();
          break;

        case AircraftUltrasonicHeight:
          startUltrasonicHeightListener();
          break;

        case CompassHasError:
          startCompassHasErrorListener();
          break;

        case CameraIsRecording:
          startIsRecordingListener();
          break;

        default:
          promise.reject("Invalid Key", "Invalid Key");
          break;
      }

      promise.resolve(null);

    } catch (IllegalArgumentException exc) {
      promise.reject("Invalid Key", "Invalid Key");
    }

  }

  private void startProductConnectionListener() {
    startEventListener(SDKEvent.ProductConnection, new EventListener() {
      @Override
      public void onValueChange(@Nullable Object oldValue, @Nullable Object newValue) {
        if (newValue != null && newValue instanceof Boolean) {
          sendEvent(SDKEvent.ProductConnection, (boolean) newValue ? "connected" : "disconnected");
        }
      }
    });
  }

  private void startBatteryPercentChargeRemainingListener() {
    startEventListener(SDKEvent.BatteryChargeRemaining, new EventListener() {
      @Override
      public void onValueChange(@Nullable Object oldValue, @Nullable Object newValue) {
      if (newValue != null && newValue instanceof Integer) {
        sendEvent(SDKEvent.BatteryChargeRemaining, newValue);
      }
      }
  });
  }

  private void startGPSSignalLevelListener() {
    startEventListener(SDKEvent.AircraftGpsSignalLevel, new EventListener() {
      @Override
      public void onValueChange(@Nullable Object oldValue, @Nullable Object newValue) {
        if (newValue != null && newValue instanceof GPSSignalLevel) {
          GPSSignalLevel gpsSignalLevel = (GPSSignalLevel) newValue;
          Integer signalValue = gpsSignalLevel.value();
          if (gpsSignalLevel == GPSSignalLevel.NONE) {
            signalValue = null;
          }
          sendEvent(SDKEvent.AircraftGpsSignalLevel, signalValue);
        }
      }
    });
  }

  private void startAircraftLocationListener() {
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
  public void getAircraftIsFlying(final Promise promise) {
    DJIKey isFlyingKey = FlightControllerKey.create(FlightControllerKey.IS_FLYING);
    DJISDKManager.getInstance().getKeyManager().getValue(isFlyingKey, new GetCallback() {
      @Override
      public void onSuccess(@NonNull Object value) {
        if (value instanceof Boolean) {
          promise.resolve(value);
        }
      }

      @Override
      public void onFailure(@NonNull DJIError djiError) {
        promise.reject("getAircraftIsFlying Error", djiError.getDescription());
      }
    });
  }

  private void startAircraftVelocityListener() {
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
          sendEvent(SDKEvent.AircraftVelocity, params);
        }
        }
      });
    }
  }

  private void startAircraftAttitudeListener() {
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
          sendEvent(SDKEvent.AircraftAttitude, params);
        }
        }
      });
    }
  }

  private void startAircraftCompassHeadingListener() {
    startEventListener(SDKEvent.AircraftCompassHeading, new EventListener() {
      @Override
      public void onValueChange(@Nullable Object oldValue, @Nullable Object newValue) {
      if (newValue != null && newValue instanceof Float) {
        sendEvent(SDKEvent.AircraftCompassHeading, newValue);
      }
      }
    });
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

  private void startAircraftHomeLocationListener() {
    startEventListener(SDKEvent.AircraftHomeLocation, new EventListener() {
      @Override
      public void onValueChange(@Nullable Object oldValue, @Nullable Object newValue) {
        if (newValue != null && newValue instanceof LocationCoordinate2D) {
          LocationCoordinate2D location = (LocationCoordinate2D) newValue;
          Double longitude = location.getLongitude();
          Double latitude = location.getLatitude();
          if (!latitude.isNaN() && !latitude.isInfinite() && !longitude.isNaN() && !longitude.isInfinite()) {
              WritableMap params = Arguments.createMap();
              params.putDouble("longitude", longitude);
              params.putDouble("latitude", latitude);
              sendEvent(SDKEvent.AircraftHomeLocation, params);
          }
        }
      }
    });
  }

  private void startUltrasonicHeightListener() {
    startEventListener(SDKEvent.AircraftUltrasonicHeight, new EventListener() {
          @Override
          public void onValueChange(@Nullable Object oldValue, @Nullable Object newValue) {
      if (newValue != null && newValue instanceof Float) {
        sendEvent(SDKEvent.AircraftUltrasonicHeight, newValue);
      }
          }
    });
  }

  private void startCompassHasErrorListener() {
    startEventListener(SDKEvent.CompassHasError, new EventListener() {
      @Override
      public void onValueChange(@Nullable Object oldValue, @Nullable Object newValue) {
        if (newValue instanceof Boolean) {
          sendEvent(SDKEvent.CompassHasError, newValue);
        }
      }
    });
  }

  private void startIsRecordingListener() {
    startEventListener(SDKEvent.CameraIsRecording, new EventListener() {
      @Override
      public void onValueChange(@Nullable Object oldValue, @Nullable Object newValue) {
        if (newValue instanceof Boolean) {
          sendEvent(SDKEvent.CameraIsRecording, newValue);
        }
      }
    });
  }

  private void startSDCardIsInsertedListener() {
    startEventListener(SDKEvent.SDCardIsInserted, new EventListener() {
      @Override
      public void onValueChange(@Nullable Object oldValue, @Nullable Object newValue) {
        if (newValue instanceof Boolean) {
          sendEvent(SDKEvent.SDCardIsInserted, newValue);
        }
      }
    });
  }

  private void startSDCardIsReadOnlyListener() {
    startEventListener(SDKEvent.SDCardIsReadOnly, new EventListener() {
      @Override
      public void onValueChange(@Nullable Object oldValue, @Nullable Object newValue) {
        if (newValue instanceof Boolean) {
          sendEvent(SDKEvent.SDCardIsReadOnly, newValue);
        }
      }
    });
  }

  @ReactMethod
  public void stopEventListener(String eventName, Promise promise) {
    if (eventName.equals(SDKEvent.AircraftVelocity)) {
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
      return;
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
    if (eventSubscriptionObject != null && sdkEventHandler != null) {
      sdkEventHandler.stopEventListener(SDKEvent, eventSubscriptionObject);
      eventListeners.remove(SDKEvent);
    }
  }

  private void sendEvent(SDKEvent SDKEvent, Object value) {
    this.eventSender.processEvent(SDKEvent, value, false);
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
      params.putDouble("value", (Double) value);
    } else if (value instanceof String) {
      params.putString("value", (String)value);
    } else if (value instanceof Boolean) {
      params.putBoolean("value", (Boolean)value);
    } else if (value instanceof WritableMap) {
      params.putMap("value", (WritableMap)value);
    } else if (value instanceof WritableArray) {
      params.putArray("value", (WritableArray) value);
    } else if (value instanceof Float) {
      params.putDouble("value", Double.valueOf((Float) value));
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

    @ReactMethod
    public void getFlightLogPath(Promise promise) {
       String path = DJISDKManager.getInstance().getLogPath();
       promise.resolve(path);
    }

    //Listener for creation and modification of flight logs
    @ReactMethod
    public void startFlightLogListener(Promise promise) {
      String pathToFlightLogs = DJISDKManager.getInstance().getLogPath();
      if (flightLogObserver == null) {
        flightLogObserver = new RecursiveFileObserver(pathToFlightLogs, "DJIFlightLogEvent", reactContext);
      }
      flightLogObserver.startWatching();
      promise.resolve(null);
    }

    @ReactMethod
    public void stopFlightLogListener(Promise promise) {
      if (flightLogObserver != null) {
        flightLogObserver.stopWatching();
      }
      promise.resolve(null);
    }

    private void startAirlinkUplinkSignalQualityListener() {
      DJIKey isLightbridgeSupportedKey = AirLinkKey.create(AirLinkKey.IS_LIGHTBRIDGE_LINK_SUPPORTED);
      KeyManager.getInstance().getValue(isLightbridgeSupportedKey, new GetCallback() {
        @Override
        public void onSuccess(@NonNull Object value) {
            if (value instanceof Boolean) {
                if ((Boolean) value) {
                    startEventListener(SDKEvent.AirLinkLightbridgeUplinkSignalQuality, new EventListener() {
                        @Override
                        public void onValueChange(@Nullable Object oldValue, @Nullable Object newValue) {
                            if (newValue != null && newValue instanceof Integer) {
                                sendEvent(SDKEvent.AirLinkUplinkSignalQuality, newValue);
                            }
                        }
                    });
                }
            }
        }

        @Override
        public void onFailure(@NonNull DJIError djiError) {

        }
      });
      if (product != null && product.getAirLink() != null) {
        boolean isOcuSyncLinkSupported = product.getAirLink().isOcuSyncLinkSupported();
        if (isOcuSyncLinkSupported) {
          startEventListener(SDKEvent.AirLinkOcuSyncUplinkSignalQuality, new EventListener() {
            @Override
            public void onValueChange(@Nullable Object oldValue, @Nullable Object newValue) {
              if (newValue != null && newValue instanceof Integer) {
                sendEvent(SDKEvent.AirLinkUplinkSignalQuality, newValue);
              }
            }
          });
        }
      }
   }

   @ReactMethod
   public void limitEventFrequency(double newEventSendFrequencyInHz, Promise promise) {
    int milliSecs = (int) Math.round((1/newEventSendFrequencyInHz) * 1000);
    this.eventSender.setNewEventSendFrequency(milliSecs);
    promise.resolve(null);
   }

  @Override
  public String getName() {
    return "DJIMobile";
  }

  @Override
  public Map<String, Object> getConstants() {
    final Map<String, Object> constants = new HashMap<>();
    for (SDKEvent sdkEvent : SDKEvent.values()) {
      constants.put(sdkEvent.name(), sdkEvent.name());
    }
    return constants;
  }

}
