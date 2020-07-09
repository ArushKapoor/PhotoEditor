package com.example.android.photoeditor;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;

public class FiltersActivity extends AppCompatActivity {

    private Bitmap bitmap;

    private OutputStream outputStream;

    private ImageView imageView;

    private Bitmap selected_img;

    private Bitmap current_img;

    private Uri uri;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filters);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /** Getting the Uri of the image passed from the previous intent */
        Intent intent = getIntent();
        uri = intent.getData();

        imageView = findViewById(R.id.image);
        InputStream in;

        /** Setting up the image on the layout */
        try {
            in = getContentResolver().openInputStream(uri);
            selected_img = BitmapFactory.decodeStream(in);
            current_img = selected_img;
            imageView.setImageBitmap(current_img);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this, "An error occured!",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_filters.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_filters, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                Intent resultIntent = new Intent();
                //Convert to byte array
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                current_img.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                resultIntent.putExtra("image",byteArray);

                setResult(Activity.RESULT_OK, resultIntent);
                finish();
                return true;
            case android.R.id.home:
                Intent intent = new Intent(FiltersActivity.this, EditorActivity.class);
                intent.setData(uri);
                startActivity(intent);
//                NavUtils.navigateUpFromSameTask(FiltersActivity.this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void defaultImage (View view) {
        current_img = selected_img;
        imageView.setImageBitmap(current_img);
    }

    public void filter1 (View view) {
        current_img = doInvert(selected_img);
        imageView.setImageBitmap(current_img);
    }

    public void filter2 (View view) {
        current_img = createContrast(selected_img, 50);
        imageView.setImageBitmap(current_img);
    }

    public Bitmap doInvert(Bitmap src) {
        // create new bitmap with the same settings as source bitmap
        Bitmap bmOut = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());
        // color info
        int A, R, G, B;
        int pixelColor;
        // image size
        int height = src.getHeight();
        int width = src.getWidth();

        // scan through every pixel
        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                // get one pixel
                pixelColor = src.getPixel(x, y);
                // saving alpha channel
                A = Color.alpha(pixelColor);
                // inverting byte for each R/G/B channel
                R = 255 - Color.red(pixelColor);
                G = 255 - Color.green(pixelColor);
                B = 255 - Color.blue(pixelColor);
                // set newly-inverted pixel to output image
                bmOut.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }

        // return final bitmap
        return bmOut;
    }

    public Bitmap createContrast(Bitmap src, double value) {
        // image size
        int width = src.getWidth();
        int height = src.getHeight();
        // create output bitmap
        Bitmap bmOut = Bitmap.createBitmap(width, height, src.getConfig());
        // color information
        int A, R, G, B;
        int pixel;
        // get contrast value
        double contrast = Math.pow((100 + value) / 100, 2);

        // scan through all pixels
        for(int x = 0; x < width; ++x) {
            for(int y = 0; y < height; ++y) {
                // get pixel color
                pixel = src.getPixel(x, y);
                A = Color.alpha(pixel);
                // apply filter contrast for every channel R, G, B
                R = Color.red(pixel);
                R = (int)(((((R / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
                if(R < 0) { R = 0; }
                else if(R > 255) { R = 255; }

                G = Color.red(pixel);
                G = (int)(((((G / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
                if(G < 0) { G = 0; }
                else if(G > 255) { G = 255; }

                B = Color.red(pixel);
                B = (int)(((((B / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
                if(B < 0) { B = 0; }
                else if(B > 255) { B = 255; }

                // set new pixel color to output bitmap
                bmOut.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }

        // return final image
        return bmOut;
    }

}
