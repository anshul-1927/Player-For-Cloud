package dev.datvt.cloudtracks.noti_service;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.alelak.soundroid.models.Track;

import dev.datvt.cloudtracks.MainActivity;
import dev.datvt.cloudtracks.R;
import dev.datvt.cloudtracks.utils.ConstantHelper;
import dev.datvt.cloudtracks.utils.ToolsHelper;


/**
 * Created by datvit on 13/7/2016.
 */
public class NotificationUtil {

    public RemoteViews bigViews;
    public RemoteViews remoteViews1;
    public int mNotificationId = 001;
    public Notification notification;
    public static int position= 0;
    private Context context;

    public NotificationUtil(Context context) {
        this.context = context;
        bigViews = new RemoteViews(context.getPackageName(), R.layout.noti_layout);
        remoteViews1 = new RemoteViews(context.getPackageName(), R.layout.noti_small_layout);
    }

    public Notification showNotification(Track song, int curPos) {
        bigViews.setImageViewResource(R.id.btnNotiNext, R.drawable.btn_noti_next);
        bigViews.setTextViewText(R.id.notiNameMusic, song.title);

        remoteViews1.setImageViewResource(R.id.btnNotiNext, R.drawable.btn_noti_next);
        remoteViews1.setTextViewText(R.id.notiNameMusic, song.title);

        if (song.bpm == ToolsHelper.IS_LOCAL) {
            bigViews.setTextViewText(R.id.notiSingerMusic, song.license);
            remoteViews1.setTextViewText(R.id.notiSingerMusic, song.license);
        } else {
            bigViews.setTextViewText(R.id.notiSingerMusic, song.genre);
            remoteViews1.setTextViewText(R.id.notiSingerMusic, song.genre);
        }

        Intent intent = new Intent(context, MainActivity.class);
        position = curPos;
        intent.putExtra("pos", curPos);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context).setContentIntent(pIntent);
        notification = mBuilder.setSmallIcon(R.drawable.default_nhaccuatui)
                .setOngoing(true)
                .setContent(remoteViews1).build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            notification.bigContentView = bigViews;
        }
        notification.contentView = remoteViews1;

        Intent pauseIntent = new Intent(context, MediaPlayerService.class);
        if (!MediaPlayerService.isNext) {
            pauseIntent.setAction(ConstantHelper.ACTION_PAUSE);
            pauseIntent.putExtra("pause", 1);
        }

        PendingIntent ppauseIntent = PendingIntent.getService(context, 100,
                pauseIntent, 0);
        bigViews.setOnClickPendingIntent(R.id.btnNotiPlay, ppauseIntent);
        remoteViews1.setOnClickPendingIntent(R.id.btnNotiPlay, ppauseIntent);

        Intent nextIntent = new Intent(context, MediaPlayerService.class);
        nextIntent.setAction(ConstantHelper.ACTION_NEXT_NOTI);
        PendingIntent pnextIntent = PendingIntent.getService(context, 246,
                nextIntent, 0);
        bigViews.setOnClickPendingIntent(R.id.btnNotiNext, pnextIntent);
        remoteViews1.setOnClickPendingIntent(R.id.btnNotiNext, pnextIntent);


        Intent closeIntent = new Intent(context, MediaPlayerService.class);
        closeIntent.setAction(ConstantHelper.ACTION_CLOSE_NOTI);
        PendingIntent pcloseIntent = PendingIntent.getService(context, 0,
                closeIntent, 0);
        bigViews.setOnClickPendingIntent(R.id.btnNotiClose, pcloseIntent);
        remoteViews1.setOnClickPendingIntent(R.id.btnNotiClose, pcloseIntent);

        return notification;
    }
}
