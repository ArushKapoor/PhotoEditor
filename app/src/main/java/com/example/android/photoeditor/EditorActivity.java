package com.example.android.photoeditor;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class EditorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        /** Getting the Uri of the image passed from the previous intent */
        Intent intent = getIntent();
        Uri uri = intent.getData();

        ImageView imageView = findViewById(R.id.image);
        InputStream in;

        /** Setting up the image on the layout */
        try {
            in = getContentResolver().openInputStream(uri);
            final Bitmap selected_img = BitmapFactory.decodeStream(in);
            imageView.setImageBitmap(selected_img);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this, "An error occured!",
                    Toast.LENGTH_LONG).show();
        }
    }
}
