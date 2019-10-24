
package com.aerobotics.DjiMobile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;

import com.aerobotics.DjiMobile.DJITimelineElements.RealTimeDataLoggerTimelineElement;
import com.aerobotics.DjiMobile.DJITimelineElements.RunJSElementTimelineElement;
import com.aerobotics.DjiMobile.DJITimelineElements.VirtualStickTimelineElement;
import com.aerobotics.DjiMobile.DJITimelineElements.WaypointMissionTimelineElement;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.HashMap;
import java.util.Map;

import dji.common.error.DJIError;
import dji.common.gimbal.Attitude;
import dji.common.model.LocationCoordinate2D;
import dji.keysdk.DJIKey;
import dji.keysdk.FlightControllerKey;
import dji.keysdk.callback.ActionCallback;
import dji.sdk.mission.MissionControl;
import dji.sdk.mission.timeline.TimelineElement;
import dji.sdk.mission.timeline.TimelineEvent;
import dji.sdk.mission.timeline.TimelineMission;
import dji.sdk.mission.timeline.actions.GimbalAttitudeAction;
import dji.sdk.mission.timeline.actions.GoHomeAction;
import dji.sdk.mission.timeline.actions.GoToAction;
import dji.sdk.mission.timeline.actions.RecordVideoAction;
import dji.sdk.mission.timeline.actions.ShootPhotoAction;
import dji.sdk.mission.timeline.actions.HotpointAction;
import dji.common.mission.hotpoint.HotpointMission;
import dji.common.mission.hotpoint.HotpointHeading;
import dji.common.mission.hotpoint.HotpointStartPoint;
import dji.sdk.mission.timeline.actions.AircraftYawAction;
import dji.sdk.mission.timeline.actions.TakeOffAction;
import dji.sdk.sdkmanager.DJISDKManager;


enum TimelineElementType {
  WaypointMissionTimelineElement,
  GimbalAttitudeAction,
  ShootPhotoAction,
  RecordVideoAction,
  HotpointAction,
  AircraftYawAction,
  TakeOffAction,
  GoToAction,
  GoHomeAction,
  VirtualStickTimelineElement,
  RecordFlightData,
  RunJSElement,
}

public class DJIMissionControlWrapper extends ReactContextBaseJavaModule {

  private final ReactApplicationContext reactContext;
  private DJIRealTimeDataLogger djiRealTimeDataLogger;

  public DJIMissionControlWrapper(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
  }

