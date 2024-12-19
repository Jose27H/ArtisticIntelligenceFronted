package com.example.artisticintelligence;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class ViewImagesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private List<Bitmap> imagesList = new ArrayList<>();
    private RecyclerView.Adapter<RecyclerView.ViewHolder> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_images);

        // Initialize views
        recyclerView = findViewById(R.id.images_recycler_view);
        progressBar = findViewById(R.id.loading_progress);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // 2 columns
        setupRecyclerView();

        // Fetch images from the server
        fetchAllImages();
    }

    private void setupRecyclerView() {
        adapter = new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                ImageView imageView = new ImageView(parent.getContext());
                imageView.setLayoutParams(new RecyclerView.LayoutParams(
                        RecyclerView.LayoutParams.MATCH_PARENT,
                        400 // Fixed height
                ));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                return new RecyclerView.ViewHolder(imageView) {};
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
                Bitmap bitmap = imagesList.get(position);
                ImageView imageView = (ImageView) holder.itemView;
                imageView.setImageBitmap(bitmap);

                // Add click listener to show image in a dialog
                imageView.setOnClickListener(v -> showGeneratedImage(bitmap));
            }

            @Override
            public int getItemCount() {
                return imagesList.size();
            }
        };

        recyclerView.setAdapter(adapter);
    }

    private void fetchAllImages() {
        try {
            progressBar.setVisibility(View.VISIBLE);
            JSONObject jsonPayload = new JSONObject();
            boolean flag = getIntent().getBooleanExtra("FLAG", false);
            if(flag) jsonPayload.put("user_id", getIntent().getStringExtra("USER_ID"));

            NetworkSender networkSender = new NetworkSender();
            networkSender.sendHttpRequest("/viewallimages", jsonPayload.toString(), null, new NetworkSender.ResponseCallback() {
                @Override
                public void onSuccess(String response) {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        displayAllImages(response);
                    });
                }

                @Override
                public void onError(String errorMessage) {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(ViewImagesActivity.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                    });
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Error creating request payload: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void displayAllImages(String jsonResponse) {
        try {
            List<Bitmap> images = parseImages(jsonResponse);
            imagesList.clear();
            imagesList.addAll(images);
            adapter.notifyDataSetChanged();
        } catch (Exception e) {
            Toast.makeText(this, "Error displaying images: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private List<Bitmap> parseImages(String jsonResponse) {
        List<Bitmap> images = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(jsonResponse);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject imageObject = jsonArray.getJSONObject(i);
                String base64Image = imageObject.getString("image");
                byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                images.add(bitmap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return images;
    }

    private void showGeneratedImage(Bitmap bitmap) {
        try {
            ImageView imageView = new ImageView(this);
            imageView.setImageBitmap(bitmap);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Generated Image");
            builder.setView(imageView);
            builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());
            builder.show();
        } catch (Exception e) {
            Toast.makeText(this, "Error displaying image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
