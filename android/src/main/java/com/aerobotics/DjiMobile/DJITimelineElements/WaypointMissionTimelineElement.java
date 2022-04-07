package com.aerobotics.DjiMobile.DJITimelineElements;

import android.util.Log;

import androidx.annotation.Nullable;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;

import dji.common.error.DJIError;
import dji.common.mission.waypoint.Waypoint;
import dji.common.mission.waypoint.WaypointAction;
import dji.common.mission.waypoint.WaypointActionType;
import dji.common.mission.waypoint.WaypointMission;
import dji.common.mission.waypoint.WaypointMissionFinishedAction;
import dji.common.mission.waypoint.WaypointMissionFlightPathMode;
import dji.common.mission.waypoint.WaypointMissionGotoWaypointMode;
import dji.common.mission.waypoint.WaypointMissionHeadingMode;

import static dji.common.mission.waypoint.WaypointMissionFlightPathMode.CURVED;

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

    if (parameters.hasKey("goToWaypointMode")) {
      this.gotoFirstWaypointMode(WaypointMissionGotoWaypointMode.valueOf(parameters.getString("goToWaypointMode")));
    }

    if(parameters.hasKey("flightPathMode")) {
      this.flightPathMode(WaypointMissionFlightPathMode.valueOf(parameters.getString("flightPathMode")));
    }

    if(parameters.hasKey("finishedAction")) {
      this.finishedAction(WaypointMissionFinishedAction.valueOf(parameters.getString("finishedAction")));
    }

    ReadableArray waypointsParameter = parameters.getArray("waypoints");
    for (int i = 0, n = waypointsParameter.size(); i < n; i++) {
      ReadableMap waypointParams = waypointsParameter.getMap(i);
      double longitude = waypointParams.getDouble("longitude");
      double latitude = waypointParams.getDouble("latitude");
      double altitude = waypointParams.getDouble("altitude");

      Waypoint waypointObject = new Waypoint(
        latitude,
        longitude,
        (float) altitude
      );
      try {
        int heading = waypointParams.getInt("heading");
        waypointObject.heading = heading;
      } catch (Exception e) {}
      try {
        double speed = waypointParams.getDouble("speed");
        waypointObject.speed = (float) speed;
      } catch (Exception e) {}

      if (waypointParams.hasKey("cornerRadiusInMeters") && this.flightPathMode == CURVED) {
        // First and last waypoints should have a corner radius of 0.2 according to the DJI docs
        if (i == 0 || i == n - 1) {
          waypointObject.cornerRadiusInMeters = Waypoint.MIN_CORNER_RADIUS;
        } else {
          double cornerRadiusInMeters = waypointParams.getDouble("cornerRadiusInMeters");
          // Ensure corner radius >= min corner radius (0.2m)
          waypointObject.cornerRadiusInMeters = Math.max((float) cornerRadiusInMeters,  Waypoint.MIN_CORNER_RADIUS);
        }
      }

      if (waypointParams.hasKey("shootPhotoDistanceInterval")) {
        double shootPhotoDistanceInterval = waypointParams.getDouble("shootPhotoDistanceInterval");
        waypointObject.shootPhotoDistanceInterval = (float) shootPhotoDistanceInterval;
      }

      if (waypointParams.hasKey("shootPhotoTimeInterval")) {
        double shootPhotoTimeInterval = waypointParams.getDouble("shootPhotoTimeInterval");
        waypointObject.shootPhotoTimeInterval = (float) shootPhotoTimeInterval;
      }

      if (waypointParams.hasKey("actions")) {
        ReadableArray waypointActions = waypointParams.getArray("actions");
        for (int j = 0; j < waypointActions.size(); j++) {
          ReadableMap actionParams = waypointActions.getMap(j);
          try {
            String actionType = actionParams.getString("actionType");
            int actionParam = 0;
            if (actionParams.hasKey("actionParam")) {
              actionParam = actionParams.getInt("actionParam");
            }
            waypointObject.addAction(new WaypointAction(WaypointActionType.valueOf(actionType), actionParam));
          } catch (Exception e) {}
        }
      }

      this.addWaypoint(waypointObject);
    }
  }

  public @Nullable DJIError checkValidity() {
    return this.checkParameters();
  }
}
