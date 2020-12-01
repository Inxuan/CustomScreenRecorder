package com.example.customscreenrecorder;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import static android.content.Context.ACTIVITY_SERVICE;

public class TouchListener implements View.OnTouchListener {
    int lastX = 0;
    int lastY = 0;
    int originX =0;
    int originY =0;
    int paramX = 0;
    int paramY = 0;

    private View ll;
    private FloatingActionButton fab;
    private WindowManager.LayoutParams lp;
    private WindowManager windowManager;
    private int screenWidth;
    private int screenHeight;
    private MainActivity mainActivity;

    public TouchListener(View LinearLayout,WindowManager windowManager, WindowManager.LayoutParams lp,int screenWidth,int screenHeight, MainActivity mainActivity) {

        this.windowManager  = windowManager;
        FloatingActionButton fab =(FloatingActionButton)((android.widget.LinearLayout)LinearLayout).getChildAt(0);
        this.fab = fab;
        this.ll = LinearLayout;
        this.lp =lp;
        this.screenWidth = screenWidth;
        this.screenHeight=screenHeight;
        this.mainActivity = mainActivity;
        //LinearLayout.setOnTouchListener(this);
        fab.setOnTouchListener(this);
    }



    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastX = (int) motionEvent.getRawX();
                lastY = (int) motionEvent.getRawY();
                originX= lastX;
                originY= lastY;
                paramX = lp.x;
                paramY = lp.y;
                break;
            case MotionEvent.ACTION_MOVE:
                int dx = (int) motionEvent.getRawX() - lastX;
                int dy = (int) motionEvent.getRawY() - lastY;
                lp.x = paramX + dx;
                lp.y = paramY + dy;
                int l = view.getLeft() + dx;
                int b = view.getBottom() + dy;
                int r = view.getRight() + dx;
                int t = view.getTop() + dy;
                if (l < 0) {
                    l = 0;
                    r = l + view.getWidth();
                }
                if (t < 0) {
                    t = 0;
                    b = t + view.getHeight();
                }
                if (r > screenWidth) {
                    r = screenWidth;
                    l = r - view.getWidth();
                }
                if (b > screenHeight) {
                    b = screenHeight;
                    t = b - view.getHeight();
                }

                slideButton(l,t,r,b);
                windowManager.updateViewLayout(ll, lp);
                break;
            case MotionEvent.ACTION_UP:
                int distance = Math.abs((int) motionEvent.getRawX() - originX) + Math.abs((int)motionEvent.getRawY() - originY);
                //Log.e("DIstance",distance+"");
                //Log.e("l",lp.x+"");
                //Log.e("t",lp.y+"");
                if(lp.y<=-880){
                    mainActivity.goDestory();
                    //Log.e("aa","");
                }
                if(lp.y>=900){
                    mainActivity.goTaskTop();
                }
                if (Math.abs(distance)<5) {
                    fab.performClick();
                }else {
                    return true;
                }
                break;
        }
        return true;
    }

    private void slideButton(int l, int t, int r, int b){
        //ll.layout(l, t, r, b);
        fab.layout(l, t, r, b);
    }
}
