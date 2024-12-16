package com.example.artisticintelligence;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.material.card.MaterialCardView;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.target.Target;
import androidx.annotation.Nullable;

public class GenerationActivity extends AppCompatActivity {
    private static final String TAG = "GenerationActivity";
    private NetworkSender networkSender;
    private Spinner endpointSpinner;
    private LinearLayout parameterContainer;
    private Button generateButton;
    private Map<String, View> parameterViews;
    private String userToken;
    private ProgressBar progressBar;
    private ImageView generatedImageView;

    private ProgressBar imageLoadingProgress;
    private TextView errorText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generation);

        networkSender = new NetworkSender();
        parameterViews = new HashMap<>();

        // Get user token from GoogleSignIn
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            userToken = account.getIdToken();
        } else {
            // If no account, return to login
            finish();
            return;
        }

        initializeViews();
        setupEndpointSpinner();
        setupGenerateButton();
    }

    private void initializeViews() {
        endpointSpinner = findViewById(R.id.endpoint_spinner);
        parameterContainer = findViewById(R.id.parameter_container);
        generateButton = findViewById(R.id.generate_button);
        progressBar = findViewById(R.id.progress_bar);
        generatedImageView = findViewById(R.id.generated_image);

        // Initially hide progress and image view
        progressBar.setVisibility(View.GONE);
        generatedImageView.setVisibility(View.GONE);

        imageLoadingProgress = findViewById(R.id.image_loading_progress);
        errorText = findViewById(R.id.error_text);

        // Initially hide views
        imageLoadingProgress.setVisibility(View.GONE);
        errorText.setVisibility(View.GONE);
    }



    private void setupEndpointSpinner() {
        List<String> endpoints = new ArrayList<>();
        endpoints.add("txt2img");
        endpoints.add("img2img");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                endpoints
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        endpointSpinner.setAdapter(adapter);

        endpointSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedEndpoint = (String) parent.getItemAtPosition(position);
                createParameterViews(selectedEndpoint);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupGenerateButton() {
        generateButton.setOnClickListener(v -> generateImage());
    }

    private void createParameterViews(String endpoint) {
        parameterContainer.removeAllViews();
        parameterViews.clear();

        if (endpoint.equals("txt2img")) {
            addParameterField("prompt", "text", "Enter your prompt", "", true);
            addParameterField("negative_prompt", "text", "Enter negative prompt", "", false);
            addParameterField("steps", "number", "Number of inference steps", "20", true);
            addParameterField("width", "number", "Image width", "512", true);
            addParameterField("height", "number", "Image height", "512", true);
            addParameterField("guidance_scale", "slider", "Guidance Scale", "7.5", true);
        } else if (endpoint.equals("img2img")) {
            addParameterField("prompt", "text", "Enter your prompt", "", true);
            addParameterField("negative_prompt", "text", "Enter negative prompt", "", false);
            addParameterField("steps", "number", "Number of inference steps", "20", true);
            addParameterField("strength", "slider", "Denoising strength", "0.75", true);
            // Add image upload button
            addImageUploadButton();
        }
    }

    private void addParameterField(String name, String type, String hint, String defaultValue, boolean required) {
        MaterialCardView card = new MaterialCardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, 16);
        card.setLayoutParams(cardParams);
        card.setRadius(8);
        card.setCardElevation(2);

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(32, 16, 32, 16);

        TextView label = new TextView(this);
        label.setText(name + (required ? " *" : ""));
        container.addView(label);

        View inputView;
        switch (type) {
            case "text":
                TextInputLayout textInputLayout = new TextInputLayout(this);
                TextInputEditText editText = new TextInputEditText(this);
                editText.setHint(hint);
                editText.setText(defaultValue);
                textInputLayout.addView(editText);
                inputView = textInputLayout;
                break;

            case "number":
                TextInputLayout numberInputLayout = new TextInputLayout(this);
                TextInputEditText numberInput = new TextInputEditText(this);
                numberInput.setHint(hint);
                numberInput.setText(defaultValue);
                numberInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
                numberInputLayout.addView(numberInput);
                inputView = numberInputLayout;
                break;

            case "slider":
                Slider slider = new Slider(this);
                slider.setValueFrom(0);
                slider.setValueTo(20);
                slider.setValue(Float.parseFloat(defaultValue));
                slider.setStepSize(0.5f);
                inputView = slider;
                break;

            default:
                throw new IllegalArgumentException("Unsupported parameter type: " + type);
        }

        container.addView(inputView);
        card.addView(container);
        parameterContainer.addView(card);
        parameterViews.put(name, inputView);
    }

    private void addImageUploadButton() {
        Button uploadButton = new Button(this);
        uploadButton.setText("Upload Image");
        uploadButton.setOnClickListener(v -> {
            // Implement image upload logic
            Toast.makeText(this, "Image upload not implemented yet", Toast.LENGTH_SHORT).show();
        });
        parameterContainer.addView(uploadButton);
    }

    private void generateImage() {
        if (userToken == null) {
            Toast.makeText(this, "Please sign in first", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show progress and disable button
        progressBar.setVisibility(View.VISIBLE);
        generateButton.setEnabled(false);

        // Collect parameters
        Map<String, Object> parameters = new HashMap<>();
        String selectedEndpoint = endpointSpinner.getSelectedItem().toString();

        for (Map.Entry<String, View> entry : parameterViews.entrySet()) {
            String paramName = entry.getKey();
            View view = entry.getValue();
            Object value = getParameterValue(view);
            if (value != null) {
                parameters.put(paramName, value);
            }
        }

        // Convert parameters to JSON
        try {
            JSONObject jsonParams = new JSONObject(parameters);
            String route = "/generate/" + selectedEndpoint;

            networkSender.sendHttpRequest(route, jsonParams.toString(), userToken,
                    new NetworkSender.ResponseCallback() {
                        @Override
                        public void onSuccess(String response) {
                            runOnUiThread(() -> {
                                progressBar.setVisibility(View.GONE);
                                generateButton.setEnabled(true);
                                handleGenerationResponse(response);
                            });
                        }

                        @Override
                        public void onError(String errorMessage) {
                            runOnUiThread(() -> {
                                progressBar.setVisibility(View.GONE);
                                generateButton.setEnabled(true);
                                Toast.makeText(GenerationActivity.this,
                                        "Generation failed: " + errorMessage,
                                        Toast.LENGTH_LONG).show();
                            });
                        }
                    });

        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            generateButton.setEnabled(true);
            Toast.makeText(this, "Error preparing request: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private Object getParameterValue(View view) {
        if (view instanceof TextInputLayout) {
            TextInputEditText editText = (TextInputEditText) ((TextInputLayout) view).getEditText();
            return editText != null ? editText.getText().toString() : null;
        } else if (view instanceof Slider) {
            return ((Slider) view).getValue();
        }
        return null;
    }

    private void handleGenerationResponse(String response) {
        try {
            JSONObject jsonResponse = new JSONObject(response);

            // Handle the response based on your API structure
            // For example:
            // String imageUrl = jsonResponse.getString("image_url");
            // loadImage(imageUrl);

            if (jsonResponse.has("image") || jsonResponse.has("image_url")) {
                String imageUrl = jsonResponse.optString("image_url",
                        jsonResponse.optString("image"));
                loadImage(imageUrl);
            } else {
                showError("No image URL in response");
            }

        } catch (Exception e) {
            showError("Error processing response: " + e.getMessage());
        }
    }



    // Optional: Add image loading method if your API returns image URLs
//    private void loadImage(String imageUrl) {
//        // Implement image loading logic here
//        // You might want to use libraries like Glide or Picasso
//        generatedImageView.setVisibility(View.VISIBLE);
//    }

    private void showError(String message) {
        errorText.setText(message);
        errorText.setVisibility(View.VISIBLE);
        generatedImageView.setVisibility(View.GONE);
    }

    private void loadImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            showError("Invalid image URL");
            return;
        }

        // Show loading progress
        imageLoadingProgress.setVisibility(View.VISIBLE);
        generatedImageView.setVisibility(View.GONE);
        errorText.setVisibility(View.GONE);

        Glide.with(this)
                .load(imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.NONE) // Disable caching for generated images
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                Target<Drawable> target, boolean isFirstResource) {
                        runOnUiThread(() -> {
                            imageLoadingProgress.setVisibility(View.GONE);
                            showError("Failed to load image: " + (e != null ? e.getMessage() : "unknown error"));
                        });
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model,
                                                   Target<Drawable> target, DataSource dataSource,
                                                   boolean isFirstResource) {
                        runOnUiThread(() -> {
                            imageLoadingProgress.setVisibility(View.GONE);
                            generatedImageView.setVisibility(View.VISIBLE);
                            errorText.setVisibility(View.GONE);
                        });
                        return false;
                    }
                })
                .into(generatedImageView);
    }
}