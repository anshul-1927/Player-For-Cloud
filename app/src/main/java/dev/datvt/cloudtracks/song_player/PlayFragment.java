package dev.datvt.cloudtracks.song_player;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;

import dev.datvt.cloudtracks.MainActivity;
import dev.datvt.cloudtracks.R;
import dev.datvt.cloudtracks.noti_service.MediaPlayerService;
import dev.datvt.cloudtracks.utils.ToolsHelper;

/**
 * Created by datvt on 7/26/2016.
 */
public class PlayFragment extends Fragment {

    public static ImageView imageView;
    public static TextView songName;
    public static TextView singer;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_play_song, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imageView = (ImageView) view.findViewById(R.id.art_im);
        songName = (TextView) view.findViewById(R.id.titleSong);
        singer = (TextView) view.findViewById(R.id.titleSinger);

        if (null != PlayMusicActivity.curTrack) {
            songName.setText(PlayMusicActivity.curTrack.title);
            if (PlayMusicActivity.curTrack.bpm == ToolsHelper.IS_LOCAL) {
                PlayFragment.singer.setText(PlayMusicActivity.curTrack.license);
            } else {
                PlayFragment.singer.setText(PlayMusicActivity.curTrack.genre);

            }
        }


        if (null != PlayMusicActivity.curTrack.artwork_url) {
            if (PlayMusicActivity.curTrack.bpm == ToolsHelper.IS_LOCAL) {
                Uri ur = Uri.parse(PlayMusicActivity.curTrack.artwork_url);
                Picasso.with(getContext()).load(ur).error(R.drawable.icon_disc_blue)
                        .placeholder(R.drawable.icon_disc_blue).into(PlayFragment.imageView);
            } else {
                Picasso.with(getContext()).load(PlayMusicActivity.curTrack.artwork_url.replace("large", "t500x500"))
                        .placeholder(R.drawable.icon_disc_blue).into(PlayFragment.imageView);
            }
        } else {
            PlayFragment.imageView.setImageResource(R.drawable.icon_disc_blue);
        }

        if (MainActivity.mMediaPlayer != null && MediaPlayerService.mMediaPlayer != null) {
            if (MainActivity.mMediaPlayer.isPlaying() || MediaPlayerService.mMediaPlayer.isPlaying()) {
                imageView.startAnimation(AnimationUtils.loadAnimation(view.getContext(), R.anim.animation_rotate));
            } else {
                imageView.clearAnimation();
            }
        }
    }
}
