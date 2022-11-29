import { Platform, NativeModules, PermissionsAndroid } from 'react-native';

import {
  filter as $filter,
  map as $map,
  distinctUntilChanged,
} from 'rxjs/operators';

import DJIMissionControl from './lib/DJIMissionControl';

import CameraControl, {
  photoAspectRatioLookup,
  readableExposureModes,
} from './lib/CameraControl';

import DJIMediaControl from './lib/DJIMedia';

import DJIFlightController from './lib/DJIFlightController';

import DJIGimbal from './lib/DJIGimbal';

import { DJIEventSubject, observeEvent } from './lib/utilities';
import { parseExposureSettings } from './lib/utilities/parseExposureSettings';
import {
  Attitude,
  LocationCoordinate3D,
  VelocityVector,
  HomeLocationCoordinate3D,
  FlightLogListenerEvent,
  MediaFileData,
  DJIDiagnostic,
  IMUState,
  WhiteBalancePresets,
  CameraExposureSettings,
  RemoteControllerFlightMode,
  AircraftFlightMode,
  ModelName,
} from './types';
import {
  PhotoFileFormat,
  PhotoAspectRatio,
  DjiExposureModes,
  DjiPhotoAspectRatio,
} from './lib/CameraControl/types';

const startListener =
  <T>(eventName: string) =>
  async () => {
    await DJIMobile.startEventListener(eventName);
    return observeEvent<T>(eventName);
  };

const stopListener = (eventName: string) => async () => {
  await DJIMobile.stopEventListener(eventName);
};

const { DJIMobile } = NativeModules;

let SDKRegistered = false;

const throwIfSDKNotRegistered = () => {
  if (SDKRegistered === false) {
    throw new Error('DJI SDK not registered!');
  }
};

