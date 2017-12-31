package com.photoadventure.photoadventure;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FolderActivity extends AppCompatActivity {

    private static final String TAG = "FolderActivity";

    // Views
    private RecyclerView mPhotoRecyclerView;

    // Properties
    private List<Object> mPhotoList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder);

        mPhotoRecyclerView = findViewById(R.id.photo_recyclerView);
        mPhotoRecyclerView.setHasFixedSize(true);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(3, 1);
        mPhotoRecyclerView.setLayoutManager(layoutManager);



        // Load photo in folder
        mPhotoList = new ArrayList<>();

        File photosPath = new File(Environment.getExternalStorageDirectory(), "Android/data/com.photoadventure.photoadventure/files/Pictures");
        if (photosPath.exists()) {
            String[] photoNames = photosPath.list();
            for (int i = 0; i < photoNames.length; i++) {
                Bitmap bitmap = BitmapFactory.decodeFile(photosPath.getPath() + "/" + photoNames[i]);
                mPhotoList.add(bitmap);
            }
        }

        Log.d(TAG, "mPhotoList: " + mPhotoList.toString());

        PhotoAdapter photoAdapter = new PhotoAdapter(mPhotoList);
        mPhotoRecyclerView.setAdapter(photoAdapter);


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
            Toast.makeText(v.getContext(), "Clicked position: " + getPosition(), Toast.LENGTH_SHORT).show();
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
