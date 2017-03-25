package dev.datvt.cloudtracks;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.ImageView;

import com.victor.loading.rotate.RotateLoading;


public class StartActivity extends RootActivity {

    private ImageView startLogo;
    private RotateLoading rotateLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_activity);

        startLogo = (ImageView) findViewById(R.id.ivStart);
        rotateLoading = (RotateLoading) findViewById(R.id.rotateloading);
        rotateLoading.start();

        CountDownTimer countDownTimer = new CountDownTimer(2500, 100) {
            @Override
            public void onTick(long l) {
            }

            @Override
            public void onFinish() {
                rotateLoading.stop();
                Intent intent = new Intent(StartActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        };
        countDownTimer.start();
    }
}
