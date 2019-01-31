
import {
  NativeModules,
} from 'react-native';

import {
  usePromiseOrCallback,
} from '../utilities';

const {
  DJISDKManagerWrapper,
} = NativeModules;

const DJISDKManager = {
  getSDKVersion(callback) {
    return usePromiseOrCallback(DJISDKManagerWrapper.getSDKVersion, callback);
  }
}

export default DJISDKManager;
