package com.aerobotics.DjiMobile;

import android.support.annotation.NonNull;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;

import dji.common.camera.SettingsDefinitions;
import dji.common.error.DJIError;
import dji.keysdk.CameraKey;
import dji.keysdk.DJIKey;
import dji.keysdk.callback.SetCallback;
import dji.sdk.sdkmanager.DJISDKManager;



public class CameraControl extends ReactContextBaseJavaModule {
    public CameraControl(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return "CameraControl";
    }

    @ReactMethod
    public void setPhotoAspectRatio(String photoAspectRatio, final Promise promise) {
        DJIKey photoAspectRatioKey = CameraKey.create(CameraKey.PHOTO_ASPECT_RATIO);
        DJISDKManager.getInstance().getKeyManager().setValue(photoAspectRatioKey, SettingsDefinitions.PhotoAspectRatio.valueOf(photoAspectRatio), new SetCallback() {
            @Override
            public void onSuccess() {
                promise.resolve("CameraControl: Photo aspect ratio set successfully");
            }

            @Override
            public void onFailure(@NonNull DJIError djiError) {
                promise.reject("CameraControl: Failed to set photo aspect ratio");
            }
        });
    }

    @ReactMethod
    public void setWhiteBalance(String whiteBalancePreset, final Promise promise) {
        DJIKey whiteBalanceKey = CameraKey.create(CameraKey.WHITE_BALANCE);
        DJISDKManager.getInstance().getKeyManager().setValue(whiteBalanceKey, SettingsDefinitions.WhiteBalancePreset.valueOf(whiteBalancePreset), new SetCallback() {
            @Override
            public void onSuccess() {
                promise.resolve("CameraControl: White balance set successfully");
            }

            @Override
            public void onFailure(@NonNull DJIError djiError) {
                promise.reject("CameraControl: Failed to set white balance");
            }
        });
    }

    @ReactMethod
    public void setExposureMode(String exposureMode, final Promise promise) {
        DJIKey exposureModeKey = CameraKey.create(CameraKey.EXPOSURE_MODE);
        DJISDKManager.getInstance().getKeyManager().setValue(exposureModeKey, SettingsDefinitions.ExposureMode.valueOf(exposureMode), new SetCallback() {
            @Override
            public void onSuccess() {
                promise.resolve("CameraControl: Exposure mode set successfully");
            }

            @Override
            public void onFailure(@NonNull DJIError djiError) {
                promise.reject("CameraControl: Failed to set exposure mode");
            }
        });
    }

}
