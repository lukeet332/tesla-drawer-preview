package com.mg4.preview;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
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

/** Tesla-style app tray, day and night, from the owner's manual and the Model 3 reference photo. */
public final class MainActivity extends Activity {
    private float d;
    private final List<View> cells = new ArrayList<>();

    private int dp(float v) { return Math.round(v * d); }

    private static final String[][] APPS = {
            {"Vehicle", "car", null}, {"Climate", "fan", null}, {"Energy", "bolt", null},
            {"Phone", "phone", null}, {"Radio", "radio", null},
            {"Music", "music", null}, {"360 Camera", "cam360", null},
            {"Spotify", "wave", "#1DB954"}, {"Waze", "nav", "#33CCFF"}, {"YouTube", "video", "#FF0000"}
    };

    /** Tesla's two palettes: night is near-black with white type; day is a light panel with dark type. */
    static final class Skin {
        final int bgTop, bgBot, sheet, text, sub, line, accent, glow, shadow;
        final boolean night;
        Skin(boolean night) {
            this.night = night;
            if (night) {
                bgTop = 0xFF14171C; bgBot = 0xFF0A0C0F; sheet = 0xFF1A1E24;
                text = 0xFFF2F3F5; sub = 0xFF9AA1AA; line = 0xFF2A3038;
                accent = 0xFF4A90E2; glow = 0x16FFFFFF; shadow = 0x6E000000;
            } else {
                bgTop = 0xFFF2F4F7; bgBot = 0xFFE4E8ED; sheet = 0xFFFFFFFF;
                text = 0xFF111318; sub = 0xFF5B626B; line = 0xFFE3E7EC;
                accent = 0xFF3B7DD8; glow = 0x00FFFFFF; shadow = 0x22000000;
            }
        }
    }

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        d = getResources().getDisplayMetrics().density;
        boolean night = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES;
        final Skin s = new Skin(night);

