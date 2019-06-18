// @flow strict

import {
  NativeModules,
  Platform,
} from 'react-native';

const {
  CameraControlNative,
} = NativeModules;


type PhotoAspectRatio = '4_3' | '16_9' | '3_2';

const photoAspectRatios = Object.freeze({
  '4_3': {
    ios: 0,
    android: 'RATIO_4_3',
  },
  '16_9': {
    ios: 1,
    android: 'RATIO_16_9',
  },
  '3_2': {
    ios: 2,
    android: 'RATIO_3_2',
  },
});

type WhiteBalanceParameters = {
  preset?: 'auto' | 'sunny' | 'cloudy' | 'waterSurface' | 'indoorIncandescent' | 'indoorFluorescent',
  colorTemperature?: number, // in range [20, 100]
}

const whiteBalancePresets = Object.freeze({
  auto: {
    ios: 0,
    android: 'AUTO',
  },
  sunny: {
    ios: 1,
    android: 'SUNNY',
  },
  cloudy: {
    ios: 2,
    android: 'CLOUDY',
  },
  waterSurface: {
    ios: 3,
    android: 'WATER_SURFACE',
  },
  indoorIncandescent: {
    ios: 4,
    android: 'INDOOR_INCANDESCENT',
  },
  indoorFluorescent: {
    ios: 5,
    android: 'INDOOR_FLUORESCENT',
  },
});

type ExposureMode = 'program' | 'shutterPriority' | 'aperturePriority' | 'manual';

const exposureModes = Object.freeze({
  'program': {
    ios: 0,
    android: 'PROGRAM',
  },
  'shutterPriority': {
    ios: 1,
    android: 'SHUTTER_PRIORITY',
  },
  'aperturePriority': {
    ios: 2,
    android: 'APERTURE_PRIORITY',
  },
  'manual': {
    ios: 2,
    android: 'MANUAL',
  },
});

const CameraControl = {

  setPhotoAspectRatio: async (photoAspectRatio: PhotoAspectRatio) => {
    await CameraControlNative.setPhotoAspectRatio(photoAspectRatios[photoAspectRatio][Platform.OS]);
  },

  setWhiteBalance: async (parameters: WhiteBalanceParameters) => {
    if (parameters.preset != null) {
      parameters.preset = whiteBalancePresets[parameters.preset][Platform.OS];
    }
    await CameraControlNative.setWhiteBalance(parameters);
  },

  setExposureMode: async (exposureMode: ExposureMode) => {
    await CameraControlNative.setExposureMode(exposureModes[exposureMode][Platform.OS]);
  },

  stopRecording: async () => {
  return await CameraControlNative.stopRecording();
  }
};

export default CameraControl;
