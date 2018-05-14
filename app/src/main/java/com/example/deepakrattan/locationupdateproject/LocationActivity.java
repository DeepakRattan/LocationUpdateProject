package com.example.deepakrattan.locationupdateproject;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

public class LocationActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private double latitude, longitude;
    public static final String TAG = "map";
    public static final int RequestPermissionCode = 999;
    public static final String PRE_MARSHMALLOW = "PreMarshMallow";
    public static final String MARSHMALLOW = "Marshmallow";
    private LocationManager locationManager;
    private static final long LOCATION_INTERVAL = 10000;
    private static final long FASTEST_INTERVAL = 1000 * 5;
    private static final float LOCATION_DISTANCE = 100f;
    LocationRequest locationRequest;
    private LocationCallback mLocationCallback;

    GoogleApiClient googleApiClient;
    Location currentLocation;
    private FusedLocationProviderClient mFusedLocationClient;


    protected void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(LOCATION_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Check the platform
        String platform = checkPlatform();
        if (platform.equals("Marshmallow")) {
            Log.d(TAG, "Runtime Permission required");
            //check the permission
            boolean permissionStatus = checkPermission();
            if (permissionStatus) {
                //Permission already granted
                Log.d(TAG, "Permission already granted");
            } else {
                //Permission not granted
                //Show an explanation
                Log.d(TAG, "explain permission");
                explainPermission();
                //Request Permission
                Log.d(TAG, "Request Permission");
                requestPermission();

            }
        } else {
            Log.d(TAG, "onClick: Runtime permission not required");

        }

        if (!isGooglePlayServicesAvailable()) {
            finish();
        }

        createLocationRequest();
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .build();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(LocationActivity.this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            moveMap();

                        } else {
                            Log.d(TAG, "onSuccess: location is null");
                        }
                    }
                });

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    moveMap();

                }
            }
        };


       /* LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);*/

    }


    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(-34, 151);
      /*  LatLng sydney = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        //Animating the camera
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));*/
        moveMap();

    }


    public boolean checkPermission() {
        int FineLocationPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);
        int CoarseLocationPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION);
        return FineLocationPermissionResult == PackageManager.PERMISSION_GRANTED && CoarseLocationPermissionResult == PackageManager.PERMISSION_GRANTED;

    }

    private void requestPermission() {

        ActivityCompat.requestPermissions(LocationActivity.this, new String[]
                {
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                }, RequestPermissionCode);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {


        }
    }

    //Check the platfrom
    public String checkPlatform() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return MARSHMALLOW;

        } else {
            return PRE_MARSHMALLOW;
        }
    }

    //Explain Permission required
    public void explainPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(LocationActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            Log.d(TAG, "explainPermission:Access Coarse Location  Permission required ");
            Toast.makeText(LocationActivity.this, "Access Coarse Location Permission Required", Toast.LENGTH_SHORT).show();
        }
        if (ActivityCompat.shouldShowRequestPermissionRationale(LocationActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            Log.d(TAG, "explainPermission:Access Fine Location Permission Required ");
            Toast.makeText(LocationActivity.this, "Access Fine Location Permission required", Toast.LENGTH_SHORT).show();
        }
    }


    private boolean isGooglePlayServicesAvailable() {

        int status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(LocationActivity.this);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            Toast.makeText(LocationActivity.this, "Google Play Services not available", Toast.LENGTH_SHORT).show();
            return false;
        }
    }







    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
        startLocationUpdates();
    }

    @Override
    protected void onStop() {
        super.onStop();
        googleApiClient.disconnect();
    }

    public void moveMap() {
        mMap.clear();

        LatLng latLng = new LatLng(latitude, longitude);

        //Adding marker
        mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title("Current Location").draggable(true));

        //Moving the camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

        //Animating the camera
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
    }

    private void startLocationUpdates() {


        boolean permissionStatus = checkPermission();
        if (permissionStatus) {
            //Permission already granted
            Log.d(TAG, "Permission already granted");
        } else {
            //Permission not granted
            //Show an explanation
            Log.d(TAG, "explain permission");
            explainPermission();
            //Request Permission
            Log.d(TAG, "Request Permission");
            requestPermission();
        }
        mFusedLocationClient.requestLocationUpdates(locationRequest, mLocationCallback, null);
    }


}




