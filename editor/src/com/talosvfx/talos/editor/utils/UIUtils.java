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

    static private FreeTypeFontGenerator freeTypeFontGenerator;

    private static OrderedMap<Integer, BitmapFont> orderedFontMap = new OrderedMap<>();

    public static BitmapFont getFontForSize (int fontSize) {
        if (orderedFontMap.containsKey(fontSize)) {
            return orderedFontMap.get(fontSize);
        }

        if (freeTypeFontGenerator == null) {
            freeTypeFontGenerator = new FreeTypeFontGenerator(Gdx.files.local("skin/VisOpenSans.ttf"));
        }

        FreeTypeFontGenerator.FreeTypeFontParameter freeTypeFontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        freeTypeFontParameter.size = fontSize;
        freeTypeFontParameter.minFilter = Texture.TextureFilter.MipMapLinearLinear;
        freeTypeFontParameter.magFilter = Texture.TextureFilter.Linear;
        freeTypeFontParameter.genMipMaps = true;
        BitmapFont bitmapFont = freeTypeFontGenerator.generateFont(freeTypeFontParameter);
        bitmapFont.setUseIntegerPositions(false);
        orderedFontMap.put(fontSize, bitmapFont);

        return bitmapFont;
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
