package com.aerobotics.DjiMobile;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;

import dji.common.error.DJIError;
import dji.common.gimbal.Rotation;
import dji.common.gimbal.RotationMode;
import dji.common.util.CommonCallbacks;
import dji.sdk.gimbal.Gimbal;
import dji.sdk.sdkmanager.DJISDKManager;

public class GimbalWrapper extends ReactContextBaseJavaModule {

  public GimbalWrapper(ReactApplicationContext reactContext) {
    super(reactContext);
  }

  @ReactMethod
  public void rotate(ReadableMap parameters, final Promise promise) {
    Gimbal gimbal = DJISDKManager.getInstance().getProduct().getGimbal();
    if (gimbal == null) {
      promise.reject(new Throwable("rotate error: Could not access gimbal"));
    }

    Rotation.Builder rotationBuilder = new Rotation.Builder();
    rotationBuilder.mode(RotationMode.ABSOLUTE_ANGLE);

    Double time = parameters.getDouble("time");
    if (time == null) {
      promise.reject(new Throwable("rotate error: Time value must be supplied"));
    }
    rotationBuilder.time(time);

    try {
      Float roll = (float)parameters.getDouble("roll");
      rotationBuilder.roll(roll);
    } catch (Exception e) {}
    try {
      Float pitch = (float)parameters.getDouble("pitch");
      rotationBuilder.pitch(pitch);
    } catch (Exception e) {}
    try {
      Float yaw = (float)parameters.getDouble("yaw");
      rotationBuilder.yaw(yaw);
    } catch (Exception e) {}

    gimbal.rotate(rotationBuilder.build(), new CommonCallbacks.CompletionCallback() {
      @Override
      public void onResult(DJIError djiError) {
        if (djiError != null) {
          promise.reject(new Throwable("rotate error: " + djiError.getDescription()));
        } else {
          promise.resolve("gimbal rotated");
        }
      }
    });

  }

  @Override
  public String getName() {
    return "GimbalWrapper";
  }

}
