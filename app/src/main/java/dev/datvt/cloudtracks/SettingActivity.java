package dev.datvt.cloudtracks;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Locale;

import dev.datvt.cloudtracks.advertisement.MoreAppActivity;
import dev.datvt.cloudtracks.utils.ToolsHelper;

/**
 * Created by datvt on 8/7/2016.
 */
public class SettingActivity extends RootActivity implements View.OnClickListener {

    private ImageView btnBack;
    private RelativeLayout btnLanguage, btnFeedback, btnAbout, btnRating, btnRelated;
    private TextView tvNameLanguage;

    private int selectedPosition;
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
        setContentView(R.layout.setting_activity);

        btnBack = (ImageView) findViewById(R.id.btnBackMain);
        btnLanguage = (RelativeLayout) findViewById(R.id.rlLanguage);
        btnFeedback = (RelativeLayout) findViewById(R.id.rlFeedback);
        btnAbout = (RelativeLayout) findViewById(R.id.rlAbout);
        btnRating = (RelativeLayout) findViewById(R.id.rlRating);
        btnRelated = (RelativeLayout) findViewById(R.id.rlRelatedApp);
        tvNameLanguage = (TextView) findViewById(R.id.tvNameLanguage);

        btnAbout.setOnClickListener(this);
        btnLanguage.setOnClickListener(this);
        btnFeedback.setOnClickListener(this);
        btnBack.setOnClickListener(this);
        btnRating.setOnClickListener(this);
        btnRelated.setOnClickListener(this);

        SharedPreferences prefs = getSharedPreferences("CommonPrefs", Activity.MODE_PRIVATE);
        String lang = prefs.getString("Language", "en");

        if (lang.equals("vi")) {
            tvNameLanguage.setText(getString(R.string.vi_language));
            selectedPosition = 0;
        } else {
            tvNameLanguage.setText(getString(R.string.en_language));
            selectedPosition = 1;
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        switch (id) {
            case R.id.btnBackMain:
                finish();
                break;
            case R.id.rlLanguage:
                changeLanguage();
                break;
            case R.id.rlFeedback:
                feedBackApp();
                break;
            case R.id.rlAbout:
//                ToolsHelper.toast(this, getString(R.string.noti_about));
                startActivity(new Intent(this, AboutActivity.class));
                break;
            case R.id.rlRating:
                openGooglePlay();
                break;
            case R.id.rlRelatedApp:
                relatedApp();
                break;
        }
    }

    private void relatedApp() {
        Intent intent = new Intent(this, MoreAppActivity.class);
        startActivity(intent);
    }


    private void feedBackApp() {
        Intent Email = new Intent(Intent.ACTION_SEND);
        Email.setType("text/email");
        Email.putExtra(Intent.EXTRA_EMAIL, new String[]{"contact@gpaddy.com"});
        Email.putExtra(Intent.EXTRA_SUBJECT, "Feedback Cloud Tracks");
        Email.putExtra(Intent.EXTRA_TEXT, "Dear ...," + "");
        startActivity(Intent.createChooser(Email, "Send Feedback:"));
    }

    private void openGooglePlay() {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
        } catch (android.content.ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
        }
    }

    private void changeLanguage() {
        CharSequence[] items = {getResources().getString(R.string.vi_language), getResources().getString(R.string.en_language)};
        final AlertDialog settingLanguage = new AlertDialog.Builder(this)
                .setTitle(R.string.language)
                .setSingleChoiceItems(items, selectedPosition, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                saveLangage("vi");
                                ToolsHelper.toast(getApplicationContext(), getString(R.string.noti_language));
                                break;
                            case 1:
                                saveLangage("en");
                                ToolsHelper.toast(getApplicationContext(), getString(R.string.noti_language));
                                break;
                            default:
                                break;
                        }
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(getResources().getString(R.string.cancel), null)
                .create();
        settingLanguage.show();
    }

    private void saveLangage(String lang) {
        String langPref = "Language";
        if (lang.equals("vi")) {
            tvNameLanguage.setText(getString(R.string.vi_language));
        } else {
            tvNameLanguage.setText(getString(R.string.en_language));
        }
        SharedPreferences prefs = getSharedPreferences("CommonPrefs", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(langPref, lang);
        editor.commit();
    }
}
