package org.mintsoft.mintly.helper;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import org.mintsoft.mintly.Splash;

public abstract class BaseAppCompat extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (Variables.isLive) {
            if (Variables.locale != null) Misc.setLocale(this, Variables.locale);
        } else {
            startActivity(new Intent(this, Splash.class));
            finish();
        }
    }
}