        FrameLayout root = new FrameLayout(this);
        root.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{s.bgTop, s.bgBot}));

        if (s.night) {
            root.addView(new SoftGlow(this, s.glow, 0x00FFFFFF, 0.42f, 0.85f),
                    new FrameLayout.LayoutParams(-1, -1));
        }
        View shadowV = new SoftGlow(this, s.shadow, 0x00000000, 0.5f, 0.5f);
        FrameLayout.LayoutParams shlp = new FrameLayout.LayoutParams(dp(720), dp(96), Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        shlp.topMargin = dp(296);
        root.addView(shadowV, shlp);

        ImageView car = new ImageView(this);
        car.setImageResource(img("car_base"));
        car.setAdjustViewBounds(true);
        if (s.night) {
            float[] m = {1.7f,0,0,0,18, 0,1.7f,0,0,18, 0,0,1.7f,0,18, 0,0,0,1,0};
            car.setColorFilter(new android.graphics.ColorMatrixColorFilter(new android.graphics.ColorMatrix(m)));
        }
        FrameLayout.LayoutParams clp = new FrameLayout.LayoutParams(dp(760), -2, Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        clp.topMargin = dp(44);
        root.addView(car, clp);
        ImageView rim = new ImageView(this);
        rim.setImageResource(img("car_highlight"));
        rim.setAlpha(s.night ? 0.55f : 0.30f);
        FrameLayout.LayoutParams rlp = new FrameLayout.LayoutParams(dp(760), -2, Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        rlp.topMargin = dp(44);
        root.addView(rim, rlp);

        LinearLayout status = new LinearLayout(this);
        status.setOrientation(LinearLayout.HORIZONTAL);
        status.setGravity(Gravity.CENTER_VERTICAL);
        status.setPadding(dp(34), dp(18), dp(34), 0);
        status.addView(txt("11:51", 17, s.text, true));
        status.addView(spacer());
        status.addView(txt("13°C", 16, s.sub, false));
        status.addView(space(dp(22)));
        status.addView(circle("user", 26, 0, s.sub));
        status.addView(space(dp(16)));
        status.addView(circle("wifi", 24, 0, s.sub));
        status.addView(space(dp(14)));
        status.addView(circle("bluetooth", 22, 0, s.sub));
        status.addView(space(dp(14)));
        status.addView(txt("4G", 13, s.sub, true));
        root.addView(status, new FrameLayout.LayoutParams(-1, dp(70), Gravity.TOP));

        LinearLayout tray = new LinearLayout(this);
        tray.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable sheetBg = new GradientDrawable();
        sheetBg.setColor(s.sheet);
        sheetBg.setCornerRadii(new float[]{dp(28), dp(28), dp(28), dp(28), 0, 0, 0, 0});
        tray.setBackground(sheetBg);
        tray.setElevation(dp(20));
        tray.setPadding(dp(44), dp(12), dp(44), dp(8));

        View grab = new View(this);
        grab.setBackground(round(s.night ? 0xFF3C434C : 0xFFC9CED5, dp(2)));
        LinearLayout.LayoutParams grlp = new LinearLayout.LayoutParams(dp(46), dp(4));
        grlp.gravity = Gravity.CENTER_HORIZONTAL;
        tray.addView(grab, grlp);

        LinearLayout head = new LinearLayout(this);
        head.setOrientation(LinearLayout.HORIZONTAL);
        head.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams hlp = new LinearLayout.LayoutParams(-1, dp(46));
        hlp.topMargin = dp(4);
        tray.addView(head, hlp);
        TextView title = txt("Apps", 22, s.text, false);
        title.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        head.addView(title);
        head.addView(spacer());
        TextView edit = txt("Edit", 15, s.sub, false);
        edit.setPadding(dp(14), dp(4), dp(14), dp(4));
        head.addView(edit);
        head.addView(circle("gear", 40, 0, s.sub));

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
            View cell = cell(APPS[i], s);
            row.addView(cell, new LinearLayout.LayoutParams(dp(196), -1));
            cells.add(cell);
        }

        View line = new View(this);
        line.setBackgroundColor(s.line);
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(-1, dp(1));
        llp.topMargin = dp(4);
        tray.addView(line, llp);

        LinearLayout dock = new LinearLayout(this);
        dock.setOrientation(LinearLayout.HORIZONTAL);
        dock.setGravity(Gravity.CENTER);
        String[] dockIcons = {"phone", "radio", "music", "cam360", "gear"};
        for (int i = 0; i < dockIcons.length; i++) {
            View v = circle(dockIcons[i], 46, 0, i == 2 ? s.accent : s.text);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(46), dp(46));
            lp.setMargins(dp(34), dp(6), dp(34), dp(2));
            dock.addView(v, lp);
        }
        tray.addView(dock, new LinearLayout.LayoutParams(-1, -2));

        root.addView(tray, new FrameLayout.LayoutParams(-1, dp(392), Gravity.BOTTOM));

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

    private View cell(String[] app, Skin s) {
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setGravity(Gravity.CENTER);
        FrameLayout box = new FrameLayout(this);
        if (app[2] != null) box.setBackground(round(Color.parseColor(app[2]), dp(18)));
        ImageView iv = new ImageView(this);
        iv.setImageResource(icon(app[1]));
        iv.setColorFilter(app[2] != null ? Color.WHITE : s.text);
        int size = app[2] != null ? dp(56) : dp(58);
        box.addView(iv, new FrameLayout.LayoutParams(size, size, Gravity.CENTER));
        int boxSize = app[2] != null ? dp(80) : dp(62);
        col.addView(box, new LinearLayout.LayoutParams(boxSize, boxSize));
        TextView label = txt(app[0], 17, s.sub, false);
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

    private TextView txt(String s, float size, int color, boolean medium) {
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

    /** Radial falloff drawn with a shader: no shape boundary. */
    static final class SoftGlow extends View {
        private final int inner, outer;
        private final float cy, rs;

        SoftGlow(Context c, int inner, int outer, float cy, float rs) {
            super(c);
            this.inner = inner; this.outer = outer; this.cy = cy; this.rs = rs;
        }

        @Override protected void onDraw(Canvas cv) {
            Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
            float w = getWidth(), h = getHeight();
            p.setShader(new RadialGradient(w / 2f, h * cy, Math.max(w, h) * rs, inner, outer, Shader.TileMode.CLAMP));
            cv.drawRect(0, 0, w, h, p);
        }
    }
}
