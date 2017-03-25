package dev.datvt.cloudtracks.utils;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by datvit on 4/8/16.
 */


public class DownloadFileFromURL extends AsyncTask<String, String, String> {

    public String surl;
    public String path;

    public DownloadFileFromURL(String ecodedurl, String filepath) {
        path = filepath;
        surl = ecodedurl;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        onStart();
    }

    @Override
    protected String doInBackground(String... f_url) {
        int count;
        try {
            URL url = new URL(f_url[0]);
            URLConnection conection = url.openConnection();
            conection.connect();

            int lenghtOfFile = conection.getContentLength();

            InputStream input = new BufferedInputStream(url.openStream(), 8192);
            OutputStream output = new FileOutputStream(path);
            byte data[] = new byte[1024];
            long total = 0;

            while ((count = input.read(data)) != -1) {
                total += count;
                publishProgress("" + (int) ((total * 100) / lenghtOfFile));
                output.write(data, 0, count);
            }
            output.flush();
            output.close();
            input.close();
        } catch (Exception e) {
            Log.e("Error: ", e.getMessage());
        }

        return null;
    }

    protected void onProgressUpdate(String... progress) {
        onUpdate(Integer.parseInt(progress[0]));
    }


    @Override
    protected void onPostExecute(String file_url) {
        Log.d("onPostExec PATH", "" + path);
        onComplete(path);
    }


    public void onComplete(String path) {
    }


    public void onStart() {
    }

    public void onUpdate(int progress) {
    }
}