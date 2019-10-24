package com.aerobotics.DjiMobile.DJITimelineElements;

import androidx.annotation.Nullable;
import android.util.Log;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;

import dji.common.error.DJIError;
import dji.common.mission.waypoint.Waypoint;
import dji.common.mission.waypoint.WaypointAction;
import dji.common.mission.waypoint.WaypointActionType;
import dji.common.mission.waypoint.WaypointMission;
import dji.common.mission.waypoint.WaypointMissionGotoWaypointMode;
import dji.common.mission.waypoint.WaypointMissionHeadingMode;

public class WaypointMissionTimelineElement extends WaypointMission.Builder {

  public WaypointMissionTimelineElement(ReadableMap parameters) {

    if (!parameters.hasKey("autoFlightSpeed")) {
      // TODO: (Adam) Throw an error here!
      return;
    }

    if (!parameters.hasKey("maxFlightSpeed")) {
      // TODO: (Adam) Throw an error here!
      return;
    }

    if (!parameters.hasKey("waypoints")) {
      // TODO: (Adam) Throw an error here!
      return;
    }

    this.autoFlightSpeed = (float) parameters.getDouble("autoFlightSpeed");
    this.maxFlightSpeed = (float) parameters.getDouble("maxFlightSpeed");

    if (parameters.hasKey("headingMode")) {
      this.headingMode(WaypointMissionHeadingMode.valueOf(parameters.getString("headingMode")));
    }

    ReadableArray waypointsParameter = parameters.getArray("waypoints");
    for (int i = 0, n = waypointsParameter.size(); i < n; i++) {
      ReadableMap waypointParams = waypointsParameter.getMap(i);
      double longitude = waypointParams.getDouble("longitude");
      double latitude = waypointParams.getDouble("latitude");
      double altitude = waypointParams.getDouble("altitude");

      Waypoint waypoint = new Waypoint(
        latitude,
        longitude,
        (float) altitude
      );

      try {
        ReadableArray waypointActions = waypointParams.getArray("actions");
        for (int j = 0; j < waypointActions.size(); j++) {
          try {
            String actionType = waypointParams.getString("actionType");
            Integer actionParam = null; // If a correct value is not supplied for actions that require it, null will ensure it is invalid
            try {
              actionParam = waypointParams.getInt("actionParam");
            } catch (Exception e) {}
            waypoint.addAction(new WaypointAction(WaypointActionType.valueOf(actionType), actionParam));
          } catch (Exception e) {}
        }
      } catch (Exception e) {}

      this.addWaypoint(waypoint);
    }

    if (parameters.hasKey("goToWaypointMode")) {
      this.gotoFirstWaypointMode(WaypointMissionGotoWaypointMode.valueOf(parameters.getString("goToWaypointMode")));
    }
  }

  public @Nullable DJIError checkValidity() {
    return this.checkParameters();
  }
}
