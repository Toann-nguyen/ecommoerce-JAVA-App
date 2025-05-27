package com.example.ecommerce.util;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.core.graphics.ColorUtils;

import com.google.android.material.color.DynamicColors;
import com.google.android.material.color.MaterialColors;

/**
 * Utility class for applying Material 3 dynamic colors across all Android versions
 */
public class DynamicColorUtils {
    private static final String TAG = "DynamicColorUtils";

    /**
     * Apply Material 3 dynamic colors to the application if available
     *
     * @param context Application context
     */
    public static void applyDynamicColors(Context context) {
        try {
            // Apply dynamic colors if available (Android 12+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Use system theme for Android 12+ devices
                DynamicColors.applyToActivitiesIfAvailable(
                        (android.app.Application) context.getApplicationContext()
                );
                Log.d(TAG, "Applied system dynamic colors (Android 12+)");
            } else {
                // Use Material You backport for older devices
                Log.d(TAG, "Using defined Material 3 colors for older devices");
                // Older devices will use the predefined Material 3 colors from values/colors.xml
            }
        } catch (Exception e) {
            Log.e(TAG, "Error applying Material 3 theme: " + e.getMessage());
        }
    }

    /**
     * Get the primary color from the current theme
     *
     * @param context Application context
     * @return Primary color integer
     */
    @ColorInt
    public static int getPrimaryColor(Context context) {
        return MaterialColors.getColor(context, com.google.android.material.R.attr.colorPrimary,
                context.getResources().getColor(android.R.color.holo_blue_dark));
    }

    /**
     * Get the secondary color from the current theme
     *
     * @param context Application context
     * @return Secondary color integer
     */
    @ColorInt
    public static int getSecondaryColor(Context context) {
        return MaterialColors.getColor(context, com.google.android.material.R.attr.colorSecondary,
                context.getResources().getColor(android.R.color.holo_orange_light));
    }

    /**
     * Apply a colored tint to an ImageView
     *
     * @param imageView The ImageView to tint
     * @param color     The color to apply
     */
    public static void tintImageView(ImageView imageView, @ColorInt int color) {
        imageView.setImageTintList(ColorStateList.valueOf(color));
    }

    /**
     * Create a ripple background for views
     *
     * @param baseColor Base color for ripple effect
     * @return ColorStateList for ripple effect
     */
    public static ColorStateList createRippleColorStateList(@ColorInt int baseColor) {
        int[][] states = new int[][]{
                new int[]{android.R.attr.state_pressed},
                new int[]{}
        };

        int[] colors = new int[]{
                ColorUtils.setAlphaComponent(baseColor, 128), // 50% alpha when pressed
                ColorUtils.setAlphaComponent(baseColor, 64)   // 25% alpha in normal state
        };

        return new ColorStateList(states, colors);
    }

    /**
     * Apply ripple effect to a view
     *
     * @param view    The view to apply ripple to
     * @param context Application context
     */
    public static void applyRippleEffect(View view, Context context) {
        view.setBackgroundTintList(createRippleColorStateList(getPrimaryColor(context)));
    }
}