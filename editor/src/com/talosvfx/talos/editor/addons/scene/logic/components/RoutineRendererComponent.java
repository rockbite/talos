package com.talosvfx.talos.editor.addons.scene.logic.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.editor.addons.scene.MainRenderer;
import com.talosvfx.talos.editor.addons.scene.apps.tween.runtime.RoutineConfigMap;
import com.talosvfx.talos.editor.addons.scene.apps.tween.runtime.RoutineInstance;
import com.talosvfx.talos.editor.addons.scene.apps.tween.runtime.RoutineNode;
import com.talosvfx.talos.editor.addons.scene.apps.tween.runtime.draw.DrawableQuad;
import com.talosvfx.talos.editor.addons.scene.apps.tween.runtime.nodes.RenderRoutineNode;
import com.talosvfx.talos.editor.addons.scene.assets.AssetRepository;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.assets.GameAssetType;
import com.talosvfx.talos.editor.addons.scene.events.ComponentUpdated;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.widgets.property.AssetSelectWidget;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.widgets.propertyWidgets.PropertyWidget;

import java.util.function.Supplier;

public class RoutineRendererComponent extends RendererComponent implements Json.Serializable, GameResourceOwner<String> {

    GameAsset<String> routineResource;

    private transient RoutineInstance routineInstance;

    @Override
    public void write(Json json) {
        GameResourceOwner.writeGameAsset(json, this);
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        String gameResourceIdentifier = GameResourceOwner.readGameResourceFromComponent(jsonData);

        GameAsset<String> assetForIdentifier = AssetRepository.getInstance().getAssetForIdentifier(gameResourceIdentifier, GameAssetType.ROUTINE);
        setGameAsset(assetForIdentifier);
    }

    @Override
    public void minMaxBounds(GameObject parentEntity, BoundingBox rectangle) {

    }


    @Override
    public Array<PropertyWidget> getListOfProperties() {
        Array<PropertyWidget> properties = new Array<>();

        AssetSelectWidget<String> widget = new AssetSelectWidget<String>("Routine", GameAssetType.ROUTINE, new Supplier<GameAsset<String>>() {
            @Override
            public GameAsset<String> get () {
                return routineResource;
            }
        }, new PropertyWidget.ValueChanged<GameAsset<String>>() {
            @Override
            public void report (GameAsset<String> value) {
                setGameAsset(value);
            }
        });

        properties.add(widget);

        Array<PropertyWidget> superList = super.getListOfProperties();
        properties.addAll(superList);

        return properties;
    }

    @Override
    public String getPropertyBoxTitle() {
        return "Routine Renderer";
    }

    @Override
    public int getPriority() {
        return 4;
    }

    @Override
    public GameAssetType getGameAssetType() {
        return GameAssetType.ROUTINE;
    }

    @Override
    public GameAsset<String> getGameResource() {
        return routineResource;
    }

    @Override
    public void setGameAsset(GameAsset<String> gameAsset) {
        this.routineResource = gameAsset;

        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run () {
                //Notifications.fireEvent(Notifications.obtainEvent(ComponentUpdated.class).set(RoutineRendererComponent.this, false));
            }
        });

        routineInstance = new RoutineInstance();
        // this needs changing
        RoutineConfigMap routineConfigMap = new RoutineConfigMap();
        routineConfigMap.loadFrom(Gdx.files.internal("addons/scene/tween-nodes.xml")); //todo: totally not okay
        routineInstance.loadFrom(gameAsset.getResource(), routineConfigMap);
    }

    @Override
    public boolean allowsMultipleOfTypeOnGameObject () {
        return false;
    }

    public void render(MainRenderer mainRenderer, Batch batch, GameObject gameObject) {
        // todo this is not nice
        routineInstance.clearMemory();
        RoutineNode main = routineInstance.getNodeById("main");
        if(main != null && main instanceof RenderRoutineNode) {
            RenderRoutineNode renderRoutineNode = (RenderRoutineNode) routineInstance.getNodeById("main");
            renderRoutineNode.receiveSignal("startSignal");

            for (DrawableQuad drawableQuad : routineInstance.drawableQuads) {
                batch.draw(mainRenderer.getWhiteTexture(),
                        drawableQuad.position.x, drawableQuad.position.y,
                        drawableQuad.size.x, drawableQuad.size.y);
            }
        }
    }
}
