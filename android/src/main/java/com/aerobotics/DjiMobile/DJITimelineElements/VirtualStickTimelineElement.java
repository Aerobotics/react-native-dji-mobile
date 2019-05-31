package com.aerobotics.DjiMobile.DJITimelineElements;

import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;

import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;

import java.security.Key;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;

import dji.common.Stick;
import dji.common.error.DJIError;
import dji.common.flightcontroller.virtualstick.FlightCoordinateSystem;
import dji.common.flightcontroller.virtualstick.RollPitchControlMode;
import dji.common.flightcontroller.virtualstick.YawControlMode;
import dji.common.util.CommonCallbacks;
import dji.keysdk.DJIKey;
import dji.keysdk.RemoteControllerKey;
import dji.keysdk.callback.KeyListener;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.mission.MissionControl;
import dji.sdk.mission.timeline.TimelineElement;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKManager;

enum VirtualStickControl {
  pitch,
  roll,
  verticalThrottle,
  yaw,
}

enum ControllerStickAxis {
  leftHorizontal,
  leftVertical,
  rightHorizontal,
  rightVertical,
}

enum Parameters {
  baseVirtualStickControlValues,
  doNotStopVirtualStickOnEnd,
  stopExistingVirtualStick,
  waitForControlSticksReleaseOnEnd,
  endTrigger,
  timerEndTime,
  ultrasonicEndDistance,
  ultrasonicDecreaseVerticalThrottleWithDistance,
  enableObstacleAvoidance,
  controlStickAdjustments,
}

interface CompletionCallback {
  void complete(@Nullable DJIError djiError);
}

public class VirtualStickTimelineElement extends TimelineElement {

  private Double CONTROLLER_STICK_LIMIT = 660.0;

  private Timer sendVirtualStickDataTimer;
  private Timer endTriggerTimer;
  private Timer waitForControlsResetTimer;
  private double secondsUntilEndTrigger;

  private boolean doNotStopVirtualStickOnEnd = false;
  private boolean waitForControlSticksReleaseOnEnd = false;
  private boolean stopExistingVirtualStick = false;

  private HashMap<VirtualStickControl, Double> virtualStickAdjustmentValues = new HashMap() {{
    put(VirtualStickControl.pitch, 0.0);
    put(VirtualStickControl.roll, 0.0);
    put(VirtualStickControl.yaw, 0.0);
    put(VirtualStickControl.verticalThrottle, 0.0);
  }};

  // The base virtual stick values for the drone to fly at (without any adjustments)
  private HashMap<VirtualStickControl, Double> baseVirtualStickControlValues = new HashMap() {{
    put(VirtualStickControl.pitch, 0.0);
    put(VirtualStickControl.roll, 0.0);
    put(VirtualStickControl.yaw, 0.0);
    put(VirtualStickControl.verticalThrottle, 0.0);

  }};

  private ArrayList<KeyListener> runningKeyListeners = new ArrayList<KeyListener>();

  public VirtualStickTimelineElement(ReadableMap parameters) {

    try {
      stopExistingVirtualStick = parameters.getBoolean(Parameters.stopExistingVirtualStick.toString());
    } catch (Exception e) {}

    if (stopExistingVirtualStick != true) {

      try {
        // The next line will throw an exception if the value does not exist in parameters.
        ReadableMap baseVirtualStickControlValuesInput = parameters.getMap(Parameters.baseVirtualStickControlValues.toString());

        ReadableMapKeySetIterator iterator = baseVirtualStickControlValuesInput.keySetIterator();
        while(iterator.hasNextKey()) {
          String baseVirtualStickControlValueKey = iterator.nextKey();
          Double baseVirtualStickControlValue = baseVirtualStickControlValuesInput.getDouble(baseVirtualStickControlValueKey);
          baseVirtualStickControlValues.put(VirtualStickControl.valueOf(baseVirtualStickControlValueKey), baseVirtualStickControlValue);
        }
      } catch (Exception e) {}

      try {
        doNotStopVirtualStickOnEnd = parameters.getBoolean(Parameters.doNotStopVirtualStickOnEnd.toString());
      } catch (Exception e) {}

      try {
        waitForControlSticksReleaseOnEnd = parameters.getBoolean(Parameters.waitForControlSticksReleaseOnEnd.toString());
      } catch (Exception e) {}

      try {
        ReadableMap controlStickAdjustments = parameters.getMap(Parameters.controlStickAdjustments.toString());

        for (VirtualStickControl virtualStickControl : VirtualStickControl.values()) {
          try {
            // If the parameters for the stick type doesn't exist, this line will throw an exception, skipping this adjustment stick type
            ReadableMap adjustmentStickParameters = controlStickAdjustments.getMap(virtualStickControl.toString());
            ControllerStickAxis controllerStickAxis = ControllerStickAxis.valueOf(adjustmentStickParameters.getString("axis"));
            Double minSpeed;
            Double maxSpeed;

            maxSpeed = adjustmentStickParameters.getDouble("maxSpeed");
            if (virtualStickControl == VirtualStickControl.yaw) { // For yaw the max (cw & ccw) rotation speed is defined, instead of a min max value
              minSpeed = -maxSpeed; // Opposite direction rotation
            } else {
              minSpeed = adjustmentStickParameters.getDouble("maxSpeed");
            }

            implementControlStickAdjustment(virtualStickControl, controllerStickAxis, minSpeed, maxSpeed);

          } catch (Exception e) {}
        }
      } catch (Exception e) {}

    }

  }

