package com.example.ecommerce.util;

import android.content.Context;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.module.AppGlideModule;
import com.bumptech.glide.request.RequestOptions;
import com.example.ecommerce.R;

/**
 * Custom Glide module for configuring Glide with better default settings
 */
@GlideModule
public final class GlideAppModule extends AppGlideModule {

    @Override
    public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder) {
        // Apply better image quality
        builder.setDefaultRequestOptions(new RequestOptions()
                .format(DecodeFormat.PREFER_RGB_565)
                .disallowHardwareConfig()
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
        );
    }

    /**
     * Utility class for improved image loading
     */
    public static class ImageLoader {
        /**
         * Load an image into an ImageView with rounded corners
         *
         * @param context      Application context
         * @param url          Image URL
         * @param imageView    Target ImageView
         * @param cornerRadius Corner radius in pixels
         */
        public static void loadRoundedImage(Context context, String url, ImageView imageView, int cornerRadius) {
            Glide.with(context)
                    .load(url)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .transform(new CenterCrop(), new RoundedCorners(cornerRadius))
                    .into(imageView);
        }

        /**
         * Load a drawable resource into an ImageView with rounded corners
         *
         * @param context      Application context
         * @param resourceId   Drawable resource ID
         * @param imageView    Target ImageView
         * @param cornerRadius Corner radius in pixels
         */
        public static void loadRoundedDrawable(Context context, @DrawableRes int resourceId,
                                               ImageView imageView, int cornerRadius) {
            Glide.with(context)
                    .load(resourceId)
                    .transform(new CenterCrop(), new RoundedCorners(cornerRadius))
                    .into(imageView);
        }

        /**
         * Preload images for smoother scrolling experience
         *
         * @param context Application context
         * @param urls    List of image URLs to preload
         */
        public static void preloadImages(Context context, String[] urls) {
            for (String url : urls) {
                Glide.with(context)
                        .load(url)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .preload();
            }
        }
    }
}