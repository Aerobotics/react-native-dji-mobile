import {
  Platform,
  NativeModules,
  PermissionsAndroid,
} from 'react-native';

import {
  filter as $filter,
  map as $map,
} from 'rxjs/operators';

import DJIMissionControl from './lib/DJIMissionControl';

import CameraControl, { exposureCompensationValues } from './lib/CameraControl';

import DJIMediaControl from './lib/DJIMedia';

import DJIFlightController from './lib/DJIFlightController';

import DJIGimbal from './lib/DJIGimbal';

import {
  DJIEventSubject,
  observeEvent,
} from './lib/utilities';
import { Observable } from 'rxjs';
import { Attitude, LocationCoordinate3D, VelocityVector } from './types';

const startListener = <T>(eventName: string): () => Promise<Observable<T>> => async () => {
  await DJIMobile.startEventListener(eventName);
  return observeEvent(eventName);
};

const stopListener = (eventName: string) => async () => {
  await DJIMobile.stopEventListener(eventName);
};

const {
  DJIMobile,
} = NativeModules;

let SDKRegistered = false;

const throwIfSDKNotRegistered = () => {
  if (SDKRegistered === false) {
    throw new Error('DJI SDK not registered!');
  }
};

export const FlightLogListenerEventNames = Object.freeze({
  create: 'create',
  modify: 'modify',
});

export type FlightLogListenerEvent = {
  value: {
    eventName: typeof FlightLogListenerEventNames,
    fileName: string,
  },
  type: string,
}

