package com.trianguloy.urlchecker.utilities.wrappers;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.ViewFlipper;

/**
 * The ViewFlipper class runs the same animations when showing next and prev,
 * and that makes the view always scroll to the same 'side'.
 * This custom view wraps it to 'fix' the direction
 */
public class FixedViewFlipper {

    private final ViewFlipper viewFlipper;

    private final Animation OutToRight;
    private final Animation OutToLeft;
    private final Animation InFromRight;
    private final Animation InFromLeft;

    public FixedViewFlipper(ViewFlipper viewFlipper) {
        this.viewFlipper = viewFlipper;

        // generate animations
        Interpolator reverse = a -> 1 - a;
        OutToRight = AnimationUtils.loadAnimation(viewFlipper.getContext(), android.R.anim.slide_out_right);
        OutToLeft = AnimationUtils.loadAnimation(viewFlipper.getContext(), android.R.anim.slide_in_left);
        OutToLeft.setInterpolator(reverse);
        InFromRight = AnimationUtils.loadAnimation(viewFlipper.getContext(), android.R.anim.slide_out_right);
        InFromRight.setInterpolator(reverse);
        InFromLeft = AnimationUtils.loadAnimation(viewFlipper.getContext(), android.R.anim.slide_in_left);
    }

    public void showPrevious() {
        viewFlipper.setInAnimation(InFromLeft);
        viewFlipper.setOutAnimation(OutToRight);
        viewFlipper.showPrevious();
    }

    public void showNext() {
        viewFlipper.setInAnimation(InFromRight);
        viewFlipper.setOutAnimation(OutToLeft);
        viewFlipper.showNext();
    }

    public int getDisplayedChild() {
        return viewFlipper.getDisplayedChild();
    }

    public int getChildCount() {
        return viewFlipper.getChildCount();
    }
}
