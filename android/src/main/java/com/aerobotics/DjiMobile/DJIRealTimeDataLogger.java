package com.aerobotics.DjiMobile;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import dji.common.error.DJIError;
import dji.common.gimbal.Attitude;
import dji.common.product.Model;
import dji.keysdk.CameraKey;
import dji.keysdk.FlightControllerKey;
import dji.keysdk.GimbalKey;
import dji.keysdk.KeyManager;
import dji.keysdk.ProductKey;
import dji.keysdk.callback.GetCallback;
import dji.keysdk.callback.KeyListener;

public class DJIRealTimeDataLogger extends ReactContextBaseJavaModule {

    private FlightControllerKey aircraftLatitudeKey = FlightControllerKey.create(FlightControllerKey.AIRCRAFT_LOCATION_LATITUDE);
    private FlightControllerKey aircraftLongitudeKey = FlightControllerKey.create(FlightControllerKey.AIRCRAFT_LOCATION_LONGITUDE);
    private FlightControllerKey altitudeKey = FlightControllerKey.create(FlightControllerKey.ALTITUDE);
    private GimbalKey gimbalAttitudeKey = GimbalKey.create(GimbalKey.ATTITUDE_IN_DEGREES);
    private FlightControllerKey velocityXKey = FlightControllerKey.create(FlightControllerKey.VELOCITY_X);
    private FlightControllerKey velocityYKey = FlightControllerKey.create(FlightControllerKey.VELOCITY_Y);
    private FlightControllerKey velocityZKey = FlightControllerKey.create(FlightControllerKey.VELOCITY_Z);
    private FlightControllerKey attitudePitchKey = FlightControllerKey.create(FlightControllerKey.ATTITUDE_PITCH);
    private FlightControllerKey attitudeRollKey = FlightControllerKey.create(FlightControllerKey.ATTITUDE_ROLL);
    private FlightControllerKey attitudeYawKey = FlightControllerKey.create(FlightControllerKey.ATTITUDE_YAW);
    private CameraKey isRecordingKey = CameraKey.create(CameraKey.IS_RECORDING);
    private FlightControllerKey ultrasonicKey = FlightControllerKey.create(FlightControllerKey.ULTRASONIC_HEIGHT_IN_METERS);
    private FlightControllerKey compassHeadingKey = FlightControllerKey.create(FlightControllerKey.COMPASS_HEADING);

    private KeyListener isRecordingListener = new KeyListener() {
        @Override
        public void onValueChange(@Nullable Object oldValue, @Nullable Object newValue) {
            if (newValue instanceof Boolean && oldValue instanceof Boolean) {
                // Check if isRecording value has changed from false to true
                if (!((Boolean) oldValue) && (Boolean) newValue) {
                    writeStringToLogFile("camera: startCaptureVideo");
                } else {
                    writeStringToLogFile("camera: stopCaptureVideo");
                }
            }
        }
    };

    private KeyListener altitudeListener = new KeyListener() {
        @Override
        public void onValueChange(Object oldValue, Object newValue) {
            if (newValue instanceof Float) {
                writeStringToLogFile("altitude:" + newValue.toString());
            }
        }
    };

    private KeyListener aircraftLatitudeListener = new KeyListener() {
        @Override
        public void onValueChange(Object oldValue, Object newValue) {
            if (newValue instanceof Double) {
                writeStringToLogFile("latitude:" + newValue.toString());
            }
        }
    };

    private KeyListener aircraftLongitudeListener = new KeyListener() {
        @Override
        public void onValueChange(Object oldValue, Object newValue) {
            if (newValue instanceof Double) {
                writeStringToLogFile("longitude:" + newValue.toString());
            }
        }
    };

    private KeyListener gimbalAttitudeListener = new KeyListener() {
        @Override
        public void onValueChange(@Nullable Object oldValue, @Nullable Object newValue) {
            if (newValue instanceof Attitude) {
                float gimbalPitch = ((Attitude) newValue).getPitch();
                float gimbalRoll = ((Attitude) newValue).getRoll();
                float gimbalYaw = ((Attitude) newValue).getYaw();
                writeStringToLogFile("gimbal_pitch:" + gimbalPitch);
                writeStringToLogFile("gimbal_roll:" + gimbalRoll);
                writeStringToLogFile("gimbal_yaw:" + gimbalYaw);
            }
        }
    };

