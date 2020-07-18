package com.example.android.photoeditor;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class FiltersActivity extends AppCompatActivity {

    private ImageView imageView;

    private Bitmap selected_img;

    private Bitmap inverted_img;

    private Bitmap contrasted_img;

    private Bitmap sepia_img;

    private Bitmap saturated_img;

    private Bitmap current_img;

    private boolean isInverted = false;

    private boolean isContrasted = false;

    private boolean isSepia = false;

    private boolean isSaturated = false;

    private Uri uri;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filters);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Button buttonView = findViewById(R.id.button_1);
        buttonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                current_img = selected_img;
                imageView.setImageBitmap(current_img);
            }
        });

        buttonView = findViewById(R.id.button_2);
        buttonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isInverted) {
                    inverted_img = doInvert(selected_img);
                    isInverted = true;
                }
                current_img = inverted_img;
                imageView.setImageBitmap(current_img);
            }
        });

        buttonView = findViewById(R.id.button_3);
        buttonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isContrasted) {
                    contrasted_img = createContrast(selected_img, 50);
                    isContrasted = true;
                }
                current_img = contrasted_img;
                imageView.setImageBitmap(current_img);
            }
        });

        buttonView = findViewById(R.id.button_4);
        buttonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isSepia) {
                    sepia_img = createSepiaToningEffect(selected_img, 100, 0.7, 0.3, 0.12);
                    isSepia = true;
                }
                current_img = sepia_img;
                imageView.setImageBitmap(current_img);
            }
        });

        buttonView = findViewById(R.id.button_5);
        buttonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isSaturated) {
                    saturated_img = applySaturationFilter(selected_img, 2);
                    isSaturated = true;
                }
                current_img = saturated_img;
                imageView.setImageBitmap(current_img);
            }
        });

        /** Getting the Uri of the image passed from the previous intent */
        Intent intent = getIntent();
        uri = intent.getData();

        imageView = findViewById(R.id.image);

        String filename = getIntent().getStringExtra("image");
        try {
            FileInputStream is = this.openFileInput(filename);
            selected_img = BitmapFactory.decodeStream(is);
            current_img = selected_img;
            imageView.setImageBitmap(current_img);
            is.close();
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
                Intent resultIntent = new Intent();
                //Convert to byte array
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                current_img.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                resultIntent.putExtra("image", byteArray);

                setResult(Activity.RESULT_OK, resultIntent);
                finish();
                return true;
            case android.R.id.home:

                try {
                    //Write file
                    String filename = "bitmap.png";
                    FileOutputStream stream1 = this.openFileOutput(filename, Context.MODE_PRIVATE);
                    selected_img.compress(Bitmap.CompressFormat.PNG, 100, stream1);

                    //Cleanup
                    stream1.close();
                    selected_img.recycle();

                    //Pop intent
                    Intent intent = new Intent(FiltersActivity.this, EditorActivity.class);
                    intent.setData(null);
                    intent.putExtra("image", filename);
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
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
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
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
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                // get pixel color
                pixel = src.getPixel(x, y);
                A = Color.alpha(pixel);
                // apply filter contrast for every channel R, G, B
                R = Color.red(pixel);
                R = (int) (((((R / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
                if (R < 0) {
                    R = 0;
                } else if (R > 255) {
                    R = 255;
                }

                G = Color.red(pixel);
                G = (int) (((((G / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
                if (G < 0) {
                    G = 0;
                } else if (G > 255) {
                    G = 255;
                }

                B = Color.red(pixel);
                B = (int) (((((B / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
                if (B < 0) {
                    B = 0;
                } else if (B > 255) {
                    B = 255;
                }

                // set new pixel color to output bitmap
                bmOut.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }

        // return final image
        return bmOut;
    }

    public Bitmap createSepiaToningEffect(Bitmap src, int depth, double red, double green, double blue) {
        // image size
        int width = src.getWidth();
        int height = src.getHeight();
        // create output bitmap
        Bitmap bmOut = Bitmap.createBitmap(width, height, src.getConfig());
        // constant grayscale
        final double GS_RED = 0.3;
        final double GS_GREEN = 0.59;
        final double GS_BLUE = 0.11;
        // color information
        int A, R, G, B;
        int pixel;

        // scan through all pixels
        for(int x = 0; x < width; ++x) {
            for(int y = 0; y < height; ++y) {
                // get pixel color
                pixel = src.getPixel(x, y);
                // get color on each channel
                A = Color.alpha(pixel);
                R = Color.red(pixel);
                G = Color.green(pixel);
                B = Color.blue(pixel);
                // apply grayscale sample
                B = G = R = (int)(GS_RED * R + GS_GREEN * G + GS_BLUE * B);

                // apply intensity level for sepid-toning on each channel
                R += (depth * red);
                if(R > 255) { R = 255; }

                G += (depth * green);
                if(G > 255) { G = 255; }

                B += (depth * blue);
                if(B > 255) { B = 255; }

                // set new pixel color to output image
                bmOut.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }

        // return final image
        return bmOut;
    }

    public Bitmap applySaturationFilter(Bitmap source, int level) {
        // get image size
        int width = source.getWidth();
        int height = source.getHeight();
        int[] pixels = new int[width * height];
        float[] HSV = new float[3];
        // get pixel array from source
        source.getPixels(pixels, 0, width, 0, 0, width, height);

        int index = 0;
        // iteration through pixels
        for(int y = 0; y < height; ++y) {
            for(int x = 0; x < width; ++x) {
                // get current index in 2D-matrix
                index = y * width + x;
                // convert to HSV
                Color.colorToHSV(pixels[index], HSV);
                // increase Saturation level
                HSV[1] *= level;
                HSV[1] = (float) Math.max(0.0, Math.min(HSV[1], 1.0));
                // take color back
                pixels[index] = Color.HSVToColor(HSV);
            }
        }
        // output bitmap
        Bitmap bmOut = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bmOut.setPixels(pixels, 0, width, 0, 0, width, height);
        return bmOut;
    }

}
