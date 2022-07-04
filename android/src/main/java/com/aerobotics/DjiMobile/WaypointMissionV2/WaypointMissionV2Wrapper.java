package com.aerobotics.DjiMobile.WaypointMissionV2;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aerobotics.DjiMobile.EventSender;
import com.aerobotics.DjiMobile.SDKEvent;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import dji.common.error.DJIError;
import dji.common.error.DJIWaypointV2Error;
import dji.common.mission.waypoint.WaypointMissionState;
import dji.common.mission.waypointv2.Action.ActionDownloadEvent;
import dji.common.mission.waypointv2.Action.ActionExecutionEvent;
import dji.common.mission.waypointv2.Action.ActionState;
import dji.common.mission.waypointv2.Action.ActionUploadEvent;
import dji.common.mission.waypointv2.Action.ActionUploadProgress;
import dji.common.mission.waypointv2.Action.WaypointV2Action;
import dji.common.mission.waypointv2.WaypointV2ExecutionProgress;
import dji.common.mission.waypointv2.WaypointV2Mission;
import dji.common.mission.waypointv2.WaypointV2MissionDownloadEvent;
import dji.common.mission.waypointv2.WaypointV2MissionExecutionEvent;
import dji.common.mission.waypointv2.WaypointV2MissionState;
import dji.common.mission.waypointv2.WaypointV2MissionUploadEvent;
import dji.common.mission.waypointv2.WaypointV2UploadProgress;
import dji.common.util.CommonCallbacks;
import dji.keysdk.DJIKey;
import dji.keysdk.KeyManager;
import dji.keysdk.ProductKey;
import dji.keysdk.callback.GetCallback;
import dji.keysdk.callback.KeyListener;
import dji.sdk.base.BaseProduct;
import dji.sdk.camera.Camera;
import dji.sdk.mission.waypoint.WaypointV2ActionListener;
import dji.sdk.mission.waypoint.WaypointV2MissionOperator;
import dji.sdk.mission.waypoint.WaypointV2MissionOperatorListener;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKManager;


public class WaypointMissionV2Wrapper extends ReactContextBaseJavaModule {
    private final EventSender eventSender;

    private boolean enableWaypointExecutionFinishListener = false;
    private boolean enableWaypointExecutionUpdateListener = false;
    private boolean enableWaypointMissionStateListener = false;
    private boolean enableWaypointMissionUploadListener = false;

    private boolean startListenersOnProductConnection = false;

    private static WaypointV2MissionOperatorListener waypointMissionOperatorListener;
    private static WaypointV2ActionListener waypointActionListener;

    List<WaypointV2Action> waypointActionsList;

    public WaypointMissionV2Wrapper(@Nonnull ReactApplicationContext reactContext) {
        super(reactContext);
        this.eventSender = new EventSender(reactContext);
    }

    @ReactMethod
    public void uploadWaypointMission(ReadableMap parameters, Promise promise) {
        try {
            WaypointV2Mission waypointMission = new WaypointMissionV2Builder(parameters).build();
            waypointActionsList = new WaypointMissionV2ActionsBuilder(parameters).build();
            loadAndUploadMission(waypointMission, promise);
        } catch (Exception error) {
            promise.reject(new Throwable(error));
        }
    }

    @ReactMethod
    public void startWaypointMission(final Promise promise) {
        try {
            if (!getWaypointMissionOperator().getCurrentState().equals(WaypointV2MissionState.READY_TO_EXECUTE)) {
                throw new Exception("Incorrect mission state " + getWaypointMissionOperator().getCurrentState().name());
            }
            getWaypointMissionOperator().startMission(new CommonCallbacks.CompletionCallback<DJIWaypointV2Error>() {
                @Override
                public void onResult(DJIWaypointV2Error error) {
                    if (error == null) {
                        promise.resolve(null);
                        eventSender.processEvent(SDKEvent.WaypointMissionStarted, true, true);
                    } else {
                        promise.reject(new Throwable("startWaypointMission: " + error.getDescription()));
                        eventSender.processEvent(SDKEvent.WaypointMissionStarted, false, true);
                    }
                }
            });
        } catch (Exception exception) {
            promise.reject(new Throwable("Failed to start mission " + exception.getMessage()));
        }
    }

