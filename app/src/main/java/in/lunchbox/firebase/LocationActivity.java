package in.lunchbox.firebase;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

import Model.CustomerInfo;
import Utils.Constants;

public class LocationActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final long MIN_TIME_BW_UPDATES = 5000;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;

    private GoogleMap mMap;

    LocationManager locationManager;
    String provider;
    Location customerLocation = null;
    MarkerOptions markerOptions;
    LatLng latLng;
    Location storeLocation;

    CustomerInfo customerInfo = null;

    GoogleApiClient mGoogleApiClient;
    private PlacesAutoCompleteAdapter placesAutoCompleteAdapter;

    // service area radius
    private static Float SEVEN_KM_IN_METERS = Float.valueOf("7000");

    // Location bounds for places auto complete text view
    private static final LatLngBounds BOUNDS_GREATER_HYDERABAD = new LatLngBounds(
            new LatLng(17.22, 78.14), new LatLng(17.59, 78.66));

    private static final String LOG_TAG = LocationActivity.class.getSimpleName();

    Button mConfirmLocationButton;
    Button mSearchButton;
    AutoCompleteTextView mAddressTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        if (null != getSupportActionBar()){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(), false);

        // initialize google api client for places auto complete on map
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Places.GEO_DATA_API)
                .build();

        mGoogleApiClient.connect();

        // get customer info if called from admin
        Intent intent = getIntent();
        if (null != intent && null != intent.getSerializableExtra("customerInfo")){
            customerInfo = (CustomerInfo) intent.getSerializableExtra("customerInfo");
        }

        // initialize auto complete adapter
        placesAutoCompleteAdapter = new PlacesAutoCompleteAdapter(this, mGoogleApiClient, BOUNDS_GREATER_HYDERABAD, null);

        mSearchButton = (Button) findViewById(R.id.button_Search);
        mAddressTextView = (AutoCompleteTextView) findViewById(R.id.userInputLocation);

        // setup auto complete text view adapter and item click listener
        if (null != mAddressTextView){
            mAddressTextView.setAdapter(placesAutoCompleteAdapter);
            mAddressTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    gotoLocationSelectedInAutoComplete(position);

                }
            });
        }


        mConfirmLocationButton = (Button) findViewById(R.id.confirmLocation);

        // setup address search click listener
        if (null != mSearchButton){
            mSearchButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    searchLocation();
                }
            });
        }

        // setup location confirmation click listener
        mConfirmLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkLocationAndStartProcess();
            }
        });

        //List<Model.Location> storeLocations = CommonUtil.getStoreLocations();
        storeLocation = new Location("");
        storeLocation.setLatitude(Constants.storeLatitude);
        storeLocation.setLongitude(Constants.storeLongitude);
    }

    /**
     * Show location selected in auto complete on the map
     * @param position int
     */
    private void gotoLocationSelectedInAutoComplete(int position){
        // Retrieve the place ID of the selected item from the Adapter.
        final AutocompletePrediction item = placesAutoCompleteAdapter.getItem(position);
        final CharSequence fullText = item.getFullText(null);

        Log.i(LOG_TAG, "Autocomplete item selected: " + fullText);

        mAddressTextView.setText(fullText.toString());
        if (null != mSearchButton){
            mSearchButton.performClick();
        }
    }

    /**
     * Search for the location entered in the text view and show on map
     */
    private void searchLocation(){
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        if (null != getCurrentFocus()){
            inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
        if (null != mAddressTextView){
            String locationAddress = mAddressTextView.getText().toString();
            if(!locationAddress.isEmpty()){
                new GeocoderTask().execute(locationAddress);
            }
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
        mMap = googleMap;

        // set marker on location when long pressed on map
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                customerLocation = new Location("");
                customerLocation.setLatitude(latLng.latitude);
                customerLocation.setLongitude(latLng.longitude);
                updateMarker(latLng);
            }
        });

        // get current location and set marker on map
        customerLocation = getLocation();
        if (null != customerLocation){
            updateMarker(new LatLng(customerLocation.getLatitude(), customerLocation.getLongitude()));
        }
    }

    /**
     * Verify customer selected location is within the service area radius and show menu
     */
    private void checkLocationAndStartProcess() {
        if (null != customerLocation){
            if (customerLocation.distanceTo(storeLocation) > SEVEN_KM_IN_METERS){
                Toast.makeText(getApplicationContext(), "Sorry the location you selected is out of our service area. Please select another location.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "Customer Location Accepted", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), MenuActivity.class);
                intent.putExtra("customerLatitude", Double.toString(customerLocation.getLatitude()));
                intent.putExtra("customerLongitude", Double.toString(customerLocation.getLongitude()));
                if (null != customerInfo){
                    intent.putExtra("customerInfo", customerInfo);
                }
                startActivity(intent);
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        // clear all map markers
        mMap.clear();
        // update customer location
        customerLocation = location;
        updateMarker(new LatLng(customerLocation.getLatitude(), customerLocation.getLongitude()));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.removeUpdates(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(provider, 500, 1, this);
        }
    }

    private void updateMarker(LatLng latLng){
        // Add a marker in the customer location
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(latLng).title("Your Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    /**
     * Async task to get the location on map based on the address entered
     */
    private class GeocoderTask extends AsyncTask<String, Void, List<Address>> {

        @Override
        protected List<Address> doInBackground(String... locationAddress) {
            Geocoder geocoder = new Geocoder(getBaseContext());
            List<Address> addresses = null;
            try {
                addresses = geocoder.getFromLocationName(locationAddress[0], 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return addresses;
        }

        @Override
        protected void onPostExecute(List<Address> addresses) {
            if(addresses == null || addresses.size() == 0){
                Toast.makeText(getBaseContext(), "No Location found", Toast.LENGTH_SHORT).show();
            } else {
                // Clears all the existing markers on the map
                mMap.clear();

                // Adding Markers on Google Map for each matching address
                Address address = addresses.get(0);

                // Creating an instance of GeoPoint, to display in Google Map
                latLng = new LatLng(address.getLatitude(), address.getLongitude());

                String addressText = String.format("%s, %s",
                        address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
                        address.getCountryName());

                markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title(addressText);
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

                mMap.addMarker(markerOptions);

                customerLocation = new Location("");
                customerLocation.setLatitude(latLng.latitude);
                customerLocation.setLongitude(latLng.longitude);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
            }
        }
    }

    /**
     * Get current location of the customer
     * @return Location
     */
    private Location getLocation(){
        Location location = null;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        // get GPS status
        boolean isGPSEnabled = locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);

        // get network status
        boolean isNetworkEnabled = locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!isGPSEnabled && !isNetworkEnabled) {
            // no network provider is enabled
            Toast.makeText(LocationActivity.this, "No Network Connection", Toast.LENGTH_SHORT).show();
        } else {
            if (isNetworkEnabled) {
                locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                Log.d("Network", "Network Enabled");
                if (locationManager != null) {
                    location = locationManager
                            .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }
            }
            // if GPS Enabled get lat/long using GPS Services
            if (isGPSEnabled) {
                if (location == null) {
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    Log.d("GPS", "GPS Enabled");
                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    }
                }
            }
        }
        return location;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                break;
        }
        return true;
    }
}
