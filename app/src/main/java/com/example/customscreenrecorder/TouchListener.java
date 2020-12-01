package com.example.customscreenrecorder;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

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



    public TouchListener(View LinearLayout,WindowManager windowManager, WindowManager.LayoutParams lp) {

        this.windowManager  = windowManager;
        FloatingActionButton fab =(FloatingActionButton)((android.widget.LinearLayout)LinearLayout).getChildAt(0);
        this.fab = fab;
        this.ll = LinearLayout;
        this.lp =lp;
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
                slideButton(l,t,r,b);
                windowManager.updateViewLayout(ll, lp);
                break;
            case MotionEvent.ACTION_UP:
                int distance = Math.abs((int) motionEvent.getRawX() - originX) + Math.abs((int)motionEvent.getRawY() - originY);
                Log.e("DIstance",distance+"");
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
