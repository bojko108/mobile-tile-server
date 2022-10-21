package com.bojko108.mobiletileserver.server;

import android.content.Context;
import android.content.res.Resources;

import com.bojko108.mobiletileserver.R;
import com.bojko108.mobiletileserver.server.tilesets.StaticFileInfo;
import com.bojko108.mobiletileserver.server.tilesets.TilesetInfo;
import com.bojko108.mobiletileserver.utils.HelperClass;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class ServerFiles {
    /**
     * This image can be returned when a map tile is not found
     */
    public static byte[] NO_TILE_IMAGE;

    public enum TilesetType {
        MBTiles,
        DirectoryTiles
    }

    /**
     * Used to access resource files like returned web pages
     */
    private static Resources APP_RESOURCES;
    /**
     * Sets the home address of the server
     */
    private static String SERVER_HOME_ADDRESS;
    /**
     * This value is stored in a template string and can be used
     * to fill the error message for Bad Request or Internal Server Error pages
     */
    private static final String ERROR_MESSAGE = "{{error_message}}";
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
    private static final String QUADKEY = "{quadkey}";
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
     * to fill the content of the web page - list of available static files
     */
    private static final String STATIC_FILES = "{{static_files}}";
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
     * to fill the content type of the file
     */
    private static final String CONTENT_TYPE = "{{content_type}}";
    /**
     * This value is stored in a template string and can be used
     * to fill the size of the file - in KB, MB...
     */
    private static final String FILE_SIZE = "{{file_size}}";
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

    /**
     * This is the URL template for loading a MBTiles tileset in a mapping application
     */
    private static final String MBTILES_URL = "%s/mbtiles?tileset=%s&z={z}&x={x}&y={y}";
    /**
     * This is the URL template for loading a directory tileset in a mapping application
     */
    private static final String TILES_URL = "%s/tiles/%s/{z}/{x}/{y}.png";
    /**
     * This is the URL template for downloading a static file
     */
    private static final String STATIC_FILE_URL = "%s/static?filename=%s";

    public static void setServerUrlAddress(String homeAddress) {
        SERVER_HOME_ADDRESS = homeAddress;
    }

    public static String getUrlAddressFor(TilesetInfo info, TilesetType tilesetType) {
        switch (tilesetType) {
            case MBTiles:
                return String.format(Locale.getDefault(),
                        MBTILES_URL,
                        SERVER_HOME_ADDRESS,
                        info.getParameter(TilesetInfo.TILESET_NAME, String.class));
            case DirectoryTiles:
                return String.format(Locale.getDefault(),
                        TILES_URL,
                        SERVER_HOME_ADDRESS,
                        info.getParameter(TilesetInfo.TILESET_NAME, String.class));
            default:
                return "";
        }
    }


    /**
     * Us this to set the application context, used for accessing resource files
     * and strings.
     *
     * @param context  application context - use {@link Context#getApplicationContext()}
     * @param rootPath sets the root directory for the server, where all map tiles are located
     */
    public static void setAppResources(Context context, String rootPath) {
        ServerFiles.APP_RESOURCES = context.getResources();

        /*
         * Read this from resource files
         */
        ServerFiles.NO_TILE_IMAGE = ServerFiles.getNoTileFile(rootPath);
    }

    public static String getHomePageHtml() {
        try {
            return ServerFiles.readToEnd(APP_RESOURCES.openRawResource(R.raw.home));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getInternalServerErrorPage(String errorMessage) {
        return ServerFiles.getErrorPage(errorMessage, R.raw.internalservererror);
    }

    public static String getBadRequestPage(String errorMessage) {
        return ServerFiles.getErrorPage(errorMessage, R.raw.badrequest);
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
     * @param info tileset info
     * @return web page
     */
    public static String getMBTilesPreviewPageHtmlFor(TilesetInfo info) {
        return ServerFiles.getPreviewHtmlFor(info, TilesetType.MBTiles);
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
     * @param info tileset info
     * @return web page
     */
    public static String getDirectoryTilesPreviewPageHtmlFor(TilesetInfo info) {
        return ServerFiles.getPreviewHtmlFor(info, TilesetType.DirectoryTiles);
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
     * @param tilesets a list of tilesets
     * @return web page as text
     */
    public static String getAvailableMBTilesPageHtmlFor(List<TilesetInfo> tilesets) {
        return ServerFiles.getPageForAvailableTilesets(tilesets, TilesetType.MBTiles);
    }

    public static String getAvailableStaticFilesPageHtmlFor(List<StaticFileInfo> staticFiles) {
        return ServerFiles.getPageForAvailableStaticFiles(staticFiles);
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
     * @param tilesets a list of tilesets
     * @return web page as text
     */
    public static String getAvailableDirectoryTilesPageHtmlFor(List<TilesetInfo> tilesets) {
        return ServerFiles.getPageForAvailableTilesets(tilesets, TilesetType.DirectoryTiles);
    }

    public static String getErrorPage(String errorMessage, int id) {
        try {
            return ServerFiles.readToEnd(ServerFiles.getRawResource(id))
                    .replace(ERROR_MESSAGE, errorMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
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
     * @param info tileset info
     * @param url  tileset template URL address
     * @return configuration in XML format
     */
    private static String formatTemplateForOruxmapsApp(TilesetInfo info, String url) {
        try {
            if (info != null) {
                return ServerFiles.readToEnd(ServerFiles.getRawResource(R.raw.oruxmaps))
                        .replace(URL, url)
                        .replace(NAME, info.getParameter(TilesetInfo.NAME, String.class))
                        .replace(MIN_ZOOM, String.valueOf(info.getParameter(TilesetInfo.MIN_ZOOM, Integer.class)))
                        .replace(MAX_ZOOM, String.valueOf(info.getParameter(TilesetInfo.MAX_ZOOM, Integer.class)));
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
     * @param info        tileset info
     * @param tilesetType MBTiles or Directory Tiles
     * @return web page
     */
    private static String getPreviewHtmlFor(TilesetInfo info, TilesetType tilesetType) {
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

            InputStream stream = ServerFiles.getRawResource(R.raw.preview);
            double[] center = info.getCenter();
            return readToEnd(stream)
                    .replace(LATITUDE, String.valueOf(center[1]))
                    .replace(LONGITUDE, String.valueOf(center[0]))
                    .replace(ZOOM_LEVEL, String.valueOf(center[2]))
                    .replace(MIN_ZOOM, String.valueOf(info.getParameter(TilesetInfo.MIN_ZOOM, Integer.class)))
                    .replace(MAX_ZOOM, String.valueOf(info.getParameter(TilesetInfo.MAX_ZOOM, Integer.class)))
                    .replace(ATTRIBUTION, info.getParameter(TilesetInfo.NAME, String.class))
                    .replace(URL, String.format(Locale.getDefault(),
                            templateUrl,
                            SERVER_HOME_ADDRESS,
                            info.getParameter(TilesetInfo.TILESET_NAME, String.class)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    /**
     * Returns the page for listing available tilesets.
     *
     * @param tilesets    list of tilesets
     * @param tilesetType type of tileset
     * @return web page
     */
    private static String getPageForAvailableTilesets(List<TilesetInfo> tilesets, TilesetType tilesetType) {
        try {
            List<String> services = new ArrayList<>();
            String result = ServerFiles.readToEnd(getRawResource(R.raw.services));
            String itemTemplate = "";
            String templateUrl = "";
            String title = "";
            String details = "";

            switch (tilesetType) {
                case MBTiles:
                    itemTemplate = ServerFiles.readToEnd(ServerFiles.getRawResource(R.raw.mbtilesserviceinfo));
                    templateUrl = MBTILES_URL;
                    title = APP_RESOURCES.getString(R.string.web_page_title_mbtiles);
                    details = ServerFiles.APP_RESOURCES.getString(R.string.web_page_details_mbtiles);
                    break;
                case DirectoryTiles:
                    itemTemplate = ServerFiles.readToEnd(ServerFiles.getRawResource(R.raw.tileserviceinfo));
                    templateUrl = TILES_URL;
                    title = APP_RESOURCES.getString(R.string.web_page_title_tiles);
                    details = APP_RESOURCES.getString(R.string.web_page_details_tiles);
                    break;
            }

            if (tilesets != null) {
                for (TilesetInfo info : tilesets) {
                    String url = String.format(Locale.getDefault(),
                            templateUrl,
                            SERVER_HOME_ADDRESS,
                            info.getParameter(TilesetInfo.TILESET_NAME, String.class));
                    String oruxmapsText = ServerFiles.formatTemplateForOruxmapsApp(info, url);
                    String bounds = info.getBoundsAsString();
                    String center = info.getCenterAsString();
                    String text = itemTemplate
                            .replace(TILESET_NAME, info.getParameter(TilesetInfo.TILESET_NAME, String.class))
                            .replace(NAME, info.getParameter(TilesetInfo.NAME, String.class))
                            .replace(VERSION, info.getParameter(TilesetInfo.VERSION, String.class))
                            .replace(DESCRIPTION, info.getParameter(TilesetInfo.DESCRIPTION, String.class))
                            .replace(MIN_ZOOM, String.valueOf(info.getParameter(TilesetInfo.MIN_ZOOM, Integer.class)))
                            .replace(MAX_ZOOM, String.valueOf(info.getParameter(TilesetInfo.MAX_ZOOM, Integer.class)))
                            .replace(BOUNDS, bounds)
                            .replace(CENTER, center)
                            .replace(ORUXMAPS, oruxmapsText)
                            .replace(URL, url);

                    services.add(text);
                }
            }

            return result
                    .replace(TITLE, title)
                    .replace(HEADER, title)
                    .replace(DETAILS, details)
                    .replace(SERVICES, String.join("", services));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    private static String getPageForAvailableStaticFiles(List<StaticFileInfo> staticFiles) {
        try {
            List<String> files = new ArrayList<>();
            String result = ServerFiles.readToEnd(getRawResource(R.raw.staticfiles));
            String itemTemplate = ServerFiles.readToEnd(ServerFiles.getRawResource(R.raw.staticfileinfo));
            String templateUrl = STATIC_FILE_URL;
            String title = APP_RESOURCES.getString(R.string.web_page_title_staticfiles);
            String details = APP_RESOURCES.getString(R.string.web_page_details_staticfiles);

            if (staticFiles != null) {
                for (StaticFileInfo info : staticFiles) {
                    String url = String.format(Locale.getDefault(),
                            templateUrl,
                            SERVER_HOME_ADDRESS,
                            info.getFileName());
                    String text = itemTemplate
                            .replace(NAME, info.getFileName())
                            .replace(CONTENT_TYPE, info.getContentType())
                            .replace(FILE_SIZE, info.getSizeAsText())
                            .replace(URL, url);

                    files.add(text);
                }
            }

            return result
                    .replace(TITLE, title)
                    .replace(HEADER, title)
                    .replace(DETAILS, details)
                    .replace(STATIC_FILES, String.join("", files));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    /**
     * Get no data tile - this image is returned when a tile isn't found by the server
     *
     * @param rootPath sets the root directory for the server, where all map tiles are located
     * @return Image
     */
    private static byte[] getNoTileFile(String rootPath) {
        InputStream stream = APP_RESOURCES.openRawResource(R.raw.no_tile);
        File customNoTileImageFile = HelperClass.getFileFromPath(rootPath + "/no_tile.png");
        if (customNoTileImageFile != null) {
            try {
                stream = new FileInputStream(customNoTileImageFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];

        try {
            while ((nRead = stream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return buffer.toByteArray();
    }


    /**
     * Gets a reference to a resource file stored in <i>res/raw</i> directory.
     *
     * @param id of theresource to return
     * @return resource with provided ID
     */
    private static InputStream getRawResource(int id) {
        return APP_RESOURCES.openRawResource(id);
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
}
