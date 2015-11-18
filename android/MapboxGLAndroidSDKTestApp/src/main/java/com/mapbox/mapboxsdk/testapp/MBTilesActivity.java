package com.mapbox.mapboxsdk.testapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mapbox.mapboxsdk.offline.MBTilesArchive;

import java.io.IOException;

/**
 * Test MBTiles functionality
 */
public class MBTilesActivity extends AppCompatActivity {

    private static final String TAG = "MBTilesActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        // Load the layout
        setContentView(R.layout.activity_mbtiles);

        // Temp content
        TextView tv = new TextView(this);
        tv.setText(R.string.coming_soon);
        LinearLayout ll = (LinearLayout) findViewById(R.id.mbtiles_container);
        ll.addView(tv);

        // Test
        testMBTiles();
    }

    private void testMBTiles() {
        Log.d(TAG, "testMBTiles");
        new MBTilesAsyncTask().execute();
    }

    class MBTilesAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            MBTilesArchive archive;
            try {
                archive = MBTilesArchive.fromAsset(MBTilesActivity.this, "road-trip.mbtiles");
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

            Log.d(TAG, "Name: " + archive.getName());
            Log.d(TAG, "Type: " + archive.getType());
            Log.d(TAG, "Version: " + archive.getVersion());
            Log.d(TAG, "Description: " + archive.getDescription());
            Log.d(TAG, "Format: " + archive.getFormat());
            Log.d(TAG, "Bounds: " + archive.getBounds());

            int zoomLevel = 4;
            int tileColumn = 2;
            int tileRow = 9;
            byte[] tileData = archive.getTileData (zoomLevel, tileColumn, tileRow);
            Log.d(TAG, "tileData: " + tileData);

            archive.close();
            return null;
        }
    }

}
