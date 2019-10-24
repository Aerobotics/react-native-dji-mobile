package com.aerobotics.DjiMobile;

import android.support.annotation.NonNull;
import android.util.Log;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableNativeArray;
import dji.common.camera.ResolutionAndFrameRate;
import dji.common.camera.SettingsDefinitions;
import dji.common.camera.WhiteBalance;
import dji.common.error.DJIError;
import dji.keysdk.CameraKey;
import dji.keysdk.DJIKey;
import dji.keysdk.callback.GetCallback;
import dji.keysdk.callback.SetCallback;
import dji.keysdk.callback.ActionCallback;
import dji.sdk.sdkmanager.DJISDKManager;

public class CameraControlNative extends ReactContextBaseJavaModule {
    public CameraControlNative(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return "CameraControlNative";
    }

    @ReactMethod
    public void setPhotoAspectRatio(String photoAspectRatio, final Promise promise) {
        DJIKey photoAspectRatioKey = CameraKey.create(CameraKey.PHOTO_ASPECT_RATIO);
        DJISDKManager.getInstance().getKeyManager().setValue(photoAspectRatioKey, SettingsDefinitions.PhotoAspectRatio.valueOf(photoAspectRatio), new SetCallback() {
            @Override
            public void onSuccess() {
                Log.i("REACT", "CameraControlNative: Photo aspect ratio set successfully");
                promise.resolve("CameraControlNative: Photo aspect ratio set successfully");
            }

            @Override
            public void onFailure(@NonNull DJIError djiError) {
                promise.reject("CameraControlNative: Failed to set photo aspect ratio");
            }
        });
    }

    @ReactMethod
    public void getPhotoAspectRatio(final Promise promise) {
        DJIKey photoAspectRatioKey = CameraKey.create(CameraKey.PHOTO_ASPECT_RATIO);
        DJISDKManager.getInstance().getKeyManager().getValue(photoAspectRatioKey, new GetCallback() {
            @Override
            public void onSuccess(@NonNull Object o) {
                if (o instanceof String) {
                    promise.resolve(o);
                }
            }

            @Override
            public void onFailure(@NonNull DJIError djiError) {
                promise.reject(new Throwable(djiError.getDescription()));
            }
        });

    }

    @ReactMethod
    public void getWhiteBalance(final Promise promise) {
        DJIKey whiteBalanceKey = CameraKey.create(CameraKey.WHITE_BALANCE);
        DJISDKManager.getInstance().getKeyManager().getValue(whiteBalanceKey, new GetCallback() {
            @Override
            public void onSuccess(@NonNull Object value) {
                if (value instanceof SettingsDefinitions.WhiteBalancePreset) {
                    promise.resolve(value.toString());
                }
                if (value instanceof Integer) {
                    promise.resolve(value);
                }
            }

            @Override
            public void onFailure(@NonNull DJIError djiError) {
                promise.reject(new Throwable(djiError.getDescription()));
            }
        });
    }

    @ReactMethod
    public void setWhiteBalance(ReadableMap whiteBalance, final Promise promise) {
        DJIKey whiteBalanceKey = CameraKey.create(CameraKey.WHITE_BALANCE);
        String preset = null;
        Integer colorTemperature = null;
        try {
           preset = whiteBalance.getString("preset");
        } catch (Exception e) {}
        try {
            colorTemperature =  whiteBalance.getInt("colorTemperature");
        } catch (Exception e) {}

        if (preset != null) {
            WhiteBalance whiteBalanceObj = new WhiteBalance(SettingsDefinitions.WhiteBalancePreset.valueOf(whiteBalance.getString("preset")));
            DJISDKManager.getInstance().getKeyManager().setValue(whiteBalanceKey, whiteBalanceObj, new SetCallback() {
                @Override
                public void onSuccess() {
                    Log.i("REACT", "CameraControlNative: White balance preset set successfully");
                    promise.resolve("CameraControlNative: White balance preset set successfully");
                }

                @Override
                public void onFailure(@NonNull DJIError djiError) {
                    promise.reject("CameraControlNative: Failed to set white balance preset");
                }
            });
        } else if (colorTemperature != null){
            WhiteBalance whiteBalanceObj = new WhiteBalance(SettingsDefinitions.WhiteBalancePreset.CUSTOM, whiteBalance.getInt("colorTemperature"));
            DJISDKManager.getInstance().getKeyManager().setValue(whiteBalanceKey, whiteBalanceObj, new SetCallback() {
                @Override
                public void onSuccess() {
                    Log.i("REACT", "CameraControlNative: White balance color temperature set successfully");
                    promise.resolve("CameraControlNative: White balance color temperature set successfully");
                }

                @Override
                public void onFailure(@NonNull DJIError djiError) {
                    promise.reject("CameraControlNative: Failed to set white balance color temperature");
                }
            });
        } else {
            promise.reject("CameraControlNative: Error invalid white balance parameters");
        }
    }

