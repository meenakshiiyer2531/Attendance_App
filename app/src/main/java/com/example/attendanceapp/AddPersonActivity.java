package com.example.attendanceapp;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AddPersonActivity extends AppCompatActivity {

    private EditText nameEditText;
    private Button addButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_person);

        nameEditText = findViewById(R.id.nameEditText);
        addButton = findViewById(R.id.addButton);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newName = nameEditText.getText().toString().trim();
                if (!newName.isEmpty()) {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("newName", newName);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                } else {
                    Toast.makeText(AddPersonActivity.this, "Please enter a name", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
