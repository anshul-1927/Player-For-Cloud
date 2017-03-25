package dev.datvt.cloudtracks.song_player;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Process;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.victor.loading.rotate.RotateLoading;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import dev.datvt.cloudtracks.MainActivity;
import dev.datvt.cloudtracks.R;
import dev.datvt.cloudtracks.lyrics.DefaultLrcBuilder;
import dev.datvt.cloudtracks.lyrics.ILrcBuilder;
import dev.datvt.cloudtracks.lyrics.ILrcView;
import dev.datvt.cloudtracks.lyrics.LrcRow;
import dev.datvt.cloudtracks.lyrics.LrcView;
import dev.datvt.cloudtracks.lyrics_tools_api.AZLyrics;
import dev.datvt.cloudtracks.lyrics_tools_api.Bollywood;
import dev.datvt.cloudtracks.lyrics_tools_api.Genius;
import dev.datvt.cloudtracks.lyrics_tools_api.JLyric;
import dev.datvt.cloudtracks.lyrics_tools_api.Lololyrics;
import dev.datvt.cloudtracks.lyrics_tools_api.LyricWiki;
import dev.datvt.cloudtracks.lyrics_tools_api.LyricsMania;
import dev.datvt.cloudtracks.lyrics_tools_api.MetalArchives;
import dev.datvt.cloudtracks.lyrics_tools_api.PLyrics;
import dev.datvt.cloudtracks.lyrics_tools_api.UrbanLyrics;
import dev.datvt.cloudtracks.lyrics_tools_api.ViewLyrics;
import dev.datvt.cloudtracks.noti_service.MediaPlayerService;
import dev.datvt.cloudtracks.utils.ConstantHelper;
import dev.datvt.cloudtracks.utils.FileOperations;
import dev.datvt.cloudtracks.utils.Net;
import dev.datvt.cloudtracks.utils.ToolsHelper;

/**
 * Created by datvt on 7/26/2016.
 */
public class LyricsFragment extends Fragment {

