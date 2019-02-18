import {
  Platform,
  NativeModules,
} from 'react-native';

import PlatformEventEmitter from './platformEventEmitter';

import {
  Subject,
} from 'rxjs';

import {
  filter,
} from 'rxjs/operators';
import { async } from 'rxjs/internal/scheduler/async';

const {
  DJIMobile,
} = NativeModules;

const DJIEventSubject = new Subject();

// DJIEventSubject.subscribe(evt => console.log(evt));

PlatformEventEmitter.addListener('DJIEvent', evt => {
  DJIEventSubject.next(evt);
});

let SDKRegistered = false;

const DJIMobileWrapper = {
  
  registerApp: () => {
    const registerPromise = DJIMobile.registerApp();
    registerPromise.then(() => SDKRegistered = true).catch(() => SDKRegistered = false);
    return registerPromise
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

  startAircraftCompassHeadingListener: async() => {
    await DJIMobile.startAircraftCompassHeadingListener();
    return DJIEventSubject.pipe(filter(evt => evt.type === 'aircraftCompassHeading')).asObservable();
  },
  stopAircraftCompassHeadingListener: async () => {
    await DJIMobile.stopKeyListener('DJIFlightControllerParamCompassHeading');
  }
};

export default DJIMobileWrapper;
