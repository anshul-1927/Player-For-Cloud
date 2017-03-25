package dev.datvt.cloudtracks.advertisement;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.List;

import dev.datvt.cloudtracks.R;


/**
 * Created by datvt on 6/30/2016.
 */
public class MoreAppAdapter extends BaseAdapter {

    private Context context;
    private List<Advertisement> apps;

    public MoreAppAdapter(Context context, List<Advertisement> apps) {
        this.context = context;
        this.apps = apps;
    }

    @Override
    public int getCount() {
        if (apps != null) {
            return apps.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if (apps != null) {
            return apps.get(position);
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
            convertView = inflater.inflate(R.layout.item_more_app, null);
            holder = createViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Advertisement app = apps.get(position);
        holder.name.setText(app.getName());
        holder.body.setText(app.getBody());
        holder.logo.setImageResource(app.getLogo());
        holder.rate.setRating(app.getRate());

        return convertView;
    }

    private ViewHolder createViewHolder(View v) {
        ViewHolder holder = new ViewHolder();
        holder.name = (TextView) v.findViewById(R.id.tvAppName);
        holder.logo = (ImageView) v.findViewById(R.id.imIcon);
        holder.body = (TextView) v.findViewById(R.id.tvBody);
        holder.rate = (RatingBar) v.findViewById(R.id.rbStar);
        return holder;
    }

    private static class ViewHolder {
        public TextView name;
        public ImageView logo;
        public TextView body;
        public RatingBar rate;
    }
}
