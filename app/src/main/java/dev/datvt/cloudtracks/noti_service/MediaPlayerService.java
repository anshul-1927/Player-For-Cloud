package dev.datvt.cloudtracks.noti_service;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;

import com.alelak.soundroid.models.Track;

import java.util.ArrayList;
import java.util.Random;

import dev.datvt.cloudtracks.MainActivity;
import dev.datvt.cloudtracks.R;
import dev.datvt.cloudtracks.song_player.PlayFragment;
import dev.datvt.cloudtracks.song_player.PlayMusicActivity;
import dev.datvt.cloudtracks.utils.ConstantHelper;
import dev.datvt.cloudtracks.utils.ToolsHelper;

/**
 * Created by datvt on 8/3/2016.
 */
public class MediaPlayerService extends Service {

    public static MediaPlayer mMediaPlayer;
    public static ArrayList<Track> tracks;
    public static boolean isNext = false;
    private int play = 0;
    private NotificationUtil notificationUtil;
    private NotificationManager mNotifyMgr;
    private int curPos = 0;
    private int SHUF = 982, FLO = 433, REP = 238;
    private int repeat = REP;
    private Track curTrack;
    private Random random;
    private int index = 0;
    private SharedPreferences.Editor editor;
    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate() {
        super.onCreate();

        sharedPreferences = getSharedPreferences("save_cloud", MODE_PRIVATE);//
        editor = sharedPreferences.edit();

        notificationUtil = new NotificationUtil(getBaseContext());
        mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        random = new Random();

        if (MainActivity.mMediaPlayer != null) {
            mMediaPlayer = MainActivity.mMediaPlayer;
        }

        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
        }
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        curPos = MainActivity.curPos;
        tracks = MainActivity.tracks;

        if (tracks != null && curPos < tracks.size()) {
            curTrack = tracks.get(curPos);
        }

        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                ToolsHelper.log("onPrepared");
                if (MainActivity.loading != null) {
                    MainActivity.loading.setVisibility(View.INVISIBLE);
                    MainActivity.loading.progressiveStop();

                    if (MainActivity.btnPlay != null) {
                        MainActivity.btnPlay.setImageResource(R.drawable.custom_pause);
                    }
                }

                if (PlayMusicActivity.loadingUpdate != null) {
                    PlayMusicActivity.loadingUpdate.progressiveStop();
                    PlayMusicActivity.loadingUpdate.setVisibility(View.INVISIBLE);

                    if (PlayMusicActivity.pp != null) {
                        PlayMusicActivity.pp.setImageResource(R.drawable.custom_pause);
                    }

                }
                mMediaPlayer.start();
                isNext = false;

