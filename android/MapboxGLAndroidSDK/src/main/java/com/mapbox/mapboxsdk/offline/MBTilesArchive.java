package com.mapbox.mapboxsdk.offline;

import android.content.Context;
import android.util.Log;

import java.io.IOException;

/**
 * An implementation of MBTiles 1.1
 * https://github.com/mapbox/mbtiles-spec/blob/master/1.1/spec.md
 */
public class MBTilesArchive {

    private final static String LOG_TAG = "MBTilesArchive ";

    private final MBTilesDatabase database;

    /*
     * Constructor
     */

    public MBTilesArchive(Context context, String name) throws IOException {
        Log.d(LOG_TAG, "Constructor: " + name);
        database = new MBTilesDatabase(context, name);
    }

    public static MBTilesArchive fromAsset(Context context, String assetName) throws IOException {
        String name = MBTilesUtils.unpackAsset(context, assetName);
        return new MBTilesArchive(context, name);
    }

    public void close() {
        database.close();
    }

    /*
     * Metadata
     */

    public String getMetada(String name) {
        return database.getMetada(name);
    }

    public void setMetada(String name, String value) {
        throw new UnsupportedOperationException("TODO: The API is read-only.");
    }

    /*
     * The plain-english name of the tileset (required by the spec).
     */
    public String getName() {
        return getMetada("name");
    }

    public void setName(String name) {
        setMetada("name", name);
    }

    /*
     * Overlay or baselayer (required by the spec).
     */
    public String getType() {
        return getMetada("type");
    }

    public void setType(String type) {
        setMetada("type", type);
    }

    /*
     * The version of the tileset, as a plain number (required by the spec).
     */
    public String getVersion() {
        return getMetada("version");
    }

    public void setVersion(String version) {
        setMetada("version", version);
    }

    /*
     * A description of the layer as plain text (required by the spec).
     */
    public String getDescription() {
        return getMetada("description");
    }

    public void setDescription(String description) {
        setMetada("description", description);
    }

    /*
     * The image file format of the tile data: png or jpg (required by the spec).
     */
    public String getFormat() {
        return getMetada("format");
    }

    public void setFormat(String format) {
        setMetada("format", format);
    }

    /*
     * The maximum extent of the rendered map area. Bounds must define an area covered by all
     * zoom levels. The bounds are represented in WGS:84 - latitude and longitude values, in the
     * OpenLayers Bounds format - left, bottom, right, top. Example of the full earth:
     * -180.0,-85,180,85 (suggested by the spec).
     */
    public String getBounds() {
        return getMetada("bounds");
    }

    public void SetBounds(String bounds) {
        setMetada("bounds", bounds);
    }

    /*
     * Tiles
     */

    public byte[] getTileData (int zoomLevel, int tileColumn, int tileRow) {
        byte[] tileData = database.getTileData(zoomLevel, tileColumn, tileRow);
        return tileData;
    }

    public void setTileData(int zoomLevel, int tileColumn, int tileRow, String tileData) {
        throw new UnsupportedOperationException("TODO: The API is read-only.");
    }

}
