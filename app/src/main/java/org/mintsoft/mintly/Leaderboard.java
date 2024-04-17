package org.mintsoft.mintly;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.mintsoft.mintlib.GetURL;
import org.mintsoft.mintlib.onResponse;
import org.mintsoft.mintly.helper.BaseAppCompat;
import org.mintsoft.mintly.helper.Misc;
import org.mintsoft.mintly.helper.Variables;

import java.util.ArrayList;
import java.util.HashMap;

public class Leaderboard extends BaseAppCompat {
    private ListView listView;
    private ArrayList<HashMap<String, String>> list;
    private Dialog conDiag, loadingDiag;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.leaderboard);
        listView = findViewById(R.id.leaderboard_listView);
        findViewById(R.id.leaderboard_close).setOnClickListener(view -> finish());
        loadingDiag = Misc.loadingDiag(this);
        list = Variables.getArrayHash("leaderboard_list");
        if (list == null) {
            netCall();
        } else {
            initList();
        }
    }

    @Override
    protected void onDestroy() {
        Variables.setArrayHash("leaderboard_list", list);
        super.onDestroy();
    }

    private void netCall() {
        loadingDiag.show();
        GetURL.leaderboard(this, new onResponse() {
            @Override
            public void onSuccessListHashMap(ArrayList<HashMap<String, String>> l) {
                loadingDiag.dismiss();
                list = l;
                initList();
            }

            @Override
            public void onError(int errorCode, String error) {
                loadingDiag.dismiss();
                if (errorCode == -9) {
                    conDiag = Misc.noConnection(conDiag, Leaderboard.this, () -> {
                        netCall();
                        conDiag.dismiss();
                    });
                } else {
                    Toast.makeText(Leaderboard.this, error, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void initList() {
        if (list.size() == 0) {
            listView.setVisibility(View.GONE);
            findViewById(R.id.leaderboard_emptyView).setVisibility(View.VISIBLE);
        } else {
            listView.setAdapter(new rAdapter(this));
        }
    }

    private class rAdapter extends BaseAdapter {
        private final LayoutInflater inflater;

        rAdapter(Context context) {
            inflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View v = view;
            if (v == null) {
                v = inflater.inflate(R.layout.leaderboard_item, viewGroup, false);
            }
            TextView rankView = v.findViewById(R.id.leaderboard_item_rank);
            TextView nameView = v.findViewById(R.id.leaderboard_item_name);
            TextView scoreView = v.findViewById(R.id.leaderboard_item_score);
            TextView rewardView = v.findViewById(R.id.leaderboard_item_reward);
            rankView.setText(String.valueOf(i + 1));
            nameView.setText(list.get(i).get("n"));
            scoreView.setText(list.get(i).get("s"));
            rewardView.setText(list.get(i).get("r"));
            ImageView imageView = v.findViewById(R.id.leaderboard_item_avatar);
            Picasso.get().load(list.get(i).get("a")).error(R.drawable.avatar)
                    .placeholder(R.drawable.loading).into(imageView);
            if (list.get(i).get("y").equals("y")) {
                v.setBackgroundResource(R.drawable.rc_white_border_trans);
            } else {
                v.setBackgroundResource(R.drawable.rc_colorprimary);
            }
            return v;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }
    }
}
