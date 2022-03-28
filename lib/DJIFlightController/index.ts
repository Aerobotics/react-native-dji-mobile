import {
  NativeModules,
} from 'react-native';

import {
  DJIEventSubject,
} from '../utilities';

import {
  filter as $filter, map as $map,
} from 'rxjs/operators';

const {
  FlightControllerWrapper,
} = NativeModules;

import {
  VirtualStickParameters,
} from '../DJIMissionControl/DJITimelineElements/VirtualStickTimelineElement';
import { LocationCoordinate3D } from '../../types';

type RemoteControllerFlightModes = 'P' | 'A' | 'S' | 'G' | 'M' | 'F' | 'T' | 'UNKNOWN'
type WaypointMissionHeadingMode = 'AUTO' | 'USING_INITIAL_DIRECTION' | 'CONTROL_BY_REMOTE_CONTROLLER' | 'USING_WAYPOINT_HEADING' | 'TOWARD_POINT_OF_INTEREST'
type WaypointMissionGotoWaypointMode = 'SAFELY' | 'POINT_TO_POINT'
type WaypointMissionFlightPathMode = 'NORMAL' | 'CURVED'
type Waypoint = LocationCoordinate3D &
  {
    heading?: number,
    speed?: number,
    cornerRadiusInMeters?: number
    actions?: [{
      actionType: string,
      actionParam: number,
    }],
    shootPhotoDistanceInterval?: number,
    shootPhotoTimeInterval?: number,
  }
interface WaypointMissionParameters {
  autoFlightSpeed: number,
  maxFlightSpeed: number,
  waypoints: Waypoint[],
  headingMode?: WaypointMissionHeadingMode,
  heading?: number,
  goToWaypointMode?: WaypointMissionGotoWaypointMode,
  flightPathMode?: WaypointMissionFlightPathMode,
}

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

  startWaypointMission: async (parameters: WaypointMissionParameters) => {
    return FlightControllerWrapper.startWaypointMission(parameters);
  },
  stopWaypointMission: async () => {
    return await FlightControllerWrapper.stopWaypointMission();
  },

  startVirtualStickTimelineElementEventListener: async () => {
    return DJIEventSubject.pipe($filter(evt => evt.type === 'VirtualStickTimelineElementEvent'));
  },
  stopVirtualStickTimelineElementEventListener: async () => {
    return; // The events are sent automatically when a virtual stick timeline event is running, so no listener needs to be started or stopped
  },

  startWaypointMissionFinishedListener: async () => {
    await FlightControllerWrapper.startWaypointMissionFinishedListener();
    return DJIEventSubject.pipe($filter(evt => evt.type === 'WaypointMissionFinished'));
  },
  startWaypointMissionStartedListener: async () => {
    return DJIEventSubject.pipe($filter(evt => evt.type === 'WaypointMissionStarted'));
  },
  startWaypointExecutionUpdateListener: async () => {
    await FlightControllerWrapper.startWaypointExecutionUpdateListener();
    return DJIEventSubject.pipe($filter(evt => evt.type === 'WaypointMissionExecutionProgress'), $map(evt => evt.value));
  },
  observeWaypointExecutionUpdateListener: DJIEventSubject.pipe($filter(evt => evt.type === 'WaypointMissionExecutionProgress'), $map(evt => evt.value)),
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
    return DJIEventSubject.pipe($filter(evt => evt.type === 'OnboardSDKDeviceData'));
  },
  stopOnboardSDKDeviceDataListener: async () => {
    await FlightControllerWrapper.stopOnboardSDKDeviceDataListener();
  },
  setPowerSupplyPortEnabled: async (enabled: boolean) => {
    return await FlightControllerWrapper.setPowerSupplyPortEnabled(enabled);
  },
  getPowerSupplyPortEnabled: async () => {
    return await FlightControllerWrapper.getPowerSupplyPortEnabled();
  },
  doesCompassNeedCalibrating: async () => {
    return await FlightControllerWrapper.doesCompassNeedCalibrating();
  },
  getRemoteControllerFlightMode: async (): Promise<RemoteControllerFlightModes> => {
    return await FlightControllerWrapper.getRemoteControllerFlightMode();
  }
 };

export default DJIFlightController;
