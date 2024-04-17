package org.mintsoft.mintly;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.preference.PreferenceManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.mintsoft.mintlib.GetURL;
import org.mintsoft.mintlib.onResponse;
import org.mintsoft.mintly.helper.BaseAppCompat;
import org.mintsoft.mintly.helper.Misc;

import java.util.HashMap;
import java.util.Objects;

public class Tos extends BaseAppCompat {
    private Dialog dialog;
    private TextView tosView;
    private Button acceptBtn;
    private boolean block;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.tos);
        dialog = Misc.loadingDiag(this);
        dialog.show();
        TextView titleView = findViewById(R.id.tos_titleView);
        Misc.setLogo(this, titleView);
        tosView = findViewById(R.id.tos_textView);
        acceptBtn = findViewById(R.id.tos_accept);
        GetURL.getTos(this, new onResponse() {
            @Override
            public void onSuccessHashMap(HashMap<String, String> data) {
                StringBuilder sb = new StringBuilder();
                if (!Objects.equals(data.get("t"), "")) {
                    sb.append("<div style='text-align:center'><h4><font color='#01b1ec'>Terms and Conditions</font>");
                    sb.append("<br>________________________________</h4></div>");
                    sb.append(data.get("t"));
                }
                if (!Objects.equals(data.get("p"), "")) {
                    sb.append("<br><br><br><br><div style='text-align:center'><h4><font color='#01b1ec'>Privacy Policy</font>");
                    sb.append("<br>________________________________</h4></div>");
                    sb.append(data.get("p"));
                }
                tosView.setText(Misc.html(sb.toString()));
                dialog.dismiss();
            }

            @Override
            public void onError(int errorCode, String error) {
                dialog.dismiss();
                Toast.makeText(Tos.this, error, Toast.LENGTH_LONG).show();
            }
        });
        findViewById(R.id.tos_reject).setOnClickListener(view -> {
            if (!block) finish();
        });
        acceptBtn.setOnClickListener(view -> {
            acceptBtn.setText(getString(R.string.please_wait));
            if (!block) {
                block = true;
                startActivity(new Intent(Tos.this, Home.class));
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                        .edit().putBoolean("tos", true).apply();
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
    }
}