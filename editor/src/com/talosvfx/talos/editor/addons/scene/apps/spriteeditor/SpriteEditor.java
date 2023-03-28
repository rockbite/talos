package com.talosvfx.talos.editor.addons.scene.apps.spriteeditor;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Scaling;
import com.talosvfx.talos.editor.addons.scene.apps.spriteeditor.widgets.VerticalIconMenu;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.widgets.ui.common.ColorLibrary;

public class SpriteEditor extends Table {
    private final VerticalIconMenu<Actor, SpriteEditorWindow> editorMenu;
    private final Table container;
    private final Cell contentCell;

    private SpriteEditorWindowMenuTab currentTab;

    public SpriteEditor() {
        editorMenu = new VerticalIconMenu<>();

        final SpriteEditorWindowMenuTab propertiesTab = new SpriteEditorWindowMenuTab("ic-menu-image-settings");
        final SpriteEditorWindowMenuTab ninePatchTab = new SpriteEditorWindowMenuTab("ic-menu-ninepatch");

        editorMenu.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (actor instanceof SpriteEditorWindowMenuTab) {
                    setCurrentTab((SpriteEditorWindowMenuTab) actor);
                }
            }
        });

        container = new Table();
        container.setBackground(ColorLibrary.obtainBackground(ColorLibrary.SHAPE_SQUARE, ColorLibrary.BackgroundColor.SUPER_DARK_GRAY));
        contentCell = container.add().grow();

        add(editorMenu).width(35).padTop(5).growY();
        add(container).grow();

        // populate windows
        editorMenu.addTab(propertiesTab, new SpritePropertiesEditorWindow(this));
        editorMenu.addTab(ninePatchTab, new NinepatchEditingWindow(this));

        // set first opened tab the properties tab
        setCurrentTab(propertiesTab);
    }

    private void setCurrentTab (SpriteEditorWindowMenuTab tab) {
        if (currentTab != null) currentTab.spriteEditorTab.setChecked(false);
        currentTab = tab;
        currentTab.spriteEditorTab.setChecked(true);
        contentCell.setActor(editorMenu.getWindow(currentTab));
    }

    public void updateForGameAsset (GameAsset<AtlasRegion> gameAsset) {
        for (SpriteEditorWindow spriteEditorWindow : editorMenu.getTabWindowMap().values()) {
            spriteEditorWindow.updateForGameAsset(gameAsset);
        }
    }

    public void setScrollFocus() {
        editorMenu.getWindow(currentTab).setScrollFocus();
    }

    public class SpriteEditorWindowMenuTab extends Table {
        private final Button spriteEditorTab;
        public SpriteEditorWindowMenuTab(String iconName) {
            spriteEditorTab = new Button(SharedResources.skin);

            final Button.ButtonStyle buttonStyle = new Button.ButtonStyle(SharedResources.skin.get(Button.ButtonStyle.class));
            buttonStyle.up = ColorLibrary.obtainBackground(ColorLibrary.SHAPE_SQUIRCLE_LEFT_2, ColorLibrary.BackgroundColor.ULTRA_DARK_GRAY);
            buttonStyle.over = ColorLibrary.obtainBackground(ColorLibrary.SHAPE_SQUIRCLE_LEFT_2, ColorLibrary.BackgroundColor.DARK_GRAY);
            buttonStyle.checked = ColorLibrary.obtainBackground(ColorLibrary.SHAPE_SQUIRCLE_LEFT_2, ColorLibrary.BackgroundColor.SUPER_DARK_GRAY);

            spriteEditorTab.setStyle(buttonStyle);

            final Table iconWrapper = new Table();
            final Image icon = new Image(SharedResources.skin.getDrawable(iconName), Scaling.fit);
            iconWrapper.add(icon).grow().pad(4);
            iconWrapper.setTouchable(Touchable.disabled);
            stack(spriteEditorTab, iconWrapper).grow().padLeft(10);
        }
    }
}