    public void startListenersOnProductConnection() {
        Log.d("REACT", "startListenersOnProductConnection!");
        if (startListenersOnProductConnection) {
            Log.d("REACT", "startListenersOnProductConnection true!");
            setWaypointMissionOperatorListener();
            setWaypointActionListener();
        }
    }

    private void setWaypointMissionOperatorListener() {
        try {
            if (waypointMissionOperatorListener != null) {
                return;
            }
            waypointMissionOperatorListener = new WaypointV2MissionOperatorListener() {
                @Override
                public void onDownloadUpdate(@NonNull WaypointV2MissionDownloadEvent event) {
                }

                @Override
                public void onUploadUpdate(@NonNull WaypointV2MissionUploadEvent event) {
                    try {
                        handlePossibleErrorUpdate(event.getError());
                        if (event.getProgress() != null) {
                            sendWaypointMissionUploadUpdate(event.getProgress());
                        }


                        // Since action state updates and mission state updates are merged, use the "action"
                        // state update to send the READY_TO_EXECUTE message
                        if (event.getCurrentState().equals(WaypointV2MissionState.READY_TO_EXECUTE)) {
                            Log.i("REACT", "Current state is READY_TO_EXECUTE, ignoring");
                            return;
                        }
                        /*
                         * The event state differs from the mission operator state at times.
                         * Choosing to use the mission operator state as the source of truth.
                         */
                        sendWaypointMissionStateUpdate(getWaypointMissionOperator().getCurrentState());
                    } catch (Exception exception) {
                        Log.w("REACT", exception);
                    }
                }

                @Override
                public void onExecutionUpdate(@NonNull WaypointV2MissionExecutionEvent event) {
                    try {
                        Log.i("REACT", "onExecutionUpdate: " + event.getCurrentState().name());
                        Log.i("REACT", "onExecutionUpdate2: " + getWaypointMissionOperator().getCurrentState().name());

                        handlePossibleErrorUpdate(event.getError());
                        sendWaypointMissionExecutionUpdate(event.getProgress());
                        /*
                         * The event state differs from the mission operator state at times.
                         * Choosing to use the mission operator state as the source of truth.
                         */
                        sendWaypointMissionStateUpdate(getWaypointMissionOperator().getCurrentState());
                    } catch (Exception exception) {
                        Log.w("REACT", exception);
                    }
                }

                @Override
                public void onExecutionStart() {
                    try {
                        sendWaypointMissionStateUpdate(getWaypointMissionOperator().getCurrentState());
                    } catch (Exception exception) {
                        Log.w("REACT", exception);
                    }
                }

                @Override
                public void onExecutionFinish(@Nullable DJIWaypointV2Error error) {
                    try {
                        Log.i("REACT", "onExecutionFinish2: " + getWaypointMissionOperator().getCurrentState().name());
                        handlePossibleErrorUpdate(error);
                        if (enableWaypointExecutionFinishListener) {
                            eventSender.processEvent(SDKEvent.WaypointMissionFinished, true, true);
                        }
                        sendWaypointMissionStateUpdate(getWaypointMissionOperator().getCurrentState());
                    } catch (Exception exception) {
                        Log.w("REACT", exception);
                    }
                }

                @Override
                public void onExecutionStopped() {
                }
            };
            getWaypointMissionOperator().addWaypointEventListener(waypointMissionOperatorListener);
            sendWaypointMissionStateUpdate(getWaypointMissionOperator().getCurrentState());
        } catch (Exception exception) {
            Log.w("REACT", exception);
            waypointMissionOperatorListener = null;
        }
    }

