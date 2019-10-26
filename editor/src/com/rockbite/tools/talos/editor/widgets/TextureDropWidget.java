package com.rockbite.tools.talos.editor.widgets;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.rockbite.tools.talos.runtime.modules.Module;

public class TextureDropWidget<F extends Module> extends Table {

    Image image;

    public TextureDropWidget(TextureRegion region, Skin skin) {
        init(region, skin, 50f);
    }

    public TextureDropWidget(TextureRegion region, Skin skin, float width) {
        init(region, skin, width);
    }

    public void init(TextureRegion region, Skin skin, float width) {

        Stack stack = new Stack();

        image = new Image(region);
        Image border = new Image(skin.getDrawable("border"));

        stack.add(image);
        stack.add(border);

        add(stack).size(width);

    }

    public void setDrawable(TextureRegionDrawable textureRegionDrawable) {
        image.setDrawable(textureRegionDrawable);
    }
}
