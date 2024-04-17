package org.mintsoft.mintly.helper;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.mintsoft.mintly.R;

import java.util.ArrayList;
import java.util.HashMap;

public class arAdapter extends RecyclerView.Adapter<arAdapter.ViewHolder> {
    private final ArrayList<HashMap<String, String>> list;
    private final LayoutInflater mInflater;
    private final Context context;
    private final int activeReward;
    private int isDone;
    private ImageView doneView;

    public arAdapter(Context context, ArrayList<HashMap<String, String>> list, int activeReward, int isDone) {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.activeReward = activeReward;
        this.list = list;
        this.isDone = isDone;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.home_ar_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (position < activeReward) {
            holder.imageView.setImageResource(R.drawable.reward_done);
        } else if (position == activeReward) {
            if (isDone == 1) {
                holder.imageView.setImageResource(R.drawable.reward_done);
            } else {
                holder.imageView.setImageResource(R.drawable.reward_active);
            }
        } else {
            holder.imageView.setImageResource(R.drawable.reward_coming);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final ImageView imageView;

        ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.home_ar_list_img);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int pos = getAbsoluteAdapterPosition();
            if (pos == activeReward) {
                if (isDone == 1) {
                    Toast.makeText(context, "Today you opened this vault!", Toast.LENGTH_LONG).show();
                } else {
                    doneView = imageView;
                    Intent intent = new Intent(context, PopupAr.class);
                    intent.putExtra("id", activeReward);
                    imageView.setTransitionName("popup_ar_img");
                    ActivityOptions options = ActivityOptions
                            .makeSceneTransitionAnimation((Activity) context, imageView, "popup_ar_img");
                    ((Activity) context).startActivityForResult(intent, 98, options.toBundle());
                }
            } else if (pos < activeReward) {
                Toast.makeText(context, "This vault is empty!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, "This vault cannot be opened today!", Toast.LENGTH_LONG).show();
            }
        }
    }

    public ImageView getImageView() {
        return doneView;
    }

    public void done() {
        isDone = 1;
    }
}