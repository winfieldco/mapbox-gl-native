package com.mapbox.mapboxsdk.geometry;

/**
 * Implementation of iOS MKCoordinateRegion
 */
public class CoordinateRegion { // TODO implement parcelable?
    private LatLng center;
    private CoordinateSpan span;

    public CoordinateRegion(final LatLng center, final CoordinateSpan span) {
        this.center = center;
        this.span = span;
    }

    public LatLng getCenter() {
        return center;
    }

    public CoordinateSpan getSpan() {
        return span;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CoordinateRegion that = (CoordinateRegion) o;

        if (center != null ? !center.equals(that.center) : that.center != null) return false;
        return !(span != null ? !span.equals(that.span) : that.span != null);

    }

    @Override
    public int hashCode() {
        int result = center != null ? center.hashCode() : 0;
        result = 31 * result + (span != null ? span.hashCode() : 0);
        return result;
    }
}
