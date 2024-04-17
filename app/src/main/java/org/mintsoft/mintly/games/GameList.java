package org.mintsoft.mintly.games;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.GridView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.mintsoft.mintlib.GetGame;
import org.mintsoft.mintlib.onResponse;
import org.mintsoft.mintly.R;
import org.mintsoft.mintly.helper.Misc;
import org.mintsoft.mintly.helper.imgAdapter;

import java.util.ArrayList;
import java.util.HashMap;

public class GameList extends AppCompatActivity {
    private GridView gridView;
    private Dialog loadingView, conDiag;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.game_html_gamelist);
        gridView = findViewById(R.id.game_html_hamelist_gridView);
        loadingView = Misc.loadingDiag(this);
        callNet();
        findViewById(R.id.game_html_gamelist_back).setOnClickListener(view -> finish());
    }

    private void callNet() {
        if (!loadingView.isShowing()) loadingView.show();
        GetGame.getHtml(this, "all", new onResponse() {
            @Override
            public void onSuccessListHashMap(ArrayList<HashMap<String, String>> list) {
                gridView.setAdapter(new imgAdapter(GameList.this, list, R.layout.game_html_gl_item));
                gridView.setOnItemClickListener((adapterView, view, i, l) -> {
                    HashMap<String, String> d = list.get(i);
                    String f = d.get("file");
                    String o = d.get("ori");
                    String na = d.get("na");
                    if (f == null || o == null || na == null) return;
                    if (f.startsWith("http")) {
                        startActivity(Misc.startHtml(GameList.this, f, o.equals("1"), na.equals("1")));
                    } else {
                        Toast.makeText(GameList.this, "This game is depreciated. Try other games", Toast.LENGTH_LONG).show();
                    }

                });
                loadingView.dismiss();
            }

            @Override
            public void onError(int errorCode, String error) {
                loadingView.dismiss();
                if (errorCode == -9) {
                    conDiag = Misc.noConnection(conDiag, GameList.this, () -> {
                        callNet();
                        loadingView.dismiss();
                    });
                } else {
                    Toast.makeText(GameList.this, error, Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        });
    }
}