    private KeyListener velocityXListener = new KeyListener() {
        @Override
        public void onValueChange(@Nullable Object oldValue, @Nullable Object newValue) {
            if (newValue instanceof Float) {
                writeStringToLogFile("velocity_n:" + newValue.toString());
            }
        }
    };

    private KeyListener velocityYListener = new KeyListener() {
        @Override
        public void onValueChange(@Nullable Object oldValue, @Nullable Object newValue) {
            if (newValue instanceof Float) {
                writeStringToLogFile("velocity_e:" + newValue.toString());
            }
        }
    };

    private KeyListener velocityZListener = new KeyListener() {
        @Override
        public void onValueChange(@Nullable Object oldValue, @Nullable Object newValue) {
            if (newValue instanceof Float) {
                writeStringToLogFile("velocity_d:" + newValue.toString());
            }
        }
    };

    private KeyListener attitudePitchListener = new KeyListener() {
        @Override
        public void onValueChange(@Nullable Object oldValue, @Nullable Object newValue) {
            if (newValue instanceof Double) {
                writeStringToLogFile("drone_pitch:" + newValue.toString());
            }
        }
    };

    private KeyListener attitudeRollListener = new KeyListener() {
        @Override
        public void onValueChange(@Nullable Object oldValue, @Nullable Object newValue) {
            if (newValue instanceof Double) {
                writeStringToLogFile("drone_roll:" + newValue.toString());
            }
        }
    };

    private KeyListener attitudeYawListener = new KeyListener() {
        @Override
        public void onValueChange(@Nullable Object oldValue, @Nullable Object newValue) {
            if (newValue instanceof Double) {
                writeStringToLogFile("drone_yaw:" + newValue.toString());
            }
        }
    };

    private KeyListener ultrasonicHeightListener = new KeyListener() {
        @Override
        public void onValueChange(@Nullable Object oldValue, @Nullable Object newValue) {
            if (newValue instanceof Float) {
                Float height = (Float) newValue;
                writeStringToLogFile("ultrasonic_height:" + height.toString());
            }
        }
    };

    private KeyListener compassHeadingListener = new KeyListener() {
        @Override
        public void onValueChange(@Nullable Object oldValue, @Nullable Object newValue) {
            if (newValue instanceof Float) {
                Float compassHeading = (Float) newValue;
                writeStringToLogFile("compass_heading:" + newValue.toString());
            }
        }
    };

    private ReactApplicationContext reactApplicationContext;
    private File logFile;
    private boolean isLogging = false;

    public DJIRealTimeDataLogger(ReactApplicationContext reactContext) {
        super(reactContext);
        reactApplicationContext = reactContext;
    }

    @Override
    public String getName() {
        return "DJIRealTimeDataLogger";
    }

    private void setUpKeyListeners() {
        tearDownKeyListeners();
        KeyManager.getInstance().addListener(altitudeKey, altitudeListener);
        KeyManager.getInstance().addListener(aircraftLatitudeKey, aircraftLatitudeListener);
        KeyManager.getInstance().addListener(aircraftLongitudeKey, aircraftLongitudeListener);
        KeyManager.getInstance().addListener(gimbalAttitudeKey, gimbalAttitudeListener);
        KeyManager.getInstance().addListener(velocityXKey, velocityXListener);
        KeyManager.getInstance().addListener(velocityYKey, velocityYListener);
        KeyManager.getInstance().addListener(velocityZKey, velocityZListener);
        KeyManager.getInstance().addListener(attitudePitchKey, attitudePitchListener);
        KeyManager.getInstance().addListener(attitudeRollKey, attitudeRollListener);
        KeyManager.getInstance().addListener(attitudeYawKey, attitudeYawListener);
        KeyManager.getInstance().addListener(isRecordingKey, isRecordingListener);
        KeyManager.getInstance().addListener(ultrasonicKey, ultrasonicHeightListener);
        KeyManager.getInstance().addListener(compassHeadingKey, compassHeadingListener);
    }

