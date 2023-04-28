package com.trianguloy.urlchecker.views;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageButton;

import com.trianguloy.urlchecker.utilities.Enums;

import java.util.List;

public class CycleImageButton<T extends Enums.ImageEnum> extends ImageButton {

    private List<T> states;
    private int currentState;

    public CycleImageButton(Context context) {
        super(context);
    }

    public CycleImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CycleImageButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setStates(List<T> states) {
        this.states = states;
        updateImageResource(0);
    }

    public void setCurrentState(T currentState) {
        updateImageResource(states.indexOf(currentState));
    }

    public T getCurrentState() {
        return states == null || states.isEmpty() ? null : states.get(currentState);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    public boolean performClick() {
        updateImageResource(currentState + 1);
        return super.performClick();
    }

    private void updateImageResource(int newState) {
        if (states == null || states.isEmpty()) {
            setImageDrawable(null);
        } else {
            currentState = newState >= 0 ? newState % states.size() : 0;
            setImageResource(states.get(currentState).getImageResource());
        }
    }
}
