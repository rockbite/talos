package com.talosvfx.talos.editor.addons.scene.logic.components;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.addons.scene.SceneEditorWorkspace;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.widgets.propertyWidgets.IPropertyProvider;
import com.talosvfx.talos.editor.widgets.propertyWidgets.PropertyOption;
import com.talosvfx.talos.editor.widgets.propertyWidgets.PropertyOptionType;

public abstract class AComponent implements IPropertyProvider {

    private transient GameObject gameObject;

    public void setGameObject(GameObject gameObject){
        this.gameObject = gameObject;
    }

    @Override
    public Class<? extends IPropertyProvider> getType () {
        return getClass();
    }

    public PropertyOptionType[] getOptions() {
        return null;
    }

    public void reset() {}

    public void remove() {
        if(gameObject!=null) {
            gameObject.removeComponent(this);
            SceneEditorWorkspace.getInstance().removeGizmos(gameObject);
            SceneEditorWorkspace.getInstance().initGizmos(gameObject,  SceneEditorWorkspace.getInstance());
        }
    }

    public Array<PropertyOption> getOptionsList(){
        Array<PropertyOption> options = new Array<>();

        for (PropertyOptionType option : getOptions()) {
            options.add(new PropertyOption(option, new ClickListener(){
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    switch (option) {
                        case RESET:
                            reset();
                            break;
                        case REMOVE:
                            remove();
                            break;
                        default:
                            break;
                    }
                }
            }));
        }
        return options;
    }
}
