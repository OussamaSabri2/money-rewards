package org.mintsoft.mintly.account;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.mintsoft.mintlib.GetAuth;
import org.mintsoft.mintlib.GetURL;
import org.mintsoft.mintlib.onResponse;
import org.mintsoft.mintly.Home;
import org.mintsoft.mintly.R;
import org.mintsoft.mintly.helper.BaseAppCompat;
import org.mintsoft.mintly.helper.Dlink;
import org.mintsoft.mintly.helper.Misc;

import java.util.HashMap;

public class Refs extends BaseAppCompat {
    private String dLink, code;
    private Dialog conDiag, loadingDiag;
    private ImageView copyLinkBtn;
    private TextView refAmtView, descView, urlView;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        loadingDiag = Misc.loadingDiag(this);
        loadingDiag.show();
        getWindow().setNavigationBarColor(Color.WHITE);
        setContentView(R.layout.refs);
        TextView refCodeView = findViewById(R.id.refs_codeView);
        urlView = findViewById(R.id.refs_refUrl_inputView);
        refAmtView = findViewById(R.id.refs_referrer_amtView);
        descView = findViewById(R.id.refs_descView);
        code = GetAuth.user(Refs.this);
        refCodeView.setText(code);
        copyLinkBtn = findViewById(R.id.refs_copyLink_btn);
        //urlView.setText(getString(R.string.please_wait));
        dLink = Home.spf.getString("rwlink", null);
        if (dLink == null) {
            new Dlink().create(this, Home.spf, link -> {
                dLink = link;
                setLink();
            });
        } else {
            setLink();
        }
        findViewById(R.id.refs_copyBtn).setOnClickListener(view -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("ReferralCode", code);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(Refs.this, getString(R.string.ref_code_copied), Toast.LENGTH_LONG).show();
        });
        findViewById(R.id.refs_go_history).setOnClickListener(view ->
                startActivity(new Intent(Refs.this, rHistory.class))
        );
        findViewById(R.id.refs_back).setOnClickListener(view -> finish());
        findViewById(R.id.refs_go_telegram).setOnClickListener(view -> {
            if (dLink == null) {
                Toast.makeText(Refs.this, getString(R.string.ref_link_not_ready), Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.setPackage("org.telegram.messenger");
                intent.putExtra(Intent.EXTRA_SUBJECT, "Join " + getString(R.string.app_name) + ":");
                intent.putExtra(Intent.EXTRA_TEXT, dLink);
                try {
                    startActivity(intent);
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(Refs.this, "Telegram is not been installed.", Toast.LENGTH_LONG).show();
                }
            }
        });
        findViewById(R.id.refs_go_facebook).setOnClickListener(view -> {
            if (dLink == null) {
                Toast.makeText(Refs.this, getString(R.string.ref_link_not_ready), Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.setPackage("com.facebook.katana");
                intent.putExtra(Intent.EXTRA_SUBJECT, "Join " + getString(R.string.app_name) + ":");
                intent.putExtra(Intent.EXTRA_TEXT, dLink);
                try {
                    startActivity(intent);
                } catch (android.content.ActivityNotFoundException e) {
                    try {
                        intent.setPackage("com.facebook.lite");
                        startActivity(intent);
                    } catch (android.content.ActivityNotFoundException ex) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/sharer/sharer.php?u=" + dLink)));
                    }
                }
            }
        });
        findViewById(R.id.refs_go_whatsapp).setOnClickListener(view -> {
            if (dLink == null) {
                Toast.makeText(Refs.this, getString(R.string.ref_link_not_ready), Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.setPackage("com.whatsapp");
                intent.putExtra(Intent.EXTRA_SUBJECT, "Join " + getString(R.string.app_name) + ":");
                intent.putExtra(Intent.EXTRA_TEXT, dLink);
                try {
                    startActivity(intent);
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(Refs.this, "Whatsapp is not been installed.", Toast.LENGTH_LONG).show();
                }
            }
        });
        callNet();
    }

    private void callNet() {
        if (!loadingDiag.isShowing()) loadingDiag.show();
        GetURL.getRef(this, new onResponse() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onSuccessHashMap(HashMap<String, String> data) {
                refAmtView.setText(data.get("ref"));
                descView.setText((getString(R.string.ref_desc_1) + " "
                        + data.get("user") + " " + Home.currency.toLowerCase() + "s "
                        + getString(R.string.ref_desc_2) + " "
                        + data.get("ref") + " " + Home.currency.toLowerCase() + "s "
                        + getString(R.string.ref_desc_3)
                ));
                loadingDiag.dismiss();
            }

            @Override
            public void onError(int errorCode, String error) {
                loadingDiag.dismiss();
                if (errorCode == -9) {
                    conDiag = Misc.noConnection(conDiag, Refs.this, () -> {
                        callNet();
                        conDiag.dismiss();
                    });
                } else {
                    Toast.makeText(Refs.this, error, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void setLink() {
        urlView.setText(dLink);
        urlView.setTypeface(Typeface.SANS_SERIF);
        urlView.setPadding(Misc.dpToPx(this, 10), 0, Misc.dpToPx(this, 42), 0);
        copyLinkBtn.setVisibility(View.VISIBLE);
        copyLinkBtn.setOnClickListener(view -> {
            if (dLink != null) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("ReferralLink", urlView.getText().toString());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(Refs.this, getString(R.string.ref_link_copied), Toast.LENGTH_LONG).show();
            }
        });
    }


}