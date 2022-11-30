package com.talosvfx.talos.editor.addons.scene.apps.tween;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.scene.SceneEditorAddon;
import com.talosvfx.talos.editor.addons.scene.utils.scriptProperties.PropertyWrapper;
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
        textValueWidget.setValue("");
        add(textValueWidget);

        typeLabel = new LabelWidget();
        typeLabel.init(skin);
        typeLabel.set("Type", Align.center);
        add(typeLabel);

        String[] options = new String[]{"Integer", "Float", "Boolean", "String"};
        typeSelectWidget = new SelectWidget();
        typeSelectWidget.init(skin);
        typeSelectWidget.setOptions(options);
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

    @Override
    public void act (float delta) {
        super.act(delta);
        if (isOver) {
            CursorUtil.setDynamicModeCursor(CursorUtil.CursorType.GRABBED);
        }
    }
}
