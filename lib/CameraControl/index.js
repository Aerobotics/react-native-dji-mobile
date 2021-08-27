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

type VideoFrameRates = '60' | '59.94' | '30' | '29.97';

const videoFrameRates = Object.freeze({
  '60': 'FRAME_RATE_60_FPS',
  '59.94': 'FRAME_RATE_59_DOT_940_FPS',
  '30' : 'FRAME_RATE_30_FPS',
  '29.97' : 'FRAME_RATE_29_DOT_970_FPS',
});

type ISOValues = 'AUTO' | '100' | '200' | '400' | '800' | '1600' | '3200' | '6400' | '12800' | '25600' | 'FIXED';

const isoValues = Object.freeze({
  '100': 'ISO_100',
  '200': 'ISO_200',
  '300': 'ISO_300',
  '400': 'ISO_400',
  '800': 'ISO_800',
  '1600': 'ISO_1600',
  '3200': 'ISO_3200',
  '6400': 'ISO_6400',
  '12800': 'ISO_12800',
  '25600': 'ISO_25600',
  'FIXED': 'FIXED',
  'AUTO': 'AUTO',
});

type ShutterSpeeds = '1/400' | '1/500' | '1/640' | '1/725' | '1/800' | '1/1000' | '1/1250' | '1/1500' | '1/1600' | '1/2000' | '1/2500' | '1/3000' | '1/3200' | '1/4000';

const shutterSpeeds = Object.freeze({
  '1/400': 'SHUTTER_SPEED_1_400',
  '1/500': 'SHUTTER_SPEED_1_500',
  '1/640' : 'SHUTTER_SPEED_1_640',
  '1/725': 'SHUTTER_SPEED_1_725',
  '1/800': 'SHUTTER_SPEED_1_800',
  '1/1000': 'SHUTTER_SPEED_1_1000',
  '1/1250': 'SHUTTER_SPEED_1_1250',
  '1/1500': 'SHUTTER_SPEED_1_1500',
  '1/1600': 'SHUTTER_SPEED_1_1600',
  '1/2000': 'SHUTTER_SPEED_1_2000',
  '1/2500': 'SHUTTER_SPEED_1_2500',
  '1/3000': 'SHUTTER_SPEED_1_3000',
  '1/3200': 'SHUTTER_SPEED_1_3200',
  '1/4000': 'SHUTTER_SPEED_1_4000',
});

type CameraModes = 'shootPhoto' | 'recordVideo' | 'playback' | 'mediaDownload';

const cameraModes = Object.freeze({
  'shootPhoto': 'SHOOT_PHOTO',
  'recordVideo': 'RECORD_VIDEO',
  'playback': 'PLAYBACK',
  'mediaDownload': 'MEDIA_DOWNLOAD',
});

type CameraColors = 'DLOG' | 'DCINELIKE'

const cameraColors = Object.freeze({
  'DLog': 'D_LOG',
  'DCinelike': 'D_CINELIKE'
});

type FocusModes = 'manual' | 'auto' | 'afc'

const focusModes = Object.freeze({
  'manual': 'MANUAL',
  'auto': 'AUTO',
  'afc': 'AFC',
});

type VideoStandard = 'pal' | 'ntsc' | 'unknown'

const videoStandards = Object.freeze({
  'pal': 'PAL',
  'ntsc': 'NTSC',
  'unknown': 'UNKNOWN',
});

