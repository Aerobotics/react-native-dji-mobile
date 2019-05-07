// @flow strict

import CustomTimelineElement from './CustomTimelineElement';

type parameters = {
  endTrigger: 'none' | 'timer' | 'ultrasonic',
  timerEndTime?: number,
  ultrasonicEndDistance?: number,
  ultrasonicDecreaseVerticalThrottleWithDistance?: boolean,
  enableObstacleAvoidance?: boolean, // True by default
}

class VirtualStickTimelineElement extends CustomTimelineElement {

  parameters: parameters;

  constructor(parameters) {
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
