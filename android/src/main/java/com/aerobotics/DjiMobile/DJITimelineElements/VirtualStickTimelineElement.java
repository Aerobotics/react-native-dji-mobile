package com.aerobotics.DjiMobile.DJITimelineElements;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.os.Handler;
import android.util.Log;

import com.aerobotics.DjiMobile.DescentControllerLogger;
import com.aerobotics.DjiMobile.EventSender;
import com.aerobotics.DjiMobile.SDKEvent;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.WritableMap;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
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
import dji.keysdk.callback.SetCallback;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.mission.MissionControl;
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
  controlStickAdjustments,
  stopAltitude,
  altitudeStopDirection,
  logFilePath,
}

enum EndTrigger {
  timer,
  ultrasonic,
  altitude,
}

enum AltitudeStopDirection {
  below,
  above,
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

  private boolean ultrasonicDecreaseVerticalThrottleWithDistance = false;
  private Double verticalThrottleLimitPercent = 1.0;

  private Float stopAltitude;
  private AltitudeStopDirection altitudeStopDirection;

  private boolean doNotStopVirtualStickOnEnd = false;
  private boolean stopExistingVirtualStick = false;

  private VirtualStickTimelineElement self;

  private HashMap<VirtualStickControl, Double> virtualStickAdjustmentValues = new HashMap<VirtualStickControl, Double>() {{
    put(VirtualStickControl.pitch, 0.0);
    put(VirtualStickControl.roll, 0.0);
    put(VirtualStickControl.yaw, 0.0);
    put(VirtualStickControl.verticalThrottle, 0.0);
  }};

  // The base virtual stick values for the drone to fly at (without any adjustments)
  private HashMap<VirtualStickControl, Double> baseVirtualStickControlValues = new HashMap<VirtualStickControl, Double>() {{
    put(VirtualStickControl.pitch, 0.0);
    put(VirtualStickControl.roll, 0.0);
    put(VirtualStickControl.yaw, 0.0);
    put(VirtualStickControl.verticalThrottle, 0.0);
  }};

  private ArrayList<KeyListener> runningKeyListeners = new ArrayList<>();

  private TimerTask sendVirtualStickDataBlock;
  private TimerTask endTriggerTimerBlock;
  private EventSender eventSender;
  private boolean allowVerticalThrottleAdjustment = true;

  private Handler mHandler;
  private Runnable runnable;

  private long previousSampleTimestamp;
  private float currentUltrasonicHeight;
  private ReactContext reactContext;
  private String logFilePath;
  private float totalDescentTime = 0.0f;

