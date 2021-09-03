package com.aerobotics.DjiMobile.DroneVideo;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.RelativeLayout;

import com.aerobotics.DjiMobile.R;

import dji.common.airlink.PhysicalSource;
import dji.common.error.DJIError;
import dji.common.util.CommonCallbacks;
import dji.midware.usb.P3.UsbAccessoryService;
import dji.sdk.base.BaseProduct;
import dji.sdk.camera.VideoFeeder;
import dji.sdk.codec.DJICodecManager;
import dji.sdk.sdkmanager.DJISDKManager;
import dji.sdk.airlink.AirLink;
import dji.sdk.airlink.OcuSyncLink;

public class DroneVideo extends RelativeLayout implements TextureView.SurfaceTextureListener {

  private DJICodecManager codecManager;
  private AirLink airLink;

  public DroneVideo(Context context) {
    super(context);
    initAirLink();
    setPrimaryVideoFeed();
    initializeSurfaceTexture();
  }

  private void initAirLink() {
    BaseProduct baseProduct = DJISDKManager.getInstance().getProduct();
    // TODO: it seems like the product has not been initialised when this code is
    // initially run. Need to use a production connection callback here.
    if (baseProduct != null && baseProduct.getAirLink() != null) {
      airLink = baseProduct.getAirLink();
    }
  }

  private void setPrimaryVideoFeed() {
    if (airLink != null) {
      OcuSyncLink ocuSyncLink = airLink.getOcuSyncLink();
      if (ocuSyncLink != null) {
        ocuSyncLink.assignSourceToPrimaryChannel(PhysicalSource.LEFT_CAM, PhysicalSource.FPV_CAM, new CommonCallbacks.CompletionCallback() {
          @Override
          public void onResult(DJIError error) {
            if (error != null) {
              Log.d("REACT", "Failed to set video feed primary channel: " + error.getDescription());
            }
          }
        });
      }
    }
  }
  private void initializeSurfaceTexture() {
    View.inflate(getContext(), R.layout.drone_video_layout, this);
    TextureView droneVideoTexture = findViewById(R.id.droneVideoTexture);
    droneVideoTexture.setSurfaceTextureListener(this);
    if (DJISDKManager.getInstance().hasSDKRegistered()) {
      VideoFeeder.getInstance().getPrimaryVideoFeed().addVideoDataListener(new VideoFeeder.VideoDataListener() {
        @Override
        public void onReceive(byte[] buffer, int size) {
          if (codecManager != null) {
            codecManager.sendDataToDecoder(buffer, size, UsbAccessoryService.VideoStreamSource.Camera.getIndex());
          }
        }
      });
    }
  }

  private void cleanUpVideoFeed() {
    VideoFeeder.getInstance().getPrimaryVideoFeed().destroy();
    if (codecManager != null) {
      codecManager.destroyCodec();
    }
  }

  @Override
  public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
    if (codecManager == null) {
      codecManager = new DJICodecManager(getContext(), surface, width, height, UsbAccessoryService.VideoStreamSource.Camera);
    }
  }

  @Override
  public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
    codecManager.cleanSurface();
    codecManager = new DJICodecManager(getContext(), surface, width, height, UsbAccessoryService.VideoStreamSource.Camera);
  }

  @Override
  public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
    if (codecManager != null) {
      codecManager.cleanSurface();
      codecManager.destroyCodec();
      codecManager = null;
    }
    return false;
  }

  @Override
  public void onSurfaceTextureUpdated(SurfaceTexture surface) {

  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    cleanUpVideoFeed();
  }
}
