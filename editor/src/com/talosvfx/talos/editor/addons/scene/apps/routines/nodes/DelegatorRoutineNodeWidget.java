package com.talosvfx.talos.editor.addons.scene.apps.routines.nodes;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.talosvfx.talos.runtime.routine.RoutineInstance;
import com.talosvfx.talos.editor.addons.scene.assets.AssetRepository;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.runtime.assets.GameAssetType;
import com.talosvfx.talos.editor.data.RoutineStageData;
import com.talosvfx.talos.editor.nodes.widgets.*;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.events.dynamicnodestage.NodeDataModifiedEvent;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.runtime.routine.nodes.RoutineExecutorNode;
import com.talosvfx.talos.runtime.scene.utils.propertyWrappers.PropertyGameAssetWrapper;
import com.talosvfx.talos.runtime.scene.utils.propertyWrappers.PropertyType;
import com.talosvfx.talos.runtime.scene.utils.propertyWrappers.PropertyWrapper;

import java.util.Locale;

public class DelegatorRoutineNodeWidget extends RoutineNodeWidget {

    private final ObjectMap<PropertyType, Class<? extends AbstractWidget>> customTypeMap = new ObjectMap<>();

    private GameAsset asset;

    public DelegatorRoutineNodeWidget() {
        customTypeMap.put(PropertyType.FLOAT, ValueWidget.class);
        customTypeMap.put(PropertyType.BOOLEAN, CheckBoxWidget.class);
        customTypeMap.put(PropertyType.COLOR, ColorWidget.class);
        customTypeMap.put(PropertyType.ASSET, GameAssetWidget.class);
        customTypeMap.put(PropertyType.VECTOR2, Vector2Widget.class);
    }

    @Override
    public void constructNode(XmlReader.Element module) {
        super.constructNode(module);

        Table container = getCustomContainer("customParams");

        GameAssetWidget assetSelector = (GameAssetWidget)widgetMap.get("asset");
        assetSelector.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                buildContainer(container, assetSelector.getValue());
            }
        });
        GameAsset value = assetSelector.getValue();

        if(value != null) {
            buildContainer(container, value);
        }
    }

    @Override
    public void read(Json json, JsonValue jsonValue) {
        if(asset == null) {
            Table container = getCustomContainer("customParams");
            JsonValue props = jsonValue.get("properties");
            if(props.has("asset") && props.get("asset").has("id")) {
                String id = props.get("asset").getString("id");
                GameAsset value = AssetRepository.getInstance().getAssetForIdentifier(id, GameAssetType.ROUTINE);
                if (value != null) {
                    buildContainer(container, value);
                }
            }

        }

        super.read(json, jsonValue);
    }

    private void buildContainer(Table container, GameAsset<RoutineStageData> routineAsset) {
        if(asset != routineAsset) {
            asset = routineAsset;

            Array<String> titles = new Array<>();
            if (!routineAsset.isBroken()) {
                RoutineStageData resource = routineAsset.getResource();
                RoutineInstance instance = resource.createInstance(true);
                Array<RoutineExecutorNode> executors = instance.getNodesByClass(RoutineExecutorNode.class);
                for (RoutineExecutorNode executor : executors) {
                    String title = executor.getTitle();
                    titles.add(title);
                }

                Array<PropertyWrapper<?>> parentPropertyWrappers = instance.getParentPropertyWrappers();
                for (PropertyWrapper<?> wrapper : parentPropertyWrappers) {
                    PropertyType type = wrapper.getType();
                    Class<? extends AbstractWidget> clazz = customTypeMap.get(type);
                    if (clazz != null) {
                        try {
                            AbstractWidget widget = ClassReflection.newInstance(clazz);

                            // todo: this needs to be done better
                            if(widget instanceof GameAssetWidget) {
                                ((GameAssetWidget) widget).build(((PropertyGameAssetWrapper)wrapper).getGameAssetType().toString(), wrapper.propertyName);
                            }

                            widget.init(SharedResources.skin);


                            if(widget instanceof ValueWidget) {
                                ((ValueWidget) widget).setLabel(wrapper.propertyName);
                            }

                            widgetMap.put(wrapper.propertyName, widget);
                            typeMap.put(wrapper.propertyName, type.toString().toLowerCase(Locale.ROOT));
                            defaultsMap.put(wrapper.propertyName, "");
                            addConnection(widget, wrapper.propertyName, true);
                            container.add(widget).growX().padTop(10).row();

                            widget.addListener(new ChangeListener() {
                                @Override
                                public void changed (ChangeEvent changeEvent, Actor actor) {
                                    reportNodeDataModified();
                                }
                            });

                        } catch (Exception e) {

                        }
                    }
                }
            }

            container.clear();

            // let's find all executors and expose them here

            SelectWidget executorSelector = new SelectWidget();
            executorSelector.init(SharedResources.skin);
            executorSelector.setOptions(titles);
            String selectorName = "executorName";
            widgetMap.put(selectorName, executorSelector);
            typeMap.put(selectorName, "string"); // ? is this right?
            defaultsMap.put(selectorName, "");
            container.add(executorSelector).growX().padTop(-10).row();

            executorSelector.addListener(new ChangeListener() {
                @Override
                public void changed (ChangeEvent changeEvent, Actor actor) {
                    reportNodeDataModified();
                }
            });




            Notifications.fireEvent(Notifications.obtainEvent(NodeDataModifiedEvent.class).set(nodeBoard.getNodeStage(), this));
        }
    }

    @Override
    public void write(Json json) {
        super.write(json);
    }
}
