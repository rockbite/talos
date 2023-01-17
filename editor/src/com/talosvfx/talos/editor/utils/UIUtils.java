package com.talosvfx.talos.editor.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.OrderedMap;
import com.talosvfx.talos.editor.project2.SharedResources;

public class UIUtils {

    public static OrderedMap<Integer, BitmapFont> orderedFontMap = new OrderedMap<>();

    public static int getClosestFontSize(float preferredFontSize) {
        int maxSize = 15;
        int minSize = 4;
        int round = MathUtils.round(preferredFontSize);
        if (orderedFontMap.containsKey(round)) {
            return round;
        }

        int foundBiggerSize = -1;
        for (int i = round; i <= maxSize; i++) {
            if (orderedFontMap.containsKey(i)) {
                foundBiggerSize = i;
                break;
            }
        }

        int foundSmallerSize = -1;
        for (int i = round; i >= minSize; i--) {
            if (orderedFontMap.containsKey(i)) {
                foundSmallerSize = i;
                break;
            }
        }

        if (foundSmallerSize == -1 && foundBiggerSize == -1) {
            // THIS MUST NEVER HAPPEN
            return -1;
        }

        if (foundSmallerSize == -1) {
            return foundBiggerSize;
        }

        if (foundBiggerSize == -1) {
            return foundSmallerSize;
        }

        if (round - foundSmallerSize < foundBiggerSize - round) {
            return foundSmallerSize;
        } else {
            return foundBiggerSize;
        }
    }

    public enum RegisteredFont {
        DEFAULT("generated-font-15", 15), SMALL("generated-font-12", 12);

        public String fontName;
        public int defaultSize;

        RegisteredFont(String fontName, int defaultSize) {
            this.fontName = fontName;
            this.defaultSize = defaultSize;
        }

        public static RegisteredFont getFontByName(String fontName) {
            for (RegisteredFont value : RegisteredFont.values()) {
                if (fontName.contains(value.fontName)) {
                    return value;
                }
            }

            return null;
        }
    }


    public static void registerFonts (Skin skin) {
        FreeTypeFontGenerator freeTypeFontGenerator = new FreeTypeFontGenerator(Gdx.files.local("skin/VisOpenSans.ttf"));
        for (int i = 4; i < 75; i++) {
            FreeTypeFontGenerator.FreeTypeFontParameter freeTypeFontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
            freeTypeFontParameter.size = i;
            BitmapFont bitmapFont = freeTypeFontGenerator.generateFont(freeTypeFontParameter);
            orderedFontMap.put(i, bitmapFont);
        }
//        for (RegisteredFont value : RegisteredFont.values()) {
//            Array<BitmapFont> bitmapFonts = new Array<>();
//            BitmapFont font = skin.getFont(value.fontName);
//            bitmapFonts.add(font);
//            int i = 0;
//            while(true) {
//                String fontName = value.fontName + "-" + ++i;
//                if (!skin.has(fontName, BitmapFont.class)) {
//                    break;
//                }
//
//                bitmapFonts.add(skin.getFont(fontName));
//            }
//
//            registeredFonts.put(value, bitmapFonts);
//        }

//        orderedFontMap.put(15, skin.getFont("default-font"));
//        orderedFontMap.put(12, skin.getFont("small-font"));
//        orderedFontMap.put(14, skin.getFont("generated-font-14"));
//        orderedFontMap.put(13, skin.getFont("generated-font-13"));
//        orderedFontMap.put(11, skin.getFont("generated-font-11"));
//        orderedFontMap.put(10, skin.getFont("generated-font-10"));
//        orderedFontMap.put(9, skin.getFont("generated-font-9"));
//        orderedFontMap.put(8, skin.getFont("generated-font-8"));
//        orderedFontMap.put(7, skin.getFont("generated-font-7"));
//        orderedFontMap.put(6, skin.getFont("generated-font-6"));
//        orderedFontMap.put(5, skin.getFont("generated-font-5"));
//        orderedFontMap.put(4, skin.getFont("generated-font-4"));

        for (BitmapFont value : orderedFontMap.values()) {
            value.setUseIntegerPositions(true);
        }
    }


    public static void invalidateForDepth(Group group, int depth) {
        if (depth <= 0) return;
        if(group.getParent() == null || !(group.getParent() instanceof Layout)) return;
        Layout parent = (Layout) group.getParent();
        if (parent != null) {
            parent.invalidate();
            invalidateForDepth(group.getParent(), depth - 1);
        }
    }

    public static Table makeSeparator() {
        Table table = new Table();

        table.setBackground(SharedResources.skin.getDrawable("white"));
        table.setColor(Color.valueOf("444444ff"));

        return table;
    }
}
