package com.aerobotics.DjiMobile;

import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aerobotics.DjiMobile.DJITimelineElements.VirtualStickTimelineElement;
import com.aerobotics.DjiMobile.DJITimelineElements.WaypointMissionTimelineElement;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;

import javax.annotation.Nonnull;

import dji.common.error.DJIError;
import dji.common.flightcontroller.LEDsSettings;
import dji.common.flightcontroller.RemoteControllerFlightMode;
import dji.common.mission.MissionState;
import dji.common.mission.waypoint.WaypointExecutionProgress;
import dji.common.mission.waypoint.WaypointMission;
import dji.common.mission.waypoint.WaypointMissionDownloadEvent;
import dji.common.mission.waypoint.WaypointMissionExecutionEvent;
import dji.common.mission.waypoint.WaypointMissionState;
import dji.common.mission.waypoint.WaypointMissionUploadEvent;
import dji.common.mission.waypoint.WaypointUploadProgress;
import dji.common.mission.waypointv2.WaypointV2MissionExecuteState;
import dji.common.util.CommonCallbacks;
import dji.keysdk.DJIKey;
import dji.keysdk.FlightControllerKey;
import dji.keysdk.callback.ActionCallback;
import dji.keysdk.callback.GetCallback;
import dji.keysdk.callback.SetCallback;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.mission.timeline.actions.AircraftYawAction;
import dji.sdk.mission.waypoint.WaypointMissionOperator;
import dji.sdk.mission.waypoint.WaypointMissionOperatorListener;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKManager;


public class FlightControllerWrapper extends ReactContextBaseJavaModule {
  private final ReactApplicationContext reactContext;
  private final EventSender eventSender;
  private WaypointMissionOperator waypointMissionOperator;
  private VirtualStickTimelineElement virtualStickTimelineElement;
  private DJIRealTimeDataLogger djiRealTimeDataLogger;

  private boolean enableWaypointExecutionFinishListener = false;
  private boolean enableWaypointExecutionUpdateListener = false;
  private boolean enableWaypointMissionStateListener = false;
  private boolean enableWaypointMissionUploadListener = false;
  private FlightController.OnboardSDKDeviceDataCallback onboardSDKDeviceDataCallback;

  private static WaypointMissionOperatorListener waypointMissionOperatorListener;

  public FlightControllerWrapper(@Nonnull ReactApplicationContext reactContext) {
    super(reactContext);
    this.eventSender = new EventSender(reactContext);
    this.reactContext = reactContext;
  }

  @ReactMethod
  public void uploadWaypointMission(ReadableMap parameters, Promise promise) {
    try {
      WaypointMissionTimelineElement waypointMissionTimelineElement = new WaypointMissionTimelineElement(parameters);
      checkMissionParameters(waypointMissionTimelineElement);
      WaypointMission waypointMission = waypointMissionTimelineElement.build();
      loadMission(waypointMission);
      uploadMission(promise);
    } catch (Exception error) {
      promise.reject(new Throwable(error.getMessage()));
    }
  }

  @ReactMethod
  public void startWaypointMission(final Promise promise) {
    if (getWaypointMissionOperator().getCurrentState().equals(WaypointMissionState.READY_TO_EXECUTE)) {
      getWaypointMissionOperator().startMission(new CommonCallbacks.CompletionCallback<DJIError>() {
        @Override
        public void onResult(DJIError djiError) {
          if (djiError == null) {
            promise.resolve(null);
            eventSender.processEvent(SDKEvent.WaypointMissionStarted, true, true);
          } else {
            promise.reject(new Throwable("startWaypointMission: " + djiError.getDescription()));
            eventSender.processEvent(SDKEvent.WaypointMissionStarted, false, true);
          }
        }
      });
    } else {
      promise.reject(new Throwable("startWaypointMission: incorrect mission state " + getWaypointMissionOperator().getCurrentState().getName()));
    }
  }

