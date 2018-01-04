# Offline Tile Server
Offline Tile Server can be used as a HTTP server, serving Map Tiles from the device storage. When the server is running you can access the tiles from different applications. The server uses XYZ Tile schema.

## Contents
* [How to use](#how-to-use)
    * [Access of local Map Tiles](#access-of-local-map-tiles)
    * [Redirect to a Tile Server with QuadKey Tile schema](#redirect-to-a-tile-server-with-quadkey-tile-schema)
    * [With MyMaps](#with-mymaps)

## How to use
The application provides two options:
- Access of local Map Tiles
- Redirect to a Tile Server with QuadKey Tile schema

## Access of local Map Tiles
Local Map Tiles can be accessed on address: 
```
http://localhost:PORT/
```

Where `PORT` is set in application settings. A directory, where the files are stored must also be specified in settings. This directory is used as a root for the server. All files in that directory (including subdirectories) will be accessible from the server.
If you have map tiles stored in `/storage/9016-4EF8/OfflineTiles/Plovdiv/{z}_{x}_{y}.png`, you can set the root directory to: `/storage/9016-4EF8/OfflineTiles`. Then in order to access this map just start the service and navigate to:
```
http://localhost:1886/Plovdiv/{z}_{x}_{y}.png
```
In this case the root directory points to the parent folder (which contains `Plovdiv` subfolder). This way you can have multiple subfolders containing different map tiles and all can be accessed through the same server!

## Redirect to a Tile Server with QuadKey Tile schema
Redirect can be accessed on address:
```
http://localhost:PORT/redirect/?url=&quadkey=true&x=&y=&z=
```

Where the `PORT` is set in application settings. A directory, where the files are stored must also be specified in settings. This directory is used as a root for the server. All files in that directory (including subdirectories) will be accessible from the server.

There are several parameters, which must be provided:
- url: url address on which to redirect
- quadkey: `true` if the server uses QuadKey Tile schema
- x: x coordinate of a map tile
- y: y coordinate of a map tile
- z: z coordinate of a map tile

If you want to use for example Bing Maps, which uses QuadKey Tile schema and you only have XYZ tile coordinates you can use the redirect option, which will calculate the quadkey value and then will redirect the request to the server. For accessing Bing Maps Aerial map tiles you can navigate to:
```text
http://localhost:1886/redirect/url=http://ecn.t0.tiles.virtualearth.net/tiles/a{quadkey}.jpeg?g=6201quadkey=true&x={x}&z={y}&z={z}
```

## With MyMaps

  1. Edit MyMaps's config file:
```
/storage/emulated/0/MyMaps/offline-tiles.json
```
  2. Run MyMaps App and use the offline tiles served by the tile server.

## License
The MIT License (MIT). Please see [License File](LICENSE) for more information.
