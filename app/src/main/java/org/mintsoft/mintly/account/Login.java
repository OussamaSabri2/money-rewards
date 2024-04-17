package org.mintsoft.mintly.account;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.preference.PreferenceManager;

import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.LoggingBehavior;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import org.mintsoft.mintlib.GetAuth;
import org.mintsoft.mintlib.onResponse;
import org.mintsoft.mintly.Home;
import org.mintsoft.mintly.R;
import org.mintsoft.mintly.Tos;
import org.mintsoft.mintly.helper.BaseAppCompat;
import org.mintsoft.mintly.helper.Misc;
import org.mintsoft.mintly.helper.Surf;
import org.mintsoft.mintly.helper.Variables;

import java.util.Arrays;

public class Login extends BaseAppCompat {
    public static SharedPreferences spf;
    private boolean isLoading;
    private TextView errorView, fErrorView;
    private static final int RC_SIGN_IN = 235;
    private CallbackManager callbackManager;
    private Dialog conDiag, loginDiag, fpassDiag, loadingDiag;
    private ActivityResultLauncher<Intent> activityForResult;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.login);
        TextView textView = findViewById(R.id.login_logo_text);
        Misc.setLogo(this, textView);
        spf = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        spf.edit().remove("rwlink").apply();
        loadingDiag = Misc.loadingDiag(this);
        activityForResult = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == 8) {
                        finish();
                    }
                });
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        findViewById(R.id.login_fb_btn).setOnClickListener(v -> {
            if (!FacebookSdk.isInitialized()) {
                FacebookSdk.sdkInitialize(getApplicationContext(), () -> {
                    if (!isFinishing() && !isDestroyed()) fbLogin();
                });
            } else {
                fbLogin();
            }
        });
        findViewById(R.id.login_goog_btn).setOnClickListener(view -> gooLogin());
        findViewById(R.id.login_ph_btn).setOnClickListener(view ->
                activityForResult.launch(new Intent(Login.this, LoginPhone.class))
        );
        findViewById(R.id.login_go_register).setOnClickListener(view ->
                activityForResult.launch(new Intent(Login.this, Register.class))
        );
        findViewById(R.id.login_go_login).setOnClickListener(view -> loginDiag());
        findViewById(R.id.login_tos_btn).setOnClickListener(v -> showTos());
    }

    @Override
    protected void onDestroy() {
        callbackManager = null;
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_SIGN_IN) {
            try {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) cLoginCall("g", account.getIdToken());
            } catch (ApiException e) {
                loadingDiag.dismiss();
                Toast.makeText(Login.this, getString(R.string.login_error) + ": " + e.getStatusCode(), Toast.LENGTH_LONG).show();
            }
        } else if (data != null && callbackManager != null) {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void fbLogin() {
        LoginManager.getInstance().logInWithReadPermissions(Login.this, Arrays.asList("email", "public_profile"));
        if (Variables.getPHash("debug").equals("1")) {
            FacebookSdk.setIsDebugEnabled(true);
            FacebookSdk.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);
        }
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        String fbToken = loginResult.getAccessToken().getToken();
                        if (fbToken != null) cLoginCall("f", fbToken);
                    }

                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Toast.makeText(Login.this, exception.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void gooLogin() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.g_server_client_id))
                .requestEmail()
                .build();
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(Login.this, gso);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void showTos() {
        startActivity(new Intent(this, Surf.class).putExtra("url", "https://" + getString(R.string.domain_name) + "/terms"));
    }

    private void cLoginCall(String type, String tok) {
        loadingDiag.show();
        GetAuth.socialLogin(Login.this, type, tok, spf, new onResponse() {
            @Override
            public void onSuccess(String response) {
                goHome(loadingDiag);
            }

            @Override
            public void onError(int errorCode, String error) {
                loadingDiag.dismiss();
                if (errorCode == -9) {
                    conDiag = Misc.noConnection(conDiag, Login.this, () -> {
                        conDiag.dismiss();
                        cLoginCall(type, tok);
                    });
                } else {
                    Toast.makeText(Login.this, error, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void loginDiag() {
        if (loginDiag == null) {
            loginDiag = Misc.decoratedDiag(this, R.layout.dialog_login, 0.5f);
            Window w = loginDiag.getWindow();
            if (w != null) {
                w.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
            }
            EditText emailInput = loginDiag.findViewById(R.id.dialog_login_emailView);
            EditText passInput = loginDiag.findViewById(R.id.dialog_login_passView);
            errorView = loginDiag.findViewById(R.id.dialog_login_errorView);
            Button submitBtn = loginDiag.findViewById(R.id.dialog_login_submit);
            submitBtn.setOnClickListener(view -> {
                String em = emailInput.getText().toString();
                String pass = passInput.getText().toString();
                String check = validate(em, pass);
                if (check == null) {
                    isLoading = true;
                    loginDiag.setCancelable(false);
                    errorView.setVisibility(View.GONE);
                    submitBtn.setText(getString(R.string.please_wait));
                    GetAuth.login(Login.this, em, pass, spf, new onResponse() {
                        @Override
                        public void onSuccess(String response) {
                            goHome(loginDiag);
                        }

                        @Override
                        public void onError(int errorCode, String error) {
                            loginDiag.setCancelable(true);
                            if (errorCode == -9) {
                                loginDiag.dismiss();
                                conDiag = Misc.noConnection(conDiag, Login.this, () -> {
                                    conDiag.dismiss();
                                    isLoading = false;
                                    submitBtn.setText(getString(R.string.login));
                                    errorView.setVisibility(View.GONE);
                                    loginDiag();
                                });
                            } else {
                                isLoading = false;
                                submitBtn.setText(getString(R.string.login));
                                errorView.setText(error);
                                errorView.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                } else {
                    errorView.setVisibility(View.VISIBLE);
                    errorView.setText(check);
                }
            });
            loginDiag.findViewById(R.id.dialog_login_fpassView).setOnClickListener(view -> {
                if (!isLoading) {
                    errorView.setVisibility(View.GONE);
                    loginDiag.dismiss();
                    retrieveDiag();
                } else {
                    Toast.makeText(Login.this, getString(R.string.please_wait), Toast.LENGTH_LONG).show();
                }
            });
            loginDiag.findViewById(R.id.dialog_login_cancel).setOnClickListener(view -> {
                if (!isLoading) {
                    errorView.setVisibility(View.GONE);
                    loginDiag.dismiss();
                }
            });
        }
        loginDiag.show();
    }

    private void retrieveDiag() {
        if (fpassDiag == null) {
            fpassDiag = Misc.decoratedDiag(this, R.layout.dialog_forget, 0.5f);
            Window w = fpassDiag.getWindow();
            if (w != null) {
                w.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
            }
            EditText fpassInput = fpassDiag.findViewById(R.id.dialog_retrieve_emailView);
            fErrorView = fpassDiag.findViewById(R.id.dialog_retrieve_errorView);
            Button fpassSubmit = fpassDiag.findViewById(R.id.dialog_retrieve_submit);
            fpassSubmit.setOnClickListener(view -> {
                String em = fpassInput.getText().toString();
                String check = validate(em, "--------");
                if (check == null) {
                    isLoading = true;
                    fpassDiag.setCancelable(false);
                    fErrorView.setVisibility(View.GONE);
                    fpassSubmit.setText(getString(R.string.please_wait));
                    GetAuth.reset(Login.this, em, new onResponse() {
                        @Override
                        public void onSuccess(String response) {
                            fpassDiag.setCancelable(true);
                            fpassDiag.dismiss();
                            new AlertDialog.Builder(Login.this)
                                    .setMessage(response)
                                    .setCancelable(false)
                                    .setPositiveButton(getString(R.string.ok), (dialog, id) -> {
                                        dialog.dismiss();
                                        finish();
                                    })
                                    .show();
                        }

                        @Override
                        public void onError(int errorCode, String error) {
                            fpassDiag.setCancelable(true);
                            if (errorCode == -9) {
                                fpassDiag.dismiss();
                                conDiag = Misc.noConnection(conDiag, Login.this, () -> {
                                    conDiag.dismiss();
                                    isLoading = false;
                                    fpassSubmit.setText(getString(R.string.retrieve));
                                    fErrorView.setVisibility(View.GONE);
                                    retrieveDiag();
                                });
                            } else {
                                isLoading = false;
                                fpassSubmit.setText(getString(R.string.retrieve));
                                fErrorView.setText(error);
                                fErrorView.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                } else {
                    fErrorView.setText(check);
                    fErrorView.setVisibility(View.VISIBLE);
                }
            });
            fpassDiag.findViewById(R.id.dialog_retrieve_cancel).setOnClickListener(view -> {
                if (!isLoading) {
                    fErrorView.setVisibility(View.GONE);
                    fpassDiag.dismiss();
                    loginDiag();
                }
            });
        }
        fpassDiag.show();
    }

    private String validate(String email, String password) {
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return getString(R.string.invalid_email);
        } else if (password.isEmpty()) {
            return getString(R.string.enter_pass);
        } else if (password.length() < 8) {
            return getString(R.string.pass_min);
        } else if (password.length() > 20) {
            return getString(R.string.pass_max);
        } else {
            return null;
        }
    }

    private void goHome(Dialog d) {
        if (spf.getBoolean("tos", false)) {
            startActivity(new Intent(this, Home.class));
        } else {
            startActivity(new Intent(this, Tos.class));
        }
        new Handler().postDelayed(() -> {
            if (isFinishing() || isDestroyed()) return;
            if (d != null && d.isShowing()) d.dismiss();
            finish();
        }, 2000);
    }
}