  private void implementControlStickAdjustment(final VirtualStickControl virtualStickControl, ControllerStickAxis controllerStickAxis, final Double minSpeed, final Double maxSpeed) {

    DJIKey controllerStickKey = null;
    boolean isHorizontal = false;

    switch (controllerStickAxis) {
      case leftHorizontal:
        isHorizontal = true;
      case leftVertical:
        controllerStickKey = RemoteControllerKey.create(RemoteControllerKey.LEFT_STICK_VALUE);
        break;

      case rightHorizontal:
        isHorizontal = true;
      case rightVertical:
        controllerStickKey = RemoteControllerKey.create(RemoteControllerKey.RIGHT_STICK_VALUE);
        break;
    }

    final boolean isHorizontal_final = isHorizontal;
    KeyListener controllerStickKeyListener = new KeyListener() {
      @Override
      public void onValueChange(@Nullable Object oldValue, @Nullable Object newValue) {
        if (newValue instanceof Stick) {
          Integer stickValue;
          if (isHorizontal_final == true) {
            stickValue = ((Stick) newValue).getHorizontalPosition();
          } else {
            stickValue = ((Stick) newValue).getVerticalPosition();
          }

          Double rescaledStickValue = rescaleControllerStickValue(stickValue, minSpeed, maxSpeed);
          virtualStickAdjustmentValues.put(virtualStickControl, rescaledStickValue);
        }
      }
    };

    DJISDKManager.getInstance().getKeyManager().addListener(controllerStickKey, controllerStickKeyListener);
    runningKeyListeners.add(controllerStickKeyListener);

  }

  private Double rescaleControllerStickValue(Integer controllerStickValue, Double minSpeed, Double maxSpeed) {
    if (controllerStickValue < 0) {
      return controllerStickValue / (-CONTROLLER_STICK_LIMIT / minSpeed);
    } else {
     return controllerStickValue / (CONTROLLER_STICK_LIMIT / maxSpeed);
    }
  }

  private void cleanUp(final CompletionCallback completionCallback) {
    for (KeyListener runningListener : runningKeyListeners) {
      DJISDKManager.getInstance().getKeyManager().removeListener(runningListener);
    }

    if (doNotStopVirtualStickOnEnd == true) {
      completionCallback.complete(null);
    } else {
      stopVirtualStick(new CompletionCallback() {
        @Override
        public void complete(@Nullable DJIError djiError) {
          completionCallback.complete(djiError);
        }
      });
    }
  }

  private void stopVirtualStick(final CompletionCallback completionCallback) {
    ((Aircraft)DJISDKManager.getInstance().getProduct()).getFlightController().setVirtualStickModeEnabled(false, new CommonCallbacks.CompletionCallback() {
      @Override
      public void onResult(DJIError djiError) {
        completionCallback.complete(djiError);
      }
    });
  }

  @Override
  public void run() {
    FlightController flightController = ((Aircraft)DJISDKManager.getInstance().getProduct()).getFlightController();
    MissionControl missionControl = DJISDKManager.getInstance().getMissionControl();

    // Using velocity and body for controlMode and coordinateSystem respectively means that a positive pitch corresponds to a roll to the right,
    // and a positive roll corresponds to a pitch forwards, THIS IS THE DJI SDK AND WE HAVE TO LIVE WITH IT
    flightController.setRollPitchControlMode(RollPitchControlMode.VELOCITY);
    flightController.setYawControlMode(YawControlMode.ANGULAR_VELOCITY);
    flightController.setRollPitchCoordinateSystem(FlightCoordinateSystem.BODY);

    

  }

  @Override
  public boolean isPausable() {
    return true;
  }

  @Override
  public void pause() {
    super.pause();
  }

  @Override
  public void resume() {
    super.resume();
  }

  @Override
  public void stop() {

  }

  @Override
  public DJIError checkValidity() {
    return null;
  }

  @Override
  public void finishRun(@Nullable DJIError djiError) {

  }
}
