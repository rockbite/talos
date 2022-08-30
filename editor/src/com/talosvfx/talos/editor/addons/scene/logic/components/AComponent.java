package com.talosvfx.talos.editor.addons.scene.logic.components;

import com.talosvfx.talos.editor.addons.scene.SceneEditorWorkspace;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.widgets.propertyWidgets.IPropertyProvider;

public abstract class AComponent implements IPropertyProvider {
    private transient GameObject gameObject;

    public void setGameObject(GameObject gameObject){
        this.gameObject = gameObject;
    }

    public GameObject getGameObject(){
        return  gameObject;
    }

    @Override
    public Class<? extends IPropertyProvider> getType () {
        return getClass();
    }

    public void reset() {}

    public void remove() {
        if(gameObject!=null) {
            gameObject.removeComponent(this);
        }
    }

    public boolean allowsMultipleOfTypeOnGameObject () {
        return false;
    }

}
