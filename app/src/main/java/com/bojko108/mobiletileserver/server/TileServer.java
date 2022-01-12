package com.bojko108.mobiletileserver.server;

import android.content.Context;
import android.os.FileUtils;
import android.util.Log;

import com.bojko108.mobiletileserver.server.tilesets.MBTilesDatabase;
import com.bojko108.mobiletileserver.server.tilesets.StaticFileInfo;
import com.bojko108.mobiletileserver.server.tilesets.TilesetInfo;
import com.bojko108.mobiletileserver.utils.HelperClass;
import com.koushikdutta.async.AsyncServer;
import com.koushikdutta.async.http.Multimap;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.HttpServerRequestCallback;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * This is the main class, responsible for managing GET requests to the tile server. The class
 * uses {@link AsyncHttpServer} as a HTTP Server and registers following HTTP routes:
 * <ul>
 *     <li><i>/</i> - home address, which returns information about how to use this tile server</li>
 *     <li><i>/preview/mbtiles</i> - address on which you can preview a specific MBTiles Tileset</li>
 *     <li><i>/preview/tiles</i> - address on which you can preview a specific Directory Tileset</li>
 *     <li><i>/mbtiles</i> - address on which you can access a specific MBTiles Tileset</li>
 *     <li><i>/tiles</i> - address on which you can access a specific Directory Tileset</li>
 *     <li><i>/availabletilesets</i> - address on which you can get all available tilesets in JSON format</li>
 *     <li><i>/static</i> - address on which you can access static files</li>
 *     <li><i>/liststaticfiles</i> - lists all static files served by the server</li>
 * </ul>
 * <p>
 * Mobile Tile Server, Copyright (c) 2020 by bojko108
 * <p/>
 */
class TileServer {
    private static final String TAG = TileServer.class.getName();

    /**
     * Represents the home url address of the tile server
     */
    private static final String URL_HOME = "http://localhost:%d";
    /**
     * Represents the address of the server's home page
     */
    private static final String URL_HOME_PAGE = "/";
    /**
     * Represents the address of the preview page for MBTiles files, relative to the home page
     */
    private static final String URL_PREVIEW_MBTILES = "/preview/mbtiles";
    /**
     * Represents the address of the preview page for Tiles directories, relative to the home page
     */
    private static final String URL_PREVIEW_TILES = "/preview/tiles";
    /**
     * Represents the address of the MBTiles operation, relative to the home page
     */
    private static final String URL_MBTILES = "/mbtiles";
    /**
     * Represents the address of the Tiles operation, relative to the home page
     */
    private static final String URL_TILES = "/tiles";
    /**
     * Represents the address of the available tilesets as JSON
     * - used in <b>Mobile Geodesy App</b>
     */
    private static final String URL_AVAILABLE_TILESETS = "/availabletilesets";
    /**
     * Represents the address of the Static Files operation, relative to the home page
     */
    private static final String URL_STATIC_FILES = "/static";
    /**
     * Represents the tileset parameter, used to set the MBTiles file in MBTiles operation
     */
    private static final String PARAMETER_TILESET = "tileset";
    /**
     * Represents the filename parameter, used to set the static file
     */
    private static final String PARAMETER_STATICFILE = "filename";

    /**
     * Represents the z coordinate of a tile
     */
    private static final String PARAMETER_Z = "z";
    /**
     * Represents the x coordinate of a tile
     */
    private static final String PARAMETER_X = "x";
    /**
     * Represents the y coordinate of a tile, use negative values if TMS is used
     */
    private static final String PARAMETER_Y = "y";

    private AsyncServer server;
    private AsyncHttpServer httpServer;

    private String rootPath;
    private int port;
    private Context appContext;
    private MBTilesDatabase mbTilesDatabase;

