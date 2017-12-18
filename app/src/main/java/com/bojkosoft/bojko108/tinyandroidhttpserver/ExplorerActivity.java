package com.bojkosoft.bojko108.tinyandroidhttpserver;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ExplorerActivity extends ListActivity implements View.OnClickListener {

    public static final String DIRECTORY = "DIRECTORY";
    public static final String FILE = "FILE";

    private List<String> path = null;
    private TextView myPath;
    private String mCurrentPath;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fileexplorer);

        this.myPath = (TextView) findViewById(R.id.path);
        findViewById(R.id.choosedirectory).setOnClickListener(this);

        // list current directory: external storage
        getDir(Environment.getExternalStorageDirectory() + "");
    }

    private void getDir(String dirPath) {
        this.mCurrentPath = dirPath;
        this.myPath.setText(dirPath);

        String root = "/";
        List<String> item = new ArrayList<String>();
        this.path = new ArrayList<String>();

        File f = new File(dirPath);
        File[] files = f.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return dir.isDirectory();
            }
        });

        Arrays.sort(files);

        if (!dirPath.equals(root)) {
            item.add(root);
            this.path.add(root);

            item.add("../");
            this.path.add(f.getParent());
        }

        for (File file : files) {
            this.path.add(file.getPath());
            if (file.isDirectory())
                item.add(file.getName() + "/");
            else
                item.add(file.getName());
        }

        ArrayAdapter<String> fileList = new ArrayAdapter<String>(this, R.layout.fileexplorerentry, item);
        this.setListAdapter(fileList);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        File file = new File(this.path.get(position));

        if (file.isDirectory() && file.canRead()) {
            getDir(this.path.get(position));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.choosedirectory:
                Intent returnToMainActivity = new Intent();
                returnToMainActivity.putExtra(DIRECTORY, this.mCurrentPath);
                setResult(RESULT_OK, returnToMainActivity);

                this.finish();
            default:
                break;
        }
    }
}