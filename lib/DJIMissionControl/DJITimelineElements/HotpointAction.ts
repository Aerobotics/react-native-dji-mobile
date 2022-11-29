import CustomTimelineElement from './CustomTimelineElement';
import {
  Hotpoint,
} from '../DJIMissionControlTypes';

const validHeadings = ['ALONG_CIRCLE_LOOKING_FORWARDS', 'ALONG_CIRCLE_LOOKING_BACKWARDS',
      'TOWARDS_HOT_POINT', 'AWAY_FROM_HOT_POINT', 'CONTROLLED_BY_REMOTE_CONTROLLER', 'USING_INITIAL_HEADING'];

const validStartPoints = ['NORTH', 'SOUTH', 'EAST', 'WEST', 'NEAREST'];

class HotpointAction extends CustomTimelineElement {
  hotpoint: Hotpoint;
  angle: number;
  radius: number;
  angularVelocity: number;
  startPoint: string;
  heading: string;
  clockwise: boolean;

  constructor(
    parameters?: {
      hotpoint: Hotpoint,
      angle: number,
      radius: number,
      angularVelocity: number,
      startPoint: string,
      heading: string,
      clockwise: boolean,
    }
  ) {
    super('HotpointAction');

    if (parameters) {
      const {
        hotpoint,
        angle,
        radius,
        angularVelocity,
        startPoint,
        heading,
        clockwise,
      } = parameters;

      this.hotpoint = hotpoint,
      this.angle = angle,
      this.radius = radius,
      this.angularVelocity = angularVelocity,
      this.startPoint = startPoint,
      this.heading = heading,
      this.clockwise = clockwise,

      this.checkValidity();
    }

  }

  getElementParameters() {
    const {
      hotpoint,
      angle,
      radius,
      angularVelocity,
      startPoint,
      heading,
      clockwise,
    } = this;

    return {
      hotpoint,
      angle,
      radius,
      angularVelocity,
      startPoint,
      heading,
      clockwise,
    };
  }

  checkValidity() {
    const {
      hotpoint,
      angle,
      radius,
      angularVelocity,
      startPoint,
      heading,
      clockwise,
    } = this;

    // TODO: check more here
    if (radius < 5 || radius > 500){
      throw Error('Invalid radius: ensure radius is between 5 and 500');
    }
    if (hotpoint.altitude < 5 || hotpoint.altitude > 500){
      throw Error('Invalid altitude: ensure altitude is between 5 and 500');
    }
    if (!validHeadings.includes(heading)){
      throw Error('Invalid heading: ensure heading is one of ' + validHeadings);
    }
    if (!validStartPoints.includes(startPoint)){
      throw Error('Invalid heading: ensure startPoint is one of ' + validStartPoints);
    }
    if (clockwise !== true && clockwise !== false){
      throw Error('Clockwise must be true or false');
    }
  }

}

export default HotpointAction;