  private void setWaypointMissionOperatorListener() {
    if (waypointMissionOperatorListener != null) {
      return;
    }
    waypointMissionOperatorListener = new WaypointMissionOperatorListener() {
      @Override
      public void onDownloadUpdate(@NonNull WaypointMissionDownloadEvent event) { }

      @Override
      public void onUploadUpdate(@NonNull WaypointMissionUploadEvent event) {
        handlePossibleErrorUpdate(event.getError());

        if (event.getProgress() != null) {
          sendWaypointMissionUploadUpdate(event.getProgress());
        }

        /*
         * The `waypointMissionUploadEvent` state differs from the mission operator state at times.
         * Choosing to use the mission operator state as the source of truth.
         */
        sendWaypointMissionStateUpdate(getWaypointMissionOperator().getCurrentState());
      }

      @Override
      public void onExecutionUpdate(@NonNull WaypointMissionExecutionEvent event) {
        handlePossibleErrorUpdate(event.getError());
        sendWaypointMissionExecutionUpdate(event.getProgress());
        /*
         * The `waypointMissionExecutionEvent` state differs from the mission operator state at times.
         * Choosing to use the mission operator state as the source of truth.
         */
        sendWaypointMissionStateUpdate(getWaypointMissionOperator().getCurrentState());
      }

      @Override
      public void onExecutionStart() {
        sendWaypointMissionStateUpdate(getWaypointMissionOperator().getCurrentState());
      }

      @Override
      public void onExecutionFinish(@Nullable DJIError djiError) {
        handlePossibleErrorUpdate(djiError);
        sendWaypointMissionStateUpdate(getWaypointMissionOperator().getCurrentState());
        if (enableWaypointExecutionFinishListener) {
          eventSender.processEvent(SDKEvent.WaypointMissionFinished, true, true);
        }
      }
    };
    getWaypointMissionOperator().addListener(waypointMissionOperatorListener);
  }

  private void handlePossibleErrorUpdate(@Nullable DJIError error) {
    if (error != null) {
      sendWaypointMissionError(error);
    } else {
      sendNullWaypointMissionError();
    }
  }

  private void checkMissionParameters(WaypointMissionTimelineElement waypointMissionTimelineElement) throws Exception {
    DJIError error = waypointMissionTimelineElement.checkValidity();
    if (error != null) {
      throw new Exception("checkMissionParameters error: " + error.getDescription());
    }
  }

  private void loadMission(WaypointMission mission) throws Exception {
    DJIError error = getWaypointMissionOperator().loadMission(mission);
    if (error != null) {
      throw new Exception("loadMission error: " + error.getDescription());
    }
  }

  private void uploadMission(final Promise promise) {
    getWaypointMissionOperator().uploadMission(new CommonCallbacks.CompletionCallback<DJIError>() {
      @Override
      public void onResult(DJIError error) {
        if (error == null) {
          promise.resolve(null);
        } else {
          if (getWaypointMissionOperator().getCurrentState().equals(WaypointMissionState.READY_TO_UPLOAD)) {
            getWaypointMissionOperator().retryUploadMission((new CommonCallbacks.CompletionCallback<DJIError>() {
              @Override
              public void onResult(DJIError error) {
                if (error != null) {
                  promise.reject(new Throwable("uploadMission error: Upload Failed" + error.getDescription()));
                } else {
                  promise.resolve(null);
                }
              }
            }));
          } else {
            promise.reject(new Throwable("uploadMission error: Upload failed due to incorrect state " + getWaypointMissionOperator().getCurrentState().getName()));
          }
        }
      }
    });
  }

  private WaypointMissionOperator getWaypointMissionOperator() {
    if(waypointMissionOperator == null) {
      waypointMissionOperator = DJISDKManager.getInstance().getMissionControl().getWaypointMissionOperator();
    }
    return waypointMissionOperator;
  }

  @ReactMethod
  public void stopWaypointMission(final Promise promise) {
    getWaypointMissionOperator().stopMission(new CommonCallbacks.CompletionCallback<DJIError>() {
      @Override
      public void onResult(DJIError djiError) {
        if (djiError == null) {
          promise.resolve(null);
        } else {
          promise.reject(new Throwable("stopWaypointMission error: " + djiError.getDescription()));
        }
      }
    });
  }

