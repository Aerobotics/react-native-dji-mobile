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
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.security.Key;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dji.common.camera.ExposureSettings;
import dji.common.camera.SettingsDefinitions;
import dji.common.camera.SystemState;
import dji.common.error.DJIError;
import dji.common.error.DJISDKError;
import dji.common.flightcontroller.GPSSignalLevel;
import dji.common.flightcontroller.LocationCoordinate3D;
import dji.common.flightcontroller.VisionControlState;
import dji.common.flightcontroller.VisionDetectionState;
import dji.common.flightcontroller.VisionSensorPosition;
import dji.common.flightcontroller.VisionSystemWarning;
import dji.common.model.LocationCoordinate2D;
import dji.common.product.Model;
import dji.internal.diagnostics.DiagnosticsBaseHandler;
import dji.keysdk.AirLinkKey;
import dji.keysdk.BatteryKey;
import dji.keysdk.DJIKey;
import dji.keysdk.FlightControllerKey;
import dji.keysdk.KeyManager;
import dji.keysdk.ProductKey;
import dji.keysdk.callback.GetCallback;
import dji.keysdk.callback.SetCallback;
import dji.sdk.base.BaseComponent;
import dji.sdk.base.BaseProduct;
import dji.sdk.base.DJIDiagnostics;
import dji.sdk.camera.Camera;
import dji.sdk.flightcontroller.FlightAssistant;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.media.MediaFile;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKInitEvent;
import dji.sdk.sdkmanager.DJISDKManager;


public class DJIMobile extends ReactContextBaseJavaModule {

  private final ReactApplicationContext reactContext;

  private HashMap eventListeners = new HashMap();

  // This must only be initialized once the SDK has registered, as it uses the SDK
  private static SdkEventHandler sdkEventHandler;
  private BaseProduct product;

  private DJIRealTimeDataLogger djiRealTimeDataLogger;
  private Handler handler;
  private FileObserver flightLogObserver;
  private EventSender eventSender;

  private VisionControlState.Callback visionControlStateCallback = new VisionControlState.Callback() {
    @Override
    public void onUpdate(VisionControlState visionControlState) {
      if (visionControlStateIsBraking != visionControlState.isBraking()) {
        WritableMap params = Arguments.createMap();
        params.putBoolean("isBraking", visionControlState.isBraking());
        sendEvent(SDKEvent.VisionControlState, params);
      }
      visionControlStateIsBraking = visionControlState.isBraking();
    }
  };
  private boolean visionControlStateIsBraking = false;

