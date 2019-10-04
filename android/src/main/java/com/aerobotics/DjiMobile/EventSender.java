package com.aerobotics.DjiMobile;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;


public class EventSender {

    private int eventSendFrequencyMilliSecs = 500;
    private Timer eventSendLimiterTimer;
    private ReactContext reactContext;
    private ConcurrentHashMap<String, Object> queuedEvents = new ConcurrentHashMap<>();
    private TimerTask eventSendLimiterTimerTask = new TimerTask() {
        @Override
        public void run() {
            sendQueuedEvents();
        }
    };
    EventSender(ReactContext reactContext) {
        this.reactContext = reactContext;
        if (eventSendLimiterTimer == null) {
            eventSendLimiterTimer = new Timer();
            eventSendLimiterTimer.scheduleAtFixedRate(this.eventSendLimiterTimerTask, 0, this.eventSendFrequencyMilliSecs);
        }
    }

    private void sendQueuedEvents() {
        ConcurrentHashMap<String, Object> queuedEventsSnapshot = new ConcurrentHashMap<>(this.queuedEvents);
        this.queuedEvents.clear();
        if (!queuedEventsSnapshot.isEmpty()) {
            for (Map.Entry<String, Object> entry: queuedEventsSnapshot.entrySet()) {
                WritableMap params = buildEventParams(entry.getValue());
                params.putString("type", entry.getKey());
                this.sendReactEvent(params);
            }
        }
    }

    private void sendReactEvent(WritableMap params) {
        reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit("DJIEvent", params);
    }

    public void processEvent(SDKEvent SDKEvent, Object value, Boolean realtime) {
        if (realtime) {
            WritableMap params = buildEventParams(value);
            params.putString("type", SDKEvent.toString());
            reactContext
                    .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                    .emit("DJIEvent", params);
        } else {
            this.queueEvent(SDKEvent, value);
        }
    }

    private void queueEvent(SDKEvent SDKEvent, Object value) {
        String type = SDKEvent.toString();
        this.queuedEvents.put(type, value);
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

    public void setNewEventSendFrequency(int newEventSendFrequency) {
        this.eventSendFrequencyMilliSecs = newEventSendFrequency;
        if (this.eventSendLimiterTimer != null) {
            this.eventSendLimiterTimer.cancel();
        }
        this.eventSendLimiterTimer = new Timer();
        eventSendLimiterTimer.scheduleAtFixedRate(this.eventSendLimiterTimerTask, 0, this.eventSendFrequencyMilliSecs);
    }
}