const DJIMobileWrapper = {
  registerApp: async (bridgeIp?: string) => {
    if (Platform.OS === 'android') {
      const granted = await PermissionsAndroid.request(
        PermissionsAndroid.PERMISSIONS.READ_PHONE_STATE,
        // {
        //   title: 'Permission Required',
        //   message: 'The phone permission is required to allow the DJI drone functionality to work',
        //   buttonPositive: 'Ok',
        // }
      );
      if (granted !== PermissionsAndroid.RESULTS.GRANTED) {
        throw new Error('Missing READ_PHONE_STATE permission');
      }
    }

    let registerSDKPromise;
    if (bridgeIp !== undefined) {
      registerSDKPromise = DJIMobile.registerAppAndUseBridge(bridgeIp);
    } else {
      registerSDKPromise = DJIMobile.registerApp();
    }
    registerSDKPromise
      .then(() => {
        SDKRegistered = true;
      })
      .catch(err => {
        SDKRegistered = false;
      });
    return registerSDKPromise;
  },

  limitEventFrequency: async (frequency: number) => {
    if (frequency < 0 || frequency > 10) {
      throw Error('Please ensure frequency is in the range (0, 10]');
    } else {
      await DJIMobile.limitEventFrequency(frequency);
    }
  },

  // TODO: (Adam) What should happen if these functions are called and the SDK is not registered?

  startProductConnectionListener: startListener<string>(
    DJIMobile.ProductConnection,
  ),
  stopProductConnectionListener: stopListener(DJIMobile.ProductConnection),
  observeProductionConnection: observeEvent<string>(
    DJIMobile.ProductConnection,
  ),

  startCameraConnectionListener: startListener<boolean>(
    DJIMobile.CameraConnection,
  ),
  stopCameraConnectionListener: stopListener(DJIMobile.CameraConnection),
  observeCameraConnection: observeEvent<boolean>(DJIMobile.CameraConnection),

  startBatteryPercentChargeRemainingListener: startListener<number>(
    DJIMobile.BatteryChargeRemaining,
  ),
  stopBatteryPercentChargeRemainingListener: stopListener(
    DJIMobile.BatteryChargeRemaining,
  ),
  observeBatteryPercentChargeRemaining: observeEvent<number>(
    DJIMobile.BatteryChargeRemaining,
  ),

  startAircraftLocationListener: startListener<LocationCoordinate3D>(
    DJIMobile.AircraftLocation,
  ),
  stopAircraftLocationListener: stopListener(DJIMobile.AircraftLocation),
  observeAircraftLocation: observeEvent<LocationCoordinate3D>(
    DJIMobile.AircraftLocation,
  ),

  startAircraftVelocityListener: startListener<VelocityVector>(
    DJIMobile.AircraftVelocity,
  ),
  stopAircraftVelocityListener: stopListener(DJIMobile.AircraftVelocity),
  observeAircraftVelocity: observeEvent<VelocityVector>(
    DJIMobile.AircraftVelocity,
  ),

  startAircraftAttitudeListener: startListener<Attitude>(
    DJIMobile.AircraftAttitude,
  ),
  stopAircraftAttitudeListener: stopListener(DJIMobile.AircraftAttitude),
  observeAircraftAttitude: observeEvent<Attitude>(DJIMobile.AircraftAttitude),

  startAircraftCompassHeadingListener: startListener<number>(
    DJIMobile.AircraftCompassHeading,
  ),
  stopAircraftCompassHeadingListener: stopListener(
    DJIMobile.AircraftCompassHeading,
  ),
  observeAircraftCompassHeading: observeEvent<number>(
    DJIMobile.AircraftCompassHeading,
  ),

  startAirLinkUplinkSignalQualityListener: startListener<number>(
    DJIMobile.AirLinkUplinkSignalQuality,
  ),
  stopAirLinkUplinkSignalQualityListener: stopListener(
    DJIMobile.AirLinkUplinkSignalQuality,
  ),
  observeAirlinkUplinkSignalQuality: observeEvent<number>(
    DJIMobile.AirLinkUplinkSignalQuality,
  ),

  startAirLinkDownlinkSignalQualityListener: startListener<number>(
    DJIMobile.AirLinkDownlinkSignalQuality,
  ),
  stopAirLinkDownlinkSignalQualityListener: stopListener(
    DJIMobile.AirLinkDownlinkSignalQuality,
  ),
  observeAirlinkDownlinkSignalQuality: observeEvent<number>(
    DJIMobile.AirLinkDownlinkSignalQuality,
  ),

  startHomeLocationListener: startListener<HomeLocationCoordinate3D>(
    DJIMobile.AircraftHomeLocation,
  ),
  stopHomeLocationListener: stopListener(DJIMobile.AircraftHomeLocation),
  observeHomeLocation: observeEvent<HomeLocationCoordinate3D>(
    DJIMobile.AircraftHomeLocation,
  ),

  startIsHomeLocationSetListener: startListener<boolean>(
    DJIMobile.IsHomeLocationSet,
  ),
  stopIsHomeLocationSetListener: stopListener(DJIMobile.IsHomeLocationSet),
  observeIsHomeLocationSet: observeEvent<boolean>(DJIMobile.IsHomeLocationSet),

  startGpsSignalLevelListener: startListener<number | null>(
    DJIMobile.AircraftGpsSignalLevel,
  ),
  stopGpsSignalLevelListener: stopListener(DJIMobile.AircraftGpsSignalLevel),
  observeGpsSignalLevel: observeEvent<number | null>(
    DJIMobile.AircraftGpsSignalLevel,
  ),

  startSatelliteCountListener: startListener<number | null>(
    DJIMobile.SatelliteCount,
  ),
  stopSatelliteCountListener: stopListener(DJIMobile.SatelliteCount),
  observeSatelliteCount: observeEvent<number | null>(DJIMobile.SatelliteCount),

  startUltrasonicHeightListener: startListener(
    DJIMobile.AircraftUltrasonicHeight,
  ),
  stopUltrasonicHeightListener: stopListener(
    DJIMobile.AircraftUltrasonicHeight,
  ),

  startCompassHasErrorListener: startListener<boolean>(
    DJIMobile.CompassHasError,
  ),
  stopCompassHasErrorListener: stopListener(DJIMobile.CompassHasError),
  observeCompassHasError: observeEvent<boolean>(DJIMobile.CompassHasError),

  startCameraIsRecordingListener: startListener(DJIMobile.CameraIsRecording),
  stopCameraIsRecordingListener: stopListener(DJIMobile.CameraIsRecording),

  startSDCardIsInsertedListener: startListener(DJIMobile.SDCardIsInserted),
  stopSDCardIsInsertedListener: stopListener(DJIMobile.SDCardIsInserted),

  startSDCardIsReadOnlyListener: startListener(DJIMobile.SDCardIsReadOnly),
  stopSDCardIsReadOnlyListener: stopListener(DJIMobile.SDCardIsReadOnly),

  startSDCardAvailableCaptureCountListener: startListener<number | null>(
    DJIMobile.SDCardAvailableCaptureCount,
  ),
  stopSDCardAvailableCaptureCountListener: stopListener(
    DJIMobile.SDCardAvailableCaptureCount,
  ),
  observeSDCardAvailableCaptureCount: observeEvent<number | null>(
    DJIMobile.SDCardAvailableCaptureCount,
  ),

  startCameraWhiteBalanceListener: startListener<WhiteBalancePresets>(
    DJIMobile.CameraWhiteBalance,
  ),
  stopCameraWhiteBalanceListener: stopListener(DJIMobile.CameraWhiteBalance),
  observeCameraWhiteBalance: observeEvent<WhiteBalancePresets>(
    DJIMobile.CameraWhiteBalance,
  ),

  startPhotoFileFormatListener: startListener<PhotoFileFormat>(
    DJIMobile.CameraPhotoFileFormat,
  ),
  stopPhotoFileFormatListener: stopListener(DJIMobile.CameraPhotoFileFormat),
  observePhotoFileFormat: observeEvent<PhotoFileFormat>(
    DJIMobile.CameraPhotoFileFormat,
  ),

  // FIXME: this start listener function will actually return a DjiPhotoAspectRatio
  startPhotoAspectRatioListener: startListener<PhotoAspectRatio>(
    DJIMobile.CameraPhotoAspectRatio,
  ),
  stopPhotoAspectRatioListener: stopListener(DJIMobile.CameraPhotoAspectRatio),
  observePhotoAspectRatio: observeEvent<DjiPhotoAspectRatio>(
    DJIMobile.CameraPhotoAspectRatio,
  ).pipe($map(ratio => photoAspectRatioLookup[ratio])),

  startCameraExposureModeListener: startListener<DjiExposureModes>(
    DJIMobile.CameraExposureMode,
  ),
  stopCameraExposureModeListener: stopListener(DJIMobile.CameraExposureMode),
  observeCameraExposureMode: observeEvent<DjiExposureModes>(
    DJIMobile.CameraExposureMode,
  ),

  startGimbalIsAtYawStopListener: startListener(DJIMobile.GimbalIsAtYawStop),
  stopGimbalIsAtYawStopListener: stopListener(DJIMobile.GimbalIsAtYawStop),

  startAircraftVirtualStickEnabledListener: startListener(
    DJIMobile.AircraftVirtualStickEnabled,
  ),
  stopAircraftVirtualStickEnabledListener: stopListener(
    DJIMobile.AircraftVirtualStickEnabled,
  ),

  startVisionDetectionStateListener: startListener(
    DJIMobile.VisionDetectionState,
  ),
  stopVisionDetectionStateListener: stopListener(
    DJIMobile.VisionDetectionState,
  ),

  startVisionControlStateListener: startListener(DJIMobile.VisionControlState),
  stopVisionControlStateListener: stopListener(DJIMobile.VisionControlState),

  startIsShootingSinglePhotoListener: startListener<boolean>(
    DJIMobile.CameraIsShootingSinglePhoto,
  ),
  stopIsShootingSinglePhotoListener: stopListener(
    DJIMobile.CameraIsShootingSinglePhoto,
  ),
  observeIsShootingSinglePhotoListener: observeEvent<boolean>(
    DJIMobile.CameraIsShootingSinglePhoto,
  ),

  startIsStoringPhotoListener: startListener<boolean>(
    DJIMobile.CameraIsStoringPhoto,
  ),
  stopIsStoringPhotoListener: stopListener(DJIMobile.CameraIsStoringPhoto),
  observeIsStoringPhotoListener: observeEvent<boolean>(
    DJIMobile.CameraIsStoringPhoto,
  ),

  startIsShootingPhotoListener: startListener<boolean>(
    DJIMobile.CameraIsShootingPhoto,
  ),
  stopIsShootingPhotoListener: stopListener(DJIMobile.CameraIsShootingPhoto),
  observeIsShootingPhotoListener: observeEvent<boolean>(
    DJIMobile.CameraIsShootingPhoto,
  ),

  startRemoteControllerFlightModeListener:
    startListener<RemoteControllerFlightMode>(
      DJIMobile.RemoteControllerFlightMode,
    ),
  stopRemoteControllerFlightModeListener: stopListener(
    DJIMobile.RemoteControllerFlightMode,
  ),
  observeRemoteControllerFlightMode: observeEvent<RemoteControllerFlightMode>(
    DJIMobile.RemoteControllerFlightMode,
  ),

  startAircraftFlightModeListener: startListener<AircraftFlightMode>(
    DJIMobile.AircraftFlightMode,
  ),
  stopAircraftFlightModeListener: stopListener(DJIMobile.AircraftFlightMode),
  observeAircraftFlightMode: observeEvent<AircraftFlightMode>(
    DJIMobile.AircraftFlightMode,
  ),

  startAircraftIsFlyingListener: startListener<boolean>(
    DJIMobile.AircraftIsFlying,
  ),
  stopAircraftIsFlyingListener: stopListener(DJIMobile.AircraftIsFlying),
  observeAircraftIsFlying: observeEvent<boolean>(DJIMobile.AircraftIsFlying),

  startDiagnosticsListener: async () => {
    await DJIMobile.resetPreviousDiagnostics();
    return observeEvent<DJIDiagnostic[]>('DJIDiagnostics');
  },
  stopDiagnosticsListener: async () => {
    await DJIMobile.stopEventListener('DJIDiagnostics');
  },
  observeDiagnostics: observeEvent<DJIDiagnostic[]>('DJIDiagnostics'),

  startCameraExposureSettingsListener: async () => {
    await DJIMobile.startCameraExposureSettingsListener();
    return observeEvent<CameraExposureSettings>(
      DJIMobile.CameraExposureSettings,
    ).pipe($map(value => parseExposureSettings(value)));
  },
  observeCameraExposureSettings: observeEvent<CameraExposureSettings>(
    DJIMobile.CameraExposureSettings,
  ).pipe($map(value => parseExposureSettings(value))),

  stopCameraExposureSettingsListener: async () => {
    await DJIMobile.stopEventListener('CameraExposureSettings');
  },

  getAircraftLocation: async () => {
    return await DJIMobile.getAircraftLocation();
  },

  startNewMediaFileListener: async () => {
    await DJIMobile.startNewMediaFileListener();
    return observeEvent<MediaFileData>('CameraDidGenerateNewMediaFile');
  },
  observeNewMediaFile: observeEvent<MediaFileData>(
    'CameraDidGenerateNewMediaFile',
  ),
  // TODO: this is working, but hasn't been tested thoroughly or properly typed
  observeCameraState: observeEvent<any>('CameraDidUpdateSystemState'),

  stopNewMediaFileListener: async () => {
    // TODO: (Adam) generalize & merge these!
    if (Platform.OS === 'ios') {
      await DJIMobile.stopNotificationCenterListener(
        'DJICameraEvent.didGenerateNewMediaFile',
      );
    } else {
      await DJIMobile.stopEventListener('CameraDidGenerateNewMediaFile');
    }
  },

  startIMUSensorStateListener: async () => {
    await DJIMobile.startIMUSensorStateListener();
    return observeEvent<IMUState>(DJIMobile.IMUSensorState);
  },
  observeIMUSensorState: observeEvent<IMUState>(DJIMobile.IMUSensorState).pipe(
    distinctUntilChanged(
      (prev, curr) =>
        prev.accelerometerState === curr.accelerometerState &&
        prev.gyroscopeState === curr.gyroscopeState,
    ),
  ),

  /**
   * ANDROID ONLY
   */
  getMediaFileList: async (numberOfResults?: number) => {
    if (numberOfResults != null) {
      return await DJIMobile.getLimitedMediaFileList(numberOfResults);
    } else {
      return await DJIMobile.getMediaFileList();
    }
  },

  getFlightLogPath: async () => {
    return await DJIMobile.getFlightLogPath();
  },

  startFlightLogListener: async () => {
    await DJIMobile.startFlightLogListener();
    return DJIEventSubject.pipe(
      $filter(
        (evt: FlightLogListenerEvent) => evt.type === 'DJIFlightLogEvent',
      ),
    );
  },

  stopFlightLogListener: async () => {
    await DJIMobile.stopFlightLogListener();
  },

  /**
   * ANDROID ONLY
   */
  getAircraftIsFlying: async () => {
    return await DJIMobile.getAircraftIsFlying();
  },

  setCollisionAvoidanceEnabled: async (enabled: boolean) => {
    return await DJIMobile.setCollisionAvoidanceEnabled(enabled);
  },

  getCollisionAvoidanceEnabled: async () => {
    return await DJIMobile.getCollisionAvoidanceEnabled();
  },

  setVirtualStickAdvancedModeEnabled: async (enabled: boolean) => {
    return await DJIMobile.setVirtualStickAdvancedModeEnabled(enabled);
  },

  setVisionAssistedPositioningEnabled: async (enabled: boolean) => {
    return await DJIMobile.setVisionAssistedPositioningEnabled(enabled);
  },

  setLandingProtectionEnabled: async (enabled: boolean) => {
    return await DJIMobile.setLandingProtectionEnabled(enabled);
  },

  isProductConnected: async () => {
    return await DJIMobile.isProductConnected();
  },

  getAircraftCompassHeading: async () => {
    return await DJIMobile.getAircraftCompassHeading();
  },

  getModelName: async (): Promise<ModelName> => {
    return await DJIMobile.getModelName();
  },
};

export default DJIMobileWrapper;

export {
  DJIMissionControl,
  CameraControl,
  DJIMediaControl,
  DJIFlightController,
  DJIGimbal,
};

export { readableExposureModes };

export * from './types';
export * from './lib/CameraControl/types';
