
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
      this.createMethod = keyClass.getMethod("create", String.class);
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    }
  }

  public DJIKey createDJIKey() {
    try {
//      Object KeyClass = this.keyClass.newInstance();
//      String args[] = {this.keyParam};
      // As the .create() method is a static method, no object instance needs to be passed to .invoke(), hence the null value
      DJIKey createdKey = (DJIKey)this.createMethod.invoke(null, this.keyParam);
      return createdKey;
    } catch (Exception e) {
      Log.i("EXCEPTION", e.getLocalizedMessage());
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
    put(
      "DJIFlightControllerParamCompassHeading",
      new ValidKeyInfo(FlightControllerKey.COMPASS_HEADING, FlightControllerKey.class)
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
    promise.resolve(null);
    startKeyListener(key, new KeyListener() {
      @Override
      public void onValueChange(@Nullable Object oldValue, @Nullable Object newValue) {
        if (newValue != null) {
          sendEvent(reactContext, "connectionStatus", (boolean) newValue ? "connected" : "disconnected");
        }
      }
    });
  }

  @ReactMethod
  public void startBatteryPercentChargeRemainingListener(Promise promise) {
    DJIKey key = BatteryKey.create(BatteryKey.CHARGE_REMAINING_IN_PERCENT);
    promise.resolve(null);
    startKeyListener(key, new KeyListener() {
      @Override
      public void onValueChange(@Nullable Object oldValue, @Nullable Object newValue) {
        if (newValue != null) {
          sendEvent(reactContext, "chargeRemaining", newValue);
        }
      }
    });
  }

  @ReactMethod
  public void startAircraftLocationListener(Promise promise) {
    DJIKey key = FlightControllerKey.create(FlightControllerKey.AIRCRAFT_LOCATION);
    promise.resolve(null);
    startKeyListener(key, new KeyListener() {
      @Override
      public void onValueChange(@Nullable Object oldValue, @Nullable Object newValue) {
        if (newValue != null) {
          LocationCoordinate3D location = (LocationCoordinate3D)newValue;
          double longitude = location.getLongitude();
          double latitude = location.getLatitude();
          double altitude = location.getAltitude();
          if (!Double.isNaN(longitude) && !Double.isNaN(latitude)) {
            WritableMap params = Arguments.createMap();
            params.putDouble("longitude", longitude);
            params.putDouble("latitude", latitude);
            params.putDouble("altitude", altitude);
            sendEvent(reactContext, "aircraftLocation", params);
          }
        }
      }
    });
  }

  @ReactMethod
  public void startAircraftVelocityListener(Promise promise) {
    DJIKey[] velocityKeys = {
      FlightControllerKey.create(FlightControllerKey.VELOCITY_X),
      FlightControllerKey.create(FlightControllerKey.VELOCITY_Y),
      FlightControllerKey.create(FlightControllerKey.VELOCITY_Z),
    };
    final double[] velocity3D = {0.0, 0.0, 0.0};

    promise.resolve(null);
    for (int i = 0; i < 3; i++) {
      final int finalI = i;
      startKeyListener(velocityKeys[i], new KeyListener() {
        @Override
        public void onValueChange(@Nullable Object oldValue, @Nullable Object newValue) {
          if (newValue != null) {
            velocity3D[finalI] = (float)newValue;
            WritableMap params = Arguments.createMap();
            params.putDouble("x", velocity3D[0]);
            params.putDouble("y", velocity3D[1]);
            params.putDouble("z", velocity3D[2]);
            sendEvent(reactContext, "aircraftVelocity", params);
          }
        }
      });
    }
  }

  @ReactMethod
  public void stopAircraftVelocityListener(Promise promise) {
    DJIKey[] velocityKeys = {
      FlightControllerKey.create(FlightControllerKey.VELOCITY_X),
      FlightControllerKey.create(FlightControllerKey.VELOCITY_Y),
      FlightControllerKey.create(FlightControllerKey.VELOCITY_Z),
    };
    for (int i = 0; i < 3; i++) {
      KeyListener updateBlock = (KeyListener) keyListeners.remove(velocityKeys[i].toString());
      if (updateBlock != null) {
        KeyManager.getInstance().removeListener(updateBlock);
      }
    }
  }

  @ReactMethod
  public void startAircraftCompassHeadingListener(Promise promise) {
    DJIKey key = FlightControllerKey.create(FlightControllerKey.COMPASS_HEADING);
    promise.resolve(null);
    startKeyListener(key, new KeyListener() {
      @Override
      public void onValueChange(@Nullable Object oldValue, @Nullable Object newValue) {
        if (newValue != null) {
          double heading = (float)newValue;
          WritableMap params = Arguments.createMap();
          params.putDouble("heading", heading);
          sendEvent(reactContext, "aircraftCompassHeading", params);
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