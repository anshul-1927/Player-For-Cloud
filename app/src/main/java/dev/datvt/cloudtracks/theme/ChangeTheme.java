package dev.datvt.cloudtracks.theme;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;

import com.victor.loading.rotate.RotateLoading;

import java.util.ArrayList;
import java.util.Locale;

import dev.datvt.cloudtracks.R;
import dev.datvt.cloudtracks.RootActivity;
import dev.datvt.cloudtracks.utils.ConstantHelper;

/**
 * Created by datvt on 7/17/2016.
 */
public class ChangeTheme extends RootActivity {

    private ArrayList<Integer> mThumbIds;
    private ArrayList<Integer> mThumbIdsSmall;
    private GridView gv;
    private ChangeThemeAdapter adapter = null;
    private RotateLoading rotateLoading;
    private int pos;
    private ImageView btnBack;
    private Locale myLocale;

    public void loadLocale() {
        String langPref = "Language";
        SharedPreferences prefs = getSharedPreferences("CommonPrefs", Activity.MODE_PRIVATE);
        String language = prefs.getString(langPref, "en");
        myLocale = new Locale(language);
        Locale.setDefault(myLocale);
        Configuration config = new Configuration();
        config.locale = myLocale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());

    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.theme_layout);

        gv = (GridView) findViewById(R.id.gridView);
        btnBack = (ImageView) findViewById(R.id.btnBackPlay);
        rotateLoading = (RotateLoading) findViewById(R.id.rotateloading);


        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        new GeTheme().execute();

    }

    private class GeTheme extends AsyncTask<Void, Void, Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            rotateLoading.setVisibility(View.VISIBLE);
            rotateLoading.start();
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            pos = integer;

            adapter = new ChangeThemeAdapter(getApplicationContext(), mThumbIdsSmall, pos);
            gv.setAdapter(adapter);
            gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Intent intent = getIntent();
                    intent.putExtra("index", mThumbIds.get(i));
                    Log.d("INDEX_SEND", mThumbIds.get(i) + "");
                    setResult(ConstantHelper.RESULT_CODE_THEME, intent);
                    finish();
                }
            });

            rotateLoading.stop();
            rotateLoading.setVisibility(View.INVISIBLE);
        }

        @Override
        protected Integer doInBackground(Void... voids) {

            mThumbIdsSmall = new ArrayList<>();
            mThumbIdsSmall.add(R.drawable.background_0_small);
            mThumbIdsSmall.add(R.drawable.background_1_small);
            mThumbIdsSmall.add(R.drawable.background_2_small);
            mThumbIdsSmall.add(R.drawable.background_3_small);
            mThumbIdsSmall.add(R.drawable.background_4_small);
            mThumbIdsSmall.add(R.drawable.background_5_small);
            mThumbIdsSmall.add(R.drawable.background_6_small);

            mThumbIdsSmall.add(R.drawable.bg_0);
            mThumbIdsSmall.add(R.drawable.bg_1);
            mThumbIdsSmall.add(R.drawable.bg_2);
            mThumbIdsSmall.add(R.drawable.bg_3);
            mThumbIdsSmall.add(R.drawable.bg_4);
            mThumbIdsSmall.add(R.drawable.bg_5);
            mThumbIdsSmall.add(R.drawable.bg_6);
            mThumbIdsSmall.add(R.drawable.bg_7);
            mThumbIdsSmall.add(R.drawable.bg_8);
            mThumbIdsSmall.add(R.drawable.bg_9);
            mThumbIdsSmall.add(R.drawable.bg_10);
            mThumbIdsSmall.add(R.drawable.bg_11);
            mThumbIdsSmall.add(R.drawable.bg_12);
            mThumbIdsSmall.add(R.drawable.bg_13);
            mThumbIdsSmall.add(R.drawable.bg_14);
            mThumbIdsSmall.add(R.drawable.bg_15);
            mThumbIdsSmall.add(R.drawable.bg_16);
            mThumbIdsSmall.add(R.drawable.bg_17);
            mThumbIdsSmall.add(R.drawable.bg_18);
            mThumbIdsSmall.add(R.drawable.bg_19);
            mThumbIdsSmall.add(R.drawable.bg_20);
            mThumbIdsSmall.add(R.drawable.bg_21);
            mThumbIdsSmall.add(R.drawable.bg_22);
            mThumbIdsSmall.add(R.drawable.bg_23);
            mThumbIdsSmall.add(R.drawable.bg_24);
            mThumbIdsSmall.add(R.drawable.bg_25);
            mThumbIdsSmall.add(R.drawable.bg_26);
            mThumbIdsSmall.add(R.drawable.bg_27);
            mThumbIdsSmall.add(R.drawable.bg_28);
            mThumbIdsSmall.add(R.drawable.bg_29);
            mThumbIdsSmall.add(R.drawable.bg_30);
            mThumbIdsSmall.add(R.drawable.bg_31);

            mThumbIds = new ArrayList<>();
            mThumbIds.add(R.drawable.background_0);
            mThumbIds.add(R.drawable.background_1);
            mThumbIds.add(R.drawable.background_2);
            mThumbIds.add(R.drawable.background_3);
            mThumbIds.add(R.drawable.background_4);
            mThumbIds.add(R.drawable.background_5);
            mThumbIds.add(R.drawable.background_6);

            mThumbIds.add(R.drawable.bg_0);
            mThumbIds.add(R.drawable.bg_1);
            mThumbIds.add(R.drawable.bg_2);
            mThumbIds.add(R.drawable.bg_3);
            mThumbIds.add(R.drawable.bg_4);
            mThumbIds.add(R.drawable.bg_5);
            mThumbIds.add(R.drawable.bg_6);
            mThumbIds.add(R.drawable.bg_7);
            mThumbIds.add(R.drawable.bg_8);
            mThumbIds.add(R.drawable.bg_9);
            mThumbIds.add(R.drawable.bg_10);
            mThumbIds.add(R.drawable.bg_11);
            mThumbIds.add(R.drawable.bg_12);
            mThumbIds.add(R.drawable.bg_13);
            mThumbIds.add(R.drawable.bg_14);
            mThumbIds.add(R.drawable.bg_15);
            mThumbIds.add(R.drawable.bg_16);
            mThumbIds.add(R.drawable.bg_17);
            mThumbIds.add(R.drawable.bg_18);
            mThumbIds.add(R.drawable.bg_19);
            mThumbIds.add(R.drawable.bg_20);
            mThumbIds.add(R.drawable.bg_21);
            mThumbIds.add(R.drawable.bg_22);
            mThumbIds.add(R.drawable.bg_23);
            mThumbIds.add(R.drawable.bg_24);
            mThumbIds.add(R.drawable.bg_25);
            mThumbIds.add(R.drawable.bg_26);
            mThumbIds.add(R.drawable.bg_27);
            mThumbIds.add(R.drawable.bg_28);
            mThumbIds.add(R.drawable.bg_29);
            mThumbIds.add(R.drawable.bg_30);
            mThumbIds.add(R.drawable.bg_31);

            int bg = getIntent().getIntExtra("theme", -1);
            pos = 0;
            for (int i = 0; i < mThumbIds.size(); i++) {
                if (bg == mThumbIds.get(i)) {
                    pos = i;
                    break;
                }
            }

            return pos;
        }
    }
}
