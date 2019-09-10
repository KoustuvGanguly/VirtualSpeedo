package com.tweekerz.ind.virtualspeedo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.GpsSatellite;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.design.widget.NavigationView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.github.anastr.speedviewlib.AwesomeSpeedometer;
import com.github.anastr.speedviewlib.SpeedView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Act1 extends AppCompatActivity implements
        LocationListener, GoogleApiClient.ConnectionCallbacks
        , GoogleApiClient.OnConnectionFailedListener, android.location.GpsStatus.Listener {
    private final static int REQUEST_LOCATION = 46;
    private LocationManager mLocationManager;
    private TextView mTopSpd, mAltitude, mLast_top_speed_time;
    private SharedPreferences mSharedPreferences;
    private SpeedView speedView;
    private AwesomeSpeedometer speedView_2;
    private TextView mDigitalSpeedView, km;
    private SwitchCompat tgl_switch_unit;
    private boolean isTglEnabled = true, isUnitKMPH = true;
    private Button rwrd_btn;
    private GoogleApiClient googleApiClient;
    private Context mContext;
    private double mCurAlt;
    private NotificationCompat.Builder mBuilder;
    private NotificationManagerCompat notificationManager;
    private AdView mAdView;
    private InterstitialAd mInterstitialAd;
    private RewardedVideoAd mRewardedVideoAd;
    private DrawerLayout dl;
    private ActionBarDrawerToggle t;
    private NavigationView nv;
    private int speedoOn = 1;
    Location mInitDistance = null;
    private float mDistanceCovered = 0;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_act1);
            dl = (DrawerLayout) findViewById(R.id.dl_main);
            t = new ActionBarDrawerToggle(this, dl, R.string.navigation_drawer_open
                    , R.string.navigation_drawer_close);
            t.setDrawerIndicatorEnabled(true);

            dl.addDrawerListener(t);
            t.syncState();

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);


            nv = (NavigationView) findViewById(R.id.nv);
            nv.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int id = item.getItemId();
                    switch (id) {
                        case R.id.speedo_one:
                            mSharedPreferences.edit().putInt("speedo_on", 1).apply();
                            speedoOn = mSharedPreferences.getInt("speedo_on", 1);
                            setSpeedometerVisibility();
                            dl.closeDrawers();
                            break;
                        case R.id.speedo_two:
                            mSharedPreferences.edit().putInt("speedo_on", 2).apply();
                            speedoOn = mSharedPreferences.getInt("speedo_on", 1);
                            setSpeedometerVisibility();
                            dl.closeDrawers();
                            break;
                        case R.id.speedo_3:
                            mSharedPreferences.edit().putInt("speedo_on", 3).apply();
                            speedoOn = mSharedPreferences.getInt("speedo_on", 1);
                            setSpeedometerVisibility();
                            dl.closeDrawers();
                            break;
                        case R.id.settings:
                            Toast.makeText(Act1.this, "Settings", Toast.LENGTH_SHORT).show();
                            dl.closeDrawers();
                            break;
                        case R.id.pp:
                            try {
                                startActivity(new Intent(Act1.this, Privacy.class));
                                dl.closeDrawers();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                        default:
                            return true;
                    }


                    return true;

                }
            });
//            setSupportActionBar(toolbar);

