package com.example.android.photoeditor;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class EditorActivity extends AppCompatActivity {

    private int REQUEST_CODE = 123;

    public static final int PICK_IMAGE = 1;

    private ImageView imageView;

    private Bitmap selected_img;

    private Uri uri;

    private Uri picUri;

    Save savefile;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        /** Getting the Uri of the image passed from the previous intent */
        Intent intent = getIntent();
        uri = intent.getData();

        imageView = findViewById(R.id.image);

        if (uri != null) {
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
        } else {
            String filename = getIntent().getStringExtra("image");
            try {
                FileInputStream is = this.openFileInput(filename);
                selected_img = BitmapFactory.decodeStream(is);
                imageView.setImageBitmap(selected_img);
                is.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Button resizeButtonView = findViewById(R.id.resize);
        resizeButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resize();
            }
        });

        Button filtersButtonView = findViewById(R.id.filters);
        filtersButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter();
            }
        });
    }

    public void resize() {

        try {
            //Write file
            String filename = "bitmap.jpg";
            FileOutputStream stream = this.openFileOutput(filename, Context.MODE_PRIVATE);
            selected_img.compress(Bitmap.CompressFormat.JPEG, 100, stream);

            //Cleanup
            stream.close();
            selected_img.recycle();

            //Pop intent
            Intent intent = new Intent(EditorActivity.this, ResizeActivity.class);
            intent.putExtra("image", filename);
            intent.setData(uri);
            startActivityForResult(intent, PICK_IMAGE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void filter() {

        try {
            //Write file
            String filename = "bitmap.jpg";
            FileOutputStream stream = this.openFileOutput(filename, Context.MODE_PRIVATE);
            selected_img.compress(Bitmap.CompressFormat.JPEG, 100, stream);

            //Cleanup
            stream.close();
            selected_img.recycle();

            //Pop intent
            Intent intent = new Intent(EditorActivity.this, FiltersActivity.class);
            intent.putExtra("image", filename);
            intent.setData(uri);
            startActivityForResult(intent, PICK_IMAGE);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    REQUEST_CODE);
                } else {
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

        byte[] byteArray = data.getByteArrayExtra("image");
        selected_img = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

        imageView.setImageBitmap(selected_img);
    }


    // This function is called when user accept or decline the permission.
    // Request Code is used to check which permission called this function.
    // This request code is provided when user is prompt for permission.

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
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
            } else {
                Toast.makeText(this,
                        "Storage Permission Denied",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }
}
