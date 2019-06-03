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

type VirtualStickParameters = {
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
  ultrasonicDecreaseVerticalThrottleWithDistance?: boolean,
  enableObstacleAvoidance?: boolean, // True by default
  controlStickAdjustments?: {
    pitch?: controlStickAdjustmentSettings,
    roll?: controlStickAdjustmentSettings,
    yaw?: yawControlStickAdjustmentSettings,
    verticalThrottle?: controlStickAdjustmentSettings,
  }
}

class VirtualStickTimelineElement extends CustomTimelineElement {

  parameters: VirtualStickParameters;

  constructor(parameters: VirtualStickParameters) {
    super('VirtualStickTimelineElement');
    this.parameters = parameters;
  }

  getElementParameters() {
    return this.parameters;
  }

  checkValidity() {
    return;
  }

}

export default VirtualStickTimelineElement;
