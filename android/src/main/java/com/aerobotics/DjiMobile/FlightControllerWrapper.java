package com.aerobotics.DjiMobile;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aerobotics.DjiMobile.DJITimelineElements.VirtualStickTimelineElement;
import com.aerobotics.DjiMobile.DJITimelineElements.WaypointMissionTimelineElement;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;

import javax.annotation.Nonnull;

import dji.common.error.DJIError;
import dji.common.mission.waypoint.WaypointExecutionProgress;
import dji.common.mission.waypoint.WaypointMission;
import dji.common.mission.waypoint.WaypointMissionDownloadEvent;
import dji.common.mission.waypoint.WaypointMissionExecutionEvent;
import dji.common.mission.waypoint.WaypointMissionState;
import dji.common.mission.waypoint.WaypointMissionUploadEvent;
import dji.common.util.CommonCallbacks;
import dji.keysdk.DJIKey;
import dji.keysdk.FlightControllerKey;
import dji.keysdk.callback.SetCallback;
import dji.sdk.mission.waypoint.WaypointMissionOperator;
import dji.sdk.mission.waypoint.WaypointMissionOperatorListener;
import dji.sdk.sdkmanager.DJISDKManager;

public class FlightControllerWrapper extends ReactContextBaseJavaModule {
    private final ReactApplicationContext reactContext;
    private EventSender eventSender;
    private WaypointMissionOperator waypointMissionOperator;
    private VirtualStickTimelineElement virtualStickTimelineElement;
    private DJIRealTimeDataLogger djiRealTimeDataLogger;
    private Promise startMissionPromise;

    private boolean enableWaypointExecutionFinishListener = false;
    private boolean enableWaypointExecutionUpdateListener = false;

    private WaypointMissionOperatorListener waypointMissionOperatorListener = new WaypointMissionOperatorListener() {
        @Override
        public void onDownloadUpdate(@NonNull WaypointMissionDownloadEvent waypointMissionDownloadEvent) {

        }

        @Override
        public void onUploadUpdate(@NonNull WaypointMissionUploadEvent waypointMissionUploadEvent) {
            if (waypointMissionUploadEvent.getCurrentState().equals(WaypointMissionState.READY_TO_EXECUTE)) {
                getWaypointMissionOperator().startMission(new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        if (djiError == null) {
                            startMissionPromise.resolve(null);
                            eventSender.processEvent(SDKEvent.WaypointMissionStarted, true, true);
                        } else {
                            eventSender.processEvent(SDKEvent.WaypointMissionStarted, false, true);
                        }
                    }
                });
            }
        }

        @Override
        public void onExecutionUpdate(@NonNull WaypointMissionExecutionEvent waypointMissionExecutionEvent) {
            if (!enableWaypointExecutionUpdateListener) { // Do not send events unnecessarily
                return;
            }
            WritableMap progressMap = Arguments.createMap();
            WaypointExecutionProgress waypointExecutionProgress = waypointMissionExecutionEvent.getProgress();
            progressMap.putDouble("targetWaypointIndex", waypointExecutionProgress.targetWaypointIndex);
            progressMap.putBoolean("isWaypointReached", waypointExecutionProgress.isWaypointReached);
            progressMap.putString("executeState", waypointExecutionProgress.executeState.name());
            eventSender.processEvent(SDKEvent.WaypointMissionExecutionProgress, progressMap, true);
        }

        @Override
        public void onExecutionStart() {
        }

        @Override
        public void onExecutionFinish(@Nullable DJIError djiError) {
            if (!enableWaypointExecutionFinishListener) {
                return;
            }
            eventSender.processEvent(SDKEvent.WaypointMissionFinished, true, true);
//            getWaypointMissionOperator().removeListener(waypointMissionOperatorListener);
        }
    };

    public FlightControllerWrapper(@Nonnull ReactApplicationContext reactContext) {
        super(reactContext);
        this.eventSender = new EventSender(reactContext);
        this.reactContext = reactContext;
    }

    @ReactMethod
    public void startWaypointMission(ReadableMap parameters, Promise promise) {
        startMissionPromise = promise;
        setWaypointMissionOperatorListener();
        WaypointMissionTimelineElement waypointMissionTimelineElement = new WaypointMissionTimelineElement(parameters);
        DJIError missionParametersError = checkMissionParameters(waypointMissionTimelineElement);
        if (missionParametersError == null) {
            WaypointMission waypointMission = waypointMissionTimelineElement.build();
            if (loadMission(waypointMission)) {
                uploadMission();
            }
        } else {
            startMissionPromise.reject(new Throwable("Start Mission Error: " + missionParametersError.getDescription()));
        }

    }

    private void setWaypointMissionOperatorListener() {
        getWaypointMissionOperator().removeListener(waypointMissionOperatorListener);
        getWaypointMissionOperator().addListener(waypointMissionOperatorListener);
    }

    private DJIError checkMissionParameters(WaypointMissionTimelineElement waypointMissionTimelineElement) {
        return waypointMissionTimelineElement.checkParameters();
    }

    private Boolean loadMission(WaypointMission mission){
        DJIError error = getWaypointMissionOperator().loadMission(mission);
        return error == null;
    }

    private void uploadMission() {
        getWaypointMissionOperator().uploadMission(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError error) {
                if (error == null) {
                   // Log.d("REACT", "Upload Success");
                } else {
                    if (getWaypointMissionOperator().getCurrentState().equals(WaypointMissionState.READY_TO_UPLOAD)) {
                        getWaypointMissionOperator().retryUploadMission((new CommonCallbacks.CompletionCallback() {
                            @Override
                            public void onResult(DJIError error) {
                                if (error == null) {
                                    Log.d("REACT", "Upload Success");
                                } else {
                                    Log.e("REACT", "Upload Failed " + error.getDescription());
                                    startMissionPromise.reject(new Throwable("Upload Failed " + error.getDescription()));
                                }
                            }
                        }));
                    } else {
                        startMissionPromise.reject(new Throwable("Upload Failed"));
                        Log.e("REACT", "Not ready to upload");
                    }
                }
            }
        });
    }

    private WaypointMissionOperator getWaypointMissionOperator() {
        if(waypointMissionOperator == null) {
            waypointMissionOperator = DJISDKManager.getInstance().getMissionControl().getWaypointMissionOperator();
        }
        return waypointMissionOperator;    }

    @ReactMethod
    public void stopWaypointMission(final Promise promise) {
        getWaypointMissionOperator().stopMission(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                if (djiError == null) {
                    promise.resolve(null);
                } else {
                    promise.reject(new Throwable(djiError.getDescription()));
                }
            }
        });
    }

    @ReactMethod
    public void setWaypointmMissionAutoFlightSpeed(Float speed, final Promise promise) {
        getWaypointMissionOperator().setAutoFlightSpeed(speed, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                if (djiError == null) {
                    promise.resolve(null);
                } else {
                    promise.reject(new Throwable(djiError.getDescription()));
                }
            }
        });
    }

    @ReactMethod
    public void startVirtualStick(ReadableMap parameters, Promise promise) {
        virtualStickTimelineElement = new VirtualStickTimelineElement(parameters);
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
        getWaypointMissionOperator().setAutoFlightSpeed(speed, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                if (djiError == null) {
                    promise.resolve(null);
                } else {
                    promise.reject(new Throwable(djiError.getDescription()));
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
                promise.reject(new Throwable(djiError.getDescription()));
            }
        });
    }

    @Nonnull
    @Override
    public String getName() {
        return "FlightControllerWrapper";
    }
}
