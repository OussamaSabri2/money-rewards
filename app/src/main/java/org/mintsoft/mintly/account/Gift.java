package org.mintsoft.mintly.account;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import org.mintsoft.mintlib.GetAuth;
import org.mintsoft.mintlib.GetURL;
import org.mintsoft.mintlib.onResponse;
import org.mintsoft.mintly.Home;
import org.mintsoft.mintly.R;
import org.mintsoft.mintly.helper.BaseAppCompat;
import org.mintsoft.mintly.helper.Misc;
import org.mintsoft.mintly.helper.Variables;

import java.util.ArrayList;
import java.util.HashMap;

public class Gift extends BaseAppCompat {
    private String bal, diagType, diagWid, title, icon;
    private EditText diagInput;
    private TextView balView, diagDesc, diagTitle;
    private ImageView diagImageView;
    private LinearLayout imageHolder;
    private Dialog loadingDiag, conDiag, confirmDiag;
    private gAdapter adapter;
    private LayoutInflater inflater;
    public static HashMap<String, String> histData;
    private ArrayList<HashMap<String, String>> list;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (!Home.canRedeem) {
            finish();
            return;
        }
        setContentView(R.layout.gift);
        balView = findViewById(R.id.gift_balView);
        imageHolder = findViewById(R.id.gift_imageHolder);
        RecyclerView recyclerView = findViewById(R.id.gift_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        loadingDiag = Misc.loadingDiag(this);
        list = Variables.getArrayHash("gift_list");
        inflater = LayoutInflater.from(this);
        adapter = new gAdapter(new HashMap<>());
        recyclerView.setAdapter(adapter);
        histData = new HashMap<>();
        if (list == null) {
            callNet();
        } else {
            updateBal(Home.balance);
            int last = list.size() - 1;
            histData = list.get(last);
            list.remove(last);
            initList();
        }
        findViewById(R.id.gift_close).setOnClickListener(view -> finish());
        findViewById(R.id.gift_go_redeemed).setOnClickListener(view -> startActivity(new Intent(Gift.this, wHistory.class)));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (histData != null && list != null) list.add(histData);
        Variables.setArrayHash("gift_list", list);
    }