    public final static String TAG = "PlayMusicActivity";
    public int mPalyTimerDuration = 1000;
    public Timer mTimer;
    public TimerTask mTask;
    public Context ctx;
    public LrcView mLrcView;
    public LyricListAdapter lyricListAdapter;
    public LyricAdapter lyricAdapter;
    public ArrayList<Lyrics> lyricsArrayList;
    public ArrayList<String> arrayList;
    private TextView tvError;
    private FileOperations fileOperations;
    private ListView listView, lvLyrics;
    private BroadcastReceiver broadcastReceiver;
    private RotateLoading rotateLoading;
    private ImageView backLyricList;
    private RelativeLayout rlLyrics;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        loadLocale();
        broadcastReceiver = new LyricReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConstantHelper.UPDATE_LYRICS);
        getActivity().registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        if (broadcastReceiver != null) {
            getActivity().unregisterReceiver(broadcastReceiver);
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {



        return inflater.inflate(R.layout.fragment_lyric_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ctx = view.getContext();
        mLrcView = (LrcView) view.findViewById(R.id.lyrics);
        rotateLoading = (RotateLoading) view.findViewById(R.id.rotateloading);
        rlLyrics = (RelativeLayout) view.findViewById(R.id.rlLyrics);

        listView = (ListView) view.findViewById(R.id.lvLyricList);
        lyricsArrayList = new ArrayList<>();
        lyricListAdapter = new LyricListAdapter(ctx, lyricsArrayList);
        listView.setAdapter(lyricListAdapter);
        listView.setVisibility(View.VISIBLE);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                rlLyrics.setBackgroundResource(R.color.colorList);
                listView.setVisibility(View.INVISIBLE);
                lvLyrics.setVisibility(View.VISIBLE);
                backLyricList.setVisibility(View.VISIBLE);
                String nameFile = ToolsHelper.folder + "/CloudMusic/Lyrics/"
                        + lyricsArrayList.get(i).getTrack() + "_" + lyricsArrayList.get(i).getArtist() + ".lc";
                File file = new File(nameFile);

                if (arrayList.size() > 0) {
                    arrayList.clear();
                }

                if (file.exists()) {
                    String lc = fileOperations.read(nameFile);
                    String[] lrc = lc.split("<br>");

                    if (lrc != null && lrc.length > 0) {

                        arrayList.add(getString(R.string.song) + ": " + lyricsArrayList.get(i).getTrack());
                        arrayList.add(getString(R.string.artist) + ": " + lyricsArrayList.get(i).getArtist());

                        for (String s : lrc) {
                            new UpdateMyLyrics().execute(s);
                        }
                    } else {
                        lvLyrics.setVisibility(View.INVISIBLE);
                        tvError.setVisibility(View.VISIBLE);
                    }
                } else {
                    getLyricsByArtist(LyricWiki.class, lyricsArrayList.get(i));
                }
            }
        });

        lvLyrics = (ListView) view.findViewById(R.id.lvLyrics);
        arrayList = new ArrayList<>();
        lyricAdapter = new LyricAdapter(ctx, arrayList);
        lvLyrics.setAdapter(lyricAdapter);
        lvLyrics.setVisibility(View.INVISIBLE);

        tvError = (TextView) view.findViewById(R.id.tvError);
        backLyricList = (ImageView) view.findViewById(R.id.ivBackLyrics);
        tvError.setVisibility(View.INVISIBLE);
        backLyricList.setVisibility(View.INVISIBLE);

        backLyricList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lvLyrics.setVisibility(View.INVISIBLE);
                updateLyric();
            }
        });

        fileOperations = new FileOperations();

        updateLyric();
    }

    private void updateLyric() {
        File file = new File(MainActivity.folder + "/Lyrics/" + PlayMusicActivity.curTrack.title + ".lrc");
        if (file.exists()) {
            rlLyrics.setBackgroundResource(R.color.colorList);
            String lrc = fileOperations.getFileFromStorage(file.getPath());
            ILrcBuilder builder = new DefaultLrcBuilder();
            List<LrcRow> rows = builder.getLrcRows(lrc);
            if (rows != null && rows.size() > 0) {
                mLrcView.setLrc(rows);
                mLrcView.setVisibility(View.VISIBLE);
                listView.setVisibility(View.INVISIBLE);
                lvLyrics.setVisibility(View.INVISIBLE);
                backLyricList.setVisibility(View.INVISIBLE);
                tvError.setVisibility(View.INVISIBLE);

                if (mTimer == null) {
                    mTimer = new Timer();
                    mTask = new LrcTask();
                    mTimer.scheduleAtFixedRate(mTask, 0, mPalyTimerDuration);
                }

                mLrcView.setListener(new ILrcView.LrcViewListener() {

                    public void onLrcSeeked(int newPosition, LrcRow row) {
                        if (MediaPlayerService.mMediaPlayer != null) {
                            Log.d(TAG, "onLrcSeeked:" + row.time);
                            MediaPlayerService.mMediaPlayer.seekTo((int) row.time);
                        }
                    }
                });

            } else {
                tvError.setVisibility(View.VISIBLE);
            }
        } else {
            mLrcView.setVisibility(View.INVISIBLE);
            backLyricList.setVisibility(View.INVISIBLE);
            lvLyrics.setVisibility(View.INVISIBLE);
            listView.setVisibility(View.VISIBLE);
            tvError.setVisibility(View.INVISIBLE);
            if (ToolsHelper.hasConnection(getActivity())) {
                if (PlayMusicActivity.curTrack != null) {
                    if (lyricsArrayList.size() > 0) {
                        lyricsArrayList.clear();
                    }
                    getLyrics(PlayMusicActivity.curTrack.title);
                } else {
                    listView.setVisibility(View.INVISIBLE);
                    tvError.setVisibility(View.VISIBLE);
                }
            } else {
                listView.setVisibility(View.INVISIBLE);
                tvError.setVisibility(View.VISIBLE);
            }
        }
    }

    private void getLyrics(final String song) {
        class GetLyrics extends AsyncTask<Object, Object, List<Lyrics>> {

            private String searchQuery;
            private Class lyricApi;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                rotateLoading.setVisibility(View.VISIBLE);
                rotateLoading.start();
            }

            @Override
            protected List<Lyrics> doInBackground(Object... objects) {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                lyricApi = (Class) objects[0];
                searchQuery = (String) objects[1];
                List<Lyrics> results;
                results = doSearch(lyricApi, searchQuery);
                return results;
            }

            @Override
            protected void onPostExecute(List<Lyrics> lyricses) {
                super.onPostExecute(lyricses);

                if (lyricses != null) {
                    if (lyricses.size() > 0) {
                        for (Lyrics l : lyricses) {
                            new UpdateListLyrics().execute(l);
                        }
                    } else {
                        listView.setVisibility(View.INVISIBLE);
                        tvError.setVisibility(View.VISIBLE);
                    }
                } else {
                    listView.setVisibility(View.INVISIBLE);
                    tvError.setVisibility(View.VISIBLE);
                }

                rotateLoading.stop();
                rotateLoading.setVisibility(View.INVISIBLE);
            }

        }

        GetLyrics getLyrics = new GetLyrics();
        getLyrics.execute(LyricWiki.class, song);
    }

    private List<Lyrics> doSearch(Class provider, String searchQuery) {
        switch (provider.getSimpleName()) {
            case "LyricWiki":
                return LyricWiki.search(searchQuery);
            case "Genius":
                return Genius.search(searchQuery);
            case "Bollywood":
                return Bollywood.search(searchQuery);
            case "JLyric":
                return JLyric.search(searchQuery);
            default:
                try {
                    return (List<Lyrics>) provider.getMethod("search", String.class).invoke(null, searchQuery);
                } catch (Exception ignored) {
                    ignored.printStackTrace();
                    return null;
                }
        }
    }

    public Lyrics download(Class provider, String url, String artist, String title) {
        Lyrics lyrics = null;
        switch (provider.getSimpleName()) {
            case "AZLyrics":
                lyrics = AZLyrics.fromURL(url, artist, title);
                break;
            case "Bollywood":
                lyrics = Bollywood.fromURL(url, artist, title);
                break;
            case "Genius":
                lyrics = Genius.fromURL(url, artist, title);
                break;
            case "JLyric":
                lyrics = JLyric.fromURL(url, artist, title);
                break;
            case "Lololyrics":
                lyrics = Lololyrics.fromURL(url, artist, title);
                break;
            case "LyricsMania":
                lyrics = LyricsMania.fromURL(url, artist, title);
                break;
            case "LyricWiki":
                lyrics = LyricWiki.fromURL(url, artist, title);
                break;
            case "MetalArchives":
                lyrics = MetalArchives.fromURL(url, artist, title);
                break;
            case "PLyrics":
                lyrics = PLyrics.fromURL(url, artist, title);
                break;
            case "UrbanLyrics":
                lyrics = UrbanLyrics.fromURL(url, artist, title);
                break;
            case "ViewLyrics":
                lyrics = ViewLyrics.fromURL(url, artist, title);
                break;
        }
        if (lyrics != null)
            return lyrics;
        return new Lyrics(Lyrics.NO_RESULT);
    }

    public Lyrics download(Class provider, String artist, String title) {
        Lyrics lyrics = new Lyrics(Lyrics.NO_RESULT);
        switch (provider.getSimpleName()) {
            case "AZLyrics":
                lyrics = AZLyrics.fromMetaData(artist, title);
                break;
            case "Bollywood":
                lyrics = Bollywood.fromMetaData(artist, title);
                break;
            case "Genius":
                lyrics = Genius.fromMetaData(artist, title);
                break;
            case "JLyric":
                lyrics = JLyric.fromMetaData(artist, title);
                break;
            case "Lololyrics":
                lyrics = Lololyrics.fromMetaData(artist, title);
                break;
            case "LyricsMania":
                lyrics = LyricsMania.fromMetaData(artist, title);
                break;
            case "LyricWiki":
                lyrics = LyricWiki.fromMetaData(artist, title);
                break;
            case "MetalArchives":
                lyrics = MetalArchives.fromMetaData(artist, title);
                break;
            case "PLyrics":
                lyrics = PLyrics.fromMetaData(artist, title);
                break;
            case "UrbanLyrics":
                lyrics = UrbanLyrics.fromMetaData(artist, title);
                break;
            case "ViewLyrics":
                try {
                    lyrics = ViewLyrics.fromMetaData(artist, title);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
        if (lyrics != null)
            return lyrics;
        return lyrics;
    }

    private void getLyricsByArtist(final Class lyricClass, final Lyrics lyrics) {
        class GetLyricsByArtist extends AsyncTask<Object, Object, Lyrics> {

            Class lyricApi;
            Lyrics lyrics;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                rotateLoading.setVisibility(View.VISIBLE);
                rotateLoading.start();
            }

            @Override
            protected Lyrics doInBackground(Object... objects) {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                lyricApi = (Class) objects[0];
                lyrics = (Lyrics) objects[1];
                Lyrics lyr;
                if (lyrics.getURL() != null && !lyrics.getURL().isEmpty()) {
                    lyr = download(lyricApi, lyrics.getURL(), lyrics.getArtist(), lyrics.getTrack());
                } else {
                    lyr = download(lyricApi, lyrics.getArtist(), lyrics.getTrack());
                }

                return lyr;
            }

            @Override
            protected void onPostExecute(Lyrics lyrics) {
                super.onPostExecute(lyrics);

                if (lyrics != null) {
                    Log.e("LYRICS", lyrics.toString());
                    if (lyrics.getText() != null && !lyrics.getText().isEmpty()) {
                        String nameFile = ToolsHelper.folder + "/CloudMusic/Lyrics/" + lyrics.getTrack() + "_" + lyrics.getArtist() + ".lc";
                        File file = new File(nameFile);
                        if (!file.exists()) {
                            fileOperations.write(nameFile, lyrics.getText().toString());
                        }

                        Log.e("LRC", lyrics.getText().toString());
                        String[] lrc = lyrics.getText().toString().split("<br>");

                        if (lrc != null && lrc.length > 0) {


                            arrayList.add(getString(R.string.song) + ": " + lyrics.getTrack());
                            arrayList.add(getString(R.string.artist) + ": " + lyrics.getArtist());

                            for (String s : lrc) {
                                new UpdateMyLyrics().execute(s);
                            }

                        } else {
                            lvLyrics.setVisibility(View.INVISIBLE);
                            tvError.setVisibility(View.VISIBLE);
                        }
                    } else {
                        lvLyrics.setVisibility(View.INVISIBLE);
                        tvError.setVisibility(View.VISIBLE);
                    }
                } else {
                    lvLyrics.setVisibility(View.INVISIBLE);
                    tvError.setVisibility(View.VISIBLE);
                }

                rotateLoading.stop();
                rotateLoading.setVisibility(View.INVISIBLE);
            }
        }

        GetLyricsByArtist getLyricsByArtist = new GetLyricsByArtist();
        getLyricsByArtist.execute(lyricClass, lyrics);
    }

    public void stopLrcPlay() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    private class UpdateListLyrics extends AsyncTask<Lyrics, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            rotateLoading.setVisibility(View.VISIBLE);
            rotateLoading.start();
        }

        @Override
        protected Void doInBackground(Lyrics... lyricses) {
//            try {
//                String art = new CoverArtLoader().execute(l).get();
//                l.setCoverURL(art);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            } catch (ExecutionException e) {
//                e.printStackTrace();
//            }
            lyricsArrayList.add(lyricses[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            lyricListAdapter.notifyDataSetChanged();
            rotateLoading.stop();
            rotateLoading.setVisibility(View.INVISIBLE);
        }
    }

    private class UpdateMyLyrics extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            rotateLoading.setVisibility(View.VISIBLE);
            rotateLoading.start();
        }

        @Override
        protected Void doInBackground(String... strings) {
            arrayList.add(strings[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            lvLyrics.setVisibility(View.VISIBLE);
            if (null != lyricAdapter) {
                lyricAdapter.notifyDataSetChanged();
            } else {
                lyricAdapter = new LyricAdapter(ctx, arrayList);
                lvLyrics.setAdapter(lyricAdapter);
            }
            rotateLoading.stop();
            rotateLoading.setVisibility(View.INVISIBLE);
        }
    }

    private class CoverArtLoader extends AsyncTask<Object, Object, String> {

        @Override
        protected String doInBackground(Object... objects) {
            Lyrics lyrics = (Lyrics) objects[0];
            String url = lyrics.getCoverURL();

            if (url == null) {
                try {
                    String requestURL = String.format(
                            "https://itunes.apple.com/search?term=%s+%s&entity=song&media=music",
                            URLEncoder.encode(lyrics.getArtist(), "UTF-8"),
                            URLEncoder.encode(lyrics.getTrack(), "UTF-8"));
                    String txt = Net.getUrlAsString(new URL(requestURL));
                    JSONObject json = new JSONObject(txt);
                    JSONArray results = json.getJSONArray("results");
                    JSONObject result = results.getJSONObject(0);
                    url = result.getString("artworkUrl60").replace("60x60bb.jpg", "600x600bb.jpg");
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException ignored) {
                    url = null;
                }
            }
            return url;
        }

        @Override
        protected void onPostExecute(String url) {
        }
    }

    class LrcTask extends TimerTask {

        long beginTime = -1;

        @Override
        public void run() {
            if (beginTime == -1) {
                beginTime = System.currentTimeMillis();
            }

            if (MediaPlayerService.mMediaPlayer != null) {
                final long timePassed = MediaPlayerService.mMediaPlayer.getCurrentPosition();

                try {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {

                            public void run() {
                                mLrcView.seekLrcToTime(timePassed);
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class LyricReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            try {

                if (intent != null) {
                    String action = intent.getAction();
                    ToolsHelper.log("ACTION_LYRICS", action);

                    if (action.equals(ConstantHelper.UPDATE_LYRICS)) {
                        if (lyricsArrayList.size() > 0) {
                            lyricsArrayList.clear();
                        }
                        lyricListAdapter.notifyDataSetChanged();

                        stopLrcPlay();
                        updateLyric();
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
