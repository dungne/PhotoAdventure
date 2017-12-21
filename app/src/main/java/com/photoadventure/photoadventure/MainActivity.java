package com.photoadventure.photoadventure;

import android.*;
import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    // Constant
    private static String TAG = "MainActivity";
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_CAMERA_PERMISSION = 2;

    // Views
    private ImageButton mTakePhotoButton;
    private ImageButton mBrowsePhotoButton;
    private ImageButton mSharePhotoButton;

    private ImageView mImageView;

    // Properties
    private String mCurrentPhotoPath;
    private Uri mPhotoUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Views
        mTakePhotoButton = findViewById(R.id.take_photo_button);
        mBrowsePhotoButton = findViewById(R.id.browse_photo_button);
        mSharePhotoButton = findViewById(R.id.share_photo_button);

        mImageView = findViewById(R.id.imageView);

        // Take photo
        mTakePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= 23) {
                    String[] PERMISSIONS = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_EXTERNAL_STORAGE};
                    if (!hasPermission(MainActivity.this, PERMISSIONS)) {
                        ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS, REQUEST_CAMERA_PERMISSION);
                    } else {
                        takePhotoIntent();
                    }
                } else {
                    takePhotoIntent();
                }
            }
        });


        BottomNavigationView navigation = findViewById(R.id.bottom_navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takePhotoIntent();
            }
        }
    }

    private boolean hasPermission(Context context, String[] permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private void takePhotoIntent() {
        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePhotoIntent.resolveActivity(getPackageManager()) != null) {
            //TODO: Save full-size photo
//            // Create the File where the photo should be stored
//            File photoFile = null;
//            try {
//                photoFile = createPhotoFile();
//            } catch (IOException ex) {
//
//            }
//
//            // Continue only if the File was successfully created
//            if (photoFile != null) {
////                Uri photoURI = FileProvider.getUriForFile(this,
////                                                        "com.example.android.fileprovider",
////                                                                 photoFile);
//                mPhotoUri = Uri.fromFile(photoFile);
//                takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mPhotoUri);
                startActivityForResult(takePhotoIntent, REQUEST_IMAGE_CAPTURE);
//            }
        }
    }

    private File createPhotoFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES); //getFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        // Save a file: path for use with ACTION_VIEW intent
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            mImageView.setImageBitmap(imageBitmap);

//            this.grabImage(mImageView);
        }
    }

    // Grab full size image after the camera intent finished
    public void grabImage(ImageView imageView) {
        this.getContentResolver().notifyChange(mPhotoUri, null);
        ContentResolver cr = this.getContentResolver();
        Bitmap bitmap;
        try {
            bitmap = android.provider.MediaStore.Images.Media.getBitmap(cr, mPhotoUri);
            imageView.setImageBitmap(bitmap);
        } catch (Exception e) {
            Log.d(TAG, "Failed to load", e);
        }

    }

    // Bottom Navigation
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:

                    return true;
                case R.id.navigation_dashboard:

                    return true;
                case R.id.navigation_notifications:

                    return true;
            }
            return false;
        }
    };

}
