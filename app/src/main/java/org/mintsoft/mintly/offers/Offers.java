package org.mintsoft.mintly.offers;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.mintsoft.mintlib.Customoffers;
import org.mintsoft.mintlib.GetAuth;
import org.mintsoft.mintlib.GetNet;
import org.mintsoft.mintlib.onResponse;
import org.mintsoft.mintly.Home;
import org.mintsoft.mintly.R;
import org.mintsoft.mintly.helper.BaseAppCompat;
import org.mintsoft.mintly.helper.Misc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class Offers extends BaseAppCompat {
    private boolean checking;
    private ViewPager2 viewPager;
    private ArrayList<String> tabs;
    private TextView balView, balText;
    public static boolean checkBalance;
    private Dialog loadingDiag, dialog;
    private ArrayList<Fragment> fragments;
    public static HashMap<String, ArrayList<HashMap<String, String>>> hashList;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        loadingDiag = Misc.loadingDiag(this);
        loadingDiag.show();
        setContentView(R.layout.offers);
        balView = findViewById(R.id.offers_balView);
        balText = findViewById(R.id.offers_balText);
        balView.setText(Home.balance);
        tabs = new ArrayList<>();
        fragments = new ArrayList<>();
        callnet();
        findViewById(R.id.offers_go_ppv).setOnClickListener(view -> startActivity(new Intent(Offers.this, PPVOffers.class)));
        findViewById(R.id.offers_go_video).setOnClickListener(view -> startActivity(new Intent(Offers.this, Yt.class)));
        findViewById(R.id.offers_back).setOnClickListener(view -> finish());
        findViewById(R.id.offers_checkBal).setOnClickListener(view -> {
            balText.setText(getString(R.string.checking));
            checkBal();
        });
    }

    @Override
    protected void onPostResume() {
        if (checkBalance || Home.balance.equals("...")) {
            checkBalance = false;
            checkBal();
        } else {
            balView.setText(Home.balance);
        }
        super.onPostResume();
    }

    @Override
    public void onBackPressed() {
        if (viewPager == null || viewPager.getCurrentItem() == 0) {
            super.onBackPressed();
        } else {
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hashList = null;
        if (loadingDiag != null && loadingDiag.isShowing()) loadingDiag.dismiss();
    }

    private void checkBal() {
        if (checking) return;
        checking = true;
        GetAuth.balance(this, Home.spf, new onResponse() {
            @Override
            public void onSuccessHashMap(HashMap<String, String> data1) {
                Home.balance = data1.get("balance");
                balText.setText(getString(R.string.balance));
                balView.setText(Home.balance);
                checking = false;
            }

            @Override
            public void onSuccess(String success) {
                Home.checkNotif = true;
            }

            @Override
            public void onError(int i, String s) {
                super.onError(i, s);
                balText.setText(getString(R.string.balance));
                checking = false;
            }
        });
    }

    private void callnet() {
        if (!loadingDiag.isShowing()) loadingDiag.show();
        Customoffers.getCustomOffers(this, new onResponse() {
            @Override
            public void onSuccessHashListHashMap(HashMap<String, ArrayList<HashMap<String, String>>> hashListHashmap) {
                if (!isFinishing() && !isDestroyed()) {
                    ArrayList<HashMap<String, String>> list;
                    hashList = new HashMap<>();
                    list = GetNet.cpiInfos();
                    if (list.size() > 0) {
                        hashList.put("offer_sdk", list);
                        fragments.add(new OSdk());
                        tabs.add(getString(R.string.sdk_offerwalls));
                    }
                    list = GetNet.cpvInfos();
                    if (list.size() > 0) {
                        hashList.put("offer_video", list);
                        fragments.add(new OVideo());
                        tabs.add(getString(R.string.video_offers));
                    }
                    list = new ArrayList<>();
                    list.addAll(Objects.requireNonNull(hashListHashmap.get("i")));
                    list.addAll(Objects.requireNonNull(hashListHashmap.get("s")));
                    if (list.size() > 0) {
                        hashList.put("offer_premium", list);
                        fragments.add(new OPremium());
                        tabs.add(getString(R.string.premium_offers));
                    }
                    list = GetNet.apiInfos();
                    if (list.size() > 0) {
                        hashList.put("offer_api", list);
                        fragments.add(new OApi());
                        tabs.add(getString(R.string.api_offerwalls));
                    }
                    list = GetNet.webInfos(Offers.this);
                    if (list.size() > 0) {
                        hashList.put("offer_web", list);
                        fragments.add(new OWeb());
                        tabs.add(getString(R.string.web_offerwall));
                    }
                    pagerAdapter adapter = new pagerAdapter(Offers.this);
                    viewPager = findViewById(R.id.offers_viewPager);
                    viewPager.setAdapter(adapter);
                    TabLayout tabLayout = findViewById(R.id.offers_tabLayout);
                    new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> tab.setText(tabs.get(position))).attach();
                    loadingDiag.dismiss();
                }
            }

            @Override
            public void onError(int errorCode, String error) {
                loadingDiag.dismiss();
                if (errorCode == -9) {
                    dialog = Misc.noConnection(dialog, Offers.this, () -> {
                        callnet();
                        dialog.dismiss();
                    });
                } else {
                    Toast.makeText(Offers.this, "Could not connect to the server", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private class pagerAdapter extends FragmentStateAdapter {
        public pagerAdapter(AppCompatActivity fa) {
            super(fa);
        }

        @NonNull
        @Override
        public Fragment createFragment(int pos) {
            return fragments.get(pos);
        }

        @Override
        public int getItemCount() {
            return fragments.size();
        }
    }
}