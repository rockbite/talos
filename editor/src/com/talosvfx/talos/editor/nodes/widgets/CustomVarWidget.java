package com.talosvfx.talos.editor.nodes.widgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;
import com.badlogic.gdx.utils.*;
import com.talosvfx.talos.editor.addons.scene.SceneEditorWorkspace;
import com.talosvfx.talos.editor.addons.scene.apps.routines.ui.DeleteButton;
import com.talosvfx.talos.editor.addons.scene.apps.routines.ui.types.ATypeWidget;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.utils.CursorUtil;
import com.talosvfx.talos.editor.utils.UIUtils;
import com.talosvfx.talos.editor.widgets.ui.common.ArrowButton;
import com.talosvfx.talos.editor.widgets.ui.common.ColorLibrary;
import com.talosvfx.talos.editor.widgets.ui.common.zoomWidgets.LabelWithZoom;
import com.talosvfx.talos.editor.widgets.ui.common.zoomWidgets.TextFieldWithZoom;
import com.talosvfx.talos.runtime.scene.utils.propertyWrappers.PropertyWrapper;
import lombok.Getter;
import lombok.Setter;

public class CustomVarWidget<T> extends AbstractWidget<T> {

    private final Table editing;
    private final Table main;
    @Getter
    private PropertyWrapper<T> propertyWrapper;

    private Label fieldNameLabel;
    private TextField fieldNameTextField;

    private Stage stageRef;

    private boolean isSelected;
    private boolean isHover;

    private EventListener stageListener;
    private ArrowButton arrowButton;

    private Table top;
    private Table bottom;

    @Getter
    private Table fieldContainer;
    private Cell<Table> contentCell;

    private ATypeWidget<T> innerWidget;
    private Label typeLabel;

    public CustomVarWidget(PropertyWrapper<T> propertyWrapper, ATypeWidget<T> innerWidget) {
        this.propertyWrapper = propertyWrapper;

        editing = new Table();
        main = new Table();

        this.innerWidget = innerWidget;

        init(SharedResources.skin);
    }

