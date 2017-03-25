package dev.datvt.cloudtracks.advertisement;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;

import dev.datvt.cloudtracks.R;
import dev.datvt.cloudtracks.RootActivity;


/**
 * Created by datvt on 8/9/2016.
 */
public class MoreAppActivity extends RootActivity {


    private ImageView btnBack;
    private ListView listView;
    private MoreAppAdapter moreAppAdapter;
    private ArrayList<Advertisement> arrayList;
    private SwipeRefreshLayout refreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.more_app_activity);

        listView = (ListView) findViewById(R.id.lvApp);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        btnBack = (ImageView) findViewById(R.id.btnBackSetting);
        refreshLayout.setRefreshing(true);

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshLayout.setRefreshing(false);
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        arrayList = new ArrayList<>();
        moreAppAdapter = new MoreAppAdapter(this, arrayList);
        listView.setAdapter(moreAppAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Object data = adapterView.getItemAtPosition(i);
                if (data instanceof Advertisement) {
                    String appPackageName = ((Advertisement) data).getPackageName();
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                    } catch (android.content.ActivityNotFoundException anfe) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id="
                                + appPackageName)));
                    }
                }
            }
        });

        initData();
    }

    private void initData() {
        arrayList.add(new Advertisement(R.drawable.equalizer_icon, getString(R.string.name_app),
                getString(R.string.body_app), 4.4f, "music.equalizer.bassbooster.eq"));
        moreAppAdapter.notifyDataSetChanged();
        refreshLayout.setRefreshing(false);
    }
}
