package com.example.ecommerce.util;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.example.ecommerce.R;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;

/**
 * A custom toolbar with built-in animations.
 * This toolbar can animate its entrance and can be used with a CoordinatorLayout
 * for scroll animation behaviors.
 */
public class AnimatedToolbar extends MaterialToolbar {

    private boolean hasAnimatedEntrance = false;

    public AnimatedToolbar(@NonNull Context context) {
        super(context);
        init();
    }

    public AnimatedToolbar(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AnimatedToolbar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // Set elevation for material design shadow
        setElevation(getResources().getDimensionPixelSize(R.dimen.toolbar_elevation));
    }

    /**
     * Animate the toolbar entrance from the top
     */
    public void animateEntrance() {
        if (hasAnimatedEntrance) return;

        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.slide_in_top);
        startAnimation(animation);
        hasAnimatedEntrance = true;
    }

    /**
     * Configure the toolbar to hide on scroll when used with a CoordinatorLayout
     */
    public void enableScrollBehavior() {
        if (getLayoutParams() instanceof AppBarLayout.LayoutParams) {
            AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) getLayoutParams();
            params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
                    | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
                    | AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP);
            setLayoutParams(params);
        } else if (getLayoutParams() instanceof CoordinatorLayout.LayoutParams) {
            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) getLayoutParams();
            params.setBehavior(new AppBarLayout.ScrollingViewBehavior());
            setLayoutParams(params);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        // Auto-animate entrance when attached to window
        post(this::animateEntrance);
    }
}