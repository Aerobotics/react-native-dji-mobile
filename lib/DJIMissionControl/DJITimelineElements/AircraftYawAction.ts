import CustomTimelineElement from './CustomTimelineElement';

class AircraftYawAction extends CustomTimelineElement {
  angle: number;
  velocity: number;
  isAbsolute: boolean;

  constructor(
    parameters?: {
      angle: number,
      velocity?: number,
      isAbsolute?: boolean,
    }
  ) {
    super('AircraftYawAction');

    if (parameters) {
      const {
        angle,
        velocity,
        isAbsolute,
      } = parameters;

      this.angle = angle,
      this.velocity = velocity,
      this.isAbsolute = isAbsolute,

      this.checkValidity();
    }

  }

  getElementParameters() {
    const {
      angle,
      velocity,
      isAbsolute,
    } = this;

    return {
      angle,
      velocity,
      isAbsolute,
    };
  }

  checkValidity() {
    const {
      angle,
      velocity,
      isAbsolute,
    } = this;

    // TODO: check more here
    if (velocity != null && isAbsolute != null){
      throw Error('Invalid parameters: set only velocity or isAbsolute');
    }
    if (isAbsolute !== true && isAbsolute !== false){
      throw Error('isAbsolute must be true or false');
    }
    if (velocity != null && (velocity < 0 || velocity > 100)){
      throw Error('velocity must be between 0 and 100');
    }
  }

}

export default AircraftYawAction;
