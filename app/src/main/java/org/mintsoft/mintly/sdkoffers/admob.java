package org.mintsoft.mintly.sdkoffers;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.ads.rewarded.ServerSideVerificationOptions;

import org.mintsoft.mintly.helper.Misc;
import org.mintsoft.mintly.offers.Offers;

import java.util.HashMap;
import java.util.Objects;

public class admob extends Activity {
    private ProgressDialog dialog;
    private String slot, user;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Intent intent = getIntent();
        HashMap<String, String> data = Misc.convertToHashMap(intent, "info");
        user = intent.getStringExtra("user");
        if (data != null && user != null) {
            dialog = Misc.customProgress(this);
            dialog.show();
            slot = data.get("rewarded_slot");
            try {
                String app_id = data.get("app_id");
                if (app_id != null) {
                    ApplicationInfo ai = getPackageManager()
                            .getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
                    ai.metaData.putString("com.google.android.gms.ads.APPLICATION_ID", app_id);
                }
                MobileAds.initialize(this);
                new Handler().postDelayed(() -> runOnUiThread(this::loadAds), 2000);
            } catch (Exception e) {
                Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_LONG).show();
                finish();
            }
        } else {
            finish();
        }
    }

    private void loadAds() {
        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedAd.load(this, Objects.requireNonNull(slot),
                adRequest, new RewardedAdLoadCallback() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        dialog.dismiss();
                        if (loadAdError.getCode() == 3) {
                            uiToast("Ads not available");
                        } else {
                            uiToast("Code: " + loadAdError.getCode() + ". Message:" + loadAdError.getMessage());
                        }
                        finish();
                    }

                    @Override
                    public void onAdLoaded(@NonNull RewardedAd ad) {
                        dialog.dismiss();
                        ServerSideVerificationOptions options = new ServerSideVerificationOptions.Builder().setUserId(user).build();
                        ad.setServerSideVerificationOptions(options);
                        ad.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdShowedFullScreenContent() {
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                                uiToast("" + adError.getMessage());
                            }

                            @Override
                            public void onAdDismissedFullScreenContent() {
                                finish();
                            }
                        });
                        ad.show(admob.this, rewardItem -> {
                            Offers.checkBalance = true;
                        });
                    }
                });
    }

    private void uiToast(final String toast) {
        runOnUiThread(() -> Toast.makeText(admob.this, toast, Toast.LENGTH_LONG).show());
    }
}