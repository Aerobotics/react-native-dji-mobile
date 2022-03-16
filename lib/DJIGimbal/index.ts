import {
  NativeModules,
} from 'react-native';

const {
  GimbalWrapper,
} = NativeModules;

type RotateParameters = {
  roll?: number,
  pitch?: number,
  yaw?: number,
  time?: number,
}

type GimbalModes =
  'FREE' |
  'FPV' |
  'YAW_FOLLOW'

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
  setMode: async(gimbalModeName: GimbalModes) => {
    return await GimbalWrapper.setMode(gimbalModeName);
  },
  resetPose: async() => {
    return await GimbalWrapper.resetPose();
  },
  isYawAtLimit: async() => {
    return await GimbalWrapper.isYawAtLimit();
  }
};

export default DJIGimbal;
