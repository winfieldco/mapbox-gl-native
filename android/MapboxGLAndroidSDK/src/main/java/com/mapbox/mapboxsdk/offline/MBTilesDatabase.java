package com.mapbox.mapboxsdk.offline;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;

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
        // Readonly by default
        this(context, name, true);
    }

    public MBTilesDatabase(Context context, String name, boolean readonly) {
        File f = new File(name);
        if (f.exists()) {
            // Open exisiting
            if (readonly) {
                database = SQLiteDatabase.openDatabase(name, null, SQLiteDatabase.OPEN_READONLY);
            } else {
                database = SQLiteDatabase.openDatabase(name, null, SQLiteDatabase.OPEN_READWRITE);
            }
        } else {
            // Create a new one with the helper
            MBTilesOpenHelper helper = new MBTilesOpenHelper(context, name);
            if (readonly) {
                database = helper.getReadableDatabase();
            } else {
                database = helper.getWritableDatabase();
            }
        }
    }

    public void close() {
        database.close();
    }

    public ArrayList<String> getMetadataKeys() {
        // Prepare the query
        String table = MBTilesConstants.TABLE_METADATA;
        String[] columns = { MBTilesConstants.METADATA_COLUMN_NAME };

        // Params selection, selectionArgs, groupBy, having, and orderBy are null
        Cursor cursor = database.query(table, columns, null, null, null, null, null);
        ArrayList<String> keys = new ArrayList<>();
        while(cursor.moveToNext()) {
            keys.add(cursor.getString(0));
        }

        // Done
        cursor.close();
        return keys;
    }

    public String getMetadata(String name) {
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
            Log.d(LOG_TAG, "getMetadata error: " + e);
            return null;
        } finally {
            cursor.close();
        }
    }

    public boolean setMetadata(String name, String value) {
        boolean success;

        // Common to both update/insert
        String table = MBTilesConstants.TABLE_METADATA;
        ContentValues values = new ContentValues();
        values.put(MBTilesConstants.METADATA_COLUMN_NAME, name);
        values.put(MBTilesConstants.METADATA_COLUMN_VALUE, value);

        ArrayList<String> keys = getMetadataKeys();
        if (keys.contains(name)) {
            // Update
            Log.d(LOG_TAG, "Updating...");
            String whereClause = String.format("%s = ?", MBTilesConstants.METADATA_COLUMN_NAME);
            String[] whereArgs = { name };
            int result = database.update(table, values, whereClause, whereArgs);
            success = (result == 1);
        } else {
            // Insert (nullColumnHack is null)
            Log.d(LOG_TAG, "Inserting...");
            long result = database.insert(table, null, values);
            success = (result > 0);
        }

        return success;
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
