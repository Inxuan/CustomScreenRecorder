package com.example.customscreenrecorder;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.hardware.SensorManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

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
    private Button button;
    private boolean isStarted = false;
    private boolean firstTime =true;
    private Button window;
    private View floatView;
    private WindowManager windowManager;
    private SensorManager sensor;
    private VideoView videoView;
    private Button btn_start,btn_end;
    private MediaController mediaController;
    private final WindowManager.LayoutParams layoutParams=new WindowManager.LayoutParams();
    private NoticeReceiver noticeReceiver;
    //public AlertDialog.Builder builder = new AlertDialog.Builder(this);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //get info of screen;
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        width = metrics.widthPixels;
        height =metrics.heightPixels;
        density = metrics.densityDpi;

        //sensor.unregisterListener(shakelistener);

        setContentView(R.layout.activity_main);
        //textView = (TextView) findViewById(R.id.tv);
        button =(Button)findViewById(R.id.startbutton);

        //windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        //setContentView(R.layout.window);


        //button = (Button)findViewById(R.id.bt_play);
        //setContentView(R.layout.activity_main);
        //windowManager.removeView(floatView);
        noticeReceiver = new NoticeReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("recreate.notice");
        registerReceiver(noticeReceiver, filter);
    }

    public void createWindow(){
        windowManager= (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        //设置悬浮窗布局属性
        //设置类型
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            layoutParams.type=WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }else {
            layoutParams.type=WindowManager.LayoutParams.TYPE_PHONE;
        }
        //设置行为选项
        layoutParams.flags=WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        //设置悬浮窗的显示位置
        layoutParams.gravity= Gravity.LEFT;
        //设置x周的偏移量
        layoutParams.x=0;
        //设置y轴的偏移量
        layoutParams.y=0;

        //如果悬浮窗图片为透明图片，需要设置该参数为PixelFormat.RGBA_8888
        layoutParams.format= PixelFormat.RGBA_8888;
        //设置悬浮窗的宽度
        layoutParams.width=24*5 * (density / 160);

        layoutParams.height=24*5 * (density / 160);


        floatView = LayoutInflater.from(this).inflate(R.layout.window,null);
        //加载显示悬浮窗

//        layoutParams.width*=0.9;
        //       layoutParams.height*=0.9;
        TouchListener tl = new TouchListener(floatView,windowManager,layoutParams);



        windowManager.addView(floatView,layoutParams);
    }

    public void onClick2(View view){
        moveTaskToBack(true);

        createWindow();
    }


    public  void onClick(View view){
        if(!isStarted){
            Log.i(TAG, "start");
            //((Button)view).setText("Stop");
            windowManager.removeView(floatView);

            manager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
            Intent captureIntent = manager.createScreenCaptureIntent();
            startActivityForResult(captureIntent, RECORD_CODE);
            isStarted=true;
        }
    /*    else{
            ((Button)view).setText("Start");
            Log.i(TAG, "else");
            Intent service = new Intent(this,RecorderService.class);
            stopService(service);
            isStarted = false;

            this.finish();
        }
    */
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RECORD_CODE){
            if(resultCode ==RESULT_OK) {
                moveTaskToBack(true);
                Intent service = new Intent(this, RecorderService.class);
                //service.putExtra("floatView",(Parcelable)floatView);
                //service.putExtra("lp",layoutParams);
                service.putExtra("resultCode", resultCode);
                service.putExtra("data", data);
                service.putExtra("width", width);
                service.putExtra("height", height);
                service.putExtra("density", density);
                startService(service);
                //service.putExtra("resultCode",resultCode);
            }
        }
        //WindowManager w = (WindowManager) new WindowManager.LayoutParams(WindowManager.LayoutParams.TYPE_TOAST);



    }

    public class NoticeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String intentAction = intent.getAction();
            if (intentAction.equals("recreate.notice")) {
                windowManager.addView(floatView,layoutParams);

                isStarted=false;

            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(noticeReceiver);
    }

    public void onClickHome(View view) {
        setContentView(R.layout.activity_main);
    }
    public void onClickView(View view) {
        setContentView(R.layout.view_video);
    }

    public WindowManager.LayoutParams getLayoutParams(){
        return this.layoutParams;
    }
}