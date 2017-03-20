package com.liuyt.liveshow;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import com.liuyt.liveshow.Activity.BaseActivity;
import com.liuyt.liveshow.Camera.CameraHelper;
import com.liuyt.liveshow.coder.AvcEncoder;
import com.liuyt.liveshow.utils.BitmapUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;

public class MainActivity3 extends BaseActivity implements SurfaceHolder.Callback, Camera.PreviewCallback {

    private ImageView imgV;
    private Button btnSwitch;
    private Button btnPush;
    private SurfaceView prevewview;
    private SurfaceHolder surfaceHolder;

    AvcEncoder avcCodec;
    public CameraHelper cameraHelper;
    int width = 720;
    int height = 480;
    int framerate = 20;
    int bitrate = 2500000;

    private SurfaceView mTimestampSurfaceView;
    private SurfaceHolder mTimestampSurfaceHolder;
    byte[] h264 = new byte[width * height * 3 / 2];
    private int cameraId = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected int getWindowFlags() {
        return WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;//保持屏幕常亮
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main2;
    }

    @Override
    protected void initView() {
        prevewview = (SurfaceView) findViewById(R.id.surface_view);
        mTimestampSurfaceView = (SurfaceView) findViewById(R.id.surface_view1);
        imgV = (ImageView) findViewById(R.id.img);
        btnSwitch = (Button) findViewById(R.id.switch_camera);
        btnPush = (Button) findViewById(R.id.btn_push);
    }

    @Override
    protected void setupProcess() {
        cameraHelper = new CameraHelper(MainActivity3.this);
        cameraHelper.setSurfaceView(prevewview);

        avcCodec = new AvcEncoder(width, height, framerate, bitrate);
        surfaceHolder = prevewview.getHolder(); // 绑定SurfaceView，取得SurfaceHolder对象
        surfaceHolder.setFixedSize(width, height); // 预览大小設置
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceHolder.addCallback(this);

        mTimestampSurfaceView.getHolder().addCallback(new TimestampSurfaceHolder());

        imgV.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        btnSwitch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                Log.i("jw", "number:" + Camera.getNumberOfCameras());
                for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
                    Camera.getCameraInfo(i, cameraInfo);
                    Log.i("jw", "oritation:" + cameraInfo.orientation + ",facing:" + cameraInfo.facing);
                    if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    } else if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    }
                }

                cameraId = 1 - cameraId;
                cameraHelper.setCameraId(cameraId);
            }
        });

        btnSwitch.setOnClickListener(new OnClickListener(){
                                         @Override
                                         public void onClick(View view) {

                                         }
                                     });

    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Size size = camera.getParameters().getPreviewSize();
        Log.i("jw", "width:" + size.width + ",height:" + size.height);
        try {
            /*//这张方式可以将NV21格式数据转化成Bitmap
        	int[] imgData = DataFormatUtils.NV21toARGB(data, size.width, size.height);
        	Bitmap bmp = Bitmap.createBitmap(imgData, size.width, size.height, Config.ARGB_8888);
        	imgV.setImageBitmap(BitmapUtils.addTimeToBitmap(bmp, System.currentTimeMillis()));*/

            //直接使用系统的YuvImage来进行转化图片，这里的支持ImageFormat.NV21和ImageFormat.YUY2,
            //但是YUY2的Camera是不支持的，所以这里会出现花屏现象
            YuvImage image = new YuvImage(data, ImageFormat.NV21, size.width, size.height, null);
            Bitmap bmp = null;
            if (image != null) {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                image.compressToJpeg(new Rect(0, 0, size.width, size.height), 80, stream);
                bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
                //图片旋转
                bmp = BitmapUtils.rotateBitmap(bmp, cameraId == 0 ? 90 : 270);
                //加水印
                bmp = BitmapUtils.addTimeToBitmap(bmp, System.currentTimeMillis());
                stream.close();
            }

            imgV.setImageBitmap(bmp);

            try {
                Canvas canvas = mTimestampSurfaceHolder.lockCanvas();
                canvas.drawBitmap(bmp, 0, 0, new Paint());
                mTimestampSurfaceHolder.unlockCanvasAndPost(canvas);
            } catch (Exception e) {
                Log.i("jw", "canvas bitmap error:" + Log.getStackTraceString(e));
            }

        } catch (Exception ex) {
            Log.e("Sys", "Error:" + ex.getMessage());
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        cameraHelper.setCameraId(cameraId);
        cameraHelper.openCamera(prevewview.getWidth(),prevewview.getHeight());
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        avcCodec.close();
    }

    private File getOutputMediaFile() {
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("linc", "failed to create directory");
                return null;
            }
        }

        File mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG.jpg");
        return mediaFile;
    }

    private class TimestampSurfaceHolder implements SurfaceHolder.Callback {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            mTimestampSurfaceHolder = holder;
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                   int height) {
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }
    }
}
