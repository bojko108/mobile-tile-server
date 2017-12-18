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
    private String mRootPath;
    private byte[] mNoTile;

    public MyServer(int port, String rootPath, byte[] noTile) throws IOException {
        super(port);
        this.mRootPath = rootPath;
        this.mNoTile = noTile;
        this.start();
    }

    @Override
    public Response handle(IHTTPSession session) {
        InputStream stream = null;
        try {
            File file = new File(this.mRootPath + session.getUri());
            if (!file.exists() || (file.exists() && file.isDirectory())) {
                stream = new ByteArrayInputStream(this.mNoTile);
            } else {
                stream = new FileInputStream(file);
            }
            return Response.newChunkedResponse(Status.OK, "image/png", stream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return Response.newFixedLengthResponse(Status.NOT_FOUND, "text/plain", e.getMessage());
        }
    }
}