    @ReactMethod
    public void getExposureMode(final Promise promise) {
        DJIKey exposureModeKey = CameraKey.create(CameraKey.EXPOSURE_MODE);
        DJISDKManager.getInstance().getKeyManager().getValue(exposureModeKey, new GetCallback() {
            @Override
            public void onSuccess(@NonNull Object value) {
                if(value instanceof String) {
                    promise.resolve(value);
                }
            }

            @Override
            public void onFailure(@NonNull DJIError djiError) {
                promise.reject(new Throwable(djiError.getDescription()));
            }
        });
    }

    @ReactMethod
    public void setExposureMode(String exposureMode, final Promise promise) {
        DJIKey exposureModeKey = CameraKey.create(CameraKey.EXPOSURE_MODE);
        DJISDKManager.getInstance().getKeyManager().setValue(exposureModeKey, SettingsDefinitions.ExposureMode.valueOf(exposureMode), new SetCallback() {
            @Override
            public void onSuccess() {
                Log.i("REACT", "CameraControlNative: Exposure mode set successfully");
                promise.resolve("CameraControlNative: Exposure mode set successfully");
            }

            @Override
            public void onFailure(@NonNull DJIError djiError) {
                promise.reject("CameraControlNative: Failed to set exposure mode");
            }
        });
    }

    @ReactMethod
    public void stopRecording(final Promise promise) {
      DJIKey stopRecordingKey = CameraKey.create(CameraKey.STOP_RECORD_VIDEO);
      DJISDKManager.getInstance().getKeyManager().performAction(stopRecordingKey, new ActionCallback() {
        @Override
        public void onSuccess() {
            Log.i("REACT", "CameraControlNative: stopRecording ran successfully");
            promise.resolve("CameraControlNative: stopRecording ran successfully");
        }

        @Override
        public void onFailure(@NonNull DJIError djiError) {
            promise.reject("CameraControlNative: stopRecording failed to stop recording");
        }
      });
    }

    @ReactMethod
    public void setVideoFileFormat(String videoFileFormat, final Promise promise) {
        DJIKey videoFileFormatKey = CameraKey.create(CameraKey.VIDEO_FILE_FORMAT);
        DJISDKManager.getInstance().getKeyManager().setValue(videoFileFormatKey, SettingsDefinitions.VideoFileFormat.valueOf(videoFileFormat), new SetCallback() {
            @Override
            public void onSuccess() {
                promise.resolve("CameraControlNative: Video file format set successfully");
            }

            @Override
            public void onFailure(@NonNull DJIError djiError) {
                promise.reject("CameraControlNative: Failed to set Video file format " + djiError.getDescription());
            }
        });
    }

    @ReactMethod
    public void setVideoFileCompressionStandard(String videoFileCompressionStandard, final Promise promise) {
        DJIKey videoFileCompressionStandardKey = CameraKey.create(CameraKey.VIDEO_FILE_COMPRESSION_STANDARD);
        DJISDKManager.getInstance().getKeyManager().setValue(videoFileCompressionStandardKey, SettingsDefinitions.VideoFileCompressionStandard.valueOf(videoFileCompressionStandard), new SetCallback() {
            @Override
            public void onSuccess() {
                promise.resolve("CameraControlNative: Video file compression standard set successfully");
            }

            @Override
            public void onFailure(@NonNull DJIError djiError) {
                promise.reject("CameraControlNative: Failed to set Video file compression standard " + djiError.getDescription());
            }
        });
    }

    @ReactMethod
    public void setVideoResolutionAndFrameRate(String resolution, String frameRate, final Promise promise) {
        DJIKey videoResolutionAndFrameRateKey = CameraKey.create(CameraKey.RESOLUTION_FRAME_RATE);
        ResolutionAndFrameRate resolutionAndFrameRate = new ResolutionAndFrameRate(SettingsDefinitions.VideoResolution.valueOf(resolution), SettingsDefinitions.VideoFrameRate.valueOf(frameRate));
        DJISDKManager.getInstance().getKeyManager().setValue(videoResolutionAndFrameRateKey, resolutionAndFrameRate, new SetCallback() {
            @Override
            public void onSuccess() {
                Log.i("REACT", "videoSet");
                promise.resolve("CameraControlNative: Video file resolution and frame rate set successfully");
            }

            @Override
            public void onFailure(@NonNull DJIError djiError) {
                Log.i("REACT", djiError.getDescription());
                promise.reject("CameraControlNativeError", "CameraControlNative: Failed to set video resolution and frame rate " + djiError.getDescription());
            }
        });
    }

