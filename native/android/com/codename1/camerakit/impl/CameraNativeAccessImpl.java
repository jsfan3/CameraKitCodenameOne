package com.codename1.camerakit.impl;

import android.Manifest;
import android.graphics.Bitmap;
import android.media.MediaRecorder;
import android.view.TextureView;

import com.codename1.camerakit.Constants;
import com.codename1.impl.android.AndroidNativeUtil;
import com.codename1.impl.android.AndroidImplementation;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import com.codename1.io.FileSystemStorage;

import co.infinum.goldeneye.GoldenEye;
import co.infinum.goldeneye.InitCallback;
import co.infinum.goldeneye.PictureCallback;
import co.infinum.goldeneye.VideoCallback;
import co.infinum.goldeneye.config.CameraConfig;
import co.infinum.goldeneye.config.CameraInfo;
import co.infinum.goldeneye.models.Facing;
import co.infinum.goldeneye.models.FlashMode;
import co.infinum.goldeneye.models.FocusMode;
import co.infinum.goldeneye.models.PreviewScale;
import co.infinum.goldeneye.models.VideoQuality;
import co.infinum.goldeneye.models.Size;
import com.codename1.io.Log;

public class CameraNativeAccessImpl {

    private int mode;
    private int width;
    private int height;
    private GoldenEye goldenEye;
    private TextureView view;
    private boolean started;
    private InitCallback initCallback = new InitCallback() {
        @Override
        public void onError(Throwable ex) {
            CameraCallbacks.onError(null, ex.getMessage(), ex.getMessage());
        }

        @Override
        public void onReady(CameraConfig config) {
            super.onReady(config);
            started = true;
        }
    };

    public void start() {
        if (!AndroidNativeUtil.checkForPermission(android.Manifest.permission.CAMERA, "This application requires permission to use your camera")) {
            CameraCallbacks.onError(null, "Permission to use camera denied", "Permission to use camera denied");
            return;
        }

        AndroidImplementation.runOnUiThreadAndBlock(new Runnable() {
            @Override
            public void run() {
                if (view == null) {
                    goldenEye = new GoldenEye.Builder(AndroidNativeUtil.getActivity()).build();
                    view = new TextureView(AndroidNativeUtil.getContext());

                }
                goldenEye.open(view, goldenEye.getAvailableCameras().get(0), initCallback);

            }
        });
    }

