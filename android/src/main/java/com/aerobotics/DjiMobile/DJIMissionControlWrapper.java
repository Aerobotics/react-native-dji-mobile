
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
    }
    promise.resolve(null);
  }

  @ReactMethod
  public void startTimeline(Promise promise) {
    MissionControl missionControl = DJISDKManager.getInstance().getMissionControl();

    missionControl.removeAllListeners();
    missionControl.addListener(new MissionControl.Listener() {
      @Override
      public void onEvent(@Nullable TimelineElement timelineElement, TimelineEvent timelineEvent, @Nullable DJIError djiError) {
        Log.i("MISSIONEVENT", timelineEvent.name());
        if (djiError != null) {
          Log.i("MISSIONEVENT", djiError.getDescription());
        }
      }
    });

    missionControl.stopTimeline();
    missionControl.startTimeline();

    promise.resolve(null);
  }

//  var timelineElementIndex = 0
//  var timelineElements: [Int: DJIMissionControlTimelineElement] = [:]
//
//  @objc(createWaypointMission:resolve:reject:)
//  func createWaypointMission(coordinates: NSArray, resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
//    let waypointMission = DJIMutableWaypointMission.init()
//    var waypoints: [DJIWaypoint] = []
//    for case let item as [String: Double] in coordinates {
//      let waypointCoordinate = CLLocationCoordinate2D.init(latitude: item["latitude"]!, longitude: item["longitude"]!)
//      let waypoint = DJIWaypoint.init(coordinate: waypointCoordinate)
//      waypoint.altitude = Float(item["altitude"]!)
//      waypoints.append(waypoint)
//    }
//
//    waypointMission.addWaypoints(waypoints)
//    let error = waypointMission.checkParameters()
//    if (error != nil) {
//      reject("Waypoint mission invalid", (error! as NSError).localizedDescription, nil)
//      return
//    } else {
//      timelineElements[timelineElementIndex] = waypointMission
//      resolve(timelineElementIndex)
//      timelineElementIndex += 1
//    }
//    //    }
//  }
//
//  @objc(destroyWaypointMission:resolve:reject:)
//  func destroyWaypointMission(missionId: NSNumber, resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
//    let removedMission = timelineElements.removeValue(forKey: missionId.intValue)
//    // TODO: (Adam) Should this reject if no valid mission was found?
//    resolve(nil)
//  }
//
//  @objc(scheduleElement:resolve:reject:)
//  func scheduleElement(elementId: NSNumber, resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
//    let element = timelineElements.first { $0.key == elementId.intValue }
//    if (element != nil) {
//      DJISDKManager.missionControl()?.scheduleElement(element!.value)
//    }
//    resolve(nil)
//  }
//
//  @objc(startTimeline:reject:)
//  func startTimeline(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
//
//    DJISDKManager.missionControl()?.stopTimeline()
//    DJISDKManager.missionControl()?.startTimeline()
//    DJISDKManager.missionControl()?.addListener(self, toTimelineProgressWith: { (event: DJIMissionControlTimelineEvent, element: DJIMissionControlTimelineElement?, error: Error?, info: Any?) in
//      print("MISSION EVENT")
//      if (error != nil) {
//        print(error!.localizedDescription)
//      }
//      print(event.rawValue)
//    })
//
//    resolve(nil)
//  }

  @Override
  public String getName() {
    return "DJIMissionControlWrapper";
  }
}