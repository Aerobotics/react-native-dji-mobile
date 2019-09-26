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
  '4_3': 'RATIO_4_3',
  '16_9': 'RATIO_16_9',
  '3_2': 'RATIO_3_2',
});

type WhiteBalanceParameters = {
  preset?: 'auto' | 'sunny' | 'cloudy' | 'waterSurface' | 'indoorIncandescent' | 'indoorFluorescent',
  colorTemperature?: number, // in range [20, 100]
}

const whiteBalancePresets = Object.freeze({
  auto: 'AUTO',
  sunny: 'SUNNY',
  cloudy: 'CLOUDY',
  waterSurface: 'WATER_SURFACE',
  indoorIncandescent: 'INDOOR_INCANDESCENT',
  indoorFluorescent: 'INDOOR_FLUORESCENT',
});

type ExposureMode = 'program' | 'shutterPriority' | 'aperturePriority' | 'manual';

const exposureModes = Object.freeze({
  'program': 'PROGRAM',
  'shutterPriority': 'SHUTTER_PRIORITY',
  'aperturePriority': 'APERTURE_PRIORITY',
  'manual': 'MANUAL',
});

type VideoFileFormat = 'mov' | 'mp4' | 'tiffSequence' | 'seq';

const videoFileFormats = Object.freeze({
  'mov': 'MOV',
  'mp4': 'MP4',
  'tiffSequence': 'TIFF_SEQ',
  'seq': 'SEQ',
});

type VideoFileCompressionStandard = 'H264' | 'H265';

const videoFileCompressionStandards = Object.freeze({
  'H264': 'H264',
  'H265': 'H265',
});

type VideoResolutions = '4096x2160' | '4608x2160' | '4608x2592' | '3840x2160';

const videoResolutions = Object.freeze({
  '4096x2160': 'RESOLUTION_4096x2160',
  '4608x2160': 'RESOLUTION_4608x2160',
  '4608x2592': 'RESOLUTION_4608x2592',
  '3840x2160': 'RESOLUTION_3840x2160',
});

type VideoFrameRates = '60' | '59.94';

const videoFrameRates = Object.freeze({
  '60': 'FRAME_RATE_60_FPS',
  '59.94': 'FRAME_RATE_59_DOT_940_FPS',
});

const CameraControl = {

  setPhotoAspectRatio: async (photoAspectRatio: PhotoAspectRatio) => {
    return await CameraControlNative.setPhotoAspectRatio(photoAspectRatios[photoAspectRatio]);
  },

  setWhiteBalance: async (parameters: WhiteBalanceParameters) => {
    if (parameters.preset != null) {
      parameters.preset = whiteBalancePresets[parameters.preset];
    }
    return await CameraControlNative.setWhiteBalance(parameters);
  },

  setExposureMode: async (exposureMode: ExposureMode) => {
    return await CameraControlNative.setExposureMode(exposureModes[exposureMode]);
  },

  stopRecording: async () => {
    return await CameraControlNative.stopRecording();
  },

  setVideoFileFormat: async (videoFileFormat: VideoFileFormat) => {
    return await CameraControlNative.setVideoFileFormat(videoFileFormats[videoFileFormat]);
  },

  setVideoFileCompressionStandard: async (videoFileCompressionStandard: VideoFileCompressionStandard) => {
    return await CameraControlNative.setVideoFileCompressionStandard(videoFileCompressionStandards[videoFileCompressionStandard]);
  },
  setVideoResolutionAndFrameRate: async (videoResolution: VideoResolutions, videoFrameRate: VideoFrameRates) => {
    return await CameraControlNative.setVideoResolutionAndFrameRate(videoResolutions[videoResolution], videoFrameRates[videoFrameRate]);
  },
  getVideoResolutionAndFrameRateRange: async () => {
    return await CameraControlNative.getVideoResolutionAndFrameRateRange();
  },
  isSDCardInserted: async () => {
    return await CameraControlNative.isSDCardInserted();
  }
};

export default CameraControl;
