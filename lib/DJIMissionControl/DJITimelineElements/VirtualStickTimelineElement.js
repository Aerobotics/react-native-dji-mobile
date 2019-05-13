// @flow strict

import CustomTimelineElement from './CustomTimelineElement';

type controllerStickAxes = 'leftHorizontal' | 'leftVertical' | 'rightHorizontal' | 'rightVertical';

type adjustmentStickSettings = {
  axis: controllerStickAxes,
  minValue: number,
  maxValue: number,
}

type yawAdjustmentStickSettings = {
  axis: controllerStickAxes,
  maxYawSpeed: number, // degrees/second
}

// Vertical throttle [-4, 4]
// Others [-15, 15]

type VirtualStickParameters = {
  virtualStickData?: {
    pitch?: number,
    roll?: number,
    yaw?: number,
    verticalThrottle?: number,
  },
  doNotStopVirtualStickOnEnd?: boolean,
  stopExistingVirtualStick?: boolean,
  // waitForControlSticksReleaseOnEnd?: boolean,
  endTrigger?: 'timer' | 'ultrasonic',
  timerEndTime?: number,
  ultrasonicEndDistance?: number,
  ultrasonicDecreaseVerticalThrottleWithDistance?: boolean,
  enableObstacleAvoidance?: boolean, // True by default
  pitchControllerStickAdjustment?: adjustmentStickSettings,
  rollControllerStickAdjustment?: adjustmentStickSettings,
  verticalThrottleControllerStickAdjustment?: adjustmentStickSettings,
  yawControllerStickAdjustment?: yawAdjustmentStickSettings,
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
