package org.mintsoft.mintly.sdkoffers;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.vungle.warren.AdConfig;
import com.vungle.warren.InitCallback;
import com.vungle.warren.LoadAdCallback;
import com.vungle.warren.PlayAdCallback;
import com.vungle.warren.Vungle;
import com.vungle.warren.error.VungleException;

import org.mintsoft.mintly.helper.BaseActivity;
import org.mintsoft.mintly.helper.Misc;
import org.mintsoft.mintly.offers.Offers;

import java.util.HashMap;

public class vungle extends BaseActivity {
    private ProgressDialog dialog;
    private LoadAdCallback loadAdCallback;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Intent intent = getIntent();
        HashMap<String, String> data = Misc.convertToHashMap(intent, "info");
        String user = intent.getStringExtra("user");
        if (data != null && user != null) {
            showDialog();
            loadAdCallback = new LoadAdCallback() {
                @Override
                public void onAdLoad(String id) {
                    if (dialog.isShowing()) dialog.dismiss();
                    AdConfig adConfig = new AdConfig();
                    adConfig.setAdOrientation(AdConfig.PORTRAIT);
                    Vungle.playAd(id, adConfig, new PlayAdCallback() {
                        @Override
                        public void creativeId(String creativeId) {

                        }

                        @Override
                        public void onAdStart(String placementId) {

                        }

                        @Override
                        public void onAdEnd(String placementId, boolean completed, boolean isCTAClicked) {
                            if (completed) {
                                Offers.checkBalance = true;
                                finish();
                            }
                        }

                        @Override
                        public void onAdEnd(String placementId) {

                        }

                        @Override
                        public void onAdClick(String placementId) {

                        }

                        @Override
                        public void onAdRewarded(String placementId) {

                        }

                        @Override
                        public void onAdLeftApplication(String placementId) {

                        }

                        @Override
                        public void onError(String placementId, VungleException exception) {
                            uiToast("" + exception.getMessage());
                        }

                        @Override
                        public void onAdViewed(String placementId) {

                        }
                    });
                }

                @Override
                public void onError(String id, VungleException exception) {
                    if (dialog.isShowing()) dialog.dismiss();
                    uiToast("Loading failed: " + exception.getMessage());
                    finish();
                }
            };
            InitCallback initCallback = new InitCallback() {
                @Override
                public void onSuccess() {
                    Vungle.setIncentivizedFields(user, null, null, null, "Close");
                    Vungle.loadAd(data.get("placement"), loadAdCallback);
                }

                @Override
                public void onError(VungleException exception) {
                    if (dialog.isShowing()) dialog.dismiss();
                    uiToast("Initialization failed: " + exception.getMessage());
                    finish();
                }

                @Override
                public void onAutoCacheAdAvailable(String placementId) {

                }
            };
            Vungle.init(data.get("app_id"), getApplicationContext(), initCallback);
        } else {
            finish();
        }
    }

    private void showDialog() {
        dialog = Misc.customProgress(this);
        dialog.show();
    }

    private void uiToast(final String toast) {
        runOnUiThread(() -> Toast.makeText(vungle.this, toast, Toast.LENGTH_LONG).show());
    }

    @Override
    public void onDestroy() {
        if (dialog.isShowing()) dialog.dismiss();
        super.onDestroy();
    }
}
