package com.photoadventure.photoadventure;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    FloatingActionButton button;

    private GPS gps;
    private Location mLocation;
    double lat, lon;
    String address;
    LatLng customMarker;

    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference();
        storageReference = FirebaseStorage.getInstance().getReference();

        gps = new GPS(getApplicationContext());
        mLocation = gps.getLocation();
        lat = mLocation.getLatitude();
        lon = mLocation.getLongitude();
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addressList = null;
        try{
            addressList = geocoder.getFromLocation(lat, lon, 1);
        } catch(IOException e) {
            e.printStackTrace();
        }
        address = addressList.get(0).getAddressLine(0);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    public void onClick(View view) {
        showInputBox();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (marker.getTitle().equals("me")) {
                    Intent intent = new Intent(MapsActivity.this, ChangePhoto.class);
                    startActivity(intent);
                } else if (!marker.getTitle().equals("me")) {
                    showInfo(marker.getTitle());
                }
                return false;
            }
        });
        customMarker = new LatLng(lat, lon);
//        LatLngBounds.Builder builder = new LatLngBounds.Builder();
//        builder.include(customMarker); //Taking Point A (First LatLng)
//        LatLngBounds bounds = builder.build();
//        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 200);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(customMarker, 14));
//        mMap.animateCamera(CameraUpdateFactory.zoomTo(14), 2000, null);
        String simpleUri = "https://www.simplifiedcoding.net/wp-content/uploads/2015/10/advertise.png";
        createMarker(simpleUri);
//        Bitmap loadedBitmap = createMarker(simpleUri);
//        ImageView dummyImage = findViewById(R.id.dummyImage);
//        dummyImage.setImageBitmap(loadedBitmap);
//        Picasso.with(getApplicationContext()).load(simpleUri).fit().centerCrop().into(dummyImage);

//        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
//            @Override
//            public void onMapLoaded() {
//                final LatLng customMarkerLocationOne = new LatLng(lat, lon);
//
//                storageReference.child("marker_photo").child("mPhoto").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//                    @Override
//                    public void onSuccess(Uri uri) {
//                        mMap.addMarker(new MarkerOptions()
//                                .position(customMarkerLocationOne)
//                                .title("me")
//                                .icon(BitmapDescriptorFactory
//                                        .fromBitmap(createMarker(MapsActivity.this, uri))));
//
//                        //LatLngBound will cover all your marker on Google Maps
//                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
//                        builder.include(customMarkerLocationOne); //Taking Point A (First LatLng)
//                        LatLngBounds bounds = builder.build();
//                        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 200);
//                        mMap.moveCamera(cu);
//                        mMap.animateCamera(CameraUpdateFactory.zoomTo(14), 2000, null);
//                    }
//                });
//            }
//        });
//        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
//            @Override
//            public void onMapLoaded() {
//                customMarker = new LatLng(lat, lon);
//                storageReference.child("marker_photo").child("mPhoto").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//                    @Override
//                    public void onSuccess(Uri uri) {
//                        Log.d("abc", "onSuccess: " + uri);
////                        mMap.addMarker(new MarkerOptions().position(customMarker).title("me")
////                                .icon(BitmapDescriptorFactory.fromBitmap()));
//
//                    }
//                });
//                 //LatLngBound will cover all your marker on Google Maps
//
//            }
//        });

        databaseReference.child("location").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    LocationStore locationStore = data.getValue(LocationStore.class);
                    double latitude = locationStore.getLatitude();
                    double longitude = locationStore.getLongitude();
                    String feature = locationStore.getFeature();
                    LatLng latLng = new LatLng(latitude, longitude);
                    switch (feature) {
                        case "home":
                            mMap.addMarker(new MarkerOptions().position(latLng).title(locationStore.getName()).icon(BitmapDescriptorFactory.fromResource(R.drawable.home)));
                            break;
                        case "work":
                            mMap.addMarker(new MarkerOptions().position(latLng).title(locationStore.getName()).icon(BitmapDescriptorFactory.fromResource(R.drawable.work)));
                            break;
                        case "restaurant":
                            mMap.addMarker(new MarkerOptions().position(latLng).title(locationStore.getName()).icon(BitmapDescriptorFactory.fromResource(R.drawable.restaurant)));
                            break;
                        case "cafe":
                            mMap.addMarker(new MarkerOptions().position(latLng).title(locationStore.getName()).icon(BitmapDescriptorFactory.fromResource(R.drawable.cafe)));
                            break;
                        case "shopping":
                            mMap.addMarker(new MarkerOptions().position(latLng).title(locationStore.getName()).icon(BitmapDescriptorFactory.fromResource(R.drawable.shopping)));
                            break;
                        default:
                            mMap.addMarker(new MarkerOptions().position(latLng).title(locationStore.getName()));
                            break;
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private void showInputBox() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.add_location);
        dialog.setTitle("Add your place");
        TextView currentLocation = (TextView) dialog.findViewById(R.id.currentLocation);
        currentLocation.setText(address);

        final Spinner spinner = (Spinner) dialog.findViewById(R.id.spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MapsActivity.this,
                android.R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.featureList));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        Button save = (Button) dialog.findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText place = (EditText) dialog.findViewById(R.id.place);
                EditText description = (EditText) dialog.findViewById(R.id.description);
                String feature = spinner.getSelectedItem().toString();
                LocationStore locationStore = new LocationStore(lat, lon, place.getText().toString(), description.getText().toString(), feature);

                if (!place.getText().toString().equals("") && !description.getText().toString().equals("")){
                    databaseReference.child("location").push().setValue(locationStore);
                    dialog.dismiss();
                } else {
                    Toast.makeText(getApplicationContext(), "Please fill the form.", Toast.LENGTH_LONG).show();
                }
            }
        });
        dialog.show();
    }

    private void showInfo(final String s) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.info_box);

        Query clickedItem = databaseReference.child("location").orderByChild("name").equalTo(s);
        clickedItem.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                    LocationStore locationStore = itemSnapshot.getValue(LocationStore.class);
                    dialog.setTitle(locationStore.getName());
                    TextView locationText = (TextView) dialog.findViewById(R.id.locationText);
                    TextView descriptionText = (TextView) dialog.findViewById(R.id.descriptionText);
                    locationText.setText(locationStore.getLatitude() +" "+ locationStore.getLongitude());
                    descriptionText.setText(locationStore.getDescription());
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        Button delete = (Button) dialog.findViewById(R.id.delete);
        Button close = (Button) dialog.findViewById(R.id.close);

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                builder.setTitle("Alert");
                builder.setMessage("Are you sure you want to delete?");
                builder.setCancelable(false);
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {
                        Query findItem = databaseReference.child("location").orderByChild("name").equalTo(s);
                        findItem.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot deleteSnapshot : dataSnapshot.getChildren()) {
                                    deleteSnapshot.getRef().removeValue();
                                }
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {}
                        });
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
                dialog.dismiss();
            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

