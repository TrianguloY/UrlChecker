package com.trianguloy.forceurl.utilities;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import com.trianguloy.forceurl.utilities.methods.AndroidUtils;
import com.trianguloy.forceurl.R;
import com.trianguloy.forceurl.utilities.methods.JavaUtils;

public class Bubble {
    private View floatView;
    private final WindowManager windowManager;
    private final LayoutParams layoutParams;
    private final JavaUtils.Consumer<Context> popAction;

    public Bubble(Context context, JavaUtils.Consumer<Context> popAction) {
        // The bubble is independent from any activity
        context = context.getApplicationContext();
        this.popAction = popAction;

        floatView = LayoutInflater.from(context).inflate(R.layout.bubble, null);
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        floatView.findViewById(R.id.bubbleImage).setOnTouchListener(onTouchListener);
        layoutParams = new LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT,
                0,
                0,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                        LayoutParams.TYPE_APPLICATION_OVERLAY :
                        LayoutParams.TYPE_TOAST, // TYPE_PHONE?
                LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        AndroidUtils.runOnUiThread(() -> {
            windowManager.addView(floatView, layoutParams);
        });
    }
    private int lastX = 0;
    private int lastY = 0;
    private int firstX = 0;
    private int firstY = 0;
    private boolean touchConsumedByMove = false;
    private boolean isMoving = false;
    private final int THRESHOLD = 5;

    // Adapted from https://stackoverflow.com/a/53092436
    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            var totalDeltaX = lastX - firstX;
            var totalDeltaY = lastY - firstY;

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    lastX = Math.round(event.getRawX());
                    lastY = Math.round(event.getRawY());
                    firstX = lastX;
                    firstY = lastY;
                    isMoving = false;
                    return true;
                case MotionEvent.ACTION_MOVE:
                    var deltaX = Math.round(event.getRawX()) - lastX;
                    var deltaY = Math.round(event.getRawY()) - lastY;
                    lastX = Math.round(event.getRawX());
                    lastY = Math.round(event.getRawY());
                    if (Math.abs(totalDeltaX) >= THRESHOLD || Math.abs(totalDeltaY) >= THRESHOLD) {
                        if (event.getPointerCount() == 1) {
                            layoutParams.x += deltaX;
                            layoutParams.y += deltaY;
                            touchConsumedByMove = true;
                            windowManager.updateViewLayout(floatView, layoutParams);
                        } else {
                            touchConsumedByMove = false;
                        }
                        isMoving = true;
                    } else {
                        touchConsumedByMove = false;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    view.performClick();
                    if (isMoving) {
                        // Store coordinates
                    } else {
                        pop();
                    }
                    isMoving = false;
                    return false;
                default:
                    break;
            }
            return touchConsumedByMove;
        }
    };

    public void pop() {
        popAction.accept(floatView.getContext());
        windowManager.removeView(floatView);
    }
}