    /**
     * Creates a new tile server. To start it call {@link TileServer#start(int)}.
     *
     * @param rootPath sets the root directory for the server, where all map tiles are located
     * @param context  application context - for accessing resource files and strings
     */
    TileServer(String rootPath, Context context) {
        ServerFiles.setAppResources(context);

        this.appContext = context;
        this.rootPath = rootPath;

        // this object is set on each request, but it's reference is kept
        // here so each successive request will use the same object
        this.mbTilesDatabase = null;

        this.createRootDirectoryIfDoesNotExist();
    }

    /**
     * Creates all needed files and directories if does not exist. The path to the directory
     * is set in app settings.
     */
    private void createRootDirectoryIfDoesNotExist() {
        // creates root directory if it does not exist
        HelperClass.createDirectory(this.rootPath);
        // create a dummy file so the photo gallery doesn't read the map tiles
        HelperClass.createDirectory(this.rootPath + "/.nomedia");
        // create MBTiles Tilesets root directory
        HelperClass.createDirectory(this.rootPath + URL_MBTILES);
        // create Directory Tilesets root directory
        HelperClass.createDirectory(this.rootPath + URL_TILES);
        // create Static Files root directory
        HelperClass.createDirectory(this.rootPath + URL_STATIC_FILES);
    }

