package org.mintsoft.mintly.helper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.mintsoft.mintly.R;

import java.util.ArrayList;
import java.util.HashMap;

public class imgAdapter extends BaseAdapter {
    private final ArrayList<HashMap<String, String>> list;
    private final LayoutInflater inflater;
    private final int layout;

    public imgAdapter(Context context, ArrayList<HashMap<String, String>> list, int layout) {
        inflater = LayoutInflater.from(context);
        this.list = list;
        this.layout = layout;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return list.get(i).get("file");
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View v = view;
        if (v == null) v = inflater.inflate(layout, viewGroup, false);
        TextView titleView = v.findViewById(R.id.home_item_titleView);
        titleView.setText(list.get(i).get("name"));
        ImageView imageView = v.findViewById(R.id.home_item_imageView);
        Picasso.get().load(list.get(i).get("image")).placeholder(R.drawable.anim_loading).into(imageView);
        return v;
    }
}
