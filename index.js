// @flow strict

import {
  Platform,
  NativeModules,
} from 'react-native';

import {
  filter,
} from 'rxjs/operators';

import DJIMissionControl from './lib/DJIMissionControl';

import {
  DJIEventSubject,
} from './lib/utilities';

const {
  DJIMobile,
} = NativeModules;

let SDKRegistered = false;

const DJIMobileWrapper = {

  helloWorld: ( ) => {
    console.log( 'HelloWorld!' );
  },

  registerApp: async (bridgeIp?: string) => {
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

  startProductConnectionListener: async () => {
    await DJIMobile.startProductConnectionListener();
    return DJIEventSubject.pipe(filter(evt => evt.type === 'connectionStatus')).asObservable();
  },
  stopProductConnectionListener: async () => {
    // TODO: (Adam) this key could potentially be used for different types (product, gimbal, etc.) so how to differentiate?
    await DJIMobile.stopKeyListener('DJIParamConnection');
  },

  startBatteryPercentChargeRemainingListener: async () => {
    await DJIMobile.startBatteryPercentChargeRemainingListener();
    return DJIEventSubject.pipe(filter(evt => evt.type === 'chargeRemaining')).asObservable();
  },
  stopBatteryPercentChargeRemainingListener: async () => {
    await DJIMobile.stopKeyListener('DJIBatteryParamChargeRemainingInPercent');
  },

  startAircraftLocationListener: async () => {
    await DJIMobile.startAircraftLocationListener();
    return DJIEventSubject.pipe(filter(evt => evt.type === 'aircraftLocation')).asObservable();
  },
  stopAircraftLocationListener: async () => {
    await DJIMobile.stopKeyListener('DJIFlightControllerParamAircraftLocation');
  },
  getAircraftLocation: async () => {
    return await DJIMobile.getAircraftLocation();
  },

  startAircraftVelocityListener: async () => {
    await DJIMobile.startAircraftVelocityListener();
    return DJIEventSubject.pipe(filter(evt => evt.type === 'aircraftVelocity')).asObservable();
  },
  stopAircraftVelocityListener: async () => {
    if (Platform.OS === 'android') {
      await DJIMobile.stopAircraftVelocityListener();
    } else {
      await DJIMobile.stopKeyListener('DJIFlightControllerParamVelocity');
    }
  },

  startAircraftCompassHeadingListener: async () => {
    await DJIMobile.startAircraftCompassHeadingListener();
    return DJIEventSubject.pipe(filter(evt => evt.type === 'aircraftCompassHeading')).asObservable();
  },
  stopAircraftCompassHeadingListener: async () => {
    await DJIMobile.stopKeyListener('DJIFlightControllerParamCompassHeading');
  },

  startRecordRealTimeData: async (fileName: string) => {
    await DJIMobile.startRecordRealTimeData(fileName);
  },
  stopRecordRealTimeData: async () => {
    await DJIMobile.stopRecordRealTimeData();
  },

  startNewMediaFileListener: async () => {
    await DJIMobile.startNewMediaFileListener();
    return DJIEventSubject.pipe(filter(evt => evt.type === 'newMediaFile')).asObservable();
  },
  stopNewMediaFileListener: async () => {
    await DJIMobile.stopNotificationCenterListener('DJICameraEvent.didGenerateNewMediaFile');
  },

};

export default DJIMobileWrapper;

export {
  DJIMissionControl,
};