//            final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
//                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
//            drawer.addDrawerListener(toggle);
//            toggle.syncState();
            //WindowManager.LayoutParams layout = getWindow().getAttributes();
            //layout.screenBrightness = 1;
            //getWindow().setAttributes(layout);
            mContext = this;

            mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            rwrd_btn = findViewById(R.id.rwrd_btn);
            mTopSpd = (TextView) findViewById(R.id.top_speed_tv);
            mAltitude = (TextView) findViewById(R.id.altitude);
            mLast_top_speed_time = (TextView) findViewById(R.id.last_top_speed_time);
            mSharedPreferences = getSharedPreferences("speed_specs", Context.MODE_PRIVATE);

            try {
                if (mLocationManager.getProvider(LocationManager.GPS_PROVIDER) != null) {
                    mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 1, Act1.this);
                    if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        enableLoc();
                    }
                }
            } catch (Exception e) {
                Toast.makeText(Act1.this, "No GPS found!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
            mDistanceCovered = mSharedPreferences.getFloat("distance_covered", 0);
            mAltitude.setText("Altitude : Searching...");
            ((TextView) findViewById(R.id.speed_accuracy)).setText("Speed accuracy : 0 m/s");
            ((TextView) findViewById(R.id.sat_count)).setText(0 + " Used In Last Fix (" + 0 + ")");
            mLast_top_speed_time.setText("Reached on : " + getTime(mSharedPreferences.getLong("top_speed_last_time", 0)));
            mTopSpd.setText("Top speed : " + convertMPStoKMPHOrMPH(mSharedPreferences.getFloat("top_spd", 0))
                    + (isUnitKMPH?"km/h":"MPH"));
            ((TextView) findViewById(R.id.distance_covered))
                    .setText("Distance covered : " + String.valueOf(convertMeterToKMOrMile(mDistanceCovered)) + (isUnitKMPH ? " km" : " Mile"));
            // mAltitude.setText("Limit crossed : " + String.valueOf(mSharedPreferences.getLong("spd_limit_crossed_times", 0)) + " times!");
            tgl_switch_unit = findViewById(R.id.tgl_switch_unit);
            mDigitalSpeedView = findViewById(R.id.speed_digital_tv);
            km = findViewById(R.id.km);
            speedView = (SpeedView) findViewById(R.id.speed_tv_1);
            speedView.setMaxSpeed(300);
            speedView.setWithTremble(false);

            speedView_2 = (AwesomeSpeedometer) findViewById(R.id.speed_tv_2);
            speedView_2.setMaxSpeed(300);
            speedView_2.setWithTremble(false);
            mBuilder = new NotificationCompat.Builder(mContext);
            //  mBuilder.setContentInfo("VS");
            mBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
            mBuilder.setSmallIcon(R.mipmap.ic_launcher_round);
            mBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round));
