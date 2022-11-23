package com.talosvfx.talos.editor.addons.scene.logic.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.editor.addons.scene.MainRenderer;
import com.talosvfx.talos.editor.addons.scene.SceneEditorAddon;
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
import com.talosvfx.talos.editor.widgets.propertyWidgets.ValueProperty;
import com.talosvfx.talos.editor.widgets.propertyWidgets.WidgetFactory;

import javax.swing.border.AbstractBorder;
import java.util.Comparator;
import java.util.function.Supplier;

public class RoutineRendererComponent extends RendererComponent implements Json.Serializable, GameResourceOwner<String> {

    GameAsset<String> routineResource;

    @ValueProperty(prefix = {"W", "H"})
    public Vector2 viewportSize = new Vector2(6, 4);


    public transient RoutineInstance routineInstance;
    private transient TextureRegion textureRegion = new TextureRegion();
    private transient float renderCoolDown  = 0f;
    private Comparator<? super DrawableQuad> zComparator;

    public RoutineRendererComponent() {
        zComparator = new Comparator<DrawableQuad>() {
            @Override
            public int compare(DrawableQuad o2, DrawableQuad o1) {
                int result = (int) (o1.z*10000 - o2.z*10000);
                if(result == 0) {
                    result = (int) (o1.position.x*10000 - o2.position.x*10000);
                }
                if(result == 0) {
                    result = (int) (o1.position.y*10000 - o2.position.y*10000);
                }
                return result;
            }
        };
    }

    @Override
    public void write(Json json) {
        super.write(json);
        GameResourceOwner.writeGameAsset(json, this);
        json.writeValue("size", viewportSize, Vector2.class);
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        String gameResourceIdentifier = GameResourceOwner.readGameResourceFromComponent(jsonData);

        GameAsset<String> assetForIdentifier = AssetRepository.getInstance().getAssetForIdentifier(gameResourceIdentifier, GameAssetType.ROUTINE);
        setGameAsset(assetForIdentifier);

        viewportSize = json.readValue(Vector2.class, jsonData.get("size"));
        if(viewportSize == null) viewportSize = new Vector2(6, 4);
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

        PropertyWidget sizeWidget = WidgetFactory.generate(this, "viewportSize", "Viewport");
        properties.add(sizeWidget);

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

        TransformComponent transform = gameObject.getComponent(TransformComponent.class);

        renderCoolDown -= Gdx.graphics.getDeltaTime();

        boolean reset = false;

        if(renderCoolDown <= 0f) {
            renderCoolDown = 1f;
            reset = true;
        }

        // todo this is not nice
        if(reset) {
            routineInstance.clearMemory();
        }
        RoutineNode main = routineInstance.getNodeById("main");
        if(main instanceof RenderRoutineNode) {
            RenderRoutineNode renderRoutineNode = (RenderRoutineNode) routineInstance.getNodeById("main");

            Vector3 cameraPos = mainRenderer.getCamera().position;
            renderRoutineNode.position.set(transform.position);
            renderRoutineNode.viewportPosition.set(cameraPos.x, cameraPos.y);
            renderRoutineNode.viewportSize.set(viewportSize.x, viewportSize.y);

            if(reset) {
                renderRoutineNode.receiveSignal("startSignal");
            }

            routineInstance.drawableQuads.sort(zComparator);

            for (DrawableQuad drawableQuad : routineInstance.drawableQuads) {
                boolean aspect = drawableQuad.aspect;
                float scl = (float)drawableQuad.texture.getWidth() / drawableQuad.texture.getHeight();
                float width = drawableQuad.size.x;
                float height = drawableQuad.size.y;
                if(aspect) {
                    height = width / scl;
                }

                textureRegion.setRegion(drawableQuad.texture);
                batch.setColor(drawableQuad.color);
                batch.draw(textureRegion,
                        drawableQuad.position.x, drawableQuad.position.y,
                        0.5f, 0.5f,
                        1f, 1f,
                        width, height,
                        drawableQuad.rotation);
                batch.setColor(Color.WHITE);
            }
        }
    }
}
