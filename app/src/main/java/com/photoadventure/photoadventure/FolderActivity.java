package com.photoadventure.photoadventure;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
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

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.photoadventure.photoadventure.MainActivity.REQUEST_CAMERA_PERMISSION;
import static com.photoadventure.photoadventure.MainActivity.REQUEST_IMAGE_CAPTURE;

public class FolderActivity extends AppCompatActivity {

    private static final String TAG = "FolderActivity";
    private static final String AUTHORITY = "com.example.android.fileprovider";
    private static final String PHOTOSPATH = "Android/data/com.photoadventure.photoadventure/files/Pictures";

    // Views
    private RecyclerView mPhotoRecyclerView;
    private FloatingActionButton mTakePhotoButton;
    private PhotoAdapter mPhotoAdapter;

    // Firebase
    FirebaseStorage mStorage = FirebaseStorage.getInstance();
    StorageReference mPhotosStorageRef = mStorage.getReference("Photos/");
    FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    DatabaseReference mPhotosDatabaseRef = mDatabase.getReference("Photos");

    // Properties
    private List<Bitmap> mPhotoList = new ArrayList<>();
    private String mCurrentPhotoPath = "";
    private List<Bitmap> mPhotosOnPhone = new ArrayList<>();
    private List<Bitmap> mPhotosOnDatabase = new ArrayList<>();

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
        mPhotoRecyclerView.setHasFixedSize(true);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(3, 1);
        mPhotoRecyclerView.setLayoutManager(layoutManager);


        // Load photo from Firebase Storage
        //TODO: Do we need to delete photo on phone after it is uploaded to database?
        mPhotosOnPhone = loadPhotosOnPhone();
        mPhotoList = mPhotosOnPhone;

        //TODO: Bug: just load image have on phone
        mPhotosDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                List<Bitmap> photoBitmapList = new ArrayList<>();

                // Get all photo information on Real-time database
                List<Photo> tempPhotoList = new ArrayList<>();
                for (DataSnapshot photoData : dataSnapshot.getChildren()) {
                    Photo photo = photoData.getValue(Photo.class);
                    Log.d(TAG, "photo in for loop: " + photo.getName());
                    tempPhotoList.add(photo);
                }

                Log.d(TAG, "tempPhotoList: " + tempPhotoList.toString());

                // Download the photo from photos' information URL
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 8;

                for (Photo photo : tempPhotoList) {
                    Log.d(TAG, "Photo: " + photo.getName());
                    Log.d(TAG, "photo URL: " + photo.getURLString());

                    Uri photoUri = Uri.parse(photo.getURLString());

                    // Load image from Uri, and put it into bitmap mPhotoList
                    Picasso.with(FolderActivity.this).load(photoUri).into(new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
//                            mPhotoList.add(bitmap);
                            
                        }

                        @Override
                        public void onBitmapFailed(Drawable errorDrawable) {

                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {

                        }
                    });


                }

                // Update photo in Recycler View
//                if (mPhotoAdapter != null) {
                    mPhotoAdapter.notifyDataSetChanged();
//                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Log.d(TAG, "Photo List onCreate: " + mPhotoList.toString());


        // Set up Recycler View with adapter
//        mPhotoAdapter = new PhotoAdapter(mPhotoList);
//        mPhotoRecyclerView.setAdapter(mPhotoAdapter);

    }

    @Override
    protected void onStart() {
        super.onStart();
        // Set up Recycler View with adapter
//        Log.d(TAG, "Photo List: " + mPhotoList.toString());
//        mPhotoAdapter = new PhotoAdapter(mPhotoList);
//        mPhotoRecyclerView.setAdapter(mPhotoAdapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Upload a new photo to server when capturing successfully
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            final File photo = new File(mCurrentPhotoPath);
            // Get new photo successfully
            if (photo.exists()) {
                // Get storage reference
                String path = "Photos/" + photo.getName();
                StorageReference storageRef = mStorage.getReference(path);

                // Create Uri to upload the photo
                Uri photoUri = FileProvider.getUriForFile(this, AUTHORITY, photo);
                UploadTask uploadTask = storageRef.putFile(photoUri);
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(FolderActivity.this, "Upload photo successfully", Toast.LENGTH_SHORT).show();
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();

                        // Add photo information to Real-time database
                        if (downloadUrl != null) {
                            String photoName = photo.getName();
                            Photo newPhoto = new Photo(photoName, downloadUrl.toString());

                            String reducedPhotoName = photoName.substring(0, photoName.indexOf(".")); //Remove .jpg from photo name, because database name cannot have "." character
                            mPhotosDatabaseRef.child(reducedPhotoName).setValue(newPhoto);
                        }


                    }
                });
            }
        }
    }

    // Return an ArrayList of photo on phone's storage
    public ArrayList<Bitmap> loadPhotosOnPhone() {
        ArrayList<Bitmap> photoList = new ArrayList<>();

        File photosPath = new File(Environment.getExternalStorageDirectory(), PHOTOSPATH);
        if (photosPath.exists()) {
            String[] photoNames = photosPath.list();

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 8;
            Bitmap bitmap;

            for (int i = 0; i < photoNames.length; i++) {
                bitmap = BitmapFactory.decodeFile(photosPath.getPath() + "/" + photoNames[i], options);
                if (bitmap != null) {
                }
                photoList.add(bitmap);
            }
        }
        return photoList;
    }

    // Handle Request Permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispathTakePhotoIntent();
            }
        }
    }


    // Check Permission of current phone
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

    // Create a new photo file, and start camera activity
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
                        AUTHORITY,
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
    protected void onResume() {
        super.onResume();


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
            holder.photoImageView.setImageBitmap((Bitmap) mPhotoList.get(position));
        }

        @Override
        public int getItemCount() {
            return mPhotoList.size();
        }
    }
}
