package com.aerobotics.DjiMobile.DJITimelineElements;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import dji.common.Stick;
import dji.common.error.DJIError;
import dji.common.flightcontroller.virtualstick.FlightControlData;
import dji.common.flightcontroller.virtualstick.FlightCoordinateSystem;
import dji.common.flightcontroller.virtualstick.RollPitchControlMode;
import dji.common.flightcontroller.virtualstick.VerticalControlMode;
import dji.common.flightcontroller.virtualstick.YawControlMode;
import dji.common.util.CommonCallbacks;
import dji.keysdk.DJIKey;
import dji.keysdk.FlightControllerKey;
import dji.keysdk.RemoteControllerKey;
import dji.keysdk.callback.GetCallback;
import dji.keysdk.callback.KeyListener;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.mission.MissionControl;
import dji.sdk.mission.timeline.TimelineElement;
import dji.sdk.mission.timeline.actions.MissionAction;
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

enum EndTrigger {
  timer,
  ultrasonic,
}

interface CompletionCallback {
  void complete(@Nullable DJIError djiError);
}

public class VirtualStickTimelineElement extends MissionAction {

  private double CONTROLLER_STICK_LIMIT = 660.0;
  private double END_TRIGGER_TIMER_UPDATE_SECONDS = 0.1;

  private Timer sendVirtualStickDataTimer;
  private Timer endTriggerTimer;
  private Timer waitForControlsResetTimer;
  private Double secondsUntilEndTrigger;

  private EndTrigger endTrigger;
  private Double timerEndTime;

  private Float ultrasonicEndDistance;

  private boolean doNotStopVirtualStickOnEnd = false;
  private boolean waitForControlSticksReleaseOnEnd = false;
  private boolean stopExistingVirtualStick = false;

  private VirtualStickTimelineElement self;

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

  private TimerTask sendVirtualStickDataBlock;
  private TimerTask endTriggerTimerBlock;

  public VirtualStickTimelineElement(ReadableMap parameters) {

    self = this;

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
        endTrigger = EndTrigger.valueOf(parameters.getString(Parameters.endTrigger.toString()));
      } catch (Exception e) {}

      try {
        timerEndTime = parameters.getDouble(Parameters.timerEndTime.toString());
        secondsUntilEndTrigger = timerEndTime;
      } catch (Exception e) {}

