package dev.datvt.cloudtracks.song_player;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alelak.soundroid.models.Track;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import dev.datvt.cloudtracks.R;
import dev.datvt.cloudtracks.noti_service.MediaPlayerService;
import dev.datvt.cloudtracks.utils.ToolsHelper;

/**
 * Created by datvt on 7/26/2016.
 */

public class SongListAdapter extends RecyclerView.Adapter<SongListAdapter.ViewHolder> {

    private final ArrayList<Track> mValues;
    private final SongListFragment.OnListFragmentInteractionListener mListener;
    Context ctx;
    private int pos = 0;
    private  AnimationDrawable animationDrawable1, animationDrawable2, animationDrawable3, animationDrawable4;

    public SongListAdapter(ArrayList<Track> items, SongListFragment.OnListFragmentInteractionListener listener, int pos) {
        mValues = items;
        mListener = listener;
        this.pos = pos;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_song_list, parent, false);
        ctx = view.getContext();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.mItem = mValues.get(position);
        holder.name.setText(mValues.get(position).title);
        if (mValues.get(position).bpm == ToolsHelper.IS_LOCAL) {
            holder.author.setText(mValues.get(position).license);
        } else {
            holder.author.setText(mValues.get(position).genre);
        }

        if (mValues.get(position).artwork_url != null) {
            if (mValues.get(position).bpm == ToolsHelper.IS_LOCAL) {
                Uri ur = Uri.parse(mValues.get(position).artwork_url);
                Picasso.with(ctx).load(ur)
                        .placeholder(R.drawable.default_nhaccuatui)
                        .into(holder.image);
            } else {
                Picasso.with(ctx).load(mValues.get(position).artwork_url)
                        .placeholder(R.drawable.default_nhaccuatui)
                        .into(holder.image);
            }
        } else {
            holder.image.setImageResource(R.drawable.default_nhaccuatui);
        }

        if (position == pos) {
            if (MediaPlayerService.mMediaPlayer != null && MediaPlayerService.mMediaPlayer.isPlaying()) {
                holder.imgPlay.setVisibility(View.VISIBLE);
                animationDrawable1 = (AnimationDrawable) holder.image_1.getDrawable();
                animationDrawable1.start();
                animationDrawable2 = (AnimationDrawable) holder.image_2.getDrawable();
                animationDrawable2.start();
                animationDrawable3 = (AnimationDrawable) holder.image_3.getDrawable();
                animationDrawable3.start();
                animationDrawable4 = (AnimationDrawable) holder.image_4.getDrawable();
                animationDrawable4.start();
            } else {
                animationDrawable1 = (AnimationDrawable) holder.image_1.getDrawable();
                animationDrawable1.stop();
                animationDrawable2 = (AnimationDrawable) holder.image_2.getDrawable();
                animationDrawable2.stop();
                animationDrawable3 = (AnimationDrawable) holder.image_3.getDrawable();
                animationDrawable3.stop();
                animationDrawable4 = (AnimationDrawable) holder.image_4.getDrawable();
                animationDrawable4.stop();
                holder.imgPlay.setVisibility(View.INVISIBLE);
            }
        } else {
            animationDrawable1 = (AnimationDrawable) holder.image_1.getDrawable();
            animationDrawable1.stop();
            animationDrawable2 = (AnimationDrawable) holder.image_2.getDrawable();
            animationDrawable2.stop();
            animationDrawable3 = (AnimationDrawable) holder.image_3.getDrawable();
            animationDrawable3.stop();
            animationDrawable4 = (AnimationDrawable) holder.image_4.getDrawable();
            animationDrawable4.stop();
            holder.imgPlay.setVisibility(View.INVISIBLE);
        }

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    if (mValues.get(position).bpm == ToolsHelper.IS_LOCAL) {
                        mListener.onListFragmentInteraction(mValues, holder.mItem, position, ToolsHelper.IS_LOCAL);
                    } else {
                        mListener.onListFragmentInteraction(mValues, holder.mItem, position, ToolsHelper.STREAM_CODE);
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView name;
        public final TextView author;
        public final ImageView image;
        public final LinearLayout imgPlay;
        public final ImageView image_1;
        public final ImageView image_2;
        public final ImageView image_3;
        public final ImageView image_4;
        public Track mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            name = (TextView) view.findViewById(R.id.tvSongName);
            author = (TextView) view.findViewById(R.id.tvSingerName);
            image = (ImageView) view.findViewById(R.id.ivArt);
            image_1 = (ImageView) view.findViewById(R.id.img_1);
            image_2 = (ImageView) view.findViewById(R.id.img_2);
            image_3 = (ImageView) view.findViewById(R.id.img_3);
            image_4 = (ImageView) view.findViewById(R.id.img_4);
            imgPlay = (LinearLayout) view.findViewById(R.id.ivVirtual);
        }
    }
}
