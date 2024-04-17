package org.mintsoft.mintly.games;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;

import org.mintsoft.mintlib.GetGame;
import org.mintsoft.mintlib.circleListener;
import org.mintsoft.mintlib.circleViews;
import org.mintsoft.mintlib.onResponse;
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

public class Wheel extends BaseAppCompat {
    private circleViews circleViews;
    private LinearLayout linearLayout;
    private Animation animation;
    private View startBtn;
    private String card;
    private boolean forceStop;
    private int remain, free, cost = 10, multiply = 1, maxMulti = -1;
    private Dialog quitDiag, conDiag, loadingDiag, lowBalDiag;
    private TextView balView, multiplyView, requireView;
    private ActivityResultLauncher<Intent> activityForResult;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (Home.gams.contains("wh")) {
            finish();
            return;
        }
        Window window = getWindow();
        window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setNavigationBarColor(ContextCompat.getColor(this, R.color.violet_2));
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.violet_3));
        setContentView(R.layout.game_wheel);
        loadingDiag = Misc.loadingDiag(this);
        loadingDiag.show();
        linearLayout = findViewById(R.id.game_wheel_circleHolder);
        startBtn = findViewById(R.id.game_wheel_start);
        balView = findViewById(R.id.game_wheel_balView);
        multiplyView = findViewById(R.id.game_wheel_multiplyView);
        requireView = findViewById(R.id.game_wheel_requireView);
        activityForResult = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    int resultCode = result.getResultCode();
                    Intent returnData = result.getData();
                    if (resultCode == 11) {
                        Variables.setArrayHash("scratcher_cat", null);
                        if (returnData != null && returnData.getBooleanExtra("btn", false)) {
                            Intent intent = new Intent(Wheel.this, ScratcherCat.class);
                            intent.putExtra("id", Integer.parseInt(card.split("-")[1]));
                            startActivity(intent);
                        }
                    } else if (resultCode == 12) {
                        Variables.setArrayHash("scratcher_cat", null);
                        if (returnData != null && returnData.getBooleanExtra("btn", false)) {
                            String[] data = card.split("@@");
                            Intent intent = new Intent(Wheel.this, Scratcher.class);
                            intent.putExtra("name", data[0]);
                            intent.putExtra("image", data[1]);
                            intent.putExtra("coord", data[2]);
                            intent.putExtra("id", data[3]);
                            intent.putExtra("exit", 1);
                            startActivity(intent);
                        }
                    }
                    card = null;
                });
        GlobalAds.fab(this, "fab_sw");
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        balView.setText(Home.balance);
        multiplyView.setText(("1X"));
        animation = Misc.btnEffect();
        circleViews = new circleViews(this);
        circleViews.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        circleViews.setStyle(Color.WHITE, 2, Color.DKGRAY, 10);
        circleViews.setBackgroundImage(ContextCompat.getDrawable(this, R.drawable.wheel_new_dots));
        linearLayout.addView(circleViews);
        circleViews.setListener(new circleListener() {
            @Override
            public void onAnimate(ViewPropertyAnimator animator, int duration, float angle) {
                animator.setInterpolator(new LinearOutSlowInInterpolator())
                        .setDuration(duration).rotation(angle)
                        .start();
            }

            @Override
            public void rotateDone(HashMap<String, String> data) {
                String b = data.get("balance");
                balView.setText(b);
                Home.balance = b;
                //bal = Integer.parseInt(b);
                free = Integer.parseInt(data.get("free"));
                remain = Integer.parseInt(data.get("played"));
                requireView.setText(String.valueOf((free > multiply ? 0 : multiply - free) * cost));
                card = data.get("card");
                if (card == null) return;
                if (card.equals("f")) {
                    Intent intent = new Intent(Wheel.this, Confetti.class);
                    intent.putExtra("text", data.get("message"));
                    intent.putExtra("icon", R.drawable.icon_free);
                    startActivity(intent);
                    card = null;
                } else if (card.equals("nf")) {
                    Intent intent = new Intent(Wheel.this, Confetti.class);
                    intent.putExtra("text", data.get("message"));
                    intent.putExtra("icon", R.drawable.icon_coin);
                    startActivity(intent);
                    card = null;
                } else if (card.startsWith("m-")) {
                    Intent intent = new Intent(Wheel.this, Confetti.class);
                    intent.putExtra("text", data.get("message"));
                    intent.putExtra("icon", R.drawable.icon_card);
                    intent.putExtra("code", 11);
                    intent.putExtra("btn_text", getString(R.string.check_card));
                    activityForResult.launch(intent);
                } else {
                    Intent intent = new Intent(Wheel.this, Confetti.class);
                    intent.putExtra("text", data.get("message"));
                    intent.putExtra("icon", R.drawable.icon_card);
                    intent.putExtra("code", 12);
                    intent.putExtra("btn_text", getString(R.string.check_card));
                    activityForResult.launch(intent);
                }
            }

            @Override
            public void onLowCredit() {
                showLowBalDiag();
            }

            @Override
            public void onError(int errorCode, String error) {
                forceStop = true;
                Toast.makeText(Wheel.this, error, Toast.LENGTH_LONG).show();
            }
        });

        startBtn.setOnClickListener(view -> {
            if (circleViews.isRunning()) return;
            startBtn.startAnimation(animation);
            if (remain == 0) {
                Toast.makeText(Wheel.this, getString(R.string.try_tomorrow), Toast.LENGTH_LONG).show();
            } else {
                balView.setText(String.valueOf(Integer.parseInt(Home.balance) - (free > multiply ? 0 : multiply - free) * cost));
                circleViews.startRotation();
            }
        });
        findViewById(R.id.game_wheel_back).setOnClickListener(view -> onBackPressed());
        findViewById(R.id.game_wheel_offer).setOnClickListener(view -> startActivity(new Intent(this, Offers.class)));
        findViewById(R.id.game_wheel_minusView).setOnClickListener(view -> {
            if (multiply - 1 > 0 && circleViews.multiply(multiply - 1)) {
                multiply--;
                multiplyView.setText((multiply + "X"));
                requireView.setText(String.valueOf((free > multiply ? 0 : multiply - free) * cost));
            }
        });
        findViewById(R.id.game_wheel_plusView).setOnClickListener(view -> {
            if (maxMulti == -1 || maxMulti <= multiply) {
                Toast.makeText(Wheel.this, "Max multiplication has reached!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (Integer.parseInt(Home.balance) >= cost * (multiply + 1) && circleViews.multiply(multiply + 1)) {
                multiply++;
                multiplyView.setText((multiply + "X"));
                requireView.setText(String.valueOf((free > multiply ? 0 : multiply - free) * cost));
            }
        });
        callNet();
    }

    @Override
    protected void onResume() {
        super.onResume();
        balView.setText(Home.balance);
    }

    @Override
    public void onBackPressed() {
        if (circleViews.isRunning() && !forceStop) {
            //showQuitDiag();
            Toast.makeText(this, getString(R.string.please_wait), Toast.LENGTH_SHORT).show();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (circleViews != null) circleViews.clearData();
        super.onDestroy();
    }

    private void callNet() {
        if (!loadingDiag.isShowing()) loadingDiag.show();
        GetGame.wheelInfo(this, new onResponse() {
            @Override
            public void onSuccessHashMap(HashMap<String, String> data) {
                String b = data.get("balance");
                balView.setText(b);
                Home.balance = b;
                String c = data.get("cost");
                cost = Integer.parseInt(c);
                free = Integer.parseInt(data.get("free"));
                remain = Integer.parseInt(data.get("played"));
                String mt = data.get("multiply");
                if (mt != null) maxMulti = Integer.parseInt(mt);
                if (free > 0) {
                    requireView.setText("0");
                } else {
                    requireView.setText(c);
                }
            }

            @Override
            public void onSuccessListHashMap(ArrayList<HashMap<String, String>> list) {
                circleViews.setData(list);
                loadingDiag.dismiss();
            }

            @Override
            public void onError(int errorCode, String error) {
                loadingDiag.dismiss();
                if (errorCode == -9) {
                    conDiag = Misc.noConnection(conDiag, Wheel.this, () -> {
                        callNet();
                        conDiag.dismiss();
                    });
                } else {
                    Toast.makeText(Wheel.this, error, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void showQuitDiag() {
        if (quitDiag == null) {
            quitDiag = Misc.decoratedDiag(this, R.layout.dialog_quit, 0.8f);
            quitDiag.findViewById(R.id.dialog_quit_no).setOnClickListener(view -> quitDiag.dismiss());
            quitDiag.findViewById(R.id.dialog_quit_yes).setOnClickListener(view -> {
                quitDiag.dismiss();
                finish();
            });
        }
        quitDiag.show();
    }

    public void showLowBalDiag() {
        if (lowBalDiag == null) lowBalDiag = Misc.lowbalanceDiag(this, new Misc.yesNo() {
            @Override
            public void yes() {
                lowBalDiag.dismiss();
                startActivity(new Intent(Wheel.this, Offers.class));
                finish();
            }

            @Override
            public void no() {
                lowBalDiag.dismiss();
                finish();
            }
        });
        lowBalDiag.show();
    }
}