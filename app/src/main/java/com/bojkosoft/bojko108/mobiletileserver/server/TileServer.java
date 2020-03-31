package com.bojkosoft.bojko108.mobiletileserver.server;

import android.content.Context;
import android.util.Log;

import com.bojkosoft.bojko108.mobiletileserver.R;
import com.bojkosoft.bojko108.mobiletileserver.server.tilesets.MBTilesDatabase;
import com.bojkosoft.bojko108.mobiletileserver.server.tilesets.ServerFiles;
import com.bojkosoft.bojko108.mobiletileserver.server.tilesets.TilesetInfo;
import com.bojkosoft.bojko108.mobiletileserver.utils.HelperClass;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * This class is responsible for processing HTTP requests.
 */
public class TileServer extends NanoHTTPD {
    /**
     * Represents the home url address of the tile server
     */
    private static final String URL_HOME = "http://localhost";
    /**
     * Represents the address of the preview page for MBTiles files, relative to the home page
     */
    private static final String URL_PREVIEW_MBTILES = "/preview/mbtiles";
    /**
     * Represents the address of the preview page for Tiles directories, relative to the home page
     */
    private static final String URL_PREVIEW_TILES = "/preview/tiles";
    /**
     * Represents the address of the Redirect operation, relative to the home page
     */
    private static final String URL_REDIRECT = "/redirect";
    /**
     * Represents the address of the MBTiles operation, relative to the home page
     */
    private static final String URL_MBTILES = "/mbtiles";
    /**
     * Represents the address of the Tiles operation, relative to the home page
     */
    private static final String URL_TILES = "/tiles";
    /**
     * Represents the url parameter, used to set the redirect address
     */
    private static final String PARAMETER_URL = "url";
    /**
     * Represents the quadkey parameter, used in the Redirect operation
     */
    private static final String PARAMETER_QUADKEY = "quadkey";
    /**
     * Represents the tileset parameter, used to set the MBTiles file in MBTiles operation
     */
    private static final String PARAMETER_TILESET = "tileset";
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

    private static final String TAG = TileServer.class.getName();

    /**
     * the main directory served by the server. All directories and
     * files are made accessible.
     */
    private String rootPath;
    /**
     * when no data this tile is returned
     */
    private byte[] noTile;

    private Context appContext;
    private MBTilesDatabase mbtilesDatabase;

    /**
     * Creates a new HTTP Tile Server
     *
     * @param port       Server Listening Port
     * @param rootPath   Path to directory, accessible by this server
     * @param noTileData Image file to return when a tile isn't found
     * @param context    Context of this server
     * @throws IOException exception
     */
    public TileServer(int port, String rootPath, byte[] noTileData, Context context) throws IOException {
        super(port);

        this.appContext = context;
        this.rootPath = rootPath;
        this.noTile = noTileData != null ? noTileData : this.getNoTileFile();

        // will be initialized on each request if needed...
        // if the requests use different MBTiles file
        this.mbtilesDatabase = null;

        Log.i(TAG, "TileServer: started");
    }

    /**
     * Gets the Server Home Address
     *
     * @return Server Home Address
     */
    public String getUrlAddress() {
        return String.format(Locale.getDefault(), URL_HOME + ":%d", this.getListeningPort());
    }

    /**
     * Gets the Server Root Directory path
     *
     * @return Server Root Directory Path
     */
    public String getRootPath() {
        return this.rootPath;
    }

    /**
     * Is the server running or not
     *
     * @return server's running status
     */
    public boolean isRunning() {
        return this.isAlive();
    }

