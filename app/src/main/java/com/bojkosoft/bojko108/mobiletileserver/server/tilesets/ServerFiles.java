package com.bojkosoft.bojko108.mobiletileserver.server.tilesets;

import android.content.Context;

import com.bojkosoft.bojko108.mobiletileserver.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class ServerFiles {

    /**
     * This value is stored in a template string and can be used
     * to fill the attribution value of the map layer
     */
    private static final String ATTRIBUTION = "{{attribution}}";
    /**
     * This value is stored in a template string and can be used
     * to fill the URL Address for the tileset
     */
    private static final String URL = "{{url}}";
    /**
     * This value is stored in a template string and can be used
     * to fill the latitude of the map's center
     */
    private static final String LATITUDE = "{{latitude}}";
    /**
     * This value is stored in a template string and can be used
     * to fill the longitude of the map's center
     */
    private static final String LONGITUDE = "{{longitude}}";
    /**
     * This value is stored in a template string and can be used
     * to fill the zoom level of the map
     */
    private static final String ZOOM_LEVEL = "{{zoom_level}}";
    /**
     * This value is stored in a template string and can be used
     * to fill the lowest zoom level for which the tileset provides data
     */
    private static final String MIN_ZOOM = "{{min_zoom}}";
    /**
     * This value is stored in a template string and can be used
     * to fill the highest zoom level for which the tileset provides data
     */
    private static final String MAX_ZOOM = "{{max_zoom}}";
    /**
     * This value is stored in a template string and can be used
     * to fill the quadkey value
     */
    public static final String QUADKEY = "{quadkey}";
    /**
     * This value is stored in a template string and can be used
     * to set the title for the web page
     */
    private static final String TITLE = "{{title}}";
    /**
     * This value is stored in a template string and can be used
     * to fill the header text for the web page
     */
    private static final String HEADER = "{{header}}";
    /**
     * This value is stored in a template string and can be used
     * to fill the details text
     */
    private static final String DETAILS = "{{details}}";
    /**
     * This value is stored in a template string and can be used
     * to fill the content of the web page - list of available services
     */
    private static final String SERVICES = "{{services}}";
    /**
     * This value is stored in a template string and can be used
     * to fill the tileset name - MBTiles file name or Tile directory name
     */
    private static final String TILESET_NAME = "{{tileset_name}}";
    /**
     * This value is stored in a template string and can be used
     * to fill the name of the tileset - MBTiles file or Tile directory
     */
    private static final String NAME = "{{name}}";
    /**
     * This value is stored in a template string and can be used
     * to fill the version of the MBTiles tileset - {@link TilesetInfo#VERSION}
     */
    private static final String VERSION = "{{version}}";
    /**
     * This value is stored in a template string and can be used
     * to fill the description of the MBTiles tileset - {@link TilesetInfo#DESCRIPTION}
     */
    private static final String DESCRIPTION = "{{description}}";
    /**
     * This value is stored in a template string and can be used
     * to fill the bounds of the MBTiles tileset - {@link TilesetInfo#BOUNDS}
     */
    private static final String BOUNDS = "{{bounds}}";
    /**
     * This value is stored in a template string and can be used
     * to fill the center of the MBTiles tileset - {@link TilesetInfo#getCenter()}
     */
    private static final String CENTER = "{{center}}";
    /**
     * This value is stored in a template string and can be used
     * to fill the XML text for use in Oruxmaps App
     */
    private static final String ORUXMAPS = "{{oruxmaps}}";

    private static final String MBTILES_URL = "%s/mbtiles?tileset=%s&z={z}&x={x}&y={y}";
    private static final String TILES_URL = "%s/tiles/%s/{z}/{x}/{y}.png";

    private enum TilesetType {
        MBTiles,
        DirectoryTiles
    }

    /**
     * Returns the page for previewing a specific MBTiles tileset. This page uses <a href="https://leafletjs.com">Leaflet</a>
     * for creating a map and displaying a basemap with source set to the provided tileset parameter.
     * <p>
     * The template HTML text is stored in <i>res/raw/preview.html</i> file. This method simply
     * replaces the template strings with values from provided tileset info parameter.
     * <p>
     * Template strings, stored in the template file include:
     * <ul>
     * <li>{@link ServerFiles#LATITUDE}</li>
     * <li>{@link ServerFiles#LONGITUDE}</li>
     * <li>{@link ServerFiles#ZOOM_LEVEL}</li>
     * <li>{@link ServerFiles#MIN_ZOOM}</li>
     * <li>{@link ServerFiles#MAX_ZOOM}</li>
     * <li>{@link ServerFiles#ATTRIBUTION}</li>
     * <li>{@link ServerFiles#URL}</li>
     * </ul>
     *
     * @param context   app context
     * @param serverUrl server URL address
     * @param info      tileset info
     * @return web page
     */
    public static String getPreviewHtmlForMBTiles(Context context, String serverUrl, TilesetInfo info) {
        return ServerFiles.getPreviewHtmlFor(context, serverUrl, info, TilesetType.MBTiles);
    }

    /**
     * Returns the page for previewing a specific tileset stored in tiles directory.
     * This page uses <a href="https://leafletjs.com">Leaflet</a> for creating a map
     * and displaying a basemap with source set to the provided tileset parameter.
     * <p>
     * The template HTML text is stored in <i>res/raw/preview.html</i> file. This method simply
     * replaces the template strings with values from provided tileset info parameter.
     * <p>
     * Template strings, stored in the template file include:
     * <ul>
     * <li>{@link ServerFiles#LATITUDE}</li>
     * <li>{@link ServerFiles#LONGITUDE}</li>
     * <li>{@link ServerFiles#ZOOM_LEVEL}</li>
     * <li>{@link ServerFiles#MIN_ZOOM}</li>
     * <li>{@link ServerFiles#MAX_ZOOM}</li>
     * <li>{@link ServerFiles#ATTRIBUTION}</li>
     * <li>{@link ServerFiles#URL}</li>
     * </ul>
     *
     * @param context   app context
     * @param serverUrl server URL address
     * @param info      tileset info
     * @return web page
     */
    public static String getPreviewHtmlForDirectoryTiles(Context context, String serverUrl, TilesetInfo info) {
        return ServerFiles.getPreviewHtmlFor(context, serverUrl, info, TilesetType.DirectoryTiles);
    }

    /**
     * Returns the page for list of available MBTiles tilesets.
     * <p>
     * The template HTML text is stored in <i>res/raw/services.html</i> file. This method simply
     * replaces the template strings with values from provided list of tilesets parameter.
     * <p>
     * Template strings, stored in the template file include:
     * <ul>
     * <li>{@link ServerFiles#TITLE}</li>
     * <li>{@link ServerFiles#HEADER}</li>
     * <li>{@link ServerFiles#DETAILS}</li>
     * <li>{@link ServerFiles#SERVICES}</li> template text for MBTiles tileset is used
     * </ul>
     * <p>
     * Template strings, stored in the template file for MBTiles tileset include:
     * <ul>
     * <li>{@link ServerFiles#URL}</li>
     * <li>{@link ServerFiles#NAME}</li>
     * <li>{@link ServerFiles#VERSION}</li>
     * <li>{@link ServerFiles#DESCRIPTION}</li>
     * <li>{@link ServerFiles#MIN_ZOOM}</li>
     * <li>{@link ServerFiles#MAX_ZOOM}</li>
     * <li>{@link ServerFiles#BOUNDS}</li>
     * <li>{@link ServerFiles#CENTER}</li>
     * </ul>
     *
     * @param context   app context
     * @param serverUrl server URL address
     * @param tilesets  a list of tilesets
     * @return web page as text
     */
    public static String getPageForAvailableMBTiles(Context context, String serverUrl, List<TilesetInfo> tilesets) {
        return ServerFiles.getPageForAvailableTilesets(context, serverUrl, tilesets, TilesetType.MBTiles);
    }

    /**
     * Returns the page for list of available directories with tiles.
     * <p>
     * The template HTML text is stored in <i>res/raw/services.html</i> file. This method simply
     * replaces the template strings with values from provided list of tilesets parameter.
     * <p>
     * Template strings, stored in the template file include:
     * <ul>
     * <li>{@link ServerFiles#TITLE}</li>
     * <li>{@link ServerFiles#HEADER}</li>
     * <li>{@link ServerFiles#DETAILS}</li>
     * <li>{@link ServerFiles#SERVICES}</li> template text for a tileset is used
     * </ul>
     * <p>
     * Template strings, stored in the template file for a tileset include:
     * <ul>
     * <li>{@link ServerFiles#URL}</li>
     * <li>{@link ServerFiles#NAME}</li>
     * <li>{@link ServerFiles#MIN_ZOOM}</li>
     * <li>{@link ServerFiles#MAX_ZOOM}</li>
     * <li>{@link ServerFiles#CENTER}</li>
     * </ul>
     *
     * @param context   app context
     * @param serverUrl server URL address
     * @param tilesets  a list of tilesets
     * @return web page as text
     */
    public static String getPageForAvailableDirectoryTiles(Context context, String serverUrl, List<TilesetInfo> tilesets) {
        return ServerFiles.getPageForAvailableTilesets(context, serverUrl, tilesets, TilesetType.DirectoryTiles);
    }

    /**
     * Gets a reference to a resource file stored in <i>res/raw</i> directory.
     *
     * @param context app context
     * @param id      of theresource to return
     * @return resource with provided ID
     */
    private static InputStream getRawResource(Context context, int id) {
        return context.getResources().openRawResource(id);
    }

    /**
     * Reads the contents of the provided {@link InputStream} and returns it as {@link String}
     *
     * @param stream to read from
     * @return stream contents as text
     * @throws IOException exception
     */
    private static String readToEnd(InputStream stream) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(stream));
        StringBuilder total = new StringBuilder();
        total.append("");
        for (String line; (line = r.readLine()) != null; ) {
            total.append(line).append('\n');
        }
        return total.toString();
    }

    /**
     * Returns text in XML format for configuring a new online map within OruxMaps App.
     * <p>
     * The template XML text is stored in <i>raw/resources/oruxmaps.txt</i> file. This method simply
     * replaces the template strings with values from provided tileset info parameter.
     * <p>
     * Template strings, stored in the template file include:
     * <ul>
     * <li>{@link ServerFiles#URL}</li>
     * <li>{@link ServerFiles#NAME}</li>
     * <li>{@link ServerFiles#MIN_ZOOM}</li>
     * <li>{@link ServerFiles#MAX_ZOOM}</li>
     * </ul>
     *
     * @param context app context
     * @param info    tileset info
     * @param url     server URL address
     * @return configuration in XML format
     */
    private static String formatTemplateForOruxmapsApp(Context context, TilesetInfo info, String url) {
        try {
            if (info != null) {
                return readToEnd(getRawResource(context, R.raw.oruxmaps))
                        .replace(ServerFiles.URL, url)
                        .replace(ServerFiles.NAME, info.getParameter(TilesetInfo.NAME, String.class))
                        .replace(ServerFiles.MIN_ZOOM, String.valueOf(info.getParameter(TilesetInfo.MIN_ZOOM, Integer.class)))
                        .replace(ServerFiles.MAX_ZOOM, String.valueOf(info.getParameter(TilesetInfo.MAX_ZOOM, Integer.class)));
            } else {
                return "";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    /**
     * Returns the page for previewing a specific tileset stored in tiles directory.
     *
     * @param context     app context
     * @param serverUrl   server URL address
     * @param info        tileset info
     * @param tilesetType MBTiles or Directory Tiles
     * @return web page
     */
    private static String getPreviewHtmlFor(Context context, String serverUrl, TilesetInfo info, TilesetType tilesetType) {
        try {
            String templateUrl = "";
            switch (tilesetType) {
                case MBTiles:
                    templateUrl = ServerFiles.MBTILES_URL;
                    break;
                case DirectoryTiles:
                    templateUrl = ServerFiles.TILES_URL;
                    break;
            }

            InputStream stream = getRawResource(context, R.raw.preview);
            double[] center = info.getCenter();
            return readToEnd(stream)
                    .replace(ServerFiles.LATITUDE, String.valueOf(center[1]))
                    .replace(ServerFiles.LONGITUDE, String.valueOf(center[0]))
                    .replace(ServerFiles.ZOOM_LEVEL, String.valueOf(center[2]))
                    .replace(ServerFiles.MIN_ZOOM, String.valueOf(info.getParameter(TilesetInfo.MIN_ZOOM, Integer.class)))
                    .replace(ServerFiles.MAX_ZOOM, String.valueOf(info.getParameter(TilesetInfo.MAX_ZOOM, Integer.class)))
                    .replace(ServerFiles.ATTRIBUTION, info.getParameter(TilesetInfo.NAME, String.class))
                    .replace(ServerFiles.URL, String.format(Locale.getDefault(),
                            templateUrl,
                            serverUrl,
                            info.getParameter(TilesetInfo.TILESET_NAME, String.class)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    /**
     * Returns the page for listing available tilesets.
     *
     * @param context     app context
     * @param serverUrl   server URL address
     * @param tilesets    list of tilesets
     * @param tilesetType type of tileset
     * @return web page
     */
    private static String getPageForAvailableTilesets(Context context, String serverUrl, List<TilesetInfo> tilesets, TilesetType tilesetType) {
        try {
            List<String> services = new ArrayList<>();
            String result = readToEnd(getRawResource(context, R.raw.services));
            String itemTemplate = "";
            String templateUrl = "";
            String title = "";
            String details = "";

            switch (tilesetType) {
                case MBTiles:
                    itemTemplate = readToEnd(getRawResource(context, R.raw.mbtilesserviceinfo));
                    templateUrl = ServerFiles.MBTILES_URL;
                    title = context.getString(R.string.web_page_title_mbtiles);
                    details = context.getString(R.string.web_page_details_mbtiles);
                    break;
                case DirectoryTiles:
                    itemTemplate = readToEnd(getRawResource(context, R.raw.tileserviceinfo));
                    templateUrl = ServerFiles.TILES_URL;
                    title = context.getString(R.string.web_page_title_tiles);
                    details = context.getString(R.string.web_page_details_tiles);
                    break;
            }

            if (tilesets != null) {
                for (TilesetInfo info : tilesets) {
                    String url = String.format(Locale.getDefault(),
                            templateUrl,
                            serverUrl,
                            info.getParameter(TilesetInfo.NAME, String.class));
                    String oruxmapsText = ServerFiles.formatTemplateForOruxmapsApp(context, info, url);
                    String bounds = info.getBoundsAsString();
                    String center = info.getCenterAsString();
                    String text = itemTemplate
                            .replace(ServerFiles.TILESET_NAME, info.getParameter(TilesetInfo.TILESET_NAME, String.class))
                            .replace(ServerFiles.NAME, info.getParameter(TilesetInfo.NAME, String.class))
                            .replace(ServerFiles.VERSION, info.getParameter(TilesetInfo.VERSION, String.class))
                            .replace(ServerFiles.DESCRIPTION, info.getParameter(TilesetInfo.DESCRIPTION, String.class))
                            .replace(ServerFiles.MIN_ZOOM, String.valueOf(info.getParameter(TilesetInfo.MIN_ZOOM, Integer.class)))
                            .replace(ServerFiles.MAX_ZOOM, String.valueOf(info.getParameter(TilesetInfo.MAX_ZOOM, Integer.class)))
                            .replace(ServerFiles.BOUNDS, bounds)
                            .replace(ServerFiles.CENTER, center)
                            .replace(ServerFiles.ORUXMAPS, oruxmapsText)
                            .replace(ServerFiles.URL, url);

                    services.add(text);
                }
            }

            return result
                    .replace(ServerFiles.TITLE, title)
                    .replace(ServerFiles.HEADER, title)
                    .replace(ServerFiles.DETAILS, details)
                    .replace(ServerFiles.SERVICES, services.stream().collect(Collectors.joining()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }
}
