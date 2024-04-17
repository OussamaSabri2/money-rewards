package org.mintsoft.mintly.account;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.squareup.picasso.Picasso;

import org.mintsoft.mintlib.GetAuth;
import org.mintsoft.mintlib.GetURL;
import org.mintsoft.mintlib.onResponse;
import org.mintsoft.mintly.R;
import org.mintsoft.mintly.helper.BaseAppCompat;
import org.mintsoft.mintly.helper.Misc;

import java.util.HashMap;

public class Profile extends BaseAppCompat {
    private int type;
    private ImageView avatarView;
    private EditText codeInput, passInput, pass2Input, currInput;
    private LinearLayout codeHolder;
    private Dialog inputDiag, loadingDiag, conDiag, passDiag, ynDiag;
    private ActivityResultLauncher<Intent> activityForResult;
    private TextView nameView, emailView, codeView, invitedByView, countryView;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.profile);
        loadingDiag = Misc.loadingDiag(this);
        avatarView = findViewById(R.id.profile_avatarView);
        nameView = findViewById(R.id.profile_nameView);
        countryView = findViewById(R.id.profile_countryView);
        emailView = findViewById(R.id.profile_emailView);
        codeView = findViewById(R.id.profile_codeView);
        codeHolder = findViewById(R.id.profile_codeHolder);
        invitedByView = findViewById(R.id.profile_invitedByView);
        codeInput = findViewById(R.id.profile_codeInput);
        passInput = findViewById(R.id.profile_new_passInput);
        findViewById(R.id.profile_new_passBtn).setOnClickListener(view -> {
            String nPass = passInput.getText().toString();
            if (nPass.isEmpty()) {
                Toast.makeText(Profile.this, getString(R.string.enter_pass), Toast.LENGTH_LONG).show();
            } else if (nPass.length() < 8) {
                Toast.makeText(Profile.this, getString(R.string.pass_min), Toast.LENGTH_LONG).show();
            } else if (nPass.length() > 20) {
                Toast.makeText(Profile.this, getString(R.string.pass_max), Toast.LENGTH_LONG).show();
            } else {
                passDiag();
            }
        });
        activityForResult = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        try {
                            Uri selectedImage = result.getData().getData();
                            String filePath = getPath(selectedImage);
                            String file_ext = filePath.substring(filePath.lastIndexOf(".") + 1);
                            if (file_ext.equals("jpeg") || file_ext.equals("jpg") || file_ext.equals("png") || file_ext.equals("gif")) {
                                Bitmap bp = resize(filePath);
                                loadingDiag.show();
                                String filename = filePath.substring(filePath.lastIndexOf("/") + 1);
                                GetAuth.uploadAvatar(Profile.this, filename, bp, new onResponse() {
                                    @Override
                                    public void onSuccess(String response) {
                                        Picasso.get().load(response).placeholder(R.drawable.loading)
                                                .error(R.drawable.avatar).into(avatarView);
                                        loadingDiag.dismiss();
                                    }

                                    @Override
                                    public void onError(int errorCode, String error) {
                                        loadingDiag.dismiss();
                                        Toast.makeText(Profile.this, error, Toast.LENGTH_LONG).show();
                                    }
                                });
                            } else {
                                Toast.makeText(Profile.this, getString(R.string.invalid_image), Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception e) {
                            Toast.makeText(Profile.this, "" + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        callNet();
        findViewById(R.id.profile_avatarNew).setOnClickListener(view -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                    Intent picker = new Intent(Intent.ACTION_PICK);
                    picker.setType("image/*");
                    activityForResult.launch(picker);
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 131);
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                    photoPickerIntent.setType("image/*");
                    activityForResult.launch(photoPickerIntent);
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 131);
                }
            } else {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                activityForResult.launch(photoPickerIntent);
            }
        });

        nameView.setOnClickListener(view -> {
            type = 2;
            inputDiag();
        });
        findViewById(R.id.profile_go_delete).setOnClickListener(view -> {
            if (ynDiag == null) {
                ynDiag = Misc.decoratedDiag(this, R.layout.dialog_quit, 0.8f);
                TextView ynDesc = ynDiag.findViewById(R.id.dialog_quit_desc);
                ynDesc.setText(getString(R.string.delete_acc_desc));
                ynDiag.findViewById(R.id.dialog_quit_no).setOnClickListener(vw -> ynDiag.dismiss());
                Button yBtn = ynDiag.findViewById(R.id.dialog_quit_yes);
                yBtn.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
                yBtn.setTextColor(Color.WHITE);
                yBtn.setOnClickListener(vw -> {
                    ynDiag.dismiss();
                    loadingDiag.show();
                    GetURL.info(Profile.this, "me/del", true, new onResponse() {
                        @Override
                        public void onSuccess(String s) {
                            loadingDiag.dismiss();
                            setResult(9);
                            finish();
                        }

                        @Override
                        public void onError(int i, String s) {
                            loadingDiag.dismiss();
                            Misc.showMessage(Profile.this, s, false);
                        }
                    });
                });
            }
            ynDiag.show();
        });
        findViewById(R.id.profile_go_logout).setOnClickListener(view -> {
            setResult(9);
            finish();
        });
        findViewById(R.id.profile_close).setOnClickListener(view -> finish());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 131) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                activityForResult.launch(photoPickerIntent);
            }
        }
    }

    private void callNet() {
        loadingDiag.show();
        GetAuth.profile(this, new onResponse() {
            @Override
            public void onSuccessHashMap(HashMap<String, String> data) {
                loadingDiag.dismiss();
                Picasso.get().load(data.get("avatar")).placeholder(R.drawable.anim_loading).error(R.drawable.avatar).into(avatarView);
                nameView.setText(data.get("name"));
                emailView.setText(data.get("email"));
                codeView.setText(data.get("code"));
                countryView.setText(data.get("cc"));
                codeView.setOnClickListener(view -> {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    if (clipboard != null) {
                        ClipData clip = ClipData.newPlainText("Referral Link", codeView.getText());
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(Profile.this, getString(R.string.ref_code_copied), Toast.LENGTH_SHORT).show();
                    }
                });
                String inv = data.get("inv");
                if (inv == null || inv.equals("-none-")) {
                    findViewById(R.id.profile_codeBtn).setOnClickListener(view -> {
                        String d = codeInput.getText().toString();
                        if (d.isEmpty()) return;
                        type = 3;
                        change(d);
                    });
                } else {
                    invitedByView.setText(inv);
                    invitedByView.setVisibility(View.VISIBLE);
                    codeHolder.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(int errorCode, String error) {
                loadingDiag.dismiss();
                if (errorCode == -9) {
                    conDiag = Misc.noConnection(conDiag, Profile.this, () -> {
                        callNet();
                        conDiag.dismiss();
                    });
                } else {
                    Toast.makeText(Profile.this, error, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void inputDiag() {
        if (inputDiag == null) {
            inputDiag = Misc.decoratedDiag(this, R.layout.dialog_profile, 0.6f);
            inputDiag.setCancelable(true);
            inputDiag.setCanceledOnTouchOutside(true);
            TextView titleView = inputDiag.findViewById(R.id.dialog_profile_titleView);
            TextView inputView = inputDiag.findViewById(R.id.dialog_profile_inputView);
            if (type == 2) {
                titleView.setText(getString(R.string.change_name));
                inputView.setText(getString(R.string.enter_name));
            }
            EditText inputEdit = inputDiag.findViewById(R.id.dialog_profile_inputEdit);
            Button btn = inputDiag.findViewById(R.id.dialog_profile_btn);
            btn.setOnClickListener(view -> {
                btn.setText(getString(R.string.updating));
                String text = inputEdit.getText().toString();
                if (type == 2) {
                    if (text.isEmpty()) {
                        btn.setText(getString(R.string.update));
                        Toast.makeText(Profile.this, "Enter your name", Toast.LENGTH_LONG).show();
                        return;
                    }
                    inputDiag.dismiss();
                    btn.setText(getString(R.string.update));
                    change(text);
                }
            });
        } else {
            TextView titleView = inputDiag.findViewById(R.id.dialog_profile_titleView);
            TextView inputView = inputDiag.findViewById(R.id.dialog_profile_inputView);
            EditText e = inputDiag.findViewById(R.id.dialog_profile_inputEdit);
            e.setText("");
            if (type == 2) {
                titleView.setText(getString(R.string.change_name));
                inputView.setText(getString(R.string.enter_name));
            }
        }
        inputDiag.show();
    }

    private void change(String inputData) {
        if (!loadingDiag.isShowing()) loadingDiag.show();
        GetAuth.updateProfile(this, inputData, type, new onResponse() {
            @Override
            public void onSuccess(String response) {
                loadingDiag.dismiss();
                if (type == 2) {
                    nameView.setText(inputData);
                } else if (type == 3) {
                    codeHolder.setVisibility(View.GONE);
                    invitedByView.setText(response);
                    invitedByView.setVisibility(View.VISIBLE);
                } else if (type == 4) {
                    passInput.setText("");
                }
                Toast.makeText(Profile.this, "Update successful", Toast.LENGTH_LONG).show();
                if (type == 4) {
                    setResult(9);
                    finish();
                }
            }

            @Override
            public void onError(int errorCode, String error) {
                loadingDiag.dismiss();
                if (errorCode == -9) {
                    conDiag = Misc.noConnection(conDiag, Profile.this, () -> {
                        conDiag.dismiss();
                        change(inputData);
                    });
                } else if (errorCode == -1) {
                    Misc.showMessage(Profile.this, error, false);
                } else {
                    Toast.makeText(Profile.this, error, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private String getPath(Uri uri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, proj, null, null, null);
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
    }

    private Bitmap resize(String path) {
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        Bitmap bp = ThumbnailUtils.extractThumbnail(bitmap, 200, 200);
        bitmap.recycle();
        return bp;
    }

    private void passDiag() {
        if (passDiag == null) {
            passDiag = Misc.decoratedDiag(this, R.layout.dialog_change_pass, 0.6f);
            passDiag.setCancelable(false);
            pass2Input = passDiag.findViewById(R.id.dialog_change_pass_newEdit);
            currInput = passDiag.findViewById(R.id.dialog_change_pass_currEdit);
            passDiag.findViewById(R.id.dialog_change_pass_cancel).setOnClickListener(view -> passDiag.dismiss());
            passDiag.findViewById(R.id.dialog_change_pass_update).setOnClickListener(view -> {
                if (currInput.getText().toString().isEmpty()) return;
                passDiag.dismiss();
                if (passInput.getText().toString().equals(pass2Input.getText().toString())) {
                    type = 4;
                    change(currInput.getText().toString() + "||" + passInput.getText().toString());
                } else {
                    Misc.showMessage(Profile.this, getString(R.string.pass_not_match), false);
                }
            });
        }
        passDiag.show();
    }
}