    /**
     * Main HTTP request handler. This method handles the requests for tiles and returns
     * them (if not tile is found in root directory - "no tile" image is returned.
     *
     * @param session the incoming session
     * @return Tile Image or HTML file
     */
    @Override
    public Response serve(IHTTPSession session) {
        Method method = session.getMethod();

        try {
            if (Method.GET.equals(method)) {
                /*
                 * Handle GET request
                 */
                String requestedUri = session.getUri();

                if (requestedUri.startsWith(URL_PREVIEW_MBTILES) && !session.getParameters().isEmpty()) {
                    // http://localhost:1886/preview/mbtiles/?tileset=defaulta.mbtiles
                    TilesetInfo tilesetInfo = this.getMBTilesInfoForMBTilesFile(session.getParameters());
                    String previewHtml = ServerFiles.getPreviewHtmlForMBTiles(this.appContext, this.getUrlAddress(), tilesetInfo);
                    return newFixedLengthResponse(Response.Status.OK, MIME_HTML, previewHtml);
                } else if (requestedUri.startsWith(URL_PREVIEW_TILES) && !session.getParameters().isEmpty()) {
                    // http://localhost:1886/preview/tiles/?tileset=default
                    String requestedTileset = session.getParameters().get(PARAMETER_TILESET).get(0);
                    File file = new File(this.getPathToDirectoryTileset(requestedTileset));
                    if (file.exists() && file.isDirectory()) {
                        TilesetInfo tilesetInfo = new TilesetInfo(file);
                        String previewHtml = ServerFiles.getPreviewHtmlForDirectoryTiles(this.appContext, this.getUrlAddress(), tilesetInfo);
                        return newFixedLengthResponse(Response.Status.OK, MIME_HTML, previewHtml);
                    }
                } else if (requestedUri.startsWith(URL_REDIRECT)) {
                    if (session.getParameters().isEmpty()) {
                        return newFixedLengthResponse(Response.Status.OK, MIME_PLAINTEXT, "show info about redirect");
                    } else {
                        /*
                         * redirect to another tile server (for example 'bingmaps'):
                         */
                        String redirectUrlAddress = this.generateRedirectUrl(session.getParameters());

                        if (redirectUrlAddress != null) {
                            /*
                             * return tile from redirected URL:
                             */
                            Response response = newFixedLengthResponse(Response.Status.REDIRECT_SEE_OTHER, MIME_HTML, "");
                            response.addHeader("Location", redirectUrlAddress);
                            return response;
                        }
                    }
                } else if (requestedUri.startsWith(URL_MBTILES)) {
                    if (session.getParameters().isEmpty()) {
                        // http://localhost:1886/mbtiles
                        List<TilesetInfo> tilesets = this.getMBTilesInfo();
                        String availableMBTilesHtml = ServerFiles.getPageForAvailableMBTiles(this.appContext, this.getUrlAddress(), tilesets);
                        return newFixedLengthResponse(Response.Status.OK, MIME_HTML, availableMBTilesHtml);
                    } else {
                        // http://localhost:1886/mbtiles/?tileset=defaulta.mbtiles&z=14&x=9323&y=6061

                        /*
                         * return tile from a local MBTiles file:
                         */
                        byte[] data = this.getTileFromMBTilesFile(session.getParameters());
                        return this.returnTile(data);
                    }
                } else if (requestedUri.startsWith(URL_TILES)) {
                    if (requestedUri.equals(URL_TILES)) {
                        // http://localhost:1886/tiles
                        List<TilesetInfo> tilesets = this.getTileDirectoriesInfo();
                        String availableTilesHtml = ServerFiles.getPageForAvailableDirectoryTiles(this.appContext, this.getUrlAddress(), tilesets);
                        return newFixedLengthResponse(Response.Status.OK, MIME_HTML, availableTilesHtml);
                    } else {
                        // http://localhost:1886/tiles/default/10/582/378.png

                        /*
                         * return a local tile:
                         */
                        return this.returnTile(session.getUri());
                    }
                }
            } else {
                /*
                 * Handle other requests like POST ...
                 */
                return newFixedLengthResponse(Response.Status.BAD_REQUEST, MIME_PLAINTEXT, this.appContext.getResources().getString(R.string.exception_bad_request));
            }
        } catch (Exception ex) {
            Log.e(TAG, "TileServer Exception:", ex);
        }

        /*
         * return help text
         */
        InputStream stream = this.appContext.getResources().openRawResource(R.raw.home);
        return newChunkedResponse(Response.Status.OK, MIME_HTML, stream);
    }

