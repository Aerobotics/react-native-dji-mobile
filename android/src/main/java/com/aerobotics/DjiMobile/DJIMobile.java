
package com.aerobotics.DjiMobile;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;


import android.support.annotation.Nullable;
import android.util.Log;

import java.lang.reflect.Method;
import java.security.Key;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import dji.common.error.DJIError;
import dji.common.error.DJISDKError;
import dji.common.flightcontroller.LocationCoordinate3D;
import dji.keysdk.DJIKey;
import dji.keysdk.FlightControllerKey;
import dji.keysdk.KeyManager;
import dji.keysdk.ProductKey;
import dji.keysdk.BatteryKey;
import dji.keysdk.callback.KeyListener;
import dji.sdk.base.BaseComponent;
import dji.sdk.base.BaseProduct;
import dji.sdk.sdkmanager.DJISDKManager;

class ValidKeyInfo {
  String keyParam;
  Class keyClass;
  Method createMethod;

  public ValidKeyInfo(String keyParam, Class keyClass) {
    this.keyParam = keyParam;
    this.keyClass = keyClass;
    try {
      this.createMethod = keyClass.getMethod("create", null);
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    }
  }

  public DJIKey createDJIKey() {
    try {
      Object KeyClass = this.keyClass.newInstance();
      DJIKey createdKey = (DJIKey)this.createMethod.invoke(KeyClass, this.keyParam);
      return createdKey;
    } catch (Exception e) {
      return null;
    }
  }
}

public class DJIMobile extends ReactContextBaseJavaModule {

  private final ReactApplicationContext reactContext;

  private HashMap keyListeners = new HashMap();

  private HashMap implementedKeys = new HashMap<String, ValidKeyInfo>() {{
    put(
      "DJIParamConnection",
      new ValidKeyInfo(ProductKey.CONNECTION, ProductKey.class)
    );
    put(
      "DJIBatteryParamChargeRemainingInPercent",
      new ValidKeyInfo(BatteryKey.CHARGE_REMAINING_IN_PERCENT, BatteryKey.class)
    );
    put(
      "DJIFlightControllerParamAircraftLocation",
      new ValidKeyInfo(FlightControllerKey.AIRCRAFT_LOCATION, FlightControllerKey.class)
    );
  }};

  public DJIMobile(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
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
      public void onProductDisconnect() {}
      @Override
      public void onProductConnect(BaseProduct baseProduct) {}
      @Override
      public void onComponentChange(BaseProduct.ComponentKey componentKey, BaseComponent baseComponent, BaseComponent baseComponent1) {}
    });
  }

  @ReactMethod
  public void startProductConnectionListener(Promise promise) {
    DJIKey key = ProductKey.create(ProductKey.CONNECTION);
    startKeyListener(key, new KeyListener() {
      @Override
      public void onValueChange(@Nullable Object oldValue, @Nullable Object newValue) {
        if (newValue != null) {
          sendEvent(reactContext, "connectionStatus", (boolean) newValue ? "connected" : "disconnected");
        }
      }
    });
    promise.resolve(null);
  }

  @ReactMethod
  public void startBatteryPercentChargeRemainingListener(Promise promise) {
    DJIKey key = BatteryKey.create(BatteryKey.CHARGE_REMAINING_IN_PERCENT);
    startKeyListener(key, new KeyListener() {
      @Override
      public void onValueChange(@Nullable Object oldValue, @Nullable Object newValue) {
        if (newValue != null) {
          sendEvent(reactContext, "chargeRemaining", (int)newValue);
        }
      }
    });
    promise.resolve(null);
  }

  @ReactMethod
  public void startAircraftLocationListener(Promise promise) {
    DJIKey key = FlightControllerKey.create(FlightControllerKey.AIRCRAFT_LOCATION);
    startKeyListener(key, new KeyListener() {
      @Override
      public void onValueChange(@Nullable Object oldValue, @Nullable Object newValue) {
        if (newValue != null) {
          LocationCoordinate3D location = (LocationCoordinate3D)newValue;
          WritableMap params = Arguments.createMap();
          params.putDouble("longitude", location.getLongitude());
          params.putDouble("latitude", location.getLatitude());
          params.putDouble("altitude", location.getAltitude());
          sendEvent(reactContext, "aircraftLocation", params);
        }

      }
    });
  }

  @ReactMethod
  public void stopKeyListener(String keyString, Promise promise) {
    ValidKeyInfo keyInfo = (ValidKeyInfo)implementedKeys.get(keyString);
    if (keyInfo != null) {
      DJIKey createdKey = keyInfo.createDJIKey();
      KeyListener updateBlock = (KeyListener)keyListeners.remove(createdKey.toString());
      if (updateBlock != null) {
        KeyManager.getInstance().removeListener(updateBlock);
      }
    }
  }

  private void startKeyListener(DJIKey key, KeyListener updateBlock) {
    KeyListener existingUpdateBlock = (KeyListener)keyListeners.get(key.toString());
    if (existingUpdateBlock == null) {
      keyListeners.put(key.toString(), updateBlock);
      KeyManager.getInstance().addListener(key, updateBlock);
    } else {
      // If there is an existing listener, don't create a new one
      return;
    }

  }

  private void sendEvent(ReactContext reactContext, String type, Object value) {
    WritableMap params = Arguments.createMap();

    if (value instanceof Integer) {
      params.putInt("value", (Integer)value);
    } else if (value instanceof Double) {
      params.putDouble("value", (Double)value);
    } else if (value instanceof String) {
      params.putString("value", (String)value);
    } else if (value instanceof Boolean) {
      params.putBoolean("value", (Boolean)value);
    } else if (value instanceof WritableMap) {
      params.putMap("value", (WritableMap)value);
    } else if (value instanceof WritableArray) {
      params.putArray("value", (WritableArray) value);
    }

    params.putString("type", type);
    reactContext
      .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
      .emit("DJIEvent", params);
  }

  @Override
  public String getName() {
    return "DJIMobile";
  }
}