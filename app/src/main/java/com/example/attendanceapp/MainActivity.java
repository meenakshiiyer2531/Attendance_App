package com.example.attendanceapp;
import android.Manifest;
import android.content.pm.PackageManager;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_READ_EXTERNAL_STORAGE = 1 ;
    private static final int REQUEST_CODE_UPLOAD_CSV = 101;
    private ListView listView;
    private Button addPersonButton, generateReportButton;
    private ArrayList<String> peopleList;
    private ArrayAdapter<String> adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.listView);
        addPersonButton = findViewById(R.id.addPersonButton);
        generateReportButton = findViewById(R.id.generateReportButton);

        peopleList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, R.layout.list_item, R.id.personName, peopleList);
        listView.setAdapter(adapter);
        listView = findViewById(R.id.listView);
        generateReportButton = findViewById(R.id.generateReportButton);

        // Disable buttons initially
        addPersonButton.setEnabled(true);
        generateReportButton.setEnabled(false);
        listView.setEnabled(false);

        peopleList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, peopleList);
        listView.setAdapter(adapter);

        addPersonButton.setOnClickListener(v -> checkPermission());
        addPersonButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddPersonActivity.class);
            startActivityForResult(intent, REQUEST_CODE_ADD_PERSON);
        });

        addPersonButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("text/csv");
            startActivityForResult(intent, REQUEST_CODE_UPLOAD_CSV);
        });


        generateReportButton.setOnClickListener(v -> generateReport());

        loadAttendanceData();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_UPLOAD_CSV && resultCode == RESULT_OK && data != null) {
            Uri selectedFileUri = data.getData();
            if (selectedFileUri != null) {
                try {
                    InputStream inputStream = getContentResolver().openInputStream(selectedFileUri);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        // Assuming each line contains a person's name
                        adapter.add(line);
                    }
                    reader.close();
                    inputStream.close();

                    // Enable buttons after successful upload
                    addPersonButton.setEnabled(true);
                    generateReportButton.setEnabled(true);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error reading CSV file", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }



    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_CODE_READ_EXTERNAL_STORAGE);
        } else {
            // Permission is already granted, proceed to read CSV
            readCsvFile();
        }
    }

    private void readCsvFile() {
        generateReportButton.setEnabled(true);
        addPersonButton.setText("Add Person");
        listView.setEnabled(true);
    }

    // Declare the constant for the request code
        private static final int REQUEST_CODE_ADD_PERSON = 100;

        // Your existing code



    private void generateReport() {
        try {
            // Get the external storage directory
            File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

            // Ensure directory exists or create it
            if (!directory.exists()) {
                directory.mkdirs(); // Create directory if it doesn't exist
            }

            // Create the CSV file
            File csvFile = new File(directory, "attendance.csv");

            // Create a FileWriter to write data into the CSV file
            FileWriter writer = new FileWriter(csvFile);

            // Write the header
            writer.write("Name,Attendance\n");

            // Write data to the CSV file
            for (int i = 0; i < listView.getCount(); i++) {
                View view = listView.getChildAt(i);
                if (view != null) {
                    String person = adapter.getItem(i);
                    CheckBox checkBox = view.findViewById(R.id.checkBoxAttendance);
                    String attendanceStatus = checkBox.isChecked() ? "Present" : "Absent";

                    // Write the person name and attendance status to the CSV file
                    writer.write(person + "," + attendanceStatus + "\n");
                }
            }

            // Close the FileWriter
            writer.close();

            // Show a toast message indicating successful report generation
            Toast.makeText(this, "Attendance report generated at: " + csvFile.getAbsolutePath(), Toast.LENGTH_LONG).show();

            // Download the CSV file using DownloadManager
            downloadCsvFile(csvFile);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to generate attendance report: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void downloadCsvFile(File csvFile) {
        // Create a DownloadManager instance
        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

        // Create a DownloadManager.Request with the CSV file Uri
        DownloadManager.Request request = new DownloadManager.Request(Uri.fromFile(csvFile));

        // Set the title and description for the download notification
        request.setTitle("Attendance Report");
        request.setDescription("Downloading attendance report...");

        // Set the destination for the downloaded file
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, csvFile.getName());

        // Enqueue the download and get the download ID
        downloadManager.enqueue(request);
    }


    private void loadAttendanceData() {
        // Read data from the CSV file in res/raw folder
        InputStream inputStream = getResources().openRawResource(R.raw.attendance_sheet);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                // Split the line based on the delimiter (e.g., comma)
                String[] parts = line.split(",");
                // Add the person to the ListView
                adapter.add(parts[0]); // Assuming the first column contains names
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
