package org.mintsoft.mintly.helper;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.mintsoft.mintlib.HtmlGame;
import org.mintsoft.mintly.Home;
import org.mintsoft.mintly.R;

import java.util.HashMap;
import java.util.Locale;

public class Misc {
    public interface yesNo {
        void yes();

        void no();
    }

    public interface resp {
        void clicked();
    }

    public static void setLogo(Context context, TextView textView) {
        if (context.getString(R.string.app_name).equals("Mintly")) {
            Spannable word1 = new SpannableString("Mint");
            word1.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.green_1)), 0, word1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            word1.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, word1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            textView.setText(word1);
            Spannable word2 = new SpannableString("ly");
            word2.setSpan(new ForegroundColorSpan(Color.WHITE), 0, word2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            textView.append(word2);
        }
    }

    public static Spanned html(String text) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(text);
        }
    }

    public static void listAnimate(final LinearLayoutManager llm, final RecyclerView recList) {
        new Handler().postDelayed(() -> {
            int start = llm.findFirstVisibleItemPosition();
            int end = llm.findLastVisibleItemPosition();
            int DELAY = 50;
            RecyclerView.ViewHolder vw;
            for (int i = start; i <= end; i++) {
                vw = recList.findViewHolderForAdapterPosition(i);
                if (vw == null) return;
                View v = vw.itemView;
                v.setAlpha(0);
                PropertyValuesHolder slide = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, 150, 0);
                PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat(View.ALPHA, 0, 1);
                ObjectAnimator a = ObjectAnimator.ofPropertyValuesHolder(v, slide, alpha);
                a.setDuration(600);
                a.setStartDelay((long) i * DELAY);
                a.setInterpolator(new DecelerateInterpolator());
                a.start();
            }
            recList.setAlpha(1);

        }, 50);
    }

    public static void onenUrl(Context context, String url) {
        if (Home.isExternal) {
            Intent sendIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            Intent chooser = Intent.createChooser(sendIntent, context.getString(R.string.open_url_with));
            if (sendIntent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(chooser);
            } else {
                context.startActivity(new Intent(context, Surf.class).putExtra("url", url));
            }
        } else {
            context.startActivity(new Intent(context, Surf.class).putExtra("url", url));
        }
    }

    @SuppressWarnings("unchecked")
    public static HashMap<String, String> convertToHashMap(Intent intent, String key) {
        try {
            return (HashMap<String, String>) intent.getSerializableExtra(key);
        } catch (Exception e) {
            return null;
        }
    }

    public static ProgressDialog customProgress(Context context) {
        ProgressDialog dialog = new ProgressDialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setMessage(context.getString(R.string.please_wait));
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        Window w = dialog.getWindow();
        if (w != null) {
            w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            w.setNavigationBarColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));
        }
        return dialog;
    }

    public static Dialog decoratedDiag(Context context, int layout, float opacity) {
        Dialog dialog = new Dialog(context, android.R.style.Theme_Translucent_NoTitleBar);
        View view = LayoutInflater.from(context).inflate(layout, null);
        dialog.setContentView(view);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        Window w = dialog.getWindow();
        WindowManager.LayoutParams lp = w.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.dimAmount = opacity;
        lp.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        w.setAttributes(lp);
        w.setGravity(Gravity.CENTER);
        w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        w.setStatusBarColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));
        w.setNavigationBarColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));
        return dialog;
    }

    public static Animation alphaAnim() {
        Animation anim = new AlphaAnimation(0.5f, 1.0f);
        anim.setDuration(800);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);
        return anim;
    }

    public static Dialog loadingDiag(Context context) {
        Dialog loadingDialog = Misc.decoratedDiag(context, R.layout.dialog_loading, 0.8f);
        loadingDialog.setCancelable(false);
        ImageView imageView = loadingDialog.findViewById(R.id.dialog_loading_imageView);
        AnimationDrawable anim = (AnimationDrawable) imageView.getDrawable();
        anim.start();
        return loadingDialog;
    }

    public static Dialog lowbalanceDiag(Activity activity, yesNo yn) {
        String credit = " " + Home.currency.toLowerCase() + "s";
        Dialog lowbalDiag = Misc.decoratedDiag(activity, R.layout.dialog_quit, 0.8f);
        lowbalDiag.setCancelable(false);
        TextView lowbalTitle = lowbalDiag.findViewById(R.id.dialog_quit_title);
        TextView lowbalDesc = lowbalDiag.findViewById(R.id.dialog_quit_desc);
        TextView quitNo = lowbalDiag.findViewById(R.id.dialog_quit_no);
        TextView quitYes = lowbalDiag.findViewById(R.id.dialog_quit_yes);
        lowbalTitle.setText((activity.getString(R.string.low_bal_title) + credit + "!"));
        lowbalDesc.setText((activity.getString(R.string.low_bal_desc_prefix)
                + credit + ". " + activity.getString(R.string.low_bal_desc_suffix) + credit + "?"));
        quitNo.setText((activity.getString(R.string.earn) + credit));
        quitYes.setText(activity.getString(R.string.quit));
        quitNo.setOnClickListener(view -> {
            yn.yes();
        });
        quitYes.setOnClickListener(view -> {
            yn.no();
        });
        return lowbalDiag;
    }

    private static Dialog diag;

    public static Dialog noConnection(Dialog conDiag, Context activity, resp r) {
        diag = conDiag;
        if (diag == null) {
            diag = new Dialog(activity, android.R.style.Theme_Translucent_NoTitleBar);
            View views = LayoutInflater.from(activity).inflate(R.layout.dialog_connection, null);
            diag.setContentView(views);
            diag.setCancelable(false);
            Window w = diag.getWindow();
            w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            w.setNavigationBarColor(ContextCompat.getColor(activity, R.color.colorPrimaryDark));
            w.setStatusBarColor(ContextCompat.getColor(activity, R.color.colorPrimaryDark));
            views.findViewById(R.id.dialog_connection_exit).setOnClickListener(view -> {
                diag.dismiss();
                ((Activity) activity).finish();
            });
            views.findViewById(R.id.dialog_connection_retry).setOnClickListener(view -> r.clicked());
        }
        try {
            diag.show();
        } catch (Exception ignored) {

        }
        return diag;
    }

    public static void lockedDiag(Activity activity, String title, String desc) {
        Dialog lokDiag = new Dialog(activity, android.R.style.Theme_Translucent_NoTitleBar);
        View lowBalView = LayoutInflater.from(activity).inflate(R.layout.dialog_connection, null);
        lokDiag.setContentView(lowBalView);
        lokDiag.setCancelable(false);
        Window w = lokDiag.getWindow();
        w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        w.setNavigationBarColor(ContextCompat.getColor(activity, R.color.colorPrimaryDark));
        w.setStatusBarColor(ContextCompat.getColor(activity, R.color.colorPrimaryDark));
        ImageView lokImgView = lowBalView.findViewById(R.id.dialog_connection_img);
        lokImgView.setImageResource(R.drawable.ic_warning);
        TextView lokTitleView = lowBalView.findViewById(R.id.dialog_connection_title);
        lokTitleView.setTextColor(ContextCompat.getColor(activity, android.R.color.holo_red_light));
        lokTitleView.setText(title);
        TextView lokDescView = lowBalView.findViewById(R.id.dialog_connection_desc);
        lokDescView.setText(desc);
        lowBalView.findViewById(R.id.dialog_connection_retry).setVisibility(View.GONE);
        lowBalView.findViewById(R.id.dialog_connection_exit).setOnClickListener(view -> {
            lokDiag.dismiss();
            activity.finish();
        });
        lokDiag.show();
    }

    public static Animation btnEffect() {
        Animation anim = new ScaleAnimation(
                1f, 0.9f, 1f, 0.9f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setRepeatCount(1);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setDuration(80);
        return anim;
    }

    public static void showMessage(Context context, String message, boolean closeActivity) {
        AlertDialog ad = new AlertDialog.Builder(context)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(context.getString(R.string.got_it), (dialog, id) -> {
                    dialog.dismiss();
                    if (closeActivity) ((Activity) context).finish();
                }).create();
        ad.setOnShowListener(arg0 -> ad.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                ContextCompat.getColor(context, R.color.fb_color)
        ));
        ad.show();
    }

    public static Bitmap addGradient(Bitmap bitmap) {
        final int reflectionGap = 10;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Matrix matrix = new Matrix();
        matrix.preScale(1, -1);
        Bitmap rBitmap = Bitmap.createBitmap(bitmap, 0, height / 2, width, height / 2, matrix, false);
        Bitmap fBitmap = Bitmap.createBitmap(width, (height + height / 3), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(fBitmap);
        canvas.drawBitmap(bitmap, 0, 0, null);
        canvas.drawBitmap(rBitmap, 0, height + reflectionGap, null);
        Paint paint = new Paint();
        LinearGradient shader = new LinearGradient(0, bitmap.getHeight(), 0, fBitmap.getHeight() + reflectionGap, 0x40ffffff, 0x00ffffff, Shader.TileMode.CLAMP);
        paint.setShader(shader);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        canvas.drawRect(0, height, width, fBitmap.getHeight() + reflectionGap, paint);
        if (bitmap.isRecycled()) {
            bitmap.recycle();
        }
        if (rBitmap != null && rBitmap.isRecycled()) {
            rBitmap.recycle();
        }
        return fBitmap;
    }

    public static int dpToPx(Context context, int dp) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public static Intent startHtml(Context context, String f, boolean ori, boolean na) {
        Intent intent = new Intent(context, HtmlGame.class);
        intent.putExtra("index", f);
        intent.putExtra("landscape", ori);
        intent.putExtra("layout", R.layout.dialog_quit);
        intent.putExtra("tv", R.id.dialog_quit_desc);
        intent.putExtra("y", R.id.dialog_quit_yes);
        intent.putExtra("n", R.id.dialog_quit_no);
        intent.putExtra("desc", context.getString(R.string.close_diag_desc2));
        intent.putExtra("color", ContextCompat.getColor(context, R.color.colorPrimaryDark));
        intent.putExtra("na", na);
        return intent;
    }

    public static void setLocale(Activity activity, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Resources resources = activity.getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }

    public static void chooseLocale(Activity activity, SharedPreferences spf, yesNo res) {
        Dialog localeDiag = new Dialog(activity, android.R.style.Theme_Translucent_NoTitleBar);
        View dView = LayoutInflater.from(activity).inflate(R.layout.dialog_locale, null);
        localeDiag.setContentView(dView);
        localeDiag.setCancelable(false);
        Window w = localeDiag.getWindow();
        w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        w.setNavigationBarColor(ContextCompat.getColor(activity, R.color.colorPrimaryDark));
        w.setStatusBarColor(ContextCompat.getColor(activity, R.color.colorPrimaryDark));
        localeDiag.findViewById(R.id.dialog_locale_en).setOnClickListener(view -> {
            spf.edit().putString("app_locale", "en").apply();
            Variables.locale = "en";
            localeDiag.dismiss();
            res.yes();
        });
        localeDiag.findViewById(R.id.dialog_locale_ar).setOnClickListener(view -> {
            spf.edit().putString("app_locale", "ar").apply();
            Variables.locale = "ar";
            localeDiag.dismiss();
            res.yes();
        });
        localeDiag.findViewById(R.id.dialog_locale_hi).setOnClickListener(view -> {
            spf.edit().putString("app_locale", "hi").apply();
            Variables.locale = "hi";
            localeDiag.dismiss();
            res.yes();
        });
        localeDiag.findViewById(R.id.dialog_locale_close).setOnClickListener(view -> {
            localeDiag.dismiss();
            res.no();
        });
        localeDiag.show();
    }

}