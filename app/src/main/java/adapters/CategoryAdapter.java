package adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ecommerce.R;
import models.Category;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private Context context;
    private List<Category> categories;
    private CategoryClickListener listener;

    public interface CategoryClickListener {
        void onCategoryClick(Category category);
    }

    public CategoryAdapter(Context context, List<Category> categories, CategoryClickListener listener) {
        this.context = context;
        this.categories = categories;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);

        // Thiết lập tên danh mục
        holder.categoryName.setText(category.getName());

        // Tải biểu tượng danh mục bằng Glide
        if (category.getIconUrl() != null && !category.getIconUrl().isEmpty()) {
            Glide.with(context)
                    .load(category.getIconUrl())
                    .placeholder(R.color.gray_placeholder) // Sử dụng màu placeholder đã định nghĩa
                    .into(holder.categoryIcon);
        } else {
            // Sử dụng màu gray_placeholder làm hình ảnh mặc định
            holder.categoryIcon.setImageResource(R.color.gray_placeholder);
        }

        // Thiết lập sự kiện nhấn vào item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCategoryClick(category);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categories != null ? categories.size() : 0;
    }

    // Cập nhật dữ liệu mới
    public void updateData(List<Category> newCategories) {
        this.categories = newCategories;
        notifyDataSetChanged();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        ImageView categoryIcon;
        TextView categoryName;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryIcon = itemView.findViewById(R.id.imgCategory);
            categoryName = itemView.findViewById(R.id.tvCategoryName);
        }
    }
}