package com.aerobotics.DjiMobile;

import androidx.annotation.NonNull;

import android.graphics.PointF;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
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
                promise.resolve("CameraControlNative: Photo aspect ratio set successfully");
            }

            @Override
            public void onFailure(@NonNull DJIError djiError) {
                promise.reject(new Throwable("setPhotoAspectRatio error: " + djiError.getDescription()));
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
                promise.reject(new Throwable("getPhotoAspectRatio error: " + djiError.getDescription()));
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
                promise.reject(new Throwable("getWhiteBalance error: " + djiError.getDescription()));
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
                    promise.resolve("CameraControlNative: White balance preset set successfully");
                }

                @Override
                public void onFailure(@NonNull DJIError djiError) {
                    promise.reject(new Throwable("setWhiteBalancePreset error: " + djiError.getDescription()));
                }
            });
        } else if (colorTemperature != null) {
            WhiteBalance whiteBalanceObj = new WhiteBalance(SettingsDefinitions.WhiteBalancePreset.CUSTOM, whiteBalance.getInt("colorTemperature"));
            DJISDKManager.getInstance().getKeyManager().setValue(whiteBalanceKey, whiteBalanceObj, new SetCallback() {
                @Override
                public void onSuccess() {
                    promise.resolve("CameraControlNative: White balance color temperature set successfully");
                }

                @Override
                public void onFailure(@NonNull DJIError djiError) {
                    promise.reject(new Throwable("setWhiteBalanceColorTemperate error: " + djiError.getDescription()));
                }
            });
        } else {
            promise.reject(new Throwable("setWhiteBalance error: Invalid argument"));
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
                promise.reject(new Throwable("getExposureMode error: " + djiError.getDescription()));
            }
        });
    }

    @ReactMethod
    public void setExposureMode(String exposureMode, final Promise promise) {
        DJIKey exposureModeKey = CameraKey.create(CameraKey.EXPOSURE_MODE);
        DJISDKManager.getInstance().getKeyManager().setValue(exposureModeKey, SettingsDefinitions.ExposureMode.valueOf(exposureMode), new SetCallback() {
            @Override
            public void onSuccess() {
                promise.resolve("CameraControlNative: Exposure mode set successfully");
            }

            @Override
            public void onFailure(@NonNull DJIError djiError) {
                promise.reject(new Throwable("setExposureMode error: " + djiError.getDescription()));
            }
        });
    }

    @ReactMethod
    public void startRecording(final Promise promise) {
        DJIKey stopRecordingKey = CameraKey.create(CameraKey.START_RECORD_VIDEO);
        DJISDKManager.getInstance().getKeyManager().performAction(stopRecordingKey, new ActionCallback() {
            @Override
            public void onSuccess() {
                promise.resolve("CameraControlNative: startRecording ran successfully");
            }

            @Override
            public void onFailure(@NonNull DJIError djiError) {
                promise.reject(new Throwable("startRecording error: " + djiError.getDescription()));
            }
        });
    }

    @ReactMethod
    public void stopRecording(final Promise promise) {
      DJIKey stopRecordingKey = CameraKey.create(CameraKey.STOP_RECORD_VIDEO);
      DJISDKManager.getInstance().getKeyManager().performAction(stopRecordingKey, new ActionCallback() {
        @Override
        public void onSuccess() {
            promise.resolve("CameraControlNative: stopRecording ran successfully");
        }

        @Override
        public void onFailure(@NonNull DJIError djiError) {
            promise.reject(new Throwable("stopRecording error: " + djiError.getDescription()));
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
                promise.reject(new Throwable("setVideoFileFormat error: " + djiError.getDescription()));
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
                promise.reject(new Throwable("setVideoFileCompressionStandard error: " + djiError.getDescription()));
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
                promise.resolve("CameraControlNative: Video file resolution and frame rate set successfully");
            }

            @Override
            public void onFailure(@NonNull DJIError djiError) {
                promise.reject(new Throwable("setVideoResolutionAndFrameRate error: " + djiError.getDescription()));
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
                    WritableArray array = Arguments.createArray();
                    for (int i = 0; i < resolutionAndFrameRates.length; i++) {
                        WritableMap map = Arguments.createMap();
                        ResolutionAndFrameRate resolutionAndFrameRate = resolutionAndFrameRates[i];
                        map.putString("videoRes", resolutionAndFrameRate.getResolution().toString());
                        map.putString("frameRate", resolutionAndFrameRate.getFrameRate().toString());
                        array.pushMap(map);
                    }
                    promise.resolve(array);
                }
            }

            @Override
            public void onFailure(@NonNull DJIError djiError) {
                promise.reject(new Throwable("getVideoResolutionAndFrameRateRange error: " + djiError.getDescription()));
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
                promise.reject(new Throwable("isSDCardInserted error: " + djiError.getDescription()));
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
                promise.reject(new Throwable("setISO error: " + djiError.getDescription()));
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
                promise.reject(new Throwable("setShutterSpeed error: " + djiError.getDescription()));
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
                promise.reject(new Throwable("setAperture error: " + djiError.getDescription()));
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
                promise.reject(new Throwable("setCameraMode error: " + djiError.getDescription()));
            }
        });
    }

    @ReactMethod
    public void setCameraColor(String cameraColor, final Promise promise) {
        DJIKey cameraColorKey = CameraKey.create(CameraKey.CAMERA_COLOR);
        DJISDKManager.getInstance().getKeyManager().setValue(cameraColorKey, SettingsDefinitions.CameraColor.valueOf(cameraColor), new SetCallback() {
            @Override
            public void onSuccess() {
                promise.resolve(null);
            }

            @Override
            public void onFailure(@NonNull DJIError djiError) {
                promise.reject(new Throwable("setCameraColor error: " + djiError.getDescription()));
            }
        });
    }

    @ReactMethod
    public void setSharpness(Integer sharpness, final Promise promise) {
        DJIKey sharpnesKey = CameraKey.create(CameraKey.SHARPNESS);
        DJISDKManager.getInstance().getKeyManager().setValue(sharpnesKey, sharpness, new SetCallback() {
            @Override
            public void onSuccess() {
                promise.resolve(null);
            }

            @Override
            public void onFailure(@NonNull DJIError djiError) {
                promise.reject(new Throwable("setSharpness error: " + djiError.getDescription()));
            }
        });
    }

    @ReactMethod void setContrast(Integer contrast, final Promise promise) {
        DJIKey contrastKey = CameraKey.create(CameraKey.CONTRAST);
        DJISDKManager.getInstance().getKeyManager().setValue(contrastKey, contrast, new SetCallback() {
            @Override
            public void onSuccess() {
                promise.resolve(null);
            }

            @Override
            public void onFailure(@NonNull DJIError djiError) {
                promise.reject(new Throwable("setContrast error: " + djiError.getDescription()));
            }
        });
    }

    @ReactMethod void setSaturation(Integer saturation, final Promise promise) {
        DJIKey saturationKey = CameraKey.create(CameraKey.SATURATION);
        DJISDKManager.getInstance().getKeyManager().setValue(saturationKey, saturation, new SetCallback() {
            @Override
            public void onSuccess() {
                promise.resolve(null);
            }

            @Override
            public void onFailure(@NonNull DJIError djiError) {
                promise.reject(new Throwable("setSaturation error: " + djiError.getDescription()));
            }
        });
    }

    @ReactMethod
    public void setVideoCaptionsEnabled(Boolean enabled, final Promise promise) {
        DJIKey videoCaptionKey = CameraKey.create(CameraKey.VIDEO_CAPTION_ENABLED);
        DJISDKManager.getInstance().getKeyManager().setValue(videoCaptionKey, enabled, new SetCallback() {
            @Override
            public void onSuccess() {
                promise.resolve("CameraControlNative: video captions set successfully");
            }

            @Override
            public void onFailure(@NonNull DJIError djiError) {
                promise.reject(new Throwable("setVideoCaptionsEnabled error: " + djiError.getDescription()));
            }
        });
    }

    @ReactMethod
    public void setFocusMode(String focusMode, final Promise promise) {
        DJIKey focusModeKey = CameraKey.create(CameraKey.FOCUS_MODE);
        DJISDKManager.getInstance().getKeyManager().setValue(focusModeKey, SettingsDefinitions.FocusMode.valueOf(focusMode), new SetCallback() {
            @Override
            public void onSuccess() {
                promise.resolve("CameraControlNative: focus mode set successfully");
            }

            @Override
            public void onFailure(@NonNull DJIError djiError) {
                promise.reject(new Throwable("setFocusMode error: " + djiError.getDescription()));
            }
        });
    }

    @ReactMethod
    public void setFocusTarget(float x, float y, final Promise promise) {
        PointF targetPoint = new PointF(x, y);
        DJIKey focusTargetKey = CameraKey.create(CameraKey.FOCUS_TARGET);
        DJISDKManager.getInstance().getKeyManager().setValue(focusTargetKey, targetPoint, new SetCallback() {
            @Override
            public void onSuccess() {
                promise.resolve("CameraControlNative: focus target set successfully");
            }

            @Override
            public void onFailure(@NonNull DJIError djiError) {
                promise.reject(new Throwable("setFocusTarget error: " + djiError.getDescription()));
            }
        });
    }

    @ReactMethod
    public void setVideoStandard(String videoStandard, final Promise promise) {
        DJIKey videoStandardKey = CameraKey.create(CameraKey.VIDEO_STANDARD);
        DJISDKManager.getInstance().getKeyManager().setValue(videoStandardKey, SettingsDefinitions.VideoStandard.valueOf(videoStandard), new SetCallback() {
            @Override
            public void onSuccess() {
                promise.resolve("setVideoStandard: video standard set successfully");
            }

            @Override
            public void onFailure(@NonNull DJIError djiError) {
                promise.reject(new Throwable("setVideoStandard error: " + djiError.getDescription()));
            }
        });
    }

    @ReactMethod
    public void getVideoStandard(final Promise promise) {
        DJIKey videoStandardKey = CameraKey.create(CameraKey.VIDEO_STANDARD);
        DJISDKManager.getInstance().getKeyManager().getValue(videoStandardKey, new GetCallback() {
            @Override
            public void onSuccess(@NonNull Object value) {
                if (value instanceof SettingsDefinitions.VideoStandard) {
                    promise.resolve(((SettingsDefinitions.VideoStandard) value).name());
                }
            }

            @Override
            public void onFailure(@NonNull DJIError djiError) {
                promise.reject(new Throwable("getVideoStandard error: " + djiError.getDescription()));
            }
        });
    }

    @ReactMethod
    public void getFocusStatus(final Promise promise) {
        DJIKey focusStatusKey = CameraKey.create(CameraKey.FOCUS_STATUS);
        DJISDKManager.getInstance().getKeyManager().getValue(focusStatusKey, new GetCallback() {
            @Override
            public void onSuccess(@NonNull Object value) {
                if (value instanceof SettingsDefinitions.FocusStatus) {
                    promise.resolve(((SettingsDefinitions.FocusStatus) value).name());
                }
            }

            @Override
            public void onFailure(@NonNull DJIError djiError) {
                promise.reject(new Throwable("getFocusStatus error: " + djiError.getDescription()));
            }
        });
    }

    @ReactMethod
    public void getFocusRingValue(final Promise promise) {
        DJIKey focusRingValueKey = CameraKey.create(CameraKey.FOCUS_RING_VALUE);
        DJISDKManager.getInstance().getKeyManager().getValue(focusRingValueKey, new GetCallback() {
            @Override
            public void onSuccess(@NonNull Object value) {
                if (value instanceof Integer) {
                    promise.resolve(value);
                }
            }

            @Override
            public void onFailure(@NonNull DJIError djiError) {
                promise.reject(new Throwable("getFocusRingValue error: " + djiError.getDescription()));
            }
        });
    }

    @ReactMethod
    public void getDisplayName(final Promise promise) {
        DJIKey getDisplayNameKey = CameraKey.create(CameraKey.DISPLAY_NAME);
        DJISDKManager.getInstance().getKeyManager().getValue(getDisplayNameKey, new GetCallback() {
            @Override
            public void onSuccess(Object value) {
                if (value instanceof String) {
                    promise.resolve(value);
                }
            }

            @Override
            public void onFailure(DJIError djiError) {
                promise.reject(new Throwable("getDisplayName error: " + djiError.getDescription()));
            }
        });
    }

    @ReactMethod
    public void isRecording(final Promise promise) {
        DJIKey isRecordingKey = CameraKey.create(CameraKey.IS_RECORDING);
        DJISDKManager.getInstance().getKeyManager().getValue(isRecordingKey, new GetCallback() {
            @Override
            public void onSuccess(Object value) {
                if (value instanceof Boolean) {
                    promise.resolve(value);
                }
            }

            @Override
            public void onFailure(DJIError djiError) {
                promise.reject(new Throwable("isRecording error: " + djiError.getDescription()));
            }
        });
    }

    @ReactMethod
    public void setExposureCompensation(String exposureCompensation, final Promise promise) {
        DJIKey exposureCompensationKey = CameraKey.create(CameraKey.EXPOSURE_COMPENSATION);
        DJISDKManager.getInstance().getKeyManager().setValue(exposureCompensationKey,
                SettingsDefinitions.ExposureCompensation.valueOf(exposureCompensation),
                new SetCallback() {
                    @Override
                    public void onSuccess() {
                        promise.resolve("setExposureCompensation: exposure compensation set successfully");
                    }

                    @Override
                    public void onFailure(DJIError djiError) {
                        promise.reject(new Throwable("setExposureCompensation error: " + djiError.getDescription()));
                    }
                });
    }

    @ReactMethod
    public void getExposureCompensation(final Promise promise) {
        DJIKey exposureCompensationKey = CameraKey.create(CameraKey.EXPOSURE_COMPENSATION);
        DJISDKManager.getInstance().getKeyManager().getValue(exposureCompensationKey,
                new GetCallback() {
                    @Override
                    public void onSuccess(Object evValue) {
                        if (evValue instanceof SettingsDefinitions.ExposureCompensation) {
                            promise.resolve(((SettingsDefinitions.ExposureCompensation) evValue).name());
                        }
                    }

                    @Override
                    public void onFailure(DJIError djiError) {
                        promise.reject(new Throwable("setExposureCompensation error: " + djiError.getDescription()));
                    }
                });
    }

    @ReactMethod
    public void getCameraMode(final Promise promise) {
        DJIKey cameraModeKey = CameraKey.create(CameraKey.MODE);
        DJISDKManager.getInstance().getKeyManager().getValue(cameraModeKey,
                new GetCallback() {
                    @Override
                    public void onSuccess(Object cameraMode) {
                        if (cameraMode instanceof SettingsDefinitions.CameraMode) {
                            promise.resolve(((SettingsDefinitions.CameraMode) cameraMode).name());
                        } else {
                            promise.reject(new Throwable("getCameraMode error: camera mode not instance of SettingsDefinitions.CameraMode"));
                        }
                    }

                    @Override
                    public void onFailure(DJIError djiError) {
                        promise.reject(new Throwable("getCameraMode error: " + djiError.getDescription()));
                    }
                });
    }

}
