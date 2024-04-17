package org.mintsoft.mintly.sdkoffers;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.chartboost.sdk.CBLocation;
import com.chartboost.sdk.Chartboost;
import com.chartboost.sdk.ChartboostDelegate;
import com.chartboost.sdk.Model.CBError;

import org.mintsoft.mintly.helper.Misc;
import org.mintsoft.mintly.offers.Offers;

import java.util.HashMap;
import java.util.Objects;

public class chartboost extends Activity {
    private boolean shownAd = false;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Intent intent = getIntent();
        HashMap<String, String> data = Misc.convertToHashMap(intent, "info");
        String user = intent.getStringExtra("user");
        if (data != null && user != null) {
            showDialog();

            Chartboost.setCustomId(user);
            ChartboostDelegate delegate = new ChartboostDelegate() {
                @Override
                public boolean shouldDisplayRewardedVideo(String location) {
                    super.shouldDisplayRewardedVideo(location);
                    if (dialog.isShowing()) dialog.dismiss();
                    return true;
                }

                @Override
                public void didInitialize() {
                    super.didInitialize();
                    if (!shownAd) {
                        shownAd = true;
                        Chartboost.showRewardedVideo(CBLocation.LOCATION_DEFAULT);
                    }
                }

                @Override
                public void didFailToLoadRewardedVideo(String location, CBError.CBImpressionError error) {
                    super.didFailToLoadRewardedVideo(location, error);
                    uiToast("" + error);
                    finish();
                }

                @Override
                public void didCloseRewardedVideo(String location) {
                    super.didCloseRewardedVideo(location);
                    if (Objects.equals(location, CBLocation.LOCATION_DEFAULT)) {
                        Offers.checkBalance = true;
                        finish();
                    }
                }
            };
            Chartboost.setDelegate(delegate);
            Chartboost.startWithAppId((Context) this, data.get("app_id"), data.get("app_signature"));
        } else {
            finish();
        }
    }

    private void showDialog() {
        dialog = Misc.customProgress(this);
        dialog.show();
    }

    private void uiToast(final String toast) {
        runOnUiThread(() -> Toast.makeText(chartboost.this, toast, Toast.LENGTH_LONG).show());
    }

    @Override
    public void onDestroy() {
        if (dialog.isShowing()) dialog.dismiss();
        super.onDestroy();
    }
}