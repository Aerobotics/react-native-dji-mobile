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

import {
  DJIEventSubject,
} from './lib/utilities';

const {
  DJIMobile,
} = NativeModules;

let SDKRegistered = false;

const throwIfSDKNotRegistered = () => {
  if (SDKRegistered === false) {
    throw new Error('DJI SDK not registered!');
  }
};

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

  // TODO: (Adam) What should happen if these functions are called and the SDK is not registered?

  startProductConnectionListener: async () => {
    await DJIMobile.startProductConnectionListener();
    return DJIEventSubject.pipe($filter(evt => evt.type === 'ProductConnection')).asObservable();
  },
  stopProductConnectionListener: async () => {
    if (Platform.OS === 'android') {
      await DJIMobile.stopEventListener('ProductConnection');
    } else {
      // TODO: (Adam) this key could potentially be used for different types (product, gimbal, etc.) so how to differentiate?
      await DJIMobile.stopKeyListener('DJIParamConnection');
    }
  },

  startBatteryPercentChargeRemainingListener: async () => {
    await DJIMobile.startBatteryPercentChargeRemainingListener();
    return DJIEventSubject.pipe($filter(evt => evt.type === 'BatteryChargeRemaining')).asObservable();
  },
  stopBatteryPercentChargeRemainingListener: async () => {
    if (Platform.OS === 'android') {
      await DJIMobile.stopEventListener('BatteryChargeRemaining');
    } else {
      await DJIMobile.stopKeyListener('DJIBatteryParamChargeRemainingInPercent');
    }
  },

  startAircraftLocationListener: async () => {
    await DJIMobile.startAircraftLocationListener();
    return DJIEventSubject.pipe($filter(evt => evt.type === 'AircraftLocation')).asObservable();
  },
  stopAircraftLocationListener: async () => {
    if (Platform.OS === 'android') {
      await DJIMobile.stopEventListener('AircraftLocation');
    } else {
      await DJIMobile.stopKeyListener('DJIFlightControllerParamAircraftLocation');
    }
  },
  getAircraftLocation: async () => {
    return await DJIMobile.getAircraftLocation();
  },

  startAircraftVelocityListener: async () => {
    await DJIMobile.startAircraftVelocityListener();
    return DJIEventSubject.pipe($filter(evt => evt.type === 'AircraftVelocity')).asObservable();
  },
  stopAircraftVelocityListener: async () => {
    if (Platform.OS === 'android') {
      await DJIMobile.stopEventListener('AircraftVelocity');
    } else {
      await DJIMobile.stopKeyListener('DJIFlightControllerParamVelocity');
    }
  },

  startAircraftAttitudeListener: async () => {
    await DJIMobile.startAircraftAttitudeListener();
    return DJIEventSubject.pipe($filter(evt => evt.type === 'AircraftAttitude')).asObservable();
  },
  stopAircraftAttitudeListener: async () => {
    if (Platform.OS === 'android') {
      await DJIMobile.stopEventListener('AircraftAttitude');
    } else {
      await DJIMobile.stopKeyListener('DJIFlightControllerParamAttitude');
    }
  },

  startAircraftCompassHeadingListener: async () => {
    await DJIMobile.startAircraftCompassHeadingListener();
    return DJIEventSubject.pipe($filter(evt => evt.type === 'AircraftCompassHeading')).asObservable();
  },
  stopAircraftCompassHeadingListener: async () => {
    if (Platform.OS === 'android') {
      await DJIMobile.stopEventListener('AircraftCompassHeading');
    } else {
      await DJIMobile.stopKeyListener('DJIFlightControllerParamCompassHeading');
    }
  },

  startRecordRealTimeData: async (fileName: string) => {
    await DJIMobile.startRecordRealTimeData(fileName);
  },
  stopRecordRealTimeData: async () => {
    await DJIMobile.stopRecordRealTimeData();
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
  getFileList: () => {
    return DJIMobile.getFileList();
  }
};

export default DJIMobileWrapper;

export {
  DJIMissionControl,
  CameraControl,
};
