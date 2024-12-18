package com.example.artisticintelligence;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class PromptActivity extends AppCompatActivity {
    private static final String TAG = "PromptActivity";

    // UI Components
    private Spinner modeSpinner;
    private Button submitButton;
    private Map<String, View> modeLayouts;
    private Map<String, ImageView> imagePreviews;
    private Map<String, Uri> selectedImages;
    private Map<String, EditText> textInputs;
    private View loadingOverlay;
    private TextView loadingText;

    // State
    private boolean isProcessing = false;
    private String currentUploadMode;

    // Activity Result Launcher for image selection
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prompt);

        initializeMaps();
        initializeViews();
        setupImagePicker();
        setupModeSpinner();
        setupSubmitButton();
    }

    private void initializeMaps() {
        modeLayouts = new HashMap<>();
        imagePreviews = new HashMap<>();
        selectedImages = new HashMap<>();
        textInputs = new HashMap<>();
    }

    private void initializeViews() {
        modeSpinner = findViewById(R.id.mode_spinner);
        submitButton = findViewById(R.id.submit_button);
        loadingOverlay = findViewById(R.id.loading_overlay);
        loadingText = findViewById(R.id.loading_text);

        // Initialize layouts
        modeLayouts.put("sketch", findViewById(R.id.sketch_layout));
        modeLayouts.put("style", findViewById(R.id.style_layout));
        modeLayouts.put("outpaint", findViewById(R.id.outpaint_layout));
        modeLayouts.put("search_replace", findViewById(R.id.search_replace_layout));
        modeLayouts.put("remove_bg", findViewById(R.id.remove_bg_layout));
        modeLayouts.put("replace_bg", findViewById(R.id.replace_bg_layout));

        // Initialize image previews
        imagePreviews.put("sketch", findViewById(R.id.sketch_image_preview));
        imagePreviews.put("style", findViewById(R.id.style_image_preview));
        imagePreviews.put("outpaint", findViewById(R.id.outpaint_image_preview));
        imagePreviews.put("search_replace", findViewById(R.id.search_replace_image_preview));
        imagePreviews.put("remove_bg", findViewById(R.id.remove_bg_image_preview));
        imagePreviews.put("replace_bg", findViewById(R.id.replace_bg_image_preview));

        // Initialize text inputs
        textInputs.put("sketch", findViewById(R.id.sketch_prompt_input));
        textInputs.put("style", findViewById(R.id.style_prompt_input));
        textInputs.put("outpaint", findViewById(R.id.outpaint_prompt_input));
        textInputs.put("search", findViewById(R.id.search_text_input));
        textInputs.put("replace", findViewById(R.id.replace_text_input));
        textInputs.put("replace_bg", findViewById(R.id.replace_bg_prompt_input));

        // Setup upload buttons
        setupUploadButton("sketch", R.id.sketch_upload_button);
        setupUploadButton("style", R.id.style_upload_button);
        setupUploadButton("outpaint", R.id.outpaint_upload_button);
        setupUploadButton("search_replace", R.id.search_replace_upload_button);
        setupUploadButton("remove_bg", R.id.remove_bg_upload_button);
        setupUploadButton("replace_bg", R.id.replace_bg_upload_button);
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null && currentUploadMode != null) {
                            selectedImages.put(currentUploadMode, selectedImageUri);
                            ImageView preview = imagePreviews.get(currentUploadMode);
                            if (preview != null) {
                                preview.setImageURI(selectedImageUri);
                                preview.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                }
        );
    }

    private void setupUploadButton(String mode, int buttonId) {
        Button uploadButton = findViewById(buttonId);
        uploadButton.setOnClickListener(v -> {
            currentUploadMode = mode;
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });
    }

    private void setupModeSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.generation_modes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        modeSpinner.setAdapter(adapter);

        modeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateVisibleLayout(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                updateVisibleLayout(0);
            }
        });
    }

    private void updateVisibleLayout(int position) {
        String selectedMode = getSelectedMode(position);
        for (Map.Entry<String, View> entry : modeLayouts.entrySet()) {
            entry.getValue().setVisibility(
                    entry.getKey().equals(selectedMode) ? View.VISIBLE : View.GONE
            );
        }
    }

    private String getSelectedMode(int position) {
        String[] modes = {"sketch", "style", "outpaint", "search_replace", "remove_bg", "replace_bg"};
        return position < modes.length ? modes[position] : modes[0];
    }

    private void setupSubmitButton() {
        submitButton.setOnClickListener(v -> {
            if (validateInputs()) {
                processSubmission();
            }
        });
    }

    private boolean validateInputs() {
        String currentMode = getSelectedMode(modeSpinner.getSelectedItemPosition());

        // Check required image uploads
        if (currentMode.equals("remove_bg") && !selectedImages.containsKey("remove_bg")) {
            showError("Please upload an image");
            return false;
        }

        // Check required text inputs
        EditText requiredInput = textInputs.get(currentMode);
        if (requiredInput != null && requiredInput.getText().toString().trim().isEmpty()) {
            showError("Please enter a description");
            return false;
        }

        // Mode-specific validation
        if (currentMode.equals("search_replace")) {
            EditText searchInput = textInputs.get("search");
            EditText replaceInput = textInputs.get("replace");
            if (searchInput.getText().toString().trim().isEmpty() ||
                    replaceInput.getText().toString().trim().isEmpty()) {
                showError("Please fill in both search and replace fields");
                return false;
            }
        }

        return true;
    }

    private void processSubmission() {
        if (isProcessing) {
            return; // Prevent multiple submissions
        }

        String currentMode = getSelectedMode(modeSpinner.getSelectedItemPosition());
        setLoadingState(true);
        loadingText.setText("Generating " + currentMode + " image...");

        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("mode", currentMode);

            // Add mode-specific parameters
            switch (currentMode) {
                case "sketch":
                    requestBody.put("prompt", textInputs.get("sketch").getText().toString());
                    if (selectedImages.containsKey("sketch")) {
                        requestBody.put("reference_image", encodeImage(selectedImages.get("sketch")));
                    }
                    break;
                case "style":
                    requestBody.put("prompt", textInputs.get("style").getText().toString());
                    if (selectedImages.containsKey("style")) {
                        requestBody.put("image", encodeImage(selectedImages.get("style")));
                    }
                    break;
                case "outpaint":
                    requestBody.put("prompt", textInputs.get("outpaint").getText().toString());
                    if (selectedImages.containsKey("outpaint")) {
                        requestBody.put("image", encodeImage(selectedImages.get("outpaint")));
                    }
                    break;
                case "search_replace":
                    requestBody.put("search", textInputs.get("search").getText().toString());
                    requestBody.put("replace", textInputs.get("replace").getText().toString());
                    if (selectedImages.containsKey("search_replace")) {
                        requestBody.put("image", encodeImage(selectedImages.get("search_replace")));
                    }
                    break;
                case "remove_bg":
                    if (selectedImages.containsKey("remove_bg")) {
                        requestBody.put("image", encodeImage(selectedImages.get("remove_bg")));
                    }
                    break;
                case "replace_bg":
                    requestBody.put("prompt", textInputs.get("replace_bg").getText().toString());
                    if (selectedImages.containsKey("replace_bg")) {
                        requestBody.put("image", encodeImage(selectedImages.get("replace_bg")));
                    }
                    break;
            }

            String authToken = getIntent().getStringExtra("GOOGLE_TOKEN");
            NetworkSender networkSender = new NetworkSender();

            networkSender.sendHttpRequest("/generate", requestBody.toString(), authToken,
                    new NetworkSender.ResponseCallback() {
                        @Override
                        public void onSuccess(String response) {
                            runOnUiThread(() -> {
                                try {
                                    JSONObject jsonResponse = new JSONObject(response);
                                    if (jsonResponse.has("error")) {
                                        showError(jsonResponse.getString("error"));
                                    } else {
                                        showSuccess("Image generated successfully!");
                                        // TODO: Handle the generated image response
                                    }
                                } catch (JSONException e) {
                                    showError("Error processing response: " + e.getMessage());
                                } finally {
                                    setLoadingState(false);
                                }
                            });
                        }

                        @Override
                        public void onError(String errorMessage) {
                            runOnUiThread(() -> {
                                showError("Generation failed: " + errorMessage);
                                setLoadingState(false);
                            });
                        }
                    });

        } catch (Exception e) {
            showError("Error preparing request: " + e.getMessage());
            setLoadingState(false);
        }
    }

    private void setLoadingState(boolean loading) {
        isProcessing = loading;
        loadingOverlay.setVisibility(loading ? View.VISIBLE : View.GONE);

        // Disable/Enable UI elements
        modeSpinner.setEnabled(!loading);
        submitButton.setEnabled(!loading);

        // Disable all text inputs
        for (EditText input : textInputs.values()) {
            if (input != null) {
                input.setEnabled(!loading);
            }
        }

        // Disable all upload buttons
        for (String mode : modeLayouts.keySet()) {
            Button uploadButton = findViewById(getUploadButtonId(mode));
            if (uploadButton != null) {
                uploadButton.setEnabled(!loading);
            }
        }
    }

    private int getUploadButtonId(String mode) {
        switch (mode) {
            case "sketch": return R.id.sketch_upload_button;
            case "style": return R.id.style_upload_button;
            case "outpaint": return R.id.outpaint_upload_button;
            case "search_replace": return R.id.search_replace_upload_button;
            case "remove_bg": return R.id.remove_bg_upload_button;
            case "replace_bg": return R.id.replace_bg_upload_button;
            default: return 0;
        }
    }

    private String encodeImage(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes);
            return Base64.encodeToString(bytes, Base64.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showSuccess(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        if (isProcessing) {
            Toast.makeText(this, "Please wait while processing...", Toast.LENGTH_SHORT).show();
        } else {
            super.onBackPressed();
        }
    }
}