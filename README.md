# Mobile Tile Server

Mobile Tile Server is a local HTTP server, serving Map Tiles from the device storage. When the server is running you can access the map tiles from different mapping applications. The application provides access to two types of tilesets:

- Map Tiles stored in directories - [Slippy Maps](https://wiki.openstreetmap.org/wiki/Slippy_map_tilenames)
- Map Tiles stored in MBTiles files - [MBTiles files](https://github.com/mapbox/mbtiles-spec)

Simmilar map tiles server is available for Windows (with NodeJS) - [Windows Tile Server](https://github.com/bojko108/windows-tile-server).

## Contents

- [Info](#info)
  - [Available HTTP routes](#available-http-routes)
- [Directory Tilesets](#directory-tilesets)
  - [Get a list of available Directory Tilesets](#get-a-list-of-available-directory-tilesets)
- [MBTiles Tilesets](#mbtiles-tilesets)
  - [Get a list of available MBTiles Tilesets](#get-a-list-of-available-mbtiles-tilesets)
- [Preview Tilesets](#preview-tilesets)
- [Examples](#examples)
  - [Mobile Geodesy App](#mobile-geodesy-app)
  - [OruxMaps App](#oruxmaps-app)
- [Dependencies](#dependencies)

## Info

The map tiles must be stored in device storage and the app should have access to the raw files. In app settings you can change the root directory, where the tilesets are stored and also the server's listening port. When the server is running all tilesets from the root directory can be accessed using HTTP GET Requests.

---

> ⚠️ The tile server is running in background and battery optimization functions in Android can cause it to become inactive after time. In order to be able to use it for a longer periods of time it is recommended to enable manual settings for **Mobile Tile Server** App in Device Settings > Battery > App Launch.

### Available HTTP routes

- `http://localhost:{port}/` - this is the home address of the server where you can get usefull infromation on how to use the tilesets in your mapping applications
- `http://localhost:{port}/preview/mbtiles?tileset={tileset}` - preview a MBTiles tileset, where `tileset` query parameter sets the name of the MBTiles file
- `http://localhost:{port}/preview/tiles?tileset={tileset}` - preview a Directory tileset, where `tileset` query parameter sets the name of the directory
- `http://localhost:{port}/mbtiles` - list all available MBTiles tilesets, served by the server
- `http://localhost:{port}/mbtiles?tileset={tileset}&z={z}&x={x}&y={y}` - returns a map tile from a MBTiles tileset, where `tileset` query parameter sets the name of the MBTiles file; `z`, `x` and `y` represents [tile coordinates](https://wiki.openstreetmap.org/wiki/Slippy_map_tilenames) - if your mapping application uses [TMS Schema](https://github.com/mapbox/mbtiles-spec/blob/master/1.3/spec.md#content-1) use nagative values for `y`.
- `http://localhost:{port}/tiles` - list all available Directory tilesets, served by the server
- `http://localhost:{port}/tiles/{tileset}/{z}/{x}/{y}.png` - returns a map tile from a Directory tileset, where `tileset` query parameters sets the name of the directory; `z`, `x` and `y` represents [tile coordinates](https://wiki.openstreetmap.org/wiki/Slippy_map_tilenames).
- `http://localhost:{port}/availabletilesets` - returns all available tilesets in JSON format - check the example use with [Mobile Geodesy App](#mobile-geodesy-app) for more details.
- `http://localhost:{port}/static` - returns all available static files for download
- `http://localhost:{port}/static?filename={file_name}` - gets the specified static file

## Directory Tilesets

Directory Tilesets are image files stored in directories. Each zoom level (`z` coordinate) is stored in a separate subdirectory and each tile column (`x` coordinate) is stored in additional subdirectory. This is an example file structure of the root directory:

```
📦MobileTileServer      --> server root directory
 ┣ 📂tiles
 ┃ ┣ 📂default          --> tileset name
 ┃ ┃ ┣ 📂1              --> zoom level (z coordinate)
 ┃ ┃ ┃ ┗ 📂1            --> x coordinate
 ┃ ┃ ┃ ┃ ┗ 📜0.png      --> map tile (y coordinate)
 ┃ ┃ ┣ 📂2
 ┃ ┃ ┃ ┗ 📂2
 ┃ ┃ ┃ ┃ ┗ 📜1.png
 ┃ ┃ ┃ ┗ ...
 ┃ ┃ ┗ ...
 ┃ ┗ ...
 ┗ 📂mbtiles
```

#### Access

Add the following url to your mapping application:

```
http://localhost:{port}/tiles/{tileset}/{z}/{x}/{y}.png
```

- `tileset` - represents the name of the directory
- `z` - represents the zoom level
- `x` - represents tile column
- `y` - represents tile row - if your mapping application uses TMS schema - set this value as negative

You can go to the [examples](#examples) for more details on how to use the tilesets.

### Get a list of available Directory Tilesets

You can list all available Directory Tilesets by going to:

```
http://localhost:{port}/tiles
```

This will return a list of all available Directory Tilesets, served from this server as well as additional information, describing the parameters of the tilesets:

### Example Repsonse

> **Available Directory Tilesets**
>
> This is a list of all directories, containing tiles and are served from this service:
>
> - **default** - preview with Leaflet viewer
>
> | Property                  | Value                                                                                                                                                       |
> | ------------------------- | :---------------------------------------------------------------------------------------------------------------------------------------------------------- |
> | Min Zoom                  | 1                                                                                                                                                           |
> | Max Zoom                  | 12                                                                                                                                                          |
> | Bounds                    | 22.0,40.5,28.0,45.0 (unable to calculate, default value is returned)                                                                                        |
> | Center                    | 25.0,42.75,6.0 (unable to calculate, default value is returned)                                                                                             |
> | For use in Mobile Geodesy | Tilesets are automatically added to Mobile Geodesy App. Just start the tile server, open Mobile Geodesy and choose desired tileset to load it as a basemap. |
> | For use in OruxMaps       | XML text to be added to OruxMaps App configuration file stored in: _/storage/emulated/0/oruxmaps/mapfiles/onlinemapsources.xml_                             |

## MBTiles Tilesets

MBTiles Tilesets are SQLite databases with known schema - [MBTiles](https://github.com/mapbox/mbtiles-spec). This is an example file structure of the root directory:

```

📦MobileTileServer                                --> server root directory
┣ 📂mbtiles
┃ ┣ 📜glavatar-kaleto-M5000-zoom1_17.mbtiles      --> MBTiles Tileset
┃ ┣ 📜glavatar-kaleto-orthophoto-zoom1_18.mbtiles --> MBTiles Tileset
┃ ┗ ...
┗ 📂tiles

```

#### Access

Add the following url to your mapping application:

```

`http://localhost:{port}/mbtiles?tileset={tileset}&z={z}&x={x}&y={y}

```

- `tileset` - represents the name of the directory
- `z` - represents the zoom level
- `x` - represents tile column
- `y` - represents tile row - if your mapping application uses TMS schema - set this value as negative

You can go to the [examples](#examples) for more details on how to use the tilesets.

### Get a list of available MBTiles Tilesets

You can list all available MBTIles Tilesets by going to:

```
http://localhost:{port}/mbtiles
```

This will return a list of all available MBTiles Tilesets, served from this server as well as additional information, describing the parameters of the tilesets:

### Example Repsonse

> **Available MBTiles Tilesets**
>
> This is a list of all MBTiles tilesets, served from this server:
>
> - **glavatar-kaleto-M5000-zoom1_17.mbtiles** - preview with Leaflet viewer
>
> | Property                  | Value                                                                                                                                                       |
> | ------------------------- | :---------------------------------------------------------------------------------------------------------------------------------------------------------- |
> | Name                      | glavatar-kaleto-1-17                                                                                                                                        |
> | Version                   | 1.1                                                                                                                                                         |
> | Description               | Glavatar - Kaleto, ETK, Scale 1:5000                                                                                                                        |
> | Min Zoom                  | 1                                                                                                                                                           |
> | Max Zoom                  | 17                                                                                                                                                          |
> | Bounds                    | 24.762587288428623,42.29438246738081,25.033514158464705,42.50673679522477                                                                                   |
> | Center                    | 24.898050723446666,42.400559631302784,10.0                                                                                                                  |
> | For use in Mobile Geodesy | Tilesets are automatically added to Mobile Geodesy App. Just start the tile server, open Mobile Geodesy and choose desired tileset to load it as a basemap. |
> | For use in OruxMaps       | XML text to be added to OruxMaps App configuration file stored in: _/storage/emulated/0/oruxmaps/mapfiles/onlinemapsources.xml_                             |

## Static files

Returns a list of all static files served by this server. This is an example file structure of the root directory:

```

📦MobileTileServer                                --> server root directory
┣ 📂static
┃ ┣ 📜cez.json                                    --> static file
┃ ┣ 📜test.dwg                                    --> another static file
┃ ┗ ...
┗ 📂tiles

```

### Get a list of all available static files 

You can list all available static files by going to:

```
http://localhost:{port}/static
```

This will return a list of all available static files, served from this server as well as additional information, describing the parameters of the files:

### Example Repsonse

> **Available Static Files**
>
> This is a list of all static files, which are served from this service:
>
> - **cez.json** - download file
>
> | Property                  | Value                                                                                                                                                       |
> | ------------------------- | :---------------------------------------------------------------------------------------------------------------------------------------------------------- |
> | Content Type                  | application/json                                                                                                                                                           |
> | Size                 | 6 KB

## Preview Tilesets

All available tilesets can be previewed in a simple Map Viewer created with [Leaflet](https://leafletjs.com). Each type of tileset is accessible on different address:

### Directory Tilesets

Navigate to:

```
http://localhost:{port}/preview/tiles?tileset={tileset}
```

where `tileset` is the name of the directory containing the map tiles.

### MBTiles Tilesets

Navigate to:

```
http://localhost:{port}/preview/mbtiles?tileset={tileset}
```

where `tileset` is the name of the MBTiles file containing the map tiles.

## Static files

Navigate to:

```
http://localhost:{port}/static?filename={file_name}
```

where `file_name` is the name of the static file to return.

## Examples

### Mobile Geodesy App

[Mobile Geodesy](https://github.com/bojko108/mobile-geodesy) is a mapping application, which can be used for collecting data and navigation. The application is capable of displaying tilesets, served from this tile server. At runtime, the Mobile Geodesy App will connect to `http://localhost:{port}/availabletilesets` and will download the list of all available tilesets. Then in the map activity, you can select the desired tileset to load as a basemap.

### OruxMaps App

[OruxMaps](https://www.oruxmaps.com/cs/en/) is another mapping application with lots of tools and capabilities. The app is also capable of loading tilesets by adding the needed infromation in app's configuration file, stored in `/storage/emulated/0/oruxmaps/mapfiles/onlinemapsources.xml`. There you can add additional map sources, which can be then used in the application as basemaps. This is an example:

1. Open the configuration file, stored in `/storage/emulated/0/oruxmaps/mapfiles/onlinemapsources.xml`.
2. Add this XML to the file:

```xml
<onlinemapsource uid="FILL_UNIQUE_ID_VALUE">
  <name>glavatar-kaleto-orthophoto-zoom1_18</name>
  <url><![CDATA[http://localhost:1886/mbtiles?tileset=glavatar-kaleto-orthophoto-zoom1_18.mbtiles&z={z}&x={x}&y={y}]]></url>
  <minzoom>1</minzoom>
  <maxzoom>18</maxzoom>
  <projection>MERCATORESFERICA</projection>
  <cacheable>0</cacheable>
  <downloadable>1</downloadable>
  <maxtilesday>0</maxtilesday>
  <maxthreads>0</maxthreads>
</onlinemapsource>
```

3. Do not forget to set the correct uid value in the xml!

## Dependecies

The HTTP server is build using [AndroidAsync](https://github.com/koush/AndroidAsync) library.

```

dependencies {
implementation 'com.koushikdutta.async:androidasync:3.0.8'
}

```