  public VirtualStickTimelineElement(ReactContext reactContext, ReadableMap parameters) {

    self = this;
    this.reactContext = reactContext;
    try {
      stopExistingVirtualStick = parameters.getBoolean(Parameters.stopExistingVirtualStick.toString());
    } catch (Exception e) {}

    if (!stopExistingVirtualStick) {

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
        ultrasonicDecreaseVerticalThrottleWithDistance = parameters.getBoolean(Parameters.ultrasonicDecreaseVerticalThrottleWithDistance.toString());
      } catch (Exception e) {}

      try {
        stopAltitude = (float) parameters.getDouble(Parameters.stopAltitude.toString());
      } catch (Exception e) {}

      try {
        altitudeStopDirection = AltitudeStopDirection.valueOf(parameters.getString(Parameters.altitudeStopDirection.toString()));
      } catch (Exception e) {}

      try {
        logFilePath = parameters.getString(String.valueOf(Parameters.logFilePath));
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

  public void initializeVirtualStickEventSender(EventSender eventSender) {
    this.eventSender = eventSender;
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
          int stickValue;
          if (isHorizontal_final) {
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

  @Override
  public void run() {
    final MissionControl missionControl = DJISDKManager.getInstance().getMissionControl();
    if (stopExistingVirtualStick) {
      sendVirtualStickTimelineEvent("stopExistingVirtualStick");
      cleanUp(new CompletionCallback() {
        @Override
        public void complete(@Nullable DJIError djiError) {
          missionControl.onFinishWithError(self, djiError);
        }
      });
      return;
    }
    Aircraft product = ((Aircraft)DJISDKManager.getInstance().getProduct());
    if (product == null) {
      missionControl.onStartWithError(self, DJIError.COMMON_EXECUTION_FAILED);
      sendVirtualStickTimelineElementEvent("VirtualStick run error: Could not get product instance");
      return;
    }
    final FlightController flightController = product.getFlightController();
    if (flightController == null) {
      missionControl.onStartWithError(self, DJIError.COMMON_EXECUTION_FAILED);
      sendVirtualStickTimelineElementEvent("VirtualStick run error: Could not get flight controller instance");
      return;
    }
    // Using velocity and body for controlMode and coordinateSystem respectively means that a positive pitch corresponds to a roll to the right,
    // and a positive roll corresponds to a pitch forwards, THIS IS THE DJI SDK AND WE HAVE TO LIVE WITH IT
    flightController.setRollPitchCoordinateSystem(FlightCoordinateSystem.BODY);
    flightController.setRollPitchControlMode(RollPitchControlMode.VELOCITY);
    flightController.setYawControlMode(YawControlMode.ANGULAR_VELOCITY);
    flightController.setVerticalControlMode(VerticalControlMode.VELOCITY);

    isVirtualStickControlAvailable(new GetCallback() {
      @Override
      public void onSuccess(@NonNull Object enabled) {
        if ((Boolean) enabled) {
          startVirtualStickEvent(missionControl);
        } else {
          setVirtualStickEnabled(true, new SetCallback() {
            @Override
            public void onSuccess() {
              startVirtualStickEvent(missionControl);
            }
            @Override
            public void onFailure(@NonNull DJIError djiError) {
              missionControl.onStartWithError(self, djiError);
              sendVirtualStickTimelineElementEvent("setVirtualStickEnabled error: " + djiError.getDescription());
            }
          });
        }
      }

      @Override
      public void onFailure(@NonNull DJIError djiError) {
        missionControl.onStartWithError(self, djiError);
        sendVirtualStickTimelineElementEvent("isVirtualStickControlAvailable error: " + djiError.getDescription());
      }
    });
  }

  private void sendVirtualStickTimelineElementEvent(String message) {
    if (this.eventSender != null) {
      WritableMap progressMap = Arguments.createMap();
      progressMap.putString("error", message);
      this.eventSender.processEvent(SDKEvent.VirtualStickTimelineElementEvent, progressMap, true);
    }
  }

  private void sendVirtualStickTimelineEvent(String message) {
    if (this.eventSender != null) {
      WritableMap progressMap = Arguments.createMap();
      progressMap.putString("eventType", message);
      this.eventSender.processEvent(SDKEvent.VirtualStickTimelineElementEvent, progressMap, true);
    }
  }

  private void startVirtualStickEvent(MissionControl missionControl) {
    missionControl.onStart(self);
    if (endTrigger == EndTrigger.ultrasonic && ultrasonicEndDistance != null) {
      descendWithController();
    } else if (endTrigger == EndTrigger.timer && timerEndTime != null) {
      sendVirtualStickCommandAtFixedRate();
      stopAtEndTriggerTime();
    } else if (endTrigger == EndTrigger.altitude && stopAltitude != null && altitudeStopDirection != null) {
      sendVirtualStickCommandAtFixedRate();
      stopAtAltitude(stopAltitude, altitudeStopDirection);
    }
  }

  private void descendWithController() {
    isUltrasonicEnabled(new GetCallback() {
      @Override
      public void onSuccess(Object o) {
        startUltrasonicHeightListener();
        final DescentControllerLogger descentControllerLogger = new DescentControllerLogger();
        mHandler = new Handler();
        descentControllerLogger.logStringToFile(logFilePath, String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",
                "timeStamp",
                "timeElapsedInSecs",
                "sampleTimeInSecs",
                "setPoint",
                "latitude",
                "longitude",
                "altitude",
                "velocityX",
                "velocityY",
                "velocityZ",
                "ultrasonicHeight",
                "doesUltrasonicHaveError",
                "isUltrasonicBeingUsed",
                "heightError",
                "heightErrorInt",
                "heightErrorDer",
                "vxCmd",
                "vyCmd",
                "vzCmd",
                "computeTimeInSecs",
                "kP",
                "kD",
                "kI"));
        getUltrasonicHeight(new GetCallback() {
          @Override
          public void onSuccess(@NonNull Object value) {
            final PidController pidController = new PidController(0.5f, 0.0f, 0.0f, baseVirtualStickControlValues.get(VirtualStickControl.verticalThrottle).floatValue(), ultrasonicEndDistance - (Float) value);
            previousSampleTimestamp = System.nanoTime();
            currentUltrasonicHeight = (Float) value;
            runnable = new Runnable() {
              @Override
              public void run() {
                // get state
                long timestamp = System.nanoTime();
                float sampleTimeInSecs = (timestamp - previousSampleTimestamp)/1000000000.0f;
                float heightError = ultrasonicEndDistance - currentUltrasonicHeight;
                float throttleCommand = pidController.computeActuatorCommand(heightError, sampleTimeInSecs);
                // check for exit
                if (Math.abs(heightError) < 0.1) {
                  cleanUp(new CompletionCallback() {
                    @Override
                    public void complete(@Nullable DJIError djiError) {
                      DJISDKManager.getInstance().getMissionControl().onFinishWithError(self, djiError);
                    }
                  });
                  descentControllerLogger.stop();
                } else {
                  // update control
                  double pitch = baseVirtualStickControlValues.get(VirtualStickControl.pitch) + virtualStickAdjustmentValues.get(VirtualStickControl.pitch);
                  double roll = baseVirtualStickControlValues.get(VirtualStickControl.roll) + virtualStickAdjustmentValues.get(VirtualStickControl.roll);
                  double yaw = baseVirtualStickControlValues.get(VirtualStickControl.yaw) + virtualStickAdjustmentValues.get(VirtualStickControl.yaw);
                  FlightControlData flightControlData = new FlightControlData((float) roll, (float) pitch, (float) yaw, throttleCommand);
                  sendVirtualStickControlData(flightControlData);
                  totalDescentTime = totalDescentTime + (System.nanoTime() - previousSampleTimestamp);
                  float computeTime = System.nanoTime() - timestamp;
                  descentControllerLogger.logControlOutputToFile(logFilePath, sampleTimeInSecs, totalDescentTime, ultrasonicEndDistance, pidController, flightControlData, computeTime);
                  previousSampleTimestamp = timestamp;
                  mHandler.postDelayed(this, 50);
                }
              }
            };
            //start control loop
            mHandler.post(runnable);
          }

          @Override
          public void onFailure(@NonNull DJIError djiError) {
            cleanUp(new CompletionCallback() {
              @Override
              public void complete(@Nullable DJIError djiError) {
                DJISDKManager.getInstance().getMissionControl().onProgressWithError(self, djiError);
                sendVirtualStickTimelineElementEvent("isUltrasonicEnabled error: " + djiError.getDescription());
              }
            });
          }
        });
      }

      @Override
      public void onFailure(DJIError djiError) {
        cleanUp(new CompletionCallback() {
          @Override
          public void complete(@Nullable DJIError djiError) {
            DJISDKManager.getInstance().getMissionControl().onProgressWithError(self, djiError);
            sendVirtualStickTimelineElementEvent("getUltrasonicHeight error: " + djiError.getDescription());
          }
        });
      }
    });

  }

  private void logControlOutputToFile(String data) {
    String dataToWrite = data + "\n";
    if (logFilePath != null) {
      try {
        FileOutputStream stream = new FileOutputStream(logFilePath, true);
        stream.write(dataToWrite.getBytes());
        stream.close();
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private void startUltrasonicHeightListener() {
    final MissionControl missionControl = DJISDKManager.getInstance().getMissionControl();
    DJIKey ultrasonicHeightKey = FlightControllerKey.create(FlightControllerKey.ULTRASONIC_HEIGHT_IN_METERS);
    KeyListener ultrasonicHeightKeyListener = new KeyListener() {
      @Override
      public void onValueChange(@Nullable Object oldValue, @Nullable Object newValue) {
        if (newValue instanceof Float) {
          currentUltrasonicHeight = (Float) newValue;
        }
      }
    };

    DJISDKManager.getInstance().getKeyManager().addListener(ultrasonicHeightKey, ultrasonicHeightKeyListener);
    runningKeyListeners.add(ultrasonicHeightKeyListener);
  }
  private void stopAtEndTriggerTime() {
    endTriggerTimer = new Timer();
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
  }
  private void stopAtAltitude(final Float stopAltitude, final AltitudeStopDirection stopDirection) {
    final MissionControl missionControl = DJISDKManager.getInstance().getMissionControl();
    final DJIKey altitudeKey = FlightControllerKey.create(FlightControllerKey.ALTITUDE);
    final KeyListener altitudeKeyListener = new KeyListener() {
      @Override
      public void onValueChange(@Nullable Object oldValue, @Nullable Object newValue) {
        if (newValue instanceof Float) {
          float altitude = (float)newValue;
          if ( (stopDirection == AltitudeStopDirection.below && altitude <= stopAltitude) || (stopDirection == AltitudeStopDirection.above && altitude >= stopAltitude) ) {
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

    DJISDKManager.getInstance().getKeyManager().addListener(altitudeKey, altitudeKeyListener);
    runningKeyListeners.add(altitudeKeyListener);
  }

  private void sendVirtualStickCommandAtFixedRate() {
    sendVirtualStickDataTimer = new Timer();
    sendVirtualStickDataBlock = new TimerTask() {
      @Override
      public void run() {
        sendVirtualStickTimelineEvent("sentData");
        FlightControlData flightControlData = getBaseFlightControlCommand();
        self.sendVirtualStickControlData(flightControlData);
      }
    };

    sendVirtualStickDataTimer.scheduleAtFixedRate(sendVirtualStickDataBlock, 0, 50);
  }
  private FlightControlData getBaseFlightControlCommand() {
    HashMap<VirtualStickControl, Double> virtualStickData = new HashMap();

    for (VirtualStickControl virtualStickControl : VirtualStickControl.values()) {
      if (virtualStickControl == VirtualStickControl.verticalThrottle && !allowVerticalThrottleAdjustment) {
        virtualStickData.put(virtualStickControl, baseVirtualStickControlValues.get(virtualStickControl));
      } else {
        virtualStickData.put(virtualStickControl, baseVirtualStickControlValues.get(virtualStickControl) + virtualStickAdjustmentValues.get(virtualStickControl));
      }
    }
    return new FlightControlData(
            // In the coordinate system we use for the drone, roll and pitch are swapped
            virtualStickData.get(VirtualStickControl.roll).floatValue(),
            virtualStickData.get(VirtualStickControl.pitch).floatValue(),
            virtualStickData.get(VirtualStickControl.yaw).floatValue(),
            virtualStickData.get(VirtualStickControl.verticalThrottle).floatValue() * self.verticalThrottleLimitPercent.floatValue()
    );

  }
  private void sendVirtualStickControlData(FlightControlData flightControlData) {
    try {
      Aircraft product = ((Aircraft)DJISDKManager.getInstance().getProduct());
      final FlightController flightController = product.getFlightController();
      flightController.sendVirtualStickFlightControlData(flightControlData, null);
    } catch (NullPointerException e) {}
//    DJIKey sendVirtualStickFlightControlDataKey = FlightControllerKey.create(FlightControllerKey.SEND_VIRTUAL_STICK_FLIGHT_CONTROL_DATA);
//    DJISDKManager.getInstance().getKeyManager().performAction(sendVirtualStickFlightControlDataKey, new ActionCallback() {
//      @Override
//      public void onSuccess() {
//        Log.d("REACT", "sent vs cmd");
//      }
//
//      @Override
//      public void onFailure(@NonNull DJIError djiError) {
//        Log.d("REACT", "failed vs cmd " + djiError.getDescription());
//
//      }
//    }, flightControlData);
  }

  private void decreaseVerticalThrottleWithDistance() {
    final MissionControl missionControl = DJISDKManager.getInstance().getMissionControl();
    DJIKey ultrasonicHeightKey = FlightControllerKey.create(FlightControllerKey.ULTRASONIC_HEIGHT_IN_METERS);
    final Double xShift = 0.693147181;

    KeyListener ultrasonicHeightKeyListener = new KeyListener() {
      @Override
      public void onValueChange(@Nullable Object oldValue, @Nullable Object newValue) {
        if (newValue instanceof Float) {
          Float remainingDistance = (Float) newValue - ultrasonicEndDistance;
          // The function used is y = 1 - e^((x+1)/2 - xShift). This is a exponentially decreasing function, starting at 1 and decreasing to 0.5 at x=1 (When the drone is 1m from the required height)

          // TODO: (Adam) Descide what the best formula for this is
          // Double throttlePercentDecay = 1 - Math.pow(Math.E, (2 * (-remainingDistance + 1) - xShift));
          Double throttlePercentDecay = 1 - Math.pow(Math.E, (2 * (-remainingDistance + 1)));

          // Ensure that the vertical throttle is not decreased by more than 50%
          self.verticalThrottleLimitPercent = Math.max(throttlePercentDecay, .5);
        }
      }
    };

    DJISDKManager.getInstance().getKeyManager().addListener(ultrasonicHeightKey, ultrasonicHeightKeyListener);
    runningKeyListeners.add(ultrasonicHeightKeyListener);
  }

  private void isVirtualStickControlAvailable(final GetCallback getCallback) {
    DJIKey isVirtualStickControlAvailableKey = FlightControllerKey.create(FlightControllerKey.IS_VIRTUAL_STICK_CONTROL_MODE_AVAILABLE);
    DJISDKManager.getInstance().getKeyManager().getValue(isVirtualStickControlAvailableKey, getCallback);
  }
  private void setVirtualStickEnabled(boolean enabled, final SetCallback setCallback) {
    DJIKey setVirtualStickEnabled = FlightControllerKey.create(FlightControllerKey.VIRTUAL_STICK_CONTROL_MODE_ENABLED);
    DJISDKManager.getInstance().getKeyManager().setValue(setVirtualStickEnabled, enabled, setCallback);
  }
  private void isUltrasonicEnabled(final GetCallback getCallback) {
    DJIKey isUltrasonicBeingUsedKey = FlightControllerKey.create(FlightControllerKey.IS_ULTRASONIC_BEING_USED);
    DJISDKManager.getInstance().getKeyManager().getValue(isUltrasonicBeingUsedKey, getCallback);
  }
  private void getUltrasonicHeight(GetCallback getCallback) {
    DJIKey getUltrasonicHeightKey = FlightControllerKey.create(FlightControllerKey.ULTRASONIC_HEIGHT_IN_METERS);
    DJISDKManager.getInstance().getKeyManager().getValue(getUltrasonicHeightKey, getCallback);
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
    if (mHandler != null) {
      if (runnable != null)
        mHandler.removeCallbacks(runnable);
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

  private void cleanUp(final CompletionCallback completionCallback) {
    if (mHandler != null) {
      if (runnable != null) {
        mHandler.removeCallbacks(runnable);
      }
    }
    for (KeyListener runningListener : runningKeyListeners) {
      DJISDKManager.getInstance().getKeyManager().removeListener(runningListener);
    }
    final MissionControl missionControl = DJISDKManager.getInstance().getMissionControl();
    if (!stopExistingVirtualStick && doNotStopVirtualStickOnEnd) {
      completionCallback.complete(null);
    } else {
      stopVirtualStick(new CompletionCallback() {
        @Override
        public void complete(@Nullable DJIError djiError) {
          sendVirtualStickTimelineEvent("ended vs");
          completionCallback.complete(djiError);
          missionControl.onStopWithError(self, djiError);
        }
      });
    }
  }

  private void stopVirtualStick(final CompletionCallback completionCallback) {
    Aircraft aircraft = ((Aircraft)DJISDKManager.getInstance().getProduct());
    if (aircraft != null) {
      aircraft.getFlightController().setVirtualStickModeEnabled(false, new CommonCallbacks.CompletionCallback() {
        @Override
        public void onResult(DJIError djiError) {
          completionCallback.complete(djiError);
        }
      });
    }
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
