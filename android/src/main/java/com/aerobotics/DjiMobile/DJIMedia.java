package com.aerobotics.DjiMobile;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

public class DJIMedia  {
  private Promise promise;
  private MediaManager.FileListState currentFileListState = MediaManager.FileListState.UNKNOWN;
  private MediaManager mediaManager;
  private Camera camera;
  private List<MediaFile> mediaFileList = new ArrayList<MediaFile>();

  private MediaManager.FileListStateListener updateFileListStateListener = new MediaManager.FileListStateListener() {
    @Override
    public void onFileListStateChange(MediaManager.FileListState state) {
      currentFileListState = state;
    }
  };

  DJIMedia(){

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
  }

  private void initMediaManager() {
    mediaManager.addUpdateFileListStateListener(this.updateFileListStateListener);
    camera.setMode(SettingsDefinitions.CameraMode.MEDIA_DOWNLOAD, new CommonCallbacks.CompletionCallback() {
      @Override
      public void onResult(DJIError djiError) {
        if (djiError == null) {
          getFileList();
        } else {
          promise.reject(djiError.toString(), djiError.getDescription());
          return;
        }
      }
    });
    return;
  }

  public void getFileList() {

    if ((currentFileListState == MediaManager.FileListState.SYNCING) || (currentFileListState == MediaManager.FileListState.DELETING)){
      promise.reject("Media manager is busy");

    } else{
      System.out.println("Refresh file list");

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

            System.out.println("Get Media File List Success");
            promise.resolve(params);
          } else {
            System.out.println("Get Media File List Failed");
            promise.reject(djiError.toString(), djiError.getDescription());
          }
        }
      });
    }
  }
}
