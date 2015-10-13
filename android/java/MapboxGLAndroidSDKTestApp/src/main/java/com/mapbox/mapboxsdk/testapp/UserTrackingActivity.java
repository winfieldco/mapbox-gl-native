package com.mapbox.mapboxsdk.testapp;

import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.views.MapView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;

public class UserTrackingActivity extends AppCompatActivity implements MapView.OnLocationChangedListener, AdapterView.OnItemSelectedListener {

    private static final String TAG = "UserTracking";
    private static final String KEY_STATE_SPINNER = "Spinner";

    private MapView mMapView;
    private Spinner mSpinner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_tracking);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        mMapView = (MapView) findViewById(R.id.mapView);
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(getResources().openRawResource(R.raw.token)));
            String line = reader.readLine();
            mMapView.setAccessToken(line);
        } catch (IOException e) {
            Log.e(TAG, "Error loading access token from token.txt: " + e.toString());
        }

        mMapView.onCreate(savedInstanceState);
        mMapView.setStyleUrl(MapView.StyleUrls.EMERALD);
        mMapView.setZoomLevel(16);
        mMapView.setMyLocationEnabled(true);

        mSpinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(actionBar.getThemedContext(), R.array.user_tracking_mode, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(spinnerAdapter);
        mSpinner.setOnItemSelectedListener(this);
        mSpinner.setEnabled(false);

        if(savedInstanceState==null){
           mMapView.setOnLocationChangedListener(this);
        }else{
            mSpinner.setEnabled(true);
            mSpinner.setSelection(savedInstanceState.getInt(KEY_STATE_SPINNER));
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        // show new location on map
        mMapView.setCenterCoordinate(new LatLng(location.getLatitude(), location.getLongitude()));
        mMapView.setOnLocationChangedListener(null);
        mSpinner.setEnabled(true);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case 0:
                mMapView.setUserLocationTrackingMode(MapView.TRACKING_NONE);
                break;

            case 1:
                mMapView.setUserLocationTrackingMode(MapView.TRACKING_FOLLOW);
                break;

            case 2:
                mMapView.setUserLocationTrackingMode(MapView.TRACKING_FOLLOW_BEARING_GPS);
                break;

            case 3:
                mMapView.setUserLocationTrackingMode(MapView.TRACKING_FOLLOW_BEARING_COMPASS);
                break;

            case 4:
                mMapView.setUserLocationTrackingMode(MapView.TRACKING_FOLLOW_BEARING);
                break;

        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause()  {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_STATE_SPINNER, mSpinner.getSelectedItemPosition());
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

