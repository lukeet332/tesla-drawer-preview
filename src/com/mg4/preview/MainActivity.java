package com.mg4.preview;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
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

/** Tesla-style app tray: status bar, car hero over a soft glow, sheet with plain icons and a dock. */
public final class MainActivity extends Activity {
    private static final int BG_TOP = 0xFF14171C, BG_BOT = 0xFF0A0C0F;
    private static final int SHEET = 0xFF1A1E24, TEXT = 0xFFF2F3F5, SUB = 0xFF9AA1AA;
    private static final int ACCENT = 0xFF4A90E2;
    private float d;
    private final List<View> cells = new ArrayList<>();

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

        // soft light behind the car: a shader, so it has no shape edge
        root.addView(new SoftGlow(this, 0x16FFFFFF, 0x00FFFFFF, 0.42f, 0.85f),
                new FrameLayout.LayoutParams(-1, -1));
        // contact shadow under the car
        View shadowV = new SoftGlow(this, 0x6E000000, 0x00000000, 0.5f, 0.5f);
        FrameLayout.LayoutParams shlp = new FrameLayout.LayoutParams(dp(780), dp(110), Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        shlp.topMargin = dp(320);
        root.addView(shadowV, shlp);

        ImageView car = new ImageView(this);
        car.setImageResource(img("car_base"));
        car.setAdjustViewBounds(true);
        // the base render is a dark car: lift it without flattening the shading
        float[] m = {1.7f,0,0,0,18, 0,1.7f,0,0,18, 0,0,1.7f,0,18, 0,0,0,1,0};
        car.setColorFilter(new android.graphics.ColorMatrixColorFilter(new android.graphics.ColorMatrix(m)));
        FrameLayout.LayoutParams clp = new FrameLayout.LayoutParams(dp(820), -2, Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        clp.topMargin = dp(78);
        root.addView(car, clp);
        ImageView rim = new ImageView(this);
        rim.setImageResource(img("car_highlight"));
        rim.setAlpha(0.55f);
        FrameLayout.LayoutParams rlp = new FrameLayout.LayoutParams(dp(820), -2, Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        rlp.topMargin = dp(78);
        root.addView(rim, rlp);

        // Tesla-like status bar
        LinearLayout status = new LinearLayout(this);
        status.setOrientation(LinearLayout.HORIZONTAL);
        status.setGravity(Gravity.CENTER_VERTICAL);
        status.setPadding(dp(34), dp(18), dp(34), 0);
        status.addView(text("11:51", 17, TEXT, true));
        status.addView(spacer());
        status.addView(text("13°C", 16, SUB, false));
        status.addView(space(dp(22)));
        status.addView(circle("user", 26, 0, SUB));
        status.addView(space(dp(16)));
        status.addView(circle("wifi", 24, 0, SUB));
        status.addView(space(dp(14)));
        status.addView(circle("bluetooth", 22, 0, SUB));
        status.addView(space(dp(14)));
        status.addView(text("4G", 13, SUB, true));
        root.addView(status, new FrameLayout.LayoutParams(-1, dp(70), Gravity.TOP));

        // sheet
        LinearLayout tray = new LinearLayout(this);
        tray.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable sheetBg = new GradientDrawable();
        sheetBg.setColor(SHEET);
        sheetBg.setCornerRadii(new float[]{dp(28), dp(28), dp(28), dp(28), 0, 0, 0, 0});
        tray.setBackground(sheetBg);
        tray.setElevation(dp(20));
        tray.setPadding(dp(44), dp(12), dp(44), dp(8));

        View grab = new View(this);
        grab.setBackground(round(0xFF3C434C, dp(2)));
        LinearLayout.LayoutParams grlp = new LinearLayout.LayoutParams(dp(46), dp(4));
        grlp.gravity = Gravity.CENTER_HORIZONTAL;
        tray.addView(grab, grlp);

        LinearLayout head = new LinearLayout(this);
        head.setOrientation(LinearLayout.HORIZONTAL);
        head.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams hlp = new LinearLayout.LayoutParams(-1, dp(44));
        hlp.topMargin = dp(6);
        tray.addView(head, hlp);
        head.addView(text("Apps", 21, TEXT, true));
        head.addView(spacer());
        TextView edit = text("Edit", 15, SUB, false);
        edit.setPadding(dp(14), dp(4), dp(14), dp(4));
        head.addView(edit);
        head.addView(circle("gear", 40, 0, SUB));

        LinearLayout grid = new LinearLayout(this);
        grid.setOrientation(LinearLayout.VERTICAL);
        tray.addView(grid, new LinearLayout.LayoutParams(-1, -2));
        LinearLayout row = null;
        for (int i = 0; i < APPS.length; i++) {
            if (i % 5 == 0) {
                row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setGravity(Gravity.CENTER);
                grid.addView(row, new LinearLayout.LayoutParams(-1, dp(122)));
            }
            View cell = cell(APPS[i]);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(196), -1);
            row.addView(cell, lp);
            cells.add(cell);
        }

        View line = new View(this);
        line.setBackgroundColor(0xFF2A3038);
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(-1, dp(1));
        llp.topMargin = dp(4);
        tray.addView(line, llp);

        LinearLayout dock = new LinearLayout(this);
        dock.setOrientation(LinearLayout.HORIZONTAL);
        dock.setGravity(Gravity.CENTER);
        String[] dockIcons = {"phone", "radio", "music", "cam360", "gear"};
        for (int i = 0; i < dockIcons.length; i++) {
            View v = circle(dockIcons[i], 46, 0, i == 2 ? ACCENT : TEXT);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(46), dp(46));
            lp.setMargins(dp(34), dp(6), dp(34), dp(2));
            dock.addView(v, lp);
        }
        tray.addView(dock, new LinearLayout.LayoutParams(-1, -2));

        FrameLayout.LayoutParams slp = new FrameLayout.LayoutParams(-1, dp(392), Gravity.BOTTOM);
        root.addView(tray, slp);

        setContentView(root);
        tray.setTranslationY(dp(50));
        tray.setAlpha(0f);
        tray.post(new Runnable() {
            @Override public void run() { tray.animate().translationY(0f).alpha(1f).setDuration(340).start(); }
        });
        for (int i = 0; i < cells.size(); i++) {
            final View c = cells.get(i);
            c.setAlpha(0f);
            c.setTranslationY(dp(12));
            c.postDelayed(new Runnable() {
                @Override public void run() { c.animate().alpha(1f).translationY(0f).setDuration(280).start(); }
            }, 80 + i * 24L);
        }
    }

