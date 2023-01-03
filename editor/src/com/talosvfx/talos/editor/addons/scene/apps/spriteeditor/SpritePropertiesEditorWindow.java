package com.talosvfx.talos.editor.addons.scene.apps.spriteeditor;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.talosvfx.talos.editor.addons.scene.assets.AssetRepository;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.widgets.propertyWidgets.FloatPropertyWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.PropertyWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.WidgetFactory;
import com.talosvfx.talos.editor.widgets.ui.common.CollapsableWidget;

public class SpritePropertiesEditorWindow extends SpriteEditorWindow {
    // property widgets
    private float width, height;
    private PropertyWidget widthWidget;
    private PropertyWidget heightWidget;

    public SpritePropertiesEditorWindow (SpriteEditor spriteEditor) {
        super(spriteEditor);

        padTop(10).defaults().growX().padLeft(10).padRight(5).space(3);

        final CollapsableWidget sizePanel = initSizePanel();

        add(sizePanel);
        row();
        add().expandY();

        // expand size panel by default
        sizePanel.expand();
    }

    private CollapsableWidget initSizePanel () {
        // init size widgets
        widthWidget = WidgetFactory.generate(this, "width", "Width");
        heightWidget = WidgetFactory.generate(this, "height", "Height");

        ((FloatPropertyWidget) widthWidget).configureFromValues(0, Integer.MAX_VALUE, 1);
        ((FloatPropertyWidget) heightWidget).configureFromValues(0, Integer.MAX_VALUE, 1);

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
        final TextButton saveButton = new TextButton("Save", SharedResources.skin);
        saveButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                AssetRepository.getInstance().resizeAsset(gameAsset, (int) width, (int) height);
                gameAsset.setUpdated();
            }
        });

        // assemble panel
        sizePanel.getContent().defaults().space(3);
        sizePanel.getContent().add(widthWidget).expandX().left().padLeft(40);
        sizePanel.getContent().row();
        sizePanel.getContent().add(heightWidget).expandX().left().padLeft(40);
        sizePanel.getContent().row();
        sizePanel.getContent().add(saveButton).expandX().left().padLeft(40);
        return sizePanel;
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
