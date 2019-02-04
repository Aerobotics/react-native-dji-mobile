
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
  },

  registerApp(callback) {
    return usePromiseOrCallback(DJISDKManagerWrapper.registerApp, callback);
  },

  startConnectionToProduct(callback) {
    return usePromiseOrCallback(DJISDKManagerWrapper.startConnectionToProduct, callback);
  },
}

export default DJISDKManager;
