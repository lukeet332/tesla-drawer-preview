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
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Tesla-style app tray preview: the car stays the hero, the tray is a sheet that slides up
 * over it, icons sit directly on the sheet (no chips), and a dock runs along the bottom.
 */
public final class MainActivity extends Activity {
    private static final int BG_TOP = 0xFF0B0D10, BG_BOT = 0xFF15181D;
    private static final int SHEET = 0xFF101318, TEXT = 0xFFF2F3F5, SUB = 0xFF8E959E;
    private float d;
    private final List<View> cells = new ArrayList<>();
    private View sheet;

    private int dp(float v) { return Math.round(v * d); }

    private static final String[][] APPS = {
            {"Vehicle", "car", null}, {"Climate", "fan", null}, {"Energy", "bolt", null},
            {"Phone", "phone", null}, {"Radio", "radio", null},
            {"Music", "music", null}, {"360 Camera", "cam360", null},
            {"Spotify", "wave", "#1DB954"}, {"Waze", "nav", "#33CCFF"}, {"YouTube", "video", "#FF0000"}
    };

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        d = getResources().getDisplayMetrics().density;

        FrameLayout root = new FrameLayout(this);
        root.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{BG_TOP, BG_BOT}));

        // car hero, as on the Tesla home screen
        ImageView car = new ImageView(this);
        car.setImageResource(icon("car_base"));
        car.setAdjustViewBounds(true);
        FrameLayout.LayoutParams clp = new FrameLayout.LayoutParams(dp(860), -2, Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        clp.topMargin = dp(34);
        root.addView(car, clp);

        // small top bar
        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setPadding(dp(36), dp(22), dp(36), 0);
        bar.addView(text("Apps", 30, TEXT, true));
        bar.addView(spacer());
        TextView edit = text("Edit", 16, SUB, false);
        edit.setPadding(dp(16), dp(6), dp(16), dp(6));
        bar.addView(edit);
        bar.addView(circle("gear", 44, 0x00000000, SUB));
        root.addView(bar, new FrameLayout.LayoutParams(-1, dp(80), Gravity.TOP));

        // the tray
        LinearLayout tray = new LinearLayout(this);
        tray.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable sheetBg = new GradientDrawable();
        sheetBg.setColor(SHEET);
        sheetBg.setCornerRadii(new float[]{dp(26), dp(26), dp(26), dp(26), 0, 0, 0, 0});
        tray.setBackground(sheetBg);
        tray.setPadding(dp(40), dp(14), dp(40), dp(10));

        View grab = new View(this);
        grab.setBackground(round(0xFF3A4049, dp(2)));
        LinearLayout.LayoutParams glp = new LinearLayout.LayoutParams(dp(44), dp(4));
        glp.gravity = Gravity.CENTER_HORIZONTAL;
        tray.addView(grab, glp);

        LinearLayout grid = new LinearLayout(this);
        grid.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams gridLp = new LinearLayout.LayoutParams(-1, -2);
        gridLp.topMargin = dp(18);
        tray.addView(grid, gridLp);
        LinearLayout row = null;
        for (int i = 0; i < APPS.length; i++) {
            if (i % 5 == 0) {
                row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                grid.addView(row, new LinearLayout.LayoutParams(-1, dp(118)));
            }
            View cell = cell(APPS[i]);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(190), -1);
            lp.setMargins(dp(8), 0, dp(8), 0);
            row.addView(cell, lp);
            cells.add(cell);
        }

        View line = new View(this);
        line.setBackgroundColor(0xFF242A31);
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(-1, dp(1));
        llp.topMargin = dp(6);
        tray.addView(line, llp);

        LinearLayout dock = new LinearLayout(this);
        dock.setOrientation(LinearLayout.HORIZONTAL);
        dock.setGravity(Gravity.CENTER);
        String[] dockIcons = {"phone", "radio", "music", "cam360", "gear"};
        for (int i = 0; i < dockIcons.length; i++) {
            View v = circle(dockIcons[i], 54, 0x00000000, i == 2 ? TEXT : SUB);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(54), dp(54));
            lp.setMargins(dp(30), dp(8), dp(30), dp(4));
            dock.addView(v, lp);
        }
        tray.addView(dock, new LinearLayout.LayoutParams(-1, -2));

        FrameLayout.LayoutParams slp = new FrameLayout.LayoutParams(-1, dp(410), Gravity.BOTTOM);
        root.addView(tray, slp);
        sheet = tray;

        setContentView(root);
        tray.setTranslationY(dp(60));
        tray.setAlpha(0f);
        tray.post(new Runnable() {
            @Override public void run() {
                tray.animate().translationY(0f).alpha(1f).setDuration(320).start();
            }
        });
        for (int i = 0; i < cells.size(); i++) {
            final View c = cells.get(i);
            c.setAlpha(0f);
            c.setTranslationY(dp(10));
            c.postDelayed(new Runnable() {
                @Override public void run() { c.animate().alpha(1f).translationY(0f).setDuration(260).start(); }
            }, 60 + i * 22L);
        }
    }

    private View cell(String[] app) {
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setGravity(Gravity.CENTER);
        FrameLayout box = new FrameLayout(this);
        if (app[2] != null) box.setBackground(round(Color.parseColor(app[2]), dp(16)));
        ImageView iv = new ImageView(this);
        iv.setImageResource(icon(app[1]));
        iv.setColorFilter(Color.WHITE);
        int size = app[2] != null ? dp(52) : dp(54);
        box.addView(iv, new FrameLayout.LayoutParams(size, size, Gravity.CENTER));
        int boxSize = app[2] != null ? dp(74) : dp(62);
        col.addView(box, new LinearLayout.LayoutParams(boxSize, boxSize));
        TextView label = text(app[0], 14, SUB, false);
        label.setGravity(Gravity.CENTER);
        label.setSingleLine(true);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.topMargin = dp(6);
        col.addView(label, lp);
        return col;
    }

    private View circle(String icon, int size, int bg, int tint) {
        FrameLayout f = new FrameLayout(this);
        if (bg != 0) f.setBackground(round(bg, dp(size / 2f)));
        ImageView iv = new ImageView(this);
        iv.setImageResource(icon(icon));
        iv.setColorFilter(tint);
        f.addView(iv, new FrameLayout.LayoutParams(dp(size * 0.5f), dp(size * 0.5f), Gravity.CENTER));
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
}
