# Get Started with react-native-dji-mobile 

## Create a new React Native project from scrash

```
$ react-native init NewProject
```
As explain in [README](../README.md), we add and link with Native Module.

```
$ cd NewProject
$ npm i --save react-native-dji-mobile
```

## Upgrade with react-native-dji-mobile new version

1 - Unlink react-native-dji-mobile
```
$ cd NewProject
$ react-native unlink react-native-dji-mobile
```

2 - Edit package.json to remove react-native-dji-mobile dependency. Remove the line: "react-native-dji-mobile": "0.0.6"

3 - Install react-native-dji-mobile from npmjs.org, then link to react-native project.
```
$ npm i --save react-native-dji-mobile
$ react-native link react-native-dji-mobile
```

4 - Clear and re-install pods
```
$ cd ios/
$ rm -rf Pods
$ rm Podfile.lock
$ pod install --repo-update
```

__NOTE__: react-native-dji-mobile is linked with DJI-SDK Version, Each time DJI publish a new version, developpers have to upgrade reac-native-dji-mobile to enjoy new features. It why we use `--repo-update`.

## DJI Integration SDK

### IOS:
Create a DJISDKAppKey key in the info.plist file of the Xcode project and paste the generated App Key string into its string value.
Open your ios/xcworkspace file with XCode and edit `Info.plist`. Add new key/value pair into the list.
![info.plist](./img/info-plist.png)

By following the [DJI integration steps](https://developer.dji.com/mobile-sdk/documentation/application-development-workflow/workflow-integrate.html), generate the __DJISDKAppKey__.

### Android:
__TODO__

## Use native module with Javascript

```javascript
import DJIMobile from 'react-native-dji-mobile';

// registerApp with DJI
DJIMobile.registerApp( '192.168.0.33' ) // update with the IP adresse diplqyed by DJI Bridge
  .then( val => { console.log( 'RegisterApp succeed!', val ); } )
  .catch( err => { console.log( 'RegisterApp fail.', err ); } );

// wait for product to connect
DJIMobile.startProductConnectionListener();
```
