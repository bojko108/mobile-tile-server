package com.bojko108.mobiletileserver.server.tilesets;

import android.text.format.Formatter;
import android.webkit.MimeTypeMap;

import com.bojko108.mobiletileserver.utils.HelperClass;

import java.io.File;
import java.util.Locale;

/**
 * This class provides information about a static file, served by this server.
 * <p>
 * Mobile Tile Server, Copyright (c) 2020 by bojko108
 * <p/>
 */
public class StaticFileInfo {
    private File file;

    public StaticFileInfo(String path) {
        this.file = HelperClass.getFileFromPath(path);
    }

    public StaticFileInfo(File file) {
        this.file = file;
    }

    public String getFileName() {
        return this.file.getName();
    }

    public String getFileNameWithoutExtension() {
        String extension = MimeTypeMap.getFileExtensionFromUrl(file.getAbsolutePath());
        return this.getFileName().replace(extension, "");
    }

    public String getContentType() {
        return HelperClass.getContentTypeForFile(this.file);
    }

    public long getFileSize() {
        return this.file.length();
    }

    public String getSizeAsText() {
        String units = "B";
        long bytes = this.getFileSize();

        if (bytes >= 1024) {
            bytes = bytes / 1024;   // to KB
            units = "KB";
        }

        return String.format(Locale.getDefault(), "%d %s", bytes, units);
    }
}
