package com.aerobotics.DjiMobile;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import dji.common.camera.SystemState;
import dji.sdk.camera.Camera;
import dji.sdk.media.MediaFile;

public class CameraEventDelegate implements SystemState.Callback, MediaFile.Callback {

  private class CameraEventObservable extends Observable {
    public void sendEvent(Object payload) {
      setChanged();
      notifyObservers(payload);
    }
  }

  private CameraEventObservable cameraEventObservable = new CameraEventObservable();
  private boolean callbacksSet = false;

  CameraEventDelegate(Camera camera) {
    setCameraCallbacks(camera);
  }

  public void setCameraCallbacks(Camera camera) {
    if (!callbacksSet) {
      camera.setSystemStateCallback(this);
      camera.setMediaFileCallback(this);
      callbacksSet = true;
    }
  }

  public boolean areCameraCallbacksSet() {
    return callbacksSet;
  }

  private void postEvent(SDKEvent sdkEvent, Object eventData) {
    HashMap payload = new HashMap();
    payload.put("eventType", sdkEvent);
    payload.put("value", eventData);
    cameraEventObservable.sendEvent(payload);
  }

  public void addObserver(Observer observer) {
    cameraEventObservable.addObserver(observer);
  }

  public void removeObserver(Observer observer) {
    cameraEventObservable.deleteObserver(observer);
  }

  @Override
  public void onUpdate(@NonNull SystemState systemState) {
    this.postEvent(SDKEvent.CameraDidUpdateSystemState, systemState);
  }

  @Override
  public void onNewFile(@NonNull MediaFile mediaFile) {
    this.postEvent(SDKEvent.CameraDidGenerateNewMediaFile, mediaFile);
  }
}
