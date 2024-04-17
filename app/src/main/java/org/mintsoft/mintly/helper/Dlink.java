package org.mintsoft.mintly.helper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import androidx.preference.PreferenceManager;

import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;

import org.mintsoft.mintlib.GetAuth;
import org.mintsoft.mintly.R;
import org.mintsoft.mintly.Splash;

public class Dlink extends Activity {
    private final String parameter = "iby";

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, pendingDynamicLinkData -> {
                    Uri deepLink = null;
                    if (pendingDynamicLinkData != null) {
                        deepLink = pendingDynamicLinkData.getLink();
                    }
                    if (deepLink != null && deepLink.getBooleanQueryParameter(parameter, false)) {
                        String uid = deepLink.getQueryParameter(parameter);
                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                                .edit().putString("rfb", uid).apply();
                    }
                    startActivity(new Intent(Dlink.this, Splash.class));
                })
                .addOnFailureListener(e -> startActivity(new Intent(Dlink.this, Splash.class)));
    }

    public interface dLinkCallback {
        void onCompleted(String link);
    }

    public void create(Context context, final SharedPreferences spf, final dLinkCallback callback) {
        String link = "https://" + context.getString(R.string.domain_name) + "/refer/?" + parameter + "=" + GetAuth.user(context);
        FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse(link))
                .setDomainUriPrefix(context.getString(R.string.dynamic_link))
                .setAndroidParameters(
                        new DynamicLink.AndroidParameters.Builder(context.getPackageName())
                                .setMinimumVersion(Integer.parseInt(Variables.getPHash("ver_c")))
                                .build())
                .buildShortDynamicLink()
                .addOnSuccessListener(shortDynamicLink -> {
                    Uri uri = shortDynamicLink.getShortLink();
                    spf.edit().putString("rwlink", uri.toString()).apply();
                    if (callback != null) callback.onCompleted(uri.toString());
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onCompleted(e.getMessage());
                });
    }
}




















