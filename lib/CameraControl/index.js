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

type VideoFileFormat = 'mov' | 'mp4' | 'tiffSeq' | 'seq';

const videoFileFormats = Object.freeze({
  'mov': {
    ios: 'DJICameraVideoFileFormatMOV',
    android: 'MOV',
  },
  'mp4': {
    ios: 'DJICameraVideoFileFormatMP4',
    android: 'MP4',
  },
  'tiffSeq': {
    ios: 'DJICameraVideoFileFormatTIFFSequence',
    android: 'TIFF_SEQ',
  },
  'seq': {
    ios: 'DJICameraVideoFileFormatSEQ',
    android: 'SEQ',
  }
});

type VideoFileCompressionStandard = 'H264' | 'H265';

const videoFileCompressionStandards = Object.freeze({
  'H264': {
    ios: 'DJIVideoFileCompressionStandardH264',
    android: 'H264',
  },
  'H265': {
    ios: 'DJIVideoFileCompressionStandardH265',
    android: 'H265',
  }
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
  },

  setVideoFileFormat: async (videoFileFormat: VideoFileFormat) => {
    return await CameraControlNative.setVideoFileFormat(videoFileFormats[videoFileFormat][Platform.OS]);
  },

  setVideoFileCompressionStandard: async (videoFileCompressionStandard: VideoFileCompressionStandard) => {
    return await CameraControlNative.setVideoFileCompressionStandard(videoFileCompressionStandards[videoFileCompressionStandard][Platform.OS]);
  }
};

export default CameraControl;