export type MediaFileData = {
  fileName: string,
  fileSizeInBytes: number,
  dateCreated: string,
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
    registerSDKPromise.then(() => {
      SDKRegistered = true;
    }).catch(err => {
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

  startProductConnectionListener: startListener<string>(DJIMobile.ProductConnection),
  stopProductConnectionListener: stopListener(DJIMobile.ProductConnection),
  observeProductionConnection: observeEvent<string>(DJIMobile.ProductConnection),

  startBatteryPercentChargeRemainingListener: startListener<number>(DJIMobile.BatteryChargeRemaining),
  stopBatteryPercentChargeRemainingListener: stopListener(DJIMobile.BatteryChargeRemaining),
  observeBatteryPercentChargeRemaining: observeEvent<number>(DJIMobile.BatteryChargeRemaining),

  startAircraftLocationListener: startListener<LocationCoordinate3D>(DJIMobile.AircraftLocation),
  stopAircraftLocationListener: stopListener(DJIMobile.AircraftLocation),
  observeAircraftLocation: observeEvent<LocationCoordinate3D>(DJIMobile.AircraftLocation),

  startAircraftVelocityListener: startListener<VelocityVector>(DJIMobile.AircraftVelocity),
  stopAircraftVelocityListener: stopListener(DJIMobile.AircraftVelocity),
  observeAircraftVelocity: observeEvent<VelocityVector>(DJIMobile.AircraftVelocity),

  startAircraftAttitudeListener: startListener<Attitude>(DJIMobile.AircraftAttitude),
  stopAircraftAttitudeListener: stopListener(DJIMobile.AircraftAttitude),
  observeAircraftAttitude: observeEvent<Attitude>(DJIMobile.AircraftAttitude),

  startAircraftCompassHeadingListener: startListener<number>(DJIMobile.AircraftCompassHeading),
  stopAircraftCompassHeadingListener: stopListener(DJIMobile.AircraftCompassHeading),
  observeAircraftCompassHeading: observeEvent<number>(DJIMobile.AircraftCompassHeading),

  startAirLinkUplinkSignalQualityListener: startListener<number>(DJIMobile.AirLinkUplinkSignalQuality),
  stopAirLinkUplinkSignalQualityListener: stopListener(DJIMobile.AirLinkUplinkSignalQuality),
  observeAirlinkUplinkSignalQuality: observeEvent<number>(DJIMobile.AirLinkUplinkSignalQuality),

  startAirLinkDownlinkSignalQualityListener: startListener<number>(DJIMobile.AirLinkDownlinkSignalQuality),
  stopAirLinkDownlinkSignalQualityListener: stopListener(DJIMobile.AirLinkDownlinkSignalQuality),
  observeAirlinkDownlinkSignalQuality: observeEvent<number>(DJIMobile.AirLinkDownlinkSignalQuality),

  startHomeLocationListener: startListener<LocationCoordinate3D>(DJIMobile.AircraftHomeLocation),
  stopHomeLocationListener: stopListener(DJIMobile.AircraftHomeLocation),
  observeHomeLocation: observeEvent<LocationCoordinate3D>(DJIMobile.AircraftHomeLocation),

  startIsHomeLocationSetListener: startListener<boolean>(DJIMobile.IsHomeLocationSet),
  stopIsHomeLocationSetListener: stopListener(DJIMobile.IsHomeLocationSet),
  observeIsHomeLocationSet: observeEvent<boolean>(DJIMobile.IsHomeLocationSet),

  startGpsSignalLevelListener: startListener<number | null>(DJIMobile.AircraftGpsSignalLevel),
  stopGpsSignalLevelListener: stopListener(DJIMobile.AircraftGpsSignalLevel),
  observeGpsSignalLevel: observeEvent<number | null>(DJIMobile.AircraftGpsSignalLevel),

  startUltrasonicHeightListener: startListener(DJIMobile.AircraftUltrasonicHeight),
  stopUltrasonicHeightListener: stopListener(DJIMobile.AircraftUltrasonicHeight),

  startCompassHasErrorListener: startListener(DJIMobile.CompassHasError),
  stopCompassHasErrorListener: stopListener(DJIMobile.CompassHasError),

  startCameraIsRecordingListener: startListener(DJIMobile.CameraIsRecording),
  stopCameraIsRecordingListener: stopListener(DJIMobile.CameraIsRecording),

  startSDCardIsInsertedListener: startListener(DJIMobile.SDCardIsInserted),
  stopSDCardIsInsertedListener: stopListener(DJIMobile.SDCardIsInserted),

  startSDCardIsReadOnlyListener: startListener(DJIMobile.SDCardIsReadOnly),
  stopSDCardIsReadOnlyListener: stopListener(DJIMobile.SDCardIsReadOnly),

  startGimbalIsAtYawStopListener: startListener(DJIMobile.GimbalIsAtYawStop),
  stopGimbalIsAtYawStopListener: stopListener(DJIMobile.GimbalIsAtYawStop),

  startAircraftVirtualStickEnabledListener: startListener(DJIMobile.AircraftVirtualStickEnabled),
  stopAircraftVirtualStickEnabledListener: stopListener(DJIMobile.AircraftVirtualStickEnabled),

  startVisionDetectionStateListener: startListener(DJIMobile.VisionDetectionState),
  stopVisionDetectionStateListener: stopListener(DJIMobile.VisionDetectionState),

  startVisionControlStateListener: startListener(DJIMobile.VisionControlState),
  stopVisionControlStateListener: stopListener(DJIMobile.VisionControlState),

  startIsShootingSinglePhotoListener: startListener<boolean>(DJIMobile.CameraIsShootingSinglePhoto),
  stopIsShootingSinglePhotoListener: stopListener(DJIMobile.CameraIsShootingSinglePhoto),
  observeIsShootingSinglePhotoListener: observeEvent<boolean>(DJIMobile.CameraIsShootingSinglePhoto),

  startIsStoringPhotoListener: startListener<boolean>(DJIMobile.CameraIsStoringPhoto),
  stopIsStoringPhotoListener: stopListener(DJIMobile.CameraIsStoringPhoto),
  observeIsStoringPhotoListener: observeEvent<boolean>(DJIMobile.CameraIsStoringPhoto),

  startIsShootingPhotoListener: startListener<boolean>(DJIMobile.CameraIsShootingPhoto),
  stopIsShootingPhotoListener: stopListener(DJIMobile.CameraIsShootingPhoto),
  observeIsShootingPhotoListener: observeEvent<boolean>(DJIMobile.CameraIsShootingPhoto),

  startDiagnosticsListener: async () => {
    return DJIEventSubject.pipe($filter(evt => evt.type === 'DJIDiagnostics'));
  },

  stopDiagnosticsListener: async () => {
    await DJIMobile.stopEventListener('DJIDiagnostics');
  },

  startCameraExposureSettingsListener: async () => {
    await DJIMobile.startCameraExposureSettingsListener();
    return DJIEventSubject.pipe(
        $filter(evt => evt.type === 'CameraExposureSettings'),
        $map(evt => {
          // Get exposure value from lookup. The possible values should only be
          // string representations of floats.
          evt.value = {
            ...evt.value,
            exposureValue: parseFloat(Object.keys(exposureCompensationValues).find(key => exposureCompensationValues[key] === evt.value.exposureValue)),
          }
          return evt;
        }),
      );
  },

  stopCameraExposureSettingsListener: async () => {
    await DJIMobile.stopEventListener('CameraExposureSettings');
  },

  getAircraftLocation: async () => {
    return await DJIMobile.getAircraftLocation();
  },

  startNewMediaFileListener: async () => {
    await DJIMobile.startNewMediaFileListener();
    return DJIEventSubject.pipe($filter(evt => evt.type === 'CameraDidGenerateNewMediaFile'));
  },
  startNewMediaFileListenerV2: async () => {
    await DJIMobile.startNewMediaFileListener();
    return observeEvent<MediaFileData>('CameraDidGenerateNewMediaFile')
  },
  observeNewMediaFile: observeEvent<MediaFileData>('CameraDidGenerateNewMediaFile'),
  // TODO: this is working, but hasn't been tested thoroughly or properly typed
  observeCameraState: observeEvent<any>('CameraDidUpdateSystemState'),

  stopNewMediaFileListener: async () => {
    // TODO: (Adam) generalize & merge these!
    if (Platform.OS === 'ios') {
      await DJIMobile.stopNotificationCenterListener('DJICameraEvent.didGenerateNewMediaFile');
    } else {
      await DJIMobile.stopEventListener('CameraDidGenerateNewMediaFile');
    }
  },
  /**
   * ANDROID ONLY
   */
   getMediaFileList: async (numberOfResults?: number) => {
    if (numberOfResults != null ) {
      return await DJIMobile.getLimitedMediaFileList(numberOfResults)
    } else {
      return await DJIMobile.getMediaFileList();
    }
  },

  getFlightLogPath: async () => {
    return await DJIMobile.getFlightLogPath();
  },

  startFlightLogListener: async () => {
    await DJIMobile.startFlightLogListener();
    return DJIEventSubject.pipe($filter((evt: FlightLogListenerEvent) => evt.type === 'DJIFlightLogEvent'));
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

  getModelName: async () => {
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

export * from './types'
