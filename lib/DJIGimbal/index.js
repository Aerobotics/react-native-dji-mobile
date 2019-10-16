// @flow strict

import {
  NativeModules,
} from 'react-native';
import {
  DJIEventSubject,
} from '../utilities';
import {
  filter as $filter,
} from 'rxjs/operators';

const {
  GimbalWrapper,
} = NativeModules;

type RotateParameters = {
  roll: number?,
  pitch: number?,
  yaw: number?,
  time: number,
}

const DJIGimbal = {
  rotate: async (parameters: RotateParameters) => {
    await GimbalWrapper.rotate(parameters);
  },
};

export default DJIGimbal;