                if (curTrack != null && PlayFragment.imageView != null && PlayFragment.songName != null
                        && PlayFragment.singer != null) {
                    PlayFragment.songName.setText(curTrack.title);
                    PlayFragment.singer.setText(curTrack.genre);

                    if (curTrack.bpm == ToolsHelper.IS_LOCAL) {
                        PlayFragment.singer.setText(curTrack.license);
                    }
                    PlayFragment.imageView.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.animation_rotate));
                }

                if (PlayMusicActivity.mHandler != null) {
                    PlayMusicActivity.mHandler.removeCallbacks(PlayMusicActivity.mUpdateTimeTask);
                    PlayMusicActivity.mHandler.postDelayed(PlayMusicActivity.mUpdateTimeTask, PlayMusicActivity.DUR);
                }


                notificationUtil.bigViews.setImageViewResource(R.id.btnNotiPlay, R.drawable.btn_noti_pause);
                notificationUtil.remoteViews1.setImageViewResource(R.id.btnNotiPlay, R.drawable.btn_noti_pause);
                mNotifyMgr.notify(notificationUtil.mNotificationId, notificationUtil.showNotification(curTrack, curPos));
                startForeground(notificationUtil.mNotificationId, notificationUtil.showNotification(curTrack, curPos));
            }
        });

        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                try {
                    ToolsHelper.log("OnComplete");
                    if (MainActivity.loading != null) {
                        MainActivity.loading.setVisibility(View.VISIBLE);
                        MainActivity.loading.progressiveStart();
                    }


                    if (PlayFragment.imageView != null) {
                        PlayFragment.imageView.clearAnimation();
                    }

                    if (mp != null) {
                        mp.stop();
                        mp.reset();
                    }

                    if (MainActivity.btnPlay != null) {
                        MainActivity.btnPlay.setImageResource(R.drawable.custom_play);
                    }

                    if (PlayMusicActivity.pp != null) {
                        PlayMusicActivity.pp.setImageResource(R.drawable.custom_play);
                    }

                    index = curPos;

                    if (repeat == FLO) {
                        curTrack = tracks.get(curPos);
                    } else {
                        if (repeat == REP) {
                            curPos++;
                        } else if (repeat == SHUF) {
//                            curPos++;
                            if (tracks.size() > 0) {
                                curPos = random.nextInt(tracks.size());
                            }
//                            Collections.shuffle(tracks);
                        }

                        if (tracks.size() == curPos || tracks.size() < curPos) {
                            curPos = 0;
                        }

                        curTrack = tracks.get(curPos);
                    }
                    String path = curTrack.stream_url;

                    if (!path.contains("?client_id=" + ConstantHelper.CLIENT_ID)
                            && curTrack.bpm != ToolsHelper.IS_LOCAL)
                        path = path + "?client_id=" + ConstantHelper.CLIENT_ID;

                    Log.d("POSITION", curPos + " - " + index);

                    Intent intent = new Intent(ConstantHelper.ACTION_COMPLETE_MUSIC);

                    if (!ToolsHelper.hasConnection(getApplicationContext())) {
                        if (curTrack.bpm == ToolsHelper.IS_LOCAL) {
                            Log.d("POSITION", "LOCAL");
                            playSong(path);
                        } else {
                            Log.d("POSITION", "NO_LOCAL");
                            curPos = index;
                            if (curPos < tracks.size() && tracks.size() > 0) {
                                curTrack = tracks.get(curPos);
                            }

                            intent.putExtra("wifi", true);

//                            notificationUtil.bigViews.setImageViewResource(R.id.btnNotiPlay, R.drawable.btn_noti_play);
//                            notificationUtil.remoteViews1.setImageViewResource(R.id.btnNotiPlay, R.drawable.btn_noti_play);
//
//                            mNotifyMgr.notify(notificationUtil.mNotificationId, notificationUtil.showNotification(curTrack, curPos));
//                            startForeground(notificationUtil.mNotificationId, notificationUtil.showNotification(curTrack, curPos));

                            editor.putBoolean("isNoti", false);
                            editor.commit();

                            stopForeground(true);
                            stopSelf();
                        }
                    } else {
                        playSong(path);
                    }

                    intent.putExtra("pos", curPos);
                    sendBroadcast(intent);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        try {
            if (intent != null) {
                String action = intent.getAction();
                if (!TextUtils.isEmpty(action)) {
                    Log.d("ACTION", action);
                    boolean isLive = sharedPreferences.getBoolean("isLive", false);
                    boolean isAlive = sharedPreferences.getBoolean("isAlive", false);

                    if (action.equals(ConstantHelper.ACTION_PLAY)) {
                        isNext = true;
                        editor.putBoolean("isNoti", true);
                        editor.commit();

                        tracks = MainActivity.tracks;
                        String path = intent.getStringExtra("path");
                        curPos = intent.getIntExtra("pos", 0);
                        Log.d("POSITION_3", curPos + "");
                        if (tracks != null && curPos < tracks.size()) {
                            curTrack = tracks.get(curPos);
                        }

                        playSong(path);
                    } else if (action.equals(ConstantHelper.ACTION_STOP)) {
                        stopPlay();
                    } else if (action.equals(ConstantHelper.ACTION_NEXT)) {

                        editor.putBoolean("isNoti", true);
                        editor.commit();

                        String path = intent.getStringExtra("path");
                        curPos = intent.getIntExtra("pos", 0);
                        if (tracks.size() > curPos) {
                            curTrack = tracks.get(curPos);
                        }
                        notificationUtil.bigViews.setImageViewResource(R.id.btnNotiPlay, R.drawable.btn_noti_pause);
                        notificationUtil.remoteViews1.setImageViewResource(R.id.btnNotiPlay, R.drawable.btn_noti_pause);
                        nextPlay(path);
                    } else if (action.equals(ConstantHelper.ACTION_NEXT_NOTI)) {

                        isNext = true;
                        editor.putBoolean("isNoti", true);
                        editor.commit();

                        if (tracks != null && tracks.size() > 0) {
                            if (curPos < tracks.size() - 1) {
                                curPos++;
                            }
                            curTrack = tracks.get(curPos);

                            if (curTrack.bpm != ToolsHelper.IS_LOCAL) {
                                if (ToolsHelper.hasConnection(getApplicationContext())) {
                                    notificationUtil.bigViews.setImageViewResource(R.id.btnNotiPlay, R.drawable.btn_noti_pause);
                                    notificationUtil.remoteViews1.setImageViewResource(R.id.btnNotiPlay, R.drawable.btn_noti_pause);

                                    curTrack = tracks.get(curPos);
                                    nextPlay(curTrack.stream_url);
                                    Intent intent1 = new Intent(ConstantHelper.NEXT_SONG);
                                    intent1.putExtra("pos", curPos);
                                    sendBroadcast(intent1);
                                } else {

                                    curPos--;
                                    curTrack = tracks.get(curPos);
                                    ToolsHelper.toast(getApplicationContext(), getString(R.string.noti_play_next_song));
                                }
                            } else {
                                notificationUtil.bigViews.setImageViewResource(R.id.btnNotiPlay, R.drawable.btn_noti_pause);
                                notificationUtil.remoteViews1.setImageViewResource(R.id.btnNotiPlay, R.drawable.btn_noti_pause);

                                nextPlay(curTrack.stream_url);
                                Intent intent1 = new Intent(ConstantHelper.NEXT_SONG);
                                intent1.putExtra("pos", curPos);
                                sendBroadcast(intent1);
                            }
                        }
                        Log.d("CUR_2", curPos + "");
                    } else if (action.equals(ConstantHelper.ACTION_PAUSE)) {
                        Log.d("CUR", curPos + "");
                        editor.putBoolean("isNoti", true);
                        editor.commit();


                        Log.e("LIVE", isLive + " - " + isAlive);
                        if (isLive || isAlive) {

                            int tmp = intent.getIntExtra("pause", 0);
                            if (play < 2) {
                                play += tmp;
                            } else {
                                play = 1;
                            }

                            Log.d("PLAY", play + "");
                            if (play == 0) {
                                notificationUtil.bigViews.setImageViewResource(R.id.btnNotiPlay, R.drawable.btn_noti_play);
                                notificationUtil.remoteViews1.setImageViewResource(R.id.btnNotiPlay, R.drawable.btn_noti_play);
                                pausePlay();
                            } else if (play == 1 && mMediaPlayer.isPlaying()) {
                                notificationUtil.bigViews.setImageViewResource(R.id.btnNotiPlay, R.drawable.btn_noti_play);
                                notificationUtil.remoteViews1.setImageViewResource(R.id.btnNotiPlay, R.drawable.btn_noti_play);
                                pausePlay();
                                Intent intentService = new Intent();
                                intentService.setAction(ConstantHelper.PAUSE_SONG);
                                sendBroadcast(intentService);
                            } else if (play == 1 && !mMediaPlayer.isPlaying()) {
                                notificationUtil.bigViews.setImageViewResource(R.id.btnNotiPlay, R.drawable.btn_noti_pause);
                                notificationUtil.remoteViews1.setImageViewResource(R.id.btnNotiPlay, R.drawable.btn_noti_pause);
                                continuePlay();
                                Intent intentService = new Intent();
                                intentService.setAction(ConstantHelper.RESUME_SONG);
                                sendBroadcast(intentService);
                            } else if (play == 2 && !mMediaPlayer.isPlaying()) {
                                play = 0;
                                notificationUtil.bigViews.setImageViewResource(R.id.btnNotiPlay, R.drawable.btn_noti_pause);
                                notificationUtil.remoteViews1.setImageViewResource(R.id.btnNotiPlay, R.drawable.btn_noti_pause);
                                continuePlay();
                                Intent intentService = new Intent();
                                intentService.setAction(ConstantHelper.RESUME_SONG);
                                sendBroadcast(intentService);
                            } else if (play == 2 && mMediaPlayer.isPlaying()) {
                                play = 0;
                                notificationUtil.bigViews.setImageViewResource(R.id.btnNotiPlay, R.drawable.btn_noti_play);
                                notificationUtil.remoteViews1.setImageViewResource(R.id.btnNotiPlay, R.drawable.btn_noti_play);
                                pausePlay();
                                Intent intentService = new Intent();
                                intentService.setAction(ConstantHelper.PAUSE_SONG);
                                sendBroadcast(intentService);
                            }
                        } else {
                            editor.putBoolean("isNoti", false);
                            editor.commit();

                            stopForeground(true);
                            stopSelf();
                            Intent intentService = new Intent();
                            intentService.setAction(ConstantHelper.PAUSE_SONG);
                            sendBroadcast(intentService);
                        }
                    } else if (action.equals(ConstantHelper.ACTION_RESUME)) {
                        editor.putBoolean("isNoti", true);
                        editor.commit();

                        notificationUtil.bigViews.setImageViewResource(R.id.btnNotiPlay, R.drawable.btn_noti_pause);
                        notificationUtil.remoteViews1.setImageViewResource(R.id.btnNotiPlay, R.drawable.btn_noti_pause);
                        continuePlay();
                    } else if (action.equals(ConstantHelper.ACTION_PREV)) {

                        editor.putBoolean("isNoti", true);
                        editor.commit();


                        String path = intent.getStringExtra("path");
                        curPos = intent.getIntExtra("pos", 0);
                        if (tracks.size() > curPos) {
                            curTrack = tracks.get(curPos);
                        }
                        notificationUtil.bigViews.setImageViewResource(R.id.btnNotiPlay, R.drawable.btn_noti_pause);
                        notificationUtil.remoteViews1.setImageViewResource(R.id.btnNotiPlay, R.drawable.btn_noti_pause);
                        prevPlay(path);
                    } else if (action.equals(ConstantHelper.ACTION_CLOSE_NOTI)) {
                        editor.putBoolean("isNoti", false);
                        editor.commit();

                        stopForeground(true);
                        stopSelf();
                        mMediaPlayer.pause();
                        Intent intent1 = new Intent(ConstantHelper.ACTION_CLOSE_NOTI);
                        intent1.putExtra("pos", curPos);
                        sendBroadcast(intent1);

                        if (isLive || isAlive) {
                            editor.putInt("repeat", repeat);
                            editor.commit();
                        } else {
                            editor.putInt("repeat", REP);
                            editor.commit();
                        }
                    } else if (action.equals(ConstantHelper.ACTION_REP)) {
                        repeat = intent.getIntExtra("rep", REP);
                    } else if (action.equals(ConstantHelper.ACTION_DESTROY_MAIN)) {
                        editor.putInt("repeat", REP);
                        editor.commit();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void nextPlay(String path) {
        try {
            playSong(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void prevPlay(String path) {
        try {
            playSong(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void playSong(String path) {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
        }
        mMediaPlayer.reset();
        Log.d("PATH", path);

        if (MainActivity.btnPlay != null) {
            MainActivity.btnPlay.setImageResource(R.drawable.custom_play);
        }

        if (PlayMusicActivity.pp != null) {
            PlayMusicActivity.pp.setImageResource(R.drawable.custom_play);
        }

        try {
            if (curTrack.bpm == ToolsHelper.IS_LOCAL) {
                mMediaPlayer.setDataSource(getBaseContext(), Uri.parse(path));
            } else {
                mMediaPlayer.setDataSource(path);
            }
            mMediaPlayer.prepareAsync();
        } catch (Exception e) {
            ToolsHelper.log("Error: " + e.toString());
        }


    }

    private void pausePlay() {
        try {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();

                if (MainActivity.btnPlay != null) {
                    MainActivity.btnPlay.setImageResource(R.drawable.custom_play);
                }

                if (PlayMusicActivity.pp != null) {
                    PlayMusicActivity.pp.setImageResource(R.drawable.custom_play);
                }
                ToolsHelper.log("NOTIFIED PAUSE");
                mNotifyMgr.notify(notificationUtil.mNotificationId, notificationUtil.showNotification(curTrack, curPos));
                startForeground(notificationUtil.mNotificationId, notificationUtil.showNotification(curTrack, curPos));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        MainActivity.loading.progressiveStop();
        MainActivity.loading.setVisibility(View.INVISIBLE);
    }

    private void continuePlay() {
        try {
            if (mMediaPlayer != null) {
                mMediaPlayer.start();

                if (MainActivity.btnPlay != null) {
                    MainActivity.btnPlay.setImageResource(R.drawable.custom_pause);
                }

                if (PlayMusicActivity.pp != null) {
                    PlayMusicActivity.pp.setImageResource(R.drawable.custom_pause);
                }

                if (PlayMusicActivity.mHandler != null) {
                    PlayMusicActivity.mHandler.postDelayed(PlayMusicActivity.mUpdateTimeTask, PlayMusicActivity.DUR);
                }

                if (PlayFragment.imageView != null) {
                    PlayFragment.imageView.startAnimation(AnimationUtils.loadAnimation(getBaseContext(),
                            R.anim.animation_rotate));
                }

                mNotifyMgr.notify(notificationUtil.mNotificationId, notificationUtil.showNotification(curTrack, curPos));
                startForeground(notificationUtil.mNotificationId, notificationUtil.showNotification(curTrack, curPos));
            } else {
                mMediaPlayer = MainActivity.mMediaPlayer;
                mMediaPlayer.start();


                if (MainActivity.btnPlay != null) {
                    MainActivity.btnPlay.setImageResource(R.drawable.custom_pause);
                }

                if (PlayMusicActivity.pp != null) {
                    PlayMusicActivity.pp.setImageResource(R.drawable.custom_pause);
                }

                if (PlayMusicActivity.mHandler != null) {
                    PlayMusicActivity.mHandler.postDelayed(PlayMusicActivity.mUpdateTimeTask, PlayMusicActivity.DUR);
                }

                if (PlayFragment.imageView != null) {
                    PlayFragment.imageView.startAnimation(AnimationUtils.loadAnimation(getBaseContext(),
                            R.anim.animation_rotate));
                }

                mNotifyMgr.notify(notificationUtil.mNotificationId, notificationUtil.showNotification(curTrack, curPos));
                startForeground(notificationUtil.mNotificationId, notificationUtil.showNotification(curTrack, curPos));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        MainActivity.loading.progressiveStop();
        MainActivity.loading.setVisibility(View.INVISIBLE);
    }

    private void stopPlay() {
        stopForeground(true);
        stopSelf();
        mMediaPlayer.stop();
        if (MainActivity.btnPlay != null) {
            MainActivity.btnPlay.setImageResource(R.drawable.custom_play);
        }

        if (PlayMusicActivity.pp != null) {
            PlayMusicActivity.pp.setImageResource(R.drawable.custom_play);
        }
    }
}