package com.example.customscreenrecorder;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.SensorManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaMetadataRetriever;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ImageView imageView;//声明ImageView对象
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

    public void onClickVideo(View view) {
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
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)+ "/";
        Log.e("Path",path+"");
        List<String>  files = getFilesAllName(path);


        for (String fileName:files) {
            Log.e("Path",fileName+"");
            getFirstframe(fileName);

        }
    }
    private void getFirstframe(String path) {
        imageView=(ImageView)findViewById(R.id.imageView);//获取布局管理器中的ImageView控件
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();//实例化MediaMetadataRetriever对象

        File file = new File(path);//实例化File对象，文件路径为/storage/emulated/0/shipin.mp4 （手机根目录）
        if (!file.exists()) {
            Toast.makeText(MainActivity.this, "文件不存在", Toast.LENGTH_SHORT).show();
        }
        mmr.setDataSource(path);
        Bitmap bitmap = mmr.getFrameAtTime(0);  //0表示首帧图片
        mmr.release(); //释放MediaMetadataRetriever对象
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);//设置ImageView显示的图片
            //存储媒体已经挂载，并且挂载点可读/写。
        } else {
            Toast.makeText(MainActivity.this, "获取视频缩略图失败", Toast.LENGTH_SHORT).show();
        }
    }


    public List<String> getFilesAllName(String path) {
        File file=new File(path);
        File[] files=file.listFiles();
        if (files == null){Log.e("error","空目录");return null;}
        List<String> s = new ArrayList<>();
        for(int i =0;i<files.length;i++){
            if(isVideo(files[i].getName())){
                Log.e("Path",files[i].getName()+"");
                s.add(path+files[i].getName());
            }
        }
        return s;
    }

    private boolean isVideo(String name) {
        if (name.endsWith( ".mp4" )) {
            return true;
        }
        return false;
    }

    public WindowManager.LayoutParams getLayoutParams(){
        return this.layoutParams;
    }
}