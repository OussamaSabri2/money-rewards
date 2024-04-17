package org.mintsoft.mintly.helper;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.TextView;

import org.mintsoft.mintly.Home;
import org.mintsoft.mintly.R;

public class GlobalMsg extends BaseAppCompat {
    private String msgId;
    private CheckBox checkBox;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        try {
            Bundle extras = getIntent().getExtras();
            assert extras != null;
            String title = extras.getString("title");
            setContentView(R.layout.global_msg);
            if (title != null) {
                TextView titleView = findViewById(R.id.global_msg_title);
                titleView.setText(Misc.html(title));
            }
            TextView desc = findViewById(R.id.global_msg_desc);
            desc.setText(Misc.html(extras.getString("info")));
            msgId = extras.getString("id");
            checkBox = findViewById(R.id.global_msg_btn_checkbox);
            findViewById(R.id.global_msg_btn_ok).setOnClickListener(view -> {
                if (msgId != null && checkBox.isChecked()) {
                    Home.spf.edit().putString("rmid", msgId).apply();
                }
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });
        } catch (Exception e) {
            finish();
        }
    }

    @Override
    public void onBackPressed() {
    }
}