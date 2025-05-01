package adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ecommerce.R;

import java.util.List;

import models.Banner;

public class SliderAdapter extends RecyclerView.Adapter<SliderAdapter.SliderViewHolder> {

    private Context context;
    private List<Banner> banners;

    public SliderAdapter(Context context, List<Banner> banners) {
        this.context = context;
        this.banners = banners;
    }

    @NonNull
    @Override
    public SliderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_slider, parent, false);
        return new SliderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SliderViewHolder holder, int position) {
        Banner banner = banners.get(position);

        // Load hình ảnh banner bằng Glide
        if (banner.getImageUrl() != null && !banner.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(banner.getImageUrl())
                    .placeholder(R.color.gray_placeholder) // Sử dụng màu placeholder
                    .centerCrop()
                    .into(holder.imgSlider);
        } else {
            holder.imgSlider.setImageResource(R.color.gray_placeholder);
        }

        // Nếu có action khi click vào banner
        holder.itemView.setOnClickListener(v -> {
            // Xử lý khi click vào banner
            // Có thể mở một sản phẩm hoặc danh mục tương ứng
        });
    }

    @Override
    public int getItemCount() {
        return banners != null ? banners.size() : 0;
    }

    static class SliderViewHolder extends RecyclerView.ViewHolder {
        ImageView imgSlider;

        public SliderViewHolder(@NonNull View itemView) {
            super(itemView);
            imgSlider = itemView.findViewById(R.id.imgSlider);
        }
    }
}