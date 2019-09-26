package com.aerobotics.DjiMobile.DJITimelineElements;

import androidx.annotation.Nullable;
import android.util.Log;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;

import dji.common.error.DJIError;
import dji.common.mission.waypoint.Waypoint;
import dji.common.mission.waypoint.WaypointMission;
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
      ReadableMap waypoint = waypointsParameter.getMap(i);
      double longitude = waypoint.getDouble("longitude");
      double latitude = waypoint.getDouble("latitude");
      double altitude = waypoint.getDouble("altitude");
      this.addWaypoint(
        new Waypoint(
          latitude,
          longitude,
          (float) altitude
        )
      );
    }

  }

  public @Nullable DJIError checkValidity() {
    return this.checkParameters();
  }
}
