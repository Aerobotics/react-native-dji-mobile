// @flow strict

import CustomTimelineElement from './CustomTimelineElement';

type Parameters = {|
  coordinate?: {|
    longitude: number,
    latitude: number,
  |},
  altitude?: number,
  flightSpeed?: number,
|}

class GoToAction extends CustomTimelineElement {
  coordinate: ?{|
    longitude: number,
    latitude: number,
  |};
  altitude: ?number;
  flightSpeed: ?number;

  constructor(
    parameters: Parameters
  ) {
    super('GoToAction');

    const {
      coordinate,
      altitude,
      flightSpeed,
    } = parameters;

    this.coordinate = coordinate;
    this.altitude = altitude;
    this.flightSpeed = flightSpeed;

    this.checkValidity();
  }

  getElementParameters() {
    const {
      coordinate,
      altitude,
      flightSpeed,
    } = this;

    return {
      coordinate,
      altitude,
      flightSpeed,
    };
  }

  checkValidity() {
    const {
      coordinate,
      altitude,
      flightSpeed,
    } = this;

    // TODO: (Adam) implement these errors!

    if (coordinate != null) {
      if (coordinate.longitude == null) {
        throw Error('Longitude Error');
      }

      if (coordinate.latitude == null) {
        throw Error('Longitude Error');
      }

    } else if (altitude == null) {
      throw Error('Please supply at least a coordinate or altitude to fly to, or both.');
    }

    if (flightSpeed != null && (flightSpeed < 2 || flightSpeed > 15)) {
      throw Error('FlightSpeed out of range [2, 15] m/s)');
    }

  }
}

export default GoToAction;
