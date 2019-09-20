// @flow strict

import {
  Platform,
  NativeModules,
  PermissionsAndroid,
} from 'react-native';

import {
  filter as $filter,
} from 'rxjs/operators';

import DJIMissionControl from './lib/DJIMissionControl';

import CameraControl from './lib/CameraControl';

import DJIMediaControl from './lib/DJIMedia';

import {
  DJIEventSubject,
} from './lib/utilities';

const startListener = (eventName: string) => async () => {
  await DJIMobile.startEventListener(eventName);
  return DJIEventSubject.pipe($filter(evt => evt.type === eventName)).asObservable();
};

const stopListener = (eventName: string) => async () => {
  await DJIMobile.stopEventListener(eventName);
};

const {
  DJIMobile,
} = NativeModules;

let SDKRegistered = false;

const throwIfSDKNotRegistered = () => {
  if (SDKRegistered === false) {
    throw new Error('DJI SDK not registered!');
  }
};

export const FlightLogListenerEventNames = Object.freeze({
  create: 'create',
  modify: 'modify',
});

export type FlightLogListenerEvent = {
  value: {
    eventName: $Values<typeof FlightLogListenerEventNames>,
    fileName: string,
  },
  type: string,
}

const DJIMobileWrapper = {

  registerApp: async (bridgeIp?: string) => {

    if (Platform.OS === 'android') {
      const granted = await PermissionsAndroid.request(
        PermissionsAndroid.PERMISSIONS.READ_PHONE_STATE,
        // {
        //   title: 'Permission Required',
        //   message: 'The phone permission is required to allow the DJI drone functionality to work',
        //   buttonPositive: 'Ok',
        // }
      );
      if (granted !== PermissionsAndroid.RESULTS.GRANTED) {
        throw new Error('Missing READ_PHONE_STATE permission');
      }
    }

    let registerSDKPromise;
    if (bridgeIp !== undefined) {
      registerSDKPromise = DJIMobile.registerAppAndUseBridge(bridgeIp);
    } else {
      registerSDKPromise = DJIMobile.registerApp();
    }
    registerSDKPromise.then(() => {
      SDKRegistered = true;
    }).catch(err => {
      SDKRegistered = false;
    });
    return registerSDKPromise;
  },

  limitEventFrequency: async (frequency: number) => {
    if (frequency < 0 || frequency > 10) {
      throw Error('Please ensure frequency is in the range (0, 10]');
    } else {
      await DJIMobile.limitEventFrequency(frequency);
    }
  },

  // TODO: (Adam) What should happen if these functions are called and the SDK is not registered?

  startProductConnectionListener: startListener(DJIMobile.ProductConnection),
  stopProductConnectionListener: stopListener(DJIMobile.ProductConnection),

  startBatteryPercentChargeRemainingListener: startListener(DJIMobile.BatteryChargeRemaining),
  stopBatteryPercentChargeRemainingListener: stopListener(DJIMobile.BatteryChargeRemaining),

  startAircraftLocationListener: startListener(DJIMobile.AircraftLocation),
  stopAircraftLocationListener: stopListener(DJIMobile.AircraftLocation),

  startAircraftVelocityListener: startListener(DJIMobile.AircraftVelocity),
  stopAircraftVelocityListener: stopListener(DJIMobile.AircraftVelocity),

  startAircraftAttitudeListener: startListener(DJIMobile.AircraftAttitude),
  stopAircraftAttitudeListener: stopListener(DJIMobile.AircraftAttitude),

  startAircraftCompassHeadingListener: startListener(DJIMobile.AircraftCompassHeading),
  stopAircraftCompassHeadingListener: stopListener(DJIMobile.AircraftCompassHeading),

  startAirLinkUplinkSignalQualityListener: startListener(DJIMobile.AirLinkUplinkSignalQuality),
  stopAirLinkUplinkSignalQualityListener: stopListener(DJIMobile.AirLinkUplinkSignalQuality),

  startHomeLocationListener: startListener(DJIMobile.AircraftHomeLocation),
  stopHomeLocationListener: stopListener(DJIMobile.AircraftHomeLocation),

  startGpsSignalLevelListener: startListener(DJIMobile.AircraftGpsSignalLevel),
  stopGpsSignalLevelListener: stopListener(DJIMobile.AircraftGpsSignalLevel),

  startUltrasonicHeightListener: startListener(DJIMobile.AircraftUltrasonicHeight),
  stopUltrasonicHeightListener: stopListener(DJIMobile.AircraftUltrasonicHeight),

  startCompassHasErrorListener: startListener(DJIMobile.CompassHasError),
  stopCompassHasErrorListener: stopListener(DJIMobile.CompassHasError),

  startCameraIsRecordingListener: startListener(DJIMobile.CameraIsRecording),
  stopCameraIsRecordingListener: stopListener(DJIMobile.CameraIsRecording),

  startSDCardIsInsertedListener: startListener(DJIMobile.SDCardIsInserted),
  stopSDCardIsInsertedListener: stopListener(DJIMobile.SDCardIsInserted),

  startSDCardIsReadOnlyListener: startListener(DJIMobile.SDCardIsReadOnly),
  stopSDCardIsReadOnlyListener: stopListener(DJIMobile.SDCardIsReadOnly),

  getAircraftLocation: async () => {
    return await DJIMobile.getAircraftLocation();
  },

  startRecordFlightData: async (fileName: string) => {
    await DJIMobile.startRecordFlightData(fileName);
  },
  stopRecordFlightData: async () => {
    await DJIMobile.stopRecordFlightData();
  },

  startNewMediaFileListener: async () => {
    await DJIMobile.startNewMediaFileListener();
    return DJIEventSubject.pipe($filter(evt => evt.type === 'CameraDidGenerateNewMediaFile')).asObservable();
  },
  stopNewMediaFileListener: async () => {
    // TODO: (Adam) generalize & merge these!
    if (Platform.OS === 'ios') {
      await DJIMobile.stopNotificationCenterListener('DJICameraEvent.didGenerateNewMediaFile');
    } else {
      await DJIMobile.stopEventListener('CameraDidGenerateNewMediaFile');
    }
  },
  /**
   * ANDROID ONLY
   */
  getFileList: async () => {
    return await DJIMobile.getFileList();
  },

  getFlightLogPath: async () => {
    return await DJIMobile.getFlightLogPath();
  },

  startFlightLogListener: async () => {
    await DJIMobile.startFlightLogListener();
    return DJIEventSubject.pipe($filter((evt: FlightLogListenerEvent) => evt.type === 'DJIFlightLogEvent')).asObservable();
  },

  stopFlightLogListener: async () => {
    await DJIMobile.stopFlightLogListener();
  },

  /**
   * ANDROID ONLY
   */
  getAircraftIsFlying: async () => {
    return await DJIMobile.getAircraftIsFlying();
  },

  setCollisionAvoidanceEnabled: async (enabled: boolean) => {
    return await DJIMobile.setCollisionAvoidanceEnabled(enabled);
  },

  setVirtualStickAdvancedModeEnabled: async (enabled: boolean) => {
    return await DJIMobile.setVirtualStickAdvancedModeEnabled(enabled);
  },

  setVisionAssistedPositioningEnabled: async (enabled: boolean) => {
    return await DJIMobile.setVisionAssistedPositioningEnabled(enabled);
  },

  setLandingProtectionEnabled: async (enabled: boolean) => {
    return await DJIMobile.setLandingProtectionEnabled(enabled);
  },
};

export default DJIMobileWrapper;

export {
  DJIMissionControl,
  CameraControl,
  DJIMediaControl,
};
