package com.talosvfx.talos.editor.addons.scene.apps.tween;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.scene.SceneEditorAddon;
import com.talosvfx.talos.editor.addons.scene.utils.scriptProperties.*;
import com.talosvfx.talos.editor.nodes.widgets.LabelWidget;
import com.talosvfx.talos.editor.nodes.widgets.SelectWidget;
import com.talosvfx.talos.editor.nodes.widgets.TextValueWidget;
import com.talosvfx.talos.editor.utils.CursorUtil;

public class VariableTemplateRow<T> extends Table {

    private LabelWidget keyLabel;
    public TextValueWidget textValueWidget;
    private LabelWidget typeLabel;
    private SelectWidget typeSelectWidget;
    private ImageButton deleteButton;

    PropertyWrapper<T> propertyWrapper;

    private boolean isOver = false;

    public VariableTemplateRow (PropertyWrapper<T> propertyWrapper) {
        setTouchable(Touchable.enabled);
        defaults().pad(5);
        Skin skin = TalosMain.Instance().getSkin();
        keyLabel = new LabelWidget();
        keyLabel.init(skin);
        keyLabel.set("Key", Align.center);
        add(keyLabel);

        textValueWidget = new TextValueWidget();
        textValueWidget.init(skin);
        textValueWidget.setValue(propertyWrapper.propertyName);
        textValueWidget.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                int index = propertyWrapper.index;
                SceneEditorAddon.get().routineEditor.changeKeyFor(index, textValueWidget.getValue());
            }
        });
        add(textValueWidget);

        typeLabel = new LabelWidget();
        typeLabel.init(skin);
        typeLabel.set("Type", Align.center);
        add(typeLabel);

        String[] options = new String[]{"Integer", "Float", "Boolean", "String", "Game Asset"};
        typeSelectWidget = new SelectWidget();
        typeSelectWidget.init(skin);
        typeSelectWidget.setOptions(options);
        typeSelectWidget.setValue(getOptionValueForProperty(propertyWrapper));
        typeSelectWidget.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                int index = propertyWrapper.index;
                SceneEditorAddon.get().routineEditor.changeTypeFor(index, typeSelectWidget.getValue());
            }
        });
        add(typeSelectWidget).growX();

        deleteButton = new ImageButton(skin.getDrawable("icon-trash"));
        deleteButton.addListener(new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                int index = propertyWrapper.index;
                SceneEditorAddon.get().routineEditor.deleteParamTemplateWithIndex(index);
            }
        });
        add(deleteButton);

        addListener(new ClickListener() {
            @Override
            public void enter (InputEvent event, float x, float y, int pointer, Actor fromActor) {
                super.enter(event, x, y, pointer, fromActor);
                isOver = true;
            }

            @Override
            public void exit (InputEvent event, float x, float y, int pointer, Actor toActor) {
                super.exit(event, x, y, pointer, toActor);
                isOver = false;
            }
        });

        this.propertyWrapper = propertyWrapper;
    }

    private String getOptionValueForProperty (PropertyWrapper<T> propertyWrapper) {
        if (propertyWrapper instanceof PropertyStringWrapper) {
            return "String";
        } else if (propertyWrapper instanceof PropertyGameObjectWrapper) {
            return "Game Asset";
        } else if (propertyWrapper instanceof PropertyBooleanWrapper) {
            return "Boolean";
        } else if (propertyWrapper instanceof PropertyIntegerWrapper) {
            return "Integer";
        } else if (propertyWrapper instanceof PropertyFloatWrapper) {
            return "Float";
        }

        return null;
    }

    @Override
    public void act (float delta) {
        super.act(delta);
        if (isOver) {
            CursorUtil.setDynamicModeCursor(CursorUtil.CursorType.GRABBED);
        }
    }
}