    private void callNet() {
        loadingDiag.show();
        GetURL.getGifts(this, new onResponse() {
            @Override
            public void onSuccess(String response) {
                updateBal(response);
            }

            @Override
            public void onSuccessListHashMap(ArrayList<HashMap<String, String>> l) {
                list = l;
                int last = list.size() - 1;
                histData = list.get(last);
                list.remove(last);
                initList();
                loadingDiag.dismiss();
            }

            @Override
            public void onError(int errorCode, String error) {
                loadingDiag.dismiss();
                if (errorCode == -9) {
                    conDiag = Misc.noConnection(conDiag, Gift.this, () -> {
                        callNet();
                        conDiag.dismiss();
                    });
                } else {
                    Toast.makeText(Gift.this, error, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void initList() {
        if (list.size() == 0) return;
        adapter.update(list.get(0));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 370);
        LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
        params2.setMargins(15, 15, 15, 15);
        int colorLight = ContextCompat.getColor(this, R.color.colorPrimaryLight);
        for (int i = 0; i < list.size(); i++) {
            CardView cV = new CardView(this);
            cV.setLayoutParams(params);
            cV.setCardBackgroundColor(colorLight);
            cV.setRadius(12);
            cV.setCardElevation(5);
            cV.setUseCompatPadding(true);
            ImageView iv = new ImageView(this);
            iv.setLayoutParams(params2);
            iv.setAdjustViewBounds(true);
            iv.setMinimumWidth(370);
            Picasso.get().load(list.get(i).get("image")).placeholder(R.drawable.anim_loading).error(R.drawable.rc_white_semitrans).into(iv);
            cV.addView(iv);
            imageHolder.addView(cV);
            final int j = i;
            cV.setOnClickListener(view -> {
                adapter.update(list.get(j));
            });
        }
    }

    private class gAdapter extends RecyclerView.Adapter<gAdapter.ViewHolder> {
        private HashMap<String, String> data;
        private final String currency;
        private ArrayList<ImageView> imageViews;

        gAdapter(HashMap<String, String> data) {
            this.data = data;
            this.currency = Home.currency.toLowerCase() + "s";
            this.imageViews = new ArrayList<>();
        }

        public void update(HashMap<String, String> data) {
            this.data = data;
            this.imageViews = new ArrayList<>();
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public gAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(inflater.inflate(R.layout.gift_list, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull gAdapter.ViewHolder holder, int position) {
            String[] strings = data.get(String.valueOf(position)).split(",@");
            holder.widView.setText(strings[0]);
            holder.titleView.setText((data.get("name") + " " + strings[2]));
            holder.quantityView.setText(("(" + strings[1] + " available)"));
            holder.pointsView.setText(("Require " + strings[3] + " " + currency));
            holder.markView.setImageResource(R.drawable.ic_circle);
            imageViews.add(holder.markView);
        }

        @Override
        public int getItemCount() {
            int size = data.size();
            return size > 4 ? size - 4 : 0;
        }

        class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            final ImageView markView;
            final TextView widView, titleView, quantityView, pointsView;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                markView = itemView.findViewById(R.id.gift_list_markView);
                widView = itemView.findViewById(R.id.gift_list_wid);
                titleView = itemView.findViewById(R.id.gift_list_titleView);
                quantityView = itemView.findViewById(R.id.gift_list_quantityView);
                pointsView = itemView.findViewById(R.id.gift_list_pointsView);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                int pos = getAbsoluteAdapterPosition();
                for (int i = 0; i < imageViews.size(); i++) {
                    if (i == pos) {
                        imageViews.get(i).setImageResource(R.drawable.ic_mark);
                    } else {
                        imageViews.get(i).setImageResource(R.drawable.ic_circle);
                    }
                }
                String[] strings = data.get(String.valueOf(pos)).split(",@");
                int rb = Integer.parseInt(strings[3]);
                if (rb == 0 || rb > Integer.parseInt(bal)) {
                    Toast.makeText(Gift.this, getString(R.string.insufficient_balance), Toast.LENGTH_LONG).show();
                } else {
                    icon = data.get("image");
                    confirmDiag(data.get("name") + " " + strings[2], data.get("desc"), data.get("type"), strings[0]);
                }
            }
        }
    }

    private void updateBal(String b) {
        if (!android.text.TextUtils.isDigitsOnly(b)) {
            bal = "0";
            checkBal();
            return;
        }
        if (bal == null || bal.equals("0")) {
            bal = b;
        } else {
            bal = String.valueOf(Integer.parseInt(bal) - Integer.parseInt(b));
        }
        //balView.setText((bal + " " + Home.currency.toLowerCase() + "s"));
        balView.setText(bal);
    }

    private void confirmDiag(String title, String desc, String type, String wid) {
        diagType = type;
        diagWid = wid;
        this.title = title;
        if (confirmDiag == null) {
            confirmDiag = Misc.decoratedDiag(this, R.layout.dialog_gift, 0.8f);
            confirmDiag.setCancelable(false);
            diagImageView = confirmDiag.findViewById(R.id.dialog_redeem_imageView);
            Picasso.get().load(icon).error(R.drawable.ic_warning).into(diagImageView);
            diagTitle = confirmDiag.findViewById(R.id.dialog_redeem_title);
            diagTitle.setText(title);
            diagDesc = confirmDiag.findViewById(R.id.dialog_redeem_desc);
            diagDesc.setText(desc);
            diagInput = confirmDiag.findViewById(R.id.dialog_redeem_edittext);
            setInputType();
            Button button = confirmDiag.findViewById(R.id.dialog_redeem_btn);
            TextView errorView = confirmDiag.findViewById(R.id.dialog_redeem_error);
            button.setOnClickListener(view -> {
                errorView.setText("");
                String inputData = diagInput.getText().toString();
                if (TextUtils.isEmpty(inputData)) {
                    errorView.setText(("Fill the input field."));
                    return;
                }
                if (diagType.equals("2")) {
                    if (!Patterns.EMAIL_ADDRESS.matcher(inputData).matches()) {
                        errorView.setText(("Enter a valid email address."));
                        return;
                    }
                } else if (diagType.equals("3")) {
                    if (!TextUtils.isDigitsOnly(inputData)) {
                        errorView.setText(("Enter valid number."));
                        return;
                    }
                }
                requestRedeem(diagWid, inputData);
            });
            confirmDiag.findViewById(R.id.dialog_redeem_close).setOnClickListener(view -> confirmDiag.dismiss());
        } else {
            Picasso.get().load(icon).error(R.drawable.ic_warning).into(diagImageView);
            diagTitle.setText(title);
            diagDesc.setText(desc);
            setInputType();
        }
        confirmDiag.show();
    }

    private void setInputType() {
        switch (diagType) {
            case "1":
                diagInput.setSingleLine(false);
                diagInput.setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION);
                diagInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                diagInput.setLines(3);
                diagInput.setMaxLines(5);
                diagInput.setVerticalScrollBarEnabled(true);
                diagInput.setMovementMethod(ScrollingMovementMethod.getInstance());
                diagInput.setScrollBarStyle(View.SCROLLBARS_INSIDE_INSET);
                break;
            case "2":
                diagInput.setSingleLine(true);
                diagInput.setImeOptions(EditorInfo.IME_FLAG_NAVIGATE_NEXT);
                diagInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                diagInput.setLines(1);
                break;
            case "3":
                diagInput.setSingleLine(true);
                diagInput.setImeOptions(EditorInfo.IME_FLAG_NAVIGATE_NEXT);
                diagInput.setInputType(InputType.TYPE_CLASS_NUMBER);
                diagInput.setLines(1);
                break;
        }
    }

    private void requestRedeem(String wid, String toAc) {
        if (confirmDiag.isShowing()) confirmDiag.dismiss();
        loadingDiag.show();
        GetURL.requestGift(this, wid, toAc, new onResponse() {
            @Override
            public void onSuccess(String response) {
                loadingDiag.dismiss();
                updateBal(response);
                Variables.setArrayHash("gift_list", null);
                histData.put(String.valueOf(histData.size()), title + ";@0");
                Toast.makeText(Gift.this, "Request added to the queue for approval.", Toast.LENGTH_LONG).show();
                startActivity(new Intent(Gift.this, wHistory.class));
            }

            @Override
            public void onError(int errorCode, String error) {
                loadingDiag.dismiss();
                if (errorCode == -9) {
                    conDiag = Misc.noConnection(conDiag, Gift.this, () -> {
                        requestRedeem(wid, toAc);
                        conDiag.dismiss();
                    });
                } else {
                    Toast.makeText(Gift.this, error, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void checkBal() {
        GetAuth.balance(this, Home.spf, new onResponse() {
            @Override
            public void onSuccessHashMap(HashMap<String, String> data1) {
                Home.balance = data1.get("balance");
                updateBal(Home.balance);
            }
        });
    }
}