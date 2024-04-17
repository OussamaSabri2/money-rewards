package org.mintsoft.mintly.sdkoffers;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.tapjoy.TJActionRequest;
import com.tapjoy.TJConnectListener;
import com.tapjoy.TJError;
import com.tapjoy.TJPlacement;
import com.tapjoy.TJPlacementListener;
import com.tapjoy.TJSetUserIDListener;
import com.tapjoy.Tapjoy;
import com.tapjoy.TapjoyConnectFlag;

import org.mintsoft.mintly.R;
import org.mintsoft.mintly.helper.BaseActivity;
import org.mintsoft.mintly.helper.Misc;
import org.mintsoft.mintly.offers.Offers;

import java.util.HashMap;
import java.util.Hashtable;

public class tapjoy extends BaseActivity {
    private TJSetUserIDListener tjSetUserIDListener;
    private TJConnectListener tjConnectListener;
    private TJPlacement tjPlacement;
    private TJPlacementListener tjPlacementListener;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Intent intent = getIntent();
        HashMap<String, String> data = Misc.convertToHashMap(intent, "info");
        String user = intent.getStringExtra("user");
        if (data != null && user != null) {
            showDialog();
            tjPlacementListener = new TJPlacementListener() {
                @Override
                public void onRequestSuccess(TJPlacement tjPlacement) {
                    if (!tjPlacement.isContentAvailable()) {
                        if (dialog.isShowing()) dialog.dismiss();
                        uiToast(tapjoy.this, getString(R.string.ad_not_available));
                        finish();
                    }
                }

                @Override
                public void onRequestFailure(TJPlacement tjPlacement, TJError tjError) {
                    if (dialog.isShowing()) dialog.dismiss();
                    uiToast(tapjoy.this, tjError.message);
                    finish();
                }

                @Override
                public void onContentReady(TJPlacement tjPlacement) {
                    tjPlacement.showContent();
                    if (dialog.isShowing()) dialog.dismiss();
                    Offers.checkBalance = true;
                }

                @Override
                public void onContentShow(TJPlacement tjPlacement) {

                }

                @Override
                public void onContentDismiss(TJPlacement tjPlacement) {
                    finish();
                }

                @Override
                public void onPurchaseRequest(TJPlacement tjPlacement, TJActionRequest tjActionRequest, String s) {

                }

                @Override
                public void onRewardRequest(TJPlacement tjPlacement, TJActionRequest tjActionRequest, String s, int i) {

                }

                @Override
                public void onClick(TJPlacement tjPlacement) {

                }
            };
            tjPlacement = new TJPlacement(this, data.get("placement_name"), tjPlacementListener);
            tjSetUserIDListener = new TJSetUserIDListener() {
                @Override
                public void onSetUserIDSuccess() {
                    if (!isFinishing() && !isDestroyed())
                        tjPlacement.requestContent();
                }

                @Override
                public void onSetUserIDFailure(String s) {
                    if (dialog.isShowing()) dialog.dismiss();
                    uiToast(tapjoy.this, "" + s);
                    finish();
                }
            };
            tjConnectListener = new TJConnectListener() {
                @Override
                public void onConnectSuccess() {
                    Tapjoy.setUserID(user, tjSetUserIDListener);
                }

                @Override
                public void onConnectFailure() {
                    if (dialog.isShowing()) dialog.dismiss();
                    finish();
                }
            };
            Hashtable<String, Object> connectFlags = new Hashtable<>();
            connectFlags.put(TapjoyConnectFlag.ENABLE_LOGGING, "true");
            Tapjoy.connect(this, data.get("sdk_key"), connectFlags, tjConnectListener);
        } else {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        tjPlacementListener = null;
        tjPlacement = null;
        tjSetUserIDListener = null;
        tjConnectListener = null;
        super.onDestroy();
    }

    private void showDialog() {
        dialog = Misc.customProgress(this);
        dialog.show();
    }

    private void uiToast(final Activity context, final String toast) {
        context.runOnUiThread(() -> Toast.makeText(context, toast, Toast.LENGTH_LONG).show());
    }
}