    public void init(Skin skin) {
        setSkin(skin);
        isSelected = false;

        top = new Table();
        bottom = new Table();
        fieldContainer = new Table();

        fieldNameLabel = new LabelWithZoom("", skin);
        fieldNameTextField = new TextFieldWithZoom("0", getSkin(), "no-bg");

        typeLabel = new LabelWithZoom(innerWidget.getTypeName(), skin);
        typeLabel.setColor(Color.GRAY);

        Stack mainStack = new Stack();

        editing.add(fieldNameTextField).growX().padLeft(12);

        arrowButton = new ArrowButton();
        top.add(arrowButton).growY().left();

        arrowButton.setCollapsed(propertyWrapper.isCollapsed);
        collapse();

        arrowButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                arrowButton.toggle();

                collapse();

                propertyWrapper.isCollapsed = arrowButton.isCollapsed();
                fireCollapse();

            }
        });

        main.add(fieldNameLabel).padLeft(12).left().width(0).growX().expandX();
        fieldNameLabel.setEllipsis(true);
        fieldNameLabel.setAlignment(Align.left);

        main.add(typeLabel).padRight(6).right().expandX();

        mainStack.add(editing);
        mainStack.add(main);

        // field name, so edit mode won't override it
        setFieldName(propertyWrapper.propertyName);
        hideEditMode();

        fieldContainer.add(mainStack).grow();
        fieldContainer.setTouchable(Touchable.enabled);
        top.add(fieldContainer).height(32).growX();

        addDeleteBtn();

        setTouchable(Touchable.enabled);

        setBackgrounds();

        fieldContainer.addListener(new ClickListener() {

            private boolean dragged = false;

            private float lastPos = 0;

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                super.enter(event, x, y, pointer, fromActor);
                isHover = true;
                setBackgrounds();
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                super.exit(event, x, y, pointer, toActor);
                isHover = false;
                if (pointer == -1) {
                    setBackgrounds();
                }
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                dragged = false;
                lastPos = x;
                return super.touchDown(event, x, y, pointer, button);
            }


            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);
                setBackgrounds();
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                if (!dragged) {
                    showEditMode();
                }
            }
        });

        fieldNameTextField.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (SceneEditorWorkspace.isEnterPressed(keycode)) {
                    hideEditMode();
                }

                return super.keyDown(event, keycode);
            }
        });

        fieldNameTextField.addListener(new FocusListener() {
            @Override
            public void keyboardFocusChanged(FocusEvent event, Actor actor, boolean focused) {
                super.keyboardFocusChanged(event, actor, focused);
                if (!focused) {
                    hideEditMode();
                }
            }
        });

        innerWidget.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (!innerWidget.isFastChange()) {
                    applyValueToWrapper();
                    fireValueChangedEvent(false);
                }
            }
        });

        stageListener = new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                Vector2 tmpVec = new Vector2();
                tmpVec.set(x, y);
                stageToLocalCoordinates(tmpVec);
                Actor touchTarget = fieldContainer.hit(tmpVec.x, tmpVec.y, false);
                if (touchTarget == null && getStage() != null) {
                    getStage().setKeyboardFocus(null);
                }

                return false;
            }
        };

        content.add(top).growX();
        content.row();
        contentCell = content.add(bottom).growX();
    }

    private void collapse() {
        if (arrowButton.isCollapsed()) {
            bottom.clearChildren();
            bottom.pack();
            UIUtils.invalidateForDepth(bottom, 4);
        } else {
            bottom.clearChildren();
            bottom.add(innerWidget);
            bottom.pack();
            UIUtils.invalidateForDepth(bottom, 4);
        }
    }

    private void addDeleteBtn() {
        DeleteButton deleteButton = new DeleteButton();
        deleteButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                CustomVarWidgetChangeListener.CustomVarChangeEvent deleteEvent = Pools.obtain(CustomVarWidgetChangeListener.CustomVarChangeEvent.class);
                deleteEvent.setType(CustomVarWidgetChangeListener.Type.delete);

                try {
                    fire(deleteEvent);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    Pools.free(deleteEvent);
                }
            }
        });
        top.add(deleteButton).growY();
    }

    @Override
    protected void setStage(Stage stage) {
        super.setStage(stage);
        if (stage != null) {
            getStage().getRoot().addCaptureListener(stageListener);
            stageRef = getStage();
        } else {
            if (stageRef != null) {
                stageRef.getRoot().removeCaptureListener(stageListener);
                stageRef = null;
            }
        }
    }

    private void showEditMode() {
        if (editing.isVisible()) return;

        if (getStage() == null) {
            return;
        }
        getStage().setKeyboardFocus(fieldNameTextField);
        fieldNameTextField.selectAll();

        editing.setVisible(true);
        main.setVisible(false);

        isSelected = true;
        setBackgrounds();
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (isHover) {
            CursorUtil.setDynamicModeCursor(CursorUtil.CursorType.MOVE_ALL_DIRECTIONS);
        }
    }

    private void hideEditMode() {
        setFieldName(fieldNameTextField.getText());

        fieldNameTextField.clearSelection();

        editing.setVisible(false);
        main.setVisible(true);

        isSelected = false;
        setBackgrounds();
    }

    private void setBackgrounds() {

        ColorLibrary.BackgroundColor color = ColorLibrary.BackgroundColor.LIGHT_GRAY;

        if (isSelected) {
            color = ColorLibrary.BackgroundColor.MID_GRAY;
        } else {
            if (isHover) {
                color = ColorLibrary.BackgroundColor.BRIGHT_GRAY;
            }
        }

        fieldContainer.setBackground(ColorLibrary.obtainBackground(getSkin(), ColorLibrary.SHAPE_SQUARE, color));
    }

    public void setFieldName(String text) {
        String oldName = propertyWrapper.propertyName;

        fieldNameLabel.setText(text);
        fieldNameTextField.setText(text);

        propertyWrapper.propertyName = text;

        fireNameChangedEvent(oldName, text, false);
    }

    protected void fireValueChangedEvent(boolean isFastChange) {
        CustomVarWidgetChangeListener.CustomVarChangeEvent nameChangedEvent = Pools.obtain(CustomVarWidgetChangeListener.CustomVarChangeEvent.class);
        nameChangedEvent.setType(CustomVarWidgetChangeListener.Type.valueChanged);
        nameChangedEvent.setFastChange(isFastChange);

        try {
            fire(nameChangedEvent);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Pools.free(nameChangedEvent);
        }
    }

    protected void fireNameChangedEvent(String oldName, String newName, boolean isFastChange) {
        CustomVarWidgetChangeListener.CustomVarChangeEvent nameChangedEvent = Pools.obtain(CustomVarWidgetChangeListener.CustomVarChangeEvent.class);
        nameChangedEvent.setType(CustomVarWidgetChangeListener.Type.nameChanged);
        nameChangedEvent.setOldName(oldName);
        nameChangedEvent.setNewName(newName);
        nameChangedEvent.setFastChange(isFastChange);

        fire(nameChangedEvent);
        Pools.free(nameChangedEvent);
    }

    protected void fireCollapse() {
        CustomVarWidgetChangeListener.CustomVarChangeEvent nameChangedEvent = Pools.obtain(CustomVarWidgetChangeListener.CustomVarChangeEvent.class);
        nameChangedEvent.setType(CustomVarWidgetChangeListener.Type.collapse);
        nameChangedEvent.setFastChange(false);

        fire(nameChangedEvent);
        Pools.free(nameChangedEvent);
    }

    private void applyValueToWrapper() {
        innerWidget.applyValueToWrapper(propertyWrapper);
    }

    @Override
    public void loadFromXML(XmlReader.Element element) {

    }

    @Override
    public T getValue() {
        return propertyWrapper.getValue();
    }

    @Override
    public void read(Json json, JsonValue jsonValue) {

    }

    @Override
    public void write(Json json, String name) {

    }

    public abstract static class CustomVarWidgetChangeListener implements EventListener {
        public enum Type {
            nameChanged,
            valueChanged,
            delete,
            collapse,
        }

        @Override
        public boolean handle(Event event) {
            if (!(event instanceof CustomVarChangeEvent)) return false;

            CustomVarChangeEvent customVarChangeEvent = (CustomVarChangeEvent) event;

            switch (customVarChangeEvent.getType()) {
                case nameChanged:
                    String oldName = customVarChangeEvent.oldName;
                    String newName = customVarChangeEvent.newName;
                    boolean isFastChange = customVarChangeEvent.isFastChange;
                    nameChanged(customVarChangeEvent, event.getTarget(), oldName, newName, isFastChange);
                    break;
                case valueChanged:
                    valueChanged(customVarChangeEvent, event.getTarget(), customVarChangeEvent.isFastChange);
                    break;
                case delete:
                    delete(customVarChangeEvent, event.getTarget());
                    break;
                case collapse:
                    collapse(customVarChangeEvent, event.getTarget());
            }

            return false;
        }


        public abstract void nameChanged(CustomVarChangeEvent event, Actor actor, String oldName, String newName, boolean isFastChange);

        public abstract void valueChanged(CustomVarChangeEvent event, Actor actor, boolean isFastChange);

        public abstract void delete(CustomVarChangeEvent event, Actor actor);

        public void collapse(CustomVarChangeEvent event, Actor actor) {

        }

        public static class CustomVarChangeEvent extends Event {
            @Getter@Setter
            private Type type;
            @Getter@Setter
            private boolean isFastChange;

            @Getter@Setter
            private String oldName;

            @Getter@Setter
            private String newName;
        }
    }
}