  @ReactMethod
  public void scheduleElement(String timelineElementType, ReadableMap parameters, Promise promise) {
    MissionControl missionControl = DJISDKManager.getInstance().getMissionControl();

    TimelineElement newElement = null;

    switch (TimelineElementType.valueOf(timelineElementType)) {
      case WaypointMissionTimelineElement:
        WaypointMissionTimelineElement waypointMissionTimelineElement = new WaypointMissionTimelineElement(parameters);
        newElement = TimelineMission.elementFromWaypointMission(
          waypointMissionTimelineElement.build()
        );
        break;

      case GimbalAttitudeAction:
        newElement = buildGimbalAttitudeAction(parameters);
        break;

      case ShootPhotoAction:
        newElement = buildShootPhotoAction(parameters);
        break;

      case RecordVideoAction:
        newElement = buildRecordVideoAction(parameters);
        break;

      case HotpointAction:
        newElement = buildHotpointAction(parameters);
        break;

      case AircraftYawAction:
        newElement = buildAircraftYawAction(parameters);
        break;

      case TakeOffAction:
        newElement = buildTakeOffAction();
        break;

      case GoToAction:
        newElement = buildGoToAction(parameters);
        break;

      case GoHomeAction:
        newElement = buildGoHomeAction();
        break;

      case VirtualStickTimelineElement:
        newElement = buildVirtualStickTimelineElement(parameters);
        break;

        case RecordFlightData:
        newElement = buildRealTimeDataLoggerTimelineElement(parameters);
        break;

        case RunJSElement:
        newElement = buildRunJSElementTimelineElement(reactContext, parameters);
        break;

      default:
        break;
    }

    if (newElement != null) {
      Log.i("ReactNativeJS", "CHECKING VALIDITY");
      DJIError validError = newElement.checkValidity();
      if (validError != null) {
        Log.i("ReactNativeJS", validError.getDescription());
        promise.reject(validError.getDescription());
      }
      DJIError scheduleError = missionControl.scheduleElement(newElement);
      if (scheduleError != null) {
        Log.i("ReactNativeJS", scheduleError.getDescription());
        promise.reject(scheduleError.getDescription());
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
      gimbalAttitudeAction.setCompletionTime(completionTime);
    }

    return gimbalAttitudeAction;
  }

  public ShootPhotoAction buildShootPhotoAction(ReadableMap parameters) {
    Integer count = null;
    Integer interval = null;
    boolean stopShoot = false;

    try {
      count = parameters.getInt("count");
    } catch (Exception e) {}

    try {
      interval = parameters.getInt("interval");
    } catch (Exception e) {}

    try {
      stopShoot = parameters.getBoolean("stopShoot");
    } catch (Exception e) {}

    if (stopShoot == true) {
      return ShootPhotoAction.newStopIntervalPhotoAction();
    } else if (count != null && interval != null) {
      return ShootPhotoAction.newShootIntervalPhotoAction(count, interval);
    } else {
      return ShootPhotoAction.newShootSinglePhotoAction();
    }
  }

  public RecordVideoAction buildRecordVideoAction(ReadableMap parameters) {
    Integer duration = null;
    boolean stopRecord = false;

    try {
      duration = parameters.getInt("duration");
    } catch (Exception e) {}

    try {
      stopRecord = parameters.getBoolean("stopRecord");
    } catch (Exception e) {}

    if (stopRecord == true) {
      return RecordVideoAction.newStopRecordVideoAction();
    } else {
      if (duration != null) {
        return RecordVideoAction.newRecordVideoActionWithDuration(duration);
      } else {
        return RecordVideoAction.newStartRecordVideoAction();
      }
    }
  }

  public HotpointAction buildHotpointAction(ReadableMap parameters) {
    ReadableMap hotpoint = parameters.getMap("hotpoint");
    double angle = parameters.getDouble("angle");
    double longitude = hotpoint.getDouble("longitude");
    double latitude = hotpoint.getDouble("latitude");
    double altitude = hotpoint.getDouble("altitude");
    double radius = parameters.getDouble("radius");
    double angularVelocity = parameters.getDouble("angularVelocity");
    String startPointString = parameters.getString("startPoint");
    String headingString = parameters.getString("heading");
    boolean clockwise = parameters.getBoolean("clockwise");

    HotpointMission hotpointMission = new HotpointMission();
    hotpointMission.setHotpoint(new LocationCoordinate2D(latitude, longitude));
    hotpointMission.setAltitude((float)altitude);
    hotpointMission.setRadius(radius);
    hotpointMission.setAngularVelocity((float)angularVelocity);
    hotpointMission.setStartPoint(HotpointStartPoint.valueOf(startPointString));
    hotpointMission.setHeading(HotpointHeading.valueOf(headingString));
    hotpointMission.setClockwise(clockwise);

    return new HotpointAction(hotpointMission, (float)angle);
  }

  public AircraftYawAction buildAircraftYawAction(ReadableMap parameters) {
    double angle = parameters.getDouble("angle");

    if(parameters.hasKey("velocity")){
      double velocity = parameters.getDouble("velocity");
      return new AircraftYawAction((float)angle, (float)velocity);
    } else {
      boolean isAbsolute = parameters.getBoolean("isAbsolute");
      return new AircraftYawAction((float)angle, isAbsolute);
    }
  }

  public TakeOffAction buildTakeOffAction() {
    return new TakeOffAction();
  }

  public GoToAction buildGoToAction(ReadableMap parameters) {
    Double latitude = null;
    Double longitude = null;
    Float altitude = null;
    Float flightSpeed = null;

    try {
      ReadableMap coordinate = parameters.getMap("coordinate");
      latitude = coordinate.getDouble("latitude");
      longitude = coordinate.getDouble("longitude");
    } catch (Exception e) {}

    try {
      altitude = (float) parameters.getDouble("altitude");
    } catch (Exception e) {}

    try {
      flightSpeed = (float) parameters.getDouble("flightSpeed");
    } catch (Exception e) {}

    if (latitude != null && longitude != null && altitude != null) {
      LocationCoordinate2D coordinate2D = new LocationCoordinate2D(latitude, longitude);
      GoToAction goToAction = new GoToAction(coordinate2D, altitude);
      if (flightSpeed != null) {
        goToAction.setFlightSpeed(flightSpeed);
      }
      return goToAction;
    } else if (altitude != null) {
      GoToAction goToAction = new GoToAction(altitude);
      if (flightSpeed != null) {
        goToAction.setFlightSpeed(flightSpeed);
      }
      return goToAction;
    } else {
      return null;
    }
  }

  public GoHomeAction buildGoHomeAction() {
    return new GoHomeAction();
  }

  public VirtualStickTimelineElement buildVirtualStickTimelineElement(ReadableMap parameters) {
    return new VirtualStickTimelineElement(parameters);
  }

  public RealTimeDataLoggerTimelineElement buildRealTimeDataLoggerTimelineElement(ReadableMap parameters) {
    if (djiRealTimeDataLogger == null) {
      djiRealTimeDataLogger = new DJIRealTimeDataLogger(reactContext);
    }
    return new RealTimeDataLoggerTimelineElement(djiRealTimeDataLogger, parameters);
  }

  public RunJSElementTimelineElement buildRunJSElementTimelineElement(ReactApplicationContext reactContext, ReadableMap parameters) {
    return new RunJSElementTimelineElement(reactContext, parameters);
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
  public void startGoHome(final Promise promise) {
      DJIKey goGomeKey = FlightControllerKey.create(FlightControllerKey.START_GO_HOME);
      DJISDKManager.getInstance().getKeyManager().performAction(goGomeKey, new ActionCallback() {
          @Override
          public void onSuccess() {
              promise.resolve("DJI Mission Control: Returning home");
          }

          @Override
          public void onFailure(@NonNull DJIError djiError) {
              promise.reject(djiError.getDescription());
          }
      });
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
          String simpleName=null;
          if (timelineElement instanceof TimelineMission) {
            simpleName = ((TimelineMission) timelineElement).getMissionObject().getClass().getSimpleName();
          } else {
            simpleName = timelineElement.getClass().getSimpleName();
          }
          if (simpleName != null){
            eventInfo.putString("eventName", simpleName);
          }
        }
        eventInfo.putInt("eventType", timelineEvent.ordinal());
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
  public void setCurrentTimelineMarker(Integer currentTimelineMarker, Promise promise) {
    DJISDKManager.getInstance().getMissionControl().setCurrentTimelineMarker(currentTimelineMarker);
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
    for (TimelineElementType elementType : TimelineElementType.values()) {
      constants.put(elementType.toString(), elementType.toString());
    }
    return constants;
  }
}
