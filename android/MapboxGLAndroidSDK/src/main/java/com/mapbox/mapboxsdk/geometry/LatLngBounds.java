package com.mapbox.mapboxsdk.geometry;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.List;

/**
 * A rectangular geographical area defined in latitude and longitude units.
 */
public final class LatLngBounds implements Parcelable {

    private LatLng mSouthWest; // TODO drop the m
    private LatLng mNorthEast;

    /**
     * Construct a new bounding box based on its corners, given in NESW
     * order.
     *
     * @param northLatitude Northern Latitude
     * @param eastLongitude   Eastern Longitude
     * @param southLatitude Southern Latitude
     * @param westLongitude   Western Longitude
     */
    public LatLngBounds(final double northLatitude, final double eastLongitude, final double southLatitude, final double westLongitude) {
        this(new LatLng(northLatitude, eastLongitude), new LatLng(southLatitude, westLongitude));
    }

    /**
     * Construct a new bounding box based on its corners, given in NESW order.
     *
     * @param northEast Coordinate
     * @param southWest Coordinate
     */
    public LatLngBounds(final LatLng northEast, final LatLng southWest) {
        mNorthEast = northEast;
        mSouthWest = southWest;
    }

    /**
     * Create a bounding box from another bounding box
     *
     * @param other the other bounding box
     */
    public LatLngBounds(final LatLngBounds other) {
        mNorthEast = other.getNorthEast();
        mSouthWest = other.getSouthWest();
    }

    /**
     * Create a new LatLngBounds with no size centered at 0, 0, also known as null island
     */
    public LatLngBounds() {
        this(0, 0, 0, 0);
    }

    /**
     * Calculates the centerpoint of this bounding box by simple interpolation and returns
     * it as a point. This is a non-geodesic calculation which is not the geographic center.
     *
     * @return LatLng center of this LatLngBounds
     */
    public LatLng getCenter() {
        return new LatLng((this.mNorthEast.getLatitude() + this.mSouthWest.getLatitude()) / 2.0,
                (this.mNorthEast.getLongitude() + this.mSouthWest.getLongitude()) / 2.0);
    }

    public LatLng getNorthEast() {
        return this.mNorthEast;
    }

    public LatLng getSouthWest() {
        return this.mSouthWest;
    }

    /**
     * Get the area spanned by this bounding box
     *
     * @return CoordinateSpan area
     */
    public CoordinateSpan getSpan() {
        return new CoordinateSpan(getLatitudeSpan(), getLongitudeSpan());
    }

    /**
     * Get the absolute distance, in degrees, between the north and
     * south boundaries of this bounding box
     *
     * @return Span distance
     */
    // TODO make private or remove? (redundant)
    public double getLatitudeSpan() {
        // TODO remove abs?
        return Math.abs(this.mNorthEast.getLatitude() - this.mSouthWest.getLatitude());
    }

    /**
     * Get the absolute distance, in degrees, between the west and
     * east boundaries of this bounding box
     *
     * @return Span distance
     */
    // TODO make private or remove? (redundant)
    public double getLongitudeSpan() {
        // TODO remove abs?
        // TODO handle wrap
        return Math.abs(this.mNorthEast.getLongitude() - this.mSouthWest.getLongitude());
    }


    /**
     * Validate if bounding box is empty, determined if absolute distance is
     *
     * @return boolean indicating if span is empty
     */
    public boolean isEmpty() {
        return getLongitudeSpan() == 0.0 || getLatitudeSpan() == 0.0;
    }

    @Override
    public String toString() {

        return "N: " + this.mNorthEast.getLatitude() +
                "; E: " + this.mNorthEast.getLongitude() +
                "; S: " + this.mSouthWest.getLatitude() +
                "; W: " + this.mSouthWest.getLongitude();
    }

    /**
     * Constructs a bounding box that contains all of a list of LatLng
     * objects. Empty lists will yield invalid bounding boxes.
     *
     * @param latLngs List of LatLng objects
     * @return LatLngBounds
     */
    // TODO replace with LatLngBounds.Builder from Google Maps
    public static LatLngBounds fromLatLngs(final List<? extends LatLng> latLngs) {
        // TODO correct for wrap around
        double minLat = 90,
                minLon = 180,
                maxLat = -90,
                maxLon = -180;

        for (final LatLng gp : latLngs) {
            final double latitude = gp.getLatitude();
            final double longitude = gp.getLongitude();

            minLat = Math.min(minLat, latitude);
            minLon = Math.min(minLon, longitude);
            maxLat = Math.max(maxLat, latitude);
            maxLon = Math.max(maxLon, longitude);
        }

        return new LatLngBounds(maxLat, maxLon, minLat, minLon);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LatLngBounds that = (LatLngBounds) o;

        if (mSouthWest != null ? !mSouthWest.equals(that.mSouthWest) : that.mSouthWest != null)
            return false;
        return !(mNorthEast != null ? !mNorthEast.equals(that.mNorthEast) : that.mNorthEast != null);

    }