//    public static Bitmap createCustomMarker(Context context, @DrawableRes int resource) {
//
//        View marker = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
//                .inflate(R.layout.custom_marker, null);
//
//        CircleImageView markerImage = (CircleImageView) marker.findViewById(R.id.user_dp);
//        markerImage.setImageResource(resource);
//
//        DisplayMetrics displayMetrics = new DisplayMetrics();
//        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
//        marker.setLayoutParams(new ViewGroup.LayoutParams(52, ViewGroup.LayoutParams.WRAP_CONTENT));
//        marker.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
//        marker.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
//        marker.buildDrawingCache();
//        Bitmap bitmap = Bitmap.createBitmap(marker.getMeasuredWidth(), marker.getMeasuredHeight(),
//                Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(bitmap);
//        marker.draw(canvas);
//
//        return bitmap;
//    }

    public void createMarker(String uri) {
        Context context = getApplicationContext();
        LayoutInflater mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final RelativeLayout view = new RelativeLayout(context);
        mInflater.inflate(R.layout.custom_marker, view, true);
        ImageView markerImage = (ImageView) view.findViewById(R.id.user_dp);
        markerImage.setImageResource(R.drawable.cafe);
//        Callback callback = new Callback() {
//            @Override
//            public void onSuccess() {
        view.setLayoutParams(new ViewGroup.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT));
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(),
                view.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bitmap);
        view.draw(c);
        Log.d("abc", "createMarker: " + bitmap);
        mMap.addMarker(new MarkerOptions().position(customMarker).title("me")
                .icon(BitmapDescriptorFactory.fromBitmap(bitmap)));
//
//            }
//
//            @Override
//            public void onError() {
//                Log.d("abc", "onError: " + "asldfjalskdfj");
//            }
//        };
//        Log.d("abc", "createMarker: ");
//        Picasso.with(context).load(uri).fit().centerCrop().into(markerImage, callback);

//        View marker = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
//                .inflate(R.layout.custom_marker, null);
//        ImageView markerImage = (ImageView) marker.findViewById(R.id.user_dp);
//        Picasso.with(context).load(uri).fit().centerCrop().into(markerImage);

//        DisplayMetrics displayMetrics = new DisplayMetrics();
//        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
//        view.setLayoutParams(new ViewGroup.LayoutParams(52, ViewGroup.LayoutParams.WRAP_CONTENT));
//        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
//        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
//        view.buildDrawingCache();
//        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(),
//                Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(bitmap);
//        view.draw(canvas);


    }

//    public Bitmap createBitmap(final Context context){
//        final View marker = ((LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_marker, null);
//        storageReference.child("marker_photo").child("mPhoto").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//            @Override
//            public void onSuccess(Uri uri) {
//                ImageView markerImage = (ImageView) marker.findViewById(R.id.user_dp);
//                Picasso.with(context).load(uri).fit().centerCrop().into(markerImage);
//
//                DisplayMetrics displayMetrics = new DisplayMetrics();
//                ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
//                marker.setLayoutParams(new ViewGroup.LayoutParams(52, ViewGroup.LayoutParams.WRAP_CONTENT));
//                marker.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
//                marker.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
//                marker.buildDrawingCache();
//                Bitmap bitmap = Bitmap.createBitmap(marker.getMeasuredWidth(), marker.getMeasuredHeight(),
//                        Bitmap.Config.ARGB_8888);
//                Canvas canvas = new Canvas(bitmap);
//                marker.draw(canvas);
//            }
//        });
//
//        return bitmap;
//    }
}
