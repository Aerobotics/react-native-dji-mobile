// @flow strict

import CustomTimelineElement from './CustomTimelineElement';

type controllerStickAxis = 'leftHorizontal' | 'leftVertical' | 'rightHorizontal' | 'rightVertical';

type controlStickAdjustmentSettings = {
  axis: controllerStickAxis,
  minSpeed: number,
  maxSpeed: number,
}

type yawControlStickAdjustmentSettings = {
  axis: controllerStickAxis,
  // yaw control only has a max speed, because -ve & +ve control stick values correspond to ccw & cw yaw rotations respectively.
  maxSpeed: number, // degrees/second
}

// Vertical throttle [-4, 4]
// Others [-15, 15]

type VirtualStickParameters = {|
  baseVirtualStickControlValues?: {
    pitch?: number,
    roll?: number,
    yaw?: number,
    verticalThrottle?: number,
  },
  doNotStopVirtualStickOnEnd?: boolean,
  stopExistingVirtualStick?: boolean,
  // waitForControlSticksReleaseOnEnd?: boolean, // TODO: Implement this!
  endTrigger?: 'timer' | 'ultrasonic',
  timerEndTime?: number,
  ultrasonicEndDistance?: number,
  // ultrasonicDecreaseVerticalThrottleWithDistance?: boolean, // TODO: Implement this!
  // enableObstacleAvoidance?: boolean, // True by default
  controlStickAdjustments?: {
    pitch?: controlStickAdjustmentSettings,
    roll?: controlStickAdjustmentSettings,
    yaw?: yawControlStickAdjustmentSettings,
    verticalThrottle?: controlStickAdjustmentSettings,
  }
|}

const throwErrorIfSetAndNotBoolean = (variable: any, variableName: string) => {
  if (variable != null && typeof variable != 'boolean') {
    throw new Error(`${variableName} must be a boolean`);
  }
};

class VirtualStickTimelineElement extends CustomTimelineElement {

  parameters: VirtualStickParameters;

  constructor(parameters: VirtualStickParameters) {
    super('VirtualStickTimelineElement');
    this.parameters = parameters;
  }

  getElementParameters() {
    return this.parameters;
  }

  // TODO:
  checkValidity() {

    const {
      baseVirtualStickControlValues,
      doNotStopVirtualStickOnEnd,
      stopExistingVirtualStick,
      endTrigger,
      timerEndTime,
      ultrasonicEndDistance,
      controlStickAdjustments,
    } = this.parameters;

    if (baseVirtualStickControlValues != null) {
      if (baseVirtualStickControlValues.pitch != null && (baseVirtualStickControlValues.pitch < -15 || baseVirtualStickControlValues.pitch > 15)) {
        throw new Error('pitch must be in the range [-15, 15] m/s');
      }

      if (baseVirtualStickControlValues.roll != null && (baseVirtualStickControlValues.roll < -15 || baseVirtualStickControlValues.roll > 15)) {
        throw new Error('roll must be in the range [-15, 15] m/s');
      }

      if (baseVirtualStickControlValues.roll != null && (baseVirtualStickControlValues.roll < -15 || baseVirtualStickControlValues.roll > 15)) {
        throw new Error('roll must be in the range [-15, 15] m/s');
      }

      if (baseVirtualStickControlValues.yaw != null && (baseVirtualStickControlValues.yaw < -100 || baseVirtualStickControlValues.yaw > 100)) {
        throw new Error('pitch must be in the range [-100, 100] degrees/s');
      }
    }

    if (endTrigger != null) {
      if (endTrigger === 'timer') {
        if (timerEndTime == null || typeof timerEndTime !== 'number' || timerEndTime < 0) {
          throw new Error('timerEndTime must be set as a positive number when using the timer end trigger');
        }

      } else if (endTrigger === 'ultrasonic') {
        if (ultrasonicEndDistance == null || typeof ultrasonicEndDistance !== 'number' || ultrasonicEndDistance < 0) {
          throw new Error('ultrasonicEndDistance must be set as a positive number when using the ultrasonic end trigger');
        }

      } else {
        throw new Error(`endTrigger must either be 'timer', 'ultrasonic', or undefined`);
      }
    }

    if (controlStickAdjustments != null) {
      if (controlStickAdjustments.pitch != null) {
        if (! ['leftHorizontal', 'leftVertical', 'rightHorizontal', 'rightVertical'].includes(controlStickAdjustments.pitch.axis)) {
          throw new Error('Please set a valid axis value for pitch control stick adjustment');
        }
        const {
          minSpeed,
          maxSpeed,
        } = controlStickAdjustments.pitch;
        if (minSpeed == null || typeof minSpeed != 'number' || minSpeed < -15) {
          throw new Error('minSpeed must be in the range [-15, 15] m/s');
        }
        if (maxSpeed == null || typeof maxSpeed != 'number' || maxSpeed > 15) {
          throw new Error('maxSpeed must be in the range [-15, 15] m/s');
        }
      }

      if (controlStickAdjustments.roll != null) {
        if (! ['leftHorizontal', 'leftVertical', 'rightHorizontal', 'rightVertical'].includes(controlStickAdjustments.roll.axis)) {
          throw new Error('Please set a valid axis value for roll control stick adjustment');
        }
        const {
          minSpeed,
          maxSpeed,
        } = controlStickAdjustments.roll;
        if (minSpeed == null || typeof minSpeed != 'number' || minSpeed < -15) {
          throw new Error('minSpeed must be in the range [-15, 15] m/s');
        }
        if (maxSpeed == null || typeof maxSpeed != 'number' || maxSpeed > 15) {
          throw new Error('maxSpeed must be in the range [-15, 15] m/s');
        }
      }

      if (controlStickAdjustments.yaw != null) {
        if (! ['leftHorizontal', 'leftVertical', 'rightHorizontal', 'rightVertical'].includes(controlStickAdjustments.yaw.axis)) {
          throw new Error('Please set a valid axis value for yaw control stick adjustment');
        }
        const {
          maxSpeed,
        } = controlStickAdjustments.yaw;
        if (maxSpeed == null || typeof maxSpeed != 'number' || maxSpeed > 100) {
          throw new Error('maxSpeed must be in the range [0, 100] degrees/s');
        }
      }

      if (controlStickAdjustments.verticalThrottle != null) {
        if (! ['leftHorizontal', 'leftVertical', 'rightHorizontal', 'rightVertical'].includes(controlStickAdjustments.verticalThrottle.axis)) {
          throw new Error('Please set a valid axis value for verticalThrottle control stick adjustment');
        }
        const {
          minSpeed,
          maxSpeed,
        } = controlStickAdjustments.verticalThrottle;
        if (minSpeed == null || typeof minSpeed != 'number' || minSpeed < -4) {
          throw new Error('minSpeed must be in the range [-4, 4] m/s');
        }
        if (maxSpeed == null || typeof maxSpeed != 'number' || maxSpeed > 4) {
          throw new Error('maxSpeed must be in the range [-4, 4] m/s');
        }
      }

    }

    throwErrorIfSetAndNotBoolean(doNotStopVirtualStickOnEnd, 'doNotStopVirtualStickOnEnd');
    throwErrorIfSetAndNotBoolean(stopExistingVirtualStick, 'stopExistingVirtualStick');

    return;
  }

}

export default VirtualStickTimelineElement;
