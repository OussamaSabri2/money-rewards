package org.mintsoft.mintly;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.android.installreferrer.api.InstallReferrerClient;
import com.android.installreferrer.api.InstallReferrerStateListener;
import com.android.installreferrer.api.ReferrerDetails;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;

import org.mintsoft.mintlib.GetURL;
import org.mintsoft.mintlib.onResponse;
import org.mintsoft.mintly.account.Login;
import org.mintsoft.mintly.helper.Misc;
import org.mintsoft.mintly.helper.Variables;

import java.util.HashMap;

public class Splash extends AppCompatActivity {
    private SharedPreferences spf;
    private String cc, irc;
    private Dialog dialog;
    private TextView textView;
    private ActivityResultLauncher<String> activityForResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        textView = findViewById(R.id.splash_text);
        Misc.setLogo(this, textView);
        spf = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        cc = spf.getString("cc", null);
        Variables.reset();
        if (!spf.getBoolean("irc", false)) {
            InstallReferrerClient client = InstallReferrerClient.newBuilder(this).build();
            client.startConnection(new InstallReferrerStateListener() {
                @Override
                public void onInstallReferrerSetupFinished(int responseCode) {
                    spf.edit().putBoolean("irc", true).apply();
                    if (responseCode == InstallReferrerClient.InstallReferrerResponse.OK) {
                        try {
                            ReferrerDetails response = client.getInstallReferrer();
                            irc = response.getInstallReferrer();
                            client.endConnection();
                        } catch (RemoteException ignored) {
                        }
                    }
                }

                @Override
                public void onInstallReferrerServiceDisconnected() {
                }
            });
        }
        try {
            FirebaseApp.initializeApp(getApplicationContext());
            FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
            firebaseAppCheck.installAppCheckProviderFactory(PlayIntegrityAppCheckProviderFactory.getInstance());
        } catch (Exception ignored) {
        }
        activityForResult = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
            if (Build.VERSION.SDK_INT >= 33) {
                if (shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)) {
                    showPrDiag();
                } else {
                    if (result) spf.edit().putBoolean("nfprm", true).apply();
                    beginProcess();
                }
            } else {
                beginProcess();
            }
        });
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (getResources().getBoolean(R.bool.show_translation_dialog)) {
            String lc = spf.getString("app_locale", null);
            if (lc == null) {
                Misc.chooseLocale(this, spf, new Misc.yesNo() {
                    @Override
                    public void yes() {
                        Intent mIntent = new Intent(Splash.this, Splash.class);
                        finish();
                        startActivity(mIntent);
                    }

                    @Override
                    public void no() {
                        startProcess();
                    }
                });
            } else {
                Variables.locale = lc;
                Misc.setLocale(this, lc);
                textView.setText(getString(R.string.app_name));
                Misc.setLogo(this, textView);
                startProcess();
            }
        } else {
            startProcess();
        }
    }

    private void startProcess() {
        if (!spf.getBoolean("nfprm", false) && Build.VERSION.SDK_INT >= 33 && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            showPrDiag();
        } else {
            beginProcess();
        }
    }

    private void beginProcess() {
        AsyncTask.execute(() -> {
            try {
                AdvertisingIdClient.Info adInfo = AdvertisingIdClient.getAdvertisingIdInfo(getApplicationContext());
                String adId = adInfo.getId();
                spf.edit().putString("gid", adId).apply();
            } catch (Exception ignored) {
            }
        });
        try {
            TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            cc = tm.getSimCountryIso();
            if (cc == null || cc.length() != 2) {
                getCC();
            } else {
                call();
            }
        } catch (Exception e) {
            if (cc == null || cc.length() != 2) getCC();
        }
    }

    private void showPrDiag() {
        final Dialog permDiag = Misc.decoratedDiag(this, R.layout.dialog_perm, 1f);
        int width = (int) (getResources().getDisplayMetrics().widthPixels);
        int height = (int) (getResources().getDisplayMetrics().heightPixels);
        Window w = permDiag.getWindow();
        if (w != null) w.setLayout(width, height);
        permDiag.findViewById(R.id.dialog_perm_allow).setOnClickListener(view -> {
            permDiag.dismiss();
            if (Build.VERSION.SDK_INT >= 33) {
                activityForResult.launch(android.Manifest.permission.POST_NOTIFICATIONS);
            } else {
                beginProcess();
            }
        });
        permDiag.findViewById(R.id.dialog_perm_no).setOnClickListener(view -> {
            spf.edit().putBoolean("nfprm", true).apply();
            permDiag.dismiss();
            beginProcess();
        });
        permDiag.show();
    }


    private void getCC() {
        GetURL.getCc(this, getString(R.string.domain_name), new onResponse() {
            @Override
            public void onSuccess(String response) {
                if (response != null && response.length() == 2) cc = response;
                call();
            }

            @Override
            public void onError(int errorCode, String error) {
                call();
            }
        });
    }

    private void call() {
        if (cc == null || cc.length() != 2) {
            cc = "us";
        } else {
            cc = cc.toLowerCase();
        }
        spf.edit().putString("cc", cc).apply();
        new Handler().postDelayed(() -> GetURL.app_java(Splash.this,
                "https://" + getString(R.string.domain_name), getInfo(), spf, cc, new onResponse() {
                    @Override
                    public void onSuccess(String response) {
                        postRedirect(true, response);
                    }

                    @Override
                    public void onError(int errorCode, String error) {
                        if (errorCode == -9) {
                            dialog = Misc.noConnection(dialog, Splash.this, () -> {
                                call();
                                dialog.dismiss();
                            });
                            if (error.startsWith("Er:")) {
                                TextView dVw = dialog.findViewById(R.id.dialog_connection_desc);
                                dVw.setText(error.replace("Er:", ""));
                                dVw.setTextColor(Color.YELLOW);
                            }
                        } else if (errorCode == -10) {
                            Misc.lockedDiag(Splash.this, error, getString(R.string.unsupported_device_desc));
                        } else if (errorCode == -2) {
                            Misc.lockedDiag(Splash.this, getString(R.string.user_banned), error);
                        } else if (errorCode == -1) {
                            postRedirect(false, error);
                        } else {
                            Toast.makeText(Splash.this, error, Toast.LENGTH_LONG).show();
                            finish();
                        }

                    }
                }), 1000);
    }

    public HashMap<String, String> getInfo() {
        HashMap<String, String> data = new HashMap<>();
        data.put("cc", spf.getString("cc", cc));
        data.put("lang", Variables.locale == null ? "en" : Variables.locale);
        data.put("referrer", irc == null ? "none" : irc);
        return data;
    }

    private void postRedirect(boolean isSuccess, String d) {
        String[] data = d.split(",");
        Variables.isLive = true;
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            Variables.setPHash("ver_n", pInfo.versionName);
            Variables.setPHash("ver_c", String.valueOf(pInfo.versionCode));
            boolean isDebuggable = 0 != (getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE);
            Variables.setPHash("debug", isDebuggable ? "1" : "0");
            if (pInfo.versionCode < Integer.parseInt(data[0])) {
                if (data[1].equals("1")) {
                    Misc.lockedDiag(Splash.this, getString(R.string.outdated_version), getString(R.string.outdated_version_desc));
                } else {
                    showQuitDiag(isSuccess);
                }
            } else {
                if (isSuccess) {
                    if (spf.getBoolean("tos", false)) {
                        startActivity(new Intent(this, Home.class));
                    } else {
                        startActivity(new Intent(this, Tos.class));
                    }
                    finish();
                } else {
                    startActivity(new Intent(Splash.this, Login.class));
                }
                finish();
            }
        } catch (Exception e) {
            Variables.setPHash("ver_n", "1.0");
            Variables.setPHash("ver_c", String.valueOf(1));
            boolean isDebuggable = 0 != (getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE);
            Variables.setPHash("debug", isDebuggable ? "1" : "0");
            if (isSuccess) {
                if (spf.getBoolean("tos", false)) {
                    startActivity(new Intent(this, Home.class));
                } else {
                    startActivity(new Intent(this, Tos.class));
                }
                finish();
            } else {
                startActivity(new Intent(Splash.this, Login.class));
            }
            finish();
        }
    }

    private void showQuitDiag(boolean isSuccess) {
        Dialog lokDiag = new Dialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        View lowBalView = LayoutInflater.from(this).inflate(R.layout.dialog_connection, null);
        lokDiag.setContentView(lowBalView);
        lokDiag.setCancelable(false);
        Window w = lokDiag.getWindow();
        //if (w != null) {
        w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        w.setNavigationBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        w.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        //}
        ImageView lokImgView = lowBalView.findViewById(R.id.dialog_connection_img);
        lokImgView.setImageResource(R.drawable.ic_warning);
        TextView lokTitleView = lowBalView.findViewById(R.id.dialog_connection_title);
        lokTitleView.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_light));
        lokTitleView.setText(getString(R.string.outdated_version));
        TextView lokDescView = lowBalView.findViewById(R.id.dialog_connection_desc);
        lokDescView.setText(getString(R.string.outdated_version_desc));
        Button pS = lowBalView.findViewById(R.id.dialog_connection_retry);
        Button close = lowBalView.findViewById(R.id.dialog_connection_exit);
        close.setText(getString(R.string.continu));
        pS.setText(getString(R.string.go_to_ps));
        close.setOnClickListener(v -> {
            lokDiag.dismiss();
            if (isSuccess) {
                if (spf.getBoolean("tos", false)) {
                    startActivity(new Intent(this, Home.class));
                } else {
                    startActivity(new Intent(this, Tos.class));
                }
                finish();
            } else {
                startActivity(new Intent(Splash.this, Login.class));
            }
            finish();
        });
        pS.setOnClickListener(v -> {
			/*
            String appPackageName = getPackageName();
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
            } catch (android.content.ActivityNotFoundException anfe) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
            }
			*/
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://content.mintsoft.org/mintly.apk")));
        });
        lokDiag.show();
    }
}