// @flow strict

import {
  NativeModules,
} from 'react-native';

import type {
  CreateWaypointMissionParameters,
  Waypoint,
} from './DJIMissionControlTypes';

import {
  DJIEventSubject,
} from '../utilities';

import {
  filter,
} from 'rxjs/operators';

const {
  DJIMissionControlWrapper,
} = NativeModules;

type TimelineElementType = DJIWaypointMission // | Other type

class DJIWaypointMission {
  _waypoints: Waypoint[] = [];
  _isMissionBuilt: number;
  elementIndex: number;

  // Flight speed is in m/s
  _autoFlightSpeed: 5;
  _maxFlightSpeed = 2; // maxFlightSpeed must be >= autoFlightSpeed or else the mission will be invalid

  constructor() {
    // DJIMissionControlWrapper.createWaypointMission().then(id => {
    //   this.elementIndex = id;
    // });
  }

  _throwErrorIfMissionBuilt() {
    if (this._isMissionBuilt) {
      throw new Error('Cannot modify mission once built!');
    }
  }

  // FIXME: (Adam) the app will crash if any waypoint is missing an altitude, rather throw an error!
  addWaypoint(waypoint: Waypoint) {
    this._throwErrorIfMissionBuilt();
    this._waypoints.push(waypoint);
  }

  addWaypoints(waypoints: Waypoint[]) {
    this._throwErrorIfMissionBuilt();
    this._waypoints = [...this._waypoints, ...waypoints];
  }

  removeWaypoint() {
    this._throwErrorIfMissionBuilt();
    this._waypoints.pop();
  }

  removeWaypointAtIndex(index: number) {
    this._throwErrorIfMissionBuilt();
    this._waypoints.splice(index, 1);
  }

  removeAllWaypoints() {
    this._throwErrorIfMissionBuilt();
    this._waypoints = [];
  }

  buildMission(): Promise<?String> {
    return new Promise((resolve, reject) => {
      this._throwErrorIfMissionBuilt();
      if (this._waypoints.length < 2) {
        // throw new Error('Please add a minimum of 2 waypoint points before building')
        reject('Please add a minimum of 2 waypoint points before building');
      }
      const parameters: CreateWaypointMissionParameters = {};
      DJIMissionControlWrapper.createWaypointMission(this._waypoints, parameters).then(id => {
        this.elementIndex = id;
        this._isMissionBuilt = true;
        resolve();
      }).catch(err => {
        // console.log(err)
        reject(err);
      });
    });
  }

  destroyMission() {
    DJIMissionControlWrapper.destroyWaypointMission(this.elementIndex);
  }
}

const DJIMissionControl = {
  DJIWaypointMission: DJIWaypointMission,

  scheduleElement: async (element: TimelineElementType) => {
    await DJIMissionControlWrapper.scheduleElement(element.elementIndex);
  },

  unscheduleEverything: async () => {
    await DJIMissionControlWrapper.unscheduleEverything();
  },

  startTimeline: async () => {
    await DJIMissionControlWrapper.startTimeline();
  },

  startListener: async () => {
    await DJIMissionControlWrapper.startListener();
    // TODO: (Adam) need to change eventType (int value) to a readable string value
    return DJIEventSubject.pipe(filter(evt => evt.type === 'missionControlEvent')).asObservable();
  },
  stopListener: async () => {
    await DJIMissionControlWrapper.stopListener();
  },
};

export default DJIMissionControl;
