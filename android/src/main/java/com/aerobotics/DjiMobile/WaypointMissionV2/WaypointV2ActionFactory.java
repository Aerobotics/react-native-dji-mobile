package com.aerobotics.DjiMobile.WaypointMissionV2;

import java.util.ArrayList;
import java.util.List;

import dji.common.mission.waypointv2.Action.CameraFocusMode;
import dji.common.mission.waypointv2.Action.WaypointActuator;
import dji.common.mission.waypointv2.Action.WaypointTrigger;
import dji.common.mission.waypointv2.Action.WaypointV2Action;

public class WaypointV2ActionFactory {
    public static WaypointV2Action getAction(WaypointTrigger trigger, int actionId, WaypointActuator actuator) {
        //noinspection ConstantConditions
        return new WaypointV2Action.Builder()
                .setTrigger(trigger)
                .setActionID(actionId)
                .setActuator(actuator)
                .build();
    }

    public static WaypointV2Action getStopFlyAction(int pointIndex, int actionId) {
        return getAction(WaypointV2ActionUtils.getSimpleReachPointTrigger(pointIndex), actionId, WaypointV2ActionUtils.getStayActuator());
    }

    public static WaypointV2Action getSerialAction(int preActionId, int actionId, WaypointActuator actuator) {
        return getAction(WaypointV2ActionUtils.getSerialTrigger(preActionId), actionId, actuator);
    }

    public static WaypointV2Action getSerialDelayAction(int preActionId, int actionId, float delayTime, WaypointActuator actuator) {
        return getAction(WaypointV2ActionUtils.getSerialWaitTrigger(preActionId, delayTime), actionId, actuator);
    }

    /**
     * Manually focus the camera when a waypoint index is reached.
     *
     * @param waypointIndex waypoint index to perform the action
     * @param actionId      an action ID
     * @param gimbalPitch   pitch adjustment of the gimbal after the focus is complete
     * @return A list of actions which represent the manual focus action
     */
    public static List<WaypointV2Action> getActiveFocusAction(int waypointIndex, int actionId, float gimbalPitch) {
        final List<WaypointV2Action> actions = new ArrayList<>();
        int id = 100 * actionId;
        actions.add(getStopFlyAction(waypointIndex, id));
        actions.add(getSerialAction(id, ++id, WaypointV2ActionUtils.getGimbalActuator(-90, 0)));
        actions.add(getSerialDelayAction(id, ++id, 1f, WaypointV2ActionUtils.getCameraFocusModeActuator(CameraFocusMode.AUTO, 0)));
        actions.add(getSerialDelayAction(id, ++id, 0.5f, WaypointV2ActionUtils.getCameraRectFocusActuator(0)));
        actions.add(getSerialDelayAction(id, ++id, 1f, WaypointV2ActionUtils.getCameraFocusModeActuator(CameraFocusMode.MANUAL, 0)));
        actions.add(getSerialAction(id, ++id, WaypointV2ActionUtils.getGimbalActuator(gimbalPitch, 0)));
        actions.add(getSerialDelayAction(id, ++id, 2f, WaypointV2ActionUtils.getStayStartActuator()));
        return actions;
    }

    /**
     * Capture single images at a distance interval
     *
     * @param waypointIndex the waypoint index at which to start the action
     * @param actionId      an action ID
     * @param distanceM     the interval distance to capture images
     * @return A list of actions which represent the interval shoot photo action
     */
    public static List<WaypointV2Action> getCameraShootPhotoIntervalAction(int waypointIndex, int actionId, float distanceM) {
        int id = 100 * actionId;
        final List<WaypointV2Action> actions = new ArrayList<>();
        actions.add(getAction(WaypointV2ActionUtils.getSimpleReachPointTrigger(waypointIndex), id++, WaypointV2ActionUtils.getGimbalActuator(-90, 0)));
        actions.add(getAction(WaypointV2ActionUtils.getDistanceIntervalTrigger(waypointIndex, distanceM), id, WaypointV2ActionUtils.getCameraShootPhotoActuator(0)));
        return actions;
    }
}
