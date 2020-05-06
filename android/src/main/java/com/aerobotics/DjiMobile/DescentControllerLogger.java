package com.aerobotics.DjiMobile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aerobotics.DjiMobile.DJITimelineElements.PidController;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Locale;

import dji.common.error.DJIError;
import dji.common.flightcontroller.virtualstick.FlightControlData;
import dji.keysdk.FlightControllerKey;
import dji.keysdk.KeyManager;
import dji.keysdk.callback.GetCallback;
import dji.keysdk.callback.KeyListener;

public class DescentControllerLogger {
    private FlightControllerKey aircraftLatitudeKey = FlightControllerKey.create(FlightControllerKey.AIRCRAFT_LOCATION_LATITUDE);
    private FlightControllerKey aircraftLongitudeKey = FlightControllerKey.create(FlightControllerKey.AIRCRAFT_LOCATION_LONGITUDE);
    private FlightControllerKey altitudeKey = FlightControllerKey.create(FlightControllerKey.ALTITUDE);
    private FlightControllerKey velocityXKey = FlightControllerKey.create(FlightControllerKey.VELOCITY_X);
    private FlightControllerKey velocityYKey = FlightControllerKey.create(FlightControllerKey.VELOCITY_Y);
    private FlightControllerKey velocityZKey = FlightControllerKey.create(FlightControllerKey.VELOCITY_Z);
    private FlightControllerKey ultrasonicHeightKey = FlightControllerKey.create(FlightControllerKey.ULTRASONIC_HEIGHT_IN_METERS);
    private FlightControllerKey doesUltrasonicHaveErrorKey = FlightControllerKey.create(FlightControllerKey.ULTRASONIC_ERROR);
    private FlightControllerKey isUltrasonicBeingUsedKey = FlightControllerKey.create(FlightControllerKey.IS_ULTRASONIC_BEING_USED);

    private Float altitude;
    private Double aircraftLatitude;
    private Double aircraftLongitude;
    private Float velocityX;
    private Float velocityY;
    private Float velocityZ;
    private Float ultrasonicHeight;
    private Boolean isUltrasonicBeingUsed;
    private Boolean doesUltrasonicHaveError;
    private Float initialAltitude;

    private float pitch;
    private float roll;
    private float yaw;
    private float verticalThrottle;

    private float controllerErrorCurrent = 0;
    private float controllerIntError = 0;
    private float controllerDerError = 0;

    private float kP = 0;
    private float kD = 0;
    private float kI = 0;

    private float sampleTime;
    private float computeTime;
    private float timeStamp;

    private String filePath;

    public DescentControllerLogger(String filePath) {
        this.filePath = filePath;
        this.setUpKeyListeners();
        this.getInitialState();
        setLogFileHeadings();
    }

