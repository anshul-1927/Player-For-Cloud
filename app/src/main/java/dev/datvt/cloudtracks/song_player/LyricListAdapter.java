package dev.datvt.cloudtracks.song_player;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import dev.datvt.cloudtracks.R;


/**
 * Created by datvt on 6/30/2016.
 */
public class LyricListAdapter extends BaseAdapter {

    private Context context;
    private List<Lyrics> lyrics;

    public LyricListAdapter(Context context, List<Lyrics> lyrics) {
        this.context = context;
        this.lyrics = lyrics;
    }

    @Override
    public int getCount() {
        if (lyrics != null) {
            return lyrics.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if (lyrics != null) {
            return lyrics.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_lyrics_list, null);
            holder = createViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Lyrics lyric = lyrics.get(position);
        holder.name.setText(lyric.getTrack());
        holder.artist.setText(lyric.getArtist());

        if (lyric.getCoverURL() != null && !lyric.getCoverURL().isEmpty()) {
            Picasso.with(context).load(lyric.getCoverURL())
                    .placeholder(R.drawable.icon_img_lyric_2)
                    .into(holder.img);
        } else {
            holder.img.setImageResource(R.drawable.icon_img_lyric_2);
        }

        return convertView;
    }

    private ViewHolder createViewHolder(View v) {
        ViewHolder holder = new ViewHolder();
        holder.name = (TextView) v.findViewById(R.id.tvTracks);
        holder.artist = (TextView) v.findViewById(R.id.tvArtist);
        holder.img = (ImageView) v.findViewById(R.id.ivArtLyrics);
        return holder;
    }

    private static class ViewHolder {
        public TextView name;
        public TextView artist;
        public ImageView img;
    }


}
