import {
  NativeModules,
} from 'react-native';

const {
  DJIMissionControlWrapper,
} = NativeModules;

type Waypoint = {
  longitude: number,
  latitude: number,
  altitude: number,
}

class DJIWaypointMission {
  _waypoints: Waypoint[] = [];
  _elementIndex: number;
  _missionBuilt = false;
  autoFlightSpeed: number;

  constructor() {
    // DJIMissionControlWrapper.createWaypointMission().then(id => {
    //   this._elementIndex = id;
    // });
  }

  _throwErrorIfMissionBuilt() {
    if (this._missionBuilt) {
      throw new Error('Cannot modify mission once built!');
    }
  }

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

  buildMission() {
    return new Promise((resolve, reject) => {
      this._throwErrorIfMissionBuilt();
      if (this._waypoints.length < 2) {
        // throw new Error('Please add a minimum of 2 waypoint points before building')
        reject('Please add a minimum of 2 waypoint points before building');
      }
      DJIMissionControlWrapper.createWaypointMission(this._waypoints).then(id => {
        this._elementIndex = id;
        this._missionBuilt = true;
        resolve();
      }).catch(err => {
        // console.log(err)
        reject(err);
      });
    });
  }

  destroyMission() {
    DJIMissionControlWrapper.destroyWaypointMission(this._elementIndex);
  }
}

const DJIMissionControl = {
  DJIWaypointMission: DJIWaypointMission,

  scheduleElement: async (element) => {
    await DJIMissionControlWrapper.scheduleElement(element._elementIndex);
  },

  startTimeline: async () => {
    await DJIMissionControlWrapper.startTimeline();
  },
}

export default DJIMissionControl;
