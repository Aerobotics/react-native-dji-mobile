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

PlatformEventEmitter.addListener('DJIEvent', evt => {
  DJIEventSubject.next(evt);
});

const DJIMobileWrapper = {
  
  registerApp: () => {
    return DJIMobile.registerApp();
  },

  startProductConnectionListener: async () => {
    await DJIMobile.startProductConnectionListener();
    return DJIEventSubject.pipe(filter(evt => evt.type === 'connectionStatus')).asObservable();
  },
  stopProductConnectionListener: async () => {
    await DJIMobile.stopProductConnectionListener();
  },

  startBatteryPercentChargeRemainingListener: async () => {
    await DJIMobile.startBatteryPercentChargeRemainingListener();
    return DJIEventSubject.pipe(filter(evt => evt.type === 'chargeRemaining')).asObservable();
  },
  stopBatteryPercentChargeRemainingListener: async () => {
    await DJIMobile.stopBatteryPercentChargeRemainingListener();
  },
};

export default DJIMobileWrapper;
