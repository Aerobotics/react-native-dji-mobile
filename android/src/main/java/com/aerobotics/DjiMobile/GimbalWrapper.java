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
import dji.common.gimbal.GimbalMode;
import dji.common.gimbal.Rotation;
import dji.common.gimbal.RotationMode;
import dji.common.util.CommonCallbacks;
import dji.keysdk.DJIKey;
import dji.keysdk.GimbalKey;
import dji.keysdk.callback.ActionCallback;
import dji.keysdk.callback.GetCallback;
import dji.keysdk.callback.SetCallback;
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
      promise.reject(new Throwable("rotate gimbal error: Could not access gimbal"));
    }

    Rotation.Builder rotationBuilder = new Rotation.Builder();
    rotationBuilder.mode(RotationMode.ABSOLUTE_ANGLE);

    Double time = parameters.getDouble("time");
    if (time == null) {
      promise.reject(new Throwable("rotate gimbal error: Time value must be supplied"));
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
          promise.reject(new Throwable("rotate gimbal error: " + djiError.getDescription()));
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
        promise.reject(new Throwable("getGimbalAttitude error: " + djiError.getDescription()));
      }
    });
  }

  @ReactMethod
  public void getMode(final Promise promise) {
    DJIKey gimbalModeKey = GimbalKey.create(GimbalKey.MODE);
    DJISDKManager.getInstance().getKeyManager().getValue(gimbalModeKey, new GetCallback() {
      @Override
      public void onSuccess(Object o) {
        promise.resolve(((GimbalMode) o).name());

      }

      @Override
      public void onFailure(DJIError djiError) {
        promise.reject(new Throwable("getMode error: " + djiError.getDescription()));
      }
    });
  }

  @ReactMethod
  public void setMode(String mode, final Promise promise) {
    DJIKey gimbalModeKey = GimbalKey.create(GimbalKey.MODE);
    DJISDKManager.getInstance().getKeyManager().setValue(gimbalModeKey, GimbalMode.valueOf(mode), new SetCallback() {
      @Override
      public void onSuccess() {
        promise.resolve("setMode success");
      }

      @Override
      public void onFailure(DJIError djiError) {
        promise.reject(new Throwable("setMode error: " + djiError.getDescription()));
      }
    });
  }

  @ReactMethod
  public void resetPose(final Promise promise) {
    DJIKey gimbalResetKey = GimbalKey.create(GimbalKey.RESET_GIMBAL);
    DJISDKManager.getInstance().getKeyManager().performAction(gimbalResetKey, new ActionCallback() {
      @Override
      public void onSuccess() {
        promise.resolve(true);
      }

      @Override
      public void onFailure(@NonNull DJIError djiError) {
        promise.reject(new Throwable("resetPose error: " + djiError.getDescription()));
      }
    });
  }

  @Override
  public String getName() {
    return "GimbalWrapper";
  }

}
