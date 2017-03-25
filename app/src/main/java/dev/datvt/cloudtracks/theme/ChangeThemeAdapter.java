package dev.datvt.cloudtracks.theme;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.ArrayList;

import dev.datvt.cloudtracks.R;

/**
 * Created by datvt on 7/17/2016.
 */
public class ChangeThemeAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<Integer> mThumbIds;
    private int index;

    public ChangeThemeAdapter(Context c, ArrayList<Integer> arrIds, int index) {
        this.mContext = c;
        this.mThumbIds = arrIds;
        this.index = index;
    }

    @Override
    public int getCount() {
        return mThumbIds.size();
    }

    @Override
    public Object getItem(int arg0) {
        if (mThumbIds != null) {
            return mThumbIds.get(arg0);
        }
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_theme, null);
            holder = createViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.image.setImageResource(mThumbIds.get(position));
        if (position == index) {
            Log.d("INDEX", index + "");
            holder.imgOk.setVisibility(View.VISIBLE);
        } else {
            holder.imgOk.setVisibility(View.INVISIBLE);
        }

        return convertView;
    }

    private ViewHolder createViewHolder(View v) {
        ViewHolder holder = new ViewHolder();
        holder.image = (ImageView) v.findViewById(R.id.imgTheme);
        holder.imgOk = (ImageView) v.findViewById(R.id.ivOk);
        return holder;
    }

    private static class ViewHolder {
        public ImageView image;
        public ImageView imgOk;
    }
}
