package com.example.artisticintelligence;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;


public class PromptActivity extends AppCompatActivity {
    private static final String TAG = "PromptActivity";

    private static final long MAX_SEED_VALUE = 4294967294L;
    private static final int MAX_OUTPAINT_VALUE = 2000;

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

    // New UI component references
    private Map<String, SeekBar> seekBars;
    private Map<String, Spinner> spinners;
    private CheckBox keepOriginalBackgroundCheckbox;

    private String userId;
    private String authToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        userId = getIntent().getStringExtra("USER_ID");
        authToken = getIntent().getStringExtra("GOOGLE_TOKEN");


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prompt);

        initializeMaps();
        initializeViews();
        setupImagePicker();
        setupModeSpinner();
        setupSpinners();
        setupSubmitButton();
    }

    private void initializeMaps() {
        modeLayouts = new HashMap<>();
        imagePreviews = new HashMap<>();
        selectedImages = new HashMap<>();
        textInputs = new HashMap<>();
        seekBars = new HashMap<>();
        spinners = new HashMap<>();
    }

    private void initializeViews() {
        modeSpinner = findViewById(R.id.mode_spinner);
        submitButton = findViewById(R.id.submit_button);
        loadingOverlay = findViewById(R.id.loading_overlay);
        loadingText = findViewById(R.id.loading_text);

        // Initialize layouts
        modeLayouts.put("generate", findViewById(R.id.generate_layout));
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
        textInputs.put("generate", findViewById(R.id.generate_prompt_input));
        textInputs.put("generate_negative", findViewById(R.id.generate_negative_prompt));
        textInputs.put("generate_seed", findViewById(R.id.generate_seed));
        textInputs.put("sketch", findViewById(R.id.sketch_prompt_input));
        textInputs.put("sketch_negative", findViewById(R.id.sketch_negative_prompt));
        textInputs.put("style", findViewById(R.id.style_prompt_input));
        textInputs.put("style_negative", findViewById(R.id.style_negative_prompt));
        textInputs.put("outpaint", findViewById(R.id.outpaint_prompt_input));
        textInputs.put("left_input", findViewById(R.id.left_input));
        textInputs.put("right_input", findViewById(R.id.right_input));
        textInputs.put("up_input", findViewById(R.id.up_input));
        textInputs.put("down_input", findViewById(R.id.down_input));
        textInputs.put("search", findViewById(R.id.search_text_input));
        textInputs.put("replace", findViewById(R.id.replace_text_input));
        textInputs.put("search_negative", findViewById(R.id.search_negative_prompt));
        textInputs.put("background_prompt", findViewById(R.id.replace_bg_prompt_input));
        textInputs.put("foreground_prompt", findViewById(R.id.replace_bg_foreground_prompt));
        textInputs.put("replace_bg_negative", findViewById(R.id.replace_bg_negative_prompt));

        // Initialize seekbars
        seekBars.put("control_strength", findViewById(R.id.control_strength_seekbar));
        seekBars.put("fidelity", findViewById(R.id.fidelity_seekbar));
        seekBars.put("creativity", findViewById(R.id.creativity_seekbar));
        seekBars.put("preserve_subject", findViewById(R.id.replace_bg_preserve_subject));
        seekBars.put("background_depth", findViewById(R.id.replace_bg_depth));
        seekBars.put("light_strength", findViewById(R.id.light_source_strength));

        // Initialize spinners
//        spinners.put("generate_aspect_ratio", findViewById(R.id.generate_aspect_ratio_spinner));
//        spinners.put("generate_output_format", findViewById(R.id.generate_output_format));
//        spinners.put("output_format", findViewById(R.id.output_format_spinner));


//        LinearLayout parentLayout = findViewById(R.id.main_container);
//
//        // Loop through child views and find ones with the specific tag
//        for (int i = 0; i < parentLayout.getChildCount(); i++) {
//            View child = parentLayout.getChildAt(i);
//            if ("output_format_tag".equals(child.getTag())) {
//                // Perform actions on views with tag "group1"
//                if (child instanceof Spinner) {
//                    Spinner spinner = (Spinner) child;
//                    spinner.setAdapter(ArrayAdapter.createFromResource(this, R.array.output_formats, android.R.layout.simple_spinner_item));
//                }
//            }
//        }

        spinners.put("aspect_ratio_gen", findViewById(R.id.aspect_ratio_spinner_gen));
        spinners.put("aspect_ratio", findViewById(R.id.aspect_ratio_spinner_style));

        spinners.put("output_format", findViewById(R.id.output_format));
        spinners.put("output_format_gen", findViewById(R.id.output_format_gen));
        spinners.put("output_format_sketch", findViewById(R.id.output_format_sketch));
        spinners.put("output_format_style", findViewById(R.id.output_format_style));
        spinners.put("output_format_outpaint", findViewById(R.id.output_format_outpaint));
        spinners.put("output_format_search_replace", findViewById(R.id.output_format_search_replace));
        spinners.put("output_format_remove_bg", findViewById(R.id.output_format_remove_bg));

        spinners.put("light_direction", findViewById(R.id.light_source_direction));


        // Initialize checkboxes
        keepOriginalBackgroundCheckbox = findViewById(R.id.keep_original_background);


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

        // spinner for generation modes
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.generation_modes,
                android.R.layout.simple_spinner_item);
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

    private void setupSpinners() {
        ArrayAdapter<CharSequence> outputFormatAdapter = ArrayAdapter.createFromResource(
                this, R.array.output_formats, android.R.layout.simple_spinner_item);
        outputFormatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ArrayAdapter<CharSequence> aspectRatioAdapter = ArrayAdapter.createFromResource(
                this, R.array.aspect_ratios, android.R.layout.simple_spinner_item);
        aspectRatioAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ArrayAdapter<CharSequence> LSDAdapter = ArrayAdapter.createFromResource(
                this, R.array.light_source_directions, android.R.layout.simple_spinner_item);
        aspectRatioAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


        setupSpinner("output_format_gen", outputFormatAdapter);
        setupSpinner("output_format_sketch", outputFormatAdapter);
        setupSpinner("output_format_style", outputFormatAdapter);
        setupSpinner("output_format_outpaint", outputFormatAdapter);
        setupSpinner("output_format_search_replace", outputFormatAdapter);
        setupSpinner("output_format_remove_bg", outputFormatAdapter);
        setupSpinner("output_format", outputFormatAdapter);

        setupSpinner("aspect_ratio_gen", aspectRatioAdapter);
        setupSpinner("aspect_ratio", aspectRatioAdapter);

        setupSpinner("light_direction", LSDAdapter);

    }

    private void setupSpinner(String spinnerKey, ArrayAdapter<CharSequence> adapter) {
        Spinner spinner = spinners.get(spinnerKey);
        if (spinner != null) {
            spinner.setAdapter(adapter);
        }
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
        String[] modes = {"generate", "sketch", "style", "outpaint", "search_replace", "remove_bg", "replace_bg"};
        return position < modes.length ? modes[position] : modes[0];
    }

    private void setupSubmitButton() {
        submitButton.setOnClickListener(v -> {
            if (validateInputs()) {
                String currentMode = getSelectedMode(modeSpinner.getSelectedItemPosition());
                switch (currentMode) {
                    case "generate":
                        sendGenerateRequest();
                    case "sketch":
                        sendSketchRequest();
                        break;
                    case "style":
                        sendStyleRequest();
                        break;
                    case "outpaint":
                        sendOutpaintRequest();
                        break;
                    case "searchAndReplace":
                        sendSearchAndReplaceRequest();
                        break;
                    case "removeBackground":
                        sendRemoveBackgroundRequest();
                    case "removeBackgroundAndRelight":
                        sendRemoveBackgroundAndRelightRequest();
                        break;
                    default:
                        showError("Unsupported mode: " + currentMode);
                }
            }
        });
    }

    private void sendGenerateRequest() {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("mode", "generate");
            requestBody.put("prompt", getTextInputValue("generate"));

            requestBody.put("negative_prompt", getOptionalString("generate_negative"));
            requestBody.put("aspect_ratio", spinners.get("aspect_ratio_gen").getSelectedItem().toString());
            requestBody.put("output_format", spinners.get("output_format_gen").getSelectedItem().toString());

            requestBody.put("user_id", Integer.parseInt(userId));

            String seed = getOptionalSeedValue("generate_seed");
            if (!seed.isEmpty()) {
                requestBody.put("seed", seed);
            }
            else {
                requestBody.put("seed", "");
            }

            sendHttpRequest(requestBody, "generate");
        } catch (Exception e) {
            showError("Error preparing Generate request: " + e.getMessage());
        }
    }


    private void sendSketchRequest() {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("mode", "sketch");
            requestBody.put("prompt", getTextInputValue("sketch"));

            if (selectedImages.containsKey("sketch")) {
                requestBody.put("reference_image", encodeImage(selectedImages.get("sketch")));
            }

            requestBody.put("negative_prompt", getOptionalString("sketch_negative"));
            requestBody.put("control_strength", getOptionalSeekBarValue("control_strength"));
            requestBody.put("output_format", spinners.get("output_format_sketch").getSelectedItem().toString());

            String seed = getOptionalSeedValue("sketch_seed");
            if (!seed.isEmpty()) {
                requestBody.put("seed", seed);
            }
            else {
                requestBody.put("seed", "");
            }

            sendHttpRequest(requestBody, "sketch");
        } catch (Exception e) {
            showError("Error preparing Sketch request: " + e.getMessage());
        }
    }

    private void sendStyleRequest() {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("mode", "style");
            requestBody.put("prompt", getTextInputValue("style"));
            requestBody.put("negative_prompt", getOptionalString("style_negative"));
            requestBody.put("aspect_ratio", spinners.get("aspect_ratio").getSelectedItem().toString());
            requestBody.put("fidelity", getOptionalSeekBarValue("fidelity"));

            if (selectedImages.containsKey("style")) {
                requestBody.put("image", encodeImage(selectedImages.get("style")));
            }

            String seed = getOptionalSeedValue("style_seed");
            if (!seed.isEmpty()) {
                requestBody.put("seed", seed);
            }
            else {
                requestBody.put("seed", "");
            }

            sendHttpRequest(requestBody, "style");
        } catch (Exception e) {
            showError("Error preparing Style request: " + e.getMessage());
        }
    }

    private void sendOutpaintRequest() {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("mode", "outpaint");
            requestBody.put("prompt", getTextInputValue("outpaint"));

            if (selectedImages.containsKey("outpaint")) {
                requestBody.put("image", encodeImage(selectedImages.get("outpaint")));
            }

            requestBody.put("left", getTextInputValue("left_input"));
            requestBody.put("right", getTextInputValue("right_input"));
            requestBody.put("up", getTextInputValue("up_input"));
            requestBody.put("down", getTextInputValue("down_input"));
            requestBody.put("creativity", getOptionalSeekBarValue("creativity"));
            requestBody.put("output_format", spinners.get("output_format_outpaint").getSelectedItem().toString());

            String seed = getOptionalSeedValue("outpaint_seed");
            if (!seed.isEmpty()) {
                requestBody.put("seed", seed);
            }
            else {
                requestBody.put("seed", "");
            }

            sendHttpRequest(requestBody, "outpaint");
        } catch (Exception e) {
            showError("Error preparing Outpaint request: " + e.getMessage());
        }
    }

    private void sendSearchAndReplaceRequest() {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("mode", "searchAndReplace");
            requestBody.put("search_prompt", getTextInputValue("search"));
            requestBody.put("replace_prompt", getTextInputValue("replace"));

            if (selectedImages.containsKey("search_replace")) {
                requestBody.put("image", encodeImage(selectedImages.get("search_replace")));
            }

            requestBody.put("negative_prompt", getOptionalString("search_negative"));
            requestBody.put("output_format", spinners.get("output_format_search_replace").getSelectedItem().toString());

            String seed = getOptionalSeedValue("search_replace_seed");
            if (!seed.isEmpty()) {
                requestBody.put("seed", seed);
            }
            else {
                requestBody.put("seed", "");
            }

            sendHttpRequest(requestBody, "searchAndReplace");
        } catch (Exception e) {
            showError("Error preparing Search and Replace request: " + e.getMessage());
        }
    }

    private void sendRemoveBackgroundRequest() {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("mode", "removeBackground");

            if (selectedImages.containsKey("remove_bg")) {
                requestBody.put("image", encodeImage(selectedImages.get("remove_bg")));
            }

            requestBody.put("output_format", spinners.get("output_format_remove_bg").getSelectedItem().toString());

            String seed = getOptionalSeedValue("remove_bg_seed");
            if (!seed.isEmpty()) {
                requestBody.put("seed", seed);
            }
            else {
                requestBody.put("seed", "");
            }

            sendHttpRequest(requestBody, "removeBackground");
        } catch (Exception e) {
            showError("Error preparing Remove Background request: " + e.getMessage());
        }
    }

    private void sendRemoveBackgroundAndRelightRequest() {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("mode", "removeBackgroundAndRelight");

            if (selectedImages.containsKey("replace_bg")) {
                requestBody.put("image", encodeImage(selectedImages.get("replace_bg")));
            }

            requestBody.put("background_prompt", getTextInputValue("background_prompt"));

            // Optional parameters
            requestBody.put("foreground_prompt", getOptionalString("foreground_prompt"));
            requestBody.put("negative_prompt", getOptionalString("replace_bg_negative"));
            requestBody.put("preserve_original_subject", getOptionalSeekBarValue("preserve_subject"));
            requestBody.put("original_background_depth", getOptionalSeekBarValue("background_depth"));
            requestBody.put("keep_original_background", keepOriginalBackgroundCheckbox.isChecked());
            requestBody.put("light_source_strength", getOptionalSeekBarValue("light_strength"));
            requestBody.put("light_source_direction", spinners.get("light_direction").getSelectedItem().toString());
            requestBody.put("output_format", spinners.get("output_format").getSelectedItem().toString());

            String seed = getOptionalSeedValue("replace_bg_seed");
            if (!seed.isEmpty()) {
                requestBody.put("seed", seed);
            }
            else {
                requestBody.put("seed", "");
            }

            sendHttpRequest(requestBody, "removeBackgroundAndRelight");
        } catch (Exception e) {
            showError("Error preparing Remove Background and Relight request: " + e.getMessage());
        }
    }