    private View cell(String[] app) {
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setGravity(Gravity.CENTER);
        FrameLayout box = new FrameLayout(this);
        if (app[2] != null) box.setBackground(round(Color.parseColor(app[2]), dp(18)));
        ImageView iv = new ImageView(this);
        iv.setImageResource(icon(app[1]));
        iv.setColorFilter(Color.WHITE);
        int size = app[2] != null ? dp(56) : dp(58);
        box.addView(iv, new FrameLayout.LayoutParams(size, size, Gravity.CENTER));
        int boxSize = app[2] != null ? dp(80) : dp(62);
        col.addView(box, new LinearLayout.LayoutParams(boxSize, boxSize));
        TextView label = text(app[0], 16, SUB, false);
        label.setGravity(Gravity.CENTER);
        label.setSingleLine(true);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.topMargin = dp(8);
        col.addView(label, lp);
        return col;
    }

    private View circle(String icon, int size, int bg, int tint) {
        FrameLayout f = new FrameLayout(this);
        if (bg != 0) f.setBackground(round(bg, dp(size / 2f)));
        ImageView iv = new ImageView(this);
        iv.setImageResource(icon(icon));
        iv.setColorFilter(tint);
        f.addView(iv, new FrameLayout.LayoutParams(dp(size * 0.55f), dp(size * 0.55f), Gravity.CENTER));
        return f;
    }

    private View space(int px) {
        View v = new View(this);
        v.setLayoutParams(new LinearLayout.LayoutParams(px, 1));
        return v;
    }

    private int icon(String name) {
        return getResources().getIdentifier("ic_" + name, "drawable", getPackageName());
    }

    private int img(String name) {
        return getResources().getIdentifier(name, "drawable", getPackageName());
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

    /** Radial falloff drawn with a shader: no oval boundary, just light. */
    static final class SoftGlow extends View {
        private final int inner, outer;
        private final float cy, rs;

        SoftGlow(Context c, int inner, int outer, float cy, float rs) {
            super(c);
            this.inner = inner;
            this.outer = outer;
            this.cy = cy;
            this.rs = rs;
        }

        @Override protected void onDraw(Canvas cv) {
            Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
            float w = getWidth(), h = getHeight();
            float r = Math.max(w, h) * rs;
            p.setShader(new RadialGradient(w / 2f, h * cy, r, inner, outer, Shader.TileMode.CLAMP));
            cv.drawRect(0, 0, w, h, p);
        }
    }
}
