// @flow strict

import {
  NativeModules,
} from 'react-native';

import CustomTimelineElement from './CustomTimelineElement';

import {
  type Waypoint,
} from '../DJIMissionControlTypes.js';

const {
  DJIMissionControlWrapper,
} = NativeModules;

const VALID_HEADING_MODES = ['AUTO', 'USING_INITIAL_DIRECTION', 'CONTROL_BY_REMOTE_CONTROLLER', 'USING_WAYPOINT_HEADING', 'TOWARD_POINT_OF_INTEREST'];

class WaypointMissionTimelineElement extends CustomTimelineElement {
  _waypoints: Waypoint[] = [];
  _isMissionBuilt: number;
  // Flight speed is in m/s
  _autoFlightSpeed: number;
  _maxFlightSpeed: number;
  _headingMode: string;
  _pauseSecondsAtWaypoints: ?number;

  constructor() {
    super('WaypointMissionTimelineElement');
  }

  getElementParameters() {
    const elementParameters = {
      waypoints: this._waypoints,
      autoFlightSpeed: this._autoFlightSpeed,
      maxFlightSpeed: this._maxFlightSpeed,
      headingMode: this._headingMode,
      pauseSecondsAtWaypoints: this._pauseSecondsAtWaypoints,
    };
    return elementParameters;
  }

  async checkValidity() {
    // TODO: (Adam) catch native errors and return JS version of them for better stack trace
    // TODO: (Adam) check ALL potential issues on JS first (missing waypoints, waypoints are valid, etc.)
    const {
      _maxFlightSpeed,
      _autoFlightSpeed,
      _headingMode,
      _pauseSecondsAtWaypoints,
    } = this;
    if (typeof _autoFlightSpeed !== 'number' || _autoFlightSpeed < -15 || _autoFlightSpeed > 15) {
      throw Error('Invalid auto flight speed: Ensure auto flight speed is in the range [-15, 15]');
    }

    if (typeof _maxFlightSpeed !== 'number' || _maxFlightSpeed < 2 || _maxFlightSpeed > 15) {
      throw Error('Invalid max flight speed: Ensure max flight speed is in the range [2, 15]');
    }

    if (typeof _headingMode !== 'string' || !VALID_HEADING_MODES.includes(_headingMode)) {
      throw Error(
        `Invalid headingMode ${_headingMode}, make sure it is one of ${VALID_HEADING_MODES}`
      );
    }

    if (_pauseSecondsAtWaypoints != null && (_pauseSecondsAtWaypoints < 0 || _pauseSecondsAtWaypoints > 32.767)) {
      throw Error(
        'Invalid pauseSecondsAtWaypoints: Ensure it is in the range [0, 32.767]'
      );
    }

    // This will return a rejected promise if the waypoint mission is not valid
    await DJIMissionControlWrapper.checkWaypointMissionValidity(this.getElementParameters());
  }

  // FIXME: (Adam) the app will crash if any waypoint is missing an altitude, rather throw an error!
  addWaypoint(waypoint: Waypoint) {
    // this._throwErrorIfMissionBuilt();
    this._waypoints.push(waypoint);
  }

  addWaypoints(waypoints: Waypoint[]) {
    // this._throwErrorIfMissionBuilt();
    this._waypoints = [...this._waypoints, ...waypoints];
  }

  removeWaypoint() {
    // this._throwErrorIfMissionBuilt();
    this._waypoints.pop();
  }

  removeWaypointAtIndex(index: number) {
    // this._throwErrorIfMissionBuilt();
    this._waypoints.splice(index, 1);
  }

  removeAllWaypoints() {
    // this._throwErrorIfMissionBuilt();
    this._waypoints = [];
  }

  setAutoFlightSpeed(autoFlightSpeed: number) {
    const maxFlightSpeed = this._maxFlightSpeed;
    if (autoFlightSpeed > maxFlightSpeed) {
      throw Error(
        'Invalid Auto Flight Speed: Received auto flight speed is invalid as it cannot be greater than '
        + `the set max flight speed of ${maxFlightSpeed} m/s.`
      );
    } else {
      this._autoFlightSpeed = autoFlightSpeed;
    }
  }

  setMaxFlightSpeed(maxFlightSpeed: number) {
    const autoFlightSpeed = this._autoFlightSpeed;
    if (maxFlightSpeed < autoFlightSpeed) {
      throw Error(
        'Invalid Max Flight Speed: Received max flight speed is invalid as it cannot be less than '
        + `the set auto flight speed of ${autoFlightSpeed} m/s.`
      );
    } else {
      this._maxFlightSpeed = maxFlightSpeed;
    }
  }

  setHeadingMode(headingMode: string) {
    if (!VALID_HEADING_MODES.includes(headingMode)) {
      throw Error(
        `Invalid headingMode ${headingMode}, make sure it is one of ${VALID_HEADING_MODES}`
      );
    } else {
      this._headingMode = headingMode;
    }
  }

  // buildMission(): Promise<?String> {
  //   return new Promise((resolve, reject) => {
  //     this._throwErrorIfMissionBuilt();
  //     if (this._waypoints.length < 2) {
  //       // throw new Error('Please add a minimum of 2 waypoint points before building')
  //       reject('Please add a minimum of 2 waypoint points before building');
  //     }
  //     const parameters: CreateWaypointMissionParameters = {};
  //     DJIMissionControlWrapper.createWaypointMission(this._waypoints, parameters).then(id => {
  //       this.elementIndex = id;
  //       this._isMissionBuilt = true;
  //       resolve();
  //     }).catch(err => {
  //       // console.log(err)
  //       reject(err);
  //     });
  //   });
  // }

  // destroyMission() {
  //   DJIMissionControlWrapper.destroyWaypointMission(this.elementIndex);
  // }
}

export default WaypointMissionTimelineElement;
