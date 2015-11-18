package com.mapbox.mapboxsdk.offline;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * A helper class to manage database creation and version management.
 */
public class MBTilesOpenHelper extends SQLiteOpenHelper {

    private final static String LOG_TAG = "MBTilesOpenHelper";

    private final static String SCHEMA_METADATA =
            String.format("CREATE TABLE %s (%s text, %s text);",
                    MBTilesConstants.TABLE_METADATA,
                    MBTilesConstants.METADATA_COLUMN_NAME,
                    MBTilesConstants.METADATA_COLUMN_VALUE);

    private final static String SCHEMA_TILES =
            String.format("CREATE TABLE %s (%s integer, %s integer, %s integer, %s blob);",
                    MBTilesConstants.TABLE_TILES,
                    MBTilesConstants.METADATA_COLUMN_ZOOM_LEVEL,
                    MBTilesConstants.METADATA_COLUMN_TILE_COLUMN,
                    MBTilesConstants.METADATA_COLUMN_TILE_ROW,
                    MBTilesConstants.METADATA_COLUMN_TILE_DATA);

    // Spec 1.0 is 1, 1.1 is 2, and 1.2 is 3
    private final static int MBTILES_VERSION = 2;

    /*
     * Constructors
     */

    public MBTilesOpenHelper(Context context) {
        super(context, null, null, MBTILES_VERSION);
        Log.d(LOG_TAG, "MBTilesOpenHelper: in-memory database.");
    }

    public MBTilesOpenHelper(Context context, String name) {
        super(context, name, null, MBTILES_VERSION);
        Log.d(LOG_TAG, "MBTilesOpenHelper: " + name);
    }

    public MBTilesOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory, MBTILES_VERSION);
        Log.d(LOG_TAG, "MBTilesOpenHelper: " + name);
    }

    public MBTilesOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                             DatabaseErrorHandler errorHandler) {
        super(context, name, factory, MBTILES_VERSION, errorHandler);
        Log.d(LOG_TAG, "MBTilesOpenHelper: " + name);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(LOG_TAG, "onCreate");
        db.execSQL(SCHEMA_METADATA);
        db.execSQL(SCHEMA_TILES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(LOG_TAG, "onUpgrade");
        throw new UnsupportedOperationException("TODO: MBTiles upgrade is not supported.");
    }
}