    private void setWaypointActionListener() {
        Log.d("REACT", "setWaypointActionListener");
        try {
            if (waypointActionListener != null) {
                return;
            }
            Log.d("REACT", "setWaypointActionListener start...");
            waypointActionListener = new WaypointV2ActionListener() {
                @Override
                public void onDownloadUpdate(@NonNull ActionDownloadEvent event) {
                }

                @Override
                public void onUploadUpdate(@NonNull ActionUploadEvent event) {
                    Log.d("REACT", "onUploadUpdate: event " + event.getCurrentState().name());
                    try {
                        handlePossibleErrorUpdate(event.getError());
                        if (event.getCurrentState().equals(ActionState.READY_TO_UPLOAD)) {
                            try {
                                uploadWaypointActions();
                            } catch (Exception error) {
                                sendWaypointMissionError(error.getMessage());
                            }
                        }
                        if (event.getProgress() != null) {
                            sendWaypointMissionActionUploadUpdate(event.getProgress());
                        }
                        if (event.getPreviousState() == ActionState.UPLOADING && event.getCurrentState() == ActionState.READY_TO_EXECUTE) {
                            Log.i("REACT", "Action state is READY_TO_EXECUTE, sending!");
                            sendWaypointMissionStateUpdate(WaypointV2MissionState.READY_TO_EXECUTE);
                        }
                        /*
                         * The event state differs from the mission operator state at times.
                         * Choosing to use the mission operator state as the source of truth.
                         */
                        sendWaypointMissionActionStateUpdate(getWaypointMissionOperator().getCurrentActionState());
                    } catch (
                            Exception exception) {
                        Log.w("REACT", exception);
                    }
                }

                @Override
                public void onExecutionUpdate(@NonNull ActionExecutionEvent event) {
                    try {
                        handlePossibleErrorUpdate(event.getError());
                        /*
                         * The event state differs from the mission operator state at times.
                         * Choosing to use the mission operator state as the source of truth.
                         */
                        sendWaypointMissionActionStateUpdate(getWaypointMissionOperator().getCurrentActionState());
                    } catch (Exception exception) {
                        Log.w("REACT", exception);
                    }
                }

                @Override
                public void onExecutionStart(int i) {
                }

                @Override
                public void onExecutionFinish(int i, @Nullable DJIWaypointV2Error error) {
                    handlePossibleErrorUpdate(error);
                }
            };
            getWaypointMissionOperator().addActionListener(waypointActionListener);
        } catch (
                Exception exception) {
            Log.w("REACT", exception);
            waypointActionListener = null;
        }
    }

    /**
     * Send an error event if an error has occurred, otherwise clear the error by sending a null
     * error.
     */
    private void handlePossibleErrorUpdate(@Nullable DJIWaypointV2Error error) {
        if (error != null) {
            sendWaypointMissionError(error);
        } else {
            // clear any existing error events
            sendNullWaypointMissionError();
        }
    }

    private void sendWaypointMissionError(@NonNull DJIError error) {
        eventSender.processEvent(SDKEvent.WaypointMissionError, error.getDescription(), true);
    }

    private void sendWaypointMissionError(String errorMsg) {
        eventSender.processEvent(SDKEvent.WaypointMissionError, errorMsg, true);
    }

    private void sendNullWaypointMissionError() {
        eventSender.processEvent(SDKEvent.WaypointMissionError, null, true);
    }

    private void loadAndUploadMission(WaypointV2Mission mission, final Promise promise) {
        try {
            getWaypointMissionOperator().loadMission(mission, new CommonCallbacks.CompletionCallback<DJIWaypointV2Error>() {
                @Override
                public void onResult(DJIWaypointV2Error error) {
                    if (error == null) {
                        uploadMission(promise);
                    } else {
                        promise.reject(new Throwable("loadAndUploadMission error: Upload Failed" + error.getDescription()));
                    }
                }
            });
        } catch (Exception exception) {
            promise.reject(new Throwable(exception));
        }
    }

    private void uploadMission(final Promise promise) {
        try {
            getWaypointMissionOperator().uploadMission(new CommonCallbacks.CompletionCallback<DJIWaypointV2Error>() {
                @Override
                public void onResult(DJIWaypointV2Error error) {
                    if (error == null) {
                        promise.resolve(null);
                    } else {
                        promise.reject(new Throwable("uploadMission error: Upload Failed" + error.getDescription()));
                    }
                }
            });
        } catch (Exception exception) {
            promise.reject(new Throwable(exception));
        }
    }

    private void uploadWaypointActions() throws Exception {
        Log.d("REACT", "Uploading actions...");
        getWaypointMissionOperator().uploadWaypointActions(waypointActionsList, new CommonCallbacks.CompletionCallback<DJIWaypointV2Error>() {
            @Override
            public void onResult(DJIWaypointV2Error error) {
                handlePossibleErrorUpdate(error);
            }
        });
    }