  @ReactMethod
  public void setWaypointMissionAutoFlightSpeed(Float speed, final Promise promise) {
    getWaypointMissionOperator().setAutoFlightSpeed(speed, new CommonCallbacks.CompletionCallback<DJIError>() {
      @Override
      public void onResult(DJIError djiError) {
        if (djiError == null) {
          promise.resolve(null);
        } else {
          promise.reject(new Throwable("setWaypointMissionAutoFlightSpeed error: " + djiError.getDescription()));
        }
      }
    });
  }

  @ReactMethod
  public void startVirtualStick(ReadableMap parameters, Promise promise) {
    virtualStickTimelineElement = new VirtualStickTimelineElement(reactContext, parameters);
    virtualStickTimelineElement.initializeVirtualStickEventSender(eventSender);
    virtualStickTimelineElement.run();
    promise.resolve(null);
  }

  @ReactMethod
  public void stopVirtualStick(Promise promise) {
    if (virtualStickTimelineElement != null) {
      virtualStickTimelineElement.stop();
    }
    promise.resolve(null);
  }

  @ReactMethod
  public void startYawAction(Float angle, boolean isAbsolute, final Integer timeoutMs, final Promise promise) {
    final AircraftYawAction aircraftYawAction = new AircraftYawAction(angle, isAbsolute);
    final Handler handler = new Handler(reactContext.getMainLooper());
    final long delayMs = 100;
    final long startTimeMs[] = {0};
    final boolean hasYawStarted[] = {false};

    final Runnable waitForYawEnd = new Runnable() {
      @Override
      public void run() {
        if (aircraftYawAction.isRunning()) {
          hasYawStarted[0] = true;
        }

        if (!aircraftYawAction.isRunning() & hasYawStarted[0]) {
          promise.resolve(null);
        } else if (System.currentTimeMillis() - startTimeMs[0] >= timeoutMs) {
          promise.reject("1", "Timeout waiting for yaw action to complete");
        } else {
          handler.postDelayed(this, delayMs);
        }
      }
    };

    // Start the yaw action and the runnable to wait for it to end
    aircraftYawAction.run();
    startTimeMs[0] = System.currentTimeMillis();
    handler.post(waitForYawEnd);
  }

  @ReactMethod
  public void stopAllWaypointMissionListeners(Promise promise) {
    getWaypointMissionOperator().removeListener(waypointMissionOperatorListener);
    enableWaypointExecutionUpdateListener = false;
    enableWaypointExecutionFinishListener = false;
    promise.resolve("stopAllWaypointMissionListeners");
  }

  @ReactMethod
  public void startWaypointMissionFinishedListener(Promise promise) {
    enableWaypointExecutionFinishListener = true;
    setWaypointMissionOperatorListener();
    promise.resolve("startWaypointMissionFinishedListener");
  }

  @ReactMethod
  public void startWaypointExecutionUpdateListener(Promise promise) {
    enableWaypointExecutionUpdateListener = true;
    setWaypointMissionOperatorListener();
    promise.resolve("startWaypointExecutionUpdateListener");
  }

  private void sendWaypointMissionExecutionUpdate(WaypointExecutionProgress progress) {
    if (enableWaypointExecutionUpdateListener) {
      WritableMap progressMap = Arguments.createMap();
      progressMap.putInt("targetWaypointIndex", progress.targetWaypointIndex);
      progressMap.putInt("totalWaypointCount", progress.totalWaypointCount);
      progressMap.putBoolean("isWaypointReached", progress.isWaypointReached);
      progressMap.putString("executeState", progress.executeState.name());

      // If last waypoint has been completed, increment target waypoint index
      WaypointMission waypointMission = getWaypointMissionOperator().getLoadedMission();
      if (waypointMission != null && progress.targetWaypointIndex == waypointMission.getWaypointCount() - 1 && progress.isWaypointReached) {
        progressMap.putInt("targetWaypointIndex", progress.totalWaypointCount);
      } else {
        progressMap.putInt("targetWaypointIndex", progress.targetWaypointIndex);
      }
      eventSender.processEvent(SDKEvent.WaypointMissionExecutionProgress, progressMap, true);
    }
  }

