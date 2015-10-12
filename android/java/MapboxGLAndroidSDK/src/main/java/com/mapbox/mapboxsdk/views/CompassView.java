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
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.mapbox.mapboxsdk.R;

import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.Calendar;
import java.util.Date;

final class CompassView extends ImageView implements SensorEventListener {

    private static final String TAG = "CompassView";
    private CompassDelegate mCompassDelegate;

    // Sensor model
    private SensorManager mSensorManager;
    private Sensor mSensorRotationVector;

    // Sensor data sensor rotation vector
    private float[] mRotationMatrix = new float[16];
    private float[] mRemappedMatrix = new float[16];
    private float[] mOrientation = new float[3];

    // Location data
    private GeomagneticField mGeomagneticField;
    private Location mGpsLocation;

    // Controls the sensor update rate in milliseconds
    private static final int UPDATE_RATE_MS = 500;

    // Compass data
    private float mCompassBearing;
    private long mCompassUpdateNextTimestamp = 0;
    private AngleLowpassFilter mLowpassFilter;

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

        // View configuration
        setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.compass));
        setContentDescription(getResources().getString(R.string.compassContentDescription));

        // Layout params
        float mScreenDensity = context.getResources().getDisplayMetrics().density;
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams((int) (48 * mScreenDensity), (int) (48 * mScreenDensity));
        setLayoutParams(lp);

        // Low pass filter
        mLowpassFilter = new AngleLowpassFilter();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        setVisibility(enabled ? View.VISIBLE : View.INVISIBLE);
    }

    public void registerListeners(CompassDelegate compassDelegate) {
        mCompassDelegate = compassDelegate;
        mSensorManager.registerListener(this, mSensorRotationVector, UPDATE_RATE_MS * 1000);
    }

    public void unRegisterListeners() {
        mCompassDelegate = null;
        mSensorManager.unregisterListener(this, mSensorRotationVector);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (mCompassDelegate != null) {
            switch (event.sensor.getType()) {
                case Sensor.TYPE_ROTATION_VECTOR:
                    SensorManager.getRotationMatrixFromVector(mRotationMatrix, event.values);
                    break;
            }

//            Display display = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
//            int rot = display.getRotation();
//            switch (rot) {
//                case Surface.ROTATION_0:
                    // Portrait
                    SensorManager.getOrientation(mRotationMatrix, mOrientation);
//                    break;
//                default:
//                    // Landscape
//                    SensorManager.remapCoordinateSystem(mRotationMatrix, SensorManager.AXIS_MINUS_Y, SensorManager.AXIS_MINUS_X, mRemappedMatrix);
//                    SensorManager.getOrientation(mRemappedMatrix, mOrientation);
//                    break;
//            }

            mLowpassFilter.add(mOrientation[0]);

            long currentTime = System.currentTimeMillis();
            if (currentTime < mCompassUpdateNextTimestamp) {
                return;
            }
            mCompassUpdateNextTimestamp = currentTime + UPDATE_RATE_MS;
            mGpsLocation = mCompassDelegate.getLocation();
            mGeomagneticField = new GeomagneticField(
                    (float) mGpsLocation.getLatitude(),
                    (float) mGpsLocation.getLongitude(),
                    (float) mGpsLocation.getAltitude(),
                    currentTime);
            mCompassBearing = (float) Math.toDegrees(mLowpassFilter.average()) + mGeomagneticField.getDeclination();
//            mCompassBearing = (mCompassBearing % 360.0f + 360.0f) % 360.0f;
            mCompassDelegate.onCompassValueChanged(mCompassBearing);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO: ignore unreliable stuff
    }

    public interface CompassDelegate {

        Location getLocation();

        void onCompassValueChanged(float bearing);

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

    public class AngleLowpassFilter {

        private final int LENGTH = 3;

        private float sumSin, sumCos;

        private ArrayDeque<Float> queue = new ArrayDeque<>();

        public void add(float radians) {
            sumSin += (float) Math.sin(radians);
            sumCos += (float) Math.cos(radians);
            queue.add(radians);

            if (queue.size() > LENGTH) {
                float old = queue.poll();
                sumSin -= Math.sin(old);
                sumCos -= Math.cos(old);
            }
        }

        public float average() {
            int size = queue.size();
            return (float) Math.atan2(sumSin / size, sumCos / size);
        }
    }
}
