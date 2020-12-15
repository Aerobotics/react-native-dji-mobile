package com.aerobotics.DjiMobile;

import androidx.annotation.Nullable;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import dji.common.camera.SettingsDefinitions;
import dji.common.error.DJIError;
import dji.common.util.CommonCallbacks;
import dji.sdk.camera.Camera;
import dji.sdk.media.DownloadListener;
import dji.sdk.media.MediaManager;
import dji.sdk.media.MediaFile;
import dji.sdk.sdkmanager.DJISDKManager;
import dji.sdk.products.Aircraft;
import dji.sdk.base.BaseProduct;

public class DJIMedia extends ReactContextBaseJavaModule {
  private Promise promise;
  private MediaManager.FileListState currentFileListState = MediaManager.FileListState.UNKNOWN;
  private MediaManager mediaManager;
  private Camera camera;
  private List<MediaFile> mediaFileList = new ArrayList<MediaFile>();
  private final ReactApplicationContext reactContext;

  private MediaManager.FileListStateListener updateFileListStateListener = new MediaManager.FileListStateListener() {
    @Override
    public void onFileListStateChange(MediaManager.FileListState state) {
      currentFileListState = state;
    }
  };

//  private HashMap<String, Promise> fileDownloadPromises = new HashMap<>();
  private Promise fileDownloadPromise;

  DJIMedia(ReactApplicationContext reactContext){
    super(reactContext);
    this.reactContext = reactContext;
  }

  public void getFileList(final Promise promise, BaseProduct baseProduct) {
    this.promise = promise;

    Aircraft aircraft =  (Aircraft) baseProduct;

    camera = aircraft.getCamera();
    if (camera == null){
      promise.reject("No camera connected");
      return;
    }
    mediaManager = camera.getMediaManager();

    initMediaManager();
    // getFileList();
  }

  private void initMediaManager() {
    mediaManager.addUpdateFileListStateListener(this.updateFileListStateListener);
    camera.setMode(SettingsDefinitions.CameraMode.MEDIA_DOWNLOAD, new CommonCallbacks.CompletionCallback() {
      @Override
      public void onResult(DJIError djiError) {
        if (djiError == null) {
          getFileList();
        } else {
          promise.reject(new Throwable("initMediaManager set camera mode error: " + djiError.getDescription()));
        }
      }
    });
  }

  private void getFileList() {

    if ((currentFileListState == MediaManager.FileListState.SYNCING) || (currentFileListState == MediaManager.FileListState.DELETING)){
      promise.reject(new Throwable("getFileList error: Media manager is busy"));
    } else{
      mediaManager.refreshFileListOfStorageLocation(SettingsDefinitions.StorageLocation.SDCARD, new CommonCallbacks.CompletionCallback() {
        @Override
        public void onResult(DJIError djiError) {
          if (null == djiError) {
            //Reset data
            if (currentFileListState != MediaManager.FileListState.INCOMPLETE) {
              mediaFileList.clear();
            }

            mediaFileList = mediaManager.getSDCardFileListSnapshot();

            // Sort to get latest file first
            Collections.sort(mediaFileList, new Comparator<MediaFile>() {
              @Override
              public int compare(MediaFile lhs, MediaFile rhs) {
                if (lhs.getTimeCreated() < rhs.getTimeCreated()) {
                  return 1;
                } else if (lhs.getTimeCreated() > rhs.getTimeCreated()) {
                  return -1;
                }
                return 0;
              }
            });

            WritableArray params = Arguments.createArray();
            for (MediaFile m : mediaFileList){
              WritableMap file = Arguments.createMap();
              file.putString("fileName", m.getFileName());
              file.putDouble("timeCreatedAt", m.getTimeCreated());
              file.putDouble("fileSize", m.getFileSize());
              params.pushMap(file);
            }
            promise.resolve(params);
          } else {
            promise.reject(new Throwable("getFileList error: " + djiError.getDescription()));
          }
        }
      });
    }
  }