//    private void sendHttpRequest(JSONObject requestBody, String mode) {
//        String authToken = getIntent().getStringExtra("GOOGLE_TOKEN");
//        NetworkSender networkSender = new NetworkSender();
//
//        networkSender.sendHttpRequest("/generate", requestBody.toString(), authToken, new NetworkSender.ResponseCallback() {
//            @Override
//            public void onSuccess(String response) {
//                runOnUiThread(() -> {
//                    try {
//                        JSONObject jsonResponse = new JSONObject(response);
//
//                        if (jsonResponse.has("error")) {
//                            showError(jsonResponse.getString("error"));
//                            return;
//                        }
//
//                        String imageData = jsonResponse.getString("image");
//                        if (imageData.startsWith("http")) {
//                            loadImageFromUrl(imageData, imagePreviews.get(mode));
//                        } else {
//                            loadBase64Image(imageData, imagePreviews.get(mode));
//                        }
//                    } catch (Exception e) {
//                        showError("Error processing response: " + e.getMessage());
//                    }
//                });
//            }
//
//            @Override
//            public void onError(String errorMessage) {
//                runOnUiThread(() -> showError("Request failed: " + errorMessage));
//            }
//        });
//    }

    private void sendHttpRequest(JSONObject requestBody, String mode) {
        String authToken = getIntent().getStringExtra("GOOGLE_TOKEN");
        if (authToken == null || authToken.isEmpty()) {
            showError("Google ID is missing. Please log in again.");
            return;
        }


        NetworkSender networkSender = new NetworkSender();
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + authToken); // Assuming Bearer token



        networkSender.sendHttpRequest("/generate", requestBody.toString(), authToken, new NetworkSender.ResponseCallback() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);

                        // Check for server-side error messages
                        if (jsonResponse.has("error")) {
                            showError("Server Error: " + jsonResponse.getString("error"));
                            return;
                        }

                        // Handle the "image" field in the response
                        String imageData = jsonResponse.getString("image");
                        ImageView targetImageView = imagePreviews.get(mode);

                        if (imageData.startsWith("http")) {
                            // Load image from URL
                            loadImageFromUrl(imageData, targetImageView);
                        } else {
                            // Decode Base64-encoded image
                            loadBase64Image(imageData, targetImageView);
                        }

                        showSuccess("Image generated successfully!");

                    } catch (JSONException e) {
                        showError("Response Parsing Error: " + e.getMessage());
                    } catch (Exception e) {
                        showError("Unexpected Error: " + e.getMessage());
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    if (errorMessage != null && !errorMessage.isEmpty()) {
                        showError("Request Failed: " + errorMessage);
                    } else {
                        showError("Request Failed: Unknown error occurred.");
                    }
                    setLoadingState(false);
                });
            }
        });
    }