    private void tearDownKeyListeners() {
        if (KeyManager.getInstance() != null) {
            KeyManager.getInstance().removeListener(altitudeListener);
            KeyManager.getInstance().removeListener(aircraftLatitudeListener);
            KeyManager.getInstance().removeListener(aircraftLongitudeListener);
            KeyManager.getInstance().removeListener(gimbalAttitudeListener);
            KeyManager.getInstance().removeListener(velocityXListener);
            KeyManager.getInstance().removeListener(velocityYListener);
            KeyManager.getInstance().removeListener(velocityZListener);
            KeyManager.getInstance().removeListener(attitudePitchListener);
            KeyManager.getInstance().removeListener(attitudeRollListener);
            KeyManager.getInstance().removeListener(attitudeYawListener);
            KeyManager.getInstance().removeListener(isRecordingListener);
            KeyManager.getInstance().removeListener(ultrasonicHeightListener);
            KeyManager.getInstance().removeListener(compassHeadingListener);
        }
    }

    public void startLogging(String fileName) {
        this.createLogFile(fileName);
        this.setUpKeyListeners();
        this.recordInitialValues();
        this.isLogging = true;
    }

    private void createLogFile(String fileName) {
        File path = reactApplicationContext.getFilesDir();
        File file = new File(path, fileName + ".txt");
        this.logFile = file;
    }

    private void recordInitialValues() {
        this.recordModelName();
        this.recordInitialGimbalPosition();
        this.recordInitialVelocities();
        this.recordInitialCompassHeading();
    }

    private void recordModelName() {
        ProductKey modelNameKey = ProductKey.create(ProductKey.MODEL_NAME);
        KeyManager.getInstance().getValue(modelNameKey, new GetCallback() {
            @Override
            public void onSuccess(@NonNull Object value) {
                if (value instanceof Model) {
                    writeStringToLogFile("modelName:" + ((Model) value).getDisplayName());
                }
            }

            @Override
            public void onFailure(@NonNull DJIError djiError) {
            }
        });
    }

    private void recordInitialGimbalPosition() {
        KeyManager.getInstance().getValue(gimbalAttitudeKey, new GetCallback() {
            @Override
            public void onSuccess(@NonNull Object value) {
                if (value instanceof Attitude) {
                    float gimbalPitch = ((Attitude) value).getPitch();
                    float gimbalRoll = ((Attitude) value).getRoll();
                    float gimbalYaw = ((Attitude) value).getYaw();
                    writeStringToLogFile("gimbal_pitch:" + gimbalPitch);
                    writeStringToLogFile("gimbal_roll:" + gimbalRoll);
                    writeStringToLogFile("gimbal_yaw:" + gimbalYaw);
                }
            }

            @Override
            public void onFailure(@NonNull DJIError djiError) {

            }
        });
    }

    private void recordInitialVelocities() {
        KeyManager.getInstance().getValue(velocityXKey, new GetCallback() {
            @Override
            public void onSuccess(@NonNull Object newValue) {
                if (newValue instanceof Float) {
                    writeStringToLogFile("velocity_n:" + newValue.toString());
                }
            }

            @Override
            public void onFailure(@NonNull DJIError djiError) {

            }
        });
        KeyManager.getInstance().getValue(velocityYKey, new GetCallback() {
            @Override
            public void onSuccess(@NonNull Object newValue) {
                if (newValue instanceof Float) {
                    writeStringToLogFile("velocity_e:" + newValue.toString());
                }
            }

            @Override
            public void onFailure(@NonNull DJIError djiError) {

            }
        });
        KeyManager.getInstance().getValue(velocityZKey, new GetCallback() {
            @Override
            public void onSuccess(@NonNull Object newValue) {
                if (newValue instanceof Float) {
                    writeStringToLogFile("velocity_d:" + newValue.toString());
                }
            }

            @Override
            public void onFailure(@NonNull DJIError djiError) {

            }
        });
    }

    private void recordInitialCompassHeading() {
        KeyManager.getInstance().getValue(compassHeadingKey, new GetCallback() {
            @Override
            public void onSuccess(@NonNull Object value) {
                if (value instanceof Float) {
                    float compassHeading = (Float) value;
                    writeStringToLogFile("compass_heading:" + compassHeading);
                }
            }

            @Override
            public void onFailure(@NonNull DJIError djiError) {

            }
        });
    }


    public void stopLogging() {
        this.tearDownKeyListeners();
        this.isLogging = false;
    }

    public boolean isLogging() {
        return isLogging;
    }

    private void writeStringToLogFile(String data) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSS");
        String timeStamp = df.format(Calendar.getInstance().getTime());
        String timeStampedData = timeStamp + " " + data + "\n";
        try {
            FileOutputStream stream = new FileOutputStream(logFile, true);
            stream.write(timeStampedData.getBytes());
            stream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
