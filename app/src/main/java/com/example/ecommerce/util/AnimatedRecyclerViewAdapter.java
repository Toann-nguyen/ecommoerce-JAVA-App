package com.example.ecommerce.util;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecommerce.R;

/**
 * Abstract base class for RecyclerView adapters with item animations.
 * Extend this class to add item animations to your RecyclerView adapter.
 *
 * @param <VH> The ViewHolder type
 */
public abstract class AnimatedRecyclerViewAdapter<VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH> {

    private int lastPosition = -1;
    private boolean animateItems = true;

    /**
     * Applies an animation to a view when it is bound to the RecyclerView.
     * Only animates items that are newly appearing on screen.
     *
     * @param holder   The ViewHolder to animate
     * @param position The position of the item in the adapter
     */
    protected void animateItem(RecyclerView.ViewHolder holder, int position) {
        if (!animateItems) return;

        // Only animate new items
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(
                    holder.itemView.getContext(), R.anim.item_animation_fall_down);
            holder.itemView.startAnimation(animation);
            lastPosition = position;
        }
    }

    /**
     * Override this method in your adapter and call animateItem() after binding views
     */
    @Override
    public abstract void onBindViewHolder(@NonNull VH holder, int position);

    @Override
    public void onViewDetachedFromWindow(@NonNull VH holder) {
        super.onViewDetachedFromWindow(holder);
        holder.itemView.clearAnimation();
    }

    /**
     * Resets the animation state when the adapter data changes
     */
    public void resetAnimationState() {
        lastPosition = -1;
    }

    /**
     * Enable or disable item animations
     *
     * @param animate true to enable animations, false to disable
     */
    public void setAnimateItems(boolean animate) {
        this.animateItems = animate;
    }
}