//    private boolean validateInputs() {
//        String currentMode = getSelectedMode(modeSpinner.getSelectedItemPosition());
//
//        // Common validation for all modes - validate seed if provided
//        String seedKey = currentMode + "_seed";
//        String seedValue = getTextInputValue(seedKey);
//        if (!seedValue.isEmpty()) {
//            try {
//                long seed = Long.parseLong(seedValue);
//                if (seed < 0 || seed > MAX_SEED_VALUE) {
//                    showError("Seed must be between 0 and " + MAX_SEED_VALUE);
//                    return false;
//                }
//            } catch (NumberFormatException e) {
//                showError("Invalid seed value");
//                return false;
//            }
//        }
//
//
//        // Seed validation for all modes
////        String seedValue = "";
//        switch (currentMode) {
//            case "generate":
//                seedValue = getTextInputValue("generate_seed");
//                break;
//            case "sketch":
//                seedValue = getTextInputValue("sketch_seed");
//                break;
//            case "style":
//                seedValue = getTextInputValue("style_seed");
//                break;
//            case "outpaint":
//                seedValue = getTextInputValue("outpaint_seed");
//                break;
//            case "search_replace":
//                seedValue = getTextInputValue("search_replace_seed");
//                break;
//            case "remove_bg":
//                seedValue = getTextInputValue("remove_bg_seed");
//                break;
//            case "replace_bg":
//                seedValue = getTextInputValue("replace_bg_seed");
//                break;
//        }
//
//        if (!seedValue.isEmpty()) {
//            try {
//                long seed = Long.parseLong(seedValue);
//                if (seed < 0 || seed > MAX_SEED_VALUE) {
//                    showError("Seed must be between 0 and " + MAX_SEED_VALUE);
//                    return false;
//                }
//            } catch (NumberFormatException e) {
//                showError("Invalid seed value");
//                return false;
//            }
//        }
//
//
//
//        // Mode specific validation
//        switch (currentMode) {
//
//            case "generate":
//                if (getTextInputValue("generate").isEmpty()) {
//                    showError("Please enter a prompt");
//                    return false;
//                }
//                break;
//
//            case "sketch":
//                if (getTextInputValue("sketch").isEmpty()) {
//                    showError("Please enter a prompt");
//                    return false;
//                }
//                if (!selectedImages.containsKey("sketch")) {
//                    showError("Please upload a reference image");
//                    return false;
//                }
//                break;
//
//            case "style":
//                if (getTextInputValue("style").isEmpty()) {
//                    showError("Please enter a style description");
//                    return false;
//                }
//                break;
//
//            case "outpaint":
//                if (getTextInputValue("outpaint").isEmpty()) {
//                    showError("Please enter a prompt");
//                    return false;
//                }
//                if (!selectedImages.containsKey("outpaint")) {
//                    showError("Please upload an image to outpaint");
//                    return false;
//                }
//
//                // Validate outpaint dimensions
//                try {
//                    int left = Integer.parseInt(getTextInputValue("left_input"));
//                    int right = Integer.parseInt(getTextInputValue("right_input"));
//                    int up = Integer.parseInt(getTextInputValue("up_input"));
//                    int down = Integer.parseInt(getTextInputValue("down_input"));
//
//                    // Enforce that all values cannot be 0
//                    if (left == 0 && right == 0 && up == 0 && down == 0) {
//                        showError("At least one outpaint direction must be non-zero");
//                        return false;
//                    }
//
//                    // Validate individual values
//                    if (left < 0 || right < 0 || up < 0 || down < 0 ||
//                            left > MAX_OUTPAINT_VALUE || right > MAX_OUTPAINT_VALUE ||
//                            up > MAX_OUTPAINT_VALUE || down > MAX_OUTPAINT_VALUE) {
//                        showError("Outpaint values must be between 0 and " + MAX_OUTPAINT_VALUE);
//                        return false;
//                    }
//                } catch (NumberFormatException e) {
//                    showError("Please enter valid numbers for outpaint dimensions");
//                    return false;
//                }
//                break;
//
//            case "search_replace":
//                if (getTextInputValue("search").isEmpty()) {
//                    showError("Please enter what to search for");
//                    return false;
//                }
//                if (getTextInputValue("replace").isEmpty()) {
//                    showError("Please enter what to replace with");
//                    return false;
//                }
//                if (!selectedImages.containsKey("search_replace")) {
//                    showError("Please upload an image");
//                    return false;
//                }
//                break;
//
//            case "remove_bg":
//                if (!selectedImages.containsKey("remove_bg")) {
//                    showError("Please upload an image");
//                    return false;
//                }
//                break;
//
//            case "replace_bg":
//                if (!selectedImages.containsKey("replace_bg")) {
//                    showError("Please upload an image");
//                    return false;
//                }
//                if (getTextInputValue("background_prompt").isEmpty()) {
//                    showError("Please enter a background prompt");
//                    return false;
//                }
//                break;
//        }
//
//        return true;
//    }

    private boolean validateInputs() {
        String currentMode = getSelectedMode(modeSpinner.getSelectedItemPosition());

        // Validate seed (common to all modes)
        String seedKey = currentMode + "_seed";
        String seedValue = getTextInputValue(seedKey);
        if (!seedValue.isEmpty()) {
            try {
                long seed = Long.parseLong(seedValue);
                if (seed < 0 || seed > MAX_SEED_VALUE) {
                    showError("Seed must be between 0 and " + MAX_SEED_VALUE);
                    return false;
                }
            } catch (NumberFormatException e) {
                showError("Invalid seed value");
                return false;
            }
        }

        // Validate mode-specific inputs
        switch (currentMode) {
            case "generate":
                if (getTextInputValue("generate").isEmpty()) {
                    showError("Please enter a prompt for Generate mode.");
                    return false;
                }
                break;

            case "sketch":
                if (getTextInputValue("sketch").isEmpty()) {
                    showError("Please enter a prompt for Sketch mode.");
                    return false;
                }
                if (!selectedImages.containsKey("sketch")) {
                    showError("Please upload a reference image for Sketch mode.");
                    return false;
                }
                break;

            case "style":
                if (getTextInputValue("style").isEmpty()) {
                    showError("Please enter a style description for Style mode.");
                    return false;
                }
                break;

            case "outpaint":
                if (getTextInputValue("outpaint").isEmpty()) {
                    showError("Please enter a prompt for Outpaint mode.");
                    return false;
                }
                if (!selectedImages.containsKey("outpaint")) {
                    showError("Please upload an image for Outpaint mode.");
                    return false;
                }
                if (!validateOutpaintDimensions()) {
                    return false;
                }
                break;

            case "searchAndReplace":
                if (getTextInputValue("search").isEmpty()) {
                    showError("Please enter a search prompt for Search and Replace mode.");
                    return false;
                }
                if (getTextInputValue("replace").isEmpty()) {
                    showError("Please enter a replace prompt for Search and Replace mode.");
                    return false;
                }
                if (!selectedImages.containsKey("search_replace")) {
                    showError("Please upload an image for Search and Replace mode.");
                    return false;
                }
                break;

            case "removeBackground":
                if (!selectedImages.containsKey("remove_bg")) {
                    showError("Please upload an image for Remove Background mode.");
                    return false;
                }
                break;

            case "removeBackgroundAndRelight":
                if (!selectedImages.containsKey("replace_bg")) {
                    showError("Please upload an image for Remove Background and Relight mode.");
                    return false;
                }
                if (getTextInputValue("background_prompt").isEmpty()) {
                    showError("Please enter a background prompt for Remove Background and Relight mode.");
                    return false;
                }
                break;

            default:
                showError("Unsupported mode: " + currentMode);
                return false;
        }

        return true;
    }

    private boolean validateOutpaintDimensions() {
        try {
            int left = Integer.parseInt(getTextInputValue("left_input"));
            int right = Integer.parseInt(getTextInputValue("right_input"));
            int up = Integer.parseInt(getTextInputValue("up_input"));
            int down = Integer.parseInt(getTextInputValue("down_input"));

            if (left == 0 && right == 0 && up == 0 && down == 0) {
                showError("At least one outpaint direction must be non-zero.");
                return false;
            }

            if (left < 0 || right < 0 || up < 0 || down < 0 ||
                    left > MAX_OUTPAINT_VALUE || right > MAX_OUTPAINT_VALUE ||
                    up > MAX_OUTPAINT_VALUE || down > MAX_OUTPAINT_VALUE) {
                showError("Outpaint dimensions must be between 0 and " + MAX_OUTPAINT_VALUE + ".");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Invalid input for Outpaint dimensions.");
            return false;
        }
        return true;
    }




    private void loadImageFromUrl(String imageUrl, ImageView imageView) {
        Picasso.get()
                .load(imageUrl)
                .into(imageView);
        imageView.setVisibility(View.VISIBLE);
    }


    private void loadBase64Image(String base64Data, ImageView imageView) {
        try {
            byte[] decodedBytes = Base64.decode(base64Data, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            imageView.setImageBitmap(bitmap);
            imageView.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            showError("Error decoding image: " + e.getMessage());
        }
    }

//    private float getSeekBarValue(String key) {
//        SeekBar seekBar = seekBars.get(key);
//        if (seekBar != null) {
//            return (float) seekBar.getProgress() / seekBar.getMax();
//        }
//        return 0f;
//    }

    private String getTextInputValue(String key) {
        EditText input = textInputs.get(key);
        return input != null ? input.getText().toString().trim() : "";
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

        // Disable all seekbars
        for (SeekBar seekBar : seekBars.values()) {
            if (seekBar != null) {
                seekBar.setEnabled(!loading);
            }
        }

        // Disable all spinners
        for (Spinner spinner : spinners.values()) {
            if (spinner != null) {
                spinner.setEnabled(!loading);
            }
        }

        // Disable all upload buttons
        for (String mode : modeLayouts.keySet()) {
            Button uploadButton = findViewById(getUploadButtonId(mode));
            if (uploadButton != null) {
                uploadButton.setEnabled(!loading);
            }
        }

        if (keepOriginalBackgroundCheckbox != null) {
            keepOriginalBackgroundCheckbox.setEnabled(!loading);
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

    // Add these methods to the class
    private String getOptionalString(String key) {
        String value = getTextInputValue(key);
        return value; // Empty string is already handled by getTextInputValue
    }

    private String getOptionalSeekBarValue(String key) {
        SeekBar seekBar = seekBars.get(key);
        if (seekBar != null) {
            return String.valueOf(seekBar.getProgress() / (float)seekBar.getMax());
        }
        return "";
    }



//    private String getOptionalSpinnerValue(String key) {
//        Spinner spinner = spinners.get(key);
//        if (spinner != null) {
//            return spinner.getSelectedItem().toString();
//        }
//        return "";
//    }

    private String getOptionalSeedValue(String key) {
        String value = getTextInputValue(key);
        if (!value.isEmpty()) {
            try {
                long seed = Long.parseLong(value);
                if (seed >= 0 && seed <= MAX_SEED_VALUE) {
                    return value;
                }
            } catch (NumberFormatException e) {
                // Return empty string if invalid
            }
        }
        return "";
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