  @ReactMethod
  public void startWaypointMissionStateListener(Promise promise) {
    enableWaypointMissionStateListener = true;
    setWaypointMissionOperatorListener();
    sendWaypointMissionStateUpdate(getWaypointMissionOperator().getCurrentState());
    promise.resolve("startWaypointMissionStateListener");
  }

  @ReactMethod
  public void stopWaypointMissionStateListener(Promise promise) {
    enableWaypointMissionStateListener = false;
    promise.resolve("stopWaypointMissionStateListener");
  }

  private void sendWaypointMissionStateUpdate(MissionState missionState) {
    if (enableWaypointMissionStateListener) {
      eventSender.processEvent(SDKEvent.WaypointMissionState, missionState.getName(), true);
    }
  }

  private void sendWaypointMissionError(DJIError error) {
    eventSender.processEvent(SDKEvent.WaypointMissionError, error.getDescription(), true);
  }

  private void sendNullWaypointMissionError() {
    eventSender.processEvent(SDKEvent.WaypointMissionError, null, true);
  }

  @ReactMethod
  public void startWaypointMissionUploadListener(Promise promise) {
    enableWaypointMissionUploadListener = true;
    setWaypointMissionOperatorListener();
    promise.resolve("startWaypointMissionUploadListener");
  }

  @ReactMethod
  public void stopWaypointMissionUploadListener(Promise promise) {
    enableWaypointExecutionUpdateListener = false;
    promise.resolve("stopWaypointMissionUploadListener");
  }

  private void sendWaypointMissionUploadUpdate(WaypointUploadProgress waypointUploadProgress) {
    if (enableWaypointMissionUploadListener && waypointUploadProgress != null) {
      WritableMap progressMap = Arguments.createMap();
      progressMap.putInt("uploadedWaypointIndex", waypointUploadProgress.uploadedWaypointIndex);
      progressMap.putInt("totalWaypointCount", waypointUploadProgress.totalWaypointCount);
      progressMap.putBoolean("isSummaryUploaded",  waypointUploadProgress.isSummaryUploaded);
      eventSender.processEvent(SDKEvent.WaypointMissionUploadProgress, progressMap, true);
    }
  }

  @ReactMethod
  public void startRecordFlightData(String fileName, Promise promise){
    if (djiRealTimeDataLogger == null) {
      djiRealTimeDataLogger = new DJIRealTimeDataLogger(reactContext);
    }
    djiRealTimeDataLogger.startLogging(fileName);
    promise.resolve(null);
  }

  @ReactMethod
  public void stopRecordFlightData(Promise promise) {
    if (djiRealTimeDataLogger != null) {
      djiRealTimeDataLogger.stopLogging();
    }
    promise.resolve(null);
  }

  @ReactMethod
  public void setAutoFlightSpeed(float speed, final Promise promise) {
    getWaypointMissionOperator().setAutoFlightSpeed(speed, new CommonCallbacks.CompletionCallback<DJIError>() {
      @Override
      public void onResult(DJIError djiError) {
        if (djiError == null) {
          promise.resolve(null);
        } else {
          promise.reject(new Throwable("setAutoFlightSpeed error: " + djiError.getDescription()));
        }
      }
    });
  }

  @ReactMethod
  public void setTerrainFollowModeEnabled(boolean enabled, final Promise promise) {
    DJIKey terrainFollowModeEnabledKey = FlightControllerKey.create(FlightControllerKey.VIRTUAL_STICK_CONTROL_MODE_ENABLED);
    DJISDKManager.getInstance().getKeyManager().setValue(terrainFollowModeEnabledKey, enabled, new SetCallback() {
      @Override
      public void onSuccess() {
        promise.resolve(null);
      }

      @Override
      public void onFailure(@NonNull DJIError djiError) {
        promise.reject(new Throwable("setTerrainFollowModeEnabled error: " + djiError.getDescription()));
      }
    });
  }

