package com.mapbox.mapboxsdk.testapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

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
    }
}
