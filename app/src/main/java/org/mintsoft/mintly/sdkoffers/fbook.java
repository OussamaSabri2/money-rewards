package org.mintsoft.mintly.sdkoffers;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdSettings;
import com.facebook.ads.AudienceNetworkAds;
import com.facebook.ads.RewardData;
import com.facebook.ads.RewardedVideoAd;
import com.facebook.ads.RewardedVideoAdListener;

import org.mintsoft.mintly.helper.BaseActivity;
import org.mintsoft.mintly.helper.Misc;
import org.mintsoft.mintly.helper.Variables;
import org.mintsoft.mintly.offers.Offers;

import java.util.HashMap;

public class fbook extends BaseActivity implements AudienceNetworkAds.InitListener, RewardedVideoAdListener {
    private ProgressDialog dialog;
    private String user;
    private RewardedVideoAd rewardedVideoAd;
    private HashMap<String, String> data;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Intent intent = getIntent();
        data = Misc.convertToHashMap(intent, "info");
        user = intent.getStringExtra("user");
        if (data != null && user != null) {
            dialog = Misc.customProgress(this);
            dialog.show();
            if (AudienceNetworkAds.isInitialized(getApplicationContext())) {
                loadAd();
            } else {
                if (Variables.getPHash("debug").equals("1")) AdSettings.turnOnSDKDebugger(this);
                AudienceNetworkAds.buildInitSettings(getApplicationContext()).withInitListener(this).initialize();
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (rewardedVideoAd != null) {
            rewardedVideoAd.destroy();
            rewardedVideoAd = null;
        }
        super.onDestroy();
    }

    @Override
    public void onInitialized(AudienceNetworkAds.InitResult initResult) {
        loadAd();
    }

    private void loadAd() {
        rewardedVideoAd = new RewardedVideoAd(this, data.get("placement_id"));
        RewardData rewardData = new RewardData(user, "1");
        rewardedVideoAd.loadAd(rewardedVideoAd.buildLoadAdConfig()
                .withAdListener(this).withRewardData(rewardData).build());
    }

    @Override
    public void onRewardedVideoCompleted() {
        Offers.checkBalance = true;
    }

    @Override
    public void onRewardedVideoClosed() {
        finish();
    }

    @Override
    public void onError(Ad ad, AdError adError) {
        dialog.dismiss();
        Toast.makeText(this, "Error: " + adError.getErrorMessage(), Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    public void onAdLoaded(Ad ad) {
        dialog.dismiss();
        rewardedVideoAd.show();
    }

    @Override
    public void onAdClicked(Ad ad) {

    }

    @Override
    public void onLoggingImpression(Ad ad) {

    }
}