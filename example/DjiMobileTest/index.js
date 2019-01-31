/** @format */

import {AppRegistry} from 'react-native';
import App from './App';
import {name as appName} from './app.json';

import {
  DJISDKManager,
} from 'react-native-dji-mobile'

DJISDKManager.getSDKVersion().then(version => console.log(version)).catch();

AppRegistry.registerComponent(appName, () => App);
