package org.mintsoft.mintly.games;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import org.mintsoft.mintlib.GetGame;
import org.mintsoft.mintlib.onResponse;
import org.mintsoft.mintly.Home;
import org.mintsoft.mintly.R;
import org.mintsoft.mintly.helper.BaseAppCompat;
import org.mintsoft.mintly.helper.Misc;
import org.mintsoft.mintly.offers.GlobalAds;
import org.mintsoft.mintly.offers.Offers;

import java.util.HashMap;

public class Quiz extends BaseAppCompat {
    private int score, grace, reward_per, round_cost, resp = -1;
    private String cat, cc, gl;
    private qAdapter adapter;
    private ProgressBar timeProgress;
    private CountDownTimer countDown;
    private boolean blockClick, blockReturn, skip;
    private Dialog loadingDialog, congratsDiag, roundDiag, lowBalDiag, quitDiag, conDiag;
    private ImageView questionImage, lastPageView;
    private RecyclerView recyclerView;
    private LinearLayout useGraceView, useNewRound;
    private final Animation alphaAnim = Misc.alphaAnim();
    private TextView graceView, scoreView, categoryView, questionView, timeView, skipAmtView, fiftyAmtView;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.game_quiz);
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            finish();
        } else {
            cat = extras.getString("cat", null);
            if (cat == null) {
                Toast.makeText(this, getString(R.string.invalid_category_selected), Toast.LENGTH_LONG).show();
                finish();
            } else {
                loadingDialog = Misc.loadingDiag(this);
                loadingDialog.show();
                graceView = findViewById(R.id.game_quiz_graceView);
                scoreView = findViewById(R.id.game_quiz_scoreView);
                categoryView = findViewById(R.id.game_quiz_categoryView);
                questionView = findViewById(R.id.game_quiz_questionView);
                questionImage = findViewById(R.id.game_quiz_questionImage);
                timeView = findViewById(R.id.game_quiz_timeView);
                timeProgress = findViewById(R.id.game_quiz_timeProgress);
                useGraceView = findViewById(R.id.game_quiz_use_grace);
                useNewRound = findViewById(R.id.game_quiz_new_round);
                lastPageView = findViewById(R.id.game_quiz_lastpage);
                skipAmtView = findViewById(R.id.game_quiz_skipAmt);
                fiftyAmtView = findViewById(R.id.game_quiz_fiftyAmt);
                lastPageView.setVisibility(View.INVISIBLE);
                initRecycler();
                callcatInfo();
                findViewById(R.id.game_quiz_use_fifty).setOnClickListener(view -> {
                    if (blockReturn) {
                        if (loadingDialog != null && !loadingDialog.isShowing())
                            loadingDialog.show();
                        callFifty();
                    } else {
                        showSnack(getString(R.string.not_in_question));
                    }
                });
                findViewById(R.id.game_quiz_use_skip).setOnClickListener(view -> {
                    if (blockReturn) {
                        skip = true;
                        callQuiz();
                    } else {
                        showSnack(getString(R.string.not_in_question));
                    }
                });
                cc = Home.spf.getString("cc", null);
                findViewById(R.id.game_quiz_back).setOnClickListener(view -> onBackPressed());
                GlobalAds.fab(this, "fab_qg");
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (blockReturn) {
            showQuitDiag();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (countDown != null) countDown.cancel();
        super.onDestroy();
    }

    private void callcatInfo() {
        GetGame.getQuizInfo(this, cat, new onResponse() {
            @Override
            public void onSuccessHashMap(HashMap<String, String> data) {
                reward_per = Integer.parseInt(data.get("reward_per"));
                round_cost = Integer.parseInt(data.get("round_cost"));
                categoryView.setText(data.get("category"));
                fiftyAmtView.setText(data.get("fifty_cost"));
                skipAmtView.setText(data.get("skip_cost"));
                gl = data.get("grace_limit");
                setGrace(data.get("grace"));
                initDiags();
                setScore(0);
                skip = false;
                callQuiz();
                newRound();
            }

            @Override
            public void onError(int errorCode, String error) {
                if (errorCode == -9) {
                    conDiag = Misc.noConnection(conDiag, Quiz.this, () -> {
                        callcatInfo();
                        conDiag.dismiss();
                    });
                } else {
                    showSnack(error);
                }
            }
        });
    }

    private void callQuiz() {
        blockReturn = true;
        if (!skip) {
            lastPageView.setVisibility(View.INVISIBLE);
            questionView.setText(getString(R.string.please_wait));
        }
        GetGame.getQuiz(this, cat, cc, skip, new onResponse() {
            @Override
            public void onSuccessHashMap(HashMap<String, String> data) {
                if (skip && countDown != null) countDown.cancel();
                blockClick = false;
                setTimer(data.get("t"));
                questionView.setText(Misc.html(data.get("q")));
                adapter.changeOptions(data);
                if (data.get("img") == null) {
                    questionImage.setVisibility(View.GONE);
                } else {
                    Picasso.get().load(data.get("img")).placeholder(R.drawable.anim_loading).into(questionImage);
                    questionImage.setVisibility(View.VISIBLE);
                }
                if (loadingDialog != null && loadingDialog.isShowing()) loadingDialog.dismiss();
            }

            @Override
            public void onError(int errorCode, String error) {
                if (loadingDialog != null && loadingDialog.isShowing()) loadingDialog.dismiss();
                if (errorCode == -9) {
                    conDiag = Misc.noConnection(conDiag, Quiz.this, () -> {
                        callQuiz();
                        conDiag.dismiss();
                    });
                } else {
                    showSnack(error);
                }
            }

            @Override
            public void onLowCredit() {
                if (loadingDialog != null && loadingDialog.isShowing()) loadingDialog.dismiss();
                showLowBalDiag();
            }
        });
    }

    private void setGrace(String g) {
        grace = Integer.parseInt(g);
        graceView.setText(("Grace (" + grace + "/" + gl + ")"));
    }

    private void setScore(int s) {
        score += s;
        scoreView.setText(("Score: " + score));
        setResult(score);
    }

    private void activeGrace(boolean a) {
        if (a && grace > 0) {
            useGraceView.setBackgroundResource(R.drawable.rc_yellow);
            useGraceView.startAnimation(alphaAnim);
            useGraceView.setOnClickListener(view -> {
                useGraceView.getAnimation().cancel();
                useGraceView.clearAnimation();
                skip = false;
                callQuiz();
                activeGrace(false);
            });
        } else {
            useGraceView.setBackgroundResource(R.drawable.rc_white_semitrans);
            useGraceView.setOnClickListener(null);
        }
    }

    private void newRound() {
        if (grace == 0) {
            useNewRound.setBackgroundResource(R.drawable.rc_violet);
            useNewRound.startAnimation(alphaAnim);
            useNewRound.setOnClickListener(view -> {
                callPurchase();
                useNewRound.setBackgroundResource(R.drawable.rc_white_semitrans);
                useNewRound.setOnClickListener(null);
            });
        } else {
            useNewRound.setBackgroundResource(R.drawable.rc_white_semitrans);
            useNewRound.setOnClickListener(null);
        }
    }

    private void setTimer(String time) {
        final int timeAmt = Integer.parseInt(time);
        countDown = new CountDownTimer(timeAmt, 1000) {
            int tick;

            @Override
            public void onTick(long l) {
                timeView.setText(String.valueOf(l / 1000));
                timeProgress.setProgress(((int) (100 * l) / timeAmt));
                tick += 1;
                if (tick == 2) {
                    lastPageView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFinish() {
                if (!blockClick) {
                    blockClick = true;
                    blockReturn = false;
                    timeView.setText(String.valueOf(0));
                    timeProgress.setProgress(0);
                    Toast.makeText(Quiz.this, "Time up!", Toast.LENGTH_LONG).show();
                    setGrace(String.valueOf(grace - 1));
                    activeGrace(true);
                }
            }
        };
        countDown.start();
    }

    private void initRecycler() {
        recyclerView = findViewById(R.id.game_quiz_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new qAdapter(this, new HashMap<>());
        recyclerView.setAdapter(adapter);
    }

    private class qAdapter extends RecyclerView.Adapter<qAdapter.ViewHolder> {
        private HashMap<String, String> data;
        private final LayoutInflater inflater;
        private final SparseArray<ImageView> imageViews;

        qAdapter(Context context, HashMap<String, String> data) {
            this.data = data;
            this.inflater = LayoutInflater.from(context);
            this.imageViews = new SparseArray<>();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(inflater.inflate(R.layout.game_quiz_option, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.numView.setText(String.valueOf(position + 1));
            holder.opView.setText(data.get(String.valueOf(position)));
            holder.selectionView.setImageResource(R.drawable.ic_mark_inactive);
            imageViews.put(position, holder.selectionView);
            holder.itemView.setVisibility(View.VISIBLE);
        }

        @Override
        public int getItemCount() {
            return data.size() - 3;
        }

        private class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            final TextView opView, numView;
            final ImageView selectionView;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                opView = itemView.findViewById(R.id.game_quiz_option_optionView);
                numView = itemView.findViewById(R.id.game_quiz_option_numberView);
                selectionView = itemView.findViewById(R.id.game_quiz_option_selectionView);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                if (!blockClick) {
                    blockClick = true;
                    if (loadingDialog != null && !loadingDialog.isShowing()) loadingDialog.show();
                    resp = getAbsoluteAdapterPosition();
                    imageViews.get(resp).setImageResource(R.drawable.ic_mark);
                    callResult();
                    countDown.cancel();
                }
            }
        }

        public void changeOptions(HashMap<String, String> data) {
            this.data = data;
            final LayoutAnimationController controller =
                    AnimationUtils.loadLayoutAnimation(Quiz.this, R.anim.slide_from_right);
            recyclerView.setLayoutAnimation(controller);
            notifyDataSetChanged();
            recyclerView.scheduleLayoutAnimation();
        }

        public void markOptions(String actual) {
            if (resp == -1) return;
            int act = Integer.parseInt(actual);
            imageViews.get(act).setImageResource(R.drawable.ic_mark_green);
            if (act != resp) {
                imageViews.get(resp).setImageResource(R.drawable.ic_wrong);
            }
            resp = -1;
        }
    }

    private void callResult() {
        blockReturn = false;
        GetGame.getQuizReward(this, cat, resp, new onResponse() {
            @Override
            public void onSuccessHashMap(HashMap<String, String> data) {
                setGrace(data.get("grace"));
                if (data.get("status").equals("2")) {
                    Toast.makeText(Quiz.this, data.get("msg"), Toast.LENGTH_LONG).show();
                } else {
                    adapter.markOptions(data.get("actual"));
                    if (data.get("correct").equals("1")) {
                        congratsDiag.show();
                        setScore(reward_per);
                    } else {
                        if (grace < 1) {
                            roundDiag.show();
                        }
                        activeGrace(true);
                        if (loadingDialog != null && loadingDialog.isShowing())
                            loadingDialog.dismiss();
                    }
                }
                newRound();
            }

            @Override
            public void onError(int errorCode, String error) {
                if (errorCode == -9) {
                    conDiag = Misc.noConnection(conDiag, Quiz.this, () -> {
                        callResult();
                        conDiag.dismiss();
                    });
                } else {
                    showSnack(error);
                }
            }
        });
    }

    private void callFifty() {
        GetGame.getFifty(this, cat, new onResponse() {
            @Override
            public void onSuccessHashMap(HashMap<String, String> data) {
                if (loadingDialog != null && loadingDialog.isShowing()) loadingDialog.dismiss();
                for (int i = 0; i < data.size(); i++) {
                    String v = data.get(String.valueOf(i));
                    recyclerView.getChildAt(Integer.parseInt(v)).setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(int errorCode, String error) {
                if (loadingDialog != null && loadingDialog.isShowing()) loadingDialog.dismiss();
                if (errorCode == -9) {
                    conDiag = Misc.noConnection(conDiag, Quiz.this, () -> {
                        callFifty();
                        conDiag.dismiss();
                    });
                } else {
                    showSnack(error);
                }
            }

            @Override
            public void onLowCredit() {
                if (loadingDialog != null && loadingDialog.isShowing()) loadingDialog.dismiss();
                showLowBalDiag();
            }
        });
    }

    private void callPurchase() {
        if (loadingDialog != null && !loadingDialog.isShowing()) loadingDialog.show();
        useNewRound.getAnimation().cancel();
        useNewRound.clearAnimation();
        GetGame.getQuizRound(this, new onResponse() {
            @Override
            public void onSuccess(String response) {
                setGrace(response);
                skip = false;
                callQuiz();
            }

            @Override
            public void onError(int errorCode, String error) {
                if (loadingDialog != null && loadingDialog.isShowing()) loadingDialog.dismiss();
                if (errorCode == -9) {
                    conDiag = Misc.noConnection(conDiag, Quiz.this, () -> {
                        callPurchase();
                        conDiag.dismiss();
                    });
                } else {
                    showSnack(error);
                }
            }

            @Override
            public void onLowCredit() {
                if (loadingDialog != null && loadingDialog.isShowing()) loadingDialog.dismiss();
                showLowBalDiag();
            }
        });
    }

    private void initDiags() {
        if (congratsDiag == null) {
            congratsDiag = Misc.decoratedDiag(this, R.layout.dialog_quiz_post, 0.8f);
        }
        congratsDiag.findViewById(R.id.dialog_quiz_post_quit).setOnClickListener(view -> {
            finish();
        });
        congratsDiag.findViewById(R.id.dialog_quiz_post_next).setOnClickListener(view -> {
            skip = false;
            callQuiz();
            congratsDiag.dismiss();
        });
        if (roundDiag == null) {
            roundDiag = Misc.decoratedDiag(this, R.layout.dialog_quiz_post, 0.8f);
        }
        ImageView roundIcon = roundDiag.findViewById(R.id.dialog_quiz_post_icon);
        TextView roundTitle = roundDiag.findViewById(R.id.dialog_quiz_post_title);
        TextView roundDesc = roundDiag.findViewById(R.id.dialog_quiz_post_desc);
        Button purchaseBtn = roundDiag.findViewById(R.id.dialog_quiz_post_next);
        roundIcon.setImageResource(R.drawable.ic_charge);
        roundTitle.setText(getString(R.string.no_grace));
        roundTitle.setTextColor(Color.RED);
        roundDesc.setText((getString(R.string.no_grace_desc) + " " + round_cost + " " + Home.currency.toLowerCase() + "s?"));
        purchaseBtn.setText(getString(R.string.purchase_round));
        purchaseBtn.getBackground().setColorFilter(ContextCompat.getColor(this, R.color.blue_2), PorterDuff.Mode.MULTIPLY);
        purchaseBtn.setOnClickListener(view -> {
            roundDiag.dismiss();
            callPurchase();
        });
        roundDiag.findViewById(R.id.dialog_quiz_post_quit).setOnClickListener(view -> {
            finish();
        });
    }

    private void showSnack(String error) {
        blockReturn = false;
        Snackbar snackbar = Snackbar.make(findViewById(R.id.game_quiz_holder), error, 15000)
                .setTextColor(Color.BLACK)
                .setActionTextColor(Color.DKGRAY);
        snackbar.getView().setBackgroundColor(ContextCompat.getColor(this, R.color.white_aa));
        snackbar.show();
    }

    public void showLowBalDiag() {
        if (lowBalDiag == null) lowBalDiag = Misc.lowbalanceDiag(this, new Misc.yesNo() {
            @Override
            public void yes() {
                lowBalDiag.dismiss();
                startActivity(new Intent(Quiz.this, Offers.class));
                finish();
            }

            @Override
            public void no() {
                lowBalDiag.dismiss();
                if (!skip) finish();
            }
        });
        lowBalDiag.show();
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
}