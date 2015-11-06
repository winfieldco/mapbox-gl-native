package com.mapbox.mapboxsdk.geometry;

/**
 * Implementation of iOS MKCoordinateSpan
 */
public class CoordinateSpan { // TODO implement parcelable?

    private double latitudeSpan;
    private double longitudeSpan;

    public CoordinateSpan(final double latitudeSpan, final double longitudeSpan) {
        this.latitudeSpan = latitudeSpan;
        this.longitudeSpan = longitudeSpan;
    }

    public double getLatitudeSpan() {
        return latitudeSpan;
    }

    public double getLongitudeSpan() {
        return longitudeSpan;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CoordinateSpan that = (CoordinateSpan) o;

        if (Double.compare(that.latitudeSpan, latitudeSpan) != 0) return false;
        return Double.compare(that.longitudeSpan, longitudeSpan) == 0;

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(latitudeSpan);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(longitudeSpan);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
