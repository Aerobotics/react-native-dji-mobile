package com.aerobotics.DjiMobile.WaypointMissionV2;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;

import java.util.ArrayList;
import java.util.List;

import dji.common.mission.waypointv2.WaypointV2;
import dji.common.mission.waypointv2.WaypointV2Mission;
import dji.common.mission.waypointv2.WaypointV2MissionTypes;
import dji.common.mission.waypointv2.WaypointV2MissionTypes.WaypointV2FlightPathMode;
import dji.common.mission.waypointv2.WaypointV2MissionTypes.WaypointV2HeadingMode;
import dji.common.model.LocationCoordinate2D;

import static dji.common.mission.waypoint.Waypoint.MIN_CORNER_RADIUS;

/**
 * Compose and build WaypointV2 list. Note that only parameters that have been tested have been
 * included in this wrapper, and thus, it is not yet exhaustive.
 */
public class WaypointMissionV2Builder extends WaypointV2Mission.Builder {
    public WaypointMissionV2Builder(ReadableMap parameters) throws Exception {
        if (!parameters.hasKey("autoFlightSpeed")) {
            throw new Exception(("Cannot build mission with null autoFlightSpeed"));
        }

        if (!parameters.hasKey("maxFlightSpeed")) {
            throw new Exception(("Cannot build mission with null maxFlightSpeed"));
        }

        if (!parameters.hasKey("waypoints")) {
            throw new Exception(("Cannot build mission with null waypoints"));
        }

        this.setAutoFlightSpeed((float) parameters.getDouble("autoFlightSpeed"));
        this.setMaxFlightSpeed((float) parameters.getDouble("maxFlightSpeed"));

        if (parameters.hasKey("goToWaypointMode")) {
            // Default is "SAFELY"
            this.setGotoFirstWaypointMode(WaypointV2MissionTypes.MissionGotoWaypointMode.valueOf(parameters.getString("goToWaypointMode")));
        }

        if (parameters.hasKey("finishedAction")) {
            this.setFinishedAction(WaypointV2MissionTypes.MissionFinishedAction.valueOf(parameters.getString("finishedAction")));
        }

        // In Waypoint Mission V2, this is defined on the waypoint level
        // Default is "CURVATURE_CONTINUOUS_PASSED" - overwrite default to "GOTO_POINT_STRAIGHT_LINE_AND_STOP"
        WaypointV2MissionTypes.WaypointV2FlightPathMode flightPathMode = WaypointV2FlightPathMode.GOTO_POINT_STRAIGHT_LINE_AND_STOP;
        if (parameters.hasKey("flightPathMode")) {
            flightPathMode = WaypointV2MissionTypes.WaypointV2FlightPathMode.valueOf(parameters.getString("flightPathMode"));
        }

        List<WaypointV2> waypointV2List = new ArrayList<>();
        ReadableArray waypointsParameter = parameters.getArray("waypoints");
        for (int i = 0, n = waypointsParameter.size(); i < n; i++) {
            ReadableMap waypointParams = waypointsParameter.getMap(i);
            double longitude = waypointParams.getDouble("longitude");
            double latitude = waypointParams.getDouble("latitude");
            double altitude = waypointParams.getDouble("altitude");

            WaypointV2.Builder waypointBuilder = new WaypointV2.Builder();

            LocationCoordinate2D coordinateLatLng = new LocationCoordinate2D(latitude, longitude);
            waypointBuilder
                    .setAltitude(altitude)
                    .setCoordinate(coordinateLatLng)
                    .setFlightPathMode(flightPathMode);

            if (waypointParams.hasKey("cornerRadiusInMeters") && flightPathMode.equals(WaypointV2FlightPathMode.COORDINATE_TURN)) {
                /*
                 * Gotcha: using 'GOTO_POINT_STRAIGHT_LINE_AND_STOP' for the first and last waypoints
                 * causes a strange curved flight path, so we have to use 'GOTO_FIRST_POINT_ALONG_STRAIGHT_LINE'
                 * and 'STRAIGHT_OUT'. However, these both need a damping distance > 0.2m, although this
                 * damping distance seems to make no difference to the flight path, so using the min (0.2m)
                 */
                if (i == 0) {
                    waypointBuilder
                            .setFlightPathMode(WaypointV2FlightPathMode.GOTO_FIRST_POINT_ALONG_STRAIGHT_LINE)
                            .setDampingDistance(MIN_CORNER_RADIUS);
                } else if (i == n - 1) {
                    waypointBuilder
                            .setFlightPathMode(WaypointV2FlightPathMode.STRAIGHT_OUT)
                            .setDampingDistance(MIN_CORNER_RADIUS);
                } else {
                    double cornerRadiusInMeters = waypointParams.getDouble("cornerRadiusInMeters");
                    // Ensure corner radius >= MIN_CORNER_RADIUS (0.2m)
                    waypointBuilder
                            .setFlightPathMode(WaypointV2FlightPathMode.COORDINATE_TURN)
                            .setDampingDistance(Math.max((float) cornerRadiusInMeters, MIN_CORNER_RADIUS));
                }
            }

            if (waypointParams.hasKey("headingMode")) {
                waypointBuilder.setHeadingMode(WaypointV2HeadingMode.valueOf(waypointParams.getString("headingMode")));
            }

            waypointV2List.add(waypointBuilder.build());
        }
        this.addwaypoints(waypointV2List);
    }
}
