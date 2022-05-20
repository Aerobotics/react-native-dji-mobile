import { CameraExposureSettings } from '../../types'
import { exposureCompensationValues } from '../CameraControl'

export const parseExposureSettings = (value: CameraExposureSettings) => {
  return {
    ...value,
    exposureValue: parseFloat(Object.keys(exposureCompensationValues).find(key => exposureCompensationValues[key] === value.exposureValue)),
  }
}