    private WaypointV2MissionOperator getWaypointMissionOperator() throws MissionOperatorNullException {
        WaypointV2MissionOperator waypointMissionOperator = DJISDKManager.getInstance().getMissionControl().getWaypointMissionV2Operator();
        if (waypointMissionOperator == null) {
            throw new MissionOperatorNullException("Waypoint mission operator is null");
        }
        return waypointMissionOperator;
    }

    @ReactMethod
    public void stopWaypointMission(final Promise promise) {
        try {
            getWaypointMissionOperator().stopMission(new CommonCallbacks.CompletionCallback<DJIWaypointV2Error>() {
                @Override
                public void onResult(DJIWaypointV2Error djiError) {
                    if (djiError == null) {
                        promise.resolve(null);
                    } else {
                        promise.reject(new Throwable("stopWaypointMission error: " + djiError.getDescription()));
                    }
                }
            });
        } catch (Exception exception) {
            promise.reject(new Throwable("stopWaypointMission error: " + exception.getMessage()));
        }
    }

    @ReactMethod
    public void setWaypointMissionAutoFlightSpeed(Float speed, final Promise promise) {
        try {
            getWaypointMissionOperator().setAutoFlightSpeed(speed, new CommonCallbacks.CompletionCallback<DJIWaypointV2Error>() {
                @Override
                public void onResult(DJIWaypointV2Error djiError) {
                    if (djiError == null) {
                        promise.resolve(null);
                    } else {
                        promise.reject(new Throwable("setWaypointMissionAutoFlightSpeed error: " + djiError.getDescription()));
                    }
                }
            });
        } catch (Exception exception) {
            promise.reject(new Throwable("setWaypointMissionAutoFlightSpeed error: " + exception.getMessage()));
        }
    }

    @ReactMethod
    public void stopAllWaypointMissionListeners(Promise promise) {
        try {
            enableWaypointExecutionUpdateListener = false;
            enableWaypointExecutionFinishListener = false;
            getWaypointMissionOperator().removeWaypointListener(waypointMissionOperatorListener);
            getWaypointMissionOperator().removeActionListener(waypointActionListener);
            promise.resolve(null);
        } catch (Exception exception) {
            promise.reject(new Throwable("stopAllWaypointMissionListeners error: " + exception.getMessage()));
        }
    }

    @ReactMethod
    public void startWaypointExecutionUpdateListener(Promise promise) {
        try {
            enableWaypointExecutionUpdateListener = true;
            startListenersOnProductConnection = true;
            startProductConnectionListener();
            promise.resolve(null);
        } catch (Exception exception) {
            promise.reject(new Throwable("startWaypointExecutionUpdateListener: " + exception.getMessage()));
        }
    }

    private void sendWaypointMissionExecutionUpdate(@Nullable WaypointV2ExecutionProgress progress) {
        if (enableWaypointExecutionUpdateListener && progress != null) {
            WritableMap progressMap = Arguments.createMap();
            progressMap.putInt("targetWaypointIndex", progress.getTargetWaypointIndex());
            progressMap.putBoolean("isWaypointReached", progress.isWaypointReached());
            progressMap.putString("executeState", progress.getExecuteState().name());

            Log.i("REACT", "Progress: " + progress.toString());

            try {
                WaypointV2Mission waypointMission = getWaypointMissionOperator().getLoadedMission();
                if (waypointMission != null) {
                    progressMap.putInt("totalWaypointCount", waypointMission.getWaypointCount());
                }
            } catch (Exception exception) {
                Log.w("React", "sendWaypointMissionExecutionUpdate: " + exception.getMessage());
            }

            eventSender.processEvent(SDKEvent.WaypointMissionExecutionProgress, progressMap, true);
        }
    }

    @ReactMethod
    public void startWaypointMissionStateListener(Promise promise) {
        try {
            enableWaypointMissionStateListener = true;
            startListenersOnProductConnection = true;
            startProductConnectionListener();
            promise.resolve(null);
        } catch (Exception exception) {
            promise.reject(new Throwable("startWaypointMissionStateListener: " + exception.getMessage()));
        }
    }

