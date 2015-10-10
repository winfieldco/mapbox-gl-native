package com.mapbox.mapboxsdk.views;

import android.content.Context;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.mapbox.mapboxsdk.R;

import java.lang.ref.WeakReference;
import java.util.Date;

final class CompassView extends ImageView implements SensorEventListener {

    private CompassDelegate mCompassDelegate;

    // Sensor model
    private SensorManager mSensorManager;
    private Sensor mSensorRotationVector;
    private Sensor mSensorAccelerometer;
    private Sensor mSensorMagneticField;

    // Sensor data sensor rotation vector
    private float[] mRotationMatrix = new float[9];
    private float[] mOrientation = new float[3];

    // Sensor data accelero + magnetic
//    private boolean mSensorValid;
//    private float[] mValuesAccelerometer = new float[3];
//    private float[] mValuesMagneticField = new float[3];
//    private float[] mMatrixR = new float[9];
//    private float[] mMatrixI = new float[9];
//    private float[] mMatrixValues = new float[3];

    // Location data
    private GeomagneticField mGeomagneticField;
    private Location mGpsLocation;

    // Controls the sensor update rate in milliseconds
    private static final int UPDATE_RATE_MS = 250;

    // Compass data
    private float mCompassBearing;
    private boolean mCompassValid;
    private long mCompassUpdateNextTimestamp = 0;

    public CompassView(Context context) {
        super(context);
        initialize(context);
    }

    public CompassView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public CompassView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }

    private void initialize(Context context) {
        // Sensor initialisation
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mSensorRotationVector = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
//        mSensorAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
//        mSensorMagneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        // View configuration
        setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.compass));
        setContentDescription(getResources().getString(R.string.compassContentDescription));

        // Layout params
        float mScreenDensity = context.getResources().getDisplayMetrics().density;
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams((int) (48 * mScreenDensity), (int) (48 * mScreenDensity));
        setLayoutParams(lp);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        setVisibility(enabled ? View.VISIBLE : View.INVISIBLE);
    }

    public boolean isValid() {
        return mCompassValid;
    }

    public float getBearing() {
        return mCompassBearing;
    }

    public void registerListeners(CompassDelegate compassDelegate) {
        // Rate limit to UPDATE_RATE_US, events can still be delivered faster
//        mSensorManager.registerListener(this, mSensorAccelerometer, UPDATE_RATE_MS * 1000);
//        mSensorManager.registerListener(this, mSensorMagneticField, UPDATE_RATE_MS * 1000);
        mSensorManager.registerListener(this, mSensorRotationVector, UPDATE_RATE_MS * 1000);
        mCompassDelegate = compassDelegate;
    }

    public void unRegisterListeners() {
        mCompassDelegate = null;
        mSensorManager.unregisterListener(this, mSensorRotationVector);
//        mSensorManager.unregisterListener(this, mSensorMagneticField);
//        mSensorManager.unregisterListener(this, mSensorAccelerometer);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
//        long currentTime = System.currentTimeMillis();
//        if (currentTime < mCompassUpdateNextTimestamp) {
//            return;
//        }
        if (mCompassDelegate != null) {

            switch (event.sensor.getType()) {
                case Sensor.TYPE_ROTATION_VECTOR:
                    SensorManager.getRotationMatrixFromVector(mRotationMatrix, event.values);
                    SensorManager.remapCoordinateSystem(mRotationMatrix,
                            SensorManager.AXIS_X, SensorManager.AXIS_Y,
                            mRotationMatrix);
                    SensorManager.getOrientation(mRotationMatrix, mOrientation);
                    break;
            }
//
//            case Sensor.TYPE_ACCELEROMETER:
//                System.arraycopy(event.values, 0, mValuesAccelerometer, 0, 3);
//                break;
//
//            case Sensor.TYPE_MAGNETIC_FIELD:
//                System.arraycopy(event.values, 0, mValuesMagneticField, 0, 3);
//                break;
//        }
//
//        if (currentTime < mCompassUpdateNextTimestamp) {
//            return;
//        }
////
//        mCompassUpdateNextTimestamp = currentTime + UPDATE_RATE_MS;
//
//        mSensorValid = SensorManager.getRotationMatrix(mMatrixR, mMatrixI,
//                mValuesAccelerometer,
//                mValuesMagneticField);
//
//        if (mSensorValid && mCompassDelegate != null) {
//            SensorManager.getOrientation(mMatrixR, mMatrixValues);

            mGpsLocation = mCompassDelegate.getLocation();
            if (mGpsLocation != null) {
                mGeomagneticField = new GeomagneticField(
                        (float) mGpsLocation.getLatitude(),
                        (float) mGpsLocation.getLongitude(),
                        (float) mGpsLocation.getAltitude(),
                        new Date().getTime());
//                mCompassBearing = (float) Math.toDegrees(mMatrixValues[0]) + mGeomagneticField.getDeclination();
//                mCompassValid = true;
//                mCompassDelegate.setBearing(mCompassBearing);
                android.text.format.DateFormat df = new android.text.format.DateFormat();
                ;
                Log.v("TAG","TIME" + df.format("yyyy-MM-dd kk:mm:ss", new java.util.Date()));
                Log.v("TAG","Orientation " + mOrientation[0]);
                mCompassDelegate.setBearing((float) Math.toDegrees(mOrientation[0]));

            }
//        }
        }
    }
//

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO: ignore unreliable stuff
    }

    public interface CompassDelegate {

        Location getLocation();

        void setBearing(float bearing);

    }

    public static class CompassClickListener implements View.OnClickListener {

        private WeakReference<MapView> mMapView;

        public CompassClickListener(final MapView mapView) {
            mMapView = new WeakReference<>(mapView);
        }

        @Override
        public void onClick(View v) {
            final MapView mapView = mMapView.get();
            if (mapView != null) {
                mapView.resetNorth();
            }
        }
    }
}
