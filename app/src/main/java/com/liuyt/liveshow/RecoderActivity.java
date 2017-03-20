package com.liuyt.liveshow;

import java.io.File;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;

/**
 * Description:
 * ʹ��MediaRecorder¼����Ƶ
 * @author jph
 * Date:2014.08.14
 * <br/>
 */
public class RecoderActivity extends Activity implements OnClickListener {
	// �����е�������ť
	ImageButton record , stop;
	// ϵͳ����Ƶ�ļ�
	File videoFile ;
	MediaRecorder mRecorder;
	// ��ʾ��ƵԤ����SurfaceView
    SurfaceView sView;
    // ��¼�Ƿ����ڽ���¼��
    private boolean isRecording = false;

    @Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		// ȥ�������� ,�������setContentView֮ǰ
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);		
		 // ���ú�����ʾ   
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); 
        // ����ȫ��   
	    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,   
	                WindowManager.LayoutParams.FLAG_FULLSCREEN);
	    // ѡ��֧�ְ�͸��ģʽ,����surfaceview��activity��ʹ�á�   
        getWindow().setFormat(PixelFormat.TRANSLUCENT);   
		// ��ȡ��������е�������ť
		record = (ImageButton) findViewById(R.id.record);
		stop = (ImageButton) findViewById(R.id.stop);
		// ��stop��ť�����á�
		stop.setEnabled(false);
		// Ϊ������ť�ĵ����¼��󶨼�����
		record.setOnClickListener(this);
		stop.setOnClickListener(this);
		// ��ȡ��������е�SurfaceView
		sView = (SurfaceView) this.findViewById(R.id.sView);
        // ���÷ֱ���
        sView.getHolder().setFixedSize(1280, 720);
        // ���ø��������Ļ�����Զ��ر�
        sView.getHolder().setKeepScreenOn(true);
	}

	@Override
	public void onClick(View source){
		switch (source.getId()){
			// ����¼�ư�ť
			case R.id.record:
				if (!Environment.getExternalStorageState().equals(
					Environment.MEDIA_MOUNTED)){
					Toast.makeText(this
						, "SD�������ڣ������SD����"
						, Toast.LENGTH_SHORT).show();
					return;
				}
				try
				{
					// ��������¼����Ƶ����Ƶ�ļ�
					videoFile = new File(Environment
						.getExternalStorageDirectory()
						.getCanonicalFile() + "/testvideo.3gp");
					// ����MediaPlayer����
					mRecorder = new MediaRecorder();
					mRecorder.reset();
					// ���ô���˷�ɼ�����(������¼���������AudioSource.CAMCORDER)
					mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
					// ���ô�����ͷ�ɼ�ͼ��
					mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
					// ������Ƶ�ļ��������ʽ
					// �������������������ʽ��ͼ������ʽ֮ǰ����
					mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
					// ������������ĸ�ʽ
					mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
					// ����ͼ�����ĸ�ʽ
					mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
					mRecorder.setVideoSize(1280, 720);
					// ÿ�� 4֡
					mRecorder.setVideoFrameRate(20);
					mRecorder.setOutputFile(videoFile.getAbsolutePath());
					// ָ��ʹ��SurfaceView��Ԥ����Ƶ
					mRecorder.setPreviewDisplay(sView
						.getHolder().getSurface());  //
					mRecorder.prepare();
					// ��ʼ¼��
					mRecorder.start();
					System.out.println("---recording---");
					// ��record��ť�����á�
					record.setEnabled(false);
					// ��stop��ť���á�
					stop.setEnabled(true);
					isRecording = true;
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				break;
			// ����ֹͣ��ť
			case R.id.stop:
				// ������ڽ���¼��
				if (isRecording)
				{
					// ֹͣ¼��
					mRecorder.stop();
					// �ͷ���Դ
					mRecorder.release();
					mRecorder = null;
					// ��record��ť���á�
					record.setEnabled(true);
					// ��stop��ť�����á�
					stop.setEnabled(false);
				}
				break;
		}
	}
}
