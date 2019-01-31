
package com.aerobotics.DjiMobile;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Promise;


import android.util.Log;

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

  @Override
  public String getName() {
    return "DJISDKManagerWrapper";
  }
}