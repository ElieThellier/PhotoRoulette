package com.example.photoroulette;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class MainAdp extends RecyclerView.Adapter<MainAdp.ViewHolder> {
    // Initialize variable
    private ArrayList<Uri> arrayList;

    // Create constructor
    public MainAdp(ArrayList<Uri> arrayList) {
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Initialize view
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_main, parent, false);
        // Return ViewHolder
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Set image using uri
        holder.ivImage.setImageURI(arrayList.get(position));
    }

    @Override
    public int getItemCount() {
        // Return list size
        return arrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // Initialize variable
        ImageView ivImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Assign variable
            ivImage = itemView.findViewById(R.id.iv_image);
        }
    }
}
