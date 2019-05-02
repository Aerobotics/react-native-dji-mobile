
package com.aerobotics.DjiMobile;

import com.aerobotics.DjiMobile.DJITimelineElements.WaypointMissionTimelineElement;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.NoSuchKeyException;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;


import android.support.annotation.Nullable;
import android.util.Log;

import java.lang.reflect.Method;
import java.security.Key;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import dji.common.error.DJIError;
import dji.common.error.DJISDKError;
import dji.common.flightcontroller.LocationCoordinate3D;
import dji.common.gimbal.Attitude;
import dji.common.mission.waypoint.Waypoint;
import dji.common.mission.waypoint.WaypointMission;
import dji.keysdk.DJIKey;
import dji.keysdk.FlightControllerKey;
import dji.keysdk.KeyManager;
import dji.keysdk.ProductKey;
import dji.keysdk.BatteryKey;
import dji.keysdk.callback.KeyListener;
import dji.sdk.base.BaseComponent;
import dji.sdk.base.BaseProduct;
import dji.sdk.mission.MissionControl;
import dji.sdk.mission.timeline.TimelineElement;
import dji.sdk.mission.timeline.TimelineEvent;
import dji.sdk.mission.timeline.TimelineMission;
import dji.sdk.mission.timeline.actions.GimbalAttitudeAction;
import dji.sdk.sdkmanager.DJISDKManager;


public class DJIMissionControlWrapper extends ReactContextBaseJavaModule {

  private final ReactApplicationContext reactContext;
//  private int timelineElementIndex = 0;
//  private HashMap<Integer, TimelineElement> timelineElements = new HashMap<>();
//  private ArrayList<Integer> scheduledElementIndexOrder = new ArrayList<>();

  private Map<String, String> timelineElements = new HashMap<String, String>() {{
    put("WaypointMissionTimelineElement", "WaypointMissionTimelineElement");
    put("GimbalAttitudeAction", "GimbalAttitudeAction");
    put("CapturePictureTimelineElement", "CapturePictureTimelineElement");
  }};


  public DJIMissionControlWrapper(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
  }

  @ReactMethod
  public void scheduleElement(String timelineElementType, ReadableMap parameters, Promise promise) {

    MissionControl missionControl = DJISDKManager.getInstance().getMissionControl();

    TimelineElement newElement = null;

    switch (timelineElementType) {
      case "WaypointMissionTimelineElement":
        WaypointMissionTimelineElement waypointMissionTimelineElement = new WaypointMissionTimelineElement(parameters);
        newElement = TimelineMission.elementFromWaypointMission(
          waypointMissionTimelineElement.build()
        );
        break;

      case "GimbalAttitudeAction":
        newElement = buildGimbalAttitudeAction(parameters);
        break;

      default:
        break;
    }

    if (newElement != null) {
      Log.i("ReactNativeJS", "CHECKING VALIDITY");
      DJIError validError = newElement.checkValidity();
      if (validError != null) {
        Log.i("ReactNativeJS", validError.getDescription());
      }
      DJIError scheduleError = missionControl.scheduleElement(newElement);
      if (scheduleError != null) {
        Log.i("ReactNativeJS", scheduleError.getDescription());
      }
    }

    promise.resolve("DJI Mission Control: Scheduled Element");
  }

  public GimbalAttitudeAction buildGimbalAttitudeAction(ReadableMap parameters) {
    float pitch = (float) parameters.getDouble("pitch");
    float roll = (float) parameters.getDouble("roll");
    float yaw = (float) parameters.getDouble("yaw");

    // TODO: adding a yaw & roll appears to break the action, is this because the gimbal mode must be changed?
    Attitude attitude = new Attitude(pitch, Attitude.NO_ROTATION, Attitude.NO_ROTATION);
    GimbalAttitudeAction gimbalAttitudeAction = new GimbalAttitudeAction(attitude);

    if (parameters.hasKey("completionTime")) {
      double completionTime = parameters.getDouble("completionTime");
      Log.i("ReactNativeJS", String.valueOf(completionTime));
      gimbalAttitudeAction.setCompletionTime(completionTime);
    }

    return gimbalAttitudeAction;
  }

  @ReactMethod
  public void startTimeline(Promise promise) {
    DJISDKManager.getInstance().getMissionControl().startTimeline();
    promise.resolve("DJI Mission Control: Start Timeline");
  }

  @ReactMethod
  public void stopTimeline(Promise promise) {
    DJISDKManager.getInstance().getMissionControl().stopTimeline();
    promise.resolve("DJI Mission Control: Stop Timeline");
  }

  @ReactMethod
  public void unscheduleEverything(Promise promise) {
    DJISDKManager.getInstance().getMissionControl().unscheduleEverything();
    promise.resolve("DJI Mission Control: Unschedule Everything");
  }

  @ReactMethod
  public void startTimelineListener(Promise promise) {
    final MissionControl missionControl = DJISDKManager.getInstance().getMissionControl();
    missionControl.removeAllListeners();
    missionControl.addListener(new MissionControl.Listener() {
      @Override
      public void onEvent(@Nullable TimelineElement timelineElement, TimelineEvent timelineEvent, @Nullable DJIError djiError) {
        WritableMap params = Arguments.createMap();
        WritableMap eventInfo = Arguments.createMap();

        int timelineIndex = missionControl.getCurrentTimelineMarker();
        if (timelineElement == null) { // This is a general timeline event (timeline start/stop, etc.)
          timelineIndex = -1;
          eventInfo.putInt("elementId", -1);
        } else {
//          eventInfo.putInt("elementId", scheduledElementIndexOrder.get(timelineIndex));
        }
        eventInfo.putString("eventType", timelineEvent.name());
        eventInfo.putInt("timelineIndex", timelineIndex);

        if (djiError != null) {
          eventInfo.putString("error", djiError.toString());
        }
        params.putMap("value", eventInfo);

        params.putString("type", "missionControlEvent");
        reactContext
          .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
          .emit("DJIEvent", params);
      }
    });

    promise.resolve(null);
  }

  @ReactMethod
  public void stopTimelineListener(Promise promise) {
    DJISDKManager.getInstance().getMissionControl().removeAllListeners();
    promise.resolve(null);
  }

  @ReactMethod
  public void checkWaypointMissionValidity(ReadableMap parameters, Promise promise) {
    WaypointMissionTimelineElement waypointMissionTimelineElement = new WaypointMissionTimelineElement(parameters);
    DJIError paramError = waypointMissionTimelineElement.checkParameters();
    if (paramError != null) {
      promise.reject(paramError.toString(), paramError.getDescription());
    } else {
      promise.resolve("Waypoint Mission Valid");
    }

  }

  @Override
  public String getName() {
    return "DJIMissionControlWrapper";
  }

  @Override
  public Map<String, Object> getConstants() {
    final Map<String, Object> constants = new HashMap<>();
    constants.putAll(timelineElements);
    return constants;
  }
}
