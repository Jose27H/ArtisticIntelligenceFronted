package com.example.artisticintelligence;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class PromptActivity extends AppCompatActivity {
    EditText descriptionInput;
    Button submitButton;
    Button uploadPictureButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_prompt);

    }

    private void setListeners(){
        descriptionInput = findViewById(R.id.description_input);
        submitButton = findViewById(R.id.submit_button);
        uploadPictureButton = findViewById(R.id.upload_picture_button);

        submitButton.setOnClickListener(this::submitFunction);
        uploadPictureButton.setOnClickListener(this::uploadPictureFunction);
    }

    private void submitFunction(View v){

    }

    private void uploadPictureFunction(View v){

    }
}