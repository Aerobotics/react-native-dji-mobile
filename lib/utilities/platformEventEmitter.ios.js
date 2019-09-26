// @flow strict

import {
  NativeModules,
  NativeEventEmitter,
} from 'react-native';

const {
  EventSender,
} = NativeModules;

const DJIMobileEmitter = new NativeEventEmitter(EventSender);

export default DJIMobileEmitter;
