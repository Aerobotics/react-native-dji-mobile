import {
  NativeModules,
  NativeEventEmitter,
} from 'react-native';

const {
  DJIMobile,
} = NativeModules;

const DJIMobileEmitter = new NativeEventEmitter(DJIMobile);

export default DJIMobileEmitter;
