package com.example.android.photoeditor;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.Permission;

public class EditorActivity extends AppCompatActivity {

    private int REQUEST_CODE = 123;

    public static final int PICK_IMAGE = 1;

    private ImageView imageView;

    private Bitmap selected_img;

    private Uri uri;

    Save savefile;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        /** Getting the Uri of the image passed from the previous intent */
        Intent intent = getIntent();
        uri = intent.getData();

        imageView = findViewById(R.id.image);

        InputStream in;
        /** Setting up the image on the layout */
        try {
            in = getContentResolver().openInputStream(uri);
            selected_img = BitmapFactory.decodeStream(in);
            imageView.setImageBitmap(selected_img);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this, "An error occured!",
                    Toast.LENGTH_LONG).show();
        }
    }

    public void filter(View view) {
        Intent intent = new Intent(EditorActivity.this, FiltersActivity.class);
        intent.setData(uri);
        startActivityForResult(intent, PICK_IMAGE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_filters.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                Log.v("Image", "Bitmap " + selected_img);
                savefile = new Save();
                // Checking if permission is not granted
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_DENIED) {
                    ActivityCompat
                            .requestPermissions(
                                    this,
                                    new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE },
                                    REQUEST_CODE);
                }
                else {
                    try {
                        savefile.saveImage(this, selected_img);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

//        /** Check if an image is selected */
//        if (requestCode == Activity.RESULT_OK) {
//            /** Creating the Uri of the image */
        byte[] byteArray = data.getByteArrayExtra("image");
        selected_img = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

        imageView.setImageBitmap(selected_img);
//        } else {
//        Toast.makeText(this, "You didn't pick an image!",
//        Toast.LENGTH_LONG).show();
//        }
    }

//    private void requestPermission() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            requestPermissions(new String[]{
//                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_PERMISSION);
//        } else {
//            openFilePicker();
//        }
//    }

    // This function is called when user accept or decline the permission.
// Request Code is used to check which permission called this function.
// This request code is provided when user is prompt for permission.

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        super
                .onRequestPermissionsResult(requestCode,
                        permissions,
                        grantResults);

        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                try {
                    savefile.saveImage(this, selected_img);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else {
                Toast.makeText(this,
                        "Storage Permission Denied",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }
}
