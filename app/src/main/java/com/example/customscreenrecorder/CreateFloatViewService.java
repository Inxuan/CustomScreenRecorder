package com.example.customscreenrecorder;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;

public class CreateFloatViewService extends Service {
    private WindowManager windowManager;
    private final WindowManager.LayoutParams layoutParams=new WindowManager.LayoutParams();
    private View floatView;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        this. createWindow();
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        windowManager.removeView(floatView);
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
        layoutParams.width=WindowManager.LayoutParams.WRAP_CONTENT;

        layoutParams.height=WindowManager.LayoutParams.WRAP_CONTENT;





        //floatView = LayoutInflater.from(this).inflate(R.layout.window,null);
        //加载显示悬浮窗

//        layoutParams.width*=0.9;
        //       layoutParams.height*=0.9;

        //TouchListener tl = new TouchListener(R.layout.window,windowManager,layoutParams);
        //windowManager.addView(floatView,layoutParams);
    }


}
