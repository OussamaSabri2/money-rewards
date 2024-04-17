package org.mintsoft.mintly.account;

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
import org.mintsoft.mintly.R;
import org.mintsoft.mintly.helper.BaseAppCompat;
import org.mintsoft.mintly.helper.Misc;

import java.util.ArrayList;
import java.util.HashMap;

public class rHistory extends BaseAppCompat {
    private Dialog loadingDiag;
    private ListView listView;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.history_ref);
        loadingDiag = Misc.loadingDiag(this);
        loadingDiag.show();
        findViewById(R.id.history_ref_back).setOnClickListener(view -> finish());
        listView = findViewById(R.id.history_ref_listView);
        GetURL.getRefHistory(this, new onResponse() {
            @Override
            public void onSuccessListHashMap(ArrayList<HashMap<String, String>> list) {
                if (list.size() == 0) {
                    findViewById(R.id.history_ref_emptyView).setVisibility(View.VISIBLE);
                    listView.setVisibility(View.GONE);
                } else {
                    listView.setAdapter(new refAdapter(rHistory.this, list));
                }
                loadingDiag.dismiss();
            }

            @Override
            public void onError(int errorCode, String error) {
                loadingDiag.dismiss();
                Toast.makeText(rHistory.this, error, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private static class refAdapter extends BaseAdapter {
        private final LayoutInflater inflater;
        private final ArrayList<HashMap<String, String>> list;

        refAdapter(Context context, ArrayList<HashMap<String, String>> list) {
            this.inflater = LayoutInflater.from(context);
            this.list = list;
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
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View v = view;
            if (v == null) v = inflater.inflate(R.layout.history_ref_item, viewGroup, false);
            ImageView avatarView = v.findViewById(R.id.history_ref_imageView);
            TextView nameView = v.findViewById(R.id.history_ref_nameView);
            TextView dateView = v.findViewById(R.id.history_ref_dateView);
            nameView.setText(list.get(i).get("name"));
            dateView.setText(("Since " + list.get(i).get("date")));
            Picasso.get().load(list.get(i).get("image")).placeholder(R.drawable.anim_loading).error(R.drawable.avatar).into(avatarView);
            return v;
        }
    }
}
