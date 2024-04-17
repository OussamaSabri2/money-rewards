package org.mintsoft.mintly.chatsupp;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.aghajari.emojiview.AXEmojiManager;
import com.aghajari.emojiview.AXEmojiTheme;
import com.aghajari.emojiview.view.AXEmojiEditText;
import com.aghajari.emojiview.view.AXEmojiPopupLayout;
import com.aghajari.emojiview.view.AXEmojiView;
import com.aghajari.emojiview.whatsappprovider.AXWhatsAppEmojiProvider;

import org.mintsoft.mintlib.chatCall;
import org.mintsoft.mintlib.onResponse;
import org.mintsoft.mintly.R;
import org.mintsoft.mintly.helper.BaseAppCompat;
import org.mintsoft.mintly.helper.Misc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;

public class Chat extends BaseAppCompat {
    private AXEmojiEditText editText;
    private boolean preload, block, activityRunning;
    private chAdapter adapter;
    private String lastId, mFileName;
    private Dialog loadingDiag;
    private long sizeKb = 2048;
    private static Handler handler, bH;
    private static Runnable reload, bR;
    private ImageView statusView, attachBtn;
    private ImageView emojiBtn;
    private AXEmojiPopupLayout emojiPopupLayout;
    private final ArrayList<String> msgs = new ArrayList<>();
    private final ArrayList<HashMap<String, Object>> attachment = new ArrayList<>();
    private ActivityResultLauncher<Intent> activityForResult;
    private RecordButton recordButton;
    private MediaRecorder mRecorder;
    private TextWatcher watcher;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setResult(11);
        loadingDiag = Misc.loadingDiag(this);
        loadingDiag.show();
        setContentView(R.layout.chat);
        activityRunning = true;
        findViewById(R.id.chat_back).setOnClickListener(view -> finish());
        statusView = findViewById(R.id.chat_statusView);
        editText = findViewById(R.id.chat_inputView);
        RecyclerView recyclerView = findViewById(R.id.chat_recyclerView);
        adapter = new chAdapter(this, recyclerView, new ArrayList<>(), editText);
        emojiBtn = findViewById(R.id.chat_emojiBtn);
        attachBtn = findViewById(R.id.chat_attachment);
        RecordView recordView = findViewById(R.id.chat_recordView);
        recordView.setRecordPermissionHandler(() -> {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true;
            boolean recordPermissionAvailable = ContextCompat.checkSelfPermission(Chat.this,
                    Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
            if (recordPermissionAvailable) return true;
            ActivityCompat.requestPermissions(Chat.this, new String[]{Manifest.permission.RECORD_AUDIO}, 0);
            return false;

        });
        recordView.setOnRecordListener(new Orl() {
            @Override
            public void onStart() {
                emojiBtn.setVisibility(View.GONE);
                editText.setVisibility(View.GONE);
                attachBtn.setVisibility(View.GONE);
                startRecording();
            }

            @Override
            public void onCancel() {
                stopRecording(true);

            }

            @Override
            public void onFinish(long recordTime, boolean limitReached) {
                emojiBtn.setVisibility(View.VISIBLE);
                editText.setVisibility(View.VISIBLE);
                attachBtn.setVisibility(View.VISIBLE);
                if (limitReached) {
                    Toast.makeText(Chat.this, "Record time limit reached!", Toast.LENGTH_LONG).show();
                }
                stopRecording(false);
            }

            @Override
            public void onLessThanSecond() {
                emojiBtn.setVisibility(View.VISIBLE);
                editText.setVisibility(View.VISIBLE);
                attachBtn.setVisibility(View.VISIBLE);
                stopRecording(true);
            }
        });
        recordView.setOnBasketAnimationEndListener(() -> {
            emojiBtn.setVisibility(View.VISIBLE);
            editText.setVisibility(View.VISIBLE);
            attachBtn.setVisibility(View.VISIBLE);
        });
        recordView.setTimeLimit(30000);
        recordButton = findViewById(R.id.chat_sendBtn);
        recordButton.setRecordView(recordView);
        watcher = new TextWatcher() {
            boolean lock;

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                int length = charSequence.length();
                if (!lock && length > 0) {
                    lock = true;
                    recordButton.setListenForRecord(false);
                    recordButton.setMicIcon(R.drawable.ic_send);
                } else if (length == 0) {
                    lock = false;
                    recordButton.setListenForRecord(true);
                    recordButton.setMicIcon(R.drawable.recv_ic_mic_white);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };
        editText.addTextChangedListener(watcher);
        recordButton.setOnRecordClickListener(v -> {
            if (!preload) {
                Toast.makeText(Chat.this, getString(R.string.please_wait), Toast.LENGTH_LONG).show();
                return;
            }
            String msg = Objects.requireNonNull(editText.getText()).toString()
                    .replace("\u200B", " ").replace(" ", " ")
                    .replace(" ", " ").replace(" ", " ")
                    .replace(" ", " ").replace(" ", " ")
                    .replace(" ", " ").replace(" ", " ")
                    .replace(" ", " ").replace("⠀", " ");
            if (msg.replace(" ", "").isEmpty()) return;
            if (msgs.size() > 2) {
                Toast.makeText(Chat.this, getString(R.string.please_wait), Toast.LENGTH_LONG).show();
                return;
            }
            editText.setText("");
            if (block || msgs.size() > 0) {
                msgs.add(msg);
                Toast.makeText(Chat.this, getString(R.string.please_wait), Toast.LENGTH_LONG).show();
            } else {
                msgs.add(msg);
                postMessage();
            }
        });
        bH = new Handler();
        bR = new Runnable() {
            boolean gr;

            @Override
            public void run() {
                if (gr) {
                    gr = false;
                    statusView.setImageResource(R.drawable.chat_yellow);
                } else {
                    gr = true;
                    statusView.setImageResource(R.drawable.chat_green);
                }
                bH.postDelayed(this, 500);
            }
        };
        callNet();
        editText.setOnClickListener(view -> emojiClose());
        emojiPopupLayout = findViewById(R.id.chat_emoji_layout);
        emojiPopupLayout.setVisibility(View.GONE);
        AXEmojiManager.install(this, new AXWhatsAppEmojiProvider(this));
        new Handler().postDelayed(() -> runOnUiThread(() -> {
            AXEmojiTheme emojiTheme = AXEmojiManager.getEmojiViewTheme();
            emojiTheme.setSelectionColor(Color.TRANSPARENT);
            emojiTheme.setBackgroundColor(ContextCompat.getColor(Chat.this, R.color.colorPrimaryDark));
            emojiTheme.setCategoryColor(ContextCompat.getColor(Chat.this, R.color.colorPrimaryDark));
            AXEmojiManager.setEmojiViewTheme(emojiTheme);
            AXEmojiView emojiView = new AXEmojiView(Chat.this);
            emojiView.setEditText(editText);
            emojiPopupLayout.initPopupView(emojiView);
            emojiPopupLayout.dismiss();
            emojiBtn.setOnClickListener(view -> {
                if (emojiPopupLayout.isShowing()) {
                    emojiClose();
                } else {
                    emojiPopupLayout.setVisibility(View.VISIBLE);
                    emojiPopupLayout.show();
                    emojiBtn.setColorFilter(ContextCompat.getColor(Chat.this, R.color.yellow_2), PorterDuff.Mode.SRC_IN);
                }
            });
        }), 1500);
        activityForResult = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        try {
                            assert result.getData() != null;
                            Uri sFile = result.getData().getData();
                            String filePath = getPath(sFile);
                            if (isValidFileSize(filePath)) {
                                String file_ext = filePath.substring(filePath.lastIndexOf(".") + 1);
                                String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
                                HashMap<String, Object> data = new HashMap<>();
                                if (file_ext.equals("jpeg") || file_ext.equals("jpg") || file_ext.equals("png") || file_ext.equals("gif")) {
                                    data.put("type", "image");
                                    data.put("file", BitmapFactory.decodeFile(filePath));
                                } else if (file_ext.equals("mp3") || file_ext.equals("mp4")) {
                                    data.put("type", "audio");
                                    data.put("file", filePath);
                                } else {
                                    Toast.makeText(Chat.this, "Unsupported content attached!", Toast.LENGTH_LONG).show();
                                    return;
                                }
                                data.put("name", fileName);
                                attachment.add(data);
                                msgs.add("-@attach@-");
                                if (!block) postMessage();
                            }
                        } catch (Exception e) {
                            Toast.makeText(Chat.this, "" + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 131) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent picker = new Intent(Intent.ACTION_PICK);
                picker.setType("image/* audio/*");
                activityForResult.launch(picker);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (emojiPopupLayout != null && emojiPopupLayout.isShowing()) {
            emojiClose();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        activityRunning = false;
        msgs.clear();
        if (handler != null) handler.removeCallbacks(reload);
        if (bH != null) bH.removeCallbacks(bR);
        adapter.playerRelease();
    }

    private void setReload() {
        handler = new Handler();
        reload = () -> {
            if (!block) {
                block = true;
                getMessage();
            }
            handler.postDelayed(reload, new Random().nextInt(10000 - 5000) + 5000);
        };
        handler.postDelayed(reload, new Random().nextInt(10000 - 5000) + 5000);
    }

    private void callNet() {
        block = true;
        setLoading(true);
        chatCall.readChat(this, new onResponse() {
            @Override
            public void onSuccessHashMap(HashMap<String, String> data) {
                if (!activityRunning) return;
                if (!Objects.requireNonNull(data.get("warn")).isEmpty())
                    showWarning(data.get("warn"));
                if (Objects.equals(data.get("attachment"), "1")) {
                    attachBtn.setVisibility(View.VISIBLE);
                    sizeKb = Long.parseLong(Objects.requireNonNull(data.get("attach_size")));
                    attachBtn.setOnClickListener(view -> {
                        if (msgs.size() > 2) {
                            Toast.makeText(Chat.this, getString(R.string.please_wait), Toast.LENGTH_LONG).show();
                        } else {
                            addAttachment();
                        }
                    });
                } else {
                    attachBtn.setVisibility(View.GONE);
                    editText.removeTextChangedListener(watcher);
                    recordButton.setListenForRecord(false);
                    recordButton.setMicIcon(R.drawable.ic_send);
                }
            }

            @Override
            public void onSuccessListHashMap(ArrayList<HashMap<String, String>> arrayList) {
                if (!activityRunning) return;
                addToList(arrayList);
                setReload();
                block = false;
                setLoading(false);
                preload = true;
                if (loadingDiag.isShowing()) loadingDiag.dismiss();
            }

            @Override
            public void onError(int i, String s) {
                if (!activityRunning) return;
                if (loadingDiag.isShowing()) loadingDiag.dismiss();
                block = false;
                setLoading(false);
                if (i == -1) {
                    Misc.showMessage(Chat.this, s, true);
                } else if (i == -2) {
                    setResult(6);
                    Toast.makeText(Chat.this, s, Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(Chat.this, s, Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        });
    }

    private void getMessage() {
        block = true;
        setLoading(true);
        chatCall.loadChat(this, lastId, new onResponse() {
            @Override
            public void onSuccessListHashMap(ArrayList<HashMap<String, String>> arrayList) {
                if (!activityRunning) return;
                addToList(arrayList);
                block = false;
                setLoading(false);
                if (msgs.size() > 0) postMessage();
            }

            @Override
            public void onError(int i, String s) {
                if (!activityRunning) return;
                block = false;
                setLoading(false);
            }
        });
    }

    private void postMessage() {
        if (msgs.size() == 0) return;
        block = true;
        setLoading(true);
        String msg = msgs.get(0);
        HashMap<String, Object> data = null;
        if (msg.equals("-@attach@-") && attachment.size() > 0) {
            data = new HashMap<>(attachment.get(0));
        }
        chatCall.postChat(this, lastId, msg, data, new onResponse() {
            @Override
            public void onSuccessListHashMap(ArrayList<HashMap<String, String>> arrayList) {
                if (!activityRunning) return;
                addToList(arrayList);
                block = false;
                setLoading(false);
                if (msgs.size() > 0) postMessage();
            }

            @Override
            public void onError(int i, String s) {
                if (!activityRunning) return;
                block = false;
                setLoading(false);
                if (i == -1) {
                    Misc.showMessage(Chat.this, s, false);
                } else if (i == -2) {
                    Misc.showMessage(Chat.this, s, false);
                    if (handler != null) handler.removeCallbacks(reload);
                } else {
                    Toast.makeText(Chat.this, s, Toast.LENGTH_LONG).show();
                }
            }
        });
        if (data != null) attachment.remove(0);
        msgs.remove(0);
    }

    private void setLoading(boolean isLoading) {
        if (isLoading) {
            bH.postDelayed(bR, 10);
        } else {
            bH.removeCallbacks(bR);
            statusView.setImageResource(R.drawable.chat_green);
        }
    }

    private void addToList(ArrayList<HashMap<String, String>> aL) {
        if (aL.size() > 0) {
            String lUid = adapter.getLastUid();
            for (int i = 0; i < aL.size(); i++) {
                if (Objects.equals(aL.get(i).get("uid"), lUid)) {
                    aL.get(i).put("avatar", "hide");
                }
                lUid = aL.get(i).get("uid");
            }
            lastId = aL.get(aL.size() - 1).get("id");
            adapter.addItems(aL);
        }
    }

    private void showWarning(String msg) {
        if (loadingDiag.isShowing()) loadingDiag.dismiss();
        final Dialog warnDiag = Misc.decoratedDiag(this, R.layout.dialog_warn, 0.7f);
        warnDiag.findViewById(R.id.dialog_warn_close).setOnClickListener(view -> warnDiag.dismiss());
        warnDiag.findViewById(R.id.dialog_warn_cancel).setOnClickListener(view -> {
            warnDiag.dismiss();
            finish();
        });
        TextView warnView = warnDiag.findViewById(R.id.dialog_warn_textView);
        warnView.setText(Misc.html(msg));
        warnDiag.show();
    }

    private void emojiClose() {
        emojiPopupLayout.dismiss();
        emojiPopupLayout.setVisibility(View.GONE);
        emojiBtn.setColorFilter(ContextCompat.getColor(Chat.this, R.color.gray), PorterDuff.Mode.SRC_IN);
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

    private void addAttachment() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                Intent picker = new Intent(Intent.ACTION_PICK);
                picker.setType("image/* audio/*");
                activityForResult.launch(picker);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 131);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Intent picker = new Intent(Intent.ACTION_PICK);
                picker.setType("image/* audio/*");
                activityForResult.launch(picker);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                        }, 131);
            }
        } else {
            Intent picker = new Intent(Intent.ACTION_PICK);
            picker.setType("image/* audio/*");
            activityForResult.launch(picker);
        }
    }

    private void startRecording() {
        File dir = new File(getFilesDir(), "cdir");
        if (!dir.exists()) dir.mkdir();
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);
        File file = new File(dir, attachment.size() + ".mp4");
        mFileName = file.getAbsolutePath();
        mRecorder.setOutputFile(mFileName);
        mRecorder.setOnErrorListener((mediaRecorder, i, i1) -> Toast.makeText(Chat.this,
                "Recording error: " + i + " and " + i1, Toast.LENGTH_LONG).show());
        try {
            mRecorder.prepare();
            mRecorder.start();
        } catch (IOException e) {
            Toast.makeText(this, "Preparing failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void stopRecording(boolean isCancelled) {
        if (mRecorder == null) return;
        try {
            mRecorder.stop();
            mRecorder.reset();
            mRecorder.release();
        } catch (RuntimeException ignored) {
        }
        mRecorder = null;
        if (mFileName == null) return;
        File file = new File(mFileName);
        if (isCancelled) {
            file.delete();
        } else {
            if (isValidFileSize(mFileName)) {
                String fileName = mFileName.substring(mFileName.lastIndexOf("/") + 1);
                HashMap<String, Object> data = new HashMap<>();
                data.put("name", fileName);
                data.put("type", "audio");
                data.put("file", mFileName);
                attachment.add(data);
                msgs.add("-@attach@-");
                if (!block) postMessage();
            }
        }
    }

    private boolean isValidFileSize(String filePath) {
        long maxFileSize = 1024 * sizeKb;
        File file = new File(filePath);
        long fileSize = file.length();
        if (fileSize > maxFileSize) {
            Toast.makeText(this, "File size is too big. Try with small size.", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }
}
