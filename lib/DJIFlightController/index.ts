import {
  NativeModules,
} from 'react-native';

import {
  DJIEventSubject, observeEvent,
} from '../utilities';

import {
  filter as $filter,
} from 'rxjs/operators';

const {
  FlightControllerWrapper,
} = NativeModules;

import {
  VirtualStickParameters,
} from '../DJIMissionControl/DJITimelineElements/VirtualStickTimelineElement';
import {
  LocationCoordinate3D,
  WaypointMissionExecutionProgressEvent,
  WaypointMissionState,
  WaypointMissionUploadEvent
} from '../../types';

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
    return FlightControllerWrapper.startVirtualStick(parameters);
  },
  stopVirtualStick: async () => {
    return FlightControllerWrapper.stopVirtualStick();
  },

  startYawAction: (angle: number, isAbsolute: boolean, timeoutMs: number) => {
    return FlightControllerWrapper.startYawAction(angle, isAbsolute, timeoutMs);
  },

  startWaypointMission: async (parameters: WaypointMissionParameters) => {
    return FlightControllerWrapper.startWaypointMission(parameters);
  },
  stopWaypointMission: async () => {
    return FlightControllerWrapper.stopWaypointMission();
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
    return observeEvent<WaypointMissionExecutionProgressEvent>('WaypointMissionExecutionProgress');
  },
  observeWaypointExecutionUpdate: observeEvent<WaypointMissionExecutionProgressEvent>('WaypointMissionExecutionProgress'),
  startWaypointMissionStateListener: async () => {
    await FlightControllerWrapper.startWaypointMissionStateListener();
    return observeEvent<WaypointMissionState>('WaypointMissionState');
  },
  observeWaypointMissionState: observeEvent<WaypointMissionState>('WaypointMissionState'),
  startWaypointMissionUploadListener: async () => {
    await FlightControllerWrapper.startWaypointMissionUploadListener();
    return observeEvent<WaypointMissionUploadEvent>('WaypointMissionUploadProgress')
  },
  observeWaypointMissionUpload: observeEvent<WaypointMissionUploadEvent>('WaypointMissionUploadProgress'),
  stopAllWaypointMissionListeners: async () => {
    return FlightControllerWrapper.stopAllWaypointMissionListeners();
  },

  startRecordFlightData: async (fileName: string) => {
    return FlightControllerWrapper.startRecordFlightData(fileName);
  },
  stopRecordFlightData: async () => {
    return FlightControllerWrapper.stopRecordFlightData();
  },
  setAutoFlightSpeed: async (speed: number) => {
    return FlightControllerWrapper.setAutoFlightSpeed(speed);
  },
  setTerrainFollowModeEnabled: async (enabled: boolean) => {
    return FlightControllerWrapper.setTerrainFollowModeEnabled(enabled);
  },
  getUltrasonicHeight: async () => {
    return FlightControllerWrapper.getUltrasonicHeight();
  },
  setVirtualStickAdvancedModeEnabled: async (enabled: boolean) => {
    return FlightControllerWrapper.setVirtualStickAdvancedModeEnabled(enabled);
  },
  isVirtualStickAdvancedModeEnabled: async () => {
    return FlightControllerWrapper.isVirtualStickAdvancedModeEnabled();
  },
  isOnboardSDKDeviceAvailable: async () => {
    return FlightControllerWrapper.isOnboardSDKDeviceAvailable();
  },
  sendDataToOnboardSDKDevice: async (data: number[]) => {
    return FlightControllerWrapper.sendDataToOnboardSDKDevice(data);
  },
  startOnboardSDKDeviceDataListener: async () => {
    await FlightControllerWrapper.startOnboardSDKDeviceDataListener();
    return DJIEventSubject.pipe($filter(evt => evt.type === 'OnboardSDKDeviceData'));
  },
  stopOnboardSDKDeviceDataListener: async () => {
    await FlightControllerWrapper.stopOnboardSDKDeviceDataListener();
  },
  setPowerSupplyPortEnabled: async (enabled: boolean) => {
    return FlightControllerWrapper.setPowerSupplyPortEnabled(enabled);
  },
  getPowerSupplyPortEnabled: async () => {
    return FlightControllerWrapper.getPowerSupplyPortEnabled();
  },
  doesCompassNeedCalibrating: async () => {
    return FlightControllerWrapper.doesCompassNeedCalibrating();
  },
  getRemoteControllerFlightMode: async (): Promise<RemoteControllerFlightModes> => {
    return FlightControllerWrapper.getRemoteControllerFlightMode();
  }
 };

export default DJIFlightController;
