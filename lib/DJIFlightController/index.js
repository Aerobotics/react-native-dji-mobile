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

  startYawAction: (angle: number, isAbsolute: boolean, timeoutMs: number) => {
    return FlightControllerWrapper.startYawAction(angle, isAbsolute, timeoutMs);
  },

  startWaypointMission: async (parameters) => {
    return await FlightControllerWrapper.startWaypointMission(parameters);
  },
  stopWaypointMission: async () => {
    return await FlightControllerWrapper.stopWaypointMission();
  },

  startVirtualStickTimelineElementEventListener: async () => {
    return DJIEventSubject.pipe($filter(evt => evt.type === 'VirtualStickTimelineElementEvent')).asObservable();
  },
  stopVirtualStickTimelineElementEventListener: async () => {
    return; // The events are sent automatically when a virtual stick timeline event is running, so no listener needs to be started or stopped
  },

  startWaypointMissionFinishedListener: async () => {
    await FlightControllerWrapper.startWaypointMissionFinishedListener();
    return DJIEventSubject.pipe($filter(evt => evt.type === 'WaypointMissionFinished')).asObservable();
  },
  startWaypointMissionStartedListener: async () => {
    return DJIEventSubject.pipe($filter(evt => evt.type === 'WaypointMissionStarted')).asObservable();
  },
  startWaypointExecutionUpdateListener: async () => {
    await FlightControllerWrapper.startWaypointExecutionUpdateListener();
    return DJIEventSubject.pipe($filter(evt => evt.type === 'WaypointMissionExecutionProgress')).asObservable();
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
  setAutoFlightSpeed: async (speed: number) => {
    return await FlightControllerWrapper.setAutoFlightSpeed(speed);
  },
  setTerrainFollowModeEnabled: async (enabled: boolean) => {
    return await FlightControllerWrapper.setTerrainFollowModeEnabled(enabled);
  },
  getUltrasonicHeight: async () => {
    return await FlightControllerWrapper.getUltrasonicHeight();
  },
  setVirtualStickAdvancedModeEnabled: async (enabled: boolean) => {
    return await FlightControllerWrapper.setVirtualStickAdvancedModeEnabled(enabled);
  },
  isVirtualStickAdvancedModeEnabled: async () => {
    return await FlightControllerWrapper.isVirtualStickAdvancedModeEnabled();
  },
  isOnboardSDKDeviceAvailable: async () => {
    return await FlightControllerWrapper.isOnboardSDKDeviceAvailable();
  },
  sendDataToOnboardSDKDevice: async (data: number[]) => {
    return await FlightControllerWrapper.sendDataToOnboardSDKDevice(data);
  },
  startOnboardSDKDeviceDataListener: async () => {
    await FlightControllerWrapper.startOnboardSDKDeviceDataListener();
    return DJIEventSubject.pipe($filter(evt => evt.type === 'OnboardSDKDeviceData')).asObservable();
  },
  stopOnboardSDKDeviceDataListener: async () => {
    await FlightControllerWrapper.stopOnboardSDKDeviceDataListener();
  },
  setPowerSupplyPortEnabled: async (enabled: boolean) => {
    return await FlightControllerWrapper.setPowerSupplyPortEnabled(enabled);
  },
  getPowerSupplyPortEnabled: async () => {
    return await FlightControllerWrapper.getPowerSupplyPortEnabled();
  }
 };

export default DJIFlightController;