    @Override
    public int hashCode() {
        int result = mSouthWest != null ? mSouthWest.hashCode() : 0;
        result = 31 * result + (mNorthEast != null ? mNorthEast.hashCode() : 0);
        return result;
    }

    /**
     * Determines whether this bounding box contains a point and the point
     * does not touch its boundary.
     *
     * @param point the point which may be contained
     * @return true, if the point is contained within the box.
     */
    public boolean contains(final LatLng point) {
        // TODO handle wrap around
        final double latitude = point.getLatitude();
        final double longitude = point.getLongitude();
        return ((latitude < this.mNorthEast.getLatitude())
                && (latitude > this.mSouthWest.getLatitude())
                && (longitude < this.mNorthEast.getLongitude())
                && (longitude > this.mSouthWest.getLongitude()));
    }

    /**
     * Returns a new LatLngBounds that stretches to contain both this and another LatLngBounds.
     *
     * @param bounds LatLngBounds to add
     * @return LatLngBounds
     */
    public LatLngBounds union(LatLngBounds bounds) {
        return union(bounds.getNorthEast().getLatitude(),
                bounds.getNorthEast().getLongitude(),
                bounds.getSouthWest().getLatitude(),
                bounds.getSouthWest().getLongitude());
    }

    /**
     * Returns a new LatLngBounds that stretches to include another bounding box,
     * given by corner points.
     *
     * @param northLatitude Northern Latitude
     * @param eastLongitude  Eastern Longitude
     * @param southLatitude Southern Latitude
     * @param westLongitude  Western Longitude
     * @return LatLngBounds
     */
    // TODO make private or remove? (redundant)
    public LatLngBounds union(final double northLatitude, final double eastLongitude, final double southLatitude, final double westLongitude) {
        // TODO handle wrap
        return new LatLngBounds(
                (this.getNorthEast().getLatitude() < northLatitude) ? northLatitude : this.getNorthEast().getLatitude(),
                (this.getNorthEast().getLongitude() < eastLongitude) ? eastLongitude : this.getNorthEast().getLongitude(),
                (this.getSouthWest().getLatitude() > southLatitude) ? southLatitude : this.getSouthWest().getLatitude(),
                (this.getSouthWest().getLongitude() > westLongitude) ? westLongitude : this.getSouthWest().getLongitude());
    }

    /**
     * Returns a new LatLngBounds that is the intersection of this with another box
     *
     * @param bounds LatLngBounds to intersect with
     * @return LatLngBounds
     */
    public LatLngBounds intersect(LatLngBounds bounds) {
        // TODO handle wrap
        double minLonWest = Math.max(mSouthWest.getLongitude(), bounds.getSouthWest().getLongitude());
        double maxLonEast = Math.min(mNorthEast.getLongitude(), bounds.getNorthEast().getLongitude());
        if (maxLonEast > minLonWest) {
            double minLatSouth = Math.max(mSouthWest.getLatitude(), bounds.getSouthWest().getLatitude());
            double maxLatNorth = Math.min(mNorthEast.getLatitude(), bounds.getNorthEast().getLatitude());
            if (maxLatNorth > minLatSouth) {
                return new LatLngBounds(maxLatNorth, maxLonEast, minLatSouth, minLonWest);
            }
        }
        return null;
    }

    /**
     * Returns a new LatLngBounds that is the intersection of this with another box
     *
     * @param northLatitude Northern Latitude
     * @param eastLongitude   Eastern Longitude
     * @param southLatitude Southern Latitude
     * @param westLongitude   Western Latitude
     * @return LatLngBounds
     */
    // TODO make private or remove? (redundant)
    public LatLngBounds intersect(double northLatitude, double eastLongitude, double southLatitude, double westLongitude) {
        return intersect(new LatLngBounds(northLatitude, eastLongitude, southLatitude, westLongitude));
    }

    public static final Creator<LatLngBounds> CREATOR =
            new Creator<LatLngBounds>() {
                @Override
                public LatLngBounds createFromParcel(final Parcel in) {
                    return readFromParcel(in);
                }

                @Override
                public LatLngBounds[] newArray(final int size) {
                    return new LatLngBounds[size];
                }
            };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel out, final int flags) {
        out.writeParcelable(mNorthEast, flags);
        out.writeParcelable(mSouthWest, flags);
    }

    private static LatLngBounds readFromParcel(final Parcel in) {
        final LatLng northEast = in.readParcelable(LatLng.class.getClassLoader());
        final LatLng southWest = in.readParcelable(LatLng.class.getClassLoader());
        return new LatLngBounds(northEast, southWest);
    }
}