  @ReactMethod
  public void getUltrasonicHeight(final Promise promise) {
    DJIKey ultrasonicHeightKey = FlightControllerKey.create(FlightControllerKey.ULTRASONIC_HEIGHT_IN_METERS);
    DJISDKManager.getInstance().getKeyManager().getValue(ultrasonicHeightKey, new GetCallback() {
      @Override
      public void onSuccess(@NonNull Object value) {
        if (value instanceof Float) {
          promise.resolve(value);
        }
      }

      @Override
      public void onFailure(@NonNull DJIError djiError) {
        promise.reject(new Throwable("getUltrasonicHeight error: " + djiError.getDescription()));
      }
    });
  }

  @ReactMethod
  public void setVirtualStickAdvancedModeEnabled(Boolean enabled, Promise promise) {
    Aircraft product = ((Aircraft)DJISDKManager.getInstance().getProduct());
    if (product != null) {
      final FlightController flightController = product.getFlightController();
      flightController.setVirtualStickAdvancedModeEnabled(enabled);
      promise.resolve(null);
    } else {
      promise.reject(new Throwable("setVirtualStickAdvancedModeEnabled error: Could not get product instance"));
    }
  }

  @ReactMethod
  public void isVirtualStickAdvancedModeEnabled(Promise promise) {
    Aircraft product = ((Aircraft)DJISDKManager.getInstance().getProduct());
    if (product != null) {
      final FlightController flightController = product.getFlightController();
      Boolean isVirtualStickAdvancedModeEnabled = flightController.isVirtualStickAdvancedModeEnabled();
      promise.resolve(isVirtualStickAdvancedModeEnabled);
    } else {
      promise.reject(new Throwable("isVirtualStickAdvancedModeEnabled error: Could not get product instance"));
    }
  }

  @ReactMethod
  public void isOnboardSDKDeviceAvailable(final Promise promise) {
    DJIKey isOnboardSDKDeviceAvailableKey = FlightControllerKey.create(FlightControllerKey.IS_ON_BOARD_SDK_AVAILABLE);
    DJISDKManager.getInstance().getKeyManager().getValue(isOnboardSDKDeviceAvailableKey, new GetCallback() {
      @Override
      public void onSuccess(Object value) {
        if (value instanceof Boolean){
          promise.resolve(value);
        }
      }

      @Override
      public void onFailure(DJIError djiError) {
        promise.reject(new Throwable("isOnboardSDKDeviceAvailable error: " + djiError.getDescription()));
      }
    });
  }

  @ReactMethod
  public void sendDataToOnboardSDKDevice(ReadableArray data, final Promise promise) {
    if (data == null) {
      promise.reject(new Throwable("sendDataToOnboardSDKDevice error: no data to send"));
      return;
    }

    byte[] bytesToSend = new byte[data.size()];
    for (int i = 0; i < data.size(); i++) {
      bytesToSend[i] = (byte) data.getInt(i);
    }
    if (bytesToSend.length < 1) {
      promise.reject(new Throwable("sendDataToOnboardSDKDevice error: no data to send " + String.valueOf(data.size())));
    }
    if (bytesToSend.length > 100) {
      promise.reject(new Throwable("sendDataToOnboardSDKDevice error: data exceeds max number of bytes"));
    }
    DJIKey sendDataToOnboardSDKDeviceKey = FlightControllerKey.create(FlightControllerKey.SEND_DATA_TO_ON_BOARD_SDK_DEVICE);
    DJISDKManager.getInstance().getKeyManager().performAction(sendDataToOnboardSDKDeviceKey, new ActionCallback() {
      @Override
      public void onSuccess() {
        promise.resolve(null);
      }

      @Override
      public void onFailure(DJIError djiError) {
        promise.reject(new Throwable("sendDataToOnboardSDKDevice error: " + djiError.getDescription()));
      }
    }, bytesToSend);
  }

