package com.tweekerz.ind.virtualspeedo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class Privacy extends AppCompatActivity {
    private WebView mWv1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_privacy);
            mWv1 = (WebView) findViewById(R.id.wv1);
            WebSettings settings = mWv1.getSettings();
            settings.setBuiltInZoomControls(true);
            settings.setDomStorageEnabled(true);
            settings.setJavaScriptCanOpenWindowsAutomatically(true);
            settings.setJavaScriptEnabled(true);
            mWv1.loadUrl("file:///android_asset/privacy_policy.html");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
