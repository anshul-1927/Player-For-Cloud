package dev.datvt.cloudtracks;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import java.util.Locale;

/**
 * Created by datvt on 8/7/2016.
 */
public class AboutActivity extends RootActivity implements View.OnClickListener {

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.about_activity);

        btnBack = (ImageView) findViewById(R.id.btnBackAbout);
        btnBack.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        switch (id) {
            case R.id.btnBackAbout:
                finish();
                break;
        }
    }
}
