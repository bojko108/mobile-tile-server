package server;

import com.bojkosoft.bojko108.tinyandroidhttpserver.utils.QuadKeyGenerator;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import server.request.Method;
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

    private QuadKeyGenerator mQuadKeyGenerator;

    public MyServer(int port, String rootPath, byte[] noTile) throws IOException {
        super(port);

        this.mRootPath = rootPath;
        this.mNoTile = noTile;

        this.mQuadKeyGenerator = new QuadKeyGenerator();

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
        Method method = session.getMethod();

        if (Method.POST.equals(method) || Method.PUT.equals(method)) {
            /*
             * POST request
             */
            Map<String, String> data = new HashMap<>();
            try {
                session.parseBody(data);
                String body = session.getQueryParameterString();
                //String param = session.getParameters();
            } catch (IOException | ResponseException e) {
                e.printStackTrace();
            }
            return Response.newFixedLengthResponse(Status.OK, "text/plain", "POST");
        } else {
            /*
             * GET request
             */
            try {
                String requestedUri = session.getUri();
                if (requestedUri.contains("redirect") && !session.getParameters().isEmpty()) {
                    /*
                     * redirect to another tile server (for example 'bingmaps'):
                     */
                    String redirectUrlAddress = this.generateRedirectUrl(session.getParameters());

                    if (redirectUrlAddress == null) {
                        /*
                         * return no tile:
                         */
                        return this.returnTile(null);
                    } else {
                        /*
                         * redirect:
                         */
                        Response response = Response.newFixedLengthResponse(Status.REDIRECT_SEE_OTHER, MIME_HTML, "");
                        response.addHeader("Location", redirectUrlAddress);
                        return response;
                    }
                } else {
                    /*
                     * return a local tile:
                     */
                    return this.returnTile(session.getUri());
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return Response.newFixedLengthResponse(Status.NOT_FOUND, "text/plain", e.getMessage());
            }
        }
    }

    private Response returnTile(String path) throws FileNotFoundException {
        File file = new File(this.mRootPath + path);
        // if tile not found - return no tile image
        InputStream stream = ((!file.exists() || (file.exists() && file.isDirectory())) ? new ByteArrayInputStream(this.mNoTile) : new FileInputStream(file));
        return Response.newChunkedResponse(Status.OK, "image/png", stream);
    }

    private String generateRedirectUrl(Map<String, List<String>> parameters) {
        String result = null;

        try {
            if (parameters.get("quadkey").get(0).equals("true")) {

                // TODO: read this url from a config!
                //String url = "http://ecn.t0.tiles.virtualearth.net/tiles/a{quadkey}.jpeg?g=6201";
                String url = parameters.get("url").get(0);

                int z = Integer.parseInt(parameters.get("z").get(0));
                int x = Integer.parseInt(parameters.get("x").get(0));
                int y = Integer.parseInt(parameters.get("y").get(0));

                String quadKey = this.mQuadKeyGenerator.TileXYToQuadKey(z, x, y);

                result = url.replace("{quadkey}", quadKey);
            }
        } catch (Exception ex) {
            result = null;
        }

        return result;
    }
}
