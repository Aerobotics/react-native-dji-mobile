package com.aerobotics.DjiMobile.DroneVideo;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aerobotics.DjiMobile.R;
import com.aerobotics.DjiMobile.SDKEvent;

import dji.common.airlink.PhysicalSource;
import dji.common.error.DJIError;
import dji.common.product.Model;
import dji.common.util.CommonCallbacks;
import dji.keysdk.DJIKey;
import dji.keysdk.KeyManager;
import dji.keysdk.ProductKey;
import dji.keysdk.callback.GetCallback;
import dji.keysdk.callback.KeyListener;
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
    private PhysicalSource videoPrimarySource;
    private VideoFeeder.PhysicalSourceListener sourceListener;

    public DroneVideo(Context context) {
        super(context);
        initializeSurfaceTexture();
        startProductConnectionListener();
        setUpListeners();
    }

    private void initOnProductConnection() {
        initAirLink();
        setPrimaryVideoFeedForModel();
    }

    /**
     * FIXME: this doesn't seem to fire, yet it does in the DJI sample
     */
    private void setUpListeners() {
        sourceListener = new VideoFeeder.PhysicalSourceListener() {
            @Override
            public void onChange(VideoFeeder.VideoFeed videoFeed, PhysicalSource newPhysicalSource) {
                Log.d("REACT", "I HAVE FIRED!");
                if (videoFeed == VideoFeeder.getInstance().getPrimaryVideoFeed()) {
                    String newText = "Primary Source: " + newPhysicalSource.toString();
                    Log.d("REACT", newText);
                    videoPrimarySource = newPhysicalSource;
                }
                if (videoFeed == VideoFeeder.getInstance().getSecondaryVideoFeed()) {
                    Log.d("REACT", "Secondary Source: " + newPhysicalSource.toString());
                }
            }
        };
    }

    private void initAirLink() {
        BaseProduct baseProduct = DJISDKManager.getInstance().getProduct();
        if (baseProduct != null && baseProduct.getAirLink() != null) {
            airLink = baseProduct.getAirLink();
        } else {
            Log.w("REACT", "Product is not yet connected!");
        }
    }

    public void setPrimaryVideoFeedForModel() {
        BaseProduct product = DJISDKManager.getInstance().getProduct();
        if (product != null) {
            Model model = DJISDKManager.getInstance().getProduct().getModel();
            if (model == Model.MATRICE_300_RTK) {
                setPrimaryVideoFeed(PhysicalSource.LEFT_CAM);
            } else {
                setPrimaryVideoFeed(PhysicalSource.MAIN_CAM);
            }
        }

    }

    public void setPrimaryVideoFeed(PhysicalSource primarySource) {
        setPrimaryVideoFeed(primarySource, PhysicalSource.UNKNOWN);
    }

    public void setPrimaryVideoFeed(final PhysicalSource primarySource, PhysicalSource secondarySource) {
        try {
            if (airLink == null) {
                throw new Exception("Airlink is null");
            }
            OcuSyncLink ocuSyncLink = airLink.getOcuSyncLink();
            if (ocuSyncLink == null) {
                throw new Exception("OcuSyncLink is null");
            }
            ocuSyncLink.assignSourceToPrimaryChannel(primarySource, secondarySource, new CommonCallbacks.CompletionCallback<DJIError>() {
                @Override
                public void onResult(DJIError error) {
                    if (error != null) {
                        Log.w("REACT", "Failed to set video feed primary channel: " + error.getDescription());
                    } else {
                        // TODO: move this to the sourceListener once that if fixed
                        videoPrimarySource = primarySource;
                    }
                }
            });
        } catch (Exception exception) {
            Log.w("REACT", "Cannot set primary video feed: " + exception.getMessage());
        }
    }

    private void initializeSurfaceTexture() {
        View.inflate(getContext(), R.layout.drone_video_layout, this);
        TextureView droneVideoTexture = findViewById(R.id.droneVideoTexture);
        droneVideoTexture.setSurfaceTextureListener(this);
        VideoFeeder.getInstance().getPrimaryVideoFeed().addVideoDataListener(new VideoFeeder.VideoDataListener() {
            @Override
            public void onReceive(byte[] buffer, int size) {
                if (codecManager != null) {
                    codecManager.sendDataToDecoder(buffer, size, getVideoStreamSource().getIndex());
                }
            }
        });
    }

    private UsbAccessoryService.VideoStreamSource getVideoStreamSource() {
        if (videoPrimarySource == PhysicalSource.FPV_CAM) {
            return UsbAccessoryService.VideoStreamSource.Fpv;
        } else {
            return UsbAccessoryService.VideoStreamSource.Camera;
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (codecManager == null) {
            codecManager = new DJICodecManager(getContext(), surface, width, height, getVideoStreamSource());
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        codecManager.cleanSurface();
        codecManager = new DJICodecManager(getContext(), surface, width, height, getVideoStreamSource());
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
        tearDownListeners();
    }


    private void cleanUpVideoFeed() {
        VideoFeeder.getInstance().getPrimaryVideoFeed().destroy();
        if (codecManager != null) {
            codecManager.destroyCodec();
        }
    }

    private void tearDownListeners() {
        VideoFeeder.getInstance().removePhysicalSourceListener(sourceListener);
    }

    private void startProductConnectionListener() {
        KeyManager keyManager = DJISDKManager.getInstance().getKeyManager();
        if (keyManager != null) {
            DJIKey productConnectedKey = ProductKey.create(ProductKey.CONNECTION);
            keyManager.getValue(productConnectedKey, new GetCallback() {
                @Override
                public void onSuccess(@NonNull Object newValue) {
                    handleProductConnection(newValue);
                }

                @Override
                public void onFailure(@NonNull DJIError djiError) {
                }
            });
        }
        DJISDKManager.getInstance().getKeyManager().addListener((DJIKey) SDKEvent.ProductConnection.getKey(), new KeyListener() {
            @Override
            public void onValueChange(@Nullable Object oldValue, @Nullable Object newValue) {
                handleProductConnection(newValue);
            }
        });
    }

    private void handleProductConnection(@Nullable Object newValue) {
        if (newValue != null && newValue instanceof Boolean) {
            Boolean isProductConnected = (Boolean) newValue;
            if (isProductConnected) {
                initOnProductConnection();
            } else {

            }
        }
    }
}