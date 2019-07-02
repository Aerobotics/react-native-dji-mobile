# Getting Started with react-native-dji-mobile 

## Create a new React Native project from scrash

```
$ react-native init NewProject
```
As explain in [README](../README.md), we add and link with Native Module.

```
$ cd NewProject
$ npm i --save react-native-dji-mobile
```

## Link react-native-dji-mobile

```
$ react-native link react-native-dji-mobile
```

## iOS Podfile

to add native dependencies to iOS project you have to create a `Podfile`. Create a file `./ios/Podfile` and copy/past following lines:

```
# Uncomment the next line to define a global platform for your project
platform :ios, '9.0'

target 'NewDJIProject' do
  use_modular_headers!
  # Uncomment the next line if you're using Swift or would like to use dynamic frameworks
  # use_frameworks!

  # Pods for NewDJIProject
  pod 'React', :path => '../node_modules/react-native', :subspecs => [
    'Core',
    'CxxBridge', # Include this for RN >= 0.47
    'DevSupport', # Include this to enable In-App Devmenu if RN >= 0.43
    'RCTText',
    'RCTNetwork',
    'RCTWebSocket', # needed for debugging
    # Add any other subspecs you want to use in your project
  ]
  # Explicitly include Yoga if you are using RN >= 0.42.0
  pod "yoga", :path => "../node_modules/react-native/ReactCommon/yoga"

  # Third party deps podspec link
  pod 'DoubleConversion', :podspec => '../node_modules/react-native/third-party-podspecs/DoubleConversion.podspec', :modular_headers => false
  pod 'glog', :podspec => '../node_modules/react-native/third-party-podspecs/glog.podspec', :modular_headers => false
  pod 'Folly', :podspec => '../node_modules/react-native/third-party-podspecs/Folly.podspec', :modular_headers => false

  pod 'DJIWidget', '~> 1.5', :modular_headers => true

  pod 'react-native-dji-mobile', :path => '../node_modules/react-native-dji-mobile'

end
```

Don't forget to change the 'NewDJIProject' ( line 4 ) with the name of your React-Native project. Use the same that you use for the `$ react-native init` command.

then install native dependencies for iOS: 

```
$ cd ./ios
$ pod install
```

## DJI Integration SDK

### IOS:
Create a DJISDKAppKey key in the info.plist file of the Xcode project and paste the generated App Key string into its string value.
Open your ios/xcworkspace file with XCode and edit `Info.plist`. Add new key/value pair into the list.
![info.plist](./img/info-plist.png)

By following the [DJI integration steps](https://developer.dji.com/mobile-sdk/documentation/application-development-workflow/workflow-integrate.html), generate the __DJISDKAppKey__. You should finish this step by adding new entry: DJISDKAppKey to Info.plist

_Enable iOS Swift project_
To use the React-Native-DJI-Mobile, you have to enable Swift for iOS project. To to that, simply add and keep an empty swift file in your iOS project.

* Open ./ios/VehicleDJI.xcworkspace with Xcode
* Right click on the NewDJIProject folder to add a new file ( same level that the Info.plist )
* Chose file type: Swift
* Name `empty.swift`
* Click save then click `Create Bridging Header`
* Build iOS project

### Android:
__TODO__

## Upgrade react-native-dji-mobile latest version

1 - Unlink react-native-dji-mobile
```
$ cd NewProject
$ react-native unlink react-native-dji-mobile
```

2 - Edit package.json to remove react-native-dji-mobile dependency. Remove the line: "react-native-dji-mobile": "x.x.x"

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


## How to subscribe DJI Event with React Native Javascript

react-native-dji-mobile use [RxJS](https://www.learnrxjs.io/) for dealing with events.

Example to subscribe to the Battery C
```javascript
DJIMobile.startBatteryPercentChargeRemainingListener()
  .then( ( observable ) => {
    const observer = observable.subscribe( evt => {
      console.log( `Battery: ${ evt.value }% charge remaining.` );
    } );
  } )
  .catch( err => {
    console.log( 'ERROR: startBatteryPercentChargeRemainingListener fail.', err );
  } );
```
__NOTE__: Do not forget to call the observer.unsubscribe() when you finish to work with the Event. And then the corresponding stop listener method ( DJIMobile.startBatteryPercentChargeRemainingListener in the example below ).

## How to get Realtime video feedback to ReactNative
TODO

## How to use mission timeline elements and start first mission
TODO
