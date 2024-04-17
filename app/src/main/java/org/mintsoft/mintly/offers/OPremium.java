package org.mintsoft.mintly.offers;

import android.app.Dialog;
import android.content.Context;
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

import org.mintsoft.mintlib.Customoffers;
import org.mintsoft.mintlib.Tracker;
import org.mintsoft.mintlib.onResponse;
import org.mintsoft.mintly.Home;
import org.mintsoft.mintly.R;
import org.mintsoft.mintly.helper.Misc;
import org.mintsoft.mintly.helper.Variables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class OPremium extends Fragment {
    private Context context;
    private Dialog loadingDiag;
    private RecyclerView recyclerView;
    private boolean shouldReload;

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
        if (Tracker.shouldReload(context)) {
            callnet();
        } else {
            recyclerView.setAdapter(new pAdapter());
        }
    }

    @Override
    public void onResume() {
        if (Tracker.shouldReload(context) || shouldReload) {
            callnet();
        }
        super.onResume();
    }

    @Override
    public void onDestroy() {
        ArrayList<HashMap<String, String>> ls;
        if (Offers.hashList == null) {
            ls = new ArrayList<>();
        } else {
            ls = Offers.hashList.get("offer_premium");
            if (ls == null) ls = new ArrayList<>();
        }
        Variables.setArrayHash("premium_list", ls);
        super.onDestroy();
    }

    private void callnet() {
        if (loadingDiag == null) loadingDiag = Misc.loadingDiag(context);
        if (!loadingDiag.isShowing()) loadingDiag.show();
        Customoffers.getCustomOffers(context, new onResponse() {
            @Override
            public void onSuccessHashListHashMap(HashMap<String, ArrayList<HashMap<String, String>>> hashListHashmap) {
                ArrayList<HashMap<String, String>> list = new ArrayList<>();
                list.addAll(Objects.requireNonNull(hashListHashmap.get("i")));
                list.addAll(Objects.requireNonNull(hashListHashmap.get("s")));
                Offers.hashList.put("offer_premium", list);
                recyclerView.setAdapter(new pAdapter());
            }

            @Override
            public void onError(int errorCode, String error) {
                loadingDiag.dismiss();
                Toast.makeText(context, "Could not connect to the server", Toast.LENGTH_LONG).show();
            }
        });
    }

    private class pAdapter extends RecyclerView.Adapter<pAdapter.ViewHolder> {
        private final LayoutInflater mInflater;
        private final String currency;

        public pAdapter() {
            this.mInflater = LayoutInflater.from(context);
            this.currency = " " + Home.currency.toLowerCase() + "s";
            if (loadingDiag != null) loadingDiag.dismiss();
        }

        @NonNull
        @Override
        public pAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = mInflater.inflate(R.layout.offers_api_grid, parent, false);
            return new pAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull pAdapter.ViewHolder holder, int position) {
            HashMap<String, String> data = Offers.hashList.get("offer_premium").get(position);
            holder.titleView.setText(data.get("title"));
            holder.descView.setText(data.get("desc"));
            holder.amountView.setText(data.get("amount") + currency);
            Picasso.get().load(data.get("image"))
                    .placeholder(R.drawable.anim_loading)
                    .error(R.color.gray)
                    .into(holder.imageView);
        }

        @Override
        public int getItemCount() {
            return Offers.hashList.get("offer_premium").size();
        }

        class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            final TextView titleView, descView, amountView;
            final ImageView imageView;

            ViewHolder(View itemView) {
                super(itemView);
                titleView = itemView.findViewById(R.id.offers_item_titleView);
                descView = itemView.findViewById(R.id.offers_item_descView);
                amountView = itemView.findViewById(R.id.offers_item_amountView);
                imageView = itemView.findViewById(R.id.offers_item_imageView);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                String url = Offers.hashList.get("offer_premium").get(getAbsoluteAdapterPosition()).get("url");
                if (url.startsWith("market://")) {
                    Toast.makeText(context, "This offer is not available anymore!", Toast.LENGTH_LONG).show();
                    remove(getAbsoluteAdapterPosition());
                } else {
                    Misc.onenUrl(context, url);
                    shouldReload = true;
                }
            }
        }

        public void remove(int position) {
            Offers.hashList.get("offer_premium").remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, Offers.hashList.get("offer_premium").size());
        }
    }
}