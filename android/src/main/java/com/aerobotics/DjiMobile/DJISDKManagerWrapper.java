
package com.aerobotics.DjiMobile;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Promise;


import android.util.Log;

import dji.common.error.DJIError;
import dji.common.error.DJISDKError;
import dji.sdk.base.BaseComponent;
import dji.sdk.base.BaseProduct;
import dji.sdk.sdkmanager.DJISDKManager;

public class DJISDKManagerWrapper extends ReactContextBaseJavaModule {

  private final ReactApplicationContext reactContext;

  public DJISDKManagerWrapper(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
  }

  @ReactMethod
  public void getSDKVersion(Promise promise) {
    promise.resolve(DJISDKManager.getInstance().getSDKVersion());
  }

  @ReactMethod
  public void registerApp(final Promise promise) {
    DJISDKManager.getInstance().registerApp(this.reactContext, new DJISDKManager.SDKManagerCallback() {
      @Override
      public void onRegister(DJIError djiError) {
        if (djiError == DJISDKError.REGISTRATION_SUCCESS) {
          promise.resolve(null);
        } else {
          promise.reject(djiError.toString(), djiError.getDescription());
        }
      }

      @Override
      public void onProductDisconnect() {
//        Log.i("PRODUCT", "DISCONNECTED");
      }

      @Override
      public void onProductConnect(BaseProduct baseProduct) {
//        Log.i("PRODUCT", "CONNECTED: " + baseProduct.getModel());
      }

      @Override
      public void onComponentChange(BaseProduct.ComponentKey componentKey, BaseComponent baseComponent, BaseComponent baseComponent1) {

      }
    });
  }

  @Override
  public String getName() {
    return "DJISDKManagerWrapper";
  }
}