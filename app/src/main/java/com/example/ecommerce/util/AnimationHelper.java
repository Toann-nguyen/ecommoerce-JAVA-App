package com.example.ecommerce.util;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.ecommerce.R;

public class AnimationHelper {

    private static Dialog loadingDialog;
    private static Dialog addToCartDialog;

    /**
     * Shows a loading animation dialog
     *
     * @param context The context to show the dialog in
     * @param message Optional message to display with the animation
     * @return The dialog that is displayed
     */
    public static Dialog showLoading(Context context, String message) {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }

        loadingDialog = new Dialog(context);
        loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        loadingDialog.setContentView(R.layout.dialog_lottie_animation);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        LottieAnimationView animationView = loadingDialog.findViewById(R.id.lottieAnimationView);
        animationView.setAnimation(R.raw.loading_animation);

        TextView textMessage = loadingDialog.findViewById(R.id.textMessage);
        if (message != null && !message.isEmpty()) {
            textMessage.setText(message);
            textMessage.setVisibility(View.VISIBLE);
        } else {
            textMessage.setVisibility(View.GONE);
        }

        loadingDialog.show();
        return loadingDialog;
    }

    /**
     * Hides the loading animation dialog if it's showing
     */
    public static void hideLoading() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    /**
     * Shows an add to cart animation dialog
     *
     * @param context        The context to show the dialog in
     * @param onAnimationEnd Callback to run when animation completes
     */
    public static void showAddToCartAnimation(Context context, Runnable onAnimationEnd) {
        if (addToCartDialog != null && addToCartDialog.isShowing()) {
            addToCartDialog.dismiss();
        }

        addToCartDialog = new Dialog(context);
        addToCartDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        addToCartDialog.setContentView(R.layout.dialog_lottie_animation);
        addToCartDialog.setCancelable(false);
        addToCartDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        LottieAnimationView animationView = addToCartDialog.findViewById(R.id.lottieAnimationView);
        animationView.setAnimation(R.raw.add_to_cart);
        animationView.setRepeatCount(0); // Play only once

        TextView textMessage = addToCartDialog.findViewById(R.id.textMessage);
        textMessage.setText("Thêm vào giỏ hàng thành công");
        textMessage.setVisibility(View.VISIBLE);

        animationView.addAnimatorListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                super.onAnimationEnd(animation);
                if (addToCartDialog != null && addToCartDialog.isShowing()) {
                    addToCartDialog.dismiss();
                }
                if (onAnimationEnd != null) {
                    onAnimationEnd.run();
                }
            }
        });

        addToCartDialog.show();
    }

    /**
     * Apply a fall down animation to a RecyclerView
     *
     * @param recyclerView The RecyclerView to animate
     */
    public static void applyRecyclerAnimation(RecyclerView recyclerView) {
        recyclerView.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(
                recyclerView.getContext(), R.anim.layout_animation_fall_down));
        recyclerView.scheduleLayoutAnimation();
    }

    /**
     * Apply an entrance animation to a view
     *
     * @param view      The view to animate
     * @param animResId The animation resource ID
     */
    public static void animateView(View view, int animResId) {
        Animation animation = AnimationUtils.loadAnimation(view.getContext(), animResId);
        view.startAnimation(animation);
    }

    /**
     * Apply fade in animation to a view
     *
     * @param view The view to animate
     */
    public static void fadeIn(View view) {
        animateView(view, R.anim.fade_in);
    }

    /**
     * Apply fade out animation to a view
     *
     * @param view The view to animate
     */
    public static void fadeOut(View view) {
        animateView(view, R.anim.fade_out);
    }

    /**
     * Configure activity transitions using the custom animations
     *
     * @param activity The activity to configure
     */
    public static void configureTransitions(Activity activity) {
        activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
}