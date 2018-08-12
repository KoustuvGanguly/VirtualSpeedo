package com.tweekerz.ind.virtualspeedo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.github.anastr.speedviewlib.SpeedView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
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
        LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private final static int REQUEST_LOCATION = 46;
    private LocationManager mLocationManager;
    private TextView mTopSpd, mAltitude, mLast_top_speed_time;
    private SharedPreferences mSharedPreferences;
    private SpeedView speedView;
    private TextView mDigitalSpeedView,km;
    private SwitchCompat tgl_switch;
    private GoogleApiClient googleApiClient;
    private Context mContext;
    private double mCurAlt;
    private NotificationCompat.Builder mBuilder;
    private NotificationManagerCompat notificationManager;
    private AdView mAdView;


    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_act1);
            WindowManager.LayoutParams layout = getWindow().getAttributes();
            layout.screenBrightness = 1;
            getWindow().setAttributes(layout);
            mContext = this;
            findViewById(R.id.pp).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        startActivity(new Intent(Act1.this, Privacy.class));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
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
            mAltitude.setText("Altitude : Searching...");
            mLast_top_speed_time.setText("Reached on : " + getTime(mSharedPreferences.getLong("top_speed_last_time", 0)));
            mTopSpd.setText("Top speed : " + convertMPStoKMPH(mSharedPreferences.getFloat("top_spd", 0)) + "km/h");
            // mAltitude.setText("Limit crossed : " + String.valueOf(mSharedPreferences.getLong("spd_limit_crossed_times", 0)) + " times!");
            tgl_switch = findViewById(R.id.tgl_switch);
            mDigitalSpeedView = findViewById(R.id.speed_digital_tv);
            km = findViewById(R.id.km);
            speedView = (SpeedView) findViewById(R.id.speed_tv);
            speedView.setMaxSpeed(300);
            speedView.setWithTremble(false);
            mBuilder = new NotificationCompat.Builder(mContext);
            //  mBuilder.setContentInfo("VS");
            mBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
            mBuilder.setSmallIcon(R.mipmap.ic_launcher_round);
            mBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round));
//            BitmapFactory.
            mBuilder.setOngoing(false);
            notificationManager = NotificationManagerCompat.from(this);
            tgl_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b) {
                        mSharedPreferences.edit().putBoolean("switch_state", true).apply();
                        speedView.setVisibility(View.GONE);
                        mDigitalSpeedView.setVisibility(View.VISIBLE);
                        km.setVisibility(View.VISIBLE);
                    } else {
                        mSharedPreferences.edit().putBoolean("switch_state", false).apply();
                        speedView.setVisibility(View.VISIBLE);
                        mDigitalSpeedView.setVisibility(View.GONE);
                        km.setVisibility(View.GONE);
                    }
                }
            });
            tgl_switch.setChecked(mSharedPreferences.getBoolean("switch_state", false));
            MobileAds.initialize(this, "ca-app-pub-3925957206744972~8869279275");
            mAdView = (AdView) findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
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
                    // Code to be executed when the user has left the app.
                }

                @Override
                public void onAdClosed() {
                    // Code to be executed when when the user is about to return
                    // to the app after tapping on an ad.
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
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
            if (location.hasSpeed()) {
                if (convertMPStoKMPH(location.getSpeed()) >= speedView.getMaxSpeed()) {
                    speedView.setMaxSpeed(speedView.getMaxSpeed() + speedView.getMaxSpeed() / 2);
                }
                float tempSpd = (float) convertMPStoKMPH(location.getSpeed());
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
                    mTopSpd.setText("Top speed : " + convertMPStoKMPH(location.getSpeed()) + "km/h");
                    mLast_top_speed_time.setText("Reached on : " + getTime(mSharedPreferences.getLong("top_speed_last_time", 0)));
                } else {

                    mTopSpd.setText("Top speed : " + convertMPStoKMPH(mSharedPreferences.getFloat("top_spd", 0)) + "km/h");
                    mLast_top_speed_time.setText("Reached on : " + getTime(mSharedPreferences.getLong("top_speed_last_time", 0)));
                }
            } else {
                try {
                    speedView.speedTo(0);
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
                    mTopSpd.setText("Top speed : " + convertMPStoKMPH(mSharedPreferences.getFloat("top_spd", 0)) + "km/h");
                    mLast_top_speed_time.setText("Reached on : " + getTime(mSharedPreferences.getLong("top_speed_last_time", 0)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private double convertMPStoKMPH(float mps) {
        try {
            BigDecimal bd = new BigDecimal((mps * 3600) / 1000);
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
            speedView.setIndicatorColor(Color.GREEN);
            speedView.setTextColor(Color.GREEN);
            speedView.setSpeedTextColor(Color.GREEN);
            //   speedView.stop();
            mTopSpd.setText("Top speed : " + convertMPStoKMPH(mSharedPreferences.getFloat("top_spd", 0)) + "km/h");
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
            speedView.setIndicatorColor(Color.GREEN);
            speedView.setTextColor(Color.GREEN);
            speedView.setSpeedTextColor(Color.GREEN);
            //speedView.stop();
            mTopSpd.setText("Top speed : " + convertMPStoKMPH(mSharedPreferences.getFloat("top_spd", 0)) + "km/h");
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
            speedView.setIndicatorColor(Color.GREEN);
            speedView.setTextColor(Color.GREEN);
            speedView.setSpeedTextColor(Color.GREEN);
            //speedView.stop();
            mTopSpd.setText("Top speed : " + convertMPStoKMPH(mSharedPreferences.getFloat("top_spd", 0)) + "km/h");
            mLast_top_speed_time.setText("Reached on : " + getTime(mSharedPreferences.getLong("top_speed_last_time", 0)));
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
        finish();
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            // mLocationManager.removeUpdates(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
}
