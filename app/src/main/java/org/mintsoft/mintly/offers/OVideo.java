package org.mintsoft.mintly.offers;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import org.mintsoft.mintlib.GetAuth;
import org.mintsoft.mintly.R;

import java.util.HashMap;

public class OVideo extends Fragment {
    private Context context;
    private RecyclerView recyclerView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = getContext();
        View v = inflater.inflate(R.layout.offers_list, container, false);
        if (context == null || getActivity() == null) return v;
        recyclerView = v.findViewById(R.id.offers_list_recyclerView);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        recyclerView.setLayoutManager(new GridLayoutManager(context, 2));
        vAdapter adapter = new vAdapter(context);
        recyclerView.setAdapter(adapter);
    }

    private class vAdapter extends RecyclerView.Adapter<vAdapter.ViewHolder> {
        private final String packagename;
        private final LayoutInflater inflater;
        private final String userId;

        vAdapter(Context context) {
            this.inflater = LayoutInflater.from(context);
            this.packagename = context.getPackageName();
            this.userId = GetAuth.user(context);
        }

        @NonNull
        @Override
        public vAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.offers_sdk_grid, parent, false);
            return new vAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull vAdapter.ViewHolder holder, int position) {
            HashMap<String,String> data = Offers.hashList.get("offer_video").get(position);
            String name = data.get("name");
            if (name != null) {
                holder.titleView.setText(data.get("title"));
                holder.descView.setText(data.get("desc"));
                Picasso.get().load(data.get("image")).into(holder.imageView);
            }

        }

        @Override
        public int getItemCount() {
            return Offers.hashList.get("offer_video").size();
        }

        class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            final TextView titleView, descView;
            final ImageView imageView;

            ViewHolder(View itemView) {
                super(itemView);
                titleView = itemView.findViewById(R.id.offers_sdk_grid_titleView);
                descView = itemView.findViewById(R.id.offers_sdk_grid_descView);
                imageView = itemView.findViewById(R.id.offers_sdk_grid_imageView);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                try {
                    int pos = getAbsoluteAdapterPosition();
                    Class<?> c = Class.forName(packagename + ".sdkoffers."
                            + Offers.hashList.get("offer_video").get(pos).get("name"));
                    startActivity(new Intent(context, c).putExtra("user", userId)
                            .putExtra("info", Offers.hashList.get("offer_video").get(pos)));
                } catch (ClassNotFoundException e) {
                    Toast.makeText(context, context.getString(R.string.class_not_found), Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}