    /**
     * Gets a map tile from specified path
     *
     * @param path File path, relative to the server's root directory
     * @return Image
     */
    private Response returnTile(String path) {
        // if tile not found - return no tile image
        byte[] data = null;

        try {
            File file = new File(this.rootPath + path);
            if (file.exists() && !file.isDirectory()) {
                data = Files.readAllBytes(file.toPath());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return returnTile(data);
    }

    /**
     * Gets a map tile from an {@link java.io.ByteArrayInputStream} ByteArrayInputStream
     *
     * @param data if null or empty, {@link TileServer#noTile} will be returned
     * @return Image file
     */
    private Response returnTile(byte[] data) {
        if (data == null || data.length == 0) {
            data = this.noTile.clone();
//            Log.i(TAG, "returnTile: no tile");
        }

        ByteArrayInputStream stream = new ByteArrayInputStream(data);
        return newFixedLengthResponse(Response.Status.OK, MIME_IMAGEPNG, stream, (long) data.length);
    }

    /**
     * Generates a redirect URL address
     *
     * @param parameters Request parameters
     *                   <ul>
     *                   <li><b>quadkey</b> - set this to <b>True</b> if the target tile server uses QuadKey scheme.</li>
     *                   <li><b>url</b> - URL address to navigate to</li>
     *                   <li><b>z</b> - specifies the zoom level</li>
     *                   <li><b>x</b> - specifies tile's x coordinate</li>
     *                   <li><b>y</b> - specifies tile's y coordinate</li>
     *                   </ul>
     * @return URL Address
     */
    private String generateRedirectUrl(Map<String, List<String>> parameters) {
        String result = null;

        try {
            if (parameters.get(PARAMETER_QUADKEY).get(0).equals("true")) {
                String url = parameters.get(PARAMETER_URL).get(0);

                int z = Integer.parseInt(parameters.get(PARAMETER_Z).get(0));
                int x = Integer.parseInt(parameters.get(PARAMETER_X).get(0));
                int y = Integer.parseInt(parameters.get(PARAMETER_Y).get(0));

                String quadKey = HelperClass.getQuadkeyFromXYZ(z, x, y);

                result = url.replace(ServerFiles.QUADKEY, quadKey);
            }
        } catch (Exception ex) {
            result = null;
        }

        return result;
    }

    /**
     * Queries a MBTiles file for a specific Map Tile and returns it. MBTiles by default use TMS for the tiles.
     * If your mapping application uses TMS, set <b>y</b> parameters as negative.
     *
     * @param parameters Request parameters:
     *                   <ul>
     *                   <li><b>tileset</b> - specifies the name of the MBTiles file to query data from (relative to the server's root directory)</li>
     *                   <li><b>z</b> - specifies the zoom level</li>
     *                   <li><b>x</b> - specifies tile's x coordinate</li>
     *                   <li><b>y</b> - specifies tile's y coordinate. Use negative value if TMS schema is used by your mapping application</li>
     *                   </ul>
     * @return Image
     */
    private byte[] getTileFromMBTilesFile(Map<String, List<String>> parameters) {
        try {
            String mbtilesFilePath = this.getPathToMBTilesTileset(parameters.get(PARAMETER_TILESET).get(0));
            if (!mbtilesFilePath.endsWith(".mbtiles")) {
                mbtilesFilePath += ".mbtiles";
            }

            File file = new File(mbtilesFilePath);
            if (!file.exists() || (file.exists() && file.isDirectory())) {
                // mbtiles file must exist
                return null;
            }

            int z = Integer.parseInt(parameters.get(PARAMETER_Z).get(0));
            int x = Integer.parseInt(parameters.get(PARAMETER_X).get(0));
            int y = Integer.parseInt(parameters.get(PARAMETER_Y).get(0));

            // MBTiles by default use TMS for the tiles. Most mapping apps use slippy maps: XYZ schema.
            // We need to handle both.
            if (y > 0) {
                y = (int) Math.pow(2, z) - Math.abs(y) - 1;
            }

            if (!MBTilesDatabase.DATABASE_NAME.equals(mbtilesFilePath)) {
                if (this.mbtilesDatabase != null) {
                    this.mbtilesDatabase.close();
                }
                MBTilesDatabase.DATABASE_NAME = mbtilesFilePath;
                this.mbtilesDatabase = new MBTilesDatabase(appContext, null);
            }

            return this.mbtilesDatabase.getTile(z, x, y);
        } catch (NullPointerException ex) {
            return null;
        }
    }

    /**
     * Reads the information about a MBTiles file and returns it.
     *
     * @param parameters session parameters - only {@link TileServer#PARAMETER_TILESET} is needed to
     *                   determine the requested tileset name
     * @return tileset info like
     */
    private TilesetInfo getMBTilesInfoForMBTilesFile(Map<String, List<String>> parameters) {
        try {
            String mbtilesFilePath = this.getPathToMBTilesTileset(parameters.get(PARAMETER_TILESET).get(0));

            File file = new File(mbtilesFilePath);
            if (!file.exists() || (file.exists() && file.isDirectory())) {
                return null;
            }

            return this.getMBTilesInfoForMBTilesFile(file);
        } catch (NullPointerException ex) {
            return null;
        }
    }

    /**
     * Reads the information about a MBTiles file and returns it.
     *
     * @param file MBTiles file to read
     * @return tileset info
     */
    private TilesetInfo getMBTilesInfoForMBTilesFile(File file) {
        try {
            MBTilesDatabase tileset = new MBTilesDatabase(appContext, file.getAbsolutePath());
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
    private List<TilesetInfo> getMBTilesInfo() {
        List<TilesetInfo> result = new ArrayList<>();

        try {
            File directory = new File(this.rootPath + URL_MBTILES);
            if (directory.exists() && directory.isDirectory()) {
                File[] files = HelperClass.getFilesFrom(directory, ".mbtiles");
                for (File file : files) {
                    TilesetInfo info = this.getMBTilesInfoForMBTilesFile(file);
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
    private List<TilesetInfo> getTileDirectoriesInfo() {
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
     * Get no data tile - this image is returned when a tile isn't found by the server
     *
     * @return Image
     */
    private byte[] getNoTileFile() {
        InputStream stream = this.appContext.getResources().openRawResource(R.raw.no_tile);

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
     * Gets the full path to a MBTiles tileset
     *
     * @param tilesetName name of MBTiles file
     * @return full path to the MBTiles file
     */
    private String getPathToMBTilesTileset(String tilesetName) {
        if (!tilesetName.endsWith(".mbtiles")) {
            tilesetName += ".mbtiles";
        }
        return this.rootPath + URL_MBTILES + "/" + tilesetName;
    }

    /**
     * Gets the full path to a directory containing tiles
     *
     * @param tilesetName name of directory
     * @return full path to the directory
     */
    private String getPathToDirectoryTileset(String tilesetName) {
        return this.rootPath + URL_TILES + "/" + tilesetName;
    }
}
