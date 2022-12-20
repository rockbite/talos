package com.talosvfx.talos.editor.addons.scene.logic.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.editor.addons.scene.assets.AssetRepository;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.assets.GameAssetType;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.widgets.property.AssetSelectWidget;
import com.talosvfx.talos.editor.nodes.widgets.ValueWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.*;

import java.util.function.Supplier;

public class SpriteRendererComponent extends RendererComponent implements GameResourceOwner<Texture> {

    public transient GameAsset<Texture> defaultGameAsset;
    public GameAsset<Texture> gameAsset;

    public Color color = new Color(Color.WHITE);
    public boolean flipX;
    public boolean flipY;
    public boolean fixAspectRatio = true;
    public RenderMode renderMode = RenderMode.simple;

    @ValueProperty(prefix = {"W", "H"})
    public Vector2 size = new Vector2(1, 1);

    @ValueProperty(prefix = {"W", "H"}, min = 0.05f)
    public Vector2 tileSize = new Vector2(1, 1);

    @Override
    public GameAssetType getGameAssetType () {
        return GameAssetType.SPRITE;
    }

    @Override
    public GameAsset<Texture> getGameResource () {
        return gameAsset;
    }

    @Override
    public void setGameAsset (GameAsset<Texture> newGameAsset) {
        if (this.gameAsset != null) {
            //Remove from old game asset, it might be the same, but it may also have changed
            this.gameAsset.listeners.removeValue(gameAssetUpdateListener, true);
        }

        if(defaultGameAsset == null && !newGameAsset.isBroken()){
            defaultGameAsset = newGameAsset;
        }

        this.gameAsset = newGameAsset;
        this.gameAsset.listeners.add(gameAssetUpdateListener);

        gameAssetUpdateListener.onUpdate();

    }

    public enum RenderMode {
        simple,
        sliced,
        tiled
    }

    @Override
    public Class<? extends IPropertyProvider> getType() {
        return getClass();
    }

