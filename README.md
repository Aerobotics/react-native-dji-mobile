# react-native-dji-mobile

**A React Native wrapper for the [DJI Mobile SDK](https://developer.dji.com/mobile-sdk/).**

<p align='center'>
    <img src='https://img.shields.io/badge/licence-MIT-blue.svg?style=flat-square'>
  <a href='https://www.npmjs.com/package/react-native-dji-mobile'>
    <img src='https://img.shields.io/npm/v/react-native-dji-mobile.svg?style=flat-square'>
  </a>
</p>

*Please note that this project is still in its infancy. Many SDK features are not yet available, and the implementation of available features may still change. Use in your production application at your own risk!*

## Installation

Install the library to your project

```bash
npm i --save react-native-dji-mobile
```

## Linking

First link the library
```bash
react-native link react-native-dji-mobile
```

### iOS
**Using Cocoapods (recommended)**

1. The library requires the manual installation of the DJIWidget pod, used to display live video feed from the drone.
    1. Add `use_modular_headers!` to the top of your podfile.

    2. Add the following dependency to your podfile:
    ```diff
    + pod 'DJIWidget', '~> 1.5', :modular_headers => false
      pod 'react-native-dji-mobile', :path => '../node_modules/react-native-dji-mobile'
    ```

    1. Add `:modular_headers => false` to the pod dependencies for `DoubleConversion`, `glog`, and `Folly` in your podfile.
  
    After applying these changes your podfile should look similar to this:
    ```ruby
    platform :ios, '9.0'

    target 'MyDJIProject' do
      use_modular_headers!

      ...

      # Pods for MyDJIProject
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

      pod 'DJIWidget', '~> 1.5', :modular_headers => false
      pod 'react-native-dji-mobile', :path => '../node_modules/react-native-dji-mobile'
      
      ...

    end
    ```

2. run `pod install` to ensure the required dependencies are installed

3. To ensure that the library will build, you must add Swift support to your Xcode project:
   1. Open `ios/YourAppName.xcworkspace
   2. Select `File > New > File...` in Xcode's menu bar or press <kbd>CMD</kbd>+<kbd>N</kbd>.
   3. Add a new Swift file to your project, and when asked by Xcode, press **Create Bridging Header**. **Do not delete the empty swift file**.

**Manually (TODO)**

---

### Android (TODO)
