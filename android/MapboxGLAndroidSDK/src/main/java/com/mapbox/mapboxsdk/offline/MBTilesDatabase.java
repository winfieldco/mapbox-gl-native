package com.mapbox.mapboxsdk.offline;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.File;

/**
 * Created by antonio on 11/17/15.
 */
public class MBTilesDatabase {

    private final static String LOG_TAG = "MBTilesDatabase";

    private SQLiteDatabase database;

    /*
     * If the `name` database doesn't exist, it'll be created.
     */

    public MBTilesDatabase(Context context, String name) {
        File f = new File(name);
        if (f.exists()) {
            // Existing file
            database = SQLiteDatabase.openDatabase(name, null, SQLiteDatabase.OPEN_READONLY);
            return;
        }

        // New file
        MBTilesOpenHelper helper = new MBTilesOpenHelper(context, name);
        database = helper.getReadableDatabase();
    }

    public void close() {
        database.close();
    }

    public String getMetada(String name) {
        // Prepare the query
        String table = MBTilesConstants.TABLE_METADATA;
        String[] columns = { MBTilesConstants.METADATA_COLUMN_VALUE };
        String selection = String.format("%s = ?", MBTilesConstants.METADATA_COLUMN_NAME);
        String[] selectionArgs = { name };

        // Params groupBy, having, and orderBy are null
        Cursor cursor = database.query(table, columns, selection, selectionArgs, null, null, null);

        try {
            cursor.moveToFirst();
            return cursor.getString(0);
        } catch (Exception e) {
            // Probably the name doesn't exist
            Log.d(LOG_TAG, "getMetada error: " + e);
            return null;
        } finally {
            cursor.close();
        }
    }

    public byte[] getTileData(int zoomLevel, int tileColumn, int tileRow) {
        // Prepare the query
        String table = MBTilesConstants.TABLE_TILES;
        String[] columns = { MBTilesConstants.METADATA_COLUMN_TILE_DATA };
        String selection = String.format("%s = ? AND %s = ? AND %s = ?",
                MBTilesConstants.METADATA_COLUMN_ZOOM_LEVEL,
                MBTilesConstants.METADATA_COLUMN_TILE_COLUMN,
                MBTilesConstants.METADATA_COLUMN_TILE_ROW);
        String[] selectionArgs = {
                String.valueOf(zoomLevel),
                String.valueOf(tileColumn),
                String.valueOf(tileRow) };

        // Params groupBy, having, and orderBy are null
        Cursor cursor = database.query(table, columns, selection, selectionArgs, null, null, null);

        try {
            cursor.moveToFirst();
            return cursor.getBlob(0);
        } catch (Exception e) {
            // Probably the name doesn't exist
            Log.d(LOG_TAG, "getTile error: " + e);
            return null;
        } finally {
            cursor.close();
        }
    }

}