    @ReactMethod
    public void stopWaypointMissionStateListener(Promise promise) {
        enableWaypointMissionStateListener = false;
        promise.resolve(null);
    }

    private void sendWaypointMissionStateUpdate(WaypointV2MissionState missionState) {
        if (enableWaypointMissionStateListener) {
            // Merge V1 and V2 waypoint mission states
            if (missionState.equals(WaypointV2MissionState.INTERRUPTED)) {
                eventSender.processEvent(SDKEvent.WaypointMissionState, WaypointMissionState.EXECUTION_PAUSED.getName(), true);
            } else {
                eventSender.processEvent(SDKEvent.WaypointMissionState, missionState.name(), true);
            }
        }
    }

    private void sendWaypointMissionActionStateUpdate(ActionState state) {
        if (enableWaypointMissionStateListener) {
            eventSender.processEvent(SDKEvent.WaypointMissionActionState, state.name(), true);
        }
    }

    @ReactMethod
    public void startWaypointMissionUploadListener(Promise promise) {
        try {
            enableWaypointMissionUploadListener = true;
            startListenersOnProductConnection = true;
            startProductConnectionListener();
            promise.resolve(null);
        } catch (Exception exception) {
            promise.reject(new Throwable("startWaypointMissionUploadListener: " + exception.getMessage()));
        }
    }

    @ReactMethod
    public void stopWaypointMissionUploadListener(Promise promise) {
        enableWaypointExecutionUpdateListener = false;
        promise.resolve("stopWaypointMissionUploadListener");
    }

    private void sendWaypointMissionUploadUpdate(WaypointV2UploadProgress waypointUploadProgress) {
        if (enableWaypointMissionUploadListener && waypointUploadProgress != null) {
            WritableMap progressMap = Arguments.createMap();
            progressMap.putInt("uploadedWaypointIndex", waypointUploadProgress.getLastUploadedWaypointIndex());
            progressMap.putInt("totalWaypointCount", waypointUploadProgress.getTotalWaypointCount());
            progressMap.putBoolean("isSummaryUploaded", waypointUploadProgress.isSummaryUploaded());
            eventSender.processEvent(SDKEvent.WaypointMissionUploadProgress, progressMap, true);
        }
    }

    private void sendWaypointMissionActionUploadUpdate(@NonNull ActionUploadProgress progress) {
        if (enableWaypointMissionUploadListener) {
            WritableMap progressMap = Arguments.createMap();
            progressMap.putInt("uploadedWaypointIndex", progress.getLastUploadedWaypointIndex());
            progressMap.putInt("totalActionCount", progress.getTotalActionCount());
            eventSender.processEvent(SDKEvent.WaypointMissionActionUploadProgress, progressMap, true);
        }
    }


    // TODO (Nick A): this is duplicate code from the DJIMobile class, refactor
    private void startProductConnectionListener() {
        KeyManager keyManager = DJISDKManager.getInstance().getKeyManager();
        if (keyManager != null) {
            DJIKey productConnectedKey = ProductKey.create(ProductKey.CONNECTION);
            keyManager.getValue(productConnectedKey, new GetCallback() {
                @Override
                public void onSuccess(@NonNull Object o) {
                    Log.d("REACT", "startProductConnectionListener onSuccess!");
                    if (o instanceof Boolean && (Boolean) o) {
                        Boolean isProductConnected = (Boolean) o;
                        if (isProductConnected) {
                            startListenersOnProductConnection();
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull DJIError djiError) {
                }
            });
        }
        DJISDKManager.getInstance().getKeyManager().addListener((DJIKey) SDKEvent.ProductConnection.getKey(), new KeyListener() {
            @Override
            public void onValueChange(@Nullable Object oldValue, @Nullable Object newValue) {
                Log.d("REACT", "startProductConnectionListener onValueChange!");
                if (newValue != null && newValue instanceof Boolean) {
                    Boolean isProductConnected = (Boolean) newValue;
                    if (isProductConnected) {
                        startListenersOnProductConnection();
                    }
                }
            }
        });
    }

    @Nonnull
    @Override
    public String getName() {
        return "WaypointMissionV2Wrapper";
    }

    private static class MissionOperatorNullException extends Exception {
        public MissionOperatorNullException(String message) {
            super(message);
        }
    }
}



