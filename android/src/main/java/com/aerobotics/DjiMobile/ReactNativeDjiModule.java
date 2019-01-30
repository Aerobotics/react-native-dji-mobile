
package com.aerobotics.DjiMobile;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;

import android.util.Log;

import dji.sdk.sdkmanager.DJISDKManager;

public class ReactNativeDjiModule extends ReactContextBaseJavaModule {

  private final ReactApplicationContext reactContext;

  public ReactNativeDjiModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
  }

  @ReactMethod
  public void version() {
    Log.i("TEST VERSION", DJISDKManager.getInstance().getSDKVersion());
    return;
  }

  @Override
  public String getName() {
    return "Module";
  }
}