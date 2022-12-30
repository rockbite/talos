package com.talosvfx.talos.editor.addons.scene.apps.routines.nodes;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;
import com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.RoutineInstance;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.utils.propertyWrappers.PropertyWrapper;
import com.talosvfx.talos.editor.data.RoutineStageData;
import com.talosvfx.talos.editor.nodes.widgets.AbstractWidget;
import com.talosvfx.talos.editor.nodes.widgets.GameAssetWidget;
import com.talosvfx.talos.editor.nodes.widgets.ValueWidget;
import com.talosvfx.talos.editor.project2.SharedResources;

public class DelegatorRoutineNodeWidget extends RoutineNodeWidget {

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

    private void buildContainer(Table container, GameAsset<RoutineStageData> routineAsset) {
        RoutineStageData resource = routineAsset.getResource();
        RoutineInstance instance = resource.createInstance(true);

        Array<PropertyWrapper<?>> parentPropertyWrappers = instance.getParentPropertyWrappers();

        container.clear();

        for (PropertyWrapper<?> wrapper : parentPropertyWrappers) {
            ValueWidget test = new ValueWidget();
            test.init(SharedResources.skin);
            widgetMap.put(wrapper.propertyName, test);
            typeMap.put(wrapper.propertyName, "string");
            defaultsMap.put(wrapper.propertyName, "linear");

            addConnection(test, wrapper.propertyName, true);

            container.add(test).growX().row();
        }
    }
}
