package dev.datvt.cloudtracks;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alelak.soundroid.models.Track;
import com.google.android.gms.analytics.Tracker;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

import dev.datvt.cloudtracks.noti_service.MediaPlayerService;
import dev.datvt.cloudtracks.noti_service.NotificationUtil;
import dev.datvt.cloudtracks.song_player.PlayMusicActivity;
import dev.datvt.cloudtracks.sound_cloud.CloudSongFragment;
import dev.datvt.cloudtracks.sound_cloud.LocalTracksFragment;
import dev.datvt.cloudtracks.sound_cloud.MyViewPagerAdapter;
import dev.datvt.cloudtracks.sound_cloud.SearchTracksFragment;
import dev.datvt.cloudtracks.utils.ConstantHelper;
import dev.datvt.cloudtracks.utils.DownloadFileFromURL;
import dev.datvt.cloudtracks.utils.FileOperations;
import dev.datvt.cloudtracks.utils.ToolsHelper;
import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

public class MainActivity extends RootActivity implements LocalTracksFragment.onPlayListSelected,
        LocalTracksFragment.OnListFragmentInteractionListener,
        CloudSongFragment.OnListFragmentInteractionListener,
        SearchTracksFragment.OnListFragmentInteractionListener, NavigationView.OnNavigationItemSelectedListener {

    public static MediaPlayer mMediaPlayer = new MediaPlayer();
    public static String folder;
    public static int curPos = 0;
    public static Track curTrack;
    public static ArrayList<Track> tracks;
    public static SmoothProgressBar loading;
    public static ImageView btnPlay;
    public int SHUF = 982, FLO = 433, REP = 238;
    public int rep = REP;
    public ImageView art;
    public TextView title, subtitle;
    private TextView prog;
    private ImageView btnNext;
    private View tabPlayer;
    private boolean isPlaying = false;
    private boolean isStop = true;
    private ViewPager mViewPager;
    private TabLayout tabLayout;
    private MyViewPagerAdapter myViewPagerAdapter;
    private BroadcastReceiver broadcastReceiver;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private AudioManager audioManager;
    private DrawerLayout drawer;
    private Toolbar toolbar;
    private Locale myLocale;
    private RelativeLayout rlMain;

    private Tracker mTracker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.main);

        // Obtain the shared Tracker instance.
        MyApplication application = (MyApplication) getApplication();
        mTracker = application.getDefaultTracker();

        broadcastReceiver = new PlayerReciever();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConstantHelper.RESUME_SONG);
        filter.addAction(ConstantHelper.PAUSE_SONG);
        filter.addAction(ConstantHelper.NEXT_SONG);
        filter.addAction(ConstantHelper.PREVIOUS_SONG);
        filter.addAction(ConstantHelper.PLAY_SONG);
        filter.addAction(ConstantHelper.ACTION_COMPLETE_MUSIC);
        filter.addAction(ConstantHelper.ACTION_CLOSE_NOTI);
        registerReceiver(broadcastReceiver, filter);

        sharedPreferences = getSharedPreferences("save_cloud", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);

        rep = sharedPreferences.getInt("repeat", REP);

        Log.d("REPEAT_MAIN", rep + "");

        Intent intentRep = new Intent(this, MediaPlayerService.class);
        intentRep.setAction(ConstantHelper.ACTION_REP);
        intentRep.putExtra("rep", rep);
        startService(intentRep);

        initViewId();
        addEvents();

        recentPlay();

        boolean isNoti = sharedPreferences.getBoolean("isNoti", false);

        if (audioManager.isMusicActive() && isNoti) {
            isPlaying = true;
        } else {
            isPlaying = false;
        }

        Log.e("isPlay_Start", isPlaying + "");

        if (audioManager.isMusicActive() && isPlaying) {
            Log.e("SONG", "audioManager");
            curPos = NotificationUtil.position;
            if (MediaPlayerService.tracks != null) {

                tracks = MediaPlayerService.tracks;
                if (tracks.size() > curPos) {
                    curTrack = tracks.get(curPos);
                    updateSong();
                }
            }
            tabPlayer.setVisibility(View.VISIBLE);
            rlMain.setBackgroundResource(R.color.colorPrimary);
        }


    }

    private void initViewId() {
        if (mMediaPlayer == null) {
            Log.d("MEDIA", "NULL");
        }

        folder = Environment.getExternalStorageDirectory().getPath() + "/CloudMusic";
        tracks = new ArrayList<>();

        File zfol = new File(folder);
        if (!zfol.exists()) {
            zfol.mkdirs();
        }

        zfol = new File(MainActivity.folder + "/Playlist");
        if (!zfol.exists()) {
            zfol.mkdirs();
        }

        zfol = new File(MainActivity.folder + "/Lyrics");
        if (!zfol.exists()) {
            zfol.mkdirs();
        }

        zfol = new File(MainActivity.folder + "/Playlist/recent.lst");
        if (!zfol.exists()) {
            new FileOperations().write(MainActivity.folder + "/Playlist/recent.lst", "[]");
        }

        if (MyApplication.CAN_DOWNLOAD) {
            zfol = new File(MainActivity.folder + "/Playlist/download.lst");
            if (!zfol.exists()) {
                new FileOperations().write(MainActivity.folder + "/Playlist/download.lst", "[]");
            }
        }

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.nav_open_drawer, R.string.nav_close_drawer);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        btnPlay = (ImageView) findViewById(R.id.ivPlay);
        btnNext = (ImageView) findViewById(R.id.ivNextSong);
        art = (ImageView) findViewById(R.id.imgArt);
        title = (TextView) findViewById(R.id.tvSong);
        subtitle = (TextView) findViewById(R.id.tvGenre);
        prog = (TextView) findViewById(R.id.prog);
        prog.setVisibility(View.INVISIBLE);
        tabPlayer = findViewById(R.id.playTabSmall);
        rlMain = (RelativeLayout) findViewById(R.id.rlMain);

        loading = (SmoothProgressBar) findViewById(R.id.loadingH);
        loading.progressiveStop();
        loading.setVisibility(View.GONE);

        mViewPager = (ViewPager) findViewById(R.id.pager);
        myViewPagerAdapter = new MyViewPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(myViewPagerAdapter);

        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(mViewPager);

        tabLayout.getTabAt(0).setIcon(R.drawable.tab_cloud);
        tabLayout.getTabAt(1).setIcon(R.drawable.tab_search);
        tabLayout.getTabAt(2).setIcon(R.drawable.tab_local);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        mViewPager.setCurrentItem(0);
    }

    private void addEvents() {
        tabPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, PlayMusicActivity.class);
                intent.putExtra("pos", curPos);
                intent.putExtra("rep", rep);
                intent.putExtra("isPlay", isPlaying);
                intent.putExtra("isStop", isStop);
                Log.d("isPLAY", isPlaying + "");

                startActivityForResult(intent, ConstantHelper.REQUEST_CODE_SHOW);
            }
        });

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePlayPause();
            }
        });
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextSong();
            }
        });
    }

    public void downloadPlay(final Track track) {
        String url = track.stream_url;
        String path = ToolsHelper.folder + "/" + track.title + "_" + track.id + ".mp3";

        Log.d("PATH_DOWNLOAD", path);

        File mp = new File(path);
        if (mp.exists()) {
            showDialogAlive(path);
        } else {
            DownloadFileFromURL df = new DownloadFileFromURL(url, path) {

                @Override
                public void onStart() {
                    Log.d("TEG STARTED", "" + surl);
                    prog.setText("" + 0 + "%");
                    prog.setVisibility(View.VISIBLE);
                    loading.setVisibility(View.VISIBLE);
                    loading.progressiveStart();
                }

                @Override
                public void onUpdate(int progress) {
                    try {
                        prog.setText("" + progress + "%");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onComplete(String path) {
                    Log.d("TEG FINISHED", "" + path);
                    loading.progressiveStop();
                    prog.setVisibility(View.INVISIBLE);
                    loading.setVisibility(View.INVISIBLE);
                    ToolsHelper.toast(getBaseContext(), getString(R.string.noti_save) + " " + path);
                    showDialog(path);
                }
            };
            df.execute(url);
        }
    }

    public void localPlay(Track track) {
        Log.d("POSITION_2", curPos + "");
        play(track.stream_url);
    }

    private void recentPlay() {
        try {
            tracks = ToolsHelper.getPlayList(getBaseContext(), "recent.lst");
            curPos = 0;
            if (tracks != null && tracks.size() > 0) {
                curTrack = tracks.get(curPos);
                updateSong();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        Intent intent = getIntent();
        if (intent != null) {
            curPos = intent.getIntExtra("pos", curPos);
            curPos = NotificationUtil.position;
            if (MediaPlayerService.tracks != null) {
                Log.d("INTENT", "OPEN_BY_NOTI");
                tracks = MediaPlayerService.tracks;
                if (tracks.size() > curPos) {
                    curTrack = tracks.get(curPos);
                    updateSong();
                }
            }
            if (mMediaPlayer == null) {
                mMediaPlayer = new MediaPlayer();
            }
        }
        try {
            loading.progressiveStop();
            loading.setVisibility(View.INVISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        editor.putBoolean("isLive", true);
        editor.commit();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
        }
        editor.putBoolean("isLive", false);
        editor.commit();
        Intent intent = new Intent(this, MediaPlayerService.class);
        intent.setAction(ConstantHelper.ACTION_DESTROY_MAIN);
        startService(intent);
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            exitApp();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void showDialog(final String path) {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(getString(R.string.noti_song_download_success));

        alertDialogBuilder.setPositiveButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        alertDialogBuilder.setNegativeButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                play(path);
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void showDialogAlive(final String path) {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(getString(R.string.noti_song_dowmloaded));

        alertDialogBuilder.setPositiveButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        alertDialogBuilder.setNegativeButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                play(path);
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void exitApp() {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(getString(R.string.noti_exit_app));

        alertDialogBuilder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        alertDialogBuilder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent playIntent = new Intent(getBaseContext(), MediaPlayerService.class);
                playIntent.setAction(ConstantHelper.ACTION_CLOSE_NOTI);
                startService(playIntent);
                finish();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void shareApp() {
        try {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            String shareBody = "https://play.google.com/store/apps/details?id=" + getPackageName();
            intent.putExtra(Intent.EXTRA_SUBJECT, "Share app");
            intent.putExtra(Intent.EXTRA_TEXT, shareBody);
            startActivity(Intent.createChooser(intent, "Share"));
        } catch (Exception e) {
            e.getMessage();
        }
    }

    private void settingApp() {
        Intent intent = new Intent(MainActivity.this, SettingActivity.class);
        startActivity(intent);
    }

    public void loadLocale() {
        String langPref = "Language";
        SharedPreferences prefs = getSharedPreferences("CommonPrefs", Activity.MODE_PRIVATE);
        String language = prefs.getString(langPref, "en");
        myLocale = new Locale(language);
        Locale.setDefault(myLocale);
        Configuration config = new Configuration();
        config.locale = myLocale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());

    }

    @Override
    public void clickedPlayList(ArrayList<Track> items, Track item, int pos, int code) {
    }

    @Override
    public void onListFragmentInteraction(ArrayList<Track> items, Track item, int pos, int code) {
        Log.d("MAIN", "PLAY " + item.title);
        Log.d("MAIN_ITEMS SIZE", "" + items.size());
        Log.d("MAIN_ITEM POS", "" + pos);

        if (code == ToolsHelper.STREAM_CODE) {
            Log.d("MAIN", "STREAM");


            if (ToolsHelper.hasConnection(MainActivity.this)) {
                if (MediaPlayerService.mMediaPlayer != null && MediaPlayerService.mMediaPlayer.isPlaying()) {
                    MediaPlayerService.mMediaPlayer.stop();
                    MediaPlayerService.mMediaPlayer.reset();
                }

                rlMain.setBackgroundResource(R.color.colorPrimary);
                tabPlayer.setVisibility(View.VISIBLE);
                loading.setVisibility(View.VISIBLE);
                loading.progressiveStart();

                Log.d("MAIN", "STREAM_WIFI :  " + curPos);
                curTrack = item;
                curPos = pos;
                if (tracks.size() > 0) {
                    tracks.clear();
                }

                tracks.addAll(items);

                updateSong();
                isPlaying = false;
                isStop = true;
                togglePlayPause();


            } else {
                Log.d("MAIN", "STREAM_NOT_WIFI :  " + curPos);
                ToolsHelper.toast(getApplicationContext(), getString(R.string.noti_play_this_song));
                loading.progressiveStop();
                loading.setVisibility(View.INVISIBLE);
            }
        } else if (code == ToolsHelper.DOWNLOAD_CODE) {
            Log.d("MAIN", "DOWNLOAD");
            if (ToolsHelper.hasConnection(MainActivity.this)) {
                downloadPlay(item);
                rlMain.setBackgroundResource(R.color.colorPrimary);
                tabPlayer.setVisibility(View.VISIBLE);
                loading.setVisibility(View.VISIBLE);
                loading.progressiveStart();
            }
        } else if (code == ToolsHelper.IS_LOCAL) {
            if (MediaPlayerService.mMediaPlayer != null && MediaPlayerService.mMediaPlayer.isPlaying()) {
                MediaPlayerService.mMediaPlayer.stop();
                MediaPlayerService.mMediaPlayer.reset();
            }

            rlMain.setBackgroundResource(R.color.colorPrimary);
            tabPlayer.setVisibility(View.VISIBLE);
            loading.setVisibility(View.VISIBLE);
            loading.progressiveStart();

            curTrack = item;
            curPos = pos;

            if (tracks.size() > 0) {
                tracks.clear();
            }

            tracks.addAll(items);

            Log.d("MAIN", "LOCAL : " + curPos);
            localPlay(curTrack);

        }

    }

    private void togglePlayPause() {
        if (curTrack != null && !curTrack.stream_url.isEmpty()) {

            if (isPlaying) {
                Log.d("MAIN", "PLAY_PAUSE");
                Intent intent = new Intent(this, MediaPlayerService.class);
                if (!MediaPlayerService.isNext) {
                    intent.setAction(ConstantHelper.ACTION_PAUSE);
                    isPlaying = false;
                }
                startService(intent);
            } else {
                Intent intent = new Intent(this, MediaPlayerService.class);
                if (isStop) {
                    Intent intent2 = new Intent(MainActivity.this, PlayMusicActivity.class);
                    intent2.putExtra("isStop", isStop);

                    if (curTrack.bpm != ToolsHelper.IS_LOCAL) {
                        if (ToolsHelper.hasConnection(MainActivity.this)) {
                            loading.setVisibility(View.VISIBLE);
                            loading.progressiveStart();

                            Log.d("MAIN", "PLAY_STREAM");
                            if (!MediaPlayerService.isNext) {
                                intent.setAction(ConstantHelper.ACTION_PLAY);
                                intent.putExtra("pos", curPos);
                                intent.putExtra("path", curTrack.stream_url);
                                isPlaying = true;
                                isStop = false;
                            }
                            startService(intent);
                        } else {
                            loading.progressiveStop();
                            loading.setVisibility(View.INVISIBLE);
                            ToolsHelper.toast(getApplicationContext(), getString(R.string.noti_play_this_song));
                        }
                    } else {
//                        btnPlay.setImageResource(R.drawable.custom_pause);

                        Log.d("MAIN", "PLAY_LOCAL");
                        if (!MediaPlayerService.isNext) {
                            intent.setAction(ConstantHelper.ACTION_PLAY);
                            intent.putExtra("pos", curPos);
                            intent.putExtra("path", curTrack.stream_url);
                            isPlaying = true;
                            isStop = false;
                        }
                        startService(intent);
                    }

                    intent2.putExtra("pos", curPos);
                    intent2.putExtra("rep", rep);
                    intent2.putExtra("isPlay", isPlaying);
                    Log.d("isPLAY", isPlaying + "");

                    startActivityForResult(intent2, ConstantHelper.REQUEST_CODE_SHOW);
                } else {
                    Log.d("MAIN", "PLAY_RESUME");
//                    btnPlay.setImageResource(R.drawable.custom_pause);
                    if (!MediaPlayerService.isNext) {
                        intent.setAction(ConstantHelper.ACTION_RESUME);
                        isPlaying = true;
                    }
                    startService(intent);
                }

            }
        } else {
            ToolsHelper.toast(getBaseContext(), getString(R.string.noti_choose_song));
        }
    }

    private void updateSong() {
        if (curTrack != null) {
            if (!curTrack.stream_url.contains("?client_id=" + ConstantHelper.CLIENT_ID)
                    && curTrack.bpm != ToolsHelper.IS_LOCAL) {
                curTrack.stream_url = curTrack.stream_url + "?client_id=" + ConstantHelper.CLIENT_ID;
            }

            Log.d("CUR TITLE & GENDER", curTrack.title + "  " + curTrack.genre);

            title.setText(curTrack.title);
            subtitle.setText(curTrack.genre);

            if (curTrack.bpm == ToolsHelper.IS_LOCAL) {
                subtitle.setText(curTrack.license);
            }

            if (curTrack.artwork_url != null) {
                if (curTrack.bpm == ToolsHelper.IS_LOCAL) {
                    Uri uri = Uri.parse(curTrack.artwork_url);
                    Picasso.with(MainActivity.this).load(uri)
                            .placeholder(R.drawable.default_nhaccuatui).into(art);
                } else {
                    Picasso.with(MainActivity.this).load(curTrack.artwork_url)
                            .placeholder(R.drawable.default_nhaccuatui).into(art);
                }
            } else {
                art.setImageResource(R.drawable.default_nhaccuatui);
            }

            btnPlay.setImageResource(R.drawable.custom_play);

            if (mMediaPlayer != null && MediaPlayerService.mMediaPlayer != null) {
                if (mMediaPlayer.isPlaying() || MediaPlayerService.mMediaPlayer.isPlaying()) {
                    Log.d("MEDIA", "RUNNING");
                    btnPlay.setImageResource(R.drawable.custom_pause);
                    isPlaying = true;
                    isStop = false;
                }
                Log.d("MEDIA", "OPEN");
            }
        }
    }

    private void play(String path) {
        Log.d("POSITION_3", curPos + "");
        updateSong();
        isPlaying = true;
        isStop = false;

        Intent intent = new Intent(this, MediaPlayerService.class);
        intent.putExtra("path", path);
        intent.putExtra("pos", curPos);
        intent.setAction(ConstantHelper.ACTION_PLAY);
        startService(intent);

        Intent intent2 = new Intent(MainActivity.this, PlayMusicActivity.class);
        intent2.putExtra("pos", curPos);
        intent2.putExtra("rep", rep);
        intent2.putExtra("isPlay", isPlaying);
        intent2.putExtra("isStop", isStop);
        Log.d("isPLAY", isPlaying + "");

        startActivityForResult(intent2, ConstantHelper.REQUEST_CODE_SHOW);
    }

    private void nextSong() {
        MediaPlayerService.isNext = true;
        loading.setVisibility(View.VISIBLE);
        loading.progressiveStart();
        if (tracks != null && tracks.size() > 0) {
            if (curPos < tracks.size() - 1) {
                curPos++;
                curTrack = tracks.get(curPos);
                if (curTrack.bpm != ToolsHelper.IS_LOCAL) {
                    if (ToolsHelper.hasConnection(this)) {
                        updateSong();
                        String path = curTrack.stream_url;
                        Intent intent = new Intent(this, MediaPlayerService.class);
                        intent.setAction(ConstantHelper.ACTION_NEXT);
                        intent.putExtra("pos", curPos);
                        intent.putExtra("path", path);
                        startService(intent);
                    } else {
                        curPos--;
                        curTrack = tracks.get(curPos);
                        ToolsHelper.toast(getApplicationContext(), getString(R.string.noti_play_next_song));
                        loading.progressiveStop();
                        loading.setVisibility(View.INVISIBLE);
                    }
                } else {
                    updateSong();
                    String path = curTrack.stream_url;
                    Intent intent = new Intent(this, MediaPlayerService.class);
                    intent.setAction(ConstantHelper.ACTION_NEXT);
                    intent.putExtra("pos", curPos);
                    intent.putExtra("path", path);
                    startService(intent);
                }
            } else {
                ToolsHelper.toast(getBaseContext(), getString(R.string.noti_song_end));
                loading.progressiveStop();
                loading.setVisibility(View.INVISIBLE);
            }
        } else {
            ToolsHelper.toast(getBaseContext(), getString(R.string.noti_choose_song));
            loading.progressiveStop();
            loading.setVisibility(View.INVISIBLE);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ConstantHelper.REQUEST_CODE_SHOW && data != null) {
            if (resultCode == ConstantHelper.RESULT_CODE_SHOW) {
                rlMain.setBackgroundResource(R.color.colorPrimary);
                tabPlayer.setVisibility(View.VISIBLE);

                curPos = data.getIntExtra("pos", 0);
                isPlaying = data.getBooleanExtra("isPlay", false);
                isStop = data.getBooleanExtra("isStop", false);
                Log.d("POS", curPos + "");

                if (tracks != null && tracks.size() > curPos) {
                    curTrack = tracks.get(curPos);
                    updateSong();
                }
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.nav_cloud:
                mViewPager.setCurrentItem(0);
                item.setChecked(true);
                break;
            case R.id.nav_share:
                shareApp();
                item.setChecked(true);
                break;
            case R.id.nav_setting:
                settingApp();
                item.setChecked(true);
                break;
            case R.id.nav_exit:
                exitApp();
                item.setChecked(true);
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * BroadcastReceiver
     */
    private class PlayerReciever extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (intent != null) {
                    String action = intent.getAction();
                    if (!TextUtils.isEmpty(action)) {
                        Log.d("ACTION_RECEIVER", action);
                        if (action.equals(ConstantHelper.PLAY_SONG)) {
                            String path = intent.getStringExtra("path");
                            play(path);
                        } else if (action.equals(ConstantHelper.PAUSE_SONG)) {
                            pauseSong();
                        } else if (action.equals(ConstantHelper.RESUME_SONG)) {
                            resumeSong();
                        } else if (action.equals(ConstantHelper.NEXT_SONG)) {
                            curPos = intent.getIntExtra("pos", 0);
                            if (tracks != null && tracks.size() > curPos) {
                                curTrack = tracks.get(curPos);
                            }
                            updateSong();
                            btnPlay.setImageResource(R.drawable.custom_pause);
                        } else if (action.equals(ConstantHelper.ACTION_COMPLETE_MUSIC)) {
                            curPos = intent.getIntExtra("pos", 0);
                            if (tracks != null && tracks.size() > curPos) {
                                curTrack = tracks.get(curPos);
                            }
                            updateSong();
                            boolean wifi = intent.getBooleanExtra("wifi", false);

                            if (wifi) {
                                isPlaying = false;
                                isStop = true;
                                ToolsHelper.toast(getApplicationContext(), getString(R.string.noti_play_next_song));
                            }

                        } else if (action.equals(ConstantHelper.ACTION_CLOSE_NOTI)) {
                            curPos = intent.getIntExtra("pos", 0);
                            if (tracks != null && tracks.size() > curPos) {
                                curTrack = tracks.get(curPos);
                            }
                            isPlaying = false;
                            isStop = false;
                            btnPlay.setImageResource(R.drawable.custom_play);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void pauseSong() {
            if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
            }
            btnPlay.setImageResource(R.drawable.custom_play);
            isPlaying = false;
        }

        private void resumeSong() {
            if (mMediaPlayer != null) {
                mMediaPlayer.start();
            }
            btnPlay.setImageResource(R.drawable.custom_pause);
            isPlaying = true;
        }
    }

}
