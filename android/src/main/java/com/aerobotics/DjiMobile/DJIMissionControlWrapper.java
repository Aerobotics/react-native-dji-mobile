
package com.aerobotics.DjiMobile;

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
import dji.sdk.sdkmanager.DJISDKManager;

//class ValidKeyInfo {
//  String keyParam;
//  Class keyClass;
//  Method createMethod;
//
//  public ValidKeyInfo(String keyParam, Class keyClass) {
//    this.keyParam = keyParam;
//    this.keyClass = keyClass;
//    try {
//      this.createMethod = keyClass.getMethod("create", String.class);
//    } catch (NoSuchMethodException e) {
//      e.printStackTrace();
//    }
//  }
//
//  public DJIKey createDJIKey() {
//    try {
////      Object KeyClass = this.keyClass.newInstance();
////      String args[] = {this.keyParam};
//      // As the .create() method is a static method, no object instance needs to be passed to .invoke(), hence the null value
//      DJIKey createdKey = (DJIKey)this.createMethod.invoke(null, this.keyParam);
//      return createdKey;
//    } catch (Exception e) {
//      Log.i("EXCEPTION", e.getLocalizedMessage());
//      return null;
//    }
//  }
//}

public class DJIMissionControlWrapper extends ReactContextBaseJavaModule {

  private final ReactApplicationContext reactContext;
  private int timelineElementIndex = 0;
  private HashMap<Integer, TimelineElement> timelineElements = new HashMap<>();
  private ArrayList<Integer> scheduledElementIndexOrder = new ArrayList<>();

  public DJIMissionControlWrapper(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
  }

  @ReactMethod
  public void createWaypointMission(ReadableArray coordinatesArray, ReadableMap parameters, Promise promise) {
    WaypointMission.Builder waypointMissionBuilder = new WaypointMission.Builder();

    Double autoFlightSpeed;
    Double maxFlightSpeed;

    try {
      autoFlightSpeed = parameters.getDouble("autoFlightSpeed");
    } catch (NoSuchKeyException e) {
      autoFlightSpeed = 2.0;
    }

    try {
      maxFlightSpeed = parameters.getDouble("maxFlightSpeed");
    } catch (NoSuchKeyException e) {
      maxFlightSpeed = autoFlightSpeed; // maxFlightSpeed must be >= autoFlightSpeed or else the mission will be invalid
    }

    waypointMissionBuilder.autoFlightSpeed(autoFlightSpeed.floatValue());
    waypointMissionBuilder.maxFlightSpeed(maxFlightSpeed.floatValue());

    for (int i = 0; i < coordinatesArray.size(); i++) {
      ReadableMap coordinate = coordinatesArray.getMap(i);
      double longitude = coordinate.getDouble("longitude");
      double latitude = coordinate.getDouble("latitude");
      double altitude = coordinate.getDouble("altitude");
      Waypoint waypoint = new Waypoint(latitude, longitude, (float)altitude);
      waypointMissionBuilder.addWaypoint(waypoint);
    }

    DJIError paramError = waypointMissionBuilder.checkParameters();
    if (paramError != null) {
      promise.reject(paramError.toString(), paramError.getDescription());
      return;
    }
    timelineElements.put(
      timelineElementIndex,
      TimelineMission.elementFromWaypointMission(
        waypointMissionBuilder.build()
      )
    );
    promise.resolve(timelineElementIndex);
    timelineElementIndex++;
    return;
  }

  @ReactMethod
  public void scheduleElement(Integer elementId, Promise promise) {
    TimelineElement timelineElement = timelineElements.get(elementId);
    if (timelineElement != null) {
      DJISDKManager.getInstance().getMissionControl().scheduleElement(timelineElement);
      scheduledElementIndexOrder.add(elementId);
    }
    promise.resolve(null);
  }

  @ReactMethod
  public void startTimeline(Promise promise) {
    MissionControl missionControl = DJISDKManager.getInstance().getMissionControl();

    missionControl.stopTimeline();
    missionControl.startTimeline();

    promise.resolve(null);
  }

  @ReactMethod
  public void unscheduleEverything(Promise promise) {
    DJISDKManager.getInstance().getMissionControl().unscheduleEverything();
    scheduledElementIndexOrder.clear();
    promise.resolve(null);

  }

  @ReactMethod
  public void startListener(Promise promise) {
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
          eventInfo.putInt("elementId", scheduledElementIndexOrder.get(timelineIndex));
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
  public void stopListener(Promise promise) {
    DJISDKManager.getInstance().getMissionControl().removeAllListeners();
    promise.resolve(null);
  }

  @Override
  public String getName() {
    return "DJIMissionControlWrapper";
  }
}
