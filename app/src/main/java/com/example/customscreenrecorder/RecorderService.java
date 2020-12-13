package com.example.customscreenrecorder;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RecorderService extends Service implements SensorEventListener {
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
    private Button button;
    private boolean isStarted = false;
    private boolean firstTime =true;
    //private WindowManager windowManager = (WindowManager)getSystemService(Context.WINDOW_SERVICE);


    private String filePath;

    private SensorManager sm = null;
    private Sensor sensor =null;
    private Vibrator vb = null;
    private float acc;
    private float accCurrent;
    private float accLast;
    private boolean accb =true;
    private View floatView;
    //private WindowManager.LayoutParams lp;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        accb =true;
        acc= 0 ;
        accCurrent=0;
        accLast=0;
        sm  = (SensorManager)getSystemService(SENSOR_SERVICE);
        vb = (Vibrator)getSystemService(Service.VIBRATOR_SERVICE);
        sensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sm.registerListener(this,sensor,SensorManager.SENSOR_DELAY_UI, new Handler());

        int resultCode = intent.getIntExtra("resultCode",-1);
        Intent data = intent.getParcelableExtra("data");
        width = intent.getIntExtra("width",720);
        height = intent.getIntExtra("height",1280);
        density = intent.getIntExtra("density",0);
        //floatView = intent.getParcelableExtra("floatView");
        //lp = intent.getParcelableExtra("lp");
        mp = ((MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE)).getMediaProjection(resultCode, data);
        mediaRecorder = createMediaRecorder();
        vd = mp.createVirtualDisplay("recorder", width, height, density, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mediaRecorder.getSurface(), null, null);
        mediaRecorder.start();
        return Service.START_NOT_STICKY;
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
        //windowManager.addView(floatView,lp);


        Intent intent = new Intent();
        intent.putExtra("Recreate", true);
        intent.putExtra("filePath",filePath);
        intent.setAction("recreate.notice");
        sendBroadcast(intent);
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
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);  //after setOutputFormat()
        mediaRecorder.setVideoSize(width, height);  //after setVideoSource(), setOutFormat()
        filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) + "/" + videoQuality + curTime + ".mp4";
        mediaRecorder.setOutputFile(filePath);

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


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // TODO Auto-generated method stub
        int sensorType = event.sensor.getType();
        float[]values = event.values;
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        accLast = accCurrent;
        accCurrent = (float) Math.sqrt((double) (x * x + y * y + z * z));
        float delta = Math.abs( accCurrent - accLast);
        acc = acc * 0.9f + delta; // perform low-cut filter

        if (delta > 2) {
            if(accb){
                accb =false;
            }
            else{
                Log.i(TAG, "detect shaking");
                sm.unregisterListener(this);
                this.onDestroy();
            }

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