  @ReactMethod
  public void startOnboardSDKDeviceDataListener(final Promise promise) {
    Aircraft product = ((Aircraft)DJISDKManager.getInstance().getProduct());
    if (product == null) {
      promise.reject(new Throwable("startOnboardSDKDeviceDataListener error: could not connect to product"));
      return;
    }
    final FlightController flightController = product.getFlightController();
    if (flightController == null) {
      promise.reject(new Throwable("startOnboardSDKDeviceDataListener error: could not connect to flight controller"));
      return;
    }

    if (onboardSDKDeviceDataCallback == null) {
      onboardSDKDeviceDataCallback = new FlightController.OnboardSDKDeviceDataCallback() {
        @Override
        public void onReceive(byte[] bytes) {
          WritableArray RNFormattedByteArray = Arguments.createArray();
          for (byte b : bytes) {
            RNFormattedByteArray.pushInt(b);
          }
          eventSender.processEvent(SDKEvent.OnboardSDKDeviceData, RNFormattedByteArray, true);
        }
      };
      flightController.setOnboardSDKDeviceDataCallback(onboardSDKDeviceDataCallback);
    }
    promise.resolve(null);
  }

  @ReactMethod
  public void stopOnboardSDKDeviceDataListener(final Promise promise) {
    if (onboardSDKDeviceDataCallback != null) {
      Aircraft product = ((Aircraft)DJISDKManager.getInstance().getProduct());
      if (product == null) {
        promise.reject(new Throwable("startOnboardSDKDeviceDataListener error: could not connect to product"));
        return;
      }
      final FlightController flightController = product.getFlightController();
      if (flightController == null) {
        promise.reject(new Throwable("startOnboardSDKDeviceDataListener error: could not connect to flight controller"));
        return;
      }
      flightController.setOnboardSDKDeviceDataCallback(null);
      onboardSDKDeviceDataCallback = null;
    }
    promise.resolve(null);
  }

  @ReactMethod
  public void setPowerSupplyPortEnabled(Boolean enabled, final Promise promise) {
    Aircraft product = ((Aircraft)DJISDKManager.getInstance().getProduct());
    if (product == null) {
      promise.reject(new Throwable("setPowerSupplyPortEnabled error: could not connect to product"));
      return;
    }
    final FlightController flightController = product.getFlightController();
    if (flightController == null) {
      promise.reject(new Throwable("setPowerSupplyPortEnabled error: could not connect to flight controller"));
      return;
    }
    flightController.setPowerSupplyPortEnabled(enabled, new CommonCallbacks.CompletionCallback() {
      @Override
      public void onResult(DJIError error) {
        if (error != null) {
          promise.reject(new Throwable("setPowerSupplyPortEnabled error: " + error.getDescription()));
        } else {
          promise.resolve(true);
        }
      }
    });

  }

  @ReactMethod
  public void getPowerSupplyPortEnabled(final Promise promise) {
    Aircraft product = ((Aircraft)DJISDKManager.getInstance().getProduct());
    if (product == null) {
      promise.reject(new Throwable("getPowerSupplyPortEnabled error: could not connect to product"));
      return;
    }
    final FlightController flightController = product.getFlightController();
    if (flightController == null) {
      promise.reject(new Throwable("getPowerSupplyPortEnabled error: could not connect to flight controller"));
      return;
    }
    flightController.getPowerSupplyPortEnabled(new CommonCallbacks.CompletionCallbackWith() {
      @Override
      public void onSuccess(@NonNull Object value) {
        if (value instanceof Boolean) {
          promise.resolve(value);
        }
      }

      @Override
      public void onFailure(@NonNull DJIError djiError) {
        promise.reject(new Throwable("getPowerSupplyPortEnabled error: " + djiError.getDescription()));
      }
    });
  }

