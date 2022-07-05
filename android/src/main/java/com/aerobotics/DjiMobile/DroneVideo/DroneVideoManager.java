package com.aerobotics.DjiMobile.DroneVideo;

import android.util.Log;

import androidx.annotation.Nullable;

import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;

import org.jetbrains.annotations.NotNull;

import dji.common.airlink.PhysicalSource;

public class DroneVideoManager extends SimpleViewManager<DroneVideo> {

  @NotNull
  @Override
  public String getName() {
    return "DroneVideo";
  }

  @NotNull
  @Override
  protected DroneVideo createViewInstance(@NotNull ThemedReactContext reactContext) {
    return new DroneVideo(reactContext);
  }

  /*
   *  FIXME: Could not get this to work as desired. I could not update the video feed once it has
   *  already been rendered. So for now, the choosing of the video feed is handled by the component
   */
  @ReactProp(name = "videoSrc")
  public void setVideoSrc(DroneVideo view, @Nullable String videoSrc) {
    if (videoSrc == null) {
      return;
    }

    switch (videoSrc) {
      case "MODEL_MAIN_CAMERA":
        view.setPrimaryVideoFeedForModel();
        break;
      case "LEFT_CAMERA":
        view.setPrimaryVideoFeed(PhysicalSource.LEFT_CAM);
        break;
      case "FPV_CAMERA":
        view.setPrimaryVideoFeed(PhysicalSource.FPV_CAM);
        break;
      case "MAIN_CAMERA":
        view.setPrimaryVideoFeed(PhysicalSource.MAIN_CAM);
        break;
      case "RIGHT_CAMERA":
        view.setPrimaryVideoFeed(PhysicalSource.RIGHT_CAM);
        break;
      case "UNKNOWN":
        view.setPrimaryVideoFeed(PhysicalSource.UNKNOWN);
        break;
      default:
        Log.w("REACT", "Unknown video src" + videoSrc);
        break;
    }
  }
}