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
  type Observer,
} from 'rxjs';

import {
  filter as $filter,
  map as $map,
} from 'rxjs/operators';

const {
  FlightControllerWrapper,
} = NativeModules;

import {
  type VirtualStickParameters,
} from '../DJIMissionControl/DJITimelineElements/VirtualStickTimelineElement';


const DJIFlightController = {

  startVirtualStick: async (parameters: VirtualStickParameters) => {
    return await FlightControllerWrapper.startVirtualStick(parameters);
  },
  stopVirtualStick: async () => {
    return await FlightControllerWrapper.stopVirtualStick();
  },

  startWaypointMission: async (parameters) => {
    return await FlightControllerWrapper.startWaypointMission(parameters);
  },
  stopWaypointMission: async () => {
    return await FlightControllerWrapper.stopWaypointMission();
  },

  startWaypointMissionFinishedListener: async () => {
    await FlightControllerWrapper.startWaypointMissionFinishedListener();
    return DJIEventSubject.pipe($filter(evt => evt.type === 'WaypointMissionFinished')).asObservable();
  },
  startWaypointMissionStartedListener: async () => {
    return DJIEventSubject.pipe($filter(evt => evt.type === 'WaypointMissionStarted')).asObservable();
  },
  stopAllWaypointMissionListeners: async () => {
    return await FlightControllerWrapper.stopAllWaypointMissionListeners();
  },

  startRecordFlightData: async (fileName: string) => {
    return await FlightControllerWrapper.startRecordFlightData(fileName);
  },
  stopRecordFlightData: async () => {
    return await FlightControllerWrapper.stopRecordFlightData();
  },

};

export default DJIFlightController;
