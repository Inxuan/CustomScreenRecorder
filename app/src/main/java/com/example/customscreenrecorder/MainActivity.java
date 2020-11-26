package com.example.customscreenrecorder;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private MediaProjectionManager manager;
    private MediaProjection mp;
    private VirtualDisplay vd;
    private static final int RECORD_CODE = 201;
    private MediaRecorder mediaRecorder;
    private boolean isVideoSd = true;
    private boolean isAudio = true;
    private int width = 360;
    private int height = 640;
    private int density = 0;
    private static final String TAG = "creatingMediaRecorder";
    private TextView textView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //get info of screen;
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        width = metrics.widthPixels;
        height =metrics.heightPixels;
        density = metrics.densityDpi;
        textView = (TextView) findViewById(R.id.tv);

        manager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        Intent captureIntent = manager.createScreenCaptureIntent();

        startActivityForResult(captureIntent, RECORD_CODE);
        setContentView(R.layout.activity_main);
        //windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        MediaProjection  mp =manager.getMediaProjection(resultCode,data);


        MediaRecorder mediaRecorder =createMediaRecorder();
        vd = mp.createVirtualDisplay("recorder",width,height,density, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mediaRecorder.getSurface(),null,null);
        mediaRecorder.start();
        //WindowManager w = (WindowManager) new WindowManager.LayoutParams(WindowManager.LayoutParams.TYPE_TOAST);
        mediaRecorder.stop();
        mediaRecorder.release();
    }

    private MediaRecorder createMediaRecorder() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        Date curDate = new Date(System.currentTimeMillis());
        String curTime = formatter.format(curDate).replace(" ", "");
        String videoQuality = "HD";
        if(isVideoSd) videoQuality = "SD";

        Log.i(TAG, "Create MediaRecorder");
        MediaRecorder mediaRecorder = new MediaRecorder();
        //if(isAudio) mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setOutputFile(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) + "/" + videoQuality + curTime + ".mp4");
        mediaRecorder.setVideoSize(width, height);  //after setVideoSource(), setOutFormat()
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);  //after setOutputFormat()
        //if(isAudio) mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);  //after setOutputFormat()
        int bitRate;
        if(isVideoSd) {
            mediaRecorder.setVideoEncodingBitRate(width * height);
            mediaRecorder.setVideoFrameRate(30);
            bitRate = width * height / 1000;
        } else {
            mediaRecorder.setVideoEncodingBitRate(5 * width * height);
            mediaRecorder.setVideoFrameRate(60); //after setVideoSource(), setOutFormat()
            bitRate = 5 * width * height / 1000;
        }
        try {
            mediaRecorder.prepare();
        } catch (IllegalStateException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Log.i(TAG, "Audio: " + isAudio + ", SD video: " + isVideoSd + ", BitRate: " + bitRate + "kbps");

        return mediaRecorder;
    }


    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        Log.i(TAG, "Service onDestroy");
        if(vd != null) {
            vd.release();
            vd = null;
        }
        if(mediaRecorder != null) {
            mediaRecorder.setOnErrorListener(null);
            mp.stop();
            mediaRecorder.reset();
        }
        if(mp != null) {
            mp.stop();
            mp = null;
        }
    }


}