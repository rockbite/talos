package com.talosvfx.talos.editor.addons.scene.widgets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.talosvfx.talos.TalosMain;

public class DirectoryViewWidget extends Table {

    private Array<Actor> items = new Array<>();
    private FileHandle fileHandle;
    private int colCount = 0;
    private float boxWidth = 0;

    public DirectoryViewWidget() {
        build();
    }

    private void build () {
    }

    public void setDirectory(String path) {
        fileHandle = Gdx.files.absolute(path);
        if(fileHandle == null || !fileHandle.exists()) return;

        rebuild();
    }

    private void rebuild () {
        clearChildren();
        items.clear();

        if (fileHandle.isDirectory()) {
            int count = 0;
            for (FileHandle child : fileHandle.list()) {
                ItemView itemView = new ItemView();
                itemView.setFile(child);

                Table box = new Table();
                box.add(itemView).pad(10).expand().left().top().padTop(4);
                items.add(box);
                box.pack();
                boxWidth = box.getWidth();

                colCount = (int) (getWidth() / boxWidth);

                add(box).top().left().expand();
                count++;
                if (count >= colCount) {
                    count = 0;
                    row();
                }
            }
        }
    }

    @Override
    protected void sizeChanged () {
        super.sizeChanged();

        if(boxWidth > 0) {

            int newColCount = (int) (getWidth() / boxWidth);

            if(colCount != newColCount) {
                rebuild();
            }
        }
    }

    public class ItemView extends Table {

        private Image icon;
        private Label label;

        public ItemView() {
            build();
        }

        private void build() {
            Skin skin = TalosMain.Instance().getSkin();
            icon = new Image();
            label = new Label("text", skin);
            label.setEllipsis(true);

            Table iconContainer = new Table();
            iconContainer.add(icon).grow();

            add(iconContainer).size(70).pad(10);
            row();
            add(label).expandX().width(70);
        }

        public void setFile(FileHandle fileHandle) {
            label.setText(fileHandle.name());
            icon.setDrawable(TalosMain.Instance().getSkin().getDrawable("ic-fileset-dir"));

            if(fileHandle.extension().equals("png")) {
                Texture texture = new Texture(fileHandle);
                TextureRegionDrawable drawable = new TextureRegionDrawable(texture);
                icon.setDrawable(drawable);
                icon.setScaling(Scaling.fit);
            }
        }
    }
}


