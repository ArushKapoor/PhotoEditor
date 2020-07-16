package com.example.android.photoeditor;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.FileInputStream;
import java.io.IOException;

public class CollageActivity extends AppCompatActivity {

    private int REQUEST_CODE = 123;

    private ImageView imageView;

    private Bitmap collage;

    Save savefile;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collage);

        /** Getting the Bitmap of the images passed from the previous intent */
        Intent intent = getIntent();
        Bitmap[] bitmaps = new Bitmap[4];
        int width = 0, height = 0;
        for (int i = 0; i < bitmaps.length; i++) {
            String filename = getIntent().getStringExtra("image" + i);
            try {
                FileInputStream is = this.openFileInput(filename);
                bitmaps[i] = BitmapFactory.decodeStream(is);
                collage = Bitmap.createScaledBitmap(bitmaps[i], bitmaps[0].getWidth(), bitmaps[0].getHeight(), false);
                bitmaps[i] = collage;
                width += bitmaps[i].getWidth();
                height += bitmaps[i].getHeight();
//                current_img = selected_img;
//                imageView.setImageBitmap(current_img);
                is.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        imageView = findViewById(R.id.collage);
//        imageView.setImageBitmap(bitmaps[0]);

        Log.v("Collage", "Testing to see if it works");

//        final Bitmap horizontalMerge = horizontalMerge(bitmaps[0], bitmaps[1]);
//        final Bitmap verticalMerge = verticalMerge(bitmaps[0], bitmaps[1]);

//        imageView.setImageBitmap(horizontalMerge);

        collage = Bitmap.createBitmap(bitmaps[0].getWidth() + bitmaps[1].getWidth(), bitmaps[0].getHeight() + bitmaps[1].getHeight(), bitmaps[0].getConfig());
        Canvas canvas = new Canvas(collage);
        canvas.drawBitmap(bitmaps[0], 0f, 0f, null);
        canvas.drawBitmap(bitmaps[1], bitmaps[0].getWidth(), 0, null);
        canvas.drawBitmap(bitmaps[2], 0, bitmaps[0].getHeight(), null);
        canvas.drawBitmap(bitmaps[3], bitmaps[0].getWidth(), bitmaps[0].getHeight(), null);

        imageView.setImageBitmap(collage);
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
                Log.v("Image", "Bitmap " + collage);
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
                        savefile.saveImage(this, collage);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
