package com.liuyt.liveshow.Camera;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.SurfaceView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static android.provider.MediaStore.Images.ImageColumns.ORIENTATION;

/**
 * Created by liuyt on 2017/3/19.
 */

public class CameraHelper {
    private final static String TAG = "CameraHelper";
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    Context mCtx;
    CameraManager mCameraManager;
    SurfaceView mView;

    private String mCameraId;
    /**
     * A {@link CameraCaptureSession } for camera preview.
     */
    private CameraCaptureSession mCaptureSession;
    /**
     * A reference to the opened {@link CameraDevice}.
     */
    private CameraDevice mCameraDevice;
    /**
     * {@link CaptureRequest.Builder} for the camera preview
     */
    private CaptureRequest.Builder mPreviewRequestBuilder;

    private Size mPreviewSize;
    private ImageReader mImageReader;
    Handler childHandler;
    public CameraHelper(Context context) {
        this.mCtx = context;
    }

    public void setSurfaceView(SurfaceView view) {
        this.mView = view;
    }

    public void setCameraId(int cameraId) {
        mCameraId = "" + cameraId;
    }

    public void initHandler(){
        HandlerThread handlerThread = new HandlerThread("Camera2");
        handlerThread.start();
        childHandler = new Handler(handlerThread.getLooper());

    }
    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice cameraDevice) {
            mCameraDevice = cameraDevice;
            Log.e(TAG, "onOpened:" + mCameraDevice);
            //begin preview
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(CameraDevice cameraDevice, int i) {
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onClosed(CameraDevice camera) {
            super.onClosed(camera);
        }
    };

    /**
     * get still picture
     */
    public void captureStillPicture() {
        try {
            if (mCameraDevice == null) {
                return;
            }
            final CaptureRequest.Builder captureRequest = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            // 将imageReader的surface作为CaptureRequest.Builder的目标
            captureRequest.addTarget(mImageReader.getSurface());
            // 设置自动对焦模式
            captureRequest.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            // 设置自动曝光模式
            captureRequest.set(CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
            // 获取设备方向
            int rotation = mCtx.getResources().getConfiguration().orientation;
            // 根据设备方向计算设置照片的方向
            captureRequest.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));
            // 停止连续取景
            mCaptureSession.stopRepeating();
            // 捕获静态图像
            mCaptureSession.capture(captureRequest.build(), new CameraCaptureSession.CaptureCallback() {
                // 拍照完成时激发该方法
                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    try {
                        // 重设自动对焦模式
                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                                CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);

                        // 设置自动曝光模式
                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                                CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);

                        // 打开连续取景模式
                        mCaptureSession.setRepeatingRequest(request, null, null);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
//    private void restartPreview() {
//        try {
//            //执行setRepeatingRequest方法就行了，注意mCaptureRequest是之前开启预览设置的请求
//            mCaptureSession.setRepeatingRequest(mCaptureRequest, null, mCameraHandler);
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
//    }
    public void capturePicture() {
        try {
            if (mCameraDevice == null) {
                return;
            }
            final CaptureRequest.Builder captureRequest = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            // 将imageReader的surface作为CaptureRequest.Builder的目标
            captureRequest.addTarget(mImageReader.getSurface());
            // 设置自动对焦模式
            captureRequest.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            // 设置自动曝光模式
            captureRequest.set(CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
            // 获取设备方向
            int rotation = mCtx.getResources().getConfiguration().orientation;
            // 根据设备方向计算设置照片的方向
            captureRequest.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));
            // 停止连续取景
            mCaptureSession.stopRepeating();
            // 捕获静态图像
            mCaptureSession.capture(captureRequest.build(), new CameraCaptureSession.CaptureCallback() {
                // 拍照完成时激发该方法
                @Override
                public void onCaptureCompleted(CameraCaptureSession session
                        , CaptureRequest request, TotalCaptureResult result) {
                    try {
                        // 重设自动对焦模式
                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                                CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);

                        // 设置自动曝光模式
                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                                CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);

                        // 打开连续取景模式
                        mCaptureSession.setRepeatingRequest(request, null, null);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }
            }, childHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void createCameraPreviewSession() {
        try {
            // 创建作为预览的CaptureRequest.Builder
            mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            Surface mSurface = mView.getHolder().getSurface();
            // 将surface作为CaptureRequest.Builder的目标
            mPreviewRequestBuilder.addTarget(mSurface);
            // 创建CameraCaptureSession，该对象负责管理处理预览请求和拍照请求
            mCameraDevice.createCaptureSession(Arrays.asList(mSurface,mImageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                            Log.e(TAG, "onConfigured:" + mCameraDevice);
                            // 如果摄像头为null，直接结束方法
                            if (null == mCameraDevice) {
                                Toast.makeText(mCtx, "null == mCameraDevice", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            // 当摄像头已经准备好时，开始显示预览
                            mCaptureSession = cameraCaptureSession;
                            try {
                                // 设置自动对焦模式
                                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                                // 设置自动曝光模式
                                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                                        CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                                // 开始显示相机预览
                                CaptureRequest previewRequest = mPreviewRequestBuilder.build();
                                // 设置预览时连续捕获图像数据
                                mCaptureSession.setRepeatingRequest(previewRequest, null, null);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                            Toast.makeText(mCtx, "配置失败！", Toast.LENGTH_SHORT).show();
                        }
                    }, childHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    private void requestCameraPermission() {
//        ActivityCompat.requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_PERMISSION_CAMERA_CODE);
    }

    public void openCamera(int width, int height) {
        if (ContextCompat.checkSelfPermission(mCtx, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission();
            return;
        }
        mCameraManager = (CameraManager) mCtx.getSystemService(Context.CAMERA_SERVICE);
        setupImageReader(new Size(width,height));
        try {
            //遍历所有摄像头
            for (String id : mCameraManager.getCameraIdList()) {
                // 获取指定摄像头的特性
                CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(mCameraId);
                // 获取摄像头支持的配置属性
                StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                // 获取摄像头支持的最大尺寸
                Size largest = Collections.max(Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),
                        new CompareSizesByArea());
                // 获取最佳的预览尺寸
                mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), width, height, largest);
                //默认打开后置摄像头
                if (characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT)
                    continue;
            }
            //打开相机，第一个参数指示打开哪个摄像头，
            // 第二个参数stateCallback为相机的状态回调接口，
            // 第三个参数用来确定Callback在哪个线程执行，为null的话就在当前线程执行
            mCameraManager.openCamera(mCameraId, mStateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

    }

    private void setupImageReader(Size size) {
        // 创建一个ImageReader对象，用于获取摄像头的图像数据
        mImageReader = ImageReader.newInstance(size.getWidth(), size.getHeight(),
                ImageFormat.JPEG, 2);
        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            // 当照片数据可用时激发该方法
            @Override
            public void onImageAvailable(ImageReader reader) {
                // 获取捕获的照片数据
                Image image = reader.acquireLatestImage();
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.remaining()];
                // 使用IO流将照片写入指定文件
                File file = new File(mCtx.getExternalFilesDir(null), "pic.jpg");
                buffer.get(bytes);
                try (FileOutputStream output = new FileOutputStream(file)) {
                    output.write(bytes);
                    Toast.makeText(mCtx, "保存: " + file, Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    image.close();
                }
            }
        }, childHandler);
    }


    private static Size chooseOptimalSize(Size[] choices, int width, int height, Size aspectRatio) {
        // 收集摄像头支持的大过预览Surface的分辨率
        List<Size> bigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getHeight() == option.getWidth() * h / w &&
                    option.getWidth() >= width && option.getHeight() >= height) {
                bigEnough.add(option);
            }
        }
        // 如果找到多个预览尺寸，获取其中面积最小的
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else {
            System.out.println("找不到合适的预览尺寸！！！");
            return choices[0];
        }
    }

    // 为Size定义一个比较器Comparator
    static class CompareSizesByArea implements Comparator<Size> {
        @Override
        public int compare(Size lhs, Size rhs) {
            // 强转为long保证不会发生溢出
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }
    }
}
