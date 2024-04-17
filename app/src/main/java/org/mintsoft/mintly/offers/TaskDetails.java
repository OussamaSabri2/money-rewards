package org.mintsoft.mintly.offers;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.mintsoft.mintlib.Tasks;
import org.mintsoft.mintlib.onResponse;
import org.mintsoft.mintly.Home;
import org.mintsoft.mintly.R;
import org.mintsoft.mintly.helper.BaseAppCompat;
import org.mintsoft.mintly.helper.Misc;
import org.mintsoft.mintly.helper.PushSurf;

import java.io.File;
import java.util.HashMap;

public class TaskDetails extends BaseAppCompat {
    private String oid;
    private Dialog loadingDiag, conDiag, proofDiag;
    private TextView titleView, timeView, startBtn, nameText;
    private WebView webView;
    private EditText editText;
    private boolean reqProof, attached;
    private HashMap<String, Object> attachment;
    private ActivityResultLauncher<Intent> activityForResult;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (!Home.tasks) {
            finish();
            return;
        }
        oid = getIntent().getExtras().getString("id", null);
        if (oid == null) {
            finish();
            return;
        }
        loadingDiag = Misc.loadingDiag(this);
        loadingDiag.show();
        Window w = getWindow();
        w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        w.setNavigationBarColor(ContextCompat.getColor(this, R.color.gray));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            w.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }
        setContentView(R.layout.offers_task_details);
        titleView = findViewById(R.id.offers_task_details_titleView);
        timeView = findViewById(R.id.offers_task_details_timeView);
        startBtn = findViewById(R.id.offers_task_details_start);
        webView = findViewById(R.id.offers_task_details_webView);
        webView.getSettings().setJavaScriptEnabled(true);
        netCall();
        findViewById(R.id.offers_task_details_back).setOnClickListener(view -> finish());
        findViewById(R.id.offers_task_details_proof).setOnClickListener(view -> popupProof());
        activityForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                try {
                    assert result.getData() != null;
                    Uri sFile = result.getData().getData();
                    String filePath = getPath(sFile);
                    if (isValidFileSize(filePath)) {
                        String file_ext = filePath.substring(filePath.lastIndexOf(".") + 1);
                        String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
                        attachment = new HashMap<>();
                        if (file_ext.equals("jpeg") || file_ext.equals("jpg") || file_ext.equals("png") || file_ext.equals("gif")) {
                            attachment.put("file", BitmapFactory.decodeFile(filePath));
                        } else {
                            Toast.makeText(TaskDetails.this, getString(R.string.unsupported_content), Toast.LENGTH_LONG).show();
                            return;
                        }
                        attachment.put("name", fileName);
                        nameText.setText(fileName);
                        attached = true;
                    }
                } catch (Exception e) {
                    Toast.makeText(TaskDetails.this, "" + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void netCall() {
        if (!loadingDiag.isShowing()) loadingDiag.show();
        Tasks.getInfo(this, oid, new onResponse() {
            @Override
            public void onSuccessHashMap(HashMap<String, String> data) {
                loadingDiag.dismiss();
                int status = Integer.parseInt(data.get("status"));
                if (status < 1) {
                    String stRes = data.get("staff");
                    if (stRes != null && !stRes.isEmpty()) {
                        Misc.showMessage(TaskDetails.this, stRes, false);
                    }
                    webView.loadData(data.get("desc"), "text/html", "UTF-8");
                    titleView.setText(data.get("title"));
                    timeView.setText(Misc.html(getString(R.string.proof_time).replace("AZA", "<b>" + data.get("time") + "</b>")));
                    startBtn.setOnClickListener(view -> visit());
                    reqProof = data.get("proof").equals("1");
                } else if (status == 1) {
                    Misc.showMessage(TaskDetails.this, getString(R.string.wait_for_approval), true);
                } else {
                    Toast.makeText(TaskDetails.this, getString(R.string.task_contact_support), Toast.LENGTH_LONG).show();
                    finish();
                }
            }

            @Override
            public void onError(int errorCode, String error) {
                loadingDiag.dismiss();
                if (errorCode == -9) {
                    conDiag = Misc.noConnection(conDiag, TaskDetails.this, () -> {
                        netCall();
                        conDiag.dismiss();
                    });
                } else {
                    Toast.makeText(TaskDetails.this, error, Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        });
    }

    private void visit() {
        if (!loadingDiag.isShowing()) loadingDiag.show();
        Tasks.visit(this, oid, new onResponse() {
            @Override
            public void onSuccess(String response) {
                loadingDiag.dismiss();
                Intent intent = new Intent(TaskDetails.this, PushSurf.class);
                intent.putExtra("url", response);
                intent.putExtra("only", true);
                startActivity(intent);
                //startActivity(new Intent(TaskDetails.this, Surf.class).putExtra("url", response));
            }

            @Override
            public void onError(int errorCode, String error) {
                loadingDiag.dismiss();
                if (errorCode == -9) {
                    conDiag = Misc.noConnection(conDiag, TaskDetails.this, () -> {
                        netCall();
                        conDiag.dismiss();
                    });
                } else {
                    Toast.makeText(TaskDetails.this, error, Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        });
    }

    private void popupProof() {
        if (proofDiag == null) {
            proofDiag = Misc.decoratedDiag(this, R.layout.dialog_proof, 0.8f);
            proofDiag.setCancelable(false);
            editText = proofDiag.findViewById(R.id.dialog_proof_editText);
            TextView scrTitle = proofDiag.findViewById(R.id.dialog_proof_scrTitle);
            LinearLayout scrHolder = proofDiag.findViewById(R.id.dialog_proof_scrHolder);
            if (reqProof) {
                nameText = proofDiag.findViewById(R.id.dialog_proof_fileName);
                proofDiag.findViewById(R.id.dialog_proof_attach).setOnClickListener(view -> addAttachment());
            } else {
                scrTitle.setVisibility(View.GONE);
                scrHolder.setVisibility(View.GONE);
            }
            proofDiag.findViewById(R.id.dialog_proof_submit).setOnClickListener(view -> {
                String msg = editText.getText().toString();
                if (msg.length() > 10) {
                    if (reqProof && !attached) {
                        Toast.makeText(TaskDetails.this, getString(R.string.attach_scr), Toast.LENGTH_LONG).show();
                        return;
                    }
                    proofDiag.dismiss();
                    submitProof(msg);
                } else {
                    editText.setError(getString(R.string.msg_limit));
                }
            });
            proofDiag.findViewById(R.id.dialog_proof_close).setOnClickListener(view -> proofDiag.dismiss());
        }
        proofDiag.show();
    }

    private void submitProof(String msg) {
        if (!loadingDiag.isShowing()) loadingDiag.show();
        Tasks.postAttachment(this, oid, msg, attachment, new onResponse() {
            @Override
            public void onSuccess(String response) {
                TaskOffers.reload = oid;
                loadingDiag.dismiss();
                Misc.showMessage(TaskDetails.this, response, true);
            }

            @Override
            public void onError(int errorCode, String error) {
                loadingDiag.dismiss();
                loadingDiag.dismiss();
                if (errorCode == -9) {
                    conDiag = Misc.noConnection(conDiag, TaskDetails.this, () -> {
                        netCall();
                        conDiag.dismiss();
                    });
                } else {
                    Toast.makeText(TaskDetails.this, error, Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        });
    }

    private void addAttachment() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                Intent picker = new Intent(Intent.ACTION_PICK);
                picker.setType("image/*");
                activityForResult.launch(picker);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 131);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Intent picker = new Intent(Intent.ACTION_PICK);
                picker.setType("image/*");
                activityForResult.launch(picker);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 131);
            }
        } else {
            Intent picker = new Intent(Intent.ACTION_PICK);
            picker.setType("image/*");
            activityForResult.launch(picker);
        }
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

    private boolean isValidFileSize(String filePath) {
        long maxFileSize = 1024 * 1024; // 1mb;
        File file = new File(filePath);
        long fileSize = file.length();
        if (fileSize > maxFileSize) {
            Toast.makeText(this, getString(R.string.file_size_limit), Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }
}
