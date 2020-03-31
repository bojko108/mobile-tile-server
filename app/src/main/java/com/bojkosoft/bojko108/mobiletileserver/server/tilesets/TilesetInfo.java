package com.bojkosoft.bojko108.mobiletileserver.server.tilesets;

import com.bojkosoft.bojko108.mobiletileserver.utils.HelperClass;

import java.io.File;

/**
 * This class provides access to various parameters for the tileset stored in
 * a MBTiles Database {@link MBTilesDatabase} or a directory with map tiles files.
 *
 * <b>Provided parameters from the tileset include: </b>
 * <ul>
 * <li><b>NAME</b> - tileset name - for MBTiles file it gives the name of the file,
 * for tiles directory it gives the name of the directory</li>
 * <li><b>DESCRIPTION</b> - tileset description</li>
 * <li><b>VERSION</b> - tileset version: <i>1.1, 1.2, 1.3...</i></li>
 * <li><b>MIN_ZOOM</b> - the lowest zoom level for which the tileset provides data</li>
 * <li><b>MAX_ZOOM</b> - the highest zoom level for which the tileset provides data</li>
 * <li><b>BOUNDS</b> - tileset extent in geographic coordinates</li>
 * <li><b>CENTER</b> - tileset center in geographic coordinates</li>
 * </ul>
 * <p/>
 */
public class TilesetInfo {
    /**
     * Use this key to get/set tileset name
     */
    public static final String TILESET_NAME = "tilesetname";
    /**
     * Use this key to get/set tileset name parameter from MBTiles Database
     */
    public static final String NAME = "name";
    /**
     * Use this key to get/set tileset description parameter from MBTiles Database
     */
    public static final String DESCRIPTION = "description";
    /**
     * Use this key to get/set tileset version parameter from MBTiles Database: <i>1.1, 1.2, 1.3...</i>
     */
    public static final String VERSION = "version";
    /**
     * Use this key to get/set tileset min zoom parameter
     * Specifies the lowest zoom level for which the tileset provides data.
     */
    public static final String MIN_ZOOM = "minzoom";
    /**
     * Use this key to get/set tileset max zoom parameter.
     * Specifies the highest zoom level for which the tileset provides data.
     */
    public static final String MAX_ZOOM = "maxzoom";
    /**
     * Use this key to set tileset extent in geographic coordinates:
     * <b>longitude_min, latitude_min, longitude_max, latitude_max</b>.
     * When this parameter is set, the value of {@link TilesetInfo#getCenter()}
     * is also calculated. To get the value of bounds parameter use {@link TilesetInfo#getBounds()}
     */
    public static final String BOUNDS = "bounds";

    private String tilesetName = "";
    private String name = "";
    private String description = "";
    private String version = "";
    private int minZoom = 999;
    private int maxZoom = -1;
    private double[] bounds;
    private double[] center;

    public TilesetInfo() {
        this(null);
    }

    public TilesetInfo(File directory) {
        /**
         * Default values are set to the extent of Bulgaria!
         */
        this.bounds = new double[4];
        this.bounds[0] = 22;
        this.bounds[1] = 40.5;
        this.bounds[2] = 28;
        this.bounds[3] = 45;
        this.center = this.calculateCenter();

        if (directory != null) {
            this.tilesetName = directory.getName();

            // calculate min and max zoom for this tileset
            // from the subdirectories with zoom levels
            File[] dirs = HelperClass.getDirectoriesFrom(directory);
            for (File dir : dirs) {
                int zoomLevel = Integer.parseInt(dir.getName());
                maxZoom = Math.max(maxZoom, zoomLevel);
                minZoom = Math.min(minZoom, zoomLevel);
            }
        }
    }

    /**
     * Get a parameter from the tileset.
     *
     * @param name of the parameter to return: {@link TilesetInfo#NAME},
     *             {@link TilesetInfo#DESCRIPTION}, {@link TilesetInfo#VERSION},
     *             {@link TilesetInfo#MIN_ZOOM} or {@link TilesetInfo#MAX_ZOOM}.
     * @return parameter value
     */
    public <T> T getParameter(String name, Class<T> type) {
        switch (name) {
            case TILESET_NAME:
                return type.cast(this.tilesetName);
            case NAME:
                return type.cast(this.name);
            case DESCRIPTION:
                return type.cast(this.description);
            case VERSION:
                return type.cast(this.version);
            case MIN_ZOOM:
                return type.cast(this.minZoom);
            case MAX_ZOOM:
                return type.cast(this.maxZoom);
        }

        return null;
    }

    /**
     * Set a parameter for the tileset.
     *
     * @param name  of the parameter to set: {@link TilesetInfo#NAME},
     *              {@link TilesetInfo#DESCRIPTION}, {@link TilesetInfo#VERSION},
     *              {@link TilesetInfo#MIN_ZOOM}, {@link TilesetInfo#MAX_ZOOM} or
     *              {@link TilesetInfo#BOUNDS}.
     * @param value to set for the parameter
     */
    public void setParameter(String name, String value) {
        switch (name) {
            case TILESET_NAME:
                this.tilesetName = value;
                break;
            case NAME:
                this.name = value;
                break;
            case DESCRIPTION:
                this.description = value;
                break;
            case VERSION:
                this.version = value;
                break;
            case MIN_ZOOM:
                this.minZoom = Integer.parseInt(value);
                break;
            case MAX_ZOOM:
                this.maxZoom = Integer.parseInt(value);
                break;
            case BOUNDS:
                this.bounds = this.readBounds(value);
                this.center = this.calculateCenter();
                break;
        }
    }

    /**
     * Use this to get tileset extent
     *
     * @return tileset extent - <b>longitude_min, latitude_min, longitude_max, latitude_max</b>
     */
    public double[] getBounds() {
        return this.bounds;
    }

    /**
     * Gets the tileset extent as text
     *
     * @return extent coordinates, separated with ","
     */
    public String getBoundsAsString() {
        return HelperClass.arrayToString(this.getBounds(), ",");
    }

    /**
     * Use this to get tileset center coordinates
     *
     * @return tileset center coordinates - <b>longitude, latitude, initial_zoom_level</b>
     */
    public double[] getCenter() {
        return this.center;
    }

    /**
     * Gets the center coordinates as text
     *
     * @return center coordinates, separated with ","
     */
    public String getCenterAsString() {
        return HelperClass.arrayToString(this.getCenter(), ",");
    }

    /**
     * Reads tileset extent from a String and converts it in geographic coordinates.
     *
     * @param boundsString tileset extent value as text -
     * @return tileset extent value as geographic coordinates
     */
    private double[] readBounds(String boundsString) {
        double[] result = new double[4];

        String[] values = boundsString.split(",");
        for (int i = 0; i < values.length; i++) {
            result[i] = Double.parseDouble(values[i]);
        }

        return result;
    }

    /**
     * Calculates tileset center coordinates from their extent.
     *
     * @return tileset center in geographic coordinates
     */
    private double[] calculateCenter() {
        double[] result = new double[3];

        if (this.bounds.length == 4) {
            result[0] = (this.bounds[0] + this.bounds[2]) / 2; // longitude
            result[1] = (this.bounds[1] + this.bounds[3]) / 2; // latitude
            result[2] = HelperClass.getZoomLevelFromTileWidth(this.bounds[2] - this.bounds[0]);
        }

        return result;
    }
}