    /**
     * Starts the {@link TileServer}
     *
     * @param port sets the listening port
     */
    void start(int port) {
        try {
            // create the server and set the paths
            this.httpServer = new AsyncHttpServer();
            this.httpServer.get(URL_HOME_PAGE, this.homePageCallback);
            this.httpServer.get(URL_PREVIEW_MBTILES, this.previewMBTilesPageCallback);
            this.httpServer.get(URL_PREVIEW_TILES, this.previewTilesPageCallback);
            this.httpServer.get(URL_MBTILES, this.getMBTileCallback);
            this.httpServer.get(URL_TILES, this.getAvailableDirectoryTiles);
            this.httpServer.get(URL_TILES + ".*", this.getTileCallback);
            this.httpServer.get(URL_AVAILABLE_TILESETS, this.getAvailableTilesetsAsJson);
            this.httpServer.get(URL_STATIC_FILES, this.getStaticFile);

            this.port = port;

            this.server = new AsyncServer();
            this.httpServer.listen(this.server, this.port);

            ServerFiles.setServerUrlAddress(this.getHomeAddress());

            Log.i(TAG, "TileServer started");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Stops the {@link TileServer}
     */
    void stop() {
        this.httpServer.stop();
        this.server.stop();
        Log.i(TAG, "TileServer stopped");
    }

    /**
     * Gets the Server Home Address
     *
     * @return Server Home Address
     */
    String getHomeAddress() {
        return String.format(Locale.getDefault(), URL_HOME, this.port);
    }

    /**
     * Gets the Server Root Directory path - where all tilesets are stored
     *
     * @return Server Root Directory Path
     */
    String getRootDirectoryPath() {
        return this.rootPath;
    }

    /**
     * Gets the full path to a MBTiles tileset
     *
     * @param tilesetName name of MBTiles file
     * @return full path to the MBTiles file
     */
    private String getPathToMBTilesTileset(String tilesetName) {
        if (!tilesetName.endsWith(".mbtiles")) {
            tilesetName += ".mbtiles";
        }
        return this.getRootDirectoryPath() + URL_MBTILES + "/" + tilesetName;
    }

    /**
     * Gets the full path to a static file
     *
     * @param filePath path to the static file, relative to the '/static' directory
     * @return full path to a static file
     */
    private String getPathToStaticFile(String filePath) {
        if (filePath == null) {
            return null;
        }
        if (filePath.equals(URL_STATIC_FILES)) {
            return null;
        }
        return this.getRootDirectoryPath() + URL_STATIC_FILES + "/" + filePath;
    }

    /**
     * Gets the full path to a directory containing tiles
     *
     * @param tilesetName name of directory
     * @return full path to the directory
     */
    private String getPathToDirectoryTileset(String tilesetName) {
        return this.getRootDirectoryPath() + URL_TILES + "/" + tilesetName;
    }

    /**
     * Queries a MBTiles file for a specific Map Tile and returns it. MBTiles by default use TMS for the tiles.
     * If your mapping application uses TMS, set {@link TileServer#PARAMETER_Y} parameters as negative.
     *
     * @param parameters Request parameters:
     *                   <ul>
     *                   <li>{@link TileServer#PARAMETER_TILESET} - specifies the name of the MBTiles file to query data from (relative to the server's root directory)</li>
     *                   <li>{@link TileServer#PARAMETER_Z} - specifies the zoom level</li>
     *                   <li>{@link TileServer#PARAMETER_X} - specifies tile's x coordinate</li>
     *                   <li>{@link TileServer#PARAMETER_Y} - specifies tile's y coordinate. Use negative value if TMS schema is used by your mapping application</li>
     *                   </ul>
     * @return Image
     */
    private byte[] getTileFromMBTilesFile(Multimap parameters) {
        try {
            String mbtilesFilePath = this.getPathToMBTilesTileset(parameters.getString(PARAMETER_TILESET));
            if (!mbtilesFilePath.endsWith(".mbtiles")) {
                mbtilesFilePath += ".mbtiles";
            }

            File file = new File(mbtilesFilePath);
            if (!file.exists() || (file.exists() && file.isDirectory())) {
                // mbtiles file must exist
                return null;
            }

            int z = Integer.parseInt(parameters.getString(PARAMETER_Z));
            int x = Integer.parseInt(parameters.getString(PARAMETER_X));
            int y = Integer.parseInt(parameters.getString(PARAMETER_Y));

            if (this.mbTilesDatabase == null) {
                this.mbTilesDatabase = new MBTilesDatabase(appContext, mbtilesFilePath);
            } else {
                if (!mbtilesFilePath.equals(this.mbTilesDatabase.getDatabaseName())) {
                    // close current mbtiles tileset
                    this.mbTilesDatabase.close();
                    // open a new mbtiles tileset
                    this.mbTilesDatabase = new MBTilesDatabase(appContext, mbtilesFilePath);
                }
            }

            return this.mbTilesDatabase.getTile(z, x, y);
        } catch (NullPointerException ex) {
            return null;
        }
    }

    /**
     * Reads the information about a MBTiles tileset and returns it.
     *
     * @param tilesetName name of the tileset
     * @return tileset info like
     */
    private TilesetInfo getMBTilesInfoFor(String tilesetName) {
        try {
            String mbtilesFilePath = this.getPathToMBTilesTileset(tilesetName);

            File file = new File(mbtilesFilePath);
            if (!file.exists() || (file.exists() && file.isDirectory())) {
                return null;
            }

            return this.getMBTilesInfoFor(file);
        } catch (NullPointerException ex) {
            return null;
        }
    }

    /**
     * Reads the information about a MBTiles tileset and returns it.
     *
     * @param file MBTiles file to read
     * @return tileset info
     */
    private TilesetInfo getMBTilesInfoFor(File file) {
        try {
            MBTilesDatabase tileset = new MBTilesDatabase(this.appContext, file.getAbsolutePath());
            tileset.close();
            return tileset.getInfo();
        } catch (NullPointerException ex) {
            return null;
        }
    }

    /**
     * Gets the information for all available MBTiles tilesets, served by the server.
     * MBTiles tilesets are located in {@link TileServer#URL_MBTILES}.
     *
     * @return list of tileset info
     */
    private List<TilesetInfo> getInfoForAllMBTilesTilesets() {
        List<TilesetInfo> result = new ArrayList<>();

        try {
            File directory = new File(this.rootPath + URL_MBTILES);
            if (directory.exists() && directory.isDirectory()) {
                File[] files = HelperClass.getFilesFrom(directory, ".mbtiles");
                for (File file : files) {
                    TilesetInfo info = this.getMBTilesInfoFor(file);
                    result.add(info);
                }
            }
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }

        return result;
    }

    private List<StaticFileInfo> getInfoForAllStaticFiles() {
        List<StaticFileInfo> result = new ArrayList<>();

        try {
            File directory = new File(this.rootPath + URL_STATIC_FILES);
            if (directory.exists() && directory.isDirectory()) {
                File[] files = HelperClass.getFilesFrom(directory, null);
                for (File file : files) {
                    StaticFileInfo info = new StaticFileInfo(file);
                    result.add(info);
                }
            }
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }

        return result;
    }

    /**
     * Gets the information for all available directories with tilesets, served by the server.
     * Tiles are located in {@link TileServer#URL_TILES}.
     *
     * @return list of tileset info
     */
    private List<TilesetInfo> getInfoForAllDirectoryTilesets() {
        List<TilesetInfo> result = new ArrayList<>();

        try {
            File directory = new File(this.rootPath + URL_TILES);
            if (directory.exists() && directory.isDirectory()) {
                File[] files = HelperClass.getDirectoriesFrom(directory);
                for (File file : files) {
                    result.add(new TilesetInfo(file));
                }
            }
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }

        return result;
    }

    /**
     * Returns a map tile from the specified path - use for Directory Tilesets.
     *
     * @param path File path, relative to the server's root directory
     * @return Image
     */
    private byte[] returnTile(String path) {
        byte[] data = null;
        try {
            File file = HelperClass.getFileFromPath(this.getRootDirectoryPath() + path);
            if (file != null && file.exists() && !file.isDirectory()) {
                data = Files.readAllBytes(file.toPath());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return returnTile(data);
    }

    /**
     * Returns a map tile - use for MBTiles Tilesets.
     *
     * @param data if null or empty, {@link ServerFiles#NO_TILE_IMAGE} will be returned
     * @return Image file
     */
    private byte[] returnTile(byte[] data) {
        if (data == null || data.length == 0) {
            data = ServerFiles.NO_TILE_IMAGE;
        }
        return data;
    }

    /**
     * @see <a href="http://192.168.100.7:1886/">Go to server's home page</a>
     */
    private final HttpServerRequestCallback homePageCallback = (request, response) -> {
        String html = ServerFiles.getHomePageHtml();
        response.send(html);
    };

    /**
     * @see <a href="http://192.168.100.7:1886/preview/mbtiles?tileset=glavatar-kaleto-M5000-zoom1_17.mbtiles">Preview a MBTiles Tileset</a>
     */
    private final HttpServerRequestCallback previewMBTilesPageCallback = (request, response) -> {
        Multimap parameters = request.getQuery();
        int responseCode = 200;
        String responseData;

        try {
            if (parameters.isEmpty()) {
                responseCode = 400;
                responseData = "'tileset' is required URL parameter but was not provided";
            } else {
                String tilesetName = parameters.getString(PARAMETER_TILESET);
                if (tilesetName == null) {
                    responseCode = 400;
                    responseData = "'tileset' is required URL parameter but was not provided";
                } else {
                    TilesetInfo tilesetInfo = getMBTilesInfoFor(tilesetName);
                    if (tilesetInfo == null) {
                        responseCode = 400;
                        responseData = "Tileset with name '" + tilesetName + "' is not available. Check the name of the tileset and try again.";
                    } else {
                        // return the preview page at last
                        responseData = ServerFiles.getMBTilesPreviewPageHtmlFor(tilesetInfo);
                    }
                }
            }
        } catch (Exception ex) {
            responseCode = 500;
            responseData = HelperClass.formatException(ex);
        }

        // add pages for Bad Request and Internal Server Error
        response.code(responseCode);
        if (responseCode == 500) {
            response.send(ServerFiles.getInternalServerErrorPage(responseData));
        } else if (responseCode == 400) {
            response.send(ServerFiles.getBadRequestPage(responseData));
        } else {
            response.send(responseData);
        }
    };

    /**
     * @see <a href="http://192.168.100.7:1886/preview/tiles?tileset=default">Preview a Directory Tileset</a>
     */
    private final HttpServerRequestCallback previewTilesPageCallback = (request, response) -> {
        Multimap parameters = request.getQuery();
        int responseCode = 200;
        String responseData;

        try {
            if (parameters.isEmpty()) {
                responseCode = 400;
                responseData = "'tileset' is required URL parameter but was not provided";
            } else {
                String tilesetName = parameters.getString(PARAMETER_TILESET);
                if (tilesetName == null) {
                    responseCode = 400;
                    responseData = "'tileset' is required URL parameter but was not provided";
                } else {
                    File file = new File(getPathToDirectoryTileset(tilesetName));
                    if (file.exists() && file.isDirectory()) {
                        TilesetInfo tilesetInfo = new TilesetInfo(file);
                        responseData = ServerFiles.getDirectoryTilesPreviewPageHtmlFor(tilesetInfo);
                    } else {
                        responseCode = 400;
                        responseData = "Tileset with name '" + tilesetName + "' is not available. Check the name of the tileset and try again.";
                    }
                }
            }
        } catch (Exception ex) {
            responseCode = 500;
            responseData = HelperClass.formatException(ex);
        }

        // add pages for Bad Request and Internal Server Error
        response.code(responseCode);
        if (responseCode == 500) {
            response.send(ServerFiles.getInternalServerErrorPage(responseData));
        } else if (responseCode == 400) {
            response.send(ServerFiles.getBadRequestPage(responseData));
        } else {
            response.send(responseData);
        }
    };

    /**
     * @see <a href="http://192.168.100.7:1886/mbtiles">Go to available MBTiles Tilesets</a>
     * @see <a href="http://192.168.100.7:1886/mbtiles?tileset=glavatar-kaleto-M5000-zoom1_17.mbtiles&z=14&x=9323&y=6061">Get an example of MBTiles Tile</a>
     */
    private final HttpServerRequestCallback getMBTileCallback = (request, response) -> {
        Multimap parameters = request.getQuery();
        int responseCode = 200;
        String responseData = "";
        byte[] responseDataArray = null;

        try {
            if (parameters.isEmpty()) {
                // send a list of available MBTiles tilesets
                List<TilesetInfo> tilesets = getInfoForAllMBTilesTilesets();
                responseData = ServerFiles.getAvailableMBTilesPageHtmlFor(tilesets);
            } else {
                responseDataArray = returnTile(getTileFromMBTilesFile(parameters));
            }
        } catch (Exception ex) {
            responseCode = 500;
            responseData = HelperClass.formatException(ex);
        }

        // add pages for Bad Request and Internal Server Error
        response.code(responseCode);
        if (responseCode == 500) {
            response.send(ServerFiles.getInternalServerErrorPage(responseData));
        } else {
            if (responseDataArray != null) {
                response.send("image/png", responseDataArray);
            } else {
                response.send(responseData);
            }
        }
    };

    /**
     * @see <a href="http://192.168.100.7:1886/tiles">Go to available Directory Tilesets</a>
     */
    private final HttpServerRequestCallback getAvailableDirectoryTiles = (request, response) -> {
        int responseCode = 200;
        String responseData;

        try {
            // send a list of available Directory tilesets
            List<TilesetInfo> tilesets = getInfoForAllDirectoryTilesets();
            responseData = ServerFiles.getAvailableDirectoryTilesPageHtmlFor(tilesets);
        } catch (Exception ex) {
            responseCode = 500;
            responseData = HelperClass.formatException(ex);
        }

        // add pages for Bad Request and Internal Server Error
        response.code(responseCode);
        if (responseCode == 500) {
            response.send(ServerFiles.getInternalServerErrorPage(responseData));
        } else {
            response.send(responseData);
        }
    };

    /**
     * @see <a href="http://192.168.100.7:1886/tiles/default/10/582/378.png">Get an example of Directory Tile</a>
     */
    private final HttpServerRequestCallback getTileCallback = (request, response) -> {
        int responseCode = 200;
        String responseData = "";
        byte[] responseDataArray = null;

        try {
            responseDataArray = returnTile(request.getPath());
        } catch (Exception ex) {
            responseCode = 500;
            responseData = HelperClass.formatException(ex);
        }

        // add pages for Bad Request and Internal Server Error
        response.code(responseCode);
        if (responseCode == 500) {
            response.send(ServerFiles.getInternalServerErrorPage(responseData));
        } else {
            if (responseDataArray != null) {
                response.send("image/png", responseDataArray);
            } else {
                response.send(responseData);
            }
        }
    };

    /**
     * @see <a href="http://192.168.100.7:1886/availabletiles">Go to available Tilesets as JSON</a>
     */
    private final HttpServerRequestCallback getAvailableTilesetsAsJson = (request, response) -> {
        int responseCode = 200;
        String responseData = "";
        JSONArray result = new JSONArray();

        try {
            // get all available directory tilesets
            List<TilesetInfo> tilesets = getInfoForAllMBTilesTilesets();
            for (int i = 0; i < tilesets.size(); i++) {
                TilesetInfo info = tilesets.get(i);
                JSONObject infoJson = info.toJson();
                String url = ServerFiles.getUrlAddressFor(info, ServerFiles.TilesetType.MBTiles);
                infoJson.put("url", url);
                result.put(i, infoJson);
            }

            int tmp = result.length();
            // get all available mbtiles tilesets
            tilesets.clear();
            tilesets = getInfoForAllDirectoryTilesets();
            for (int i = 0; i < tilesets.size(); i++, tmp++) {
                TilesetInfo info = tilesets.get(i);
                JSONObject infoJson = info.toJson();
                String url = ServerFiles.getUrlAddressFor(info, ServerFiles.TilesetType.DirectoryTiles);
                infoJson.put("url", url);
                result.put(tmp, infoJson);
            }
        } catch (Exception ex) {
            responseCode = 500;
            responseData = HelperClass.formatException(ex);
        }

        // add pages for Bad Request and Internal Server Error
        response.code(responseCode);
        if (responseCode == 500) {
            response.send(ServerFiles.getInternalServerErrorPage(responseData));
        } else {
            response.send(result);
        }
    };

    /**
     * @see <a href="http://192.168.100.7:1886/static?file=cez.json">Get a static file</a>
     */
    private final HttpServerRequestCallback getStaticFile = (request, response) -> {
        Multimap parameters = request.getQuery();
        int responseCode = 200;
        String responseData = "";
        byte[] responseDataArray = null;
//        String contentType = null;
        File file = null;

        try {
            if (parameters.isEmpty()) {
                // send a list of all static files
                List<StaticFileInfo> staticFiles = getInfoForAllStaticFiles();
                responseData = ServerFiles.getAvailableStaticFilesPageHtmlFor(staticFiles);
            } else {
                String fileName = parameters.getString(PARAMETER_STATICFILE);
                String pathToFile = this.getPathToStaticFile(fileName);

                file = HelperClass.getFileFromPath(pathToFile);
//                if (file != null) {
//                    contentType = HelperClass.getContentTypeForFile(file);
//                    responseDataArray = Files.readAllBytes(file.toPath());
//                }
            }
        } catch (Exception ex) {
            responseCode = 500;
            responseData = HelperClass.formatException(ex);
        }

        // add pages for Bad Request and Internal Server Error
        response.code(responseCode);
        if (responseCode == 500) {
            response.send(ServerFiles.getInternalServerErrorPage(responseData));
        } else {
            if (file == null) {
                response.send(responseData);
            } else {
                response.sendFile(file);
            }

//            if (responseDataArray != null) {
//                response.sendFile(file);
//                //response.send(contentType, responseDataArray);
//            } else {
//                response.send(responseData);
//            }
        }
    };
}