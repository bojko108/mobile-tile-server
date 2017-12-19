package com.bojkosoft.bojko108.tinyandroidhttpserver;

import android.app.Activity;
import android.content.Intent;

public class ExplorerActivity {
    public static final int DIRECTORY_RESULT = 108;
    public static final int FILE_RESULT = 109;

    public static Intent DirectoryBrowser() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        return intent;
    }
}
