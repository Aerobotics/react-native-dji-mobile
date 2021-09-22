package com.aerobotics.DjiMobile;

import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;

import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import dji.common.camera.SystemState;
import dji.common.error.DJIError;
import dji.keysdk.CameraKey;
import dji.keysdk.DJIKey;
import dji.keysdk.KeyManager;
import dji.keysdk.ProductKey;
import dji.keysdk.callback.GetCallback;
import dji.keysdk.callback.KeyListener;
import dji.sdk.base.BaseProduct;
import dji.sdk.camera.Camera;
import dji.sdk.media.MediaFile;
import dji.sdk.sdkmanager.DJISDKManager;

// The CameraEventDelegate assumes that the app has successfully registered, otherwise things may crash!
public class CameraEventDelegate implements SystemState.Callback, MediaFile.Callback {

  private class CameraEventObservable extends Observable {
    public void sendEvent(Object payload) {
      setChanged();
      notifyObservers(payload);
    }
  }

  private CameraEventObservable cameraEventObservable = new CameraEventObservable();

  private boolean isCameraConnected = false;
  private boolean callbacksSet = false;
  private final CameraEventDelegate cameraDelegateSenderInstance = this;

  private KeyListener CameraConnectedKeyListener = new KeyListener() {
    @Override
    public void onValueChange(@Nullable Object oldValue, @Nullable Object newValue) {
      if (newValue != null && newValue instanceof Boolean) {
        if ((boolean) newValue && !isCameraConnected) {
          isCameraConnected = (boolean)newValue;
          // When the SDK has just registered, calling getProduct may return null, so wait a second before trying
          Handler handler = new Handler();
          handler.postDelayed(new Runnable() {
            @Override
            public void run() {
              cameraDelegateSenderInstance.setCameraCallbacks();
            }
          }, 1000);
        }
      }
    }
  };

  CameraEventDelegate() {
    boolean cameraConnected = isCameraConnected();
    if (!cameraConnected) {
      startCameraConnectionListener();
    } else {
      isCameraConnected = true;
      setCameraCallbacks();
    }
  }

  private boolean isCameraConnected() {
    BaseProduct product = DJISDKManager.getInstance().getProduct();
    if (product == null) {
      // cannot access the camera if the product is null
      return false;
    }
    Camera camera = product.getCamera();
    return camera != null;
  }

  public boolean areCameraCallbacksSet() {
    return callbacksSet;
  }

  private void startCameraConnectionListener() {
    KeyManager keyManager = DJISDKManager.getInstance().getKeyManager();
    keyManager.addListener(CameraKey.create(CameraKey.CONNECTION), CameraConnectedKeyListener);
  }

  private void postEvent(SDKEvent sdkEvent, Object eventData) {
    HashMap payload = new HashMap();
    payload.put("eventType", sdkEvent);
    payload.put("value", eventData);
    cameraEventObservable.sendEvent(payload);
  }

  public void setCameraCallbacks() {
    if (!callbacksSet) {
      Camera camera = DJISDKManager.getInstance().getProduct().getCamera();
      camera.setSystemStateCallback(this);
      camera.setMediaFileCallback(this);
      callbacksSet = true;
    }

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
