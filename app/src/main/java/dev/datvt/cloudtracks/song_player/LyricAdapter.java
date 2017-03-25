package dev.datvt.cloudtracks.song_player;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import dev.datvt.cloudtracks.R;


/**
 * Created by datvt on 6/30/2016.
 */
public class LyricAdapter extends BaseAdapter {

    private Context context;
    private List<String> lyrics;

    public LyricAdapter(Context context, List<String> lyrics) {
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
            convertView = inflater.inflate(R.layout.item_lyrics, null);
            holder = createViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String lyric = lyrics.get(position);
        holder.name.setText(lyric);
        holder.name.setTextColor(context.getResources().getColor(R.color.colorWhite));
        if (position == 0) {
            holder.name.setTextColor(context.getResources().getColor(R.color.colorGreen));
        } else if  (position == 1) {
            holder.name.setTextColor(context.getResources().getColor(R.color.colorYellow));
        }

        return convertView;
    }

    private ViewHolder createViewHolder(View v) {
        ViewHolder holder = new ViewHolder();
        holder.name = (TextView) v.findViewById(R.id.tvNameLyric);
        return holder;
    }

    private static class ViewHolder {
        public TextView name;
    }
}
