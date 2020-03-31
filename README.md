# Mobile Tile Server

Mobile Tile Server can be used as a HTTP server, serving Map Tiles from the device storage. When the server is running you can access the tiles from different applications. The server uses [XYZ Tile schema](https://wiki.openstreetmap.org/wiki/Slippy_map_tilenames).

## Contents

- [How to use](#how-to-use)
  - [Access to local Map Tiles](#access-to-local-map-tiles)
  - [Access to local MBTiles files](#access-to-local-mbtiles-files)
  - [Redirect to a Tile Server with QuadKey Tile schema](#redirect-to-a-tile-server-with-quadkey-tile-schema)

## How to use

The application provides three options:

- Access to local Map Tiles [Slippy Map](https://wiki.openstreetmap.org/wiki/Slippy_map_tilenames)
- Access to local [MBTiles files](https://github.com/mapbox/mbtiles-spec)
- Redirect to a Tile Server with QuadKey Tile schema

## Access to local Map Tiles

Local Map Tiles can be accessed on address:

```text
http://localhost:PORT/
```

Where `PORT` is set in application settings. In settings, you must specify a directory, where the files are stored. This directory is used as a root for the server. All files in that directory (including subdirectories) will be accessible from the server.
If you have map tiles stored in your memory card on address `/storage/emulated/0/OfflineTiles/Plovdiv/{z}_{x}_{y}.png`, you can set the root directory to: `/storage/emulated/0/OfflineTiles`. Then in order to access this map just start the service and navigate to:

```text
http://localhost:1886/Plovdiv/{z}_{x}_{y}.png
```

In this case the root directory points to the parent folder (which contains `Plovdiv` subfolder). This way you can have multiple subfolders containing different map tiles and all can be accessed through the same server!

## Access to local MBTiles files

Local MBTiles Tiles can be accessed on address:

```text
http://localhost:1886/mbtiles/?file=&useTMS=&z={z}&x={x}&y={y}
```

Where `PORT` is set in application settings. In settings, you must specify a directory, where the files are stored. This directory is used as a root for the server. All files in that directory (including subdirectories) will be accessible from the server.

There are several parameters, which must be provided:

- `file`: MBTiles file (including extension)
- `z`: map zoom level
- `x`: x coordinate of a map tile
- `y`: y coordinate of a map tile

If you have tiles stored in MBTiles format, you can place your files in the root directory and access them with:

```text
http://localhost:1886/mbtiles/?file=test.mbtiles&z={z}&x={x}&y={y}
```

or if XYZ schema is used:

```text
http://localhost:1886/mbtiles/?file=test.mbtiles&z={z}&x={x}&y=-{y}
```

## Redirect to a Tile Server with QuadKey Tile schema

Redirect can be accessed on address:

```text
http://localhost:PORT/redirect/?url=...&quadkey=true&x=&y=&z=
```

Where `PORT` is set in application settings. In settings, you must specify a directory, where the files are stored. This directory is used as a root for the server. All files in that directory (including subdirectories) will be accessible from the server.

There are several parameters, which must be provided:

- `url`: url address on which to redirect
- `quadkey`: `true` if the server uses QuadKey Tile schema
- `z`: map zoom level
- `x`: x coordinate of a map tile
- `y`: y coordinate of a map tile

If you want to use for example Bing Maps, which uses QuadKey Tile schema and you only have XYZ tile coordinates you can use the redirect option, which will calculate the quadkey value and then will redirect the request to the server. For accessing Bing Maps Aerial map tiles you can navigate to:

```text
http://localhost:1886/redirect/?url=http://ecn.t0.tiles.virtualearth.net/tiles/a{quadkey}.jpeg?g=6201&quadkey=true&z={z}&x={x}&y={y}
```
