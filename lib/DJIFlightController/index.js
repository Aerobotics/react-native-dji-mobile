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
    await FlightControllerWrapper.startVirtualStick(parameters);
  },

  stopVirtualStick: async () => {
    await FlightControllerWrapper.stopVirtualStick();
  }

};

export default DJIFlightController;
