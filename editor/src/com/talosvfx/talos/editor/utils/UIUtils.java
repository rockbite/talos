package com.talosvfx.talos.editor.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
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
        for (int i = 4; i < 50; i++) {
            FreeTypeFontGenerator.FreeTypeFontParameter freeTypeFontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
            freeTypeFontParameter.size = i;
            freeTypeFontParameter.minFilter = Texture.TextureFilter.MipMapLinearLinear;
            freeTypeFontParameter.magFilter = Texture.TextureFilter.Linear;
            freeTypeFontParameter.genMipMaps = true;
            BitmapFont bitmapFont = freeTypeFontGenerator.generateFont(freeTypeFontParameter);
            orderedFontMap.put(i, bitmapFont);
        }

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
