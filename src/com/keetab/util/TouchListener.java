package com.keetab.util;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;

public abstract class TouchListener implements OnTouchListener {
    private static final int SWIPE_MIN_DISTANCE = 150;
    private static final int SWIPE_THRESHOLD_VELOCITY = 1000;
    
    private final GestureDetector gestureDetector;
    		
    public TouchListener(Context ctx) {
    	gestureDetector= new GestureDetector(ctx, new GestureListener(ctx));
    }

    public boolean onTouch(final View view, final MotionEvent motionEvent) {
        return gestureDetector.onTouchEvent(motionEvent);
    }

    private final class GestureListener extends SimpleOnGestureListener {
    	
    	int width;
    	
    	public GestureListener(Context ctx) {
        	DisplayMetrics metrics = ctx.getResources().getDisplayMetrics();
        	width = metrics.widthPixels;
    	}
    	
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, 
        		float velocityX, float velocityY) {
        	float dX = e1.getX() - e2.getX();
        	float dY = e1.getY() - e2.getY();
        	float vX = Math.abs(velocityX);
        	float vY = Math.abs(velocityY);
        
        	if (Math.abs(dX) > Math.abs(dY) && vX > SWIPE_THRESHOLD_VELOCITY) {
        		if (dX > SWIPE_MIN_DISTANCE) {
        			onSwipeRight();
        		} else if (-dX > SWIPE_MIN_DISTANCE) {
        			onSwipeLeft();
        		}
        	} else if (vY > SWIPE_THRESHOLD_VELOCITY) {
        		if (dY > SWIPE_MIN_DISTANCE) {
        			onSwipeUp();
        		} else if (-dY > SWIPE_MIN_DISTANCE) {
        			onSwipeDown();
        		}
        	}
            return true;
        }
        
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
        	
        	if (e.getX() < 40 && e.getY() > 60) {
        		leftTap();
        	}
        	
        	if (e.getX() > (width-40) && e.getY() > 60) {
        		rightTap();
        	}

        	
        	return super.onSingleTapUp(e);
        }
    }

    abstract public void onSwipeLeft();
    abstract public void onSwipeRight();
    abstract public void onSwipeDown();
    abstract public void onSwipeUp();
    
    abstract public void leftTap();
    abstract public void rightTap();

}