package com.example.android.photoeditor;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public static final int PICK_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /**
     * This method will open the gallery allowing the user to select the desired image.
     */
    @SuppressLint("IntentReset")
    public void uploadImage(View view) {
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        callBroadCast();

        /** Check if an image is selected */
        if (requestCode == PICK_IMAGE) {
            /** Creating the Uri of the image */
            final Uri uri = data.getData();

            /** Calling the intent to open the Editor Activity
             *  and sending in the Uri of the image */
            Intent intent = new Intent(MainActivity.this, EditorActivity.class);
            intent.setData(uri);
            startActivity(intent);
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

}