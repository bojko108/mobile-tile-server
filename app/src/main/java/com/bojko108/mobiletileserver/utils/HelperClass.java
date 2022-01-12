package com.bojko108.mobiletileserver.utils;

import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.annotation.Nullable;

public class HelperClass {
    private static final String TAG = HelperClass.class.getName();

    /**
     * Calculates the quadkey from tile coordinates.
     *
     * @param z coordinate of a tile
     * @param x coordinate of a tile
     * @param y coordinate of a tile
     * @return Quadkey
     * @see <a href='https://docs.microsoft.com/en-us/bingmaps/articles/bing-maps-tile-system#tile-coordinates-and-quadkeys'>Bing Maps Tile System</a>
     */
    public static String getQuadkeyFromXYZ(int z, int x, int y) {
        List<Integer> quadKey = new ArrayList<>();

        for (int i = z; i > 0; i--) {
            int digit = 0;
            int mask = 1 << (i - 1);
            if ((x & mask) != 0) {
                digit++;
            }
            if ((y & mask) != 0) {
                digit++;
                digit++;
            }
            quadKey.add(digit);
        }

        return TextUtils.join("", quadKey);
    }

    /**
     * Joins the elements of an array to a string
     *
     * @param array     values to join in a string
     * @param separator string to divide the elements, default is ','
     * @return array elements joined in a string with provided separator
     */
    public static String arrayToString(double[] array, @Nullable String separator) {
        if (separator == null) {
            separator = ",";
        }
        StringBuilder sb = new StringBuilder();
        for (double d : array) {
            sb.append(d).append(separator);
        }

        String result = sb.toString();
        if (result.endsWith(separator)) {
            result = result.substring(0, result.length() - 1);
        }

        return result;
    }

    /**
     * Calculate approximate zoom level from tile size in decimal degrees. You can calculate
     * the size of the tile with {@link HelperClass#getTileCoordinates(int z, int x, int y)}.
     *
     * @param tileWidth tile width in decimal degrees
     * @return zoom level
     * @see <a href="https://wiki.openstreetmap.org/wiki/Zoom_levels">https://wiki.openstreetmap.org/wiki/Zoom_levels</a>
     */
    public static double getZoomLevelFromTileWidth(double tileWidth) {
        if (270 < tileWidth && tileWidth <= 360)
            return 0; // 360deg	whole world
        if (135 < tileWidth && tileWidth <= 270)
            return 1; // 180deg
        if (67.5 < tileWidth && tileWidth <= 135)
            return 2; // 90deg subcontinental area
        if (33.75 < tileWidth && tileWidth <= 67.5)
            return 3; // 45deg largest country
        if (16.88 < tileWidth && tileWidth <= 33.75)
            return 4; // 22.5deg
        if (8.44 < tileWidth && tileWidth <= 16.88)
            return 5; // 11.25deg large African country
        if (4.22 < tileWidth && tileWidth <= 8.44)
            return 6; // 5.625deg large European country
        if (2.11 < tileWidth && tileWidth <= 4.22)
            return 7; // 2.813deg small country, US state
        if (1.05 < tileWidth && tileWidth <= 2.11)
            return 8; // 1.406deg
        if (0.53 < tileWidth && tileWidth <= 1.05)
            return 9; // 0.703deg wide area, large metropolitan area
        if (0.26 < tileWidth && tileWidth <= 0.53)
            return 10; // 0.352deg metropolitan area
        if (0.13 < tileWidth && tileWidth <= 0.26)
            return 11; // 0.176deg city
        if (0.066 < tileWidth && tileWidth <= 0.13)
            return 12; // 0.088deg town, or city district
        if (0.033 < tileWidth && tileWidth <= 0.066)
            return 13; // 0.044deg village, or suburb
        if (0.017 < tileWidth && tileWidth <= 0.033)
            return 14; // 0.022deg
        if (0.008 < tileWidth && tileWidth <= 0.017)
            return 15; // 0.011deg small road
        if (0.004 < tileWidth && tileWidth <= 0.008)
            return 16; // 0.005deg street
        if (0.002 < tileWidth && tileWidth <= 0.004)
            return 17; // 0.003deg block, park, addresses
        if (0.00075 < tileWidth && tileWidth <= 0.002)
            return 18; // 0.001deg some buildings, trees
        if (0.00038 < tileWidth && tileWidth <= 0.00075)
            return 19; // 0.0005deg local highway and crossing details
        //if (deltaLongitude <= 0.00038)
        return 20; // 0.00025deg A mid -sized building
    }

    /**
     * Get geographic coordinates from tile coordinates (upper-left corner?)
     *
     * @param z zoom level
     * @param x coordinate of a tile
     * @param y coordinate of a tile
     * @return geographic coordinates of a tile (upper-left corner?)
     */
    private static double[] getTileCoordinates(int z, int x, int y) {
        double n = Math.pow(2, z);
        double latitude = Math.atan(Math.sinh(Math.PI * (1 - 2 * y / n))) * 180 / Math.PI;
        double longitude = x / n * 360 - 180;
        return new double[]{latitude, longitude};
    }

    /**
     * Gets all subdirectories from a directory
     *
     * @param directory from which to return subdirectories
     * @return list of subdirectories
     */
    public static File[] getDirectoriesFrom(File directory) {
        return directory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory();
            }
        });
    }

    /**
     * Gets all files from a directory. Optionally, you can filter the files based
     * on their extension.
     *
     * @param directory      from which to return files
     * @param validExtension files to return, default is <i>null</i> - all files will be returned
     * @return list of files, filtered by <i>validExtension</i>
     */
    public static File[] getFilesFrom(File directory, @Nullable final String validExtension) {
        return directory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                if (validExtension == null) {
                    return file.isFile();
                } else {
                    return file.getName().endsWith(validExtension);
                }
            }
        });
    }

    /**
     * Returns the file from the specified path.
     *
     * @param path Full file path
     * @return File
     */
    public static File getFileFromPath(String path) {
        File file = new File(path);
        if (file.exists() && !file.isDirectory()) {
            return file;
        }
        return null;
    }

    /**
     * Returns the MIME type for specified File. Default result is "application/octet-stream"
     *
     * @param file to process
     * @return MIME type
     */
    public static String getContentTypeForFile(File file) {
        String extension = file != null
                ? MimeTypeMap.getFileExtensionFromUrl(file.getAbsolutePath())
                : "";

//        if (extension == null || extension.equals("")) {
//            return "application/octet-stream";
//        }

        String contentType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        return contentType != null
                ? contentType
                : "application/octet-stream";
    }

    /**
     * Creates a directory of a file at the specified absolute path
     *
     * @param absolutePath path to a directory or a file
     */
    public static void createDirectory(String absolutePath) {
        try {
            File directory = new File(absolutePath);
            if (!directory.exists()) {
                Files.createDirectories(directory.toPath());
            }
        } catch (IOException e) {
            Log.i(TAG, "createDirectory: unable to create directory: " + absolutePath);
            e.printStackTrace();
        }
    }

    public static String formatException(Exception ex) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%s\n", ex.toString()));
        for (StackTraceElement ste : ex.getStackTrace()) {
            sb.append(String.format(Locale.getDefault(), "   at %s(%s:%d)\n", ste.getMethodName(), ste.getClassName(), ste.getLineNumber()));
        }
        return sb.toString();
    }
}