    @ReactMethod
    public void getVideoResolutionAndFrameRateRange(final Promise promise) {
        DJIKey videoResolutionAndFrameRateRange = CameraKey.create(CameraKey.VIDEO_RESOLUTION_FRAME_RATE_RANGE);
        DJISDKManager.getInstance().getKeyManager().getValue(videoResolutionAndFrameRateRange, new GetCallback() {
            @Override
            public void onSuccess(@NonNull Object value) {
                if (value instanceof ResolutionAndFrameRate[]) {
                    ResolutionAndFrameRate[] resolutionAndFrameRates = (ResolutionAndFrameRate[]) value;
                    WritableArray array = new WritableNativeArray();
                    for (int i = 0; i < resolutionAndFrameRates.length; i++) {
                        ResolutionAndFrameRate resolutionAndFrameRate = resolutionAndFrameRates[i];
                        String resolutionAndFrameRateString = resolutionAndFrameRate.toString();
                        array.pushString(resolutionAndFrameRateString);
                    }
                    promise.resolve(array);
                }
            }

            @Override
            public void onFailure(@NonNull DJIError djiError) {
                promise.reject("CameraControlNativeError", "CameraControlNative: Failed to get video resolution and frame rate range " + djiError.getDescription());
            }
        });
    }

    @ReactMethod
    public void isSDCardInserted(final Promise promise) {
        DJIKey sdCardIsInsertedKey = CameraKey.create(CameraKey.SDCARD_IS_INSERTED);
        DJISDKManager.getInstance().getKeyManager().getValue(sdCardIsInsertedKey, new GetCallback() {
            @Override
            public void onSuccess(@NonNull Object value) {
                if (value instanceof Boolean) {
                    promise.resolve(value);
                }
            }

            @Override
            public void onFailure(@NonNull DJIError djiError) {
                promise.reject("CameraControlNativeError", djiError.getDescription());
            }
        });
    }

    @ReactMethod
    public void setISO(String iso, final Promise promise) {
        DJIKey isoKey = CameraKey.create(CameraKey.ISO);
        DJISDKManager.getInstance().getKeyManager().setValue(isoKey, SettingsDefinitions.ISO.valueOf(iso), new SetCallback() {
            @Override
            public void onSuccess() {
                promise.resolve("CameraControlNative: ISO set successfully");
            }

            @Override
            public void onFailure(@NonNull DJIError djiError) {
                promise.reject("CameraControlNativeError", "CameraControlNative: Failed to set ISO " + djiError.getDescription());
            }
        });
    }

    @ReactMethod
    public void setShutterSpeed(String shutterSpeed, final Promise promise) {
        DJIKey shutterSpeedKey = CameraKey.create(CameraKey.SHUTTER_SPEED);
        DJISDKManager.getInstance().getKeyManager().setValue(shutterSpeedKey, SettingsDefinitions.ShutterSpeed.valueOf(shutterSpeed), new SetCallback() {
            @Override
            public void onSuccess() {
                promise.resolve("CameraControlNative: Shutter speed set successfully");
            }

            @Override
            public void onFailure(@NonNull DJIError djiError) {
                promise.reject("CameraControlNativeError", "CameraControlNative: Failed to set shutter speed " + djiError.getDescription());
            }
        });
    }

    @ReactMethod
    public void setAperture(String aperture, final Promise promise) {
        DJIKey apertureKey = CameraKey.create(CameraKey.APERTURE);
        DJISDKManager.getInstance().getKeyManager().setValue(apertureKey, SettingsDefinitions.Aperture.valueOf(aperture), new SetCallback() {
            @Override
            public void onSuccess() {
                promise.resolve("CameraControlNative: Aperture set successfully");
            }

            @Override
            public void onFailure(@NonNull DJIError djiError) {
                promise.reject("CameraControlNativeError", "CameraControlNative: Failed to set aperture " + djiError.getDescription());
            }
        });
    }

    @ReactMethod
    public void setCameraMode(String cameraMode, final Promise promise) {
        DJIKey cameraModeKey = CameraKey.create(CameraKey.MODE);
        DJISDKManager.getInstance().getKeyManager().setValue(cameraModeKey, SettingsDefinitions.CameraMode.valueOf(cameraMode), new SetCallback() {
            @Override
            public void onSuccess() {
                promise.resolve("CameraControlNative: Camera mode set successfully");
            }

            @Override
            public void onFailure(@NonNull DJIError djiError) {
                promise.reject("CameraControlNativeError", "CameraControlNative: Failed to set camera mode " + djiError.getDescription());
            }
        });
    }

}
