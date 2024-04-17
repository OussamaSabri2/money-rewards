package org.mintsoft.mintly.sdkoffers;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.ironsource.adapters.supersonicads.SupersonicConfig;
import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.model.Placement;
import com.ironsource.mediationsdk.sdk.RewardedVideoListener;

import org.mintsoft.mintly.helper.BaseActivity;
import org.mintsoft.mintly.helper.Misc;
import org.mintsoft.mintly.helper.Variables;
import org.mintsoft.mintly.offers.Offers;

import java.util.HashMap;

public class ironsrc_v extends BaseActivity implements RewardedVideoListener {
    private boolean available;
    private ProgressDialog dialog;
    private HashMap<String, String> data;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Intent intent = getIntent();
        data = Misc.convertToHashMap(intent, "info");
        String user = intent.getStringExtra("user");
        if (data != null && user != null) {
            dialog = Misc.customProgress(this);
            dialog.show();
            IronSource.setUserId(user);
            SupersonicConfig.getConfigObj().setClientSideCallbacks(false);
            IronSource.setRewardedVideoListener(this);
            String ironsrc = Variables.getHash("ironsrc");
            if (ironsrc != null && ironsrc.equals("1")) {
                if (IronSource.isRewardedVideoAvailable()) {
                    available = true;
                    if (dialog.isShowing()) dialog.dismiss();
                    IronSource.showRewardedVideo(data.get("offerwall_placement"));
                } else {
                    IronSource.init(this, data.get("app_key"), () -> Variables.setHash("ironsrc", "1"), IronSource.AD_UNIT.OFFERWALL, IronSource.AD_UNIT.REWARDED_VIDEO);
                }
            } else {
                IronSource.init(this, data.get("app_key"), () -> Variables.setHash("ironsrc", "1"), IronSource.AD_UNIT.OFFERWALL, IronSource.AD_UNIT.REWARDED_VIDEO);
            }
            new Handler().postDelayed(() -> {
                if (!available) {
                    dialog.dismiss();
                    finish();
                }
            }, 15000);
        } else {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        if (dialog != null && dialog.isShowing()) dialog.dismiss();
        super.onDestroy();
    }

    @Override
    public void onRewardedVideoAdOpened() {

    }

    @Override
    public void onRewardedVideoAdClosed() {
        finish();
    }

    @Override
    public void onRewardedVideoAvailabilityChanged(boolean b) {
        if (b && !available) {
            available = true;
            if (dialog.isShowing()) dialog.dismiss();
            IronSource.showRewardedVideo(data.get("offerwall_placement"));
        }
    }

    @Override
    public void onRewardedVideoAdStarted() {

    }

    @Override
    public void onRewardedVideoAdEnded() {
        finish();
    }

    @Override
    public void onRewardedVideoAdRewarded(Placement placement) {
        Offers.checkBalance = true;
    }

    @Override
    public void onRewardedVideoAdShowFailed(IronSourceError ironSourceError) {
        Toast.makeText(this, "Error: " + ironSourceError.getErrorMessage(), Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    public void onRewardedVideoAdClicked(Placement placement) {
    }
}