  @ReactMethod
  public void startFullResMediaFileDownload(final String nameOfFileToDownload, @Nullable final String newFileName, final Promise promise) {
    final BaseProduct product = DJISDKManager.getInstance().getProduct();
    if (product instanceof Aircraft){
      camera = ((Aircraft) product).getCamera();
    }

    String newFileNameWithoutExtension = null;
    if (newFileName != null) {
      newFileNameWithoutExtension = newFileName.split("\\.")[0];
    }

    if (camera != null) {
        final String finalNewFileNameWithoutExtension = newFileNameWithoutExtension;
        camera.setMode(SettingsDefinitions.CameraMode.MEDIA_DOWNLOAD, new CommonCallbacks.CompletionCallback() {
        @Override
        public void onResult(DJIError djiError) {
          if (djiError == null) {
            final MediaManager mediaManager = camera.getMediaManager();
            try {
              mediaManager.refreshFileListOfStorageLocation(SettingsDefinitions.StorageLocation.SDCARD, new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                  if (djiError == null) {
                    MediaManager.FileListState fileListState = mediaManager.getSDCardFileListState();
                    if ((fileListState != MediaManager.FileListState.RESET) && (fileListState != MediaManager.FileListState.DELETING)) {
                      List<MediaFile> mediaFiles = mediaManager.getSDCardFileListSnapshot();
                      for (MediaFile mediaFile: mediaFiles) {
                        final String fileName = mediaFile.getFileName();
                        if (nameOfFileToDownload.equals(fileName)) {
                          mediaFile.fetchFileData(reactContext.getFilesDir(), finalNewFileNameWithoutExtension, downloadListener);
                          fileDownloadPromise = promise;
                          return; // Do not reject with the file not found error
                        }
                      }
                      promise.reject(new Throwable("Error: File not found"));
                    } else {
                      promise.reject(new Throwable("Error: Could not get file list"));
                    }
                  } else {
                    promise.reject(new Throwable("startFullResMediaFileDownload getting file list error: " + djiError.getDescription()));
                  }
                }
              });
            } catch (NullPointerException e) {
              promise.reject(new Throwable("Error: " + e.getMessage()));
            }
          } else {
            promise.reject(new Throwable("startFullResMediaFileDownload set camera mode error: " + djiError.getDescription()));
          }
        }
      });

    } else {
      promise.reject(new Throwable("startFullResMediaFileDownload error: Camera not connected"));
    }
  }

  private DownloadListener<String> downloadListener = new DownloadListener<String>() {
    @Override
    public void onStart() {
      // Log.i("REACT", "Started file download: " + fileName);

      WritableMap params = Arguments.createMap();
      WritableMap eventInfo = Arguments.createMap();
      eventInfo.putString("eventName", "onStart");
      params.putMap("value", eventInfo);
      params.putString("type", "mediaFileDownloadEvent");
      reactContext
              .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
              .emit("DJIEvent", params);
    }

    @Override
    public void onRateUpdate(long total, long current, long persize) {
      WritableMap params = Arguments.createMap();
      WritableMap eventInfo = Arguments.createMap();
      eventInfo.putString("eventName", "onRateUpdate");
      eventInfo.putInt("total", (int) total);
      eventInfo.putInt("current", (int) current);
      eventInfo.putInt("persize", (int) persize);
      params.putMap("value", eventInfo);
      params.putString("type", "mediaFileDownloadEvent");
      reactContext
              .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
              .emit("DJIEvent", params);

    }

    @Override
    public void onRealtimeDataUpdate(byte[] bytes, long l, boolean b) {
      
    }

    @Override
    public void onProgress(long total, long current) {
      WritableMap params = Arguments.createMap();
      WritableMap eventInfo = Arguments.createMap();
      eventInfo.putString("eventName", "onProgress");
      eventInfo.putInt("total", (int) total);
      eventInfo.putInt("current", (int) current);
      params.putMap("value", eventInfo);
      params.putString("type", "mediaFileDownloadEvent");
//      reactContext
//              .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
//              .emit("DJIEvent", params);
    }

    @Override
    public void onSuccess(String s) {
//       Log.i("REACT", "Successful file download: " + s);
      WritableMap params = Arguments.createMap();
      WritableMap eventInfo = Arguments.createMap();
      eventInfo.putString("eventName", "onSuccess");
      params.putMap("value", eventInfo);
      params.putString("type", "mediaFileDownloadEvent");
      reactContext
              .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
              .emit("DJIEvent", params);

      fileDownloadPromise.resolve(null);
    }

    @Override
    public void onFailure(DJIError djiError) {
      // Log.i("REACT", "Failed to download file " + fileName + ": " + djiError.getDescription());
      WritableMap params = Arguments.createMap();
      WritableMap eventInfo = Arguments.createMap();
      eventInfo.putString("eventName", "onFailure");
      eventInfo.putString("error", djiError.toString());
      params.putMap("value", eventInfo);
      params.putString("type", "mediaFileDownloadEvent");
      reactContext
              .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
              .emit("DJIEvent", params);

      fileDownloadPromise.reject(new Throwable("File download failure: " + djiError.getDescription()));
    }
  };

  @Override
  public String getName() {
    return "DJIMedia";
  }
}
