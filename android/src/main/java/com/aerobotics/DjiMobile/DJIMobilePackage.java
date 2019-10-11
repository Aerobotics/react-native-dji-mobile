package com.aerobotics.DjiMobile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.aerobotics.DjiMobile.DroneVideo.DroneVideoManager;
import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.JavaScriptModule;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;

public class DJIMobilePackage implements ReactPackage {
    @Override
    public List<NativeModule> createNativeModules(ReactApplicationContext reactContext) {
        List<NativeModule> modules = new ArrayList<>();
        modules.add(new DJIMobile(reactContext));
        modules.add(new DJIMissionControlWrapper(reactContext));
        modules.add(new CameraControlNative(reactContext));
        modules.add(new DJIMedia(reactContext));
        return modules;
//        return Arrays.<NativeModule>asList(
//          new DJIMobile(reactContext),
//          new DJIMissionControlWrapper(reactContext),
//          new CameraControlNative(reactContext),
//          new DJIMedia(reactContext)
//        );
    }

    @Override
    public List<ViewManager> createViewManagers(ReactApplicationContext reactContext) {
        return Arrays.<ViewManager>asList(
          new DroneVideoManager()
        );
    }
}
