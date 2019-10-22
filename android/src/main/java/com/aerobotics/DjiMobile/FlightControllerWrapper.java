package com.aerobotics.DjiMobile;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aerobotics.DjiMobile.DJITimelineElements.WaypointMissionTimelineElement;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;

import javax.annotation.Nonnull;

import dji.common.error.DJIError;
import dji.common.mission.waypoint.WaypointMission;
import dji.common.mission.waypoint.WaypointMissionDownloadEvent;
import dji.common.mission.waypoint.WaypointMissionExecutionEvent;
import dji.common.mission.waypoint.WaypointMissionState;
import dji.common.mission.waypoint.WaypointMissionUploadEvent;
import dji.common.util.CommonCallbacks;
import dji.sdk.mission.waypoint.WaypointMissionOperator;
import dji.sdk.mission.waypoint.WaypointMissionOperatorListener;
import dji.sdk.sdkmanager.DJISDKManager;

public class FlightControllerWrapper extends ReactContextBaseJavaModule {
    private EventSender eventSender;
    private WaypointMissionOperator waypointMissionOperator;

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
                        Log.d("REACT", "Waypoint Mission Start Success");
                    }
                });
            }
        }

        @Override
        public void onExecutionUpdate(@NonNull WaypointMissionExecutionEvent waypointMissionExecutionEvent) {

        }

        @Override
        public void onExecutionStart() {
            eventSender.processEvent(SDKEvent.WaypointMissionStarted, true, true);
        }

        @Override
        public void onExecutionFinish(@Nullable DJIError djiError) {
            eventSender.processEvent(SDKEvent.WaypointMissionFinished, true, true);
        }
    };

    public FlightControllerWrapper(@Nonnull ReactApplicationContext reactContext) {
        super(reactContext);
        this.eventSender = new EventSender(reactContext);
    }

    @ReactMethod
    public void startWaypointMission(ReadableMap parameters, Promise promise) {
        setWaypointMissionOperatorListener();
        WaypointMissionTimelineElement waypointMissionTimelineElement = new WaypointMissionTimelineElement(parameters);
        DJIError missionParametersError = checkMissionParameters(waypointMissionTimelineElement);
        if (missionParametersError == null) {
            WaypointMission waypointMission = waypointMissionTimelineElement.build();
            if (loadMission(waypointMission)) {
                uploadMission();
                promise.resolve(null);
            }
        } else {
            promise.reject(new Throwable("Start Mission Error: " + missionParametersError.getDescription()));
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
                    Log.d("REACT", "Upload Success");
                } else {
                    Log.d("REACT", "Retrying Upload");
                    getWaypointMissionOperator().retryUploadMission((new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError error) {
                            if (error == null) {
                                Log.d("REACT", "Upload Success");
                            } else {
                                Log.e("REACT", "Upload Failed");

                            }
                        }
                    }));
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

    @Nonnull
    @Override
    public String getName() {
        return "FlightControllerWrapper";
    }
}
