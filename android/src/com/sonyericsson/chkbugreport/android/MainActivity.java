/*
 * Copyright (C) 2013 Sony Mobile Communications AB
 *
 * This file is part of ChkBugReport.
 *
 * ChkBugReport is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * ChkBugReport is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ChkBugReport.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.sonyericsson.chkbugreport.android;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

public class MainActivity extends Activity implements OnItemClickListener {

    private ListView mList;
    private BugReportAdapter mAdapter;
    private Button mBtnTakeBugreport;
    private Button mBtnTakeMinireport;
    private TextView mFilesHeader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mList = new ListView(this);
        setContentView(mList);

        mBtnTakeBugreport = new Button(this);
        mBtnTakeBugreport.setText("Take BugReport");
        mList.addHeaderView(mBtnTakeBugreport);

        mBtnTakeMinireport = new Button(this);
        mBtnTakeMinireport.setText("Take MiniReport");
        mList.addHeaderView(mBtnTakeMinireport);

        mFilesHeader = new TextView(this);
        mFilesHeader.setText("Looking for bugreports...");
        mList.addHeaderView(mFilesHeader);

        mAdapter = new BugReportAdapter(this);
        mList.setAdapter(mAdapter);
        mList.setOnItemClickListener(this);

        new LoaderTask().execute();
    }

    @Override
    public void onItemClick(AdapterView<?> list, View item, int position, long id) {
        if (item.getTag() instanceof BugReportSource) {
            BugReportSource bugreport = (BugReportSource) item.getTag();
            Intent intent = new Intent(this, StatusActivity.class);
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(new File(bugreport.path)), "text/plain");
            startActivity(intent);
        }
    }

    class LoaderTask extends AsyncTask<Void, Void, Void> implements Comparator<BugReportSource> {

        private Vector<BugReportSource> mData = new Vector<BugReportSource>();

        @Override
        protected void onPreExecute() {
            mFilesHeader.setText("Looking for bugreports...");
        }

        @Override
        protected Void doInBackground(Void... params) {
            scan("/sdcard/bugreports");
            scan("/sdcard/bugreport");
            scan("/sdcard/");
            Collections.sort(mData, this);
            return null;
        }

        private void scan(String path) {
            File dir = new File(path);
            if (!dir.isDirectory()) {
                return;
            }
            for (File f : dir.listFiles()) {
                if (!f.isFile()) continue;
                String filename = f.getName().toLowerCase();
                if (filename.startsWith("bugreport")) {
                    add(f);
                }
            }
        }

        private void add(File f) {
            BugReportSource item = new BugReportSource();
            item.path = f.getAbsolutePath();
            item.timestamp = f.lastModified();
            mData.add(item);
        }

        @Override
        protected void onPostExecute(Void result) {
            mFilesHeader.setText("Found bugreports:");
            mAdapter.setData(mData);
        }

        @Override
        public int compare(BugReportSource lhs, BugReportSource rhs) {
            if (lhs.timestamp > rhs.timestamp) return -1;
            if (lhs.timestamp < rhs.timestamp) return +1;
            return lhs.path.compareTo(rhs.path);
        }

    }

}


