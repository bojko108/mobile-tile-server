package server;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import server.response.Response;
import server.response.Status;

public class MyServer extends NanoHTTPD {
    /**
     * this.mRoothPath - the main directory served by the server. All directories and
     * files are made accessible.
     */
    private String mRootPath;
    /**
     * this.mNoTile - when no data this tile is returned
     */
    private byte[] mNoTile;

    public MyServer(int port, String rootPath, byte[] noTile) throws IOException {
        super(port);
        this.mRootPath = rootPath;
        this.mNoTile = noTile;
        this.start();
    }

    /**
     * Main HTTP request handler. This method handles the requests for tiles and returns
     * them (if not tile is found in root directory - "no tile" image is returned.
     *
     * @param session the incoming session
     * @return image
     */
    @Override
    public Response handle(IHTTPSession session) {
        InputStream stream = null;
        try {
            File file = new File(this.mRootPath + session.getUri());
            // if tile not found - return no tile
            stream = ((!file.exists() || (file.exists() && file.isDirectory())) ? new ByteArrayInputStream(this.mNoTile) : new FileInputStream(file));
            return Response.newChunkedResponse(Status.OK, "image/png", stream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return Response.newFixedLengthResponse(Status.NOT_FOUND, "text/plain", e.getMessage());
        }
    }
}
