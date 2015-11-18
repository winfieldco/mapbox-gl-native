package com.mapbox.mapboxsdk.offline;

/**
 * Shared constants
 */
public class MBTilesConstants {

    /*
     * Data path
     */

    public final static String MBTILES_PATH = "mbtiles";

    /*
     * Metadata table
     */

    public final static String TABLE_METADATA = "metadata";
    public final static String METADATA_COLUMN_NAME = "name";
    public final static String METADATA_COLUMN_VALUE = "value";

    /*
     * Tiles table
     */

    public final static String TABLE_TILES = "tiles";
    public final static String METADATA_COLUMN_ZOOM_LEVEL = "zoom_level";
    public final static String METADATA_COLUMN_TILE_COLUMN = "tile_column";
    public final static String METADATA_COLUMN_TILE_ROW = "tile_row";
    public final static String METADATA_COLUMN_TILE_DATA = "tile_data";

}
