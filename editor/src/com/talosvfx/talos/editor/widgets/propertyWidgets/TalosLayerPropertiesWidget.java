package com.talosvfx.talos.editor.widgets.propertyWidgets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.spine.Bone;
import com.kotcrab.vis.ui.widget.tabbedpane.Tab;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.bvb.AttachmentPoint;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.assets.GameAssetType;
import com.talosvfx.talos.editor.addons.scene.maps.LayerType;
import com.talosvfx.talos.editor.addons.scene.maps.TalosLayer;
import com.talosvfx.talos.editor.addons.scene.widgets.property.AssetSelectWidget;

import java.util.function.Supplier;

public class TalosLayerPropertiesWidget extends PropertyWidget<TalosLayer> {

    Table subWidgetTable;

    public TalosLayerPropertiesWidget (String name, Supplier<TalosLayer> supplier, ValueChanged<TalosLayer> valueChanged) {
        super(name, supplier, valueChanged);
    }

    @Override
    public void updateWidget (TalosLayer value) {
        //Update it from the existing shit

        subWidgetTable.clearChildren();

        Array<PropertyWidget<?>> widgets = new Array<>();
        if (value != null) {
            PropertyWidget typeWidget = WidgetFactory.generate(value, "type", "Type");
            typeWidget.updateValue(); //kind of a hack to do this /shrug face
            widgets.add(typeWidget);

            typeWidget.addListener(new ChangeListener() {
                @Override
                public void changed (ChangeEvent event, Actor actor) {
                    Gdx.app.postRunnable(new Runnable() {
                        @Override
                        public void run () {
                            updateWidget(value);
                        }
                    });
                }
            });


            //Static properties only

            if (value.getType() == LayerType.STATIC) {

                PropertyWidget mapWidthX = WidgetFactory.generate(value, "mapWidth", "MapWidth");
                mapWidthX.updateValue(); //kind of a hack to do this /shrug face
                widgets.add(mapWidthX);

                PropertyWidget mapWidthY = WidgetFactory.generate(value, "mapHeight", "MapHeight");
                mapWidthY.updateValue(); //kind of a hack to do this /shrug face
                widgets.add(mapWidthY);

                PropertyWidget tileSizeX = WidgetFactory.generate(value, "tileSizeX", "TileSize X");
                tileSizeX.updateValue(); //kind of a hack to do this /shrug face
                widgets.add(tileSizeX);

                PropertyWidget tileSizeY = WidgetFactory.generate(value, "tileSizeY", "TileSize Y");
                tileSizeY.updateValue(); //kind of a hack to do this /shrug face
                widgets.add(tileSizeY);
            }

        }

        for (PropertyWidget<?> widget : widgets) {
            subWidgetTable.add(widget).row();;
        }

    }


    @Override
    public Actor getSubWidget () {
        subWidgetTable = new Table();
        subWidgetTable.defaults().growX().padTop(4);


        return subWidgetTable;
    }

    @Override
    public PropertyWidget clone() {
        TalosLayerPropertiesWidget clone = (TalosLayerPropertiesWidget) super.clone();

        return clone;
    }



}
