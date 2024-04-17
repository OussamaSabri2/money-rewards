package org.mintsoft.mintly.games;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;

import org.mintsoft.mintlib.GetGame;
import org.mintsoft.mintlib.onResponse;
import org.mintsoft.mintlib.slotListener;
import org.mintsoft.mintlib.slotViews;
import org.mintsoft.mintly.Home;
import org.mintsoft.mintly.R;
import org.mintsoft.mintly.helper.BaseAppCompat;
import org.mintsoft.mintly.helper.Confetti;
import org.mintsoft.mintly.helper.Misc;
import org.mintsoft.mintly.helper.Variables;
import org.mintsoft.mintly.offers.GlobalAds;
import org.mintsoft.mintly.offers.Offers;

import java.util.ArrayList;
import java.util.HashMap;

public class Slot extends BaseAppCompat {
    private Dialog conDiag, loadingDiag;
    private slotViews views;
    private LinearLayout holder;
    private ArrayList<Integer> images;
    private int free_new, free, cost, r, rm, c, s, mx, m = 1;
    private String card;
    private ActivityResultLauncher<Intent> activityForResult;
    private boolean canStart = true;
    private ImageView startBtn;
    private ScaleAnimation smallBounce;
    private TextView balView, maxView, useView, chanceView;
    private final int[] drawables = new int[]{
            R.drawable.slot_icon_0,
            R.drawable.slot_icon_1,
            R.drawable.slot_icon_2,
            R.drawable.slot_icon_3,
            R.drawable.slot_icon_4,
            R.drawable.slot_icon_5,
            R.drawable.slot_icon_6,
            R.drawable.slot_icon_7,
            R.drawable.slot_icon_8,
            R.drawable.slot_icon_9
    };

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (Home.gams.contains("sl")) {
            finish();
            return;
        }
        Window window = getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setNavigationBarColor(Color.BLACK);
        loadingDiag = Misc.loadingDiag(this);
        setContentView(R.layout.game_slot);
        holder = findViewById(R.id.game_slot_holder);
        startBtn = findViewById(R.id.game_slot_start);
        balView = findViewById(R.id.game_slot_balView);
        maxView = findViewById(R.id.game_slot_win);
        useView = findViewById(R.id.game_slot_use);
        chanceView = findViewById(R.id.game_slot_chances);
        findViewById(R.id.game_slot_back).setOnClickListener(view -> finish());
        smallBounce = new ScaleAnimation(0.85f, 1f, 0.85f, 1f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        smallBounce.setInterpolator(new BounceInterpolator());
        smallBounce.setDuration(500);
        smallBounce.setFillAfter(true);
        callNet();
        activityForResult = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    int resultCode = result.getResultCode();
                    Intent returnData = result.getData();
                    if (resultCode == 10) {
                        free += free_new;
                        Intent intent = new Intent(Slot.this, Confetti.class);
                        intent.putExtra("text", getString(R.string.you_won) + " " + free_new + " free chances");
                        intent.putExtra("icon", R.drawable.icon_free);
                        if (card.isEmpty()) {
                            startActivity(intent);
                        } else {
                            intent.putExtra("code", 11);
                            activityForResult.launch(intent);
                        }
                    } else if (resultCode == 11) {
                        Intent intent = new Intent(Slot.this, Confetti.class);
                        intent.putExtra("icon", R.drawable.icon_card);
                        intent.putExtra("btn_text", getString(R.string.check_card));
                        if (card.startsWith("m-")) {
                            intent.putExtra("text", getString(R.string.you_won) + " 2 scratch cards");
                            intent.putExtra("code", 12);
                        } else {
                            intent.putExtra("text", getString(R.string.you_won) + " a scratch card");
                            intent.putExtra("code", 13);
                        }
                        activityForResult.launch(intent);
                    } else if (resultCode == 12) {
                        Variables.setArrayHash("scratcher_cat", null);
                        if (returnData != null && returnData.getBooleanExtra("btn", false)) {
                            Intent intent = new Intent(Slot.this, ScratcherCat.class);
                            intent.putExtra("id", Integer.parseInt(card.split("-")[1]));
                            startActivity(intent);
                        }
                    } else if (resultCode == 13) {
                        Variables.setArrayHash("scratcher_cat", null);
                        if (returnData != null && returnData.getBooleanExtra("btn", false)) {
                            String[] data = card.split("@@");
                            Intent intent = new Intent(Slot.this, Scratcher.class);
                            intent.putExtra("name", data[0]);
                            intent.putExtra("image", data[1]);
                            intent.putExtra("coord", data[2]);
                            intent.putExtra("id", data[3]);
                            intent.putExtra("exit", 1);
                            startActivity(intent);
                        }
                    }
                });
        GlobalAds.fab(this, "fab_sg");
    }

    @Override
    protected void onResume() {
        super.onResume();
        balView.setText(Home.balance);
    }

    @Override
    public void onBackPressed() {
        if (canStart) {
            super.onBackPressed();
        } else {
            Toast.makeText(this, getString(R.string.please_wait), Toast.LENGTH_SHORT).show();
        }
    }

    private void callNet() {
        if (!loadingDiag.isShowing()) loadingDiag.show();
        GetGame.getSlot(this, new onResponse() {
            @Override
            public void onSuccessHashMap(HashMap<String, String> data) {
                balView.setText(data.get("balance"));
                maxView.setText(data.get("max"));
                useView.setText(data.get("cost"));
                chanceView.setText(getString(R.string.available_chances) + " " + data.get("remain"));
                Home.balance = data.get("balance");
                cost = Integer.parseInt(data.get("cost"));
                r = Integer.parseInt(data.get("rows"));
                c = Integer.parseInt(data.get("cols"));
                s = Integer.parseInt(data.get("speed"));
                mx = Integer.parseInt(data.get("max"));
                rm = Integer.parseInt(data.get("remain"));
                free = Integer.parseInt(data.get("free"));
                images = new ArrayList<>();
                for (int i = 0; i < Integer.parseInt(data.get("icons")); i++) {
                    images.add(drawables[i]);
                }
                images.add(R.drawable.icon_free);
                images.add(R.drawable.icon_card);
                initGame();
                loadingDiag.dismiss();
            }

            @Override
            public void onError(int errorCode, String error) {
                loadingDiag.dismiss();
                if (errorCode == -9) {
                    conDiag = Misc.noConnection(conDiag, Slot.this, () -> {
                        callNet();
                        conDiag.dismiss();
                    });
                } else {
                    Toast.makeText(Slot.this, error, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void initGame() {
        views = new slotViews(this);
        views.setWinItemAnimation(R.drawable.anim_border);
        Interpolator[] ips = new Interpolator[3];
        ips[0] = new AnticipateInterpolator();
        ips[1] = new LinearInterpolator();
        ips[2] = new LinearOutSlowInInterpolator();
        views.setup(this, c, holder, ips, new slotListener() {
            @Override
            public void onSetItems() {
                views.setItems(r, images);
            }

            @Override
            public void onError(int errorCode, String error) {
                Toast.makeText(Slot.this, error, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onResult(String b, String won, int fr, String cd) {
                if (free > 0) free -= 1;
                Home.balance = b;
                //bal = Integer.parseInt(b);
                free_new = fr;
                card = cd;
                balView.setText(b);
                if (!won.equals("0")) {
                    Intent intent = new Intent(Slot.this, Confetti.class);
                    intent.putExtra("text", getString(R.string.you_won) + " " + won + " " + Home.currency.toLowerCase() + "s");
                    intent.putExtra("icon", R.drawable.icon_coin);
                    if (free_new == 0 && card.isEmpty()) {
                        startActivity(intent);
                    } else if (free_new != 0) {
                        intent.putExtra("code", 10);
                        activityForResult.launch(intent);
                    } else {
                        intent.putExtra("code", 11);
                        activityForResult.launch(intent);
                    }
                } else if (free_new != 0) {
                    free += free_new;
                    Intent intent = new Intent(Slot.this, Confetti.class);
                    intent.putExtra("text", getString(R.string.you_won) + " " + free_new + " free chances");
                    intent.putExtra("icon", R.drawable.icon_free);
                    if (card.isEmpty()) {
                        startActivity(intent);
                    } else {
                        intent.putExtra("code", 11);
                        activityForResult.launch(intent);
                    }
                } else if (!card.isEmpty()) {
                    Intent intent = new Intent(Slot.this, Confetti.class);
                    intent.putExtra("icon", R.drawable.icon_card);
                    intent.putExtra("btn_text", getString(R.string.check_card));
                    if (card.startsWith("m-")) {
                        intent.putExtra("text", getString(R.string.you_won) + " 2 scratch cards");
                        intent.putExtra("code", 12);
                    } else {
                        intent.putExtra("text", getString(R.string.you_won) + " a scratch card");
                        intent.putExtra("code", 13);
                    }
                    activityForResult.launch(intent);
                }
                canStart = true;
            }
        });
        startBtn.setOnClickListener(view -> {
            if (views.canStart()) {
                if (free > 0) {
                    freeStart();
                } else if (m * cost <= Integer.parseInt(Home.balance)) {
                    if (rm > 0) {
                        if (!canStart) return;
                        rm -= 1;
                        chanceView.setText(getString(R.string.available_chances) + " " + rm);
                        startBtn.startAnimation(smallBounce);
                        Home.balance = String.valueOf(Integer.parseInt(Home.balance) - m * cost);
                        //bal -= m * cost;
                        balView.setText(Home.balance);
                        views.start(s, m);
                        canStart = false;
                    } else {
                        Toast.makeText(Slot.this, getString(R.string.no_more_chance), Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
        findViewById(R.id.game_slot_plus).setOnClickListener(view -> {
            if ((m + 1) * cost <= Integer.parseInt(Home.balance)) {
                m += 1;
                useView.setText(String.valueOf(m * cost));
                maxView.setText(String.valueOf(m * mx));
            } else {
                Toast.makeText(Slot.this, getString(R.string.insufficient_balance), Toast.LENGTH_LONG).show();
            }
        });
        findViewById(R.id.game_slot_minus).setOnClickListener(view -> {
            if (m - 1 > 0) {
                m -= 1;
                useView.setText(String.valueOf(m * cost));
                maxView.setText(String.valueOf(m * mx));
            }
        });
        findViewById(R.id.game_slot_offerwall).setOnClickListener(view -> {
            startActivity(new Intent(Slot.this, Offers.class));
            finish();
        });
    }

    private void freeStart() {
        if (!canStart) return;
        startBtn.startAnimation(smallBounce);
        views.start(s, 1);
        canStart = false;
    }
}