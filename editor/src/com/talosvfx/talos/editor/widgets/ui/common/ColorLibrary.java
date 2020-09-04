package com.talosvfx.talos.editor.widgets.ui.common;


import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.ObjectMap;
import com.talosvfx.talos.editor.widgets.ClippedNinePatchDrawable;

public class ColorLibrary {

    public static ColorLibrary instance;

    public static Color FONT_WHITE = new Color(Color.valueOf("#cccccc"));
    public static Color FONT_GRAY = new Color(Color.valueOf("#919191"));

    public static Color BLUE = new Color(Color.valueOf("#4f8cb6"));

    public static String SHAPE_SQUIRCLE = "squircle-6";
    public static String SHAPE_SQUIRCLE_TOP = "squircle-t-6";
    public static String SHAPE_SQUIRCLE_BOTTOM = "squircle-b-6";
    public static String SHAPE_CIRCLE = "circle";
    public static String SHAPE_SQUARE = "square-patch";

    public enum BackgroundColor {
        BRIGHT_GRAY ("#787878"),
        LIGHT_GRAY ("#525252"),
        PANEL_GRAY ("#4e4d4d"),
        MID_GRAY ("#434343"),
        DARK_GRAY ("#3a3a3a"),
        BLACK_TRANSPARENT ("#2a2a2add"),
        RED ("#92313a"),

        LIGHT_BLUE ("#5b86ae"),

        BLACK ("#202020"),
        WHITE ("#FFFFFF"),
        BROKEN_WHITE ("#b5b5b5");

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

    private IntMap<Drawable> colorDrawableCache = new IntMap<>();
    private IntMap<Drawable> drawableCache = new IntMap<>();
    private IntMap<ClippedNinePatchDrawable> clippedPatchCache = new IntMap<>();

    public static ColorLibrary instance() {
        if(instance == null) {
            instance = new ColorLibrary();
        }

        return instance;
    }

    public static ClippedNinePatchDrawable createClippedPatch(Skin skin, String name, BackgroundColor backgroundColor) {
        ClippedNinePatchDrawable drawable = new ClippedNinePatchDrawable((TextureAtlas.AtlasRegion) skin.getRegion(name));
        drawable.setColor(backgroundColor.getColor());

        return drawable;
    }

    public static ClippedNinePatchDrawable obtainClippedPatch(Skin skin, String name, BackgroundColor backgroundColor) {
        int colorInteger = backgroundColor.color.toIntBits();
        int code = name.hashCode() * 31 + colorInteger;
        if (instance().clippedPatchCache.containsKey(code)) {
            return instance().clippedPatchCache.get(code);
        } else {
            ClippedNinePatchDrawable drawable = new ClippedNinePatchDrawable((TextureAtlas.AtlasRegion) skin.getRegion(name));
            drawable.setColor(backgroundColor.getColor());
            instance().clippedPatchCache.put(code, drawable);

            return drawable;
        }
    }

    public static Drawable obtainBackground(Skin skin, String name, BackgroundColor backgroundColor) {
        int colorInteger = backgroundColor.color.toIntBits();
        int code = name.hashCode() * 31 + colorInteger;
        if (instance().drawableCache.containsKey(code)) {
            return instance().drawableCache.get(code);
        } else {
            Drawable drawable = skin.newDrawable(name, backgroundColor.getColor());
            instance().drawableCache.put(code, drawable);

            return drawable;
        }
    }

    public static Drawable obtainBackground(Skin skin, BackgroundColor backgroundColor) {
        int colorInteger = backgroundColor.color.toIntBits();
        if (instance().colorDrawableCache.containsKey(colorInteger)) {
            return instance().colorDrawableCache.get(colorInteger);
        } else {
            Drawable drawable = skin.newDrawable("timeline-white-bg", backgroundColor.getColor());
            instance().colorDrawableCache.put(colorInteger, drawable);

            return drawable;
        }
    }
}
