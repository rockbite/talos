package com.talosvfx.talos.editor.widgets.ui.common;


import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.IntMap;

public class ColorLibrary {

    public static ColorLibrary instance;

    public static Color FONT_WHITE = new Color(Color.valueOf("#cccccc"));
    public static Color FONT_GRAY = new Color(Color.valueOf("#919191"));

    public enum BackgroundColor {
        LIGHT_GRAY ("#525252"),
        PANEL_GRAY ("#4e4d4d"),
        DARK_GRAY ("#3a3a3a"),
        BLACK ("#202020");

        private Color color;

        BackgroundColor(String hex) {
            this.color = new Color(Color.valueOf(hex));
        }

        public Color getColor() {
            return color;
        }
    }

    private ColorLibrary() {

    }

    private IntMap<Drawable> drawableCache = new IntMap<>();

    public static ColorLibrary instance() {
        if(instance == null) {
            instance = new ColorLibrary();
        }

        return instance;
    }

    public static Drawable obtainBackground(Skin skin, BackgroundColor backgroundColor) {
        int colorInteger = backgroundColor.color.toIntBits();
        if (instance().drawableCache.containsKey(colorInteger)) {
            return instance().drawableCache.get(colorInteger);
        } else {
            Drawable drawable = skin.newDrawable("timeline-white-bg", backgroundColor.getColor());
            instance().drawableCache.put(colorInteger, drawable);

            return drawable;
        }
    }
}
