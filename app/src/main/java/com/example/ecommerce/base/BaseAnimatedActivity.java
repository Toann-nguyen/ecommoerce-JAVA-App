package com.example.ecommerce.base;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecommerce.R;
import com.example.ecommerce.util.AnimationHelper;

/**
 * Base activity class with built-in animations for all activities.
 * Extend this class for any activity that should have animations.
 */
public abstract class BaseAnimatedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Add activity transition animations
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

        // Request features before setting content view
        requestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS);

        // Enable enter/exit transitions
        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);

        // Animate all RecyclerViews in the layout
        animateRecyclerViews();
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);

        // Animate all RecyclerViews in the layout
        animateRecyclerViews();
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);

        // Animate all RecyclerViews in the layout
        animateRecyclerViews();
    }

    @Override
    public void finish() {
        super.finish();

        // Add exit transition animations
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    /**
     * Find and animate all RecyclerViews in the activity
     */
    private void animateRecyclerViews() {
        View rootView = findViewById(android.R.id.content);
        if (rootView instanceof ViewGroup) {
            findAndAnimateRecyclerViews((ViewGroup) rootView);
        }
    }

    /**
     * Recursively find and animate all RecyclerViews
     */
    private void findAndAnimateRecyclerViews(ViewGroup viewGroup) {
        int childCount = viewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = viewGroup.getChildAt(i);
            if (child instanceof RecyclerView) {
                // Apply fall down animation to RecyclerView items
                RecyclerView recyclerView = (RecyclerView) child;
                AnimationHelper.applyRecyclerAnimation(recyclerView);
            } else if (child instanceof ViewGroup) {
                // Search deeper in the view hierarchy
                findAndAnimateRecyclerViews((ViewGroup) child);
            }
        }
    }

    /**
     * Apply a fade-in animation to a view
     */
    protected void fadeInView(View view) {
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        view.startAnimation(fadeIn);
        view.setVisibility(View.VISIBLE);
    }

    /**
     * Apply a fade-out animation to a view
     */
    protected void fadeOutView(View view) {
        Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        view.startAnimation(fadeOut);
    }
}