//            BitmapFactory.
            mBuilder.setOngoing(false);
            notificationManager = NotificationManagerCompat.from(this);
            speedoOn = mSharedPreferences.getInt("speedo_on", 1);
            if (mSharedPreferences.getBoolean("switch_state", false)) {
                speedoOn = 3;
                mSharedPreferences.edit().putInt("speedo_on", speedoOn).apply();
                mSharedPreferences.edit().putBoolean("switch_state", false).apply();

            }
            setSpeedometerVisibility();
            tgl_switch_unit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    try {
                        isUnitKMPH = !b;
                        mSharedPreferences.edit().putBoolean("unit_kmph", !b).apply();
                        speedView.setUnit(b ? "MPH" : "Km/h");
                        speedView_2.setUnit(b ? "MPH" : "Km/h");
                        km.setText(b ? "MPH" : "Km/h");
                        mTopSpd.setText("Top speed : " + convertMPStoKMPHOrMPH(mSharedPreferences.getFloat("top_spd", 0))
                                + (isUnitKMPH?"km/h":"MPH"));
                        ((TextView) findViewById(R.id.distance_covered))
                                .setText("Distance covered : " + String.valueOf(convertMeterToKMOrMile(mDistanceCovered)) + (isUnitKMPH ? " km" : " Mile"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            ((Button) findViewById(R.id.distance_reset)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        mDistanceCovered = 0;
                        mSharedPreferences.edit().putFloat("distance_covered", mDistanceCovered).apply();
                        ((TextView) findViewById(R.id.distance_covered))
                                .setText("Distance covered : " + String.valueOf(convertMeterToKMOrMile(mDistanceCovered)) + (isUnitKMPH ? " km" : " Mile"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            tgl_switch_unit.setChecked(!mSharedPreferences.getBoolean("unit_kmph", true));
            rwrd_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        if (mRewardedVideoAd.isLoaded()) {
                            mRewardedVideoAd.show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            MobileAds.initialize(this, "ca-app-pub-3925957206744972~8869279275");
            mAdView = (AdView) findViewById(R.id.adView);
            mInterstitialAd = new InterstitialAd(this);
            mInterstitialAd.setAdUnitId("ca-app-pub-3925957206744972/8325155655");

            mInterstitialAd.loadAd(buildAd());
            mAdView.loadAd(buildAd());
            mAdView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                }

                @Override
                public void onAdFailedToLoad(int errorCode) {
                    // Code to be executed when an ad request fails.
                }

                @Override
                public void onAdOpened() {
                    // Code to be executed when an ad opens an overlay that
                    // covers the screen.
                }

                @Override
                public void onAdLeftApplication() {
                }

                @Override
                public void onAdClosed() {
                }
            });

            mInterstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    try {
                        super.onAdClosed();
                        mInterstitialAd.loadAd(buildAd());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onAdLeftApplication() {
                    try {
                        super.onAdLeftApplication();
                        mInterstitialAd.loadAd(buildAd());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);
            mRewardedVideoAd.setRewardedVideoAdListener(new RewardedVideoAdListener() {
                @Override
                public void onRewardedVideoAdLoaded() {
                    try {
//                        rwrd_btn.setEnabled(true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onRewardedVideoAdOpened() {

                }

                @Override
                public void onRewardedVideoStarted() {

                }

                @Override
                public void onRewardedVideoAdClosed() {
                    loadRewardedVideoAd();
                }

                @Override
                public void onRewarded(RewardItem rewardItem) {
                    try {
                        rwrd_btn.setVisibility(View.GONE);
                        //   ((TextView) findViewById(R.id.lbl1)).setTextColor(Color.GREEN);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onRewardedVideoAdLeftApplication() {

                }

                @Override
                public void onRewardedVideoAdFailedToLoad(int i) {
                    loadRewardedVideoAd();
                }

                @Override
                public void onRewardedVideoCompleted() {

                }
            });

            loadRewardedVideoAd();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setSpeedometerVisibility() {
        switch (speedoOn) {
            case 1:
                speedView.setVisibility(View.VISIBLE);
                speedView_2.setVisibility(View.GONE);
                mDigitalSpeedView.setVisibility(View.GONE);
                km.setVisibility(View.GONE);
                break;
            case 2:
                speedView_2.setVisibility(View.VISIBLE);
                speedView.setVisibility(View.GONE);
                mDigitalSpeedView.setVisibility(View.GONE);
                km.setVisibility(View.GONE);
                break;
            case 3:
                speedView_2.setVisibility(View.GONE);
                speedView.setVisibility(View.GONE);
                mDigitalSpeedView.setVisibility(View.VISIBLE);
                km.setVisibility(View.VISIBLE);
                break;

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (t.onOptionsItemSelected(item))
            return true;

        return super.onOptionsItemSelected(item);
    }
    private void loadRewardedVideoAd() {
        mRewardedVideoAd.loadAd("ca-app-pub-3925957206744972/4329645292",
                new AdRequest.Builder().addTestDevice("35ACB0CA10D03B177A1E01926D280D44").build());
    }

    private AdRequest buildAd() {
        return new AdRequest.Builder().addTestDevice("35ACB0CA10D03B177A1E01926D280D44").build();
    }

    private String getTime(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ENGLISH);
        if (time > 0)
            return sdf.format(new Date(time));
        else return "n/a";
    }

    @Override
    public void onLocationChanged(Location location) {
        try {

            if (location.hasAltitude()) {
                double alt = 0.0;
                BigDecimal bd = new BigDecimal(location.getAltitude());
                bd = bd.setScale(2, RoundingMode.HALF_UP);
                alt = bd.doubleValue();
                mCurAlt = alt;
                mAltitude.setText("Alt : " + alt + " meters");
            }

            if (location.hasAccuracy()) {
                double accuracy = 0.0;
                BigDecimal bd = new BigDecimal(location.getAccuracy());
                bd = bd.setScale(2, RoundingMode.HALF_UP);
                accuracy = bd.doubleValue();
                ((TextView) findViewById(R.id.accuracy)).setText("Accuracy : " + accuracy + " meters");
            }
            if (mInitDistance == null) {
                mInitDistance = location;
            }
            if (mInitDistance != null) {
                mDistanceCovered += location.distanceTo(mInitDistance);
                mSharedPreferences.edit().putFloat("distance_covered", mDistanceCovered).apply();
                ((TextView) findViewById(R.id.distance_covered))
                        .setText("Distance covered : " + String.valueOf(convertMeterToKMOrMile(mDistanceCovered)) + (isUnitKMPH ? " km" : " Mile"));
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (location.hasSpeedAccuracy()) {
                    double speedAccuracy = 0.0;
                    BigDecimal bd = new BigDecimal(location.getSpeedAccuracyMetersPerSecond());
                    bd = bd.setScale(2, RoundingMode.HALF_UP);
                    speedAccuracy = bd.doubleValue();
                    ((TextView) findViewById(R.id.speed_accuracy)).setText("Speed accuracy : " + speedAccuracy + " m/s");
                }
            }
            if (location.hasSpeed()) {
                if (convertMPStoKMPHOrMPH(location.getSpeed()) >= speedView.getMaxSpeed()) {
                    speedView.setMaxSpeed(speedView.getMaxSpeed() + speedView.getMaxSpeed() / 2);
                }
                float tempSpd = (float) convertMPStoKMPHOrMPH(location.getSpeed());
                float mCurSpd = tempSpd >= 1 ? tempSpd : 0;
                if (speedView.isInLowSection()) {
                    mDigitalSpeedView.setTextColor(Color.GREEN);
                    km.setTextColor(Color.GREEN);
                    speedView.setIndicatorColor(Color.GREEN);
                    speedView.setTextColor(Color.GREEN);
                    speedView.setSpeedTextColor(Color.GREEN);
                    mBuilder.setColor(Color.GREEN);
                } else if (speedView.isInMediumSection()) {
                    mDigitalSpeedView.setTextColor(Color.YELLOW);
                    km.setTextColor(Color.YELLOW);
                    speedView.setIndicatorColor(Color.YELLOW);
                    speedView.setTextColor(Color.YELLOW);
                    speedView.setSpeedTextColor(Color.YELLOW);
                    mBuilder.setColor(Color.YELLOW);
                } else {
                    mDigitalSpeedView.setTextColor(Color.RED);
                    km.setTextColor(Color.RED);
                    speedView.setIndicatorColor(Color.RED);
                    speedView.setTextColor(Color.RED);
                    speedView.setSpeedTextColor(Color.RED);
                    mBuilder.setColor(Color.RED);
                }
                speedView.speedTo(mCurSpd);
                speedView_2.speedTo(mCurSpd);
                mDigitalSpeedView.setText(String.valueOf(mCurSpd));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            mBuilder.setCategory(NotificationCompat.CATEGORY_STATUS);
                            mBuilder.setProgress((int) speedView.getMaxSpeed(), (int) speedView.getSpeed(), false);
                            mBuilder.setStyle(new NotificationCompat.BigTextStyle());
                            mBuilder.setColorized(true);
                            mBuilder.setContentText("Altitude " + mCurAlt + " m");
                            mBuilder.setContentTitle("Current speed " + speedView.getSpeed() + " km/h");
                            mBuilder.setTicker(speedView.getSpeed() + " km/h");
                            mBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
                            notificationManager.notify(46, mBuilder.build());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

                if (location.getSpeed() > mSharedPreferences.getFloat("top_spd", 0)) {
                    mSharedPreferences.edit().putFloat("top_spd", location.getSpeed()).apply();
                    mSharedPreferences.edit().putLong("top_speed_last_time", System.currentTimeMillis()).apply();
                    mTopSpd.setText("Top speed : " + convertMPStoKMPHOrMPH(location.getSpeed()) + (isUnitKMPH?"km/h":"MPH"));
                    mLast_top_speed_time.setText("Reached on : " + getTime(mSharedPreferences.getLong("top_speed_last_time", 0)));
                } else {

                    mTopSpd.setText("Top speed : " + convertMPStoKMPHOrMPH(mSharedPreferences.getFloat("top_spd", 0)) + (isUnitKMPH?"km/h":"MPH"));
                    mLast_top_speed_time.setText("Reached on : " + getTime(mSharedPreferences.getLong("top_speed_last_time", 0)));
                }
            } else {
                try {
                    speedView.speedTo(0);
                    speedView_2.speedTo(0);
                    mDigitalSpeedView.setText("0.0");
                    mDigitalSpeedView.setTextColor(Color.GREEN);
                    km.setTextColor(Color.GREEN);
                    speedView.setIndicatorColor(Color.GREEN);
                    speedView.setTextColor(Color.GREEN);
                    speedView.setSpeedTextColor(Color.GREEN);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                mBuilder.setCategory(NotificationCompat.CATEGORY_STATUS);
                                mBuilder.setProgress((int) speedView.getMaxSpeed(), (int) speedView.getSpeed(), false);
                                mBuilder.setStyle(new NotificationCompat.BigTextStyle());
                                mBuilder.setColorized(true);
                                mBuilder.setContentText("Altitude " + mCurAlt + " m");
                                mBuilder.setContentTitle("Current speed " + speedView.getSpeed() + " km/h");
                                mBuilder.setTicker(speedView.getSpeed() + " km/h");
                                mBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
                                notificationManager.notify(46, mBuilder.build());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    // speedView.stop();
                    mTopSpd.setText("Top speed : " + convertMPStoKMPHOrMPH(mSharedPreferences.getFloat("top_spd", 0)) + (isUnitKMPH?"km/h":"MPH"));
                    mLast_top_speed_time.setText("Reached on : " + getTime(mSharedPreferences.getLong("top_speed_last_time", 0)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private double convertMPStoKMPHOrMPH(float mps) {
        try {
            BigDecimal bd = new BigDecimal((mps * 3600) / (isUnitKMPH ? 1000 : 1609));
            bd = bd.setScale(2, RoundingMode.HALF_UP);
            return bd.doubleValue();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private double convertMeterToKMOrMile(float meter) {
        try {
            BigDecimal bd = new BigDecimal((meter) / (isUnitKMPH ? 1000 : 1609));
            bd = bd.setScale(2, RoundingMode.HALF_UP);
            return bd.doubleValue();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        try {
            mDigitalSpeedView.setText("0.0");
            mDigitalSpeedView.setTextColor(Color.GREEN);
            km.setTextColor(Color.GREEN);
            speedView.speedTo(0);
            speedView_2.speedTo(0);
            speedView.setIndicatorColor(Color.GREEN);
            speedView.setTextColor(Color.GREEN);
            speedView.setSpeedTextColor(Color.GREEN);
            //   speedView.stop();
            mTopSpd.setText("Top speed : " + convertMPStoKMPHOrMPH(mSharedPreferences.getFloat("top_spd", 0)) + (isUnitKMPH?"km/h":"MPH"));
            mLast_top_speed_time.setText("Reached on : " + getTime(mSharedPreferences.getLong("top_speed_last_time", 0)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onProviderEnabled(String s) {
        try {
            mDigitalSpeedView.setText("0.0");
            mDigitalSpeedView.setTextColor(Color.GREEN);
            km.setTextColor(Color.GREEN);
            speedView.speedTo(0);
            speedView_2.speedTo(0);
            speedView.setIndicatorColor(Color.GREEN);
            speedView.setTextColor(Color.GREEN);
            speedView.setSpeedTextColor(Color.GREEN);
            //speedView.stop();
            mTopSpd.setText("Top speed : " + convertMPStoKMPHOrMPH(mSharedPreferences.getFloat("top_spd", 0)) + (isUnitKMPH?"km/h":"MPH"));
            mLast_top_speed_time.setText("Reached on : " + getTime(mSharedPreferences.getLong("top_speed_last_time", 0)));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onProviderDisabled(String s) {
        try {
            mDigitalSpeedView.setText("0.0");
            mDigitalSpeedView.setTextColor(Color.GREEN);
            km.setTextColor(Color.GREEN);
            speedView.speedTo(0);
            speedView_2.speedTo(0);
            speedView.setIndicatorColor(Color.GREEN);
            speedView.setTextColor(Color.GREEN);
            speedView.setSpeedTextColor(Color.GREEN);
            //speedView.stop();
            mTopSpd.setText("Top speed : " + convertMPStoKMPHOrMPH(mSharedPreferences.getFloat("top_spd", 0)) + (isUnitKMPH?"km/h":"MPH"));
            mLast_top_speed_time.setText("Reached on : " + getTime(mSharedPreferences.getLong("top_speed_last_time", 0)));
            showAd();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        try {
            super.onDestroy();
            mLocationManager.removeUpdates(this);
            notificationManager.cancel(46);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        showAd();
    }

    private void showAd() {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onResume() {
        super.onResume();
        try {
            if (mLocationManager.getProvider(LocationManager.GPS_PROVIDER) != null) {
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 1, Act1.this);
            }
//            showAd();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void enableLoc() {

        try {
            if (googleApiClient == null) {
                googleApiClient = new GoogleApiClient.Builder(this)
                        .addApi(LocationServices.API)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                            @Override
                            public void onConnectionFailed(ConnectionResult connectionResult) {

                                Log.e("Location error", connectionResult.getErrorCode() + "");
                            }
                        }).build();
                googleApiClient.connect();

                LocationRequest locationRequest = LocationRequest.create();
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                locationRequest.setInterval(1500);
                locationRequest.setFastestInterval(1000);
                locationRequest.setSmallestDisplacement(1f);
                LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                        .addLocationRequest(locationRequest);

                builder.setAlwaysShow(true);

                PendingResult<LocationSettingsResult> result =
                        LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
                result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                    @Override
                    public void onResult(LocationSettingsResult result) {
                        final Status status = result.getStatus();
                        switch (status.getStatusCode()) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                try {
                                    // Show the dialog by calling startResolutionForResult(),
                                    // and check the result in onActivityResult().
                                    status.startResolutionForResult(
                                            (Activity) mContext, REQUEST_LOCATION);
                                } catch (IntentSender.SendIntentException e) {
                                    // Ignore the error.
                                }
                                break;
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_LOCATION:
                switch (resultCode) {
                    case Activity.RESULT_CANCELED: {
                        // The user was asked to change settings, but chose not to
                        finish();
                        break;
                    }
                    default: {
                        break;
                    }
                }
                break;
        }

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onGpsStatusChanged(int event) {
        try {
            int satellites = 0;
            int satellitesInFix = 0;

            int timetofix = mLocationManager.getGpsStatus(null).getTimeToFirstFix();
            Log.i("VS", "Time to first fix = " + timetofix);
            for (GpsSatellite sat : mLocationManager.getGpsStatus(null).getSatellites()) {
                if (sat.usedInFix()) {
                    satellitesInFix++;
                }
                satellites++;
            }

            ((TextView) findViewById(R.id.sat_count)).setText(satellites + " Used In Last Fix (" + satellitesInFix + ")");
            Log.i("VS", satellites + " Used In Last Fix (" + satellitesInFix + ")");
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("VS", e.getMessage());
        }
    }
}
