package com.aerobotics.DjiMobile.DJITimelineElements;

public class PidController {
    private float kP;
    private float kI;
    private float kD;
    private float actuatorLimit;
    private float errorPrevious;
    private float errorCurrent;
    private float errorIntegral;
    private float errorDerivative;


    PidController(float kP, float kI, float kD, float actuatorLimit, float initialError) {

        this.kP = kP;
        this.kI = kI;
        this.kD = kD;
        this.actuatorLimit = actuatorLimit;
        this.errorPrevious = initialError;
        this.errorCurrent = initialError;
        errorIntegral = 0;
        errorDerivative = 0;
    }

    public float computeActuatorCommand(float errorSignal, float sampleTime) {
        errorPrevious = errorCurrent;
        errorCurrent = errorSignal;
        errorIntegral = errorIntegral + errorCurrent*sampleTime;
        errorDerivative = (errorCurrent - errorPrevious)/sampleTime;

        float actuatorCommand = (kP*errorCurrent) + (kD*errorDerivative) + kI* errorIntegral;
        float throttleSaturation = Math.abs(actuatorCommand/this.actuatorLimit);
        if (throttleSaturation > 1.0) {
            actuatorCommand = (1/throttleSaturation) * actuatorCommand;
        }
        return actuatorCommand;
    }

    public float getErrorDerivative() {
        return this.errorDerivative;
    }

    public float getErrorIntegral() {
        return this.errorIntegral;
    }
}
