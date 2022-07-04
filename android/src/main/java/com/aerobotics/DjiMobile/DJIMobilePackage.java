package com.aerobotics.DjiMobile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.aerobotics.DjiMobile.DroneVideo.DroneVideoManager;
import com.aerobotics.DjiMobile.WaypointMissionV2.WaypointMissionV2Wrapper;
import com.facebook.react.ReactPackage;
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
        modules.add(new FlightControllerWrapper(reactContext));
        modules.add(new WaypointMissionV2Wrapper(reactContext));
        modules.add(new GimbalWrapper(reactContext));
        return modules;
    }

    @Override
    public List<ViewManager> createViewManagers(ReactApplicationContext reactContext) {
        return Arrays.<ViewManager>asList(
          new DroneVideoManager()
        );
    }
}
