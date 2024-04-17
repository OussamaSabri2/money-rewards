package org.mintsoft.mintly.sdkoffers;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.applovin.adview.AppLovinIncentivizedInterstitial;
import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.MaxReward;
import com.applovin.mediation.MaxRewardedAdListener;
import com.applovin.mediation.ads.MaxRewardedAd;
import com.applovin.sdk.AppLovinAd;
import com.applovin.sdk.AppLovinAdDisplayListener;
import com.applovin.sdk.AppLovinAdLoadListener;
import com.applovin.sdk.AppLovinPrivacySettings;
import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinSdkSettings;

import org.mintsoft.mintly.helper.Misc;
import org.mintsoft.mintly.offers.Offers;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class applovin extends Activity {
    private ProgressDialog dialog;
    private AppLovinIncentivizedInterstitial myIncent;
    private AppLovinAdDisplayListener listener;
    private AppLovinSdk instance;
    private MaxRewardedAd rewardedAd;
    private boolean loaded = false;
    private int retryAttempt;
    private Handler handler;
    private String unit, err = "Ad loading timeout!";

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Intent intent = getIntent();
        HashMap<String, String> data = Misc.convertToHashMap(intent, "info");
        String user = intent.getStringExtra("user");
        if (data != null && user != null) {
            showDialog();
            String key = data.get("sdk_key");
            if (key == null) {
                Toast.makeText(this, "Setup first", Toast.LENGTH_LONG).show();
                if (!isFinishing()) finish();
                return;
            } else {
                String[] k = key.split(",");
                if (k.length > 1) {
                    key = k[0];
                    unit = k[1];
                } else {
                    unit = data.get("unit_id");
                }
            }
            try {
                ApplicationInfo ai = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
                ai.metaData.putString("applovin.sdk.key", key);
            } catch (Exception ignored) {
            }
            AppLovinSdkSettings settings = new AppLovinSdkSettings(this);
            settings.setVerboseLogging(false);
            AppLovinPrivacySettings.setHasUserConsent(true, this);
            AppLovinPrivacySettings.setDoNotSell(true, this);
            AppLovinPrivacySettings.setIsAgeRestrictedUser(false, this);
            instance = AppLovinSdk.getInstance(key, settings, this);
            instance.setUserIdentifier(user);
            if (unit != null && !unit.isEmpty()) instance.setMediationProvider("max");
            AppLovinSdk.SdkInitializationListener sdkListener = config -> {
                if (isFinishing() || isDestroyed()) return;
                if (unit == null || unit.isEmpty()) {
                    myIncent = AppLovinIncentivizedInterstitial.create(instance);
                    listener = new AppLovinAdDisplayListener() {
                        @Override
                        public void adDisplayed(AppLovinAd ad) {

                        }

                        @Override
                        public void adHidden(AppLovinAd ad) {
                            myIncent.preload(null);
                            if (dialog.isShowing()) dialog.dismiss();
                            if (!isFinishing()) finish();
                        }
                    };
                    myIncent.preload(new AppLovinAdLoadListener() {
                        @Override
                        public void adReceived(AppLovinAd appLovinAd) {
                            if (dialog.isShowing()) dialog.dismiss();
                            myIncent.show(appLovinAd, applovin.this, null,
                                    null, listener, null);
                            Offers.checkBalance = true;
                        }

                        @Override
                        public void failedToReceiveAd(int errorCode) {
                            if (dialog.isShowing()) dialog.dismiss();
                            uiToast("Error code: " + errorCode);
                        }
                    });
                } else {
                    rewardedAd = MaxRewardedAd.getInstance(unit, instance, applovin.this);
                    rewardedAd.setListener(new MaxRewardedAdListener() {
                        @Override
                        public void onRewardedVideoStarted(MaxAd ad) {
                            if (dialog.isShowing()) dialog.dismiss();
                        }

                        @Override
                        public void onRewardedVideoCompleted(MaxAd ad) {
                            if (!isFinishing()) finish();
                        }

                        @Override
                        public void onUserRewarded(MaxAd ad, MaxReward reward) {
                            Offers.checkBalance = true;
                        }

                        @Override
                        public void onAdLoaded(MaxAd ad) {
                            if (dialog.isShowing()) dialog.dismiss();
                            if (!loaded) {
                                loaded = true;
                                rewardedAd.showAd();
                            }
                        }

                        @Override
                        public void onAdDisplayed(MaxAd ad) {

                        }

                        @Override
                        public void onAdHidden(MaxAd ad) {
                            if (!loaded) rewardedAd.loadAd();
                        }

                        @Override
                        public void onAdClicked(MaxAd ad) {

                        }

                        @Override
                        public void onAdLoadFailed(String adUnitId, MaxError error) {
                            if (retryAttempt < 5) {
                                retryAttempt++;
                                long delayMillis = TimeUnit.SECONDS.toMillis((long) Math.pow(2, Math.min(6, retryAttempt)));
                                new Handler().postDelayed(() -> {
                                    if (!loaded) rewardedAd.loadAd();
                                }, delayMillis);
                                err = "ErrorCode: " + error.getCode() + " message" + error.getMessage();
                            } else {
                                uiToast("" + error.getMessage());
                            }
                        }

                        @Override
                        public void onAdDisplayFailed(MaxAd ad, MaxError error) {
                            uiToast("" + error.getMessage());
                        }
                    });
                    if (!loaded) rewardedAd.loadAd();
                    handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (isFinishing() || isDestroyed()) return;
                            if (!loaded) {
                                if (rewardedAd.isReady()) {
                                    loaded = true;
                                    rewardedAd.showAd();
                                } else {
                                    handler.postDelayed(this, 3000);
                                }
                            }
                        }
                    }, 3000);
                }
            };
            instance.initializeSdk(sdkListener);
            new Handler().postDelayed(() -> runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) return;
                if (dialog.isShowing()) dialog.dismiss();
                if (instance != null && !instance.isInitialized()) {
                    Toast.makeText(applovin.this, "Could not initialize SDK", Toast.LENGTH_LONG).show();
                } else if (!loaded) {
                    Toast.makeText(applovin.this, err, Toast.LENGTH_LONG).show();
                }
                finish();
            }), 64000);
        } else {
            finish();
        }
    }

    private void showDialog() {
        dialog = Misc.customProgress(this);
        dialog.show();
    }

    private void uiToast(final String toast) {
        runOnUiThread(() -> {
            if (dialog.isShowing()) dialog.dismiss();
            Toast.makeText(applovin.this, toast, Toast.LENGTH_LONG).show();
            if (!isFinishing()) finish();
        });
    }

    @Override
    public void onDestroy() {
        if (dialog.isShowing()) dialog.dismiss();
        super.onDestroy();
    }
}