      try {
        ultrasonicEndDistance = (float) parameters.getDouble(Parameters.ultrasonicEndDistance.toString());
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
              minSpeed = adjustmentStickParameters.getDouble("minSpeed");
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
    final MissionControl missionControl = DJISDKManager.getInstance().getMissionControl();
    if (stopExistingVirtualStick != true && doNotStopVirtualStickOnEnd == true) {
      completionCallback.complete(null);
    } else {
      stopVirtualStick(new CompletionCallback() {
        @Override
        public void complete(@Nullable DJIError djiError) {
          completionCallback.complete(djiError);
          missionControl.onStopWithError(self, djiError);
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

  private void isUltrasonicEnabled(final CompletionCallback completionCallback) {
    DJIKey isUltrasonicBeingUsedKey = FlightControllerKey.create(FlightControllerKey.IS_ULTRASONIC_BEING_USED);
    DJISDKManager.getInstance().getKeyManager().getValue(isUltrasonicBeingUsedKey, new GetCallback() {
      @Override
      public void onSuccess(@NonNull Object o) {
        completionCallback.complete(null);
      }

      @Override
      public void onFailure(@NonNull DJIError djiError) {
        completionCallback.complete(djiError);

      }
    });
  }

  private void stopAtUltrasonicHeight() {
    final MissionControl missionControl = DJISDKManager.getInstance().getMissionControl();
    DJIKey ultrasonicHeightKey = FlightControllerKey.create(FlightControllerKey.ULTRASONIC_HEIGHT_IN_METERS);
    KeyListener ultrasonicHeightKeyListener = new KeyListener() {
      @Override
      public void onValueChange(@Nullable Object oldValue, @Nullable Object newValue) {
        if (newValue instanceof Float) {
          if ((Float) newValue <= ultrasonicEndDistance) {
            sendVirtualStickDataTimer.cancel();
            cleanUp(new CompletionCallback() {
              @Override
              public void complete(@Nullable DJIError djiError) {
                missionControl.onFinishWithError(self, djiError);
              }
            });
          }
        }
      }
    };

    DJISDKManager.getInstance().getKeyManager().addListener(ultrasonicHeightKey, ultrasonicHeightKeyListener);
    runningKeyListeners.add(ultrasonicHeightKeyListener);
  }

  @Override
  public void run() {
    final FlightController flightController = ((Aircraft)DJISDKManager.getInstance().getProduct()).getFlightController();
    final MissionControl missionControl = DJISDKManager.getInstance().getMissionControl();
    if (flightController != null) {
        // Using velocity and body for controlMode and coordinateSystem respectively means that a positive pitch corresponds to a roll to the right,
        // and a positive roll corresponds to a pitch forwards, THIS IS THE DJI SDK AND WE HAVE TO LIVE WITH IT
        flightController.setRollPitchCoordinateSystem(FlightCoordinateSystem.BODY);
        flightController.setRollPitchControlMode(RollPitchControlMode.VELOCITY);
        flightController.setYawControlMode(YawControlMode.ANGULAR_VELOCITY);
        flightController.setVerticalControlMode(VerticalControlMode.VELOCITY);
        if (stopExistingVirtualStick == true) {
            cleanUp(new CompletionCallback() {
                @Override
                public void complete(@Nullable DJIError djiError) {
                    missionControl.onFinishWithError(self, djiError);
                }
            });

        } else {
            flightController.setVirtualStickModeEnabled(true, new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    if (djiError != null) {
                        missionControl.onStartWithError(self, djiError);
                    } else {
                        missionControl.onStart(self);
                        sendVirtualStickDataTimer = new Timer();
                        sendVirtualStickDataBlock = new TimerTask() {
                            @Override
                            public void run() {
                                try {
                                    double pitch = baseVirtualStickControlValues.get(VirtualStickControl.pitch) + virtualStickAdjustmentValues.get(VirtualStickControl.pitch);
                                    double roll = baseVirtualStickControlValues.get(VirtualStickControl.roll) + virtualStickAdjustmentValues.get(VirtualStickControl.roll);
                                    double yaw = baseVirtualStickControlValues.get(VirtualStickControl.yaw) + virtualStickAdjustmentValues.get(VirtualStickControl.yaw);
                                    double verticalThrottle = baseVirtualStickControlValues.get(VirtualStickControl.verticalThrottle) + virtualStickAdjustmentValues.get(VirtualStickControl.verticalThrottle);

                                    flightController.sendVirtualStickFlightControlData(new FlightControlData(
                                            // In the coordinate system we use for the drone, roll and pitch are swapped
                                            (float) roll,
                                            (float) pitch,
                                            (float) yaw,
                                            (float) verticalThrottle
                                    ), null);
                                } catch (NullPointerException e) {
                                    Log.e("REACT", "sendVirtualStickDataBlock Error");
                                }
                            }
                        };
                        sendVirtualStickDataTimer.scheduleAtFixedRate(sendVirtualStickDataBlock, 0, 50);
                        if (endTrigger == EndTrigger.timer && timerEndTime != null) {
                            endTriggerTimer = new Timer();
                            // NB if the period
                            endTriggerTimerBlock = new TimerTask() {
                                @Override
                                public void run() {

                                    secondsUntilEndTrigger -= END_TRIGGER_TIMER_UPDATE_SECONDS;

                                    if (secondsUntilEndTrigger <= 0) {
                                        sendVirtualStickDataTimer.cancel();
                                        endTriggerTimer.cancel();
                                        cleanUp(new CompletionCallback() {
                                            @Override
                                            public void complete(@Nullable DJIError djiError) {
                                                DJISDKManager.getInstance().getMissionControl().onFinishWithError(self, djiError);
                                            }
                                        });
                                    }

                                }
                            };
                            endTriggerTimer.scheduleAtFixedRate(endTriggerTimerBlock, 0, (long) (END_TRIGGER_TIMER_UPDATE_SECONDS * 1000));
                        } else if (endTrigger == EndTrigger.ultrasonic && ultrasonicEndDistance != null) {
                            isUltrasonicEnabled(new CompletionCallback() {
                                @Override
                                public void complete(@Nullable DJIError djiError) {
                                    if (djiError == null) {
                                        stopAtUltrasonicHeight();
                                    } else {
                                        sendVirtualStickDataTimer.cancel();
                                        cleanUp(new CompletionCallback() {
                                            @Override
                                            public void complete(@Nullable DJIError djiError) {
                                                DJISDKManager.getInstance().getMissionControl().onProgressWithError(self, djiError);
                                            }
                                        });
                                    }
                                }
                            });
                        }
                    }
                }
            });
        }
    } else {
        missionControl.onStartWithError(self, DJIError.COMMON_EXECUTION_FAILED);
    }

  }

  @Override
  public boolean isPausable() {
    return true;
  }

  @Override
  public void pause() {
    super.pause();
    sendVirtualStickDataTimer.cancel();
    if (endTriggerTimer != null) {
      endTriggerTimer.cancel();
    }
  }

  @Override
  public void resume() {
    super.resume();
    sendVirtualStickDataTimer = new Timer();
    sendVirtualStickDataBlock = new TimerTask() {
      @Override
      public void run() {
        FlightController flightController = ((Aircraft)DJISDKManager.getInstance().getProduct()).getFlightController();
        double pitch = baseVirtualStickControlValues.get(VirtualStickControl.pitch) + virtualStickAdjustmentValues.get(VirtualStickControl.pitch);
        double roll = baseVirtualStickControlValues.get(VirtualStickControl.roll) + virtualStickAdjustmentValues.get(VirtualStickControl.roll);
        double yaw = baseVirtualStickControlValues.get(VirtualStickControl.yaw) + virtualStickAdjustmentValues.get(VirtualStickControl.yaw);
        double verticalThrottle = baseVirtualStickControlValues.get(VirtualStickControl.verticalThrottle) + virtualStickAdjustmentValues.get(VirtualStickControl.verticalThrottle);

        flightController.sendVirtualStickFlightControlData(new FlightControlData(
                // In the coordinate system we use for the drone, roll and pitch are swapped
                (float)roll,
                (float)pitch,
                (float)yaw,
                (float)verticalThrottle
        ), null);
      }
    };
    sendVirtualStickDataTimer.scheduleAtFixedRate(sendVirtualStickDataBlock, 0, 50);
  }

  @Override
  public void stop() {
    if (sendVirtualStickDataTimer != null) {
      sendVirtualStickDataTimer.cancel();
    }
    if (endTriggerTimer != null) {
      endTriggerTimer.cancel();
    }
    cleanUp(new CompletionCallback() {
      @Override
      public void complete(@Nullable DJIError djiError) {
        if (djiError != null) {
          Log.i("REACT", djiError.getDescription());
        }
      }
    });
  }

  @Override
  public DJIError checkValidity() {
    return null;
  }

  @Override
  protected void startListen() {

  }

  @Override
  protected void stopListen() {

  }

  @Override
  public void finishRun(@Nullable DJIError djiError) {

  }
}
