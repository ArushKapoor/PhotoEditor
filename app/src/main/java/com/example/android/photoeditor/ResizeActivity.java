package com.example.android.photoeditor;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class ResizeActivity extends AppCompatActivity {

    private ImageView imageView;

    private Bitmap current_img;

    private Bitmap selected_img;

    private Uri uri;

    private Uri picUri;

    final int PIC_CROP = 1;

    int x = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resize);

        Button buttonView = findViewById(R.id.button_1);
        buttonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performCrop();
            }
        });

        buttonView = findViewById(R.id.button_2);
        buttonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                current_img = rotate(-90);
                imageViewAnimatedChange();
                //imageView.setImageBitmap(current_img);
            }
        });

        buttonView = findViewById(R.id.button_3);
        buttonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                current_img = rotate(90);
                imageViewAnimatedChange();
                //imageView.setImageBitmap(current_img);
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
                    selected_img.compress(Bitmap.CompressFormat.JPEG, 100, stream1);

                    //Cleanup
                    stream1.close();
                    selected_img.recycle();

                    //Pop intent
                    Intent intent = new Intent(ResizeActivity.this, EditorActivity.class);
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

    public Bitmap rotate(float degree) {
        // create new matrix
        Matrix matrix = new Matrix();
        // setup rotation degree
        matrix.postRotate(degree);

        // return new bitmap rotated using matrix
        return Bitmap.createBitmap(current_img, 0, 0, current_img.getWidth(), current_img.getHeight(), matrix, true);
    }

    private void performCrop() {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            current_img.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            String path = MediaStore.Images.Media.insertImage(this.getContentResolver(), current_img, "Title" + x, null);
            x++;
            picUri = Uri.parse(path);

            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            // indicate image type and Uri
            cropIntent.setDataAndType(picUri, "image/*");
            // set crop properties here
            cropIntent.putExtra("crop", true);
            // indicate aspect of desired crop
            cropIntent.putExtra("aspectX", 1);
            cropIntent.putExtra("aspectY", 1);
            // indicate output X and Y
            cropIntent.putExtra("outputX", 128);
            cropIntent.putExtra("outputY", 128);
            // retrieve data on return
            cropIntent.putExtra("return-data", true);
            // start the activity - we handle returning in onActivityResult
            startActivityForResult(cropIntent, PIC_CROP);
        }
        // respond to users whose devices do not support the crop action
        catch (ActivityNotFoundException anfe) {
            // display an error message
            String errorMessage = "Whoops - your device doesn't support the crop action!";
            Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PIC_CROP) {
            if (data != null) {
                // get the returned data
                Bundle extras = data.getExtras();
                // get the cropped bitmap
                current_img = extras.getParcelable("data");
                imageView.setImageBitmap(current_img);
                String imgPath = getRealPathFromURI(picUri);
                callBroadCast();
                File fdelete = new File(imgPath);
                Log.v("Images", "Inside onActivityResult  " + fdelete.exists());

                if (fdelete.exists()) {
                    if (fdelete.delete()) {
                        Log.v("Images", "Inside true fdelete");
                        Log.v("Images", "File deleted");
                    } else {
                        Log.v("Images", "Inside false fdelete");
                        Log.v("Images", "File not deleted");
                    }
                }
            }
        }
    }

    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
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
                    Log.e("ExternalStorage", "Scanned " + path + ":");
                    Log.e("ExternalStorage", "-> uri=" + uri);
                }
            });
        } else {
            Log.e("-->", " < 14");
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
                    Uri.parse("file://" + Environment.getExternalStorageDirectory())));
        }
    }

    public void imageViewAnimatedChange() {
        final Animation anim_out = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
        final Animation anim_in  = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        anim_out.setAnimationListener(new Animation.AnimationListener()
        {
            @Override public void onAnimationStart(Animation animation) {}
            @Override public void onAnimationRepeat(Animation animation) {}
            @Override public void onAnimationEnd(Animation animation)
            {
                imageView.setImageBitmap(current_img);
                anim_in.setAnimationListener(new Animation.AnimationListener() {
                    @Override public void onAnimationStart(Animation animation) {}
                    @Override public void onAnimationRepeat(Animation animation) {}
                    @Override public void onAnimationEnd(Animation animation) {}
                });
                imageView.startAnimation(anim_in);
            }
        });
        imageView.startAnimation(anim_out);
    }
}