  // Used to set up the vision control state listener when the aircraft connects, if currently unavailable
  private boolean startVisionControlStateListenerOnNextConnect = false;

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
          promise.reject(new Throwable("registerAppInternal error: " + djiError.getDescription()));
        }
      }

      @Override
      public void onProductDisconnect() {
        product = null;
      }

      @Override
      public void onProductConnect(BaseProduct baseProduct) {
        product = baseProduct;
        startDiagnosticsListener();
      }

      @Override
      public void onProductChanged(BaseProduct baseProduct) {

      }

      @Override
      public void onComponentChange(BaseProduct.ComponentKey componentKey, BaseComponent oldBaseComponent, BaseComponent newBaseComponent) {
        if (startVisionControlStateListenerOnNextConnect) {
          startVisionControlStateListener();
        }

        if (newBaseComponent != null && newBaseComponent instanceof Camera && sdkEventHandler != null) {
          sdkEventHandler.initCameraEventDelegate((Camera) newBaseComponent);
        }
      }

      @Override
      public void onInitProcess(DJISDKInitEvent djisdkInitEvent, int i) {

      }

      @Override
      public void onDatabaseDownloadProgress(long l, long l1) {

      }
    });
  }

  @ReactMethod
  public void getMediaFileList(final Promise promise) {
    DJIMedia m = new DJIMedia(reactContext);
    if (product == null) {
      promise.reject(new Throwable("getMediaFileList error: No product connected"));
    } else {
      m.getMediaFileList(promise, product);
    }
  }

  @ReactMethod
  public void getLimitedMediaFileList(Integer numberOfResults, final Promise promise) {
    DJIMedia m = new DJIMedia(reactContext);
    if (product == null) {
      promise.reject(new Throwable("getLimitedMediaFileList error: No product connected"));
    } else {
      m.getMediaFileList(promise, product, numberOfResults);
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
        promise.reject(new Throwable("setCollisionAvoidanceEnabled error: " + djiError.getDescription()));
      }
    });
  }

  @ReactMethod
  public void getCollisionAvoidanceEnabled(final Promise promise) {
    DJIKey collisionAvoidanceKey = FlightControllerKey.createFlightAssistantKey(FlightControllerKey.COLLISION_AVOIDANCE_ENABLED);
    DJISDKManager.getInstance().getKeyManager().getValue(collisionAvoidanceKey, new GetCallback() {
      @Override
      public void onSuccess(@NonNull Object value) {
        if (value instanceof Boolean) {
          promise.resolve(value);
        }
      }

      @Override
      public void onFailure(@NonNull DJIError djiError) {
        promise.reject(new Throwable("getCollisionAvoidanceEnabled error: " + djiError.getDescription()));
      }
    });
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
        promise.reject(new Throwable("setLandingProtectionEnabled error: " + djiError.getDescription()));
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
        promise.reject(new Throwable("setVisionAssistedPositioningEnabled error: " + djiError.getDescription()));
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

        case AirLinkDownlinkSignalQuality:
          startAirlinkDownlinkSignalQualityListener();
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

        case SDCardIsInserted:
          startSDCardIsInsertedListener();
          break;

        case SDCardIsReadOnly:
          startSDCardIsReadOnlyListener();
          break;

        case GimbalIsAtYawStop:
          startGimbalIsAtYawStopListener();
          break;

        case AircraftVirtualStickEnabled:
          startVirtualStickEnabledListener();
          break;

        case VisionDetectionState:
          startVisionDetectionStateListener();
          break;

        case VisionControlState:
          startVisionControlStateListener();
          break;
        case IsHomeLocationSet:
          startIsHomeLocationSetListener();
          break;
        case CameraIsShootingPhoto:
          startCameraIsShootingPhotoListener();
          break;
        case CameraIsShootingSinglePhoto:
          startCameraIsShootingSinglePhotoListener();
          break;
        case CameraIsStoringPhoto:
          startCameraIsStoringPhotoListener();
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
    KeyManager keyManager = DJISDKManager.getInstance().getKeyManager();
    if (keyManager != null) {
      DJIKey productConnectedKey = ProductKey.create(ProductKey.CONNECTION);
      keyManager.getValue(productConnectedKey, new GetCallback() {
        @Override
        public void onSuccess(@NonNull Object o) {
          if (o instanceof Boolean) {
            sendEvent(SDKEvent.ProductConnection, (boolean) o ? "connected" : "disconnected");
          }
        }

        @Override
        public void onFailure(@NonNull DJIError djiError) {}
      });
    }
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
    KeyManager keyManager = DJISDKManager.getInstance().getKeyManager();
    if (keyManager != null) {
      DJIKey batteryKey = BatteryKey.create(BatteryKey.CHARGE_REMAINING_IN_PERCENT);
      keyManager.getValue(batteryKey, new GetCallback() {
        @Override
        public void onSuccess(@NonNull Object o) {
          if (o instanceof Integer) {
            sendEvent(SDKEvent.BatteryChargeRemaining, o);
          }
        }

        @Override
        public void onFailure(@NonNull DJIError djiError) {

        }
      });
    }
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
    KeyManager keyManager = DJISDKManager.getInstance().getKeyManager();
    if (keyManager != null) {
      DJIKey key = FlightControllerKey.create(FlightControllerKey.AIRCRAFT_LOCATION);
      keyManager.getValue(key, new GetCallback() {
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
            sendEvent(SDKEvent.AircraftLocation, params); //TODO: send as realtime event
          }
        }

        @Override
        public void onFailure(@NonNull DJIError djiError) { }
      });
    }

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
        promise.reject(new Throwable("getAircraftLocation error: " + djiError.getDescription()));
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
        promise.reject(new Throwable("getAircraftIsFlying error: " + djiError.getDescription()));
      }
    });
  }

  @ReactMethod
  public void getAircraftCompassHeading(final Promise promise) {
    DJIKey compassHeadingKey = FlightControllerKey.create(FlightControllerKey.COMPASS_HEADING);
    DJISDKManager.getInstance().getKeyManager().getValue(compassHeadingKey, new GetCallback() {
      @Override
      public void onSuccess(@NonNull Object value) {
        if (value instanceof Float) {
          promise.resolve(value);
        }
      }

      @Override
      public void onFailure(@NonNull DJIError djiError) {
        promise.reject(new Throwable("getAircraftCompassHeading error: " + djiError.getDescription()));
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
    DJIKey aircraftCompassHeadingKey = (DJIKey) SDKEvent.AircraftCompassHeading.getKey();
    DJISDKManager.getInstance().getKeyManager().getValue(aircraftCompassHeadingKey, new GetCallback() {
      @Override
      public void onSuccess(@NonNull Object value) {
        if (value instanceof Float) {
          sendEvent(SDKEvent.AircraftCompassHeading, value);
        }
      }

      @Override
      public void onFailure(@NonNull DJIError djiError) {}
    });

    startEventListener(SDKEvent.AircraftCompassHeading, new EventListener() {
      @Override
      public void onValueChange(@Nullable Object oldValue, @Nullable Object newValue) {
      if (newValue != null && newValue instanceof Float) {
        sendEvent(SDKEvent.AircraftCompassHeading, newValue);
      }
      }
    });
  }

  private void startDiagnosticsListener() {
    // Add product connection listener
    DJIDiagnostics.DiagnosticsInformationCallback diagnosticsInformationCallback = new DJIDiagnostics.DiagnosticsInformationCallback() {
      @Override
      public void onUpdate(List<DJIDiagnostics> list) {
        if (!list.isEmpty()) {
          WritableArray diagnosticsToSend = Arguments.createArray();
          for (DJIDiagnostics djiDiagnostics : list) {
            DiagnosticsBaseHandler.DJIDiagnosticsError error = DiagnosticsBaseHandler.DJIDiagnosticsError.find(djiDiagnostics.getCode());
            WritableMap params = Arguments.createMap();
            params.putString("type", djiDiagnostics.getType().name());
            params.putString("reason", djiDiagnostics.getReason());
            params.putString("solution", djiDiagnostics.getSolution());
            params.putString("error", error.name());
            diagnosticsToSend.pushMap(params);
          }
          sendEvent(SDKEvent.DJIDiagnostics, diagnosticsToSend);
        }
      }
    };
    if (product != null) {
      product.setDiagnosticsInformationCallback(diagnosticsInformationCallback);
    } else {
      Log.d("REACT", "product null: could not set diag callback");
    }
  }

  @ReactMethod
  public void startCameraExposureSettingsListener(Promise promise) {
    startEventListener(SDKEvent.CameraExposureSettings, new EventListener() {
      @Override
      public void onValueChange(@Nullable Object oldValue, @Nullable Object newValue) {
        if (newValue != null && newValue instanceof ExposureSettings) {
          ExposureSettings exposureSettings = (ExposureSettings) newValue;
          WritableMap params = Arguments.createMap();

          // Form of DJI exposure settings:
          // * Aperture: 100*f-stop number. E.g. 110 = f/1.1
          // * Shutter Speed: evaluated double, e.g. 0.0008 = 1/1250
          // * ISO: int value of ISO
          // * EV: an enum value, e.g. N_0_7 = -0.7
          // Camera setting is not sent in event if setting is in error mode

          SettingsDefinitions.Aperture aperture = exposureSettings.getAperture();
          if (aperture != null && aperture != SettingsDefinitions.Aperture.UNKNOWN) {
            params.putDouble("aperture", aperture.value() / 100.0);
          }

          SettingsDefinitions.ShutterSpeed shutterSpeed = exposureSettings.getShutterSpeed();
          if (shutterSpeed != null && shutterSpeed != SettingsDefinitions.ShutterSpeed.UNKNOWN) {
            params.putDouble("shutterSpeed", shutterSpeed.value());
          }

          params.putInt("iso", exposureSettings.getISO());

          SettingsDefinitions.ExposureCompensation exposureValue = exposureSettings.getExposureCompensation();
          if (exposureValue != null && exposureValue != SettingsDefinitions.ExposureCompensation.FIXED &&
                  exposureValue != SettingsDefinitions.ExposureCompensation.UNKNOWN) {
            // transformation to float is handled in Camera Control js file
            params.putString("exposureValue", exposureValue.toString());
          }

          sendEvent(SDKEvent.CameraExposureSettings, params);
        }
      }
    });
    promise.resolve(null);
  }

  @ReactMethod
  public void startNewMediaFileListener(Promise promise) {
    boolean isCallbacksSet = sdkEventHandler.isCameraCallbacksInitialized();
    if (!isCallbacksSet) {
      if (product == null) {
        promise.reject(new Throwable("Error product not connected"));
        return;
      }
      Camera camera = product.getCamera();
      if (camera == null) {
        promise.reject(new Throwable("Error camera not connected"));
        return;
      }
      sdkEventHandler.setCameraCallbacks(camera);
    }

    startEventListener(SDKEvent.CameraDidGenerateNewMediaFile, new EventListener() {
      @Override
      public void onValueChange(@Nullable Object oldValue, @Nullable Object newValue) {
        if (newValue != null && newValue instanceof HashMap) {
          HashMap payload = (HashMap) newValue;
          MediaFile mediaFile = (MediaFile) payload.get("value");
          WritableMap params = Arguments.createMap();
          params.putString("fileName", mediaFile.getFileName());
          params.putString("dateCreated", mediaFile.getDateCreated());
          params.putDouble("fileSizeInBytes",mediaFile.getFileSize());
          sendRealTimeEvent(SDKEvent.CameraDidGenerateNewMediaFile, params);
        }
      }
    });
    startEventListener(SDKEvent.CameraDidUpdateSystemState, new EventListener() {
      @Override
      public void onValueChange(@Nullable Object oldValue, @Nullable Object newValue) {
        if (newValue != null && newValue instanceof HashMap) {
          HashMap payload = (HashMap) newValue;
          SystemState systemState = (SystemState) payload.get("value");
          boolean isShootingSinglePhoto = systemState.isShootingSinglePhoto();
          boolean isStoringPhoto = systemState.isStoringPhoto();
          WritableMap params = Arguments.createMap();
          params.putBoolean("isShootingPhoto", isShootingSinglePhoto);
          params.putBoolean("isStoringPhoto", isStoringPhoto);
          sendRealTimeEvent(SDKEvent.CameraDidUpdateSystemState, params);
        }
      }
    });
    promise.resolve(null);
  }

  private void startAircraftHomeLocationListener() {

    final Double[] homeLocation = {null, null, null};

    DJIKey aircraftHomeLocationKey = (DJIKey) SDKEvent.AircraftHomeLocation.getKey();
    DJISDKManager.getInstance().getKeyManager().getValue(aircraftHomeLocationKey, new GetCallback() {
      @Override
      public void onSuccess(@NonNull Object value) {
        if (value != null && value instanceof LocationCoordinate2D) {
          LocationCoordinate2D location = (LocationCoordinate2D) value;
          Double longitude = location.getLongitude();
          Double latitude = location.getLatitude();
          if (!latitude.isNaN() && !latitude.isInfinite() && !longitude.isNaN() && !longitude.isInfinite()) {
            homeLocation[0] = latitude;
            homeLocation[1] = longitude;
            sendAircraftHomeLocationEvent(homeLocation[0], homeLocation[1], homeLocation[2]);
          }
        }
      }

      @Override
      public void onFailure(@NonNull DJIError djiError) {}
    });


    startEventListener(SDKEvent.AircraftHomeLocation, new EventListener() {
      @Override
      public void onValueChange(@Nullable Object oldValue, @Nullable Object newValue) {
        if (newValue != null && newValue instanceof LocationCoordinate2D) {
          LocationCoordinate2D location = (LocationCoordinate2D) newValue;
          Double longitude = location.getLongitude();
          Double latitude = location.getLatitude();
          if (!latitude.isNaN() && !latitude.isInfinite() && !longitude.isNaN() && !longitude.isInfinite()) {
            homeLocation[0] = latitude;
            homeLocation[1] = longitude;
            sendAircraftHomeLocationEvent(homeLocation[0], homeLocation[1], homeLocation[2]);
          }
        }
      }
    });

    startEventListener(SDKEvent.TakeoffLocationAltitude, new EventListener() {
      @Override
      public void onValueChange(@Nullable Object oldValue, @Nullable Object newValue) {
        if (!isObjectValidFloatValue(newValue)) {
          return;
        }
        Float takeoffLocationAltitude = (Float) newValue;

        if (!isObjectValidFloatValue(oldValue)) {
          homeLocation[2] = Double.valueOf(takeoffLocationAltitude);
          sendAircraftHomeLocationEvent(homeLocation[0], homeLocation[1], homeLocation[2]);
        } else {
          // Don't send updates which are negligible
          Float oldTakeoffLocationAltitude = (Float) oldValue;
          Float altitudeDelta = Math.abs(oldTakeoffLocationAltitude - takeoffLocationAltitude);
          if (altitudeDelta > 0.2) {
            homeLocation[2] = Double.valueOf(takeoffLocationAltitude);
            sendAircraftHomeLocationEvent(homeLocation[0], homeLocation[1], homeLocation[2]);
          }
        }
      }
    });
  }

  private boolean isObjectValidFloatValue(@Nullable Object value) {
    if (!(value instanceof Float)) {
      return false;
    }
    Float valueAsFloat = (Float) value;
    return (!valueAsFloat.isNaN() && !valueAsFloat.isInfinite());
  }

  private void sendAircraftHomeLocationEvent(Double latitude, Double longitude, Double altitude) {
    WritableMap homeLocation = Arguments.createMap();
    if (latitude != null) {
      homeLocation.putDouble("latitude", latitude);
    }
    if (longitude != null) {
      homeLocation.putDouble("longitude", longitude);
    }
    if (altitude != null) {
      homeLocation.putDouble("altitude", altitude);
    }
    sendEvent(SDKEvent.AircraftHomeLocation, homeLocation);
  }

  private void startIsHomeLocationSetListener() {
    KeyManager keyManager = DJISDKManager.getInstance().getKeyManager();
    if (keyManager != null) {
      DJIKey isHomeLocationSetKey = FlightControllerKey.create(FlightControllerKey.IS_HOME_LOCATION_SET);
      keyManager.getValue(isHomeLocationSetKey, new GetCallback() {
        @Override
        public void onSuccess(@NonNull Object o) {
          if (o instanceof Boolean) {
            sendRealTimeEvent(SDKEvent.IsHomeLocationSet, o);
          }
        }

        @Override
        public void onFailure(@NonNull DJIError djiError) {

        }
      });
    }
    startEventListener(SDKEvent.IsHomeLocationSet, new EventListener() {
      @Override
      public void onValueChange(@Nullable Object oldValue, @Nullable Object newValue) {
        if (newValue instanceof Boolean) {
          sendEvent(SDKEvent.IsHomeLocationSet, newValue);
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

  private void startCameraIsShootingPhotoListener() {
    startEventListener(SDKEvent.CameraIsShootingPhoto, new EventListener() {
      @Override
      public void onValueChange(@Nullable Object oldValue, @Nullable Object newValue) {
        if (newValue instanceof Boolean) {
          sendEvent(SDKEvent.CameraIsShootingPhoto, newValue);
        }
      }
    });
  }

  private void startCameraIsShootingSinglePhotoListener() {
    startEventListener(SDKEvent.CameraIsShootingSinglePhoto, new EventListener() {
      @Override
      public void onValueChange(@Nullable Object oldValue, @Nullable Object newValue) {
        if (newValue instanceof Boolean) {
          sendEvent(SDKEvent.CameraIsShootingSinglePhoto, newValue);
        }
      }
    });
  }

  private void startCameraIsStoringPhotoListener() {
    startEventListener(SDKEvent.CameraIsStoringPhoto, new EventListener() {
      @Override
      public void onValueChange(@Nullable Object oldValue, @Nullable Object newValue) {
        if (newValue instanceof Boolean) {
          sendEvent(SDKEvent.CameraIsStoringPhoto, newValue);
        }
      }
    });
  }

  private void startVirtualStickEnabledListener() {
    startEventListener(SDKEvent.AircraftVirtualStickEnabled, new EventListener() {
      @Override
      public void onValueChange(@Nullable Object oldValue, @Nullable Object newValue) {
        if (newValue instanceof Boolean) {
          sendEvent(SDKEvent.AircraftVirtualStickEnabled, newValue);
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

  private void startGimbalIsAtYawStopListener() {
    startEventListener(SDKEvent.GimbalIsAtYawStop, new EventListener() {
      @Override
      public void onValueChange(@Nullable Object oldValue, @Nullable Object newValue) {
        if (newValue instanceof Boolean) {
          sendEvent(SDKEvent.GimbalIsAtYawStop, newValue);
        }
      }
    });
  }

  private void startVisionDetectionStateListener() {
    final HashMap<VisionSensorPosition, VisionSystemWarning> sensorValues = new HashMap<>();
    sensorValues.put(VisionSensorPosition.LEFT, VisionSystemWarning.UNKNOWN);
    sensorValues.put(VisionSensorPosition.NOSE, VisionSystemWarning.UNKNOWN);
    sensorValues.put(VisionSensorPosition.RIGHT, VisionSystemWarning.UNKNOWN);
    sensorValues.put(VisionSensorPosition.TAIL, VisionSystemWarning.UNKNOWN);

    startEventListener(SDKEvent.VisionDetectionState, new EventListener() {
      @Override
      public void onValueChange(@Nullable Object oldValue, @Nullable Object newValue) {
        if (newValue instanceof VisionDetectionState) {
          VisionDetectionState visionDetectionState = ((VisionDetectionState) newValue);
          VisionSystemWarning visionSystemWarning = visionDetectionState.getSystemWarning();
          VisionSensorPosition visionSensorPosition = visionDetectionState.getPosition();

          if (sensorValues.get(visionSensorPosition) != visionSystemWarning) {
            final WritableMap params = Arguments.createMap();
            for (VisionSensorPosition key : sensorValues.keySet()) {
              params.putString(key.name(), sensorValues.get(key).name());
            }
            sendEvent(SDKEvent.VisionDetectionState, params);
          }
          sensorValues.put(visionSensorPosition, visionSystemWarning);

        }
      }
    });
  }

  private void startVisionControlStateListener() {
    Log.i("Vision", "startVisionControlStateListener");
    Aircraft drone = (Aircraft) DJISDKManager.getInstance().getProduct();
    if (drone == null) {
      startVisionControlStateListenerOnNextConnect = true;
      return;
    }
    FlightController flightController = drone.getFlightController();
    if (flightController == null) {
      startVisionControlStateListenerOnNextConnect = true;
      return;
    }
    FlightAssistant flightAssistant = flightController.getFlightAssistant();
    if (flightAssistant == null) {
      startVisionControlStateListenerOnNextConnect = true;
      return;
    }
    startVisionControlStateListenerOnNextConnect = false;
    flightAssistant.setVisionControlStateUpdatedcallback(visionControlStateCallback);
  }

  private void stopVisionControlStateListener() {
    Aircraft drone = (Aircraft) DJISDKManager.getInstance().getProduct();
    FlightController flightController = drone.getFlightController();
    FlightAssistant flightAssistant = flightController.getFlightAssistant();
    if (flightAssistant != null) {
      flightAssistant.setVisionControlStateUpdatedcallback(null);
    }
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

    } else if (eventName.equals(SDKEvent.AircraftHomeLocation)) {
      stopEventListenerInternal(SDKEvent.AircraftHomeLocation);
      stopEventListenerInternal(SDKEvent.TakeoffLocationAltitude);

    } else if (eventName.equals(SDKEvent.VisionControlState)) {
      stopVisionControlStateListener();

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
      // if sdkEventHandler is null it is because the sdk has not been registered so dont attempt to start new event listeners
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

  private void sendRealTimeEvent(SDKEvent SDKEvent, Object value) {
    this.eventSender.processEvent(SDKEvent, value, true);
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
      KeyManager keyManager = DJISDKManager.getInstance().getKeyManager();
      if (keyManager != null) {
        keyManager.getValue(isLightbridgeSupportedKey, new GetCallback() {
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
          public void onFailure(@NonNull DJIError djiError) {}
        });
      }

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

  private void startAirlinkDownlinkSignalQualityListener() {
    DJIKey isLightbridgeSupportedKey = AirLinkKey.create(AirLinkKey.IS_LIGHTBRIDGE_LINK_SUPPORTED);
    KeyManager keyManager = DJISDKManager.getInstance().getKeyManager();
    if (keyManager != null) {
      keyManager.getValue(isLightbridgeSupportedKey, new GetCallback() {
        @Override
        public void onSuccess(@NonNull Object value) {
          if (value instanceof Boolean) {
            if ((Boolean) value) {
              startEventListener(SDKEvent.AirLinkLightbridgeDownlinkSignalQuality, new EventListener() {
                @Override
                public void onValueChange(@Nullable Object oldValue, @Nullable Object newValue) {
                  if (newValue != null && newValue instanceof Integer) {
                    sendEvent(SDKEvent.AirLinkDownlinkSignalQuality, newValue);
                  }
                }
              });
            }
          }
        }

        @Override
        public void onFailure(@NonNull DJIError djiError) {}
      });
    }

    if (product != null && product.getAirLink() != null) {
      boolean isOcuSyncLinkSupported = product.getAirLink().isOcuSyncLinkSupported();
      if (isOcuSyncLinkSupported) {
        startEventListener(SDKEvent.AirLinkOcuSyncDownlinkSignalQuality, new EventListener() {
          @Override
          public void onValueChange(@Nullable Object oldValue, @Nullable Object newValue) {
            if (newValue != null && newValue instanceof Integer) {
              sendEvent(SDKEvent.AirLinkDownlinkSignalQuality, newValue);
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

    @ReactMethod
    public void isProductConnected(final Promise promise) {
      DJIKey productConnectedKey = ProductKey.create(ProductKey.CONNECTION);
      DJISDKManager.getInstance().getKeyManager().getValue(productConnectedKey, new GetCallback() {
        @Override
        public void onSuccess(@NonNull Object o) {
          if (o instanceof Boolean) {
            promise.resolve((Boolean) o);
          }
        }

        @Override
        public void onFailure(@NonNull DJIError djiError) {
          promise.reject(new Throwable("isProductConnected error: " + djiError.getDescription()));
        }
      });
    }

    @ReactMethod
    public void getModelName(final Promise promise) {
      ProductKey modelNameKey = ProductKey.create(ProductKey.MODEL_NAME);
      KeyManager.getInstance().getValue(modelNameKey, new GetCallback() {
        @Override
        public void onSuccess(@NonNull Object value) {
          if (value instanceof Model) {
            promise.resolve(((Model) value).name());
          }
        }

        @Override
        public void onFailure(@NonNull DJIError djiError) {
          promise.reject(new Throwable("getModelName error: " + djiError.getDescription()));
        }
      });
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
