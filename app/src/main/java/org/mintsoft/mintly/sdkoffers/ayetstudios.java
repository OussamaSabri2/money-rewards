package org.mintsoft.mintly.sdkoffers;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.ayetstudios.publishersdk.AyetSdk;
import com.ayetstudios.publishersdk.interfaces.UserBalanceCallback;
import com.ayetstudios.publishersdk.messages.SdkUserBalance;

import org.mintsoft.mintly.helper.BaseActivity;
import org.mintsoft.mintly.helper.Misc;
import org.mintsoft.mintly.offers.Offers;

import java.util.HashMap;

public class ayetstudios extends BaseActivity {
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Intent intent = getIntent();
        HashMap<String, String> data = Misc.convertToHashMap(intent, "info");
        String user = intent.getStringExtra("user");
        if (data != null && user != null) {
            showDialog();
            try {
                AyetSdk.init(getApplication(), user, new UserBalanceCallback() {
                    @Override
                    public void userBalanceChanged(SdkUserBalance sdkUserBalance) {

                    }

                    @Override
                    public void userBalanceInitialized(SdkUserBalance sdkUserBalance) {
                        AyetSdk.showOfferwall(getApplication(), data.get("slot_name"));
                        new Handler().postDelayed(() -> {
                            if (dialog.isShowing()) dialog.dismiss();
                            finish();
                        }, 1000);
                        Offers.checkBalance = true;
                    }

                    @Override
                    public void initializationFailed() {
                        if (dialog.isShowing()) dialog.dismiss();
                        Toast.makeText(ayetstudios.this, "Could not connect! Did you set your APP API KEY?", Toast.LENGTH_LONG).show();
                        finish();
                    }
                }, data.get("app_key"));
            } catch (Exception e) {
                Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        } else {
            finish();
        }
    }

    private void showDialog() {
        dialog = Misc.customProgress(this);
        dialog.show();
    }
}