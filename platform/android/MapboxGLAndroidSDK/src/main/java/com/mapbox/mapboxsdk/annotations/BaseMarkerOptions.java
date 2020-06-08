package com.mapbox.mapboxsdk.annotations;

import android.os.Parcelable;

import com.mapbox.mapboxsdk.geometry.LatLng;

/**
 * Abstract builder class for composing custom Marker objects.
 * <p/>
 * Extending this class requires implementing Parceable interface.
 *
 * @param <U> Type of the marker to be composed
 * @param <T> Type of the builder to be used for composing a custom Marker
 */
public abstract class BaseMarkerOptions<U extends Marker, T extends BaseMarkerOptions<U, T>> implements Parcelable {

    protected LatLng position;
    protected String snippet;
    protected String title;
    protected Icon icon;

    public T position(LatLng position) {
        this.position = position;
        return getThis();
    }

    public T snippet(String snippet) {
        this.snippet = snippet;
        return getThis();
    }

    public T title(String title) {
        this.title = title;
        return getThis();
    }

    public T icon(Icon icon) {
        this.icon = icon;
        return getThis();
    }

    public abstract T getThis();

    public abstract U getMarker();

}