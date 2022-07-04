import { CameraExposureSettings } from '../../types';
import {
  ExposureCompensationValues,
  exposureCompensationValues,
} from '../CameraControl';

export const parseExposureSettings = (settings: CameraExposureSettings) => {
  const newExposureValueString = Object.keys(exposureCompensationValues).find(
    key =>
      exposureCompensationValues[key as ExposureCompensationValues] ===
      `${settings.exposureValue}`,
  );
  const exposureValue =
    newExposureValueString == null
      ? undefined
      : parseFloat(newExposureValueString);

  return {
    ...settings,
    exposureValue,
  };
};
