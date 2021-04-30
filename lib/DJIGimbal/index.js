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
  roll: ?number,
  pitch: ?number,
  yaw: ?number,
  time: ?number,
}

const gimbalModes = Object.freeze({
  'FREE': 'FREE',
  'FPV': 'FPV',
  'YAW_FOLLOW': 'YAW_FOLLOW',
});

const DJIGimbal = {
  getGimbalAttitude: async () => {
    return await GimbalWrapper.getGimbalAttitude();
  },
  rotate: async (parameters: RotateParameters) => {
    return await GimbalWrapper.rotate(parameters);
  },
  getMode: async () => {
    return await GimbalWrapper.getMode();
  },
  setMode: async(gimbalModeName: $Keys<typeof gimbalModes>) => {
    return await GimbalWrapper.setMode(gimbalModes[gimbalModeName]);
  },
  resetPose: async() => {
    return await GimbalWrapper.resetPose();
  }
};

export default DJIGimbal;
