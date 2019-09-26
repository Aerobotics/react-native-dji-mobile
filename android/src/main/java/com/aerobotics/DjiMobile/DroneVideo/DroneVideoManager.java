package com.aerobotics.DjiMobile.DroneVideo;

import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;

public class DroneVideoManager extends SimpleViewManager<DroneVideo> {

  @Override
  public String getName() {
    return "DroneVideo";
  }

  @Override
  protected DroneVideo createViewInstance(ThemedReactContext reactContext) {
    return new DroneVideo(reactContext);
  }
}