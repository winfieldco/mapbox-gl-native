package com.mapbox.mapboxsdk.annotations;

import android.graphics.Bitmap;

public final class Sprite {
    private Bitmap mBitmap;
    private String mId;
    private int mIdNumber;

    Sprite(String id, int idNumber, Bitmap bitmap) {
        mId = id;
        mIdNumber = idNumber;
        mBitmap = bitmap;
    }

    /**
     * Do not use this method. Used internally by the SDK.
     */
    public String getId() {
        return mId;
    }

    /**
     * Do not use this method. Used internally by the SDK.
     */
    public Bitmap getBitmap() {
        return mBitmap;
    }

    /**
     * Do not use this method. Used internally by the SDK.
     */
    public int getIdNumber() {
        return mIdNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Sprite sprite = (Sprite) o;

        if (!mBitmap.equals(sprite.mBitmap)) return false;
        return mId.equals(sprite.mId);

    }

    @Override
    public int hashCode() {
        int result = mBitmap.hashCode();
        result = 31 * result + mId.hashCode();
        return result;
    }
}
