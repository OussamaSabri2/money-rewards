package org.mintsoft.mintly.helper;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.mintsoft.mintly.Home;
import org.mintsoft.mintly.R;

public class PopupPreQuiz extends BaseAppCompat {
    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Bundle extras = getIntent().getExtras();
        setResult(9);
        if (extras == null) {
            finish();
        } else {
            setContentView(R.layout.popup_pre_quiz);
            try {
                String imageUrl = extras.getString("image");
                if (imageUrl != null) {
                    ImageView imageView = findViewById(R.id.popup_pre_quiz_img);
                    Picasso.get().load(imageUrl).into(imageView);
                }
                TextView titleView = findViewById(R.id.popup_pre_quiz_title);
                TextView descView = findViewById(R.id.popup_pre_quiz_desc);
                TextView qsView = findViewById(R.id.popup_pre_quiz_qs);
                TextView costView = findViewById(R.id.popup_pre_quiz_cost);
                TextView roundView = findViewById(R.id.popup_pre_quiz_round);
                titleView.setText(extras.getString("title"));
                descView.setText(extras.getString("desc"));
                qsView.setText((extras.getString("qs") + " questions"));
                String cost = extras.getString("cost");
                costView.setText((cost + " " + Home.currency.toLowerCase() + "s"));
                int remain = extras.getInt("remain");
                roundView.setText((remain + " rounds"));
                Button yesBtn = findViewById(R.id.popup_pre_quiz_yes);
                if (remain < 1) {
                    yesBtn.setAlpha(0.4f);
                    yesBtn.setText(getString(R.string.exceed_daily_limit));
                } else {
                    if (cost.equals("0")) {
                        yesBtn.setText(getString(R.string.free_play));
                    }
                    yesBtn.setOnClickListener(view1 -> {
                        setResult(8);
                        finish();
                    });
                }
                findViewById(R.id.popup_pre_quiz_no).setOnClickListener(view -> finish());
                findViewById(R.id.popup_pre_quiz_close).setOnClickListener(view -> finish());
            } catch (Exception e) {
                finish();
            }
        }
    }
}