  @ReactMethod
  public void doesCompassNeedCalibrating(final Promise promise) {
    DJIKey compassCalibrationKey = FlightControllerKey.create(FlightControllerKey.COMPASS_HAS_ERROR);
    DJISDKManager.getInstance().getKeyManager().getValue(compassCalibrationKey, new GetCallback() {
      @Override
      public void onSuccess(@NonNull Object value) {
        if (value instanceof Boolean) {
          promise.resolve(value);
        }
      }

      @Override
      public void onFailure(@NonNull DJIError djiError) {
        promise.reject(new Throwable("doesCompassNeedCalibrating error: " + djiError.getDescription()));
      }
    });
  }

  @ReactMethod
  public void getRemoteControllerFlightMode(final Promise promise) {
    DJIKey remoteControllerFlightModeKey = FlightControllerKey.create(FlightControllerKey.CURRENT_MODE);
    DJISDKManager.getInstance().getKeyManager().getValue(remoteControllerFlightModeKey, new GetCallback() {
      @Override
      public void onSuccess(Object value) {
        if (value instanceof RemoteControllerFlightMode) {
          promise.resolve(((RemoteControllerFlightMode) value).name());
        }
      }

      @Override
      public void onFailure(DJIError djiError) {
        promise.reject(new Throwable("getRemoteControllerFlightMode error: " + djiError.getDescription()));
      }
    });
  }

  @ReactMethod
  public void setAircraftLEDsState(final ReadableMap ledSettingsMap, final Promise promise) {
    LEDsSettings.Builder builder = new LEDsSettings.Builder();
    try {
      Boolean frontLEDsOn = ledSettingsMap.getBoolean("frontLEDsOn");
      builder.frontLEDsOn(frontLEDsOn);
    } catch (Exception e) {}
    try {
      Boolean rearLEDsOn = ledSettingsMap.getBoolean("rearLEDsOn");
      builder.rearLEDsOn(rearLEDsOn);
    } catch (Exception e) {}
    try {
      Boolean statusIndicatorOn = ledSettingsMap.getBoolean("statusIndicatorOn");
      builder.statusIndicatorOn(statusIndicatorOn);
    } catch (Exception e) {}
    try {
      Boolean beaconsOn = ledSettingsMap.getBoolean("beaconsOn");
      builder.beaconsOn(beaconsOn);
    } catch (Exception e) {}

    LEDsSettings ledSettings = builder.build();
    DJIKey key = FlightControllerKey.create(FlightControllerKey.LEDS_ENABLED_SETTINGS);
    DJISDKManager.getInstance().getKeyManager().setValue(key, ledSettings, new SetCallback() {
      @Override
      public void onSuccess() {
        promise.resolve(null);
      }

      @Override
      public void onFailure(@NonNull DJIError djiError) {
        promise.reject(new Throwable("setAircraftLEDsState error: " + djiError.getDescription()));
      }
    });
  }

  @ReactMethod
  public void getAircraftLEDsState(final Promise promise) {
    DJIKey key = FlightControllerKey.create(FlightControllerKey.LEDS_ENABLED_SETTINGS);
    DJISDKManager.getInstance().getKeyManager().getValue(key, new GetCallback() {
      @Override
      public void onSuccess(@NonNull Object o) {
        if (o instanceof LEDsSettings) {
          LEDsSettings ledSettings = (LEDsSettings)o;
          WritableMap settings = Arguments.createMap();
          settings.putBoolean("areFrontLEDsOn", ledSettings.areFrontLEDsOn());
          settings.putBoolean("areRearLEDsOn", ledSettings.areRearLEDsOn());
          settings.putBoolean("areBeaconsOn", ledSettings.areBeaconsOn());
          settings.putBoolean("isStatusIndicatorOn", ledSettings.isStatusIndicatorOn());
          promise.resolve(settings);
        }
      }

      @Override
      public void onFailure(@NonNull DJIError djiError) {
        promise.reject(new Throwable("getAircraftLEDsState error: " + djiError.getDescription()));
      }
    });
  }

  @Nonnull
  @Override
  public String getName() {
    return "FlightControllerWrapper";
  }
}
