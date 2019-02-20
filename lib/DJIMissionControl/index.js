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
  _missionId: number;
  _missionBuilt = false;
  autoFlightSpeed: number;

  constructor() {
    // DJIMissionControlWrapper.createWaypointMission().then(id => {
    //   this._missionId = id;
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
    this._throwErrorIfMissionBuilt();
    if (this._waypoints.length < 2) {
      console.log('I AM HERE');
      throw new Error('Please add a minimum of 2 waypoint points before building')
    }
    DJIMissionControlWrapper.createWaypointMission(this._waypoints).then(id => {
      this._missionId = id;
      this._missionBuilt = true;
    }).catch(err => {
      console.log(err)
    });
  }

  destroyMission() {
    DJIMissionControlWrapper.destroyWaypointMission(this._missionId);
  }
}

const DJIMissionControl = {
  DJIWaypointMission: DJIWaypointMission,

  scheduleElement: async () => {
    await DJIMissionControlWrapper.scheduleElement();
  },
}

export default DJIMissionControl;
