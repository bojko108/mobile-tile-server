# Mobile Tile Server

Mobile Tile Server is a local HTTP server, serving Map Tiles from the device storage. When the server is running you can access the map tiles from different mapping applications. The application provides access to two types of tilesets:

- Map Tiles stored in directories - [Slippy Maps](https://wiki.openstreetmap.org/wiki/Slippy_map_tilenames)
- Map Tiles stored in MBTiles files - [MBTiles files](https://github.com/mapbox/mbtiles-spec)

# Available HTTP routes

### [Get a list of all MBTiles Tilesets](/mbtiles)

### [Get a list of all Directory Tilesets](/tiles)

### [Get a list of all Tilesets in JSON format](/availabletilesets)

# Info

The map tiles must be stored in device storage and the app should have access to the raw files. In app settings you can change the root directory, where the tilesets are stored and also the server's listening port. When the server is running all tilesets from the root directory can be accessed using HTTP GET Requests.

---

> ⚠️ The tile server is running in background and battery optimization functions in Android can cause it to become inactive after time. In order to be able to use it for a longer periods of time it is recommended to enable manual settings for **Mobile Tile Server** App in Device Settings > Battery > App Launch.

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

## How to use in mapping applications

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
