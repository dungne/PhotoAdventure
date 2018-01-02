package com.photoadventure.photoadventure;

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
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.storage.FirebaseStorage;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.photoadventure.photoadventure.MainActivity.REQUEST_CAMERA_PERMISSION;
import static com.photoadventure.photoadventure.MainActivity.REQUEST_IMAGE_CAPTURE;

public class FolderActivity extends AppCompatActivity {

    private static final String TAG = "FolderActivity";

    // Views
    private RecyclerView mPhotoRecyclerView;
    private FloatingActionButton mTakePhotoButton;
    private PhotoAdapter mPhotoAdapter;

    // Firebase
    FirebaseStorage mStorage = FirebaseStorage.getInstance();

    // Properties
    private List<Bitmap> mPhotoList;
    private String mCurrentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) throws Error {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder);


        // Take Photo Button init
        mTakePhotoButton = findViewById(R.id.take_photo_FloatingButton);
        mTakePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= 23) {
                    String[] PERMISSIONS = {android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.READ_EXTERNAL_STORAGE};
                    if (!hasPermission(FolderActivity.this, PERMISSIONS)) {
                        ActivityCompat.requestPermissions(FolderActivity.this, PERMISSIONS, REQUEST_CAMERA_PERMISSION);
                    } else {
                        Log.d(TAG, "get in onClick");
                        dispathTakePhotoIntent();
                    }
                } else {
                    dispathTakePhotoIntent();
                }
            }
        });



        // RecyclerView init
        mPhotoRecyclerView = findViewById(R.id.photo_recyclerView);
//        mPhotoRecyclerView.setHasFixedSize(true);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(3, 1);
        mPhotoRecyclerView.setLayoutManager(layoutManager);

        mPhotoAdapter = new PhotoAdapter(loadPhotos());

        mPhotoRecyclerView.setAdapter(mPhotoAdapter);


        // Load photo in folder
//        mPhotoList = new ArrayList<>();



//
//        File photosPath = new File(Environment.getExternalStorageDirectory(), "Android/data/com.photoadventure.photoadventure/files/Pictures");
//        if (photosPath.exists()) {
//            String[] photoNames = photosPath.list();
//            for (int i = 0; i < photoNames.length; i++) {
//                Bitmap bitmap = BitmapFactory.decodeFile(photosPath.getPath() + "/" + photoNames[i]);
//                mPhotoList.add(bitmap);
//            }
//        }
//
//        Log.d(TAG, "mPhotoList: " + mPhotoList.toString());
//
//        PhotoAdapter photoAdapter = new PhotoAdapter(mPhotoList);
//        mPhotoRecyclerView.setAdapter(photoAdapter);



    }

    public ArrayList<Bitmap> loadPhotos() {

        ArrayList<Bitmap> photoList = new ArrayList<>();
//        photoList.clear();

        File photosPath = new File(Environment.getExternalStorageDirectory(), "Android/data/com.photoadventure.photoadventure/files/Pictures");
        if (photosPath.exists()) {
            String[] photoNames = photosPath.list();

            BitmapFactory.Options options = new BitmapFactory.Options();
//            options.inJustDecodeBounds = true;
            options.inSampleSize = 8;

            Bitmap bitmap;

            for (int i = 0; i < photoNames.length; i++) {
                bitmap = BitmapFactory.decodeFile(photosPath.getPath() + "/" + photoNames[i], options);
                if (bitmap != null) {
//                    Log.d(TAG, "bitmap: " + bitmap.toString());
                }
                photoList.add(bitmap);
//                bitmap.recycle();

            }

//            Log.d(TAG, "mPhotoList: " + photoList.toString());


//            photoAdapter = new PhotoAdapter(photoList);
//            recyclerView.setAdapter(photoAdapter);
        }
        return photoList;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispathTakePhotoIntent();
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

    private void dispathTakePhotoIntent() {
        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //Ensure that there's a camera activity to handle the intent
        if (takePhotoIntent.resolveActivity(getPackageManager()) != null) {
            Log.d(TAG, "inside dispath");
            //TODO: Save full-size photo
            // Create the File where the photo should be stored
            File photoFile = null;
            try {
                photoFile = createPhotoFile();
            } catch (IOException ex) {
                Toast.makeText(FolderActivity.this, "Could not create a file to store image", Toast.LENGTH_SHORT).show();
            }

            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoUri = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);

                takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePhotoIntent, REQUEST_IMAGE_CAPTURE);
            }
        } else {
            Toast.makeText(this, "Could not open Camera", Toast.LENGTH_SHORT).show();
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
            //Get full-size image
//            File imageFile = new File(mCurrentPhotoPath);
//            if (imageFile.exists()) {
//                Bitmap myBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
//                mImageView.setImageBitmap(myBitmap);
//                mImageView.setRotation(20);
//            }

//            mPhotoAdapter = new PhotoAdapter(loadPhotos()); // Update with the new photo
//            mPhotoAdapter.notifyDataSetChanged(); // Reload photos

//            mPhotoRecyclerView.invalidate();



//            Uri photoUri = data.getParcelableExtra(MediaStore.EXTRA_OUTPUT);
//            Log.d(TAG, photoUri.toString() + "" );
//
//            String path = "Photos/" + UUID.randomUUID() + ".jpg";
//
//            StorageReference storageRef = mStorage.getReference(path);
//
//            storageRef.putFile(photoUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                @Override
//                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                    Uri downloadUri = taskSnapshot.getDownloadUrl();
//                    Log.d(TAG, "" + downloadUri.toString());
//                    Toast.makeText(FolderActivity.this, "Upload photo successfully", Toast.LENGTH_SHORT).show();
//                }
//            });



        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        mPhotoAdapter = new PhotoAdapter(loadPhotos()); // Update with the new photo
//            mPhotoAdapter.notifyDataSetChanged(); // Reload photos

            mPhotoRecyclerView.invalidate();

        ArrayList<Bitmap> photoList = loadPhotos();
        Bitmap photo = photoList.get(photoList.size() - 1);
        Log.d(TAG, "photo: " + photo.toString());
    }

    public class PhotoViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public ImageView photoImageView;

        public PhotoViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            photoImageView = itemView.findViewById(R.id.photo_imageView);
        }

        @Override
        public void onClick(View v) {
//            Toast.makeText(v.getContext(), "Clicked position: " + getPosition(), Toast.LENGTH_SHORT).show();
        }
    }


    public class PhotoAdapter extends RecyclerView.Adapter<PhotoViewHolder> {

        private List<Object> mPhotoList;

        public PhotoAdapter(List photoList) {
            mPhotoList = photoList;
        }

        @Override
        public PhotoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.photo_row, null);
            return new PhotoViewHolder(view);
        }

        @Override
        public void onBindViewHolder(PhotoViewHolder holder, int position) {
            //TODO: Bug: init list of photo as bitmap
            holder.photoImageView.setImageBitmap((Bitmap)mPhotoList.get(position));
        }

        @Override
        public int getItemCount() {
            return mPhotoList.size();
        }
    }
}
