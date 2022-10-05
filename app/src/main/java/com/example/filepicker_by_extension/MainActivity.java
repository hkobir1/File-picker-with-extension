package com.example.filepicker_by_extension;

import static android.os.Build.VERSION.SDK_INT;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.rosuh.filepicker.config.FilePickerManager;

public class MainActivity extends AppCompatActivity {
    private AppCompatTextView viewText;
    private StringBuilder resultText;
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 2001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewText = findViewById(R.id.viewTV);
        resultText = new StringBuilder();
        findViewById(R.id.pickBtn).setOnClickListener(v -> {
            if (isPermissionGranted())
                pickFile();
            else
                checkUserPermission();

        });
    }

    public void pickFile() {
        List<String> allowedExtensions = new ArrayList<String>();

        // Add to the allowed Extensions the javascript and text files
        allowedExtensions.add("ovpn");
        allowedExtensions.add("conf");
        allowedExtensions.add("json");
        allowedExtensions.add("config");

        // 2. Apply filter by extension
        FilePickerManager.INSTANCE
                .from(this)
                .enableSingleChoice()
                .setTheme(R.style.FilePickerTheme)
                .filter(new FilterByExtension(allowedExtensions))
                .forResult(FilePickerManager.REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case FilePickerManager.REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    List<String> list = FilePickerManager.obtainData();
                    resultText.append("path: " + list.get(0));
                    resultText.append("\n");
                    Log.d("MainFilePick", "onActivityResult: " + list.get(0));

                    // do your work
                    resultText.append("--------------\n");
                    File file = new File(list.get(0));
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(file));
                        String line;

                        while ((line = br.readLine()) != null) {
                            resultText.append(line);
                            resultText.append('\n');
                        }
                        br.close();
                    } catch (IOException e) {
                        //You'll need to add proper error handling here
                        Log.e("MainFilePick", "Error: " + e.getLocalizedMessage());

                    }
                    //Set the text
                    viewText.setText(resultText.toString());


                } else {
                    Toast.makeText(MainActivity.this, "You didn't choose anything~", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void checkUserPermission() {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                //request for the permission
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
        } else
            requestPermission();
    }

    private void requestPermission() {
        List<String> listPermissionsNeeded = new ArrayList<>();
        listPermissionsNeeded.clear();
        int readStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int writeStorage = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (readStorage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (writeStorage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray
                    (new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);

        }
    }

    private boolean isPermissionGranted() {
        if (SDK_INT >= Build.VERSION_CODES.R)
            return (Environment.isExternalStorageManager());
        else
            return ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }
}