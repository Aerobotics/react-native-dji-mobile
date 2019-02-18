import {
  Platform,
} from 'react-native';

import DJISDKManager from './lib/DJISDKManager';

if (Platform.OS === 'ios') {
  console.log('ios');
} else if (Platform.OS === 'android') {
  console.log('android');
} else {
  throw new Error('Unsupported platform! Only iOS or Android is currently supported');
}

export {
  DJISDKManager,
}
