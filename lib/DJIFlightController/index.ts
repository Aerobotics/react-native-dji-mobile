import { NativeModules } from 'react-native';

import { DJIEventSubject, observeEvent } from '../utilities';

import { filter as $filter } from 'rxjs/operators';

import { VirtualStickParameters } from '../DJIMissionControl/DJITimelineElements/VirtualStickTimelineElement';
import {
  WaypointMissionExecutionProgressEvent,
  WaypointMissionState,
  WaypointMissionUploadEvent,
} from '../../types';

import {
  AircraftLEDsState,
  RemoteControllerFlightModes,
  SetAircraftLEDsState,
  WaypointMissionParameters,
  WaypointMissionV2Parameters,
} from './types';
import { modelSupportsWaypointV2 } from './utils/modelSupportsWaypointV2';

const { FlightControllerWrapper, WaypointMissionV2Wrapper, DJIMobile } =
  NativeModules;

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

  uploadWaypointMission: async (parameters: WaypointMissionParameters) => {
    const modelName = await DJIMobile.getModelName();
    if (modelSupportsWaypointV2(modelName)) {
      throw new Error(
        `Attempted to start waypoint V1 mission with incompatible drone model ${modelName}`,
      );
    }
    return FlightControllerWrapper.uploadWaypointMission(parameters);
  },
  uploadWaypointV2Mission: async (parameters: WaypointMissionV2Parameters) => {
    const modelName = await DJIMobile.getModelName();
    if (!modelSupportsWaypointV2(modelName)) {
      throw new Error(
        `Attempted to start waypoint V2 mission with incompatible drone model ${modelName}`,
      );
    }
    return WaypointMissionV2Wrapper.uploadWaypointMission(parameters);
  },
  startWaypointMission: async () => {
    const modelName = await DJIMobile.getModelName();
    if (modelSupportsWaypointV2(modelName)) {
      return WaypointMissionV2Wrapper.startWaypointMission();
    }
    return FlightControllerWrapper.startWaypointMission();
  },
  stopWaypointMission: async () => {
    const modelName = await DJIMobile.getModelName();
    if (modelSupportsWaypointV2(modelName)) {
      return WaypointMissionV2Wrapper.stopWaypointMission();
    }
    return FlightControllerWrapper.stopWaypointMission();
  },

  /**
   * Waypoint Mission V2
   *
   * You should not need to call these V2 methods explicitly since calling
   * startWaypointMission, etc. should choose the appropriate waypoint mission method
   */
  uploadWaypointMissionV2: async (parameters: WaypointMissionParameters) => {
    return WaypointMissionV2Wrapper.uploadWaypointMission(parameters);
  },
  startWaypointMissionV2: async () => {
    return WaypointMissionV2Wrapper.startWaypointMission();
  },
  stopWaypointMissionV2: async () => {
    return WaypointMissionV2Wrapper.stopWaypointMission();
  },

  startVirtualStickTimelineElementEventListener: async () => {
    return DJIEventSubject.pipe(
      $filter(evt => evt.type === 'VirtualStickTimelineElementEvent'),
    );
  },
  stopVirtualStickTimelineElementEventListener: async () => {
    return; // The events are sent automatically when a virtual stick timeline event is running, so no listener needs to be started or stopped
  },

  // Waypoint mission started/stopped listeners
  // TODO: add to V2 mission if useful
  startWaypointMissionFinishedListener: async () => {
    await FlightControllerWrapper.startWaypointMissionFinishedListener();
    return DJIEventSubject.pipe(
      $filter(evt => evt.type === 'WaypointMissionFinished'),
    );
  },
  startWaypointMissionStartedListener: async () => {
    return DJIEventSubject.pipe(
      $filter(evt => evt.type === 'WaypointMissionStarted'),
    );
  },

  // Waypoint mission execution state listeners
  startWaypointExecutionUpdateListener: async () => {
    const modelName = await DJIMobile.getModelName();
    if (modelSupportsWaypointV2(modelName)) {
      await WaypointMissionV2Wrapper.startWaypointExecutionUpdateListener();
    } else {
      await FlightControllerWrapper.startWaypointExecutionUpdateListener();
    }
    return observeEvent<WaypointMissionExecutionProgressEvent>(
      'WaypointMissionExecutionProgress',
    );
  },
  startWaypointV1ExecutionUpdateListener: async () => {
    await FlightControllerWrapper.startWaypointExecutionUpdateListener();
    return observeEvent<WaypointMissionExecutionProgressEvent>(
      'WaypointMissionExecutionProgress',
    );
  },
  startWaypointV2ExecutionUpdateListener: async () => {
    await WaypointMissionV2Wrapper.startWaypointExecutionUpdateListener();
    return observeEvent<WaypointMissionExecutionProgressEvent>(
      'WaypointMissionExecutionProgress',
    );
  },
  observeWaypointExecutionUpdate:
    observeEvent<WaypointMissionExecutionProgressEvent>(
      'WaypointMissionExecutionProgress',
    ),

  // Waypoint mission state listeners
  startWaypointMissionStateListener: async () => {
    const modelName = await DJIMobile.getModelName();
    if (modelSupportsWaypointV2(modelName)) {
      await WaypointMissionV2Wrapper.startWaypointMissionStateListener();
    } else {
      await FlightControllerWrapper.startWaypointMissionStateListener();
    }
    return observeEvent<WaypointMissionExecutionProgressEvent>(
      'WaypointMissionExecutionProgress',
    );
  },
  startWaypointMissionV1StateListener: async () => {
    await FlightControllerWrapper.startWaypointMissionStateListener();
    return observeEvent<WaypointMissionState>('WaypointMissionState');
  },
  startWaypointMissionV2StateListener: async () => {
    await WaypointMissionV2Wrapper.startWaypointMissionStateListener();
    return observeEvent<WaypointMissionState>('WaypointMissionState');
  },
  observeWaypointMissionState: observeEvent<WaypointMissionState>(
    'WaypointMissionState',
  ),

  // Waypoint mission upload listeners
  startWaypointMissionUploadListener: async () => {
    const modelName = await DJIMobile.getModelName();
    if (modelSupportsWaypointV2(modelName)) {
      await WaypointMissionV2Wrapper.startWaypointMissionUploadListener();
    } else {
      await FlightControllerWrapper.startWaypointMissionUploadListener();
    }
    return observeEvent<WaypointMissionUploadEvent>(
      'WaypointMissionUploadProgress',
    );
  },
  startWaypointMissionV1UploadListener: async () => {
    await FlightControllerWrapper.startWaypointMissionUploadListener();
    return observeEvent<WaypointMissionUploadEvent>(
      'WaypointMissionUploadProgress',
    );
  },
  startWaypointMissionV2UploadListener: async () => {
    await WaypointMissionV2Wrapper.startWaypointMissionUploadListener();
    return observeEvent<WaypointMissionUploadEvent>(
      'WaypointMissionUploadProgress',
    );
  },
  observeWaypointMissionUpload: observeEvent<WaypointMissionUploadEvent>(
    'WaypointMissionUploadProgress',
  ),

  // Waypoint mission error listeners
  startWaypointMissionErrorListener: async () => {
    const modelName = await DJIMobile.getModelName();
    if (modelSupportsWaypointV2(modelName)) {
      await WaypointMissionV2Wrapper.startWaypointMissionStateListener();
    } else {
      await FlightControllerWrapper.startWaypointMissionStateListener();
    }
    return observeEvent<string>('WaypointMissionError');
  },
  startWaypointMissionV1ErrorListener: async () => {
    await FlightControllerWrapper.startWaypointMissionStateListener();
    return observeEvent<string>('WaypointMissionError');
  },
  startWaypointMissionV2ErrorListener: async () => {
    await WaypointMissionV2Wrapper.startWaypointMissionStateListener();
    return observeEvent<string>('WaypointMissionError');
  },
  observeWaypointMissionError: observeEvent<string>('WaypointMissionError'),

  // Waypoint mission action listeners - V2 only
  startWaypointMissionActionStateListener: async () => {
    const modelName = await DJIMobile.getModelName();
    if (modelSupportsWaypointV2(modelName)) {
      await WaypointMissionV2Wrapper.startWaypointMissionStateListener();
    }
    return observeEvent<string>('WaypointMissionActionState');
  },
  observeWaypointMissionActionState: observeEvent<string>(
    'WaypointMissionActionState',
  ),
  observeWaypointMissionActionUploadProgress: observeEvent<any>(
    'WaypointMissionActionUploadProgress',
  ),

  // Stop waypoint mission listeners
  stopAllWaypointMissionListeners: async () => {
    return FlightControllerWrapper.stopAllWaypointMissionListeners();
  },
  stopAllWaypointMissionV1Listeners: async () => {
    return FlightControllerWrapper.stopAllWaypointMissionListeners();
  },
  stopAllWaypointMissionV2Listeners: async () => {
    return WaypointMissionV2Wrapper.stopAllWaypointMissionListeners();
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
    return DJIEventSubject.pipe(
      $filter(evt => evt.type === 'OnboardSDKDeviceData'),
    );
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
  getRemoteControllerFlightMode:
    async (): Promise<RemoteControllerFlightModes> => {
      return FlightControllerWrapper.getRemoteControllerFlightMode();
    },
  setAircraftLEDsState: async (settings: SetAircraftLEDsState) => {
    return FlightControllerWrapper.setAircraftLEDsState(settings);
  },
  getAircraftLEDsState: async (): Promise<AircraftLEDsState> => {
    return FlightControllerWrapper.getAircraftLEDsState();
  },
};

export default DJIFlightController;
