package org.mintsoft.mintly.account;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;

import org.mintsoft.mintlib.GetAuth;
import org.mintsoft.mintlib.onResponse;
import org.mintsoft.mintly.Home;
import org.mintsoft.mintly.R;
import org.mintsoft.mintly.Tos;
import org.mintsoft.mintly.helper.BaseAppCompat;
import org.mintsoft.mintly.helper.Misc;
import org.mintsoft.mintly.helper.Surf;

import java.util.Objects;

public class Register extends BaseAppCompat {
    private String rb, em, pass, name;
    private Dialog conDiag, loadingDiag;
    private TextInputEditText nameInput, emailInput, passInput1, passInput2, refInput;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.register);
        setResult(9);
        TextView titleView = findViewById(R.id.register_titleView);
        Misc.setLogo(this, titleView);
        nameInput = findViewById(R.id.register_nameInput);
        emailInput = findViewById(R.id.register_emailInput);
        passInput1 = findViewById(R.id.register_passInput);
        passInput2 = findViewById(R.id.register_pass2Input);
        refInput = findViewById(R.id.register_refInput);
        loadingDiag = Misc.loadingDiag(this);
        rb = Login.spf.getString("rfb", null);
        if (rb != null) {
            refInput.setText(rb);
            refInput.setEnabled(false);
            refInput.setFocusable(false);
            passInput2.setImeOptions(EditorInfo.IME_ACTION_DONE);
        }
        findViewById(R.id.register_submit).setOnClickListener(view -> {
            name = Objects.requireNonNull(nameInput.getText()).toString();
            em = Objects.requireNonNull(emailInput.getText()).toString();
            pass = Objects.requireNonNull(passInput1.getText()).toString();
            String regP2 = Objects.requireNonNull(passInput2.getText()).toString();
            if (!pass.equals(regP2)) {
                passInput2.setError(getString(R.string.pass_not_match));
                return;
            }
            if (rb == null) {
                String rfb = Objects.requireNonNull(refInput.getText()).toString();
                if (rfb.length() > 0 && rfb.length() != 13) {
                    refInput.setError(getString(R.string.invalid_ref_code));
                    return;
                } else if (rfb.length() == 13) {
                    rb = rfb;
                }
            }
            if (validate(em, pass)) return;
            register();
        });
        findViewById(R.id.register_tos_btn).setOnClickListener(view ->
                startActivity(new Intent(this, Surf.class)
                        .putExtra("url", "https://" + getString(R.string.domain_name) + "/terms"))
        );
        findViewById(R.id.register_back).setOnClickListener(view -> finish());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (conDiag != null && conDiag.isShowing()) conDiag.dismiss();
        if (loadingDiag != null && loadingDiag.isShowing()) loadingDiag.dismiss();
    }

    private void register() {
        if (!loadingDiag.isShowing()) loadingDiag.show();
        GetAuth.register(this, name, em, pass, rb, Login.spf, new onResponse() {
            @Override
            public void onSuccess(String response) {
                setResult(8);
                if (loadingDiag.isShowing()) loadingDiag.dismiss();
                if (Login.spf.getBoolean("tos", false)) {
                    startActivity(new Intent(Register.this, Home.class));
                } else {
                    startActivity(new Intent(Register.this, Tos.class));
                }
                finish();
            }

            @Override
            public void onError(int errorCode, String error) {
                if (loadingDiag.isShowing()) loadingDiag.dismiss();
                if (errorCode == -9) {
                    conDiag = Misc.noConnection(conDiag, Register.this, () -> {
                        conDiag.dismiss();
                        register();
                    });
                } else Misc.showMessage(Register.this, error, errorCode == -2);
            }
        });
    }

    private boolean validate(String email, String password) {
        if (name.isEmpty()) {
            nameInput.setError(getString(R.string.enter_name));
            return true;
        } else if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError(getString(R.string.invalid_email));
            return true;
        } else if (password.isEmpty()) {
            passInput1.setError(getString(R.string.enter_pass));
            return true;
        } else if (password.length() < 8) {
            passInput1.setError(getString(R.string.pass_min));
            return true;
        } else if (password.length() > 20) {
            passInput1.setError(getString(R.string.pass_max));
            return true;
        } else {
            return false;
        }
    }
}