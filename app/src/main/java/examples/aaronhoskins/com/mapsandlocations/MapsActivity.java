package examples.aaronhoskins.com.mapsandlocations;

import android.annotation.SuppressLint;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class MapsActivity extends FragmentActivity
        implements OnMapReadyCallback,
                    PermissionsManager.IPermissionManager,
                    GoogleApiClient.ConnectionCallbacks,
                    GoogleApiClient.OnConnectionFailedListener,
                    LocationListener {

    private GoogleMap mMap;
    //Brighter Brain home office latitude longitude
    LatLng bbOfficeLocation = new LatLng(33.909129, -84.478956);

    //Marker Options
    MarkerOptions markerOptionsSydney;
    MarkerOptions markerOptionsBB;

    //Runtime Permissions manager
    PermissionsManager permissionsManager;
    boolean isGranted;

    //Location Request
    LocationRequest locationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //instantiate permissions manager
        permissionsManager = new PermissionsManager(this);
        //request runtime permissions
        permissionsManager.requestPermission();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //check for the permissions
        permissionsManager.checkPermission();
        if(isGranted) {
            //getLastKnownLocation();
            locationChangeFusedSetup();
        }
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
        LatLng sydney = new LatLng(-34, 151);
        markerOptionsSydney = new MarkerOptions().position(sydney).title("Marker in Sydney");
        mMap = googleMap;
        //mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        //mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        //mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        //mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
        // Add a marker in Sydney and move the camera

        mMap.addMarker(markerOptionsSydney);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    public void onClick(View view) {
        mMap.clear(); //remove all markers
        //markerOptionsSydney.visible(false); //set markers so they are not seen

        //create new marker on map
        markerOptionsBB = new MarkerOptions()
                .position(bbOfficeLocation)
                .title("Brighter Brains Office");

        //Add the marker to the map
        mMap.addMarker(markerOptionsBB);

        //move to the latlng
        mMap.moveCamera(CameraUpdateFactory.newLatLng(markerOptionsBB.getPosition()));

        //Set the Zoom level (0 - 20)
        mMap.setMinZoomPreference(19.0f);

    }

    @SuppressLint("MissingPermission")
    public void getLastKnownLocation() {
        FusedLocationProviderClient fusedLocationProviderClient =
                new FusedLocationProviderClient(this);

        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                moveToLocation(location, "Last Known Location");
                Log.d("TAG", "onSuccess: " + location.toString());
            }

        });
    }



    public void moveToLocation(Location location, String locationTag) {
        LatLng locationsLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        //mMap.clear();
        mMap.addMarker(new MarkerOptions().position(locationsLatLng).title(locationTag));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(locationsLatLng));
        Log.d("TAG", "moveToLocation: Address = " + getAddress(location));
        Log.d("TAG", "moveToLocation: LatLng = " + getAddressesLatLng("208 Derek Ct Portland TN"));

    }

    @Override
    public void onPermissionResult(boolean isGranted) {
        this.isGranted = isGranted;
        if(isGranted) {
            getLastKnownLocation();
            Toast
                    .makeText(this, "LOCATIONS PERMISSIONS ARE GRANTED", Toast.LENGTH_LONG)
                    .show();

        } else {
            Toast
                    .makeText(this, "LOCATIONS PERMISSIONS ARE NOT GRANTED", Toast.LENGTH_LONG)
                    .show();
        }
    }

    //GeoCoding
    public String getAddress(Location location) {
        Geocoder geocoder = new Geocoder(this);
        List<Address> addressList = new ArrayList<>();
        try {
            addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(),1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return addressList.size() > 0 ? addressList.get(0).getAddressLine(0) : "";
    }

    //Reverse Geocoding
    public LatLng getAddressesLatLng(String address) {
        Geocoder geocoder = new Geocoder(this);
        List<Address> addressList = new ArrayList<>();

        try {
            addressList = geocoder.getFromLocationName(address, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return addressList.size() > 0 ? new LatLng(addressList.get(0).getLatitude(), addressList.get(0).getLongitude()) : null;
    }






    @SuppressLint("MissingPermission")
    public void locationChangeFusedSetup() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(3000);
        locationRequest.setMaxWaitTime(5000);
        locationRequest.setFastestInterval(4000);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        SettingsClient settingsClient = new SettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        getFusedLocationProviderClient(this).requestLocationUpdates(
                locationRequest,
                new LocationCallback(){
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        super.onLocationResult(locationResult);
                        onLocationChanged(locationResult.getLocations().get(0));
                    }
                },
                Looper.myLooper());

    }

    @Override
    public void onLocationChanged(Location location) {
        moveToLocation(location, location.toString());
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
