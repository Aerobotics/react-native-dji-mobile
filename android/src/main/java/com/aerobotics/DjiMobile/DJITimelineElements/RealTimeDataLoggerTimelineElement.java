package com.aerobotics.DjiMobile.DJITimelineElements;

import com.aerobotics.DjiMobile.DJIRealTimeDataLogger;
import com.facebook.react.bridge.ReadableMap;

import dji.common.error.DJIError;
import dji.sdk.mission.timeline.actions.MissionAction;
import dji.sdk.sdkmanager.DJISDKManager;

public class RealTimeDataLoggerTimelineElement extends MissionAction {
    private DJIRealTimeDataLogger djiRealTimeDataLogger;
    private RealTimeDataLoggerTimelineElement self;

    private boolean stopRecordingFlightData = false;
    private String fileName;

    public RealTimeDataLoggerTimelineElement(DJIRealTimeDataLogger djiRealTimeDataLogger, ReadableMap parameters) {
        this.self = this;
        try {
            this.fileName = parameters.getString("fileName");
        } catch (Exception e) {

        }
        try {
            this.stopRecordingFlightData = parameters.getBoolean("stopRecordFlightData");
        } catch (Exception e) {

        }
        this.djiRealTimeDataLogger = djiRealTimeDataLogger;
    }

    @Override
    protected void startListen() {

    }

    @Override
    protected void stopListen() {

    }

    @Override
    public void run() {
        DJISDKManager.getInstance().getMissionControl().onStart(self);
        if (this.stopRecordingFlightData) {
            djiRealTimeDataLogger.stopLogging();
            DJISDKManager.getInstance().getMissionControl().onFinishWithError(self, null);
        } else {
            if (djiRealTimeDataLogger.isLogging()) {
                djiRealTimeDataLogger.stopLogging();
                DJISDKManager.getInstance().getMissionControl().onFinishWithError(self, null);
            }
            djiRealTimeDataLogger.startLogging(fileName);
            DJISDKManager.getInstance().getMissionControl().onFinishWithError(self, null);
        }
    }

    @Override
    public boolean isPausable() {
        return false;
    }

    @Override
    public void stop() {
        djiRealTimeDataLogger.stopLogging();
        DJISDKManager.getInstance().getMissionControl().onStopWithError(self, null);
    }

    @Override
    public DJIError checkValidity() {
        return null;
    }
}
