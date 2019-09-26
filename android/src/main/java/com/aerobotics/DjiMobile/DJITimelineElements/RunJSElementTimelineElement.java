package com.aerobotics.DjiMobile.DJITimelineElements;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import dji.common.error.DJIError;
import dji.sdk.mission.timeline.actions.MissionAction;
import dji.sdk.sdkmanager.DJISDKManager;

public class RunJSElementTimelineElement extends MissionAction {

    private RunJSElementTimelineElement self = this;
    private ReactApplicationContext reactContext;

    private Integer callbackFuncId;
    public RunJSElementTimelineElement(ReactApplicationContext reactContext, ReadableMap parameters) {
        this.reactContext = reactContext;
        this.callbackFuncId = parameters.getInt("callbackFuncId");
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
        WritableMap params = Arguments.createMap();
        WritableMap eventInfo = Arguments.createMap();
        eventInfo.putInt("callbackFuncId", callbackFuncId);
        params.putMap("value", eventInfo);
        params.putString("type", "RunJSElementEvent");
        reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit("DJIEvent", params);
        DJISDKManager.getInstance().getMissionControl().onFinishWithError(self, null);
    }

    @Override
    public boolean isPausable() {
        return false;
    }

    @Override
    public void stop() {
        DJISDKManager.getInstance().getMissionControl().onStopWithError(self, null);
    }

    @Override
    public DJIError checkValidity() {
        return null;
    }
}
