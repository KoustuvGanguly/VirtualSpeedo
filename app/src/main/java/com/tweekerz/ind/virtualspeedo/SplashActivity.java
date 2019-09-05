package com.tweekerz.ind.virtualspeedo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.widget.Toast;

import com.github.anastr.speedviewlib.SpeedView;

public class SplashActivity extends AppCompatActivity {
    boolean locationGranted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_splash);
            WindowManager.LayoutParams layout = getWindow().getAttributes();
            layout.screenBrightness = 1;
            getWindow().setAttributes(layout);
            final SpeedView speedView = (SpeedView) findViewById(R.id.speed_tv_spl);
            speedView.setMaxSpeed(300);
            speedView.setWithTremble(false);
            speedView.speedTo(1);
            new CountDownTimer(4000, 10) {
                @Override
                public void onTick(long l) {
                    try {
                        if ((4000 - l) <= 2000) {
                            speedView.speedTo(speedView.getSpeed() + 10);
                        } else {
                            speedView.speedTo(speedView.getSpeed() - 10);
                        }
                        if (speedView.isInLowSection()) {
                            speedView.setIndicatorColor(Color.GREEN);
                            speedView.setTextColor(Color.GREEN);
                            speedView.setSpeedTextColor(Color.GREEN);
                        } else if (speedView.isInMediumSection()) {
                            speedView.setIndicatorColor(Color.YELLOW);
                            speedView.setTextColor(Color.YELLOW);
                            speedView.setSpeedTextColor(Color.YELLOW);
                        } else {
                            speedView.setIndicatorColor(Color.RED);
                            speedView.setTextColor(Color.RED);
                            speedView.setSpeedTextColor(Color.RED);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onFinish() {
                    try {
                        if (ActivityCompat.checkSelfPermission(SplashActivity.this
                                , Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                && ActivityCompat.checkSelfPermission(SplashActivity.this
                                , Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(SplashActivity.this
                                    , new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1); // 1 is a integer which will return the result in onRequestPermissionsResult
                        } else {
                            locationGranted = true;
                        }
                        if (locationGranted) {
                            startActivity(new Intent(SplashActivity.this, Act1.class));
                            finish();
                            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        try {
            switch (requestCode) {
                case 1:
                    try {
                        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                            locationGranted = true;
                            if (locationGranted) {
                                startActivity(new Intent(SplashActivity.this, Act1.class));
                                finish();
                                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                            }
                            //  get Location from your device by some method or code

                        } else {
                            Toast.makeText(this, "Sorry without this permission you will not be able to use this app!"
                                    , Toast.LENGTH_LONG).show();
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        finish();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }, 9000);
                            // show user that permission was denied. inactive the location based feature or force user to close the app
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
