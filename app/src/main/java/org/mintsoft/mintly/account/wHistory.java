package org.mintsoft.mintly.account;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import org.mintsoft.mintly.R;
import org.mintsoft.mintly.helper.BaseAppCompat;
import org.mintsoft.mintly.helper.Misc;

import java.util.ArrayList;
import java.util.HashMap;


public class wHistory extends BaseAppCompat {

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (Gift.histData == null) {
            Toast.makeText(this, getString(R.string.empty_history), Toast.LENGTH_LONG).show();
            finish();
        } else if (Gift.histData.size() == 0) {
            setContentView(R.layout.history_wd);
            findViewById(R.id.history_wd_back).setOnClickListener(view -> finish());
            findViewById(R.id.history_wd_descView).setVisibility(View.GONE);
            findViewById(R.id.history_wd_emptyView).setVisibility(View.VISIBLE);
        } else {
            setContentView(R.layout.history_wd);
            findViewById(R.id.history_wd_back).setOnClickListener(view -> finish());
            GridView gridView = findViewById(R.id.history_wd_gridView);
            gridView.setAdapter(new wAdapter());
            gridView.setOnItemClickListener((adapterView, view, i, l) -> {
                String message = adapterView.getAdapter().getItem(i).toString();
                if (!message.isEmpty()) {
                    Misc.showMessage(wHistory.this, message, false);
                }
            });
        }
    }

    private class wAdapter extends BaseAdapter {
        private final LayoutInflater inflater;
        private final int red, green, white;
        private final ArrayList<HashMap<String, String>> list;

        wAdapter() {
            inflater = LayoutInflater.from(wHistory.this);
            red = ContextCompat.getColor(wHistory.this, R.color.red_1);
            green = ContextCompat.getColor(wHistory.this, R.color.green_1);
            white = ContextCompat.getColor(wHistory.this, R.color.white_aa);
            list = new ArrayList<>();
            HashMap<String, String> data;
            for (int i = 0; i < Gift.histData.size(); i++) {
                String[] st = Gift.histData.get(String.valueOf(i)).split(";@");
                data = new HashMap<>();
                data.put("title", st[0]);
                data.put("status", st[1]);
                data.put("message", st.length < 3 ? "" : st[2]);
                list.add(data);
            }
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int i) {
            return list.get(i).get("message");
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View v = view;
            if (v == null) v = inflater.inflate(R.layout.history_wd_item, viewGroup, false);
            TextView titleView = v.findViewById(R.id.history_wd_item_titleView);
            TextView statusView = v.findViewById(R.id.history_wd_item_statusView);
            titleView.setText(list.get(i).get("title"));
            String status = list.get(i).get("status");
            switch (status) {
                case "0":
                    statusView.setText(("pending"));
                    statusView.setTextColor(white);
                    break;
                case "1":
                    statusView.setText(("completed"));
                    statusView.setTextColor(green);
                    break;
                case "2":
                    statusView.setText(("rejected"));
                    statusView.setTextColor(red);
                    break;
            }
            return v;
        }
    }
}