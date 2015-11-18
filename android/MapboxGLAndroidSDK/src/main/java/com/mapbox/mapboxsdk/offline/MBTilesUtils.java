package com.mapbox.mapboxsdk.offline;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by antonio on 11/18/15.
 */
public class MBTilesUtils {

    private static final int BUF_SIZE = 0x1000; // 4K

    private final static String LOG_TAG = "MBTilesUtils";

    /*
     * Because assets are not files on the filesystem on the Android device. They are entries in
     * the ZIP file that is the APK, and they cannot be directly used by SQLite. Instead, they
     * need to be unpacked into a local file first.
     */

    public static String unpackAsset(Context context, String assetName) throws IOException {
        // Open asset as an input stream
        InputStream is = context.getAssets().open(assetName);

        // Destination folder
        String databasePath = context.getApplicationInfo().dataDir + File.separator + MBTilesConstants.MBTILES_PATH;
        Log.d(LOG_TAG, "Database path: " + databasePath);

        // Make sure the folder exists
        File f = new File(databasePath + File.separator);
        if (!f.exists()) {
            Log.d(LOG_TAG, "Folder created.");
            f.mkdir();
        }

        // Destination file
        String destination = databasePath + File.separator + assetName;
        Log.d(LOG_TAG, "Destination: " + destination);

        // Copy the file
        OutputStream os = new FileOutputStream(destination);
        long total = copyStream(is, os);
        Log.d(LOG_TAG, "Total bytes: " + total);

        return destination;
    }

    /*
     * See http://guava-libraries.googlecode.com/svn/tags/release08/javadoc/com/google/common/io/ByteStreams.html
     */

    private static long copyStream(InputStream from, OutputStream to) throws IOException {
        byte[] buf = new byte[BUF_SIZE];
        long total = 0;
        while (true) {
            int r = from.read(buf);
            if (r == -1) {
                break;
            }
            to.write(buf, 0, r);
            total += r;
        }
        return total;
    }

}
