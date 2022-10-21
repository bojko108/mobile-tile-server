package com.bojko108.mobiletileserver.server.tilesets;

import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

public class MBTilesDatabase extends SQLiteOpenHelper {
    private static final String GET_TILE_SQL_STRING = "SELECT \"tile_data\" FROM \"tiles\" where zoom_level = %d and tile_column=%d and tile_row=%d";
    private static final String GET_INFO_SQL_STRING = "SELECT * FROM \"metadata\"";

    private TilesetInfo info;

    public MBTilesDatabase(Context context, String databaseName) {
        super(context, databaseName, null, 1);
        this.info = this.readInfo(databaseName);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public TilesetInfo getInfo() {
        return this.info;
    }

    public byte[] getTile(int z, int x, int y) {
        byte[] result = null;
        SQLiteDatabase db = this.getReadableDatabase();

        // MBTiles by default use TMS for the tiles. Most mapping apps use slippy maps: XYZ schema.
        // We need to handle both.
        if (y > 0) {
            y = (int) Math.pow(2, z) - Math.abs(y) - 1;
        } else {
            y = Math.abs(y);
        }

        String sql = String.format(Locale.getDefault(), GET_TILE_SQL_STRING, z, x, y);

        try (Cursor cur = db.rawQuery(sql, null)) {
            cur.moveToFirst();
            if (!cur.isAfterLast()) {
                result = cur.getBlob(0);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return result;
    }

    private TilesetInfo readInfo(String fileName) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] path = fileName.split("/");

        TilesetInfo info = new TilesetInfo();
        info.setParameter(TilesetInfo.TILESET_NAME, path[path.length - 1]);

        try (Cursor cur = db.rawQuery(GET_INFO_SQL_STRING, null)) {
            if (cur != null) {
                cur.moveToFirst();
                while (!cur.isAfterLast()) {
                    String name = cur.getString(cur.getColumnIndex("name"));
                    String value = cur.getString(cur.getColumnIndex("value"));
                    info.setParameter(name, value);
                    cur.moveToNext();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return info;
    }

}
