package com.liuyt.liveshow;

import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.liuyt.liveshow.Activity.BaseActivity;
import com.liveshow.media.MediaPublisher;

public class MainActivity extends BaseActivity implements SurfaceHolder.Callback, Camera.PreviewCallback {
    private static final String TAG = "MainActivity";
    private Button btnSwitch;
    private SurfaceView mPrevewview;
    private SurfaceHolder mSurfaceHolder;

    private boolean isPublished;
    private MediaPublisher mMediaPublisher = new MediaPublisher();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMediaPublisher.init();
    }

    @Override
    protected int getWindowFlags() {
        return WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;//保持屏幕常亮
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initView() {
        mPrevewview = (SurfaceView) findViewById(R.id.surface_view);
        btnSwitch = (Button) findViewById(R.id.switch_camera);
    }

    @Override
    protected void setupProcess() {
        mPrevewview.setKeepScreenOn(true);
        mSurfaceHolder = mPrevewview.getHolder(); // 绑定SurfaceView，取得SurfaceHolder对象
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mSurfaceHolder.addCallback(this);

        btnSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchPublish();
            }
        });

    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int i, int i1, int i2) {
        Log.i(TAG, "surfaceChanged: ");
        mMediaPublisher.initVideoGatherer(MainActivity.this, holder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

    private void switchPublish() {
        if (isPublished) {
            stop();
        } else {
            start();
        }
        btnSwitch.setText(isPublished ? "停止" : "开始");
    }

    private void start() {
        //初始化声音采集
        mMediaPublisher.initAudioGatherer();
        //初始化编码器
        mMediaPublisher.initEncoders();
        //开始采集
        mMediaPublisher.startGather();
        //开始编码
        mMediaPublisher.startEncoder();
        //开始推送
        mMediaPublisher.starPublish("rtmp://192.168.1.108:1935/live/test");
        isPublished = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume: ");
        mMediaPublisher.initVideoGatherer(this, mSurfaceHolder);

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause: ");
        stop();
    }

    private void stop() {
        mMediaPublisher.stopPublish();
        mMediaPublisher.stopGather();
        mMediaPublisher.stopEncoder();
        isPublished = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMediaPublisher.release();
    }
}
