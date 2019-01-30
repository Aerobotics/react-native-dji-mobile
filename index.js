
import { NativeModules } from 'react-native';

const { Module, ToastExample } = NativeModules;

console.log('here');
console.log(Module);

ToastExample.show('hello?', 2);

export default Module;