    private void setLogFileHeadings() {
            logStringToFile(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",
                    "timeStamp",
                    "timeElapsedInSecs",
                    "sampleTimeInSecs",
                    "heightSetPoint",
                    "latitude",
                    "longitude",
                    "altitude",
                    "velocityX",
                    "velocityY",
                    "velocityZ",
                    "z",
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
    }

    public void stop() {
        this.tearDownKeyListeners();
    }
    private void getInitialState() {
        this.getInitialAircraftLatLongAlt();
        this.getInitialUltrasonicStatus();
        this.getInitialVelocities();
    }

    private void setUpKeyListeners() {
        tearDownKeyListeners();
        KeyManager.getInstance().addListener(altitudeKey, altitudeListener);
        KeyManager.getInstance().addListener(aircraftLatitudeKey, aircraftLatitudeListener);
        KeyManager.getInstance().addListener(aircraftLongitudeKey, aircraftLongitudeListener);
        KeyManager.getInstance().addListener(velocityXKey, velocityXListener);
        KeyManager.getInstance().addListener(velocityYKey, velocityYListener);
        KeyManager.getInstance().addListener(velocityZKey, velocityZListener);
        KeyManager.getInstance().addListener(ultrasonicHeightKey, ultrasonicHeightListener);
        KeyManager.getInstance().addListener(doesUltrasonicHaveErrorKey, doesUltrasonicHaveErrorListener);
        KeyManager.getInstance().addListener(isUltrasonicBeingUsedKey, isUltrasonicBeingUsedListener);
    }

    private void tearDownKeyListeners() {
        if (KeyManager.getInstance() != null) {
            KeyManager.getInstance().removeListener(altitudeListener);
            KeyManager.getInstance().removeListener(aircraftLatitudeListener);
            KeyManager.getInstance().removeListener(aircraftLongitudeListener);
            KeyManager.getInstance().removeListener(velocityXListener);
            KeyManager.getInstance().removeListener(velocityYListener);
            KeyManager.getInstance().removeListener(velocityZListener);
            KeyManager.getInstance().removeListener(ultrasonicHeightListener);
        }
    }

    private KeyListener altitudeListener = new KeyListener() {
        @Override
        public void onValueChange(Object oldValue, Object newValue) {
            if (newValue instanceof Float) {
                altitude = (Float) newValue;
            }
        }
    };

    private KeyListener aircraftLatitudeListener = new KeyListener() {
        @Override
        public void onValueChange(Object oldValue, Object newValue) {
            if (newValue instanceof Double) {
                aircraftLatitude = (Double) newValue;
            }
        }
    };

    private KeyListener aircraftLongitudeListener = new KeyListener() {
        @Override
        public void onValueChange(Object oldValue, Object newValue) {
            if (newValue instanceof Double) {
                aircraftLongitude = (Double) newValue;
            }
        }
    };

    private KeyListener velocityXListener = new KeyListener() {
        @Override
        public void onValueChange(@Nullable Object oldValue, @Nullable Object newValue) {
            if (newValue instanceof Float) {
                velocityX = (Float) newValue;
            }
        }
    };

    private KeyListener velocityYListener = new KeyListener() {
        @Override
        public void onValueChange(@Nullable Object oldValue, @Nullable Object newValue) {
            if (newValue instanceof Float) {
                velocityY = (Float) newValue;
            }
        }
    };

    private KeyListener velocityZListener = new KeyListener() {
        @Override
        public void onValueChange(@Nullable Object oldValue, @Nullable Object newValue) {
            if (newValue instanceof Float) {
                velocityZ = (Float) newValue;
            }
        }
    };


    private KeyListener ultrasonicHeightListener = new KeyListener() {
        @Override
        public void onValueChange(@Nullable Object oldValue, @Nullable Object newValue) {
            if (newValue instanceof Float) {
                ultrasonicHeight = (Float) newValue;
            }
        }
    };

    private KeyListener isUltrasonicBeingUsedListener = new KeyListener() {
        @Override
        public void onValueChange(@Nullable Object oldValue, @Nullable Object newValue) {
            if (newValue instanceof Boolean) {
                isUltrasonicBeingUsed = (Boolean) newValue;
            }
        }
    };

    private KeyListener doesUltrasonicHaveErrorListener = new KeyListener() {
        @Override
        public void onValueChange(@Nullable Object oldValue, @Nullable Object newValue) {
            if (newValue instanceof Boolean) {
                doesUltrasonicHaveError = (Boolean) newValue;
            }
        }
    };

    private void getInitialVelocities() {
        KeyManager.getInstance().getValue(velocityXKey, new GetCallback() {
            @Override
            public void onSuccess(@NonNull Object newValue) {
                if (newValue instanceof Float) {
                    velocityX = (Float) newValue;
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
                    velocityY = (Float) newValue;
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
                    velocityZ = (Float) newValue;
                }
            }

            @Override
            public void onFailure(@NonNull DJIError djiError) {

            }
        });
    }

    private void getInitialAircraftLatLongAlt() {
        KeyManager.getInstance().getValue(aircraftLatitudeKey, new GetCallback() {
            @Override
            public void onSuccess(@NonNull Object newValue) {
                if (newValue instanceof Double) {
                    aircraftLatitude = (Double) newValue;
                }
            }

            @Override
            public void onFailure(DJIError djiError) {

            }
        });

        KeyManager.getInstance().getValue(aircraftLongitudeKey, new GetCallback() {
            @Override
            public void onSuccess(@NonNull Object newValue) {
                if (newValue instanceof Double) {
                    aircraftLongitude = (Double) newValue;
                }
            }

            @Override
            public void onFailure(DJIError djiError) {

            }
        });

        KeyManager.getInstance().getValue(altitudeKey, new GetCallback() {
            @Override
            public void onSuccess(@NonNull Object newValue) {
                if (newValue instanceof Float) {
                    altitude = (Float) newValue;
                    initialAltitude = (Float) newValue;
                }
            }

            @Override
            public void onFailure(DJIError djiError) {

            }
        });
    }

    private void getInitialUltrasonicStatus() {
        KeyManager.getInstance().getValue(ultrasonicHeightKey, new GetCallback() {
            @Override
            public void onSuccess(@NonNull Object newValue) {
                if (newValue instanceof Float) {
                    ultrasonicHeight = (Float) newValue;
                }
            }

            @Override
            public void onFailure(DJIError djiError) {

            }
        });

        KeyManager.getInstance().getValue(doesUltrasonicHaveErrorKey, new GetCallback() {
            @Override
            public void onSuccess(@NonNull Object newValue) {
                if (newValue instanceof Boolean) {
                    doesUltrasonicHaveError = (Boolean) newValue;
                }
            }

            @Override
            public void onFailure(DJIError djiError) {

            }
        });

        KeyManager.getInstance().getValue(isUltrasonicBeingUsedKey, new GetCallback() {
            @Override
            public void onSuccess(@NonNull Object newValue) {
                if (newValue instanceof Boolean) {
                    isUltrasonicBeingUsed = (Boolean) newValue;
                }
            }

            @Override
            public void onFailure(DJIError djiError) {

            }
        });
    }

    private void logStringToFile(String data) {
        String dataToWrite = data + "\n";
        if (filePath != null) {
            try {
                FileOutputStream stream = new FileOutputStream(filePath, true);
                stream.write(dataToWrite.getBytes());
                stream.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void updateFlightControlData(FlightControlData flightControlData) {
        roll = flightControlData.getRoll();
        pitch = flightControlData.getPitch();
        yaw = flightControlData.getYaw();
        verticalThrottle = flightControlData.getVerticalThrottle();
    }

    public void updateController(PidController pidController) {
        kP = pidController.getkP();
        kI = pidController.getkI();
        kD = pidController.getkD();
        controllerErrorCurrent = pidController.getErrorCurrent();
        controllerDerError = pidController.getErrorDerivative();
        controllerIntError = pidController.getErrorIntegral();
    }

    public void logControlOutputToFile(float sampleTime, float timeElapsed, Float setPoint, float computeTime) {
        String dataToWrite = (String.format(Locale.US, "%s,%.6f,%.6f,%.2f,%.5f,%.5f,%.1f,%.1f,%.1f,%.1f,%.1f,%.1f,%s,%s,%.3f,%.3f,%.3f,%.2f,%.2f,%.2f,%.9f,%.1f,%.1f,%.1f",
                new Date().getTime(),
                timeElapsed/1000000000.0f,
                sampleTime,
                setPoint,
                aircraftLatitude,
                aircraftLongitude,
                altitude,
                velocityX,
                velocityY,
                velocityZ,
                initialAltitude - altitude,
                ultrasonicHeight,
                doesUltrasonicHaveError,
                isUltrasonicBeingUsed,
                controllerErrorCurrent,
                controllerIntError,
                controllerDerError,
                roll,
                pitch,
                verticalThrottle,
                computeTime/1000000000.0f,
                kP,
                kD,
                kI)) + "\n";
        if (filePath != null) {
            try {
                FileOutputStream stream = new FileOutputStream(filePath, true);
                stream.write(dataToWrite.getBytes());
                stream.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
