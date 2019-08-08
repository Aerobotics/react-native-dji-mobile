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
    ios: CameraControlNative.AspectRatios['4_3'],
    android: 'RATIO_4_3',
  },
  '16_9': {
    ios: CameraControlNative.AspectRatios['16_9'],
    android: 'RATIO_16_9',
  },
  '3_2': {
    ios: CameraControlNative.AspectRatios['3_2'],
    android: 'RATIO_3_2',
  },
});

type WhiteBalanceParameters = {
  preset?: 'auto' | 'sunny' | 'cloudy' | 'waterSurface' | 'indoorIncandescent' | 'indoorFluorescent',
  colorTemperature?: number, // in range [20, 100]
}

const whiteBalancePresets = Object.freeze({
  auto: {
    ios: CameraControlNative.AspectRatios['auto'],
    android: 'AUTO',
  },
  sunny: {
    ios: CameraControlNative.AspectRatios['sunny'],
    android: 'SUNNY',
  },
  cloudy: {
    ios: CameraControlNative.AspectRatios['cloudy'],
    android: 'CLOUDY',
  },
  waterSurface: {
    ios: CameraControlNative.AspectRatios['waterSurface'],
    android: 'WATER_SURFACE',
  },
  indoorIncandescent: {
    ios: CameraControlNative.AspectRatios['indoorIncandescent'],
    android: 'INDOOR_INCANDESCENT',
  },
  indoorFluorescent: {
    ios: CameraControlNative.AspectRatios['indoorFluorescent'],
    android: 'INDOOR_FLUORESCENT',
  },
});

type ExposureMode = 'program' | 'shutterPriority' | 'aperturePriority' | 'manual';

const exposureModes = Object.freeze({
  'program': {
    ios: CameraControlNative.ExposureModes['program'],
    android: 'PROGRAM',
  },
  'shutterPriority': {
    ios: CameraControlNative.ExposureModes['shutterPriority'],
    android: 'SHUTTER_PRIORITY',
  },
  'aperturePriority': {
    ios: CameraControlNative.ExposureModes['aperturePriority'],
    android: 'APERTURE_PRIORITY',
  },
  'manual': {
    ios: CameraControlNative.ExposureModes['manual'],
    android: 'MANUAL',
  },
});

type VideoFileFormat = 'mov' | 'mp4' | 'tiffSequence' | 'seq';

const videoFileFormats = Object.freeze({
  'mov': {
    ios: CameraControlNative.VideoFileFormats['mov'],
    android: 'MOV',
  },
  'mp4': {
    ios: CameraControlNative.VideoFileFormats['mp4'],
    android: 'MP4',
  },
  'tiffSequence': {
    ios: CameraControlNative.VideoFileFormats['tiffSequence'],
    android: 'TIFF_SEQ',
  },
  'seq': {
    ios: CameraControlNative.VideoFileFormats['seq'],
    android: 'SEQ',
  },
});

type VideoFileCompressionStandard = 'H264' | 'H265';

const videoFileCompressionStandards = Object.freeze({
  'H264': {
    ios: CameraControlNative.VideoFileCompressionStandards['h264'],
    android: 'H264',
  },
  'H265': {
    ios: CameraControlNative.VideoFileCompressionStandards['h265'],
    android: 'H265',
  },
});

type VideoResolutions = '4096x2160' | '4608x2160' | '4608x2592' | '3840x2160';

const videoResolutions = Object.freeze({
  '4096x2160': {
    ios: CameraControlNative.VideoResolutions['4096x2160'],
    android: 'RESOLUTION_4096x2160',
  },
  '4608x2160': {
    ios: CameraControlNative.VideoResolutions['4608x2160'],
    android: 'RESOLUTION_4608x2160',
  },
  '4608x2592': {
    ios: CameraControlNative.VideoResolutions['4608x2592'],
    android: 'RESOLUTION_4608x2592',
  },
  '3840x2160': {
    ios: CameraControlNative.VideoResolutions['3840x2160'],
    android: 'RESOLUTION_3840x2160',
  },
});

type VideoFrameRates = '60' | '59.94';

const videoFrameRates = Object.freeze({
  '60': {
    ios: CameraControlNative.VideoFrameRates['60'],
    android: 'FRAME_RATE_60_FPS',
  },
  '59.94': {
    ios: CameraControlNative.VideoFrameRates['59.940'],
    android: 'FRAME_RATE_59_DOT_940_FPS',
  },
});

const CameraControl = {

  setPhotoAspectRatio: async (photoAspectRatio: PhotoAspectRatio) => {
    return await CameraControlNative.setPhotoAspectRatio(photoAspectRatios[photoAspectRatio][Platform.OS]);
  },

  setWhiteBalance: async (parameters: WhiteBalanceParameters) => {
    if (parameters.preset != null) {
      parameters.preset = whiteBalancePresets[parameters.preset][Platform.OS];
    }
    return await CameraControlNative.setWhiteBalance(parameters);
  },

  setExposureMode: async (exposureMode: ExposureMode) => {
    return await CameraControlNative.setExposureMode(exposureModes[exposureMode][Platform.OS]);
  },

  stopRecording: async () => {
    return await CameraControlNative.stopRecording();
  },

  setVideoFileFormat: async (videoFileFormat: VideoFileFormat) => {
    return await CameraControlNative.setVideoFileFormat(videoFileFormats[videoFileFormat][Platform.OS]);
  },

  setVideoFileCompressionStandard: async (videoFileCompressionStandard: VideoFileCompressionStandard) => {
    return await CameraControlNative.setVideoFileCompressionStandard(videoFileCompressionStandards[videoFileCompressionStandard][Platform.OS]);
  },
  setVideoResolutionAndFrameRate: async (videoResolution: VideoResolutions, videoFrameRate: VideoFrameRates) => {
    return await CameraControlNative.setVideoResolutionAndFrameRate(videoResolutions[videoResolution][Platform.OS], videoFrameRates[videoFrameRate][Platform.OS]);
  },
  getVideoResolutionAndFrameRateRange: async () => {
    return await CameraControlNative.getVideoResolutionAndFrameRateRange();
  },
};

export default CameraControl;
