package com.mg4.preview;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/** Static preview of the Tesla-style app drawer (no car services, no permissions). */
public final class MainActivity extends Activity {
    private static final int BG_TOP = 0xFF191C22, BG_BOT = 0xFF07080A;
    private static final int PANEL = 0xFF1E2126, TEXT = 0xFFF2F3F5, SUB = 0xFF9BA1A9;
    private float d;
    private final List<View> tiles = new ArrayList<>();

    private int dp(float v) { return Math.round(v * d); }

    private static final String[][] CAR = {
            {"Vehicle", "car"}, {"Climate", "fan"}, {"Energy", "bolt"}, {"Consumption", "chart"}, {"Phone", "phone"},
            {"Radio", "radio"}, {"Music", "music"}, {"USB video", "video"}, {"360 Camera", "cam360"}, {"Settings", "gear"},
            {"Navigator", "nav"}, {"EV Route", "route"}
    };
    private static final String[][] MINE = {
            {"Spotify", "wave", "#1DB954"}, {"Waze", "nav", "#33CCFF"}, {"YouTube", "video", "#FF0000"}
    };

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        d = getResources().getDisplayMetrics().density;
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{BG_TOP, BG_BOT}));
        int pad = dp(20);
        root.setPadding(pad, pad, pad, pad);

        LinearLayout head = new LinearLayout(this);
        head.setOrientation(LinearLayout.HORIZONTAL);
        head.setGravity(Gravity.CENTER_VERTICAL);
        head.setPadding(dp(8), dp(4), dp(8), dp(4));
        head.addView(text("Apps", 32, TEXT, true));
        head.addView(spacer());
        TextView edit = text("Edit", 19, TEXT, true);
        edit.setGravity(Gravity.CENTER);
        edit.setPadding(dp(26), 0, dp(26), 0);
        edit.setBackground(round(PANEL, dp(30)));
        LinearLayout.LayoutParams elp = new LinearLayout.LayoutParams(-2, dp(58));
        elp.setMargins(0, 0, dp(12), 0);
        head.addView(edit, elp);
        head.addView(circle("gear", 58, PANEL));
        root.addView(head);

        ScrollView sv = new ScrollView(this);
        sv.setVerticalScrollBarEnabled(false);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(32), dp(18), dp(32), dp(20));
        content.addView(section("AUTO"));
        content.addView(grid(CAR, false));
        content.addView(section("INSTALLED BY YOU"));
        content.addView(grid(MINE, true));
        sv.addView(content, new FrameLayout.LayoutParams(-1, -2));
        root.addView(sv, new LinearLayout.LayoutParams(-1, 0, 1f));

        root.addView(dock());
        setContentView(root);
        animate();
    }

    private View section(String title) {
        TextView tv = text(title, 15, SUB, true);
        tv.setLetterSpacing(0.07f);
        tv.setPadding(0, 0, 0, dp(14));
        return tv;
    }

    private View grid(String[][] apps, boolean mine) {
        LinearLayout all = new LinearLayout(this);
        all.setOrientation(LinearLayout.VERTICAL);
        LinearLayout row = null;
        for (int i = 0; i < apps.length; i++) {
            if (i % 5 == 0) {
                row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setGravity(Gravity.CENTER);
                LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(-1, dp(200));
                all.addView(row, rlp);
            }
            View t = tile(apps[i], mine);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(190), -1);
            lp.setMargins(dp(10), 0, dp(10), 0);
            row.addView(t, lp);
            tiles.add(t);
        }
        
        return all;
    }

    private View tile(String[] app, boolean mine) {
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setGravity(Gravity.CENTER);
        FrameLayout box = new FrameLayout(this);
        int bg = PANEL;
        if (mine && app.length > 2) bg = Color.parseColor(app[2]);
        box.setBackground(round(bg, dp(32)));
        ImageView iv = new ImageView(this);
        iv.setImageResource(icon(app[1]));
        if (mine) iv.setColorFilter(Color.WHITE);
        box.addView(iv, new FrameLayout.LayoutParams(dp(58), dp(58), Gravity.CENTER));
        col.addView(box, new LinearLayout.LayoutParams(dp(128), dp(128)));
        TextView label = text(app[0], 18, TEXT, true);
        label.setGravity(Gravity.CENTER);
        label.setSingleLine(true);
        col.addView(label, new LinearLayout.LayoutParams(-1, -2));
        return col;
    }

    private View dock() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);
        String[] icons = {"phone", "radio", "music", "cam360", "gear"};
        for (int i = 0; i < icons.length; i++) {
            View b = circle(icons[i], 68, PANEL);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(68), dp(68));
            if (i > 0) lp.setMargins(dp(26), 0, 0, 0);
            row.addView(b, lp);
        }
        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.HORIZONTAL);
        wrap.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams wlp = new LinearLayout.LayoutParams(-1, dp(90));
        wlp.setMargins(0, dp(8), 0, 0);
        wrap.addView(row, new LinearLayout.LayoutParams(-2, dp(86)));
        wrap.setId(android.R.id.content + 1);
        return wrap;
    }

    private View circle(String icon, int size, int bg) {
        FrameLayout f = new FrameLayout(this);
        f.setBackground(round(bg, dp(size / 2f)));
        ImageView iv = new ImageView(this);
        iv.setImageResource(icon(icon));
        f.addView(iv, new FrameLayout.LayoutParams(dp(size * 0.44f), dp(size * 0.44f), Gravity.CENTER));
        return f;
    }

    private int icon(String name) {
        return getResources().getIdentifier("ic_" + name, "drawable", getPackageName());
    }

    private TextView text(String s, float size, int color, boolean medium) {
        TextView tv = new TextView(this);
        tv.setText(s);
        tv.setTextSize(size);
        tv.setTextColor(color);
        tv.setTypeface(Typeface.create(medium ? "sans-serif-medium" : "sans-serif", Typeface.NORMAL));
        return tv;
    }

    private View spacer() {
        View v = new View(this);
        v.setLayoutParams(new LinearLayout.LayoutParams(0, 1, 1f));
        return v;
    }

    private GradientDrawable round(int color, int radius) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(radius);
        return g;
    }

    private void animate() {
        for (int i = 0; i < tiles.size(); i++) {
            final View t = tiles.get(i);
            t.setAlpha(0f);
            t.setTranslationY(dp(16));
            t.postDelayed(new Runnable() {
                @Override public void run() {
                    t.animate().alpha(1f).translationY(0f).setDuration(320).start();
                }
            }, i * 26L);
        }
    }
}
