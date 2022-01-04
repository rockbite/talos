package com.talosvfx.talos.editor.addons.scene.logic;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.talosvfx.talos.editor.widgets.propertyWidgets.ColorPropertyWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.EditableLabelWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.IPropertyProvider;
import com.talosvfx.talos.editor.widgets.propertyWidgets.PropertyWidget;
import com.talosvfx.talos.editor.widgets.ui.EditableLabel;

public class MultiPropertyHolder implements IPropertyHolder {

    Array<IPropertyHolder> holderArray = new Array<>();
    private ObjectMap<Class<? extends IPropertyProvider>, MultiPropertyProvider>  mainMap;

    public MultiPropertyHolder(Array<IPropertyHolder> holderArray) {
        this.holderArray.addAll(holderArray);

        generateLists();
    }

    private void generateLists() {
        mainMap = new ObjectMap<>();

        Array<Class<? extends IPropertyProvider>> allowList = new Array<>();
        Iterable<IPropertyProvider> firstProviders = holderArray.first().getPropertyProviders();
        for(IPropertyProvider provider: firstProviders) {
            allowList.add(provider.getClass());
        }

        for(IPropertyHolder holder: holderArray) {
            Iterable<IPropertyProvider> propertyProviders = holder.getPropertyProviders();
            for (IPropertyProvider provider : propertyProviders) {
                if (allowList.contains(provider.getClass(), true)) {
                    if (mainMap.get(provider.getClass()) == null) {
                        mainMap.put(provider.getClass(), new MultiPropertyProvider());
                    }
                    mainMap.get(provider.getClass()).addProvider(provider);
                }
            }
        }

        for(MultiPropertyProvider provider : mainMap.values()) {
            provider.initWidgets();
        }
    }

    @Override
    public Iterable<IPropertyProvider> getPropertyProviders() {
        Array<IPropertyProvider> list = new Array<>();
        for(IPropertyProvider provider : mainMap.values()) {
            list.add(provider);
        }
        return list;
    }

    public static class MultiPropertyProvider implements IPropertyProvider {

        private Array<IPropertyProvider> providers = new Array<>();
        private ObjectMap<Integer, Array<PropertyWidget>> map = new ObjectMap<>();

        public void initWidgets() {

            for(IPropertyProvider provider: providers) {
                Array<PropertyWidget> properties = provider.getListOfProperties();
                for(int i = 0; i < properties.size;  i++) {
                    PropertyWidget childWidget = properties.get(i);
                    if(map.get(i) == null) {
                        map.put(i, new Array<>());
                    }
                    map.get(i).add(childWidget);



                }


                /*
                ColorPropertyWidget widget = new ColorPropertyWidget("qaq") {
                    @Override
                    public Color getValue() {
                        return providers.first().getListOfProperties();
                    }

                    @Override
                    public void valueChanged(Color value) {
                        super.valueChanged(value);
                    }
                };*/
            }
        }

        public void addProvider(IPropertyProvider provider) {
            providers.add(provider);
        }

        @Override
        public Array<PropertyWidget> getListOfProperties() {



            return null;
        }

        @Override
        public String getPropertyBoxTitle() {
            return providers.first().getPropertyBoxTitle();
        }

        @Override
        public int getPriority() {
            return providers.first().getPriority();
        }
    }
}
