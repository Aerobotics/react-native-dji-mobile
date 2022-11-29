package com.aerobotics.DjiMobile.WaypointMissionV2;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;

import java.util.ArrayList;
import java.util.List;

import dji.common.mission.waypointv2.Action.WaypointV2Action;
import dji.sdk.base.BaseProduct;
import dji.sdk.camera.Camera;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKManager;

/**
 * Note that this is non-generic, and very much built for a waypoint mapping mission application.
 * This is largely due to the complexity of building these actions
 *
 * TODO: if necessary, make more generic/customizable
 */
public class WaypointMissionV2ActionsBuilder {
    List<WaypointV2Action> waypointActionList;

    public WaypointMissionV2ActionsBuilder(ReadableMap parameters) throws Exception {
        if (!parameters.hasKey("waypoints")) {
            throw new Exception(("Cannot build mission actions with null waypoints"));
        }

        // Each action collection is allocated 100 possible actions to avoid duplicated IDs
        int actionId = 0;
        waypointActionList = new ArrayList<>();
        ReadableArray waypointsParameter = parameters.getArray("waypoints");
        for (int i = 0; i < waypointsParameter.size(); i++) {
            ReadableMap waypointParams = waypointsParameter.getMap(i);

            /*
             * Focus camera at first waypoint for P1 camera
             * TODO: Do we want this? (this was straight from the DJI sample)
             */
            if (i == 0) {
                final BaseProduct product = DJISDKManager.getInstance().getProduct();
                if (product instanceof Aircraft) {
                    Camera camera = ((Aircraft) product).getCamera();
                    if (camera != null && camera.getDisplayName().equals("Zenmuse P1")) {
                        waypointActionList.addAll(WaypointV2ActionFactory.getActiveFocusAction(0, actionId, -90f));
                        actionId += 1;
                    }
                }
            }

            /*
             * Add take photo at distance interval action to all waypoints except the last.
             */
            if (i < waypointsParameter.size() - 1) {
                if (waypointParams.hasKey("shootPhotoDistanceInterval")) {
                    double shootPhotoDistanceInterval = waypointParams.getDouble("shootPhotoDistanceInterval");
                    waypointActionList.addAll(WaypointV2ActionFactory.getCameraShootPhotoIntervalAction(i, actionId, (float) shootPhotoDistanceInterval));
                    actionId += 1;
                }
            }
        }
    }

    public List<WaypointV2Action> build() {
        return waypointActionList;
    }
}
