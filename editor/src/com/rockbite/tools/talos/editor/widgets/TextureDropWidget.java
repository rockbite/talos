package com.rockbite.tools.talos.editor.widgets;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class TextureDropWidget extends Table {

    Image image;

    public TextureDropWidget(TextureRegion region, Skin skin) {

        Stack stack = new Stack();

        image = new Image(region);
        Image border = new Image(skin.getDrawable("border"));

        stack.add(image);
        stack.add(border);

        add(stack).size(50);

    }

    public void setDrawable(TextureRegionDrawable textureRegionDrawable) {
        image.setDrawable(textureRegionDrawable);
    }
}