    public void stop() {
        AndroidNativeUtil.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                started = false;
                goldenEye.release();
            }
        });
    }

    public boolean isStarted() {
        if (view == null) {
            return false;
        }
        return started;
    }

    public void setMethod(final int param) {
        AndroidNativeUtil.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                //view.setMethod(param);
            }
        });
    }

    public android.view.View getView() {
        return view;
    }

    public void setPermissions(final int param) {
        AndroidNativeUtil.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                //view.setPermissions(param);
            }
        });
    }

    public int getFlash() {
        try {
            /*
            In com.codename1.camerakit.Constants.java:
            public static final int FLASH_OFF = 0;
            public static final int FLASH_ON = 1;
            public static final int FLASH_AUTO = 2;
            public static final int FLASH_TORCH = 3;

             */

            switch (goldenEye.getConfig().getFlashMode()) {
                case ON:
                    return 1;
                case OFF:
                    return 0;
                case AUTO:
                    return 2;
                case TORCH:
                    return 3;
            }
            return 0;
        } catch (Exception ex) {
            Log.e(ex);
            Log.sendLogAsync();
        }
        return 0;
    }

    public void setZoom(final float param) {
        AndroidNativeUtil.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                try {
                    goldenEye.getConfig().setZoom((int) param);
                } catch (Exception ex) {
                    Log.e(ex);
                    Log.sendLogAsync();
                }
            }
        });
    }

    public void captureVideoFile(final String param) {
        if (!AndroidNativeUtil.checkForPermission(Manifest.permission.RECORD_AUDIO, "This application requires permission to record audio")) {
            CameraCallbacks.onError(null, "Permission to record denied", "Permission to record audio denied");
            return;
        }
        AndroidNativeUtil.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                String f = param;
                if (param.startsWith("file://")) {
                    f = param.substring(7);
                }
                final File file = new File(f);
                if (goldenEye.getConfig() != null) {
                    try {
                        goldenEye.getConfig().setPreviewScale(PreviewScale.AUTO_FILL);
                    } catch (Exception ex) {
                        Log.e(ex);
                        Log.sendLogAsync();
                    }
                };
                goldenEye.startRecording(file, new VideoCallback() {
                    @Override
                    public void onVideoRecorded(File file) {
                        CameraCallbacks.onVideo("file://" + file.getAbsolutePath());
                    }

                    @Override
                    public void onError(Throwable ex) {
                        com.codename1.io.Log.e(ex);
                        CameraCallbacks.onError(null, ex.getMessage(), ex.getMessage());
                    }
                });

            }
        });
    }

    public void setFocus(final int param) {
        /*
         public static final int FOCUS_OFF = 0;
    public static final int FOCUS_CONTINUOUS = 1;
    public static final int FOCUS_TAP = 2;
    public static final int FOCUS_TAP_WITH_MARKER = 3;
         */
        AndroidNativeUtil.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                try {
                    switch (param) {
                        case 0:

                            //view.setFocusable(false);
                            goldenEye.getConfig().setFocusMode(FocusMode.FIXED);
                            goldenEye.getConfig().setTapToFocusEnabled(false);
                            break;
                        case 1:
                            goldenEye.getConfig().setFocusMode(FocusMode.CONTINUOUS_VIDEO);
                            goldenEye.getConfig().setTapToFocusEnabled(false);
                            break;
                        case 2:
                            goldenEye.getConfig().setFocusMode(FocusMode.AUTO);
                            goldenEye.getConfig().setTapToFocusEnabled(true);
                            break;
                        case 3:
                            goldenEye.getConfig().setFocusMode(FocusMode.AUTO);
                            goldenEye.getConfig().setTapToFocusEnabled(true);
                            break;
                    }
                } catch (Exception ex) {
                    Log.e(ex);
                    Log.sendLogAsync();
                }
            }
        });
    }

    public void setFlash(final int param) {
        /*
        public static final int FLASH_OFF = 0;
    public static final int FLASH_ON = 1;
    public static final int FLASH_AUTO = 2;
    public static final int FLASH_TORCH = 3;
         */
        AndroidNativeUtil.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                try {
                    switch (param) {
                        case 0:
                            goldenEye.getConfig().setFlashMode(FlashMode.OFF);
                            break;
                        case 1:
                            goldenEye.getConfig().setFlashMode(FlashMode.ON);
                            break;
                        case 2:
                            goldenEye.getConfig().setFlashMode(FlashMode.AUTO);
                            break;
                        case 3:
                            goldenEye.getConfig().setFlashMode(FlashMode.TORCH);
                            break;
                    }
                } catch (Exception ex) {
                    Log.e(ex);
                    Log.sendLogAsync();
                }

            }
        });
    }

    public float getVerticalViewingAngle() {
        return 0;
    }

    public float getHorizontalViewingAngle() {
        return 0;
    }

    public int getFacing() {
        try {
            if (goldenEye.getConfig() != null) {
                switch (goldenEye.getConfig().getFacing()) {
                    case BACK:
                        return 0;
                    default:
                        return 1;
                }
            }
        } catch (Exception ex) {
            Log.e(ex);
            Log.sendLogAsync();
        }
        return 1; // fallback for errors
    }

    public boolean isFacingFront() {
        return goldenEye.getConfig().getFacing() == Facing.FRONT;
    }

    public boolean isFacingBack() {
        return goldenEye.getConfig().getFacing() == Facing.BACK;
    }

    public void setFacing(final int param) {
        AndroidNativeUtil.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                switch (param) {
                    case Constants.FACING_BACK:
                        if (!isFacingBack()) {
                            started = false;
                            goldenEye.release();
                            CameraInfo cam = null;
                            for (CameraInfo c : goldenEye.getAvailableCameras()) {
                                if (c.getFacing() == Facing.BACK) {
                                    cam = c;
                                    break;
                                }
                            }
                            if (cam == null) {
                                initCallback.onError(new IllegalStateException("No back camera found"));
                                return;
                            }
                            goldenEye.open(view, cam, initCallback);
                            Timer t = new Timer();
                            t.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    try {
                                        if (goldenEye.getConfig() != null && width > 0 && height > 0) {
                                            goldenEye.getConfig().setPreviewSize(new Size(width, height));
                                            goldenEye.getConfig().setPictureSize(new Size(width, height));
                                        }
                                    } catch (Exception ex) {
                                        Log.e(ex);
                                        Log.sendLogAsync();
                                    }
                                }
                            }, 500L);
                        }
                        break;
                    default:
                        if (!isFacingFront()) {
                            started = false;
                            goldenEye.release();
                            CameraInfo cam = null;
                            for (CameraInfo c : goldenEye.getAvailableCameras()) {
                                if (c.getFacing() == Facing.FRONT) {
                                    cam = c;
                                    break;
                                }
                            }
                            if (cam == null) {
                                initCallback.onError(new IllegalStateException("No front camera found"));
                                return;
                            }
                            goldenEye.open(view, cam, initCallback);
                            Timer t = new Timer();
                            t.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    try {
                                        if (goldenEye.getConfig() != null && width > 0 && height > 0) {
                                            goldenEye.getConfig().setPreviewSize(new Size(width, height));
                                            goldenEye.getConfig().setPictureSize(new Size(width, height));
                                        }
                                    } catch (Exception ex) {
                                        Log.e(ex);
                                        Log.sendLogAsync();
                                    }
                                }
                            }, 500L);
                        }

                }
            }
        });
    }

    public void setPinchToZoom(final boolean param) {
        AndroidNativeUtil.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                //view.
                try {
                    goldenEye.getConfig().setPinchToZoomEnabled(param);
                } catch (Exception ex) {
                    Log.e(ex);
                    Log.sendLogAsync();
                }
            }
        });
    }

    public void setVideoQuality(final int param) {
        AndroidNativeUtil.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                try {
                    switch (param) {
                        case Constants.VIDEO_QUALITY_480P:
                            goldenEye.getConfig().setVideoQuality(VideoQuality.HIGH_SPEED_480P);

                            break;
                        case Constants.VIDEO_QUALITY_720P:
                            goldenEye.getConfig().setVideoQuality(VideoQuality.RESOLUTION_720P);
                            break;
                        case Constants.VIDEO_QUALITY_1080P:
                            goldenEye.getConfig().setVideoQuality(VideoQuality.RESOLUTION_1080P);
                            break;
                        case Constants.VIDEO_QUALITY_2160P:
                            goldenEye.getConfig().setVideoQuality(VideoQuality.RESOLUTION_2160P);
                            break;
                        case Constants.VIDEO_QUALITY_HIGHEST:
                            goldenEye.getConfig().setVideoQuality(VideoQuality.HIGH);
                            break;
                        case Constants.VIDEO_QUALITY_LOWEST:
                            goldenEye.getConfig().setVideoQuality(VideoQuality.LOW);
                            break;
                        case Constants.VIDEO_QUALITY_QVGA:
                            goldenEye.getConfig().setVideoQuality(VideoQuality.LOW);
                            break;

                    }
                } catch (Exception ex) {
                    Log.e(ex);
                    Log.sendLogAsync();
                }
            }
        });
    }

    public void setVideoBitRate(final int param) {
        AndroidNativeUtil.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                //goldenEye.getConfig().set
            }
        });
    }

    public void setLockVideoAspectRatio(final boolean param) {
        AndroidNativeUtil.getActivity().runOnUiThread(new Runnable() {
            public void run() {

            }
        });
    }

    public void setJpegQuality(final int param) {
        AndroidNativeUtil.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                //view.
                try {
                    goldenEye.getConfig().setPictureQuality(param);
                } catch (Exception ex) {
                    Log.e(ex);
                    Log.sendLogAsync();
                }
            }
        });
    }

    public void setCropOutput(final boolean param) {
        AndroidNativeUtil.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                //view.setCr

            }
        });
    }

    public int toggleFacing() {
        //return view.toggleFacing();
        return 0;
    }

    public int toggleFlash() {
        //return view.toggleFlash();
        return 0;
    }

    public void captureImage() {
        AndroidNativeUtil.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                goldenEye.takePicture(new PictureCallback() {
                    @Override
                    public void onPictureTaken(Bitmap bmp) {
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bmp.compress(Bitmap.CompressFormat.JPEG, 70, stream);
                        byte[] byteArray = stream.toByteArray();
                        CameraCallbacks.onImage(byteArray);

                    }

                    @Override
                    public void onError(Throwable throwable) {
                        initCallback.onError(throwable);
                    }
                });
            }
        });
    }

    public void captureVideo() {

        FileSystemStorage fs = FileSystemStorage.getInstance();
        String path = fs.toNativePath(new com.codename1.io.File("TempCameraKitVideo.mp4").getAbsolutePath());
        captureVideoFile(path);

    }

    public void stopVideo() {
        AndroidNativeUtil.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                goldenEye.stopRecording();
            }
        });
    }

    public int getPreviewWidth() {
        try {
            return goldenEye.getConfig().getPreviewSize().getWidth();
        } catch (Exception ex) {
            Log.e(ex);
            Log.sendLogAsync();
        }
        return 0;
    }

    public int getPreviewHeight() {
        try {
            return goldenEye.getConfig().getPreviewSize().getHeight();
        } catch (Exception ex) {
            Log.e(ex);
            Log.sendLogAsync();
        }
        return 0;
    }

    public int getCaptureWidth() {
        try {
            return goldenEye.getConfig().getVideoSize().getWidth();
        } catch (Exception ex) {
            Log.e(ex);
            Log.sendLogAsync();
        }
        return 0;
    }

    public int getCaptureHeight() {
        try {
            return goldenEye.getConfig().getVideoSize().getHeight();
        } catch (Exception ex) {
            Log.e(ex);
            Log.sendLogAsync();
        }
        return 0;
    }

    public boolean isSupported() {
        return true;
    }

    public void setPictureSize(final int w, final int h) {
        AndroidNativeUtil.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                try {
                    width = w;
                    height = h;
                    if (goldenEye.getConfig() != null) {
                        // If you call GoldenEye.config before InitCallback#onReady is dispatched, returned config will be null
                        goldenEye.getConfig().setPreviewSize(new Size(width, height));
                        goldenEye.getConfig().setPictureSize(new Size(width, height));
                    }
                } catch (Exception ex) {
                    Log.e(ex);
                    Log.sendLogAsync();
                }
            }
        });
    }

    /**
     * CaptureImage=0, CaptureVideo=1, Zoom=2, Focus=3, Flash=4, Crop=5
     * HorizontalViewingAngle=6 VerticalViewingAngle=7 ToggleFacing=8
     */
    public boolean supportsFeature(int feature) {
        return feature >= 0 && feature < 9;
    }

}
