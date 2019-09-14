package com.aerobotics.DjiMobile.DroneVideo;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.RelativeLayout;

import com.aerobotics.DjiMobile.R;

import dji.sdk.camera.VideoFeeder;
import dji.sdk.codec.DJICodecManager;
import dji.sdk.sdkmanager.DJISDKManager;

public class DroneVideo extends RelativeLayout implements TextureView.SurfaceTextureListener {

  private DJICodecManager codecManager;

  public DroneVideo(Context context) {
    super(context);
    initializeSurfaceTexture();
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
            codecManager.sendDataToDecoder(buffer, size);
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
      codecManager = new DJICodecManager(getContext(), surface, width, height);
    }
  }

  @Override
  public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
    codecManager.cleanSurface();
    codecManager = new DJICodecManager(getContext(), surface, width, height);
  }

  @Override
  public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
    codecManager.destroyCodec();
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