type ExposureCompensationValues = '-5.0' | '-4.7' | '-4.3' | '-4.0' | '-3.7' | '-3.3' | '-3.0' | '-2.7' | '-2.3' | '-2.0' | '-1.7' | '-1.3' | '-1.0' | '-0.7' | '-0.3' | '0.0' | '0.3' | '0.7' | '1.0' | '1.3' | '1.7' | '2.0' | '2.3' | '2.7' | '3.0' | '3.3' | '3.7' | '4.0' | '5.0' | 'FIXED'
const exposureCompensationValues = Object.freeze({
  '-5.0': 'N_5_0',
  '-4.7': 'N_4_7',
  '-4.3': 'N_4_3',
  '-4.0': 'N_4_0',
  '-3.7': 'N_3_7',
  '-3.3': 'N_3_3',
  '-3.0': 'N_3_0',
  '-2.7': 'N_2_7',
  '-2.3': 'N_2_3',
  '-2.0': 'N_2_0',
  '-1.7': 'N_1_7',
  '-1.3': 'N_1_3',
  '-1.0': 'N_1_0',
  '-0.7': 'N_0_7',
  '-0.3': 'N_0_3',
  '0.0':  'N_0_0',
  '0.3': 'P_0_3',
  '0.7': 'P_0_7',
  '1.0': 'P_1_0',
  '1.3': 'P_1_3',
  '1.7': 'P_1_7',
  '2.0': 'P_2_0',
  '2.3': 'P_2_3',
  '2.7': 'P_2_7',
  '3.0': 'P_3_0',
  '3.3': 'P_3_3',
  '3.7': 'P_3_7',
  '4.0': 'P_4_0',
  '5.0': 'P_5_0',
  'FIXED': 'FIXED'
})

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

  startRecording: async () => {
    return await CameraControlNative.startRecording();
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
  },
  setISO: async (isoValue: ISOValues) => {
    return await CameraControlNative.setISO(isoValues[isoValue]);
  },
  setShutterSpeed: async (shutterSpeed: ShutterSpeeds) => {
    return await CameraControlNative.setShutterSpeed(shutterSpeeds[shutterSpeed]);
  },
  setCameraMode: async (cameraMode: CameraModes) => {
    return await CameraControlNative.setCameraMode(cameraModes[cameraMode]);
  },
  setVideoCaptionsEnabled: async (enabled: boolean) => {
    return await CameraControlNative.setVideoCaptionsEnabled(enabled);
  },
  setCameraColor: async (cameraColor: CameraColors) => {
    return await CameraControlNative.setCameraColor(cameraColors[cameraColor]);
  },
  setSharpness: async (sharpness: number) => {
    return await CameraControlNative.setSharpness(sharpness);
  },
  setContrast: async (contrast: number) => {
    return await CameraControlNative.setContrast(contrast);
  },
  setSaturation: async (saturation: number) => {
    return await CameraControlNative.setSaturation(saturation);
  },
  setFocusMode: async (focusMode: FocusModes) => {
    return await CameraControlNative.setFocusMode(focusModes[focusMode]);
  },
  setFocusTarget: async (x: number, y: number) => {
    return await CameraControlNative.setFocusTarget(x, y);
  },
  setVideoStandard: async (videoStandard: VideoStandard) => {
    return await CameraControlNative.setVideoStandard(videoStandards[videoStandard]);
  },
  getVideoStandard: async () => {
    return await CameraControlNative.getVideoStandard();
  },
  getFocusStatus: async () => {
    return await CameraControlNative.getFocusStatus();
  },
  getFocusRingValue: async () => {
    return await CameraControlNative.getFocusRingValue();
  },
  getDisplayName: async () => {
    return await CameraControlNative.getDisplayName();
  },
  isRecording: async () => {
    return await CameraControlNative.isRecording();
  },
  setExposureCompensation: async (exposureCompensationValue: ExposureCompensationValues) => {
    return await CameraControlNative.setExposureCompensation(exposureCompensationValues[exposureCompensationValue]);
  },
  getExposureCompensation: async (): ExposureCompensationValues => {
    const djiExposureCompensationValue = await CameraControlNative.getExposureCompensation();
    // map the DJI exposure value string to a ExposureCompensationValues type
    return Object.keys(exposureCompensationValues).find(key => exposureCompensationValues[key] === djiExposureCompensationValue)
  }
};

export default CameraControl;
