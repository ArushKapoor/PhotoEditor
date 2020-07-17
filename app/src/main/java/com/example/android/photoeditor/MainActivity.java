package com.example.android.photoeditor;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    public static final int PICK_IMAGE = 1;
    public static final int PICK_IMAGES = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button uploadButtonView = findViewById(R.id.upload);
        uploadButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });

        Button collageButtonView = findViewById(R.id.collage);
        collageButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createCollage();
            }
        });

    }

    /**
     * This method will open the gallery allowing the user to select the desired image.
     */
    @SuppressLint("IntentReset")
    public void uploadImage() {
        /** Setting up the intent to call the gallery */
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});

        /** Starting the intent to get the result back from the gallery */
        startActivityForResult(chooserIntent, PICK_IMAGE);

    }

    private void createCollage() {
//        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
//        getIntent.setType("image/*");
//
//        Intent pickIntent = new Intent(Intent.ACTION_PICK,
//                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//        pickIntent.setType("image/*");
//
//        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
//        chooserIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
//        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});
//
//        startActivityForResult(chooserIntent, PICK_IMAGES);

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, PICK_IMAGES);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        callBroadCast();

        Log.v("SelectImages", "Inside onActivityResult, checking requestCode   " + requestCode);

        /** Check if an image is selected */
        if (requestCode == PICK_IMAGE) {
            /** Creating the Uri of the image */
            final Uri uri = data.getData();

            /** Calling the intent to open the Editor Activity
             *  and sending in the Uri of the image */
            Intent intent = new Intent(MainActivity.this, EditorActivity.class);
            intent.setData(uri);
            startActivity(intent);
        } else if (requestCode == PICK_IMAGES) {
            if (data.getClipData() != null) {
                ClipData mClipData = data.getClipData();
                if(mClipData.getItemCount() != 4) {
                    Toast.makeText(this, "You can only choose 4 images for a collage", Toast.LENGTH_SHORT).show();
                    return;
                } else {
//                    Intent intent = new Intent(MainActivity.this, EditorActivity.class);
//                    intent.setData(uri);
//                    startActivity(intent);
                    InputStream in;
                    Uri uri;
                    Bitmap[] bitmaps = new Bitmap[mClipData.getItemCount()];
                    Bitmap selected_img;
                    for (int i = 0; i < mClipData.getItemCount(); i++) {
                        ClipData.Item item = mClipData.getItemAt(i);
                        uri = item.getUri();

                        /** Setting up the image on the layout */
                        try {
                            in = getContentResolver().openInputStream(uri);
                            selected_img = BitmapFactory.decodeStream(in);
                            bitmaps[i] = selected_img;
//                            imageView.setImageBitmap(selected_img);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                            Toast.makeText(this, "An error occured!",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                    if(bitmaps.length != 0) {
                        startCollageIntent(bitmaps);
                    }
                }
            } else {
                Toast.makeText(this, "You can only choose 4 images for a collage", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            Toast.makeText(this, "You didn't pick an image!",
                    Toast.LENGTH_LONG).show();
        }
    }

    public void callBroadCast() {
        if (Build.VERSION.SDK_INT >= 14) {
            Log.e("-->", " >= 14");
            MediaScannerConnection.scanFile(this, new String[]{Environment.getExternalStorageDirectory().toString()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                /*
                 *   (non-Javadoc)
                 * @see android.media.MediaScannerConnection.OnScanCompletedListener#onScanCompleted(java.lang.String, android.net.Uri)
                 */
                public void onScanCompleted(String path, Uri uri) {
//                    Log.e("ExternalStorage", "Scanned " + path + ":");
//                    Log.e("ExternalStorage", "-> uri=" + uri);
                }
            });
        } else {
//            Log.e("-->", " < 14");
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
                    Uri.parse("file://" + Environment.getExternalStorageDirectory())));
        }
    }

    private void startCollageIntent(Bitmap[] bitmaps) {
        Intent intent = new Intent(MainActivity.this, CollageActivity.class);
        for(int i = 0; i < bitmaps.length; i++) {
            try {
                //Write file
                String filename = "bitmap" + i + ".jpg";
                FileOutputStream stream = this.openFileOutput(filename, Context.MODE_PRIVATE);

                bitmaps[i].compress(Bitmap.CompressFormat.JPEG, 100, stream);

                //Cleanup
                stream.close();
                bitmaps[i].recycle();

                //Pop intent
                intent.putExtra("image" + i, filename);
//                intent.setData(uri);
//                startActivityForResult(intent, PICK_IMAGE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        startActivity(intent);
    }

}