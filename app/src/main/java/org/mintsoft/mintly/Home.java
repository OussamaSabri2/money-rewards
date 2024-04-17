package org.mintsoft.mintly;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import androidx.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.squareup.picasso.Picasso;
import com.unity3d.ads.IUnityAdsInitializationListener;
import com.unity3d.ads.IUnityAdsLoadListener;
import com.unity3d.ads.IUnityAdsShowListener;
import com.unity3d.ads.UnityAds;

import org.mintsoft.mintlib.GetAuth;
import org.mintsoft.mintlib.GetGame;
import org.mintsoft.mintlib.GetNet;
import org.mintsoft.mintlib.GetURL;
import org.mintsoft.mintlib.onResponse;
import org.mintsoft.mintly.account.Gift;
import org.mintsoft.mintly.account.Login;
import org.mintsoft.mintly.account.Profile;
import org.mintsoft.mintly.account.Refs;
import org.mintsoft.mintly.account.Settings;
import org.mintsoft.mintly.chatsupp.Chat;
import org.mintsoft.mintly.games.GameList;
import org.mintsoft.mintly.games.GuessWord;
import org.mintsoft.mintly.games.ImagepuzzleCat;
import org.mintsoft.mintly.games.JigsawpuzzleCat;
import org.mintsoft.mintly.games.Lotto;
import org.mintsoft.mintly.games.QuizCat;
import org.mintsoft.mintly.games.ScratcherCat;
import org.mintsoft.mintly.games.Slot;
import org.mintsoft.mintly.games.Tournament;
import org.mintsoft.mintly.games.TournamentRes;
import org.mintsoft.mintly.games.Wheel;
import org.mintsoft.mintly.helper.BaseAppCompat;
import org.mintsoft.mintly.helper.GlobalMsg;
import org.mintsoft.mintly.helper.Misc;
import org.mintsoft.mintly.helper.PopupNotif;
import org.mintsoft.mintly.helper.PushMsg;
import org.mintsoft.mintly.helper.Variables;
import org.mintsoft.mintly.helper.arAdapter;
import org.mintsoft.mintly.helper.fullGridView;
import org.mintsoft.mintly.helper.imgAdapter;
import org.mintsoft.mintly.offers.Offers;
import org.mintsoft.mintly.offers.TaskOffers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

