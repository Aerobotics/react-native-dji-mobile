package com.aerobotics.DjiMobile;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;

import dji.common.error.DJIError;
import dji.common.gimbal.Attitude;
import dji.common.gimbal.Rotation;
import dji.common.gimbal.RotationMode;
import dji.common.util.CommonCallbacks;
import dji.keysdk.DJIKey;
import dji.keysdk.GimbalKey;
import dji.keysdk.callback.GetCallback;
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

  @ReactMethod
  public void getGimbalAttitude(final Promise promise) {
    DJIKey gimbalAttitudeInDegrees = GimbalKey.create(GimbalKey.ATTITUDE_IN_DEGREES);
    DJISDKManager.getInstance().getKeyManager().getValue(gimbalAttitudeInDegrees, new GetCallback() {
      @Override
      public void onSuccess(@NonNull Object value) {
        if (value instanceof Attitude) {
          Attitude attitude = (Attitude) value;
          float pitch = attitude.getPitch();
          float roll = attitude.getRoll();
          float yaw = attitude.getYaw();
          WritableMap params = Arguments.createMap();
          params.putDouble("pitch", pitch);
          params.putDouble("roll", roll);
          params.putDouble("yaw", yaw);
          promise.resolve(params);
        }
      }

      @Override
      public void onFailure(@NonNull DJIError djiError) {
        promise.reject(new Throwable(djiError.getDescription()));
      }
    });
  }

  @Override
  public String getName() {
    return "GimbalWrapper";
  }

}
