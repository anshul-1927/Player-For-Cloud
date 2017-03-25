package dev.datvt.cloudtracks.song_player;

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
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.alelak.soundroid.models.Track;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Locale;

import dev.datvt.cloudtracks.MainActivity;
import dev.datvt.cloudtracks.R;
import dev.datvt.cloudtracks.RootActivity;
import dev.datvt.cloudtracks.noti_service.MediaPlayerService;
import dev.datvt.cloudtracks.theme.ChangeTheme;
import dev.datvt.cloudtracks.utils.ConstantHelper;
import dev.datvt.cloudtracks.utils.ToolsHelper;
import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;
import me.relex.circleindicator.CircleIndicator;

public class PlayMusicActivity extends RootActivity implements View.OnClickListener
        , SongListFragment.OnListFragmentInteractionListener {

    public static ArrayList<Track> tracks;
    public static Track curTrack;
    public static int curPos = 0;
    public static Context ctx;
    public static int DUR = 100;
    public static Handler mHandler = new Handler();
    public static ImageView pp;
    public static SeekBar seek;
    public static TextView tol;
    public static TextView cur;
    public static MediaPlayer mp;
    public static String loadedTitle = "";
    public static SmoothProgressBar loadingUpdate;

    private InterstitialAd interstitialAd;
    private AdRequest adRequestFull;

    private void requestNewInterstitial() {
        adRequestFull = new AdRequest.Builder()
//                .addTestDevice("12616862D8B293769AAA7C7D2B71CC74")
                .build();

        interstitialAd.loadAd(adRequestFull);
    }

    private void showFullAds() {
        if (interstitialAd != null && interstitialAd.isLoaded()) {
            interstitialAd.show();
        }
    }

    /**
     * Background Runnable thread
     */
    public static Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            if (mp == null) {
                Log.d("MP", "NULL");
            }
            try {
                if (mp != null && MediaPlayerService.mMediaPlayer != null) {
                    if (mp.isPlaying() || MediaPlayerService.mMediaPlayer.isPlaying()) {
                        pp.setImageResource(R.drawable.custom_pause);
                    } else {
                        updateProgressBar();
                        pp.setImageResource(R.drawable.custom_play);
                    }

                    if (MediaPlayerService.mMediaPlayer.isPlaying()) {
                        long totalDuration = MediaPlayerService.mMediaPlayer.getDuration();
                        long currentDuration = MediaPlayerService.mMediaPlayer.getCurrentPosition();
                        tol.setText("" + ToolsHelper.milliSecondsToTimer(totalDuration));
                        cur.setText("" + ToolsHelper.milliSecondsToTimer(currentDuration));
                        int progress = ToolsHelper.getProgressPercentage(currentDuration, totalDuration);
                        seek.setProgress(progress);
                        mHandler.postDelayed(this, DUR);
                    }

                    if (curTrack.artwork_url != null) {
                        if (curTrack.bpm == ToolsHelper.IS_LOCAL && !loadedTitle.equalsIgnoreCase(curTrack.title)) {
                            Uri ur = Uri.parse(curTrack.artwork_url);
                            Picasso.with(ctx).load(ur).placeholder(R.drawable.icon_disc_blue).into(PlayFragment.imageView);
                            loadedTitle = curTrack.title;
                        } else if (!loadedTitle.equalsIgnoreCase(curTrack.title)) {
                            Picasso.with(ctx).load(curTrack.artwork_url.replace("large", "t500x500"))
                                    .placeholder(R.drawable.icon_disc_blue).into(PlayFragment.imageView);
                        }
                    } else {
                        PlayFragment.imageView.setImageResource(R.drawable.icon_disc_blue);
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    private int SHUF = 982, FLO = 433, REP = 238;
    private int repeat = REP;
    private ImageView equalizerP, rep, next, prev, share, volume, ivTheme;
    private boolean isVolume = true;
    private boolean isStop = true;
    private int indexBackGround;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private LinearLayout linearLayout;
    private Locale myLocale;
    private BroadcastReceiver broadcastReceiver;
    private AudioManager audioManager;
    private boolean isPlaying = false;
    private ViewPager viewPager;
    private CircleIndicator indicator;
    private PlayMusicApdater playMusicApdater;
    private ImageView btnClose;

    public static void updateProgressBar() {
        if (mHandler != null) {
            mHandler.removeCallbacks(mUpdateTimeTask);
        }
        mHandler = new Handler();
        if (mp != null && MediaPlayerService.mMediaPlayer != null) {
            if (mp.isPlaying() || MediaPlayerService.mMediaPlayer.isPlaying()) {
                Log.d("HAND", "RUNNING");
                mHandler.postDelayed(mUpdateTimeTask, DUR);
            }
        }
    }

    public void updatePlaying() {
        try {
            if (mp != null) {
                if (mp.isPlaying()) {
                    mHandler.postDelayed(mUpdateTimeTask, DUR);
                    pp.setImageResource(R.drawable.custom_pause);
                } else {
                    pp.setImageResource(R.drawable.custom_play);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.play_music_activity);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorBlack));
        }

        MobileAds.initialize(getApplicationContext(), getResources().getString(R.string.app_id));
        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId(getResources().getString(R.string.ad_unit_full));
        requestNewInterstitial();

        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                requestNewInterstitial();
            }
        });

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


        viewPager = (ViewPager) findViewById(R.id.viewPager);
        indicator = (CircleIndicator) findViewById(R.id.indicator);
        playMusicApdater = new PlayMusicApdater(getSupportFragmentManager());
        viewPager.setAdapter(playMusicApdater);
        indicator.setViewPager(viewPager);
        viewPager.setCurrentItem(1);

        indicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                viewPager.setCurrentItem(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        ctx = getApplicationContext();
        if (MainActivity.mMediaPlayer != null) {
            mp = MainActivity.mMediaPlayer;
        } else {
            if (MediaPlayerService.mMediaPlayer != null) {
                mp = MediaPlayerService.mMediaPlayer;
            }
        }
        tracks = new ArrayList<>();
        audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);

        Intent intent = getIntent();
        if (intent != null) {
            curPos = intent.getIntExtra("pos", 0);
            repeat = intent.getIntExtra("rep", REP);
            isPlaying = intent.getBooleanExtra("isPlay", isPlaying);
            isStop = intent.getBooleanExtra("isStop", isStop);
            Log.d("isPLAY", isPlaying + "");
            Log.d("CUR_POS", curPos + "");
        }

        initiate();

        if (MainActivity.tracks != null) {
            tracks = MainActivity.tracks;

            if (tracks.size() > curPos) {
                curTrack = tracks.get(curPos);

                if (curTrack.bpm != ToolsHelper.IS_LOCAL && isStop) {
                    loadingUpdate.setVisibility(View.VISIBLE);
                    loadingUpdate.progressiveStart();
                }
            }
        }


        if (isPlaying) {
            Log.d("OPEN", "RUNNING");
            isStop = false;
            if (mHandler == null) {
                Log.d("HANDLE", "NULL");
            }
            mHandler.postDelayed(mUpdateTimeTask, DUR);
            pp.setImageResource(R.drawable.custom_pause);
        } else {
            Log.d("OPEN", "PAUSE");
            mHandler.postDelayed(mUpdateTimeTask, DUR);
            pp.setImageResource(R.drawable.custom_play);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        editor.putBoolean("isAlive", true);
        editor.commit();
    }

    @Override
    public void onDestroy() {
        this.mHandler.removeCallbacks(mUpdateTimeTask);
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
        }
        editor.putInt("theme", indexBackGround);
        editor.putBoolean("isAlive", false);
        editor.commit();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        editor.putInt("index", curPos);
        editor.putInt("view_index", viewPager.getCurrentItem());
        editor.commit();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            showFullAds();
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("pos", curPos);
            setResult(ConstantHelper.RESULT_CODE_SHOW, intent);
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ConstantHelper.REQUEST_CODE_THEME && data != null) {
            if (resultCode == ConstantHelper.RESULT_CODE_THEME) {
                Log.d("PLAY_MUSIC", "onActivityResult");
                int index = data.getIntExtra("index", 0);
                indexBackGround = index;
                if (indexBackGround != 0) {
                    linearLayout.setBackgroundResource(indexBackGround);
                }

                curPos = sharedPreferences.getInt("index", 0);
                if (curPos < tracks.size()) {
                    curTrack = tracks.get(curPos);
                    updatePlaying();
                }


                viewPager.setCurrentItem(sharedPreferences.getInt("view_index", 0));
            }
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        switch (id) {
            case R.id.ivClose:
                showFullAds();
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("pos", curPos);
                intent.putExtra("isPlay", isPlaying);
                intent.putExtra("isStop", isStop);
                Log.d("POS_1", curPos + "");
                setResult(ConstantHelper.RESULT_CODE_SHOW, intent);
                finish();
                break;
            case R.id.ivChangeTheme:
                Intent intentTheme = new Intent(this, ChangeTheme.class);
                intentTheme.putExtra("theme", indexBackGround);
                startActivityForResult(intentTheme, ConstantHelper.REQUEST_CODE_THEME);
                break;
            case R.id.equalizerP:
                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setMessage(getString(R.string.noti_action_equalizer));

                alertDialogBuilder.setPositiveButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

                alertDialogBuilder.setNegativeButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        openMusicEqualizer(PlayMusicActivity.this, "music.equalizer.bassbooster.eq");
                    }
                });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
                break;
            case R.id.ivVolume:
                offSound();
                break;
            case R.id.ivShare:
                shareSong();
                break;
            case R.id.ppP:
                play();
                break;
            case R.id.nextP:
                next();
                break;
            case R.id.prevP:
                previous();
                break;
            case R.id.repP:
                repeatPlay();
                break;
        }
    }

    private void initiate() {
        pp = (ImageView) findViewById(R.id.ppP);
        equalizerP = (ImageView) findViewById(R.id.equalizerP);
        share = (ImageView) findViewById(R.id.ivShare);
        volume = (ImageView) findViewById(R.id.ivVolume);
        rep = (ImageView) findViewById(R.id.repP);
        next = (ImageView) findViewById(R.id.nextP);
        prev = (ImageView) findViewById(R.id.prevP);
        seek = (SeekBar) findViewById(R.id.seekP);
        cur = (TextView) findViewById(R.id.cur);
        tol = (TextView) findViewById(R.id.tot);
        btnClose = (ImageView) findViewById(R.id.ivClose);
        linearLayout = (LinearLayout) findViewById(R.id.main_layout);
        ivTheme = (ImageView) findViewById(R.id.ivChangeTheme);

        loadingUpdate = (SmoothProgressBar) findViewById(R.id.loadingUpdate);
        loadingUpdate.progressiveStop();
        loadingUpdate.setVisibility(View.GONE);

        sharedPreferences = getSharedPreferences("save_cloud", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        indexBackGround = sharedPreferences.getInt("theme", R.drawable.background_0);
        linearLayout.setBackgroundResource(indexBackGround);

        repeat = sharedPreferences.getInt("repeat", REP);
        if (repeat == REP) {
            rep.setImageResource(R.drawable.custom_repeat_all);
        } else if (repeat == SHUF) {
            rep.setImageResource(R.drawable.custom_shuffle);
        } else if (repeat == FLO) {
            rep.setImageResource(R.drawable.custom_repeat_single);
        }
        Log.d("REPEAT", repeat + "");

        Intent intentRep = new Intent(this, MediaPlayerService.class);
        intentRep.setAction(ConstantHelper.ACTION_REP);
        intentRep.putExtra("rep", repeat);
        startService(intentRep);

        seek.setProgress(0);
        seek.setMax(100);
        updateProgressBar();

        if (curTrack != null) {
            tol.setText(ToolsHelper.milliSecondsToTimer(curTrack.duration));
        }

        pp.setOnClickListener(this);
        next.setOnClickListener(this);
        prev.setOnClickListener(this);
        rep.setOnClickListener(this);
        equalizerP.setOnClickListener(this);
        share.setOnClickListener(this);
        volume.setOnClickListener(this);
        btnClose.setOnClickListener(this);
        ivTheme.setOnClickListener(this);

        updatePlaying();

        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mHandler.removeCallbacks(mUpdateTimeTask);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mHandler.removeCallbacks(mUpdateTimeTask);
                int totalDuration = mp.getDuration();
                int currentPosition = ToolsHelper.progressToTimer(seekBar.getProgress(), totalDuration);
                mp.seekTo(currentPosition);
                updateProgressBar();
            }
        });

    }

    private void play() {
        if (curTrack != null && !curTrack.stream_url.isEmpty()) {
            SongListFragment.setUpList(tracks);

            Log.d("ISPLAY", isPlaying + "");
            if (isPlaying) {
                if (mp.isPlaying() || MediaPlayerService.mMediaPlayer.isPlaying()) {
                    Intent i = new Intent(this, MediaPlayerService.class);
                    if (!MediaPlayerService.isNext) {
                        PlayFragment.imageView.clearAnimation();
                        i.setAction(ConstantHelper.ACTION_PAUSE);
                        isPlaying = false;
                    }
                    startService(i);
                } else {
                    Intent i = new Intent(this, MediaPlayerService.class);
                    if (!MediaPlayerService.isNext) {
                        i.setAction(ConstantHelper.ACTION_RESUME);
                        isPlaying = true;
                    }
                    startService(i);
                }
            } else {
                if (curTrack.bpm == ToolsHelper.IS_LOCAL) {
                    Intent intent = new Intent(this, MediaPlayerService.class);

                    if (isStop) {
                        if (!MediaPlayerService.isNext) {
                            intent.setAction(ConstantHelper.ACTION_PLAY);
                            intent.putExtra("path", curTrack.stream_url);
                            intent.putExtra("pos", curPos);
                            isStop = false;
                            isPlaying = true;
                        }
                    } else {
                        if (!MediaPlayerService.isNext) {
                            intent.setAction(ConstantHelper.ACTION_RESUME);
                            isPlaying = true;
                        }
                    }
                    startService(intent);
                } else {
                    Intent intent = new Intent(this, MediaPlayerService.class);

                    if (isStop) {
                        if (ToolsHelper.hasConnection(PlayMusicActivity.this)) {

                            if (!MediaPlayerService.isNext) {
                                loadingUpdate.setVisibility(View.VISIBLE);
                                loadingUpdate.progressiveStart();
                                intent.setAction(ConstantHelper.ACTION_PLAY);
                                intent.putExtra("path", curTrack.stream_url);
                                intent.putExtra("pos", curPos);
                                isPlaying = true;
                                isStop = false;
                            }
                        } else {
                            ToolsHelper.toast(getApplicationContext(), getString(R.string.noti_play_this_song));
                        }
                    } else {
                        if (!MediaPlayerService.isNext) {
                            intent.setAction(ConstantHelper.ACTION_RESUME);
                            isPlaying = true;
                        }
                    }
                    startService(intent);

                }
            }
        } else {
            ToolsHelper.toast(getBaseContext(), getString(R.string.noti_choose_song));
        }
    }

    private void setUpPlay() {
        PlayFragment.imageView.clearAnimation();
        mHandler.removeCallbacks(mUpdateTimeTask);

        if (curTrack.artwork_url != null) {
            if (curTrack.bpm == ToolsHelper.IS_LOCAL) {
                Uri ur = Uri.parse(curTrack.artwork_url);
                Picasso.with(ctx).load(ur)
                        .placeholder(R.drawable.icon_disc_blue).into(PlayFragment.imageView);
            } else {
                Picasso.with(ctx).load(curTrack.artwork_url.replace("large", "t500x500"))
                        .placeholder(R.drawable.icon_disc_blue).into(PlayFragment.imageView);
            }
        } else {
            PlayFragment.imageView.setImageResource(R.drawable.icon_disc_blue);
        }

        PlayFragment.songName.setText(curTrack.title);
        PlayFragment.singer.setText(curTrack.genre);

        if (curTrack.bpm == ToolsHelper.IS_LOCAL) {
            PlayFragment.singer.setText(curTrack.license);
        }

        mHandler.postDelayed(mUpdateTimeTask, DUR);

        PlayFragment.imageView.startAnimation(AnimationUtils.loadAnimation(getBaseContext(),
                R.anim.animation_rotate));
    }

    private void next() {
        MediaPlayerService.isNext = true;
        if (tracks != null && tracks.size() > 0) {
            if (curPos < tracks.size() - 1) {
                curPos++;
                curTrack = tracks.get(curPos);
                SongListFragment.setUpList(tracks);
                if (curTrack.bpm == ToolsHelper.IS_LOCAL) {

                    Intent i = new Intent(this, MediaPlayerService.class);
                    i.putExtra("path", curTrack.stream_url);
                    i.putExtra("pos", curPos);
                    i.setAction(ConstantHelper.ACTION_NEXT);
                    startService(i);

                    setUpPlay();

                    Intent intent = new Intent(ConstantHelper.UPDATE_LYRICS);
                    sendBroadcast(intent);
                } else {
                    if (ToolsHelper.hasConnection(PlayMusicActivity.this)) {
                        loadingUpdate.setVisibility(View.VISIBLE);
                        loadingUpdate.progressiveStart();

                        Intent i = new Intent(this, MediaPlayerService.class);
                        i.putExtra("path", curTrack.stream_url);
                        i.putExtra("pos", curPos);
                        i.setAction(ConstantHelper.ACTION_NEXT);
                        startService(i);

                        setUpPlay();

                        Intent intent = new Intent(ConstantHelper.UPDATE_LYRICS);
                        sendBroadcast(intent);
                    } else {
                        curPos--;
                        curTrack = tracks.get(curPos);
                        ToolsHelper.toast(getApplicationContext(), getString(R.string.noti_play_next_song));
                    }
                }


            } else {
                ToolsHelper.toast(getBaseContext(), getString(R.string.noti_song_end));
            }
        } else {
            ToolsHelper.toast(getBaseContext(), getString(R.string.empty_playlist));
        }
    }

    private void previous() {
        MediaPlayerService.isNext = true;
        if (tracks != null && tracks.size() > 0) {
            if (curPos > 0) {
                curPos--;
                curTrack = tracks.get(curPos);
                SongListFragment.setUpList(tracks);
                if (curTrack.bpm == ToolsHelper.IS_LOCAL) {

                    Intent i = new Intent(this, MediaPlayerService.class);
                    i.putExtra("path", curTrack.stream_url);
                    i.putExtra("pos", curPos);
                    i.setAction(ConstantHelper.ACTION_PREV);
                    startService(i);

                    setUpPlay();

                    Intent intent = new Intent(ConstantHelper.UPDATE_LYRICS);
                    sendBroadcast(intent);
                } else {
                    if (ToolsHelper.hasConnection(PlayMusicActivity.this)) {
                        loadingUpdate.setVisibility(View.VISIBLE);
                        loadingUpdate.progressiveStart();

                        Intent i = new Intent(this, MediaPlayerService.class);
                        i.putExtra("path", curTrack.stream_url);
                        i.putExtra("pos", curPos);
                        i.setAction(ConstantHelper.ACTION_PREV);
                        startService(i);

                        setUpPlay();

                        Intent intent = new Intent(ConstantHelper.UPDATE_LYRICS);
                        sendBroadcast(intent);
                    } else {
                        curPos++;
                        curTrack = tracks.get(curPos);
                        ToolsHelper.toast(getApplicationContext(), getString(R.string.noti_play_previous_song));
                    }
                }

            } else {
                ToolsHelper.toast(getBaseContext(), getString(R.string.noti_song_begin));
            }
        } else {
            ToolsHelper.toast(getBaseContext(), getString(R.string.empty_playlist));
        }
    }

    private void repeatPlay() {
        Intent intent = new Intent(this, MediaPlayerService.class);
        intent.setAction(ConstantHelper.ACTION_REP);
        if (repeat == FLO) {
            rep.setImageResource(R.drawable.custom_repeat_all);
            repeat = REP;
            intent.putExtra("rep", repeat);
            ToolsHelper.toast(getBaseContext(), getString(R.string.repeat_all));
        } else if (repeat == REP) {
            rep.setImageResource(R.drawable.custom_shuffle);
            repeat = SHUF;
            intent.putExtra("rep", repeat);
            ToolsHelper.toast(getBaseContext(), getString(R.string.shuffle));
        } else if (repeat == SHUF) {
            rep.setImageResource(R.drawable.custom_repeat_single);
            repeat = FLO;
            intent.putExtra("rep", repeat);
            ToolsHelper.toast(getBaseContext(), getString(R.string.repeat_current));
        }
        editor.putInt("repeat", repeat);
        editor.commit();
        Log.d("REPEAT_COMMIT", repeat + "");
        startService(intent);
    }

    private void shareSong() {
        String shareBody = "Hi ! Check out this awesome song : "
                + MainActivity.curTrack.title + " \nStreaming URL :" + MainActivity.curTrack.stream_url;
        final Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Stream URL");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(sharingIntent, ("Share")));
    }

    private void offSound() {
        if (isVolume) {
            volume.setImageResource(R.drawable.custom_sound_off);
            audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
            isVolume = false;
            ToolsHelper.toast(getApplicationContext(), getString(R.string.sound_off));
        } else {
            volume.setImageResource(R.drawable.custom_sound_on);
            audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
            isVolume = true;
            ToolsHelper.toast(getApplicationContext(), getString(R.string.sound_on));
        }
    }

    private void openMusicEqualizer(Context context, final String packageName) {
        final Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        if (intent == null) {
            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setMessage(getString(R.string.noti_download_equalizer));

            alertDialogBuilder.setPositiveButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });

            alertDialogBuilder.setNegativeButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    onGoToAnotherInAppStore(intent, packageName);
                }
            });

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    private void onGoToAnotherInAppStore(Intent intent, String appPackageName) {
        try {
            intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setData(Uri.parse("market://details?id=" + appPackageName));
            startActivity(intent);
        } catch (android.content.ActivityNotFoundException anfe) {
            intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setData(Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName));
            startActivity(intent);
        }

    }


    @Override
    public void onListFragmentInteraction(ArrayList<Track> items, Track item, int pos, int code) {


        if (code == ToolsHelper.IS_LOCAL) {
            curPos = pos;
            tracks = items;
            curTrack = item;

            SongListFragment.setUpList(items);

            PlayFragment.imageView.clearAnimation();

            String url = curTrack.stream_url;

            PlayFragment.songName.setText(curTrack.title);
            PlayFragment.singer.setText(curTrack.genre);

            if (curTrack.bpm == ToolsHelper.IS_LOCAL) {
                PlayFragment.singer.setText(curTrack.license);
            }

            if (curTrack.artwork_url != null) {
                if (curTrack.bpm == ToolsHelper.IS_LOCAL) {
                    Uri ur = Uri.parse(MainActivity.curTrack.artwork_url);
                    Picasso.with(ctx).load(ur).into(PlayFragment.imageView);
                } else {
                    Picasso.with(ctx).load(curTrack.artwork_url.replace("large", "t500x500"))
                            .placeholder(R.drawable.icon_disc_blue).into(PlayFragment.imageView);
                }
            } else {
                PlayFragment.imageView.setImageResource(R.drawable.icon_disc_blue);
            }

            PlayFragment.imageView.startAnimation(AnimationUtils.loadAnimation(getBaseContext(),
                    R.anim.animation_rotate));

            Intent intent = new Intent(this, MediaPlayerService.class);
            intent.setAction(ConstantHelper.ACTION_PLAY);
            intent.putExtra("pos", curPos);
            intent.putExtra("path", url);
            startService(intent);

            mHandler.removeCallbacks(mUpdateTimeTask);
            mHandler.postDelayed(mUpdateTimeTask, DUR);

            Intent intent2 = new Intent(ConstantHelper.UPDATE_LYRICS);
            sendBroadcast(intent2);


        } else if (code == ToolsHelper.STREAM_CODE) {
            if (ToolsHelper.hasConnection(PlayMusicActivity.this)) {

                loadingUpdate.setVisibility(View.VISIBLE);
                loadingUpdate.progressiveStart();

                curPos = pos;
                tracks = items;
                curTrack = item;

                SongListFragment.setUpList(items);

                PlayFragment.imageView.clearAnimation();

                String url = curTrack.stream_url;

                PlayFragment.songName.setText(curTrack.title);
                PlayFragment.singer.setText(curTrack.genre);

                if (curTrack.bpm == ToolsHelper.IS_LOCAL) {
                    PlayFragment.singer.setText(curTrack.license);
                }

                if (curTrack.artwork_url != null) {
                    if (curTrack.bpm == ToolsHelper.IS_LOCAL) {
                        Uri ur = Uri.parse(MainActivity.curTrack.artwork_url);
                        Picasso.with(ctx).load(ur).into(PlayFragment.imageView);
                    } else {
                        Picasso.with(ctx).load(curTrack.artwork_url.replace("large", "t500x500"))
                                .placeholder(R.drawable.icon_disc_blue).into(PlayFragment.imageView);
                    }
                } else {
                    PlayFragment.imageView.setImageResource(R.drawable.icon_disc_blue);
                }

                PlayFragment.imageView.startAnimation(AnimationUtils.loadAnimation(getBaseContext(),
                        R.anim.animation_rotate));

                Intent intent = new Intent(this, MediaPlayerService.class);
                intent.setAction(ConstantHelper.ACTION_PLAY);
                intent.putExtra("pos", curPos);
                intent.putExtra("path", url);
                startService(intent);

                mHandler.removeCallbacks(mUpdateTimeTask);
                mHandler.postDelayed(mUpdateTimeTask, DUR);
                pp.setImageResource(R.drawable.custom_pause);

                Intent intent2 = new Intent(ConstantHelper.UPDATE_LYRICS);
                sendBroadcast(intent2);

            } else {
                ToolsHelper.toast(getApplicationContext(), getString(R.string.noti_play_this_song));
            }
        }

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
                        if (action.equals(ConstantHelper.PAUSE_SONG)) {
                            pauseSong();
                            SongListFragment.setUpList(tracks);
                        } else if (action.equals(ConstantHelper.RESUME_SONG)) {
                            resumeSong();
                            SongListFragment.setUpList(tracks);
                        } else if (action.equals(ConstantHelper.NEXT_SONG)) {
                            loadingUpdate.setVisibility(View.VISIBLE);
                            loadingUpdate.progressiveStart();

                            curPos = intent.getIntExtra("pos", 0);
                            if (tracks != null && tracks.size() > curPos) {
                                curTrack = tracks.get(curPos);
                            }
                            updatePlaying();
                            pp.setImageResource(R.drawable.custom_pause);

                            Intent intent2 = new Intent(ConstantHelper.UPDATE_LYRICS);
                            sendBroadcast(intent2);
                            SongListFragment.setUpList(tracks);
                        } else if (action.equals(ConstantHelper.ACTION_COMPLETE_MUSIC)) {

                            curPos = intent.getIntExtra("pos", 0);
                            if (tracks != null && tracks.size() > curPos) {
                                curTrack = tracks.get(curPos);
                            }

                            if (curTrack.bpm != ToolsHelper.IS_LOCAL) {
                                loadingUpdate.setVisibility(View.VISIBLE);
                                loadingUpdate.progressiveStart();

                            }
                            updatePlaying();
                            boolean wifi = intent.getBooleanExtra("wifi", false);

                            if (wifi) {
                                isPlaying = false;
                                isStop = true;

                                ToolsHelper.toast(getApplicationContext(), getString(R.string.noti_play_next_song));
                            }

                            seek.setProgress(0);
                            if (curTrack != null) {
                                tol.setText(ToolsHelper.milliSecondsToTimer(curTrack.duration));
                            }
                            cur.setText("00:00");

                            Intent intent2 = new Intent(ConstantHelper.UPDATE_LYRICS);
                            sendBroadcast(intent2);

                            SongListFragment.setUpList(tracks);
                        } else if (action.equals(ConstantHelper.ACTION_CLOSE_NOTI)) {
                            PlayFragment.imageView.clearAnimation();
                            curPos = intent.getIntExtra("pos", 0);
                            if (tracks != null && tracks.size() > curPos) {
                                curTrack = tracks.get(curPos);
                            }
                            isPlaying = false;
                            isStop = false;
                            pp.setImageResource(R.drawable.custom_play);
                            SongListFragment.setUpList(tracks);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void pauseSong() {
            if (mp != null && mp.isPlaying()) {
                mp.pause();
            }
            pp.setImageResource(R.drawable.custom_play);
            isPlaying = false;
            PlayFragment.imageView.clearAnimation();
        }

        private void resumeSong() {
            if (mp != null) {
                mp.start();
            }
            pp.setImageResource(R.drawable.custom_pause);
            isPlaying = true;
            updateProgressBar();
            PlayFragment.imageView.startAnimation(AnimationUtils.loadAnimation(getBaseContext(),
                    R.anim.animation_rotate));
        }
    }
}
