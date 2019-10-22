package com.aerobotics.DjiMobile;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;

import dji.common.error.DJIError;
import dji.common.gimbal.Rotation;
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

    Double time = parameters.getDouble("time");
    if (time == null) {
      promise.reject(new Throwable("rotate error: Time value must be supplied"));
    }

    Rotation.Builder rotationBuilder = new Rotation.Builder();

    Float roll = (float)parameters.getDouble("roll");
    Float pitch = (float)parameters.getDouble("pitch");
    Float yaw = (float)parameters.getDouble("yaw");

    if (roll != null) {
      rotationBuilder.roll(roll);
    }
    if (pitch != null) {
      rotationBuilder.pitch(pitch);
    }
    if (yaw != null) {
      rotationBuilder.yaw(yaw);
    }

    rotationBuilder.time(time);

    gimbal.rotate(rotationBuilder.build(), new CommonCallbacks.CompletionCallback() {
      @Override
      public void onResult(DJIError djiError) {
        if (djiError != null) {
          promise.resolve("gimbal rotated");
        } else {
          promise.reject(new Throwable("rotate error: " + djiError.getDescription()));
        }
      }
    });

  }

  @Override
  public String getName() {
    return "GimbalWrapper";
  }

}
