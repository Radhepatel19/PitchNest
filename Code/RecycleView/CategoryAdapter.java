package com.example.businessidea.RecycleView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.businessidea.Module.Category;
import com.example.businessidea.R;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private List<Category> categoryList;
    private int selectedPosition = -1; // To track selected category
    private OnCategorySelectedListener categorySelectedListener;
    private Context context;
    private FragmentManager fragmentManager;

    // Interface to send selected category back to HomeFragment
    public interface OnCategorySelectedListener {
        void onCategorySelected(String categoryName);
    }

    // Constructor
    public CategoryAdapter(List<Category> categoryList, Context context, FragmentManager fragmentManager, OnCategorySelectedListener listener) {
        this.categoryList = categoryList;
        this.context = context;
        this.fragmentManager = fragmentManager;
        this.categorySelectedListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Category category = categoryList.get(position);
        holder.categoryText.setText(category.getName());

        // Highlight selected category
        if (position == selectedPosition) {
            holder.categoryText.setBackgroundResource(R.drawable.selected_category_bg);
            holder.closeButton.setVisibility(View.VISIBLE);
            holder.categoryStyle.setElevation(8f);
            holder.itemView.animate().scaleX(1f).scaleY(1f).setDuration(200).start();
        } else {
            int nightModeFlags = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
                holder.categoryText.setBackgroundResource(R.drawable.default_category_bg);
            } else {
                holder.categoryText.setBackgroundResource(R.drawable.default_category_light);
            }

            holder.closeButton.setVisibility(View.GONE);
            holder.categoryStyle.setElevation(0f);
            holder.itemView.animate().scaleX(1f).scaleY(1f).setDuration(200).start();
        }

        // Handle category click
        holder.categoryText.setOnClickListener(v -> {
            if (selectedPosition == position) {
                // Deselect category if clicked again
                selectedPosition = -1;
                categorySelectedListener.onCategorySelected(null);
            } else {
                // Select new category
                selectedPosition = position;
                categorySelectedListener.onCategorySelected(category.getName());
            }
            notifyDataSetChanged();
        });

        // Handle close button click (reset filtering)
        holder.closeButton.setOnClickListener(v -> {
            selectedPosition = -1;
            categorySelectedListener.onCategorySelected(null);
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView categoryText;
        ImageView closeButton;
        LinearLayout categoryStyle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryText = itemView.findViewById(R.id.categoryText);
            closeButton = itemView.findViewById(R.id.closeButton);
            categoryStyle = itemView.findViewById(R.id.categoryStyle);
        }
    }
}
