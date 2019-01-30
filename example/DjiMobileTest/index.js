/** @format */

import {AppRegistry} from 'react-native';
import App from './App';
import {name as appName} from './app.json';

import DjiMobile from 'react-native-dji-mobile'

DjiMobile.version();

AppRegistry.registerComponent(appName, () => App);
