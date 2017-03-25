package dev.datvt.cloudtracks.sound_cloud;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.alelak.soundroid.models.Track;
import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

import dev.datvt.cloudtracks.MyApplication;
import dev.datvt.cloudtracks.R;
import dev.datvt.cloudtracks.utils.ToolsHelper;


/**
 * TODO: Replace the implementation with code for your data type.
 */
public class MySearchTracksRecyclerViewAdapter extends RecyclerView.Adapter<MySearchTracksRecyclerViewAdapter.ViewHolder> {

    private final ArrayList<Track> mValues;
    private final SearchTracksFragment.OnListFragmentInteractionListener mListener;
    Context ctx;
    String[] quanitiy = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
    int sel = 0;

    public MySearchTracksRecyclerViewAdapter(ArrayList<Track> items,
                                             SearchTracksFragment.OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_item, parent, false);
        ctx = view.getContext();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.mItem = mValues.get(position);
        holder.title.setText(mValues.get(position).title);
        holder.subtitle.setText(mValues.get(position).genre);
        holder.duration.setText(ToolsHelper.milliSecondsToTimer(mValues.get(position).duration));
        holder.ivLike.setVisibility(View.VISIBLE);

        if (mValues.get(position).playback_count >= 1000) {
            holder.playview.setText(ToolsHelper.intToString(mValues.get(position).playback_count));
        } else {
            holder.playview.setText(mValues.get(position).playback_count + "");
        }

        if (MyApplication.CAN_DOWNLOAD) {
            String path = ToolsHelper.folder + "/" + mValues.get(position).title + "_" + mValues.get(position).id + ".mp3";

            File mp = new File(path);
            if (mp.exists()) {
                holder.download.setVisibility(View.INVISIBLE);
            } else {
                holder.download.setVisibility(View.VISIBLE);
                holder.download.setImageResource(R.drawable.icon_download_cloud);
            }
        }

        if (mValues.get(position).artwork_url != null) {
            Picasso.with(ctx).load(mValues.get(position).artwork_url)
                    .placeholder(R.drawable.default_nhaccuatui)
                    .into(holder.art);
        } else {
            holder.art.setImageResource(R.drawable.default_nhaccuatui);
        }

        holder.addplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Play URL :", "" + mValues.get(position).stream_url);
                dialogShow(mValues.get(position));
            }
        });

        holder.download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Download URL :", "" + mValues.get(position).stream_url);
                mListener.onListFragmentInteraction(mValues, holder.mItem, position, ToolsHelper.DOWNLOAD_CODE);
                ToolsHelper.addToDownload(mValues.get(position));
                holder.download.setVisibility(View.GONE);
            }
        });

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    mListener.onListFragmentInteraction(mValues, holder.mItem, position, ToolsHelper.STREAM_CODE);
                    ToolsHelper.addToRecent(mValues.get(position));
                }
            }
        });


        if (position == mValues.size() - 1) {
            //  holder.mView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public void dialogShow(final Track track) {
        ArrayList<String> listplaylist = ToolsHelper.getListPlaylist();
        quanitiy = new String[listplaylist.size()];
        for (int i = 0; i < listplaylist.size(); i++) {
            quanitiy[i] = listplaylist.get(i).replace(".lst", "");
        }

        if (quanitiy.length > 0) {
            new AlertDialog.Builder(ctx)
                    .setTitle(ctx.getString(R.string.choose_playlist))
                    .setSingleChoiceItems(quanitiy, 0, null)
                    .setPositiveButton(ctx.getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (quanitiy.length > 0) {
                                sel = ((AlertDialog) dialogInterface).getListView().getCheckedItemPosition();
                                Log.d("Selection ", "" + quanitiy[(sel)]);
                                ToolsHelper.addToPlaylist(ctx, track, quanitiy[sel]);
                                ToolsHelper.toast(ctx, ctx.getString(R.string.noti_add_plist) + " " + quanitiy[sel] + " "
                                        + ctx.getString(R.string.success));
                                dialogInterface.dismiss();
                            }
                        }
                    }).create().show();
        } else {
            ToolsHelper.toast(ctx, ctx.getString(R.string.no_playlist));
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public final View mView;
        public final TextView title;
        public final TextView subtitle;
        public final TextView duration;
        public final TextView playview;

        public final ImageView art;
        public final ImageView download;
        public final ImageView addplay;
        public final ImageView ivLike;
        public Track mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            title = (TextView) view.findViewById(R.id.title);
            subtitle = (TextView) view.findViewById(R.id.subtitle);
            duration = (TextView) view.findViewById(R.id.tvDurationTracks);
            playview = (TextView) view.findViewById(R.id.tvViewTracks);
            download = (ImageView) view.findViewById(R.id.download);
            addplay = (ImageView) view.findViewById(R.id.addplay);
            art = (ImageView) view.findViewById(R.id.art);
            ivLike = (ImageView) view.findViewById(R.id.ivLike);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + subtitle.getText() + "'";
        }
    }
}
