package org.mintsoft.mintly.sdkoffers;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.unity3d.ads.IUnityAdsInitializationListener;
import com.unity3d.ads.IUnityAdsLoadListener;
import com.unity3d.ads.IUnityAdsShowListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.ads.metadata.PlayerMetaData;

import org.mintsoft.mintly.helper.BaseAppCompat;
import org.mintsoft.mintly.helper.Misc;
import org.mintsoft.mintly.offers.Offers;

import java.util.HashMap;

public class unityads extends BaseAppCompat {
    private String user;
    private ProgressBar progressBar;
    private HashMap<String, String> data;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        RelativeLayout layout = new RelativeLayout(this);
        RelativeLayout.LayoutParams match_parent = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
        );
        layout.setLayoutParams(match_parent);
        progressBar = new ProgressBar(this);
        progressBar.setIndeterminate(true);
        RelativeLayout.LayoutParams wrap_content = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        wrap_content.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        progressBar.setLayoutParams(wrap_content);
        layout.addView(progressBar);
        setContentView(layout);
        Intent intent = getIntent();
        data = Misc.convertToHashMap(intent, "info");
        user = intent.getStringExtra("user");
        if (data != null && user != null) {
            progressBar.setVisibility(View.VISIBLE);
            if (UnityAds.isInitialized()) {
                new Handler().postDelayed(this::showAds, 2000);
            } else {
                UnityAds.initialize((Context) this, data.get("game_id"), false, new IUnityAdsInitializationListener() {
                    @Override
                    public void onInitializationComplete() {
                        showAds();
                    }

                    @Override
                    public void onInitializationFailed(UnityAds.UnityAdsInitializationError unityAdsInitializationError, String s) {
                        Toast.makeText(unityads.this, "" + s, Toast.LENGTH_LONG).show();
                        finish();
                    }
                });
            }
        } else {
            finish();
        }
    }

    private void showAds() {
        PlayerMetaData playerMetaData = new PlayerMetaData(this);
        playerMetaData.setServerId(user);
        playerMetaData.commit();
        UnityAds.load(data.get("unit_id_r"), new IUnityAdsLoadListener() {
            @Override
            public void onUnityAdsAdLoaded(String s) {
                if (s.equals(data.get("unit_id_r"))) {
                    progressBar.setVisibility(View.GONE);
                    UnityAds.show(unityads.this, data.get("unit_id_r"), new IUnityAdsShowListener() {
                        @Override
                        public void onUnityAdsShowFailure(String placementId, UnityAds.UnityAdsShowError error, String message) {
                            Toast.makeText(unityads.this, "" + message, Toast.LENGTH_LONG).show();
                            finish();
                        }

                        @Override
                        public void onUnityAdsShowStart(String placementId) {

                        }

                        @Override
                        public void onUnityAdsShowClick(String placementId) {

                        }

                        @Override
                        public void onUnityAdsShowComplete(String placementId, UnityAds.UnityAdsShowCompletionState state) {
                            if (state == UnityAds.UnityAdsShowCompletionState.COMPLETED) {
                                Offers.checkBalance = true;
                                finish();
                            }
                        }
                    });
                }
            }

            @Override
            public void onUnityAdsFailedToLoad(String s, UnityAds.UnityAdsLoadError unityAdsLoadError, String s1) {
                Toast.makeText(unityads.this, "" + s1, Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }
}