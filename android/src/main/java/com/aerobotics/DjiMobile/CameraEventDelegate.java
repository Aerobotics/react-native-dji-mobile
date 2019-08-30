package com.aerobotics.DjiMobile;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

  private CameraEventDelegate cameraDelegateSenderInstance = this;

  private boolean isProductConnected = false;
  private boolean isCameraConnected = false;

  private KeyListener ProductConnectedKeyListener = new KeyListener() {
    @Override
    public void onValueChange(@Nullable Object oldValue, @Nullable Object newValue) {
      if (newValue != null && newValue instanceof Boolean) {
        isProductConnected = (boolean)newValue;
        setCameraCallbacks();
      }
    }
  };

  private KeyListener CameraConnectedKeyListener = new KeyListener() {
    @Override
    public void onValueChange(@Nullable Object oldValue, @Nullable Object newValue) {
      if (newValue != null && newValue instanceof Boolean) {
        isCameraConnected = (boolean)newValue;
        setCameraCallbacks();
      }
    }
  };

  CameraEventDelegate() {
    // Sometimes the camera component is reported as connected before the base product, so we need to ensure that both are connected before
    // attempting to access the camera via the base product.
    KeyManager keyManager = DJISDKManager.getInstance().getKeyManager();
//    keyManager.addListener(ProductKey.create(ProductKey.CONNECTION), ProductConnectedKeyListener);
    keyManager.addListener(CameraKey.create(CameraKey.CONNECTION), CameraConnectedKeyListener);
  }



  private void postEvent(SDKEvent sdkEvent, Object eventData) {
    HashMap payload = new HashMap();
    payload.put("eventType", sdkEvent);
    payload.put("value", eventData);
    cameraEventObservable.sendEvent(payload);
  }

  private void setCameraCallbacks() {
    if (isCameraConnected) {

      // When the SDK has just registered, calling getProduct may return null, so wait before trying
      Handler handler = new Handler();
      handler.postDelayed(new Runnable() {
        @Override
        public void run() {
            Camera camera = DJISDKManager.getInstance().getProduct().getCamera();
            Log.i("REACT", "SETTING CALLBACKS!");
            if (camera != null) {
              // Add additional callbacks here as required
              camera.setSystemStateCallback(cameraDelegateSenderInstance);
              camera.setMediaFileCallback(cameraDelegateSenderInstance);
            }
        }
      }, 3000);

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