    @Override
    public Array<PropertyWidget> getListOfProperties () {
        Array<PropertyWidget> properties = new Array<>();

        AssetSelectWidget<Texture> textureWidget = new AssetSelectWidget<>("Texture", GameAssetType.SPRITE, new Supplier<GameAsset<Texture>>() {
            @Override
            public GameAsset<Texture> get() {
                return gameAsset;
            }
        }, new PropertyWidget.ValueChanged<GameAsset<Texture>>() {
            @Override
            public void report(GameAsset<Texture> value) {
                setGameAsset(value);
            }
        });

        PropertyWidget colorWidget = WidgetFactory.generate(this, "color", "Color");
        PropertyWidget flipXWidget = WidgetFactory.generate(this, "flipX", "Flip X");
        PropertyWidget flipYWidget = WidgetFactory.generate(this, "flipY", "Flip Y");
        PropertyWidget fixAspectRatioWidget = WidgetFactory.generate(this, "fixAspectRatio", "Fix Aspect Ratio");
        PropertyWidget renderModesWidget = WidgetFactory.generate(this, "renderMode", "Render Mode");
        PropertyWidget sizeWidget = WidgetFactory.generate(this, "size", "Size");
        PropertyWidget tileSizeWidget = WidgetFactory.generate(this, "tileSize", "Tile Size");

        renderModesWidget.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                if (renderMode == RenderMode.tiled) {
                    tileSizeWidget.setVisible(true);
                } else {
                    tileSizeWidget.setVisible(false);
                }
            }
        });

        // snap to aspect ratio
        fixAspectRatioWidget.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (!fixAspectRatio) return;;

                final Texture texture = getGameResource().getResource();

                if (texture != null) {
                    final float aspect = texture.getHeight() * 1f / texture.getWidth();
                    size.y = size.x * aspect;
                }

                final ValueWidget yValue = ((Vector2PropertyWidget) sizeWidget).yValue;
                yValue.setValue(size.y, false);

            }
        });

        // change size by aspect ratio if aspect ratio is fixed
        sizeWidget.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (!fixAspectRatio) return;

                if (event.getTarget() instanceof ValueWidget) {
                    final Vector2PropertyWidget vector2PropertyWidget = ((Vector2PropertyWidget) sizeWidget);
                    final ValueWidget xValue = vector2PropertyWidget.xValue;
                    final ValueWidget yValue = vector2PropertyWidget.yValue;
                    final Texture texture = getGameResource().getResource();

                    if (texture != null) {
                        final float aspect = texture.getHeight() * 1f / texture.getWidth();

                        if (event.getTarget() == xValue) {
                            size.y = size.x * aspect;
                        }

                        if (event.getTarget() == yValue) {
                            size.x = size.y / aspect;
                        }
                    }

                    xValue.setValue(size.x, false);
                    yValue.setValue(size.y, false);
                }
            }
        });

        properties.add(textureWidget);
        properties.add(colorWidget);
        properties.add(fixAspectRatioWidget);
        properties.add(flipXWidget);
        properties.add(flipYWidget);
        properties.add(renderModesWidget);

        Array<PropertyWidget> superList = super.getListOfProperties();
        properties.addAll(superList);
        properties.add(sizeWidget);
        properties.add(tileSizeWidget);

        return properties;
    }

    @Override
    public String getPropertyBoxTitle () {
        return "Sprite Renderer";
    }

    @Override
    public int getPriority () {
        return 2;
    }

    transient GameAsset.GameAssetUpdateListener gameAssetUpdateListener = new GameAsset.GameAssetUpdateListener() {
        @Override
        public void onUpdate () {
            if (gameAsset.isBroken()) {
            } else {
            }
        }
    };

    private void loadTextureFromIdentifier (String gameResourceIdentifier) {
        GameAsset<Texture> assetForIdentifier = AssetRepository.getInstance().getAssetForIdentifier(gameResourceIdentifier, GameAssetType.SPRITE);
        setGameAsset(assetForIdentifier);
    }

    @Override
    public void write (Json json) {
        GameResourceOwner.writeGameAsset(json, this);

        json.writeValue("color", color);
        json.writeValue("flipX", flipX);
        json.writeValue("flipY", flipY);
        json.writeValue("fixAspectRatio", fixAspectRatio);
        json.writeValue("renderMode", renderMode);
        json.writeValue("size", size);
        json.writeValue("tileSize", tileSize);

        super.write(json);

    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        String gameResourceIdentifier = GameResourceOwner.readGameResourceFromComponent(jsonData);

        loadTextureFromIdentifier(gameResourceIdentifier);

        color = json.readValue(Color.class, jsonData.get("color"));
        if(color == null) color = new Color(Color.WHITE);

        flipX = jsonData.getBoolean("flipX", false);
        flipY = jsonData.getBoolean("flipY", false);
        fixAspectRatio = jsonData.getBoolean("fixAspectRatio", fixAspectRatio);
        renderMode = json.readValue(RenderMode.class, jsonData.get("renderMode"));
        JsonValue size = jsonData.get("size");
        if (size != null) {
            this.size = json.readValue(Vector2.class, size);
        }
        JsonValue tileSize = jsonData.get("tileSize");
        if (tileSize != null) {
            this.tileSize = json.readValue(Vector2.class, tileSize);
        }

        if(renderMode == null) renderMode = RenderMode.simple;

        super.read(json, jsonData);
    }

    Vector2 vec = new Vector2();
    @Override
    public void minMaxBounds (GameObject ownerEntity, BoundingBox boundingBox) {
        TransformComponent transformComponent = ownerEntity.getComponent(TransformComponent.class);
        if (transformComponent != null) {
            vec.set(0, 0);
            transformComponent.localToWorld(ownerEntity, vec);

            float width = transformComponent.scale.x * size.x;
            float height = transformComponent.scale.y * size.y;

            boundingBox.ext(-width/2, -height/2, 0);
            boundingBox.ext(+width/2, +height/2, 0);
        }
    }

    @Override
    public void reset() {
        super.reset();
        size.set(1, 1);
        color.set(Color.WHITE);
        flipX = false;
        flipY = false;
        fixAspectRatio = true;
        renderMode = RenderMode.simple;
        if (defaultGameAsset != null) {
            setGameAsset(defaultGameAsset);
        }
    }
}
