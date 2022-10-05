package com.example.filepicker_by_extension;

import static android.os.Build.VERSION.SDK_INT;

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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import me.rosuh.filepicker.config.FilePickerManager;

public class MainActivity extends AppCompatActivity {
    private AppCompatTextView viewText;
    private StringBuilder resultText;
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 2001, PICK_SYSTEM_FILE = 2365;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewText = findViewById(R.id.viewTV);
        findViewById(R.id.pickBtn).setOnClickListener(v -> {
            if (isPermissionGranted())
                pickFile();
            else
                checkUserPermission();

        });
        findViewById(R.id.pickSystemBtn).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.putExtra("android.provider.extra.INITIAL_URI",
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));
            intent.setType("*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(intent, PICK_SYSTEM_FILE);

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
        FilePickerManager
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
                    showFileData(list.get(0));
                } else {
                    Toast.makeText(MainActivity.this, "You didn't choose anything~", Toast.LENGTH_SHORT).show();
                }
                break;
            case PICK_SYSTEM_FILE:
                if (resultCode == Activity.RESULT_OK) {
                    Uri uri = data.getData();

                    try {
                        String extension = uri.toString().substring(uri.toString().lastIndexOf(".") + 1);
                        if (uri != null && (extension.equals("json")||extension.equals("ovpn")))
                            showFileData(uri);
                        else {
                            Log.e("MainFilePick", "onFilePath: " + uri);
                            Toast.makeText(MainActivity.this, "Wrong file type with extension!", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }


    public void showFileData(Object filePath) {
        resultText = new StringBuilder();
        resultText.append("path: " + filePath);
        resultText.append("\n");
        Log.d("MainFilePick", "onActivityResult: " + filePath);

        // do your work
        resultText.append("--------------\n");
        try {
            BufferedReader br = null;
            if (filePath instanceof String) {
                File file = new File((String) filePath);
                br = new BufferedReader(new FileReader(file));
            } else if (filePath instanceof Uri) {
                InputStream in = getContentResolver().openInputStream((Uri) filePath);
                br = new BufferedReader(new InputStreamReader(in));
            }
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