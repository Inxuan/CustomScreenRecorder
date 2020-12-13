package com.example.customscreenrecorder;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.hardware.SensorManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Environment;
import android.widget.ImageView;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
    private View floatView2;
    private WindowManager windowManager;
    private SensorManager sensor;
    private VideoView videoView;
    private Button btn_start,btn_end;
    private MediaController mediaController;
    private final WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
    private final WindowManager.LayoutParams layoutParams2 =new WindowManager.LayoutParams();
    private NoticeReceiver noticeReceiver;
    private ArrayList<Bitmap> images = new ArrayList<Bitmap>();
    private HashMap<ImageView,String> map = new HashMap<ImageView,String>();
    private ArrayList<String> paths = new ArrayList<String>();
    private boolean floatButtonIsShowing = false;
    private VideoView playView;
    private VideoView playView2;
    private String currentPath;


    //public AlertDialog.Builder builder = new AlertDialog.Builder(this);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
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
        //floatButtonIsShowing=false;
        noticeReceiver = new NoticeReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("recreate.notice");
        filter.addAction("quit.notice");
        registerReceiver(noticeReceiver, filter);
    }

    public void createWindow(){
        windowManager= (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        //设置悬浮窗布局属性
        //设置类型
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            layoutParams.type=WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            layoutParams2.type=WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }else {
            layoutParams.type=WindowManager.LayoutParams.TYPE_PHONE;
            layoutParams2.type=WindowManager.LayoutParams.TYPE_PHONE;
        }
        //设置行为选项
        layoutParams.flags=WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        layoutParams2.flags=WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        //设置悬浮窗的显示位置
        layoutParams.gravity= Gravity.LEFT;
        layoutParams2.gravity= Gravity.LEFT;
        //设置x周的偏移量
        layoutParams.x=0;
        layoutParams2.x=0;
        //设置y轴的偏移量
        layoutParams.y=0;
        layoutParams2.y=0;

        //如果悬浮窗图片为透明图片，需要设置该参数为PixelFormat.RGBA_8888
        layoutParams.format= PixelFormat.RGBA_8888;
        layoutParams2.format= PixelFormat.RGBA_8888;
        //设置悬浮窗的宽度
        layoutParams.width=24*5 * (density / 160);
        layoutParams2.width= width;
        layoutParams.height=24*5 * (density / 160);
        layoutParams2.height= height ;



        floatView2 = LayoutInflater.from(this).inflate(R.layout.window2,null);
        floatView = LayoutInflater.from(this).inflate(R.layout.window,null);






        //加载显示悬浮窗

//        layoutParams.width*=0.9;
        //       layoutParams.height*=0.9;
        TouchListener tl = new TouchListener(floatView,floatView2,windowManager,layoutParams,layoutParams2,width,height,this);

        addBackFloatWindow();
    }






    public void onClick2(View view){
        moveTaskToBack(true);
        if(!floatButtonIsShowing) {
            createWindow();
        }
    }

    public void onClickSave(View view){
        if(view.getId()==R.id.save_button) {
            playView.stopPlayback();
            playView.setOnCompletionListener(null);
            playView.setOnPreparedListener(null);
            setContentView(R.layout.activity_main);
            moveTaskToBack(true);
            addBackFloatWindow();
            isStarted = false;
        }
        else if(view.getId()==R.id.cancel_button){
            if(currentPath!=null) {
                File f = new File(currentPath);
                if (f.delete()) {
                    Toast.makeText(MainActivity.this, "delete successfully", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(MainActivity.this, "fail to delete", Toast.LENGTH_SHORT).show();
                }
                playView.stopPlayback();
                playView.setOnCompletionListener(null);
                playView.setOnPreparedListener(null);
                setContentView(R.layout.activity_main);
                moveTaskToBack(true);
                addBackFloatWindow();
                isStarted = false;
            }
        }
        else if(view.getId()==R.id.cancel_button2){
            playView2.stopPlayback();
            playView2.setOnCompletionListener(null);
            playView2.setOnPreparedListener(null);
            setContentView(R.layout.view_video);
            onClickView(null);
        }else if(view.getId()==R.id.delete_button){
            File f = new File(currentPath);
            if(f.delete()){
                Toast.makeText(MainActivity.this, "delete successfully", Toast.LENGTH_SHORT).show();

            }else{
                Toast.makeText(MainActivity.this, "fail to delete", Toast.LENGTH_SHORT).show();
            }
            playView2.stopPlayback();
            playView2.setOnCompletionListener(null);
            playView2.setOnPreparedListener(null);
            setContentView(R.layout.view_video);
            onClickView(null);
        }
    }


    public  void onClick(View view){
        if(!isStarted){
            Log.i(TAG, "start");
            //((Button)view).setText("Stop");
            windowManager.removeView(floatView);
            floatButtonIsShowing = false;
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
                ActivityManager activityManager =(ActivityManager) getSystemService(ACTIVITY_SERVICE);
                activityManager.moveTaskToFront(getTaskId(),0);
                currentPath = intent.getStringExtra("filePath");
                setContentView(R.layout.playview);
                playView =(VideoView)findViewById(R.id.play_view);
                playView.setVideoPath(currentPath);
                playView.start();
                playView.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
                {
                    @Override
                    public void onCompletion(MediaPlayer mp)
                    {
                        //播放结束后的动作


                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        //builder.setTitle("title：");
                        builder.setMessage("Do you want to save this video?");
                        builder.setIcon(R.mipmap.ic_launcher);
                        builder.setCancelable(false);            //点击对话框以外的区域是否让对话框消失

                        //设置正面按钮
                        builder.setPositiveButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //Toast.makeText(MainActivity.this, "你点击了确定", Toast.LENGTH_SHORT).show();
                                File f = new File(currentPath);
                                if(f.delete()){
                                    Toast.makeText(MainActivity.this, "delete successfully", Toast.LENGTH_SHORT).show();

                                }else{
                                    Toast.makeText(MainActivity.this, "fail to delete", Toast.LENGTH_SHORT).show();
                                }
                                dialog.dismiss();
                                playView.stopPlayback();
                                playView.setOnCompletionListener(null);
                                playView.setOnPreparedListener(null);
                                setContentView(R.layout.activity_main);
                                moveTaskToBack(true);
                                addBackFloatWindow();
                                isStarted=false;
                            }
                        });
                        //设置反面按钮
                        builder.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //Toast.makeText(MainActivity.this, "你点击了取消", Toast.LENGTH_SHORT).show();

                                dialog.dismiss();
                                playView.stopPlayback();
                                playView.setOnCompletionListener(null);
                                playView.setOnPreparedListener(null);
                                setContentView(R.layout.activity_main);
                                moveTaskToBack(true);
                                addBackFloatWindow();
                                isStarted=false;
                            }
                        });
                        //设置中立按钮
                        builder.setNeutralButton("Replay", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                dialog.dismiss();
                                Toast.makeText(MainActivity.this, "Start replay", Toast.LENGTH_SHORT).show();
                                playView.start();
                            }
                        });


                        AlertDialog dialog = builder.create();      //创建AlertDialog对象
                        //对话框显示的监听事件
                        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                            @Override
                            public void onShow(DialogInterface dialog) {
                                Log.e(TAG, "对话框显示了");
                            }
                        });
                        //对话框消失的监听事件
                        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                Log.e(TAG, "对话框消失了");
                            }
                        });
                        dialog.show();


                    }
                });


            }
            else if(intentAction.equals("quit.notice")){
                isStarted=false;
            }
        }
    }

    public void addBackFloatWindow(){
        if(!floatButtonIsShowing){
            windowManager.addView(floatView,layoutParams);
            floatButtonIsShowing= true;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        windowManager.removeView(floatView);
        floatButtonIsShowing =false;
        unregisterReceiver(noticeReceiver);
        //if(videoView!=null){
        //    videoView.suspend();
        //}
        System.exit(0);
    }

    public void onClickHome(View view) {
        setContentView(R.layout.activity_main);
    }
    public void onClickView(View view) {
        images=new ArrayList<Bitmap>() ;
        map = new HashMap<ImageView,String>();
        paths = new ArrayList<String>();
        setContentView(R.layout.view_video);
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)+ "/";
        Log.e("Path",path+"");
        List<String>  files = getFilesAllName(path);
        int index = 0;
        for (String fileName:files) {
            Log.e("Path",fileName+"");
            getFirstframe(fileName,index);
            index++;
        }
        GridView gridView = (GridView) findViewById(R.id.gridView);
        gridView.setAdapter(new ImageAdapter(this));
    }
    private void getFirstframe(String path,int index) {
/*
        imageView = new ImageView(this);
        LinearLayout     view2 = findViewById(R.id.container);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)view2.getLayoutParams();
        params.setMargins(50,50,50,50);
        view2.setLayoutParams(params);
*/


        MediaMetadataRetriever mmr = new MediaMetadataRetriever();//实例化MediaMetadataRetriever对象

        File file = new File(path);//实例化File对象，文件路径为/storage/emulated/0/shipin.mp4 （手机根目录）
        if (!file.exists()) {
            Toast.makeText(MainActivity.this, "file does not exist", Toast.LENGTH_SHORT).show();
        }
        mmr.setDataSource(path);
        Bitmap bitmap = mmr.getFrameAtTime(2000000 * 10, MediaMetadataRetriever.OPTION_CLOSEST);  //0表示首帧图片
        mmr.release(); //释放MediaMetadataRetriever对象
        if (bitmap != null) {
            //imageView.setImageBitmap(bitmap);//设置ImageView显示的图片
            //view2.addView(imageView);
            images.add(bitmap);
            paths.add(path);
        } else {
            Toast.makeText(MainActivity.this, "fail to load bitmap", Toast.LENGTH_SHORT).show();
        }
    }


    public List<String> getFilesAllName(String path) {
        File file=new File(path);
        File[] files=file.listFiles();
        if (files == null){Log.e("error","empty path");return null;}
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
        if(name.startsWith("."))return false;
        if (name.endsWith( ".mp4" )) {
            return true;
        }
        return false;
    }

    public WindowManager.LayoutParams getLayoutParams(){
        return this.layoutParams;
    }

    public void goDestroy(){
        this.onDestroy();
    }


    public void goTaskTop(){
        ActivityManager activityManager =(ActivityManager) getSystemService(ACTIVITY_SERVICE);
        activityManager.moveTaskToFront(this.getTaskId(),0);
        windowManager.removeView(floatView);
        floatButtonIsShowing=false;
    }


    private class ImageAdapter extends BaseAdapter {

        private Context mContext;

        public ImageAdapter(Context context) {
            this.mContext = context;
        }


        @Override
        public int getCount() {
            return images.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                imageView = new ImageView(mContext);

                imageView.setLayoutParams(new GridView.LayoutParams(141*density/160, 259*density/160));//设置ImageView宽高
                imageView.setOnClickListener(
                        new View.OnClickListener() {
                            public void onClick(View v) {
                                //Log.i(TAG, map.get(v));
                                currentPath = map.get(v);
                                setContentView(R.layout.playview2);
                                playView2 =(VideoView)findViewById(R.id.review_view);
                                playView2.setVideoPath(currentPath);
                                playView2.start();
                                playView2.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
                                {
                                    @Override
                                    public void onCompletion(MediaPlayer mp)
                                    {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                        //builder.setTitle("title：");
                                        builder.setMessage("Do you want to replay this video?");
                                        builder.setIcon(R.mipmap.ic_launcher);
                                        builder.setCancelable(false);


                                        builder.setPositiveButton("No", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                                playView2.stopPlayback();
                                                playView2.setOnCompletionListener(null);
                                                playView2.setOnPreparedListener(null);
                                                setContentView(R.layout.view_video);
                                                }
                                        });

                                        builder.setNeutralButton("Yes", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                                Toast.makeText(MainActivity.this, "Start replay", Toast.LENGTH_SHORT).show();
                                                playView2.start();
                                            }
                                        });

                                        AlertDialog dialog = builder.create();
                                        dialog.show();
                                    }
                                });
                            }
                        }
                );
            } else {
                imageView = (ImageView) convertView;
            }

            imageView.setImageBitmap(images.get(position));
            map.put(imageView,paths.get(position));
            return imageView;
        }


        class ViewHolder {
            ImageView itemImg;
        }
    }

}