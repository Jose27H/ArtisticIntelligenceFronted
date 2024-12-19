package com.example.artisticintelligence;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class ViewImagesActivity extends AppCompatActivity {

    // UI components
    private RecyclerView recyclerView;
    private ProgressBar progressBar;

    // List to store images fetched from the server
    private List<Bitmap> imagesList = new ArrayList<>();

    // RecyclerView adapter
    private RecyclerView.Adapter<RecyclerView.ViewHolder> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_images); // Set the layout for the activity

        // Initialize views
        recyclerView = findViewById(R.id.images_recycler_view);
        progressBar = findViewById(R.id.loading_progress);

        // Setup RecyclerView with a grid layout
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // 2 columns
        setupRecyclerView(); // Initialize and bind the adapter

        fetchAllImages(); // Fetch images from the server
    }

    private void setupRecyclerView() {
        adapter = new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                // Create an ImageView for each item in the grid
                ImageView imageView = new ImageView(parent.getContext());
                imageView.setLayoutParams(new RecyclerView.LayoutParams(
                        RecyclerView.LayoutParams.MATCH_PARENT,
                        400 // Fixed height for the images
                ));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                return new RecyclerView.ViewHolder(imageView) {};
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
                // Bind the image bitmap to the ImageView
                Bitmap bitmap = imagesList.get(position);
                ImageView imageView = (ImageView) holder.itemView;
                imageView.setImageBitmap(bitmap);

                // Add click listener to show image in a dialog
                imageView.setOnClickListener(v -> showGeneratedImage(bitmap));
            }

            @Override
            public int getItemCount() {
                return imagesList.size(); // Return the number of images
            }
        };

        recyclerView.setAdapter(adapter); // Set the adapter to the RecyclerView
    }

    private void fetchAllImages() {
        try {
            progressBar.setVisibility(View.VISIBLE); // Show the progress bar while fetching data

            // Prepare the payload for the request
            JSONObject jsonPayload = new JSONObject();
            boolean flag = getIntent().getBooleanExtra("FLAG", false);
            if (flag) jsonPayload.put("user_id", getIntent().getStringExtra("USER_ID"));

            // Use NetworkSender to send the request
            NetworkSender networkSender = new NetworkSender();
            networkSender.sendHttpRequest("/viewallimages", jsonPayload.toString(), null, new NetworkSender.ResponseCallback() {
                @Override
                public void onSuccess(String response) {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE); // Hide the progress bar
                        displayAllImages(response); // Parse and display the images
                    });
                }

                @Override
                public void onError(String errorMessage) {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE); // Hide the progress bar
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
            // Parse and add images to the list
            List<Bitmap> images = parseImages(jsonResponse);
            imagesList.clear();
            imagesList.addAll(images);
            adapter.notifyDataSetChanged(); // Notify the adapter of data changes
        } catch (Exception e) {
            Toast.makeText(this, "Error displaying images: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private List<Bitmap> parseImages(String jsonResponse) {
        List<Bitmap> images = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(jsonResponse); // Parse the JSON array
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject imageObject = jsonArray.getJSONObject(i);
                String base64Image = imageObject.getString("image");
                byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT); // Decode the Base64 string
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                images.add(bitmap); // Add the bitmap to the list
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return images; // Return the list of parsed images
    }

    private void showGeneratedImage(Bitmap bitmap) {
        try {
            // Display the selected image in a dialog
            ImageView imageView = new ImageView(this);
            imageView.setImageBitmap(bitmap);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Generated Image");
            builder.setView(imageView);
            builder.setPositiveButton("Save", (dialog, which) -> saveImageToGallery(bitmap)); // Save image button
            builder.setNegativeButton("Close", (dialog, which) -> dialog.dismiss()); // Close button
            builder.show();
        } catch (Exception e) {
            Toast.makeText(this, "Error displaying image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveImageToGallery(Bitmap bitmap) {
        try {
            // Save the bitmap to the device's gallery
            String fileName = "AI_Generated_" + System.currentTimeMillis() + ".jpg";
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/ArtisticIntelligence");
                values.put(MediaStore.Images.Media.IS_PENDING, 1);
            }

            ContentResolver resolver = getContentResolver();
            Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            if (imageUri != null) {
                try (OutputStream outputStream = resolver.openOutputStream(imageUri)) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream); // Compress and save the bitmap
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                        values.clear();
                        values.put(MediaStore.Images.Media.IS_PENDING, 0);
                        resolver.update(imageUri, values, null, null); // Update the media store
                    }
                    Toast.makeText(this, "Image saved successfully!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Error: Could not save image", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error saving image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
