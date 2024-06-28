package com.example.just_plant.Adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.just_plant.R;
import com.example.just_plant.model.Category;
import com.squareup.picasso.Picasso;

import java.util.List;
public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {
    private List<Category> categories;
    private OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }

    public CategoryAdapter(List<Category> categories, OnCategoryClickListener listener) {
        this.categories = categories;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.bind(category, listener);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView;
        public ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.category_name);
            imageView = itemView.findViewById(R.id.category_image);
        }

        public void bind(final Category category, final OnCategoryClickListener listener) {
            nameTextView.setText(category.getName());
            Picasso.get().load(category.getImage()).into(imageView);

            itemView.setOnClickListener(v -> {
                Log.d("CategoryAdapter", "Category ID: " + category.getId());
                listener.onCategoryClick(category);
            });
        }
    }

}