public class Home extends BaseAppCompat {
    public static SharedPreferences spf;
    public static String currency, balance, interstitialUnit, rewardedUnit, uName, gams;
    private int delay, joinTour, intentType, adLoading, requestCode;
    private arAdapter aradapter;
    private boolean backClick, unityIntReady, cbl;
    public static int fab_iv = 10000;
    public static boolean isExternal, adMobInitialized, fab, checkNotif,
            sAdv, showInterstitial = false, confettiAds = true, canRedeem = true, tasks = false;
    public static InterstitialAd interstitialAd;
    private ImageView tourEnrol, notifIcon;
    private CountDownTimer countDown, pubTimer;
    private Dialog lowBalDiag;
    private CardView tourHolder;
    private long pubTime, cTime;
    private ImageView avatarView;
    private fullGridView gridView;
    private DrawerLayout drawerLayout;
    private Toast exitToast;
    private Intent drawerIntent;
    private Animation blink;
    private TextView notifCountView, tourBtnText, nameView, balView, goChat;
    private ImageView goWheel, goSlot, goLotto, goScratcher;
    private LinearLayout homeFrame, goQuiz, goGW, goIP, goJPZ;
    private ActivityResultLauncher<Intent> activityForResult;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.home);
        currency = "Coin";
        balance = "0";
        spf = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String tour = spf.getString("tour", null);
        tourHolder = findViewById(R.id.home_tour_holder);
        if (tour == null) tourHolder.setVisibility(View.GONE);
        drawerLayout = findViewById(R.id.drawerLayout);
        homeFrame = findViewById(R.id.homeFrame);
        nameView = findViewById(R.id.drawer_nameView);
        avatarView = findViewById(R.id.drawer_avatarView);
        balView = findViewById(R.id.drawer_balanceView);
        notifIcon = findViewById(R.id.home_notifView);
        Toolbar toolbar = findViewById(R.id.home_toolBar);
        setSupportActionBar(toolbar);
        TextView titleView = findViewById(R.id.home_titleView);
        Misc.setLogo(this, titleView);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayShowTitleEnabled(false);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close) {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                homeFrame.setTranslationX(slideOffset * drawerView.getWidth());
                drawerLayout.bringChildToFront(drawerView);
                drawerLayout.requestLayout();
                drawerLayout.setScrimColor(Color.TRANSPARENT);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                cTime = System.currentTimeMillis();
                long sT = spf.getLong("r_time", cTime);
                if (sT <= cTime) {
                    balance = "...";
                    balView.setText(balance);
                    checkBal();
                }
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (drawerIntent != null) {
                    if (intentType == 1) {
                        requestCode = 96;
                        activityForResult.launch(drawerIntent);
                    } else {
                        startActivity(drawerIntent);
                    }
                    drawerIntent = null;
                }
            }
        };
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        blink = AnimationUtils.loadAnimation(Home.this, R.anim.blink);
        isExternal = spf.getBoolean("ex_surf", false);
        currency = spf.getString("currency", "Coin");
        if (tour != null) {
            String[] strs = tour.split(";");
            if (strs[0].equals("rs")) {
                if (strs[1].equals("-1")) {
                    showResultBtn();
                } else {
                    tourHolder.removeAllViews();
                    tourHolder.setBackground(null);
                    pubTime = Long.parseLong(strs[1]);
                    String dt = new SimpleDateFormat("dd MMM, h:mm a", Locale.getDefault()).format(new Date(pubTime + System.currentTimeMillis()));
                    View penView = LayoutInflater.from(this).inflate(R.layout.home_result_pending, null, false);
                    penView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    TextView timeView = penView.findViewById(R.id.home_result_pending_time);
                    timeView.setText(dt);
                    tourHolder.addView(penView);
                    pubTimer = new CountDownTimer(pubTime, pubTime) {
                        @Override
                        public void onTick(long l) {

                        }

                        @Override
                        public void onFinish() {
                            showResultBtn();
                        }
                    };
                    pubTimer.start();
                }
            } else {
                TextView tourTitleView = findViewById(R.id.home_tour_titleView);
                TextView tourFeeView = findViewById(R.id.home_tour_feeView);
                TextView tourRewardView = findViewById(R.id.home_tour_rewardView);
                TextView tourTimeView = findViewById(R.id.home_tour_timeView);
                TextView tourDayView = findViewById(R.id.home_tour_dateView);
                tourBtnText = findViewById(R.id.home_tour_enrollText);
                tourTitleView.setText(strs[0]);
                tourFeeView.setText(Misc.html("<b>" + getString(R.string.enroll_fee) + "</b>"
                        + " " + strs[1] + " " + currency.toLowerCase() + "s"
                ));
                tourRewardView.setText(Misc.html("<b>" + getString(R.string.total_prize) + "</b>"
                        + " " + strs[2] + " " + currency.toLowerCase() + "s"
                ));
                tourEnrol = findViewById(R.id.home_tour_enroll);
                if (strs.length > 4 && strs[4].equals("1")) {
                    enrolled();
                }
                tourEnrol.setOnClickListener(view -> {
                    if (joinTour == 2) {
                        requestCode = 97;
                        activityForResult.launch(new Intent(this, Tournament.class).putExtra("t", strs[0]));
                    } else if (joinTour == 0) {
                        tourEnroll();
                    }
                });
                tourTime(tourTimeView, tourDayView, strs[3]);
            }
        }
        activityForResult = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    int resultCode = result.getResultCode();
                    if (requestCode == 99) {
                        if (resultCode == 0) {
                            notifCountView.setVisibility(View.GONE);
                            notifCountView.clearAnimation();
                        } else if (resultCode > 9) {
                            notifCountView.setText("9+");
                            notifCountView.clearAnimation();
                            notifCountView.startAnimation(blink);
                        } else {
                            notifCountView.setText(String.valueOf(resultCode));
                            notifCountView.clearAnimation();
                            notifCountView.startAnimation(blink);
                        }
                    } else if (requestCode == 97) {
                        if (resultCode == 10) tourHolder.setVisibility(View.GONE);
                    } else if (requestCode == 96) {
                        if (resultCode == 9) {
                            Variables.reset();
                            GetAuth.removeCred(this);
                            startActivity(new Intent(this, Login.class));
                            finish();
                        } else if (resultCode == 7) {
                            startActivity(new Intent(this, Splash.class));
                            finish();
                        } else if (resultCode == 6) {
                            goChat.setVisibility(View.GONE);
                        }
                    }
                });
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        goQuiz = findViewById(R.id.home_go_quiz);
        goGW = findViewById(R.id.home_go_guess_word);
        goIP = findViewById(R.id.home_go_imagepuzzle);
        goJPZ = findViewById(R.id.home_go_jigsawpuzzle);
        goWheel = findViewById(R.id.home_go_wheel);
        goSlot = findViewById(R.id.home_go_slot);
        goLotto = findViewById(R.id.home_go_lotto);
        goScratcher = findViewById(R.id.home_go_scratcher);
        notifCountView = findViewById(R.id.home_notif_count);
        goChat = findViewById(R.id.drawer_go_chat);
        GetAuth.userinfo(this, new onResponse() {
            @Override
            public void onSuccessHashMap(HashMap<String, String> data) {
                delay = spf.getInt("interval", 10) * 1000;
                uName = data.get("name");
                nameView.setText(uName);
                Picasso.get().load(data.get("avatar")).placeholder(R.drawable.anim_loading).error(R.drawable.avatar).into(avatarView);
                checkNotifCount();
            }
        });
        gridView = findViewById(R.id.home_gridView);
        GetGame.getHtml(this, "4", new onResponse() {
            @Override
            public void onSuccessListHashMap(ArrayList<HashMap<String, String>> list) {
                if (list.size() == 0) {
                    findViewById(R.id.home_html5_all_title).setVisibility(View.GONE);
                    findViewById(R.id.home_html5_all_titleLine).setVisibility(View.GONE);
                } else {
                    View vAll = findViewById(R.id.home_html5_all);
                    if (list.size() < 4) {
                        vAll.setVisibility(View.GONE);
                    } else {
                        findViewById(R.id.home_html5_all_btn).setOnClickListener(view ->
                                startActivity(new Intent(Home.this, GameList.class))
                        );
                    }
                    gridView.setAdapter(new imgAdapter(Home.this, list, R.layout.home_item));
                    gridView.setOnItemClickListener((adapterView, view, i, l) -> {
                        HashMap<String, String> d = list.get(i);
                        String f = d.get("file");
                        String o = d.get("ori");
                        String na = d.get("na");
                        if (f == null || o == null || na == null) return;
                        if (f.startsWith("http")) {
                            startActivity(Misc.startHtml(Home.this, f, o.equals("1"), na.equals("1")));
                        } else {
                            Toast.makeText(Home.this, "This game is depreciated. Try other games", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
        checkGlobalMsg();
        checkActivityReward();
        initListners();
        preload();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (showInterstitial || spf.getBoolean("show_ad", false)) {
            showInterstitial = false;
            if (sAdv && unityIntReady) {
                spf.edit().putBoolean("show_ad", false).apply();
                showUnityAds();
            } else {
                if (interstitialAd != null) {
                    spf.edit().putBoolean("show_ad", false).apply();
                    interstitialAd.show(this);
                } else {
                    loadAd();
                }
            }
        }
        if (checkNotif) {
            checkNotif = false;
            checkNotifCount();
        }
    }

    @Override
    public void onBackPressed() {
        if (backClick) {
            super.onBackPressed();
        } else {
            backClick = true;
            if (exitToast == null) {
                exitToast = Toast.makeText(this, getString(R.string.double_back), Toast.LENGTH_SHORT);
            }
            exitToast.show();
            new Handler().postDelayed(() -> backClick = false, 2000);
        }
    }

    @Override
    protected void onDestroy() {
        if (countDown != null) countDown.cancel();
        if (pubTimer != null) pubTimer.cancel();
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (exitToast != null) exitToast.cancel();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 98) {
            if (resultCode == 8) {
                aradapter.done();
                aradapter.getImageView().setImageResource(R.drawable.reward_done);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void checkBal() {
        if (cbl) return;
        cbl = true;
        GetAuth.balance(Home.this, spf, new onResponse() {
            @Override
            public void onSuccessHashMap(HashMap<String, String> data1) {
                cbl = false;
                spf.edit().putLong("r_time", cTime + delay).commit();
                balance = data1.get("balance");
                balView.setText(balance);
                uName = data1.get("name");
                nameView.setText(uName);
            }

            @Override
            public void onSuccess(String success) {
                checkNotifCount();
            }

            @Override
            public void onError(int i, String s) {
                cbl = false;
            }
        });
    }

    private void initListners() {
        gams = GetURL.getMiscData("games");
        if (gams == null) gams = "";
        if (gams.contains("to")) {
            tourHolder.setVisibility(View.GONE);
        }
        if (gams.contains("qz")) {
            findViewById(R.id.home_go_quiz_holder).setVisibility(View.GONE);
        } else {
            goQuiz.setOnClickListener(view -> {
                Intent intent = new Intent(Home.this, QuizCat.class);
                startTransition(intent, goQuiz);
            });
        }
        if (gams.contains("gw")) {
            findViewById(R.id.home_go_guess_word_holder).setVisibility(View.GONE);
        } else {
            goGW.setOnClickListener(view -> {
                Intent intent = new Intent(Home.this, GuessWord.class);
                startTransition(intent, goGW);
            });
        }
        if (gams.contains("ip")) {
            findViewById(R.id.home_go_imagepuzzle_holder).setVisibility(View.GONE);
        } else {
            goIP.setOnClickListener(view -> {
                Intent intent = new Intent(Home.this, ImagepuzzleCat.class);
                startTransition(intent, goIP);
            });
        }
        if (gams.contains("jp")) {
            findViewById(R.id.home_go_jigsawpuzzle_holder).setVisibility(View.GONE);
        } else {
            goJPZ.setOnClickListener(view -> {
                Intent intent = new Intent(Home.this, JigsawpuzzleCat.class);
                startTransition(intent, goJPZ);
            });
        }
        if (gams.contains("wh")) {
            findViewById(R.id.home_go_wheel_holder).setVisibility(View.GONE);
        } else {
            goWheel.setOnClickListener(view -> {
                Intent intent = new Intent(Home.this, Wheel.class);
                startActivity(intent);
            });
        }
        if (gams.contains("sl")) {
            findViewById(R.id.home_go_slot_holder).setVisibility(View.GONE);
        } else {
            goSlot.setOnClickListener(view -> {
                Intent intent = new Intent(Home.this, Slot.class);
                startActivity(intent);
            });
        }
        if (gams.contains("lo")) {
            findViewById(R.id.home_go_lotto_holder).setVisibility(View.GONE);
        } else {
            goLotto.setOnClickListener(view -> {
                Intent intent = new Intent(Home.this, Lotto.class);
                startActivity(intent);
            });
        }
        if (gams.contains("sc")) {
            findViewById(R.id.home_go_scratcher_holder).setVisibility(View.GONE);
        } else {
            goScratcher.setOnClickListener(view -> {
                Intent intent = new Intent(Home.this, ScratcherCat.class);
                startActivity(intent);
            });
        }

        findViewById(R.id.drawer_go_leaderboard).setOnClickListener(view -> {
            intentType = 0;
            drawerIntent = new Intent(Home.this, Leaderboard.class);
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        findViewById(R.id.drawer_go_offerwall).setOnClickListener(view -> {
            intentType = 0;
            drawerIntent = new Intent(Home.this, Offers.class);
            drawerLayout.closeDrawer(GravityCompat.START);
        });
        TextView goTasks = findViewById(R.id.drawer_go_tasks);
        String tsk = GetURL.getMiscData("tasks");
        tasks = tsk != null && tsk.equals("1");
        if (tasks) {
            goTasks.setOnClickListener(view -> {
                intentType = 0;
                drawerIntent = new Intent(Home.this, TaskOffers.class);
                drawerLayout.closeDrawer(GravityCompat.START);
            });
        } else {
            goTasks.setVisibility(View.GONE);
        }
        TextView goGift = findViewById(R.id.drawer_go_gift);
        String gft = GetURL.getMiscData("redeem");
        canRedeem = gft == null || !gft.equals("0");
        if (canRedeem) {
            goGift.setOnClickListener(view -> {
                intentType = 0;
                drawerIntent = new Intent(Home.this, Gift.class);
                drawerLayout.closeDrawer(GravityCompat.START);
            });
        } else {
            goGift.setVisibility(View.GONE);
        }
        findViewById(R.id.drawer_go_refs).setOnClickListener(view -> {
            intentType = 0;
            drawerIntent = new Intent(Home.this, Refs.class);
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        findViewById(R.id.drawer_go_profile).setOnClickListener(view -> {
            intentType = 1;
            drawerIntent = new Intent(Home.this, Profile.class);
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        findViewById(R.id.drawer_go_settings).setOnClickListener(view -> {
            intentType = 1;
            drawerIntent = new Intent(Home.this, Settings.class);
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        findViewById(R.id.drawer_go_faq).setOnClickListener(view -> {
            intentType = 0;
            drawerIntent = new Intent(Home.this, Faq.class);
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        goChat.setOnClickListener(view -> {
            intentType = 1;
            drawerIntent = new Intent(Home.this, Chat.class);
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        findViewById(R.id.drawer_go_contact).setOnClickListener(view -> {
            intentType = 0;
            drawerIntent = new Intent(Home.this, Support.class);
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        findViewById(R.id.drawer_go_exit).setOnClickListener(view -> finishAndRemoveTask());
        notifIcon.setOnClickListener(view -> {
            requestCode = 99;
            activityForResult.launch(new Intent(Home.this, PopupNotif.class));
        });
    }

    private void checkNotifCount() {
        int msgCount = GetNet.messageCount(Home.this);
        if (msgCount == 0) {
            notifCountView.setVisibility(View.GONE);
            notifCountView.clearAnimation();
        } else {
            if (msgCount > 9) {
                notifCountView.setText("9+");
            } else {
                notifCountView.setText(String.valueOf(msgCount));
            }
            notifCountView.setVisibility(View.VISIBLE);
            notifCountView.clearAnimation();
            notifCountView.startAnimation(blink);

        }
    }

    private void checkActivityReward() {
        final RecyclerView arView = findViewById(R.id.home_ar_recyclerView);
        final TextView arViewTitle = findViewById(R.id.home_ar_titleView);
        final View arViewTitleLine = findViewById(R.id.home_ar_titleViewLine);
        arView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        GetGame.activityReward(this, new onResponse() {
            @Override
            public void onSuccessListHashMap(ArrayList<HashMap<String, String>> list) {
                int activeReward = Integer.parseInt(Objects.requireNonNull(list.get(0).get("current")));
                int isDone = Integer.parseInt(Objects.requireNonNull(list.get(0).get("is_done")));
                list.remove(0);
                list.remove(1);
                if (activeReward + isDone >= list.size()) {
                    arViewTitle.setVisibility(View.GONE);
                    arViewTitleLine.setVisibility(View.GONE);
                    arView.setVisibility(View.GONE);
                } else {
                    arViewTitle.setVisibility(View.VISIBLE);
                    arViewTitleLine.setVisibility(View.VISIBLE);
                    arView.setVisibility(View.VISIBLE);
                    aradapter = new arAdapter(Home.this, list, activeReward, isDone);
                    arView.setAdapter(aradapter);
                }
            }

            @Override
            public void onError(int errorCode, String error) {
                Toast.makeText(Home.this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void checkGlobalMsg() {
        new Handler().postDelayed(() -> {
            if (spf.getStringSet("push_msg", null) != null) {
                startActivity(new Intent(Home.this, PushMsg.class));
            } else if (spf.getBoolean("g_msg", true)) {
                String title = spf.getString("g_title", "");
                if (!title.isEmpty()) {
                    Intent intent = new Intent(Home.this, GlobalMsg.class);
                    intent.putExtra("id", spf.getString("gmid", "none"));
                    intent.putExtra("title", title);
                    intent.putExtra("info", spf.getString("g_desc", "Empty message body."));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    startActivity(intent);
                }
            }
        }, 2000);
    }

    private void startTransition(Intent intent, View v) {
        ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(
                Home.this, v, v.getTransitionName());
        startActivity(intent, activityOptionsCompat.toBundle());
    }

    private void tourEnroll() {
        joinTour = 1;
        tourBtnText.setText(getString(R.string.please_wait));
        GetGame.tourEnroll(this, new onResponse() {
            @Override
            public void onSuccess(String response) {
                if (response.equals("2")) {
                    enrolled();
                    Toast.makeText(Home.this, getString(R.string.enrolled_already), Toast.LENGTH_LONG).show();
                } else if (response.equals("1")) {
                    enrolled();
                }
            }

            @Override
            public void onLowCredit() {
                joinTour = 0;
                tourBtnText.setText(getString(R.string.enroll_now));
                tourBtnText.setAlpha(1.0f);
                if (lowBalDiag == null) {
                    lowBalDiag = Misc.lowbalanceDiag(Home.this, new Misc.yesNo() {
                        @Override
                        public void yes() {
                            lowBalDiag.dismiss();
                            startActivity(new Intent(Home.this, Offers.class));
                        }

                        @Override
                        public void no() {
                            lowBalDiag.dismiss();
                        }
                    });
                }
                lowBalDiag.show();
            }

            @Override
            public void onError(int errorCode, String error) {
                joinTour = 0;
                tourBtnText.setText(getString(R.string.enroll_now));
                tourBtnText.setAlpha(1.0f);
                Toast.makeText(Home.this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void tourTime(TextView tv, TextView dv, String time) {
        countDown = new CountDownTimer(Long.parseLong(time) - System.currentTimeMillis(), 1000) {
            long day;

            @Override
            public void onTick(long l) {
                long t = l / 1000;
                long s = t % 60;
                long m = (t / 60) % 60;
                long h = (t / (60 * 60)) % 24;
                long d = Math.abs(t / (60 * 60 * 24));
                if (d != day) {
                    day = d;
                    dv.setText(("In " + d + " days"));
                }
                tv.setText(String.format(Locale.getDefault(), "%d:%02d:%02d", h, m, s));
            }

            @Override
            public void onFinish() {
                if (joinTour == 1) {
                    joinTour = 2;
                    tourBtnText.setText(getString(R.string.lets_start));
                    tourBtnText.setAlpha(1.0f);
                    tourEnrol.setAlpha(0.8f);
                } else {
                    tourBtnText.setText(("Ongoing..."));
                    tourBtnText.setAlpha(0.5f);
                    tourEnrol.setOnClickListener(null);
                    tourEnrol.setAlpha(0.1f);
                }
                dv.setVisibility(View.INVISIBLE);
                tv.setText(getString(R.string.on_live));
                //tv.setPadding(0, 0, 0, 30);
                tv.setTextColor(ContextCompat.getColor(Home.this, R.color.red_1));
                //tv.startAnimation(blink);
                countDown = null;
            }
        };
        countDown.start();
    }

    private void enrolled() {
        joinTour = 1;
        tourBtnText.setText(getString(R.string.enrolled));
        tourBtnText.setAlpha(0.5f);
        tourEnrol.setAlpha(0.3f);
    }

    private void showResultBtn() {
        tourHolder.removeAllViews();
        tourHolder.setBackground(null);
        View pubView = LayoutInflater.from(this).inflate(R.layout.home_result_published, null, false);
        pubView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        pubView.findViewById(R.id.home_result_published_btn).setOnClickListener(view ->
                startActivity(new Intent(Home.this, TournamentRes.class))
        );
        tourHolder.addView(pubView);
    }

    public void preload() {
        try {
            HashMap<String, String> unityData = GetNet.sdkInfo("infos_cpv",
                    "unityads", new String[]{"active", "game_id", "unit_id_i",
                            "unit_id_r", "fab", "fab_iv", "confetti"});
            if (unityData.containsKey("active")) {
                String active = unityData.get("active");
                sAdv = active != null && active.equals("yes");
            }
            if (sAdv) {
                String i_slot = unityData.get("unit_id_i");
                if (i_slot != null && !i_slot.isEmpty()) {
                    interstitialUnit = i_slot;
                }
                String r_slot = unityData.get("unit_id_r");
                if (r_slot != null && !r_slot.isEmpty()) {
                    rewardedUnit = r_slot;
                }
                String fa = unityData.get("fab");
                if (fa != null && fa.equals("yes")) fab = true;
                String fiv = unityData.get("fab_iv");
                if (fiv != null) fab_iv = Integer.parseInt(fiv) * 1000;
                UnityAds.setDebugMode(false);
                UnityAds.initialize(getApplicationContext(), unityData.get("game_id"),
                        false, new IUnityAdsInitializationListener() {
                            @Override
                            public void onInitializationComplete() {
                                loadUnityAds();
                            }

                            @Override
                            public void onInitializationFailed(UnityAds.UnityAdsInitializationError error, String message) {
                                unityIntReady = false;
                            }
                        });
                String cf = unityData.get("confetti");
                if (cf != null && !cf.equals("yes")) {
                    confettiAds = false;
                }
            } else {
                HashMap<String, String> admobData = GetNet.sdkInfo("infos_cpv",
                        "admob", new String[]{"app_id", "interstitial_slot", "rewarded_slot",
                                "fab", "fab_iv", "confetti"});
                String app_id = admobData.get("app_id");
                if (app_id != null) {
                    ApplicationInfo ai = getPackageManager()
                            .getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
                    ai.metaData.putString("com.google.android.gms.ads.APPLICATION_ID", app_id);
                }
                String i_slot = admobData.get("interstitial_slot");
                if (i_slot != null && !i_slot.isEmpty()) {
                    interstitialUnit = i_slot;
                }
                String r_slot = admobData.get("rewarded_slot");
                if (r_slot != null && !r_slot.isEmpty()) {
                    rewardedUnit = r_slot;
                }
                String fa = admobData.get("fab");
                if (fa != null && fa.equals("yes")) fab = true;
                MobileAds.initialize(getApplicationContext(), initializationStatus -> {
                    adMobInitialized = true;
                    loadAd();
                });
                String fiv = admobData.get("fab_iv");
                if (fiv != null) fab_iv = Integer.parseInt(fiv) * 1000;
                String cf = unityData.get("confetti");
                if (cf != null && !cf.equals("yes")) {
                    confettiAds = false;
                }
            }
        } catch (Exception ignored) {
        }
    }

    public void loadAd() {
        if (adLoading == 1 || interstitialAd != null || interstitialUnit == null) return;
        adLoading = 1;
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(getApplicationContext(), interstitialUnit, adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitial) {
                        adLoading = 0;
                        interstitialAd = interstitial;
                        interstitialAd.setFullScreenContentCallback(
                                new FullScreenContentCallback() {
                                    @Override
                                    public void onAdDismissedFullScreenContent() {
                                        interstitialAd = null;
                                        loadAd();
                                    }

                                    @Override
                                    public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                                        interstitialAd = null;
                                    }

                                    @Override
                                    public void onAdShowedFullScreenContent() {
                                        loadAd();
                                    }
                                });
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        adLoading = 0;
                        interstitialAd = null;
                    }
                });
    }

    private void loadUnityAds() {
        UnityAds.load(interstitialUnit, new IUnityAdsLoadListener() {
            @Override
            public void onUnityAdsAdLoaded(String placementId) {
                unityIntReady = true;
            }

            @Override
            public void onUnityAdsFailedToLoad(String placementId, UnityAds.UnityAdsLoadError error, String message) {
                unityIntReady = false;
            }
        });
    }

    private void showUnityAds() {
        UnityAds.show(this, interstitialUnit, new IUnityAdsShowListener() {
            @Override
            public void onUnityAdsShowFailure(String placementId, UnityAds.UnityAdsShowError error, String message) {
                unityIntReady = false;
            }

            @Override
            public void onUnityAdsShowStart(String placementId) {

            }

            @Override
            public void onUnityAdsShowClick(String placementId) {

            }

            @Override
            public void onUnityAdsShowComplete(String placementId, UnityAds.UnityAdsShowCompletionState state) {
                loadUnityAds();
            }
        });
    }
}