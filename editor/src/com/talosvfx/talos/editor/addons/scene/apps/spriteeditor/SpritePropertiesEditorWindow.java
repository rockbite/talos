package com.talosvfx.talos.editor.addons.scene.apps.spriteeditor;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.talosvfx.talos.editor.addons.scene.assets.AssetRepository;
import com.talosvfx.talos.editor.project2.AppManager;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.widgets.propertyWidgets.FloatPropertyWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.PropertyWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.WidgetFactory;
import com.talosvfx.talos.editor.widgets.ui.common.CollapsableWidget;
import com.talosvfx.talos.editor.widgets.ui.common.SquareButton;

public class SpritePropertiesEditorWindow extends SpriteEditorWindow {
    // size property widgets
    private float width, height;
    private PropertyWidget widthWidget;
    private PropertyWidget heightWidget;

    // color property widget
    public Color color = new Color(Color.WHITE);
    private PropertyWidget colorWidget;

    public SpritePropertiesEditorWindow (SpriteEditor spriteEditor) {
        super(spriteEditor);

        padTop(10).defaults().growX().padLeft(10).padRight(5).space(3);

        final CollapsableWidget sizePanel = initSizePanel();
        final CollapsableWidget colorPanel = initColorPanel();

        add(sizePanel);
        row();
        add(colorPanel);
        row();
        add().expandY();

        // expand size panel by default
        sizePanel.expand();
    }

    private CollapsableWidget initSizePanel () {
        // init size widgets
        widthWidget = WidgetFactory.generate(this, "width", "Width");
        heightWidget = WidgetFactory.generate(this, "height", "Height");

        ((FloatPropertyWidget) widthWidget).configureFromValues(1, 4096, 1);
        ((FloatPropertyWidget) heightWidget).configureFromValues(1, 4096, 1);

        // create panel
        final CollapsableWidget sizePanel = new CollapsableWidget("Size");

        // change into smaller font
        final Label.LabelStyle labelStyle = new Label.LabelStyle(SharedResources.skin.get(Label.LabelStyle.class));
        labelStyle.font= SharedResources.skin.getFont("small-font");
        if (widthWidget.getPropertyName() != null) {
            widthWidget.getPropertyName().setStyle(labelStyle);
        }
        if (heightWidget.getPropertyName() != null) {
            heightWidget.getPropertyName().setStyle(labelStyle);
        }

        // init save button
        final Label buttonLabel = new Label("Save", SharedResources.skin);
        final SquareButton saveButton = new SquareButton(SharedResources.skin, buttonLabel, "Save");
        saveButton.setStyle(new Button.ButtonStyle(saveButton.getStyle()));
        saveButton.getStyle().checked = saveButton.getStyle().up;
        saveButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);

                // TODO: 23.02.23 dummy refactor
                if (gameAsset == null) {
                    return;
                }

                AssetRepository.getInstance().resizeAsset(gameAsset, (int) width, (int) height);
            }
        });

        // assemble panel
        sizePanel.getContent().padLeft(40).defaults().space(3);
        sizePanel.getContent().add(widthWidget).growX();
        sizePanel.getContent().row();
        sizePanel.getContent().add(heightWidget).growX();
        sizePanel.getContent().row();
        sizePanel.getContent().add(saveButton).expandX().left().padTop(5);
        return sizePanel;
    }

    private CollapsableWidget initColorPanel () {
        // init panel
        final CollapsableWidget colorPanel = new CollapsableWidget("Color");
        colorWidget = WidgetFactory.generate(this, "color", "Fill Color");

        // init save button
        final Label buttonLabel = new Label("Save", SharedResources.skin);
        final SquareButton saveButton = new SquareButton(SharedResources.skin, buttonLabel, "Save");
        saveButton.setStyle(new Button.ButtonStyle(saveButton.getStyle()));
        saveButton.getStyle().checked = saveButton.getStyle().up;
        saveButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);

                // TODO: 23.02.23 dummy refactor
                if (gameAsset == null) {
                    return;
                }

                AssetRepository.getInstance().fillAssetColor(gameAsset, color);
            }
        });

        // assemble panel
        colorPanel.getContent().padLeft(40).defaults().space(3);
        colorPanel.getContent().add(colorWidget).growX();
        colorPanel.getContent().row();
        colorPanel.getContent().add(saveButton).expandX().left().padTop(5);

        return colorPanel;
    }

    @Override
    public void updateForGameAsset (GameAsset<Texture> gameAsset) {
        this.gameAsset = gameAsset;

        final Texture texture = gameAsset.getResource();

        widthWidget.valueChanged((float) texture.getTextureData().getWidth());
        heightWidget.valueChanged((float) texture.getTextureData().getHeight());

        widthWidget.updateValue();
        heightWidget.updateValue();
    }
}
