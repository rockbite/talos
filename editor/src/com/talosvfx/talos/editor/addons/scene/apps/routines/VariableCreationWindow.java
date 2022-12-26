package com.talosvfx.talos.editor.addons.scene.apps.routines;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.talosvfx.talos.editor.addons.scene.apps.routines.nodes.RoutineExposedVariableNodeWidget;
import com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.RoutineInstance;
import com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.RoutineNode;
import com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.nodes.ExposedVariableNode;
import com.talosvfx.talos.editor.addons.scene.apps.routines.ui.CustomVarWidget;
import com.talosvfx.talos.editor.addons.scene.apps.routines.ui.types.ATypeWidget;
import com.talosvfx.talos.editor.addons.scene.utils.propertyWrappers.PropertyType;
import com.talosvfx.talos.editor.addons.scene.utils.propertyWrappers.PropertyWrapper;
import com.talosvfx.talos.editor.nodes.NodeBoard;
import com.talosvfx.talos.editor.nodes.NodeListPopup;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.events.NodeCreatedEvent;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.project2.vfxui.GenericStageWrappedViewportWidget;
import com.talosvfx.talos.editor.widgets.ui.common.ColorLibrary;
import com.talosvfx.talos.editor.widgets.ui.common.ImageButton;
import com.talosvfx.talos.editor.widgets.ui.menu.BasicPopup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class VariableCreationWindow extends Table {

    private static final Logger logger = LoggerFactory.getLogger(VariableCreationWindow.class);
    private final Table content;
    private final Cell<Table> contentCell;
    private DragAndDrop dragAndDrop;
    private Array<CustomVarWidget> templateRowArray = new Array<>();

    private RoutineStage routineStage;

    public VariableCreationWindow (RoutineStage routineStage) {
        setTouchable(Touchable.enabled);

        this.routineStage = routineStage;
        setBackground(ColorLibrary.obtainBackground(SharedResources.skin, ColorLibrary.SHAPE_SQUIRCLE, ColorLibrary.BackgroundColor.DARK_GRAY));
        dragAndDrop = new DragAndDrop();

        content = new Table();

        Skin skin = SharedResources.skin;

        Table topBar = new Table();
        topBar.setBackground(ColorLibrary.obtainBackground(SharedResources.skin, ColorLibrary.SHAPE_SQUIRCLE_TOP, ColorLibrary.BackgroundColor.LIGHT_GRAY));
        Label label = new Label("gavno.rt", skin);
        topBar.add(label).left().pad(5).expandX().padLeft(7);


        ImageButton plusButton = new ImageButton(
                SharedResources.skin.newDrawable("mini-btn-bg", Color.WHITE),
                SharedResources.skin.newDrawable("ic-plus", Color.WHITE));
        topBar.add(plusButton).right().pad(5).expandX();

        plusButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                BasicPopup.build(PropertyType.class)
                        .addItem("Float", PropertyType.FLOAT)
                        .addItem("Vector2", PropertyType.VECTOR2)
                        .addItem("Color", PropertyType.COLOR)
                        .addItem("Asset", PropertyType.ASSET)
                        .onClick(new BasicPopup.PopupListener<PropertyType>() {
                            @Override
                            public void itemClicked(PropertyType type) {
                                routineStage.data.createNewPropertyWrapper(type);
                                routineStage.routineUpdated();
                                reloadWidgets();
                            }
                        })
                        .show(plusButton, x, y);
            }
        });

        add(topBar).growX();
        row();
        contentCell = add(content).grow();

        addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
               event.stop();
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                event.stop();
            }
        });
    }

    public void reloadWidgets() {
        content.clear();

        Table inner = new Table();
        ScrollPane scrollPane = new ScrollPane(inner);
        scrollPane.setScrollingDisabled(true, false);
        content.add(scrollPane).grow().maxHeight(300).padBottom(10);

        RoutineInstance routineInstance = routineStage.data.getRoutineInstance();
        Array<PropertyWrapper<?>> propertyWrappers = routineInstance.getParentPropertyWrappers();

        inner.add().padTop(5).row();

        templateRowArray.clear();
        for (int i = 0; i < propertyWrappers.size; i++) {
            PropertyWrapper<?> propertyWrapper = propertyWrappers.get(i);

            try {
                PropertyType type = propertyWrapper.getType();
                ATypeWidget innerWidget = ClassReflection.newInstance(type.getWidgetClass());
                CustomVarWidget widget = new CustomVarWidget(routineStage, innerWidget, propertyWrapper.index);
                widget.setValue(propertyWrapper.propertyName);
                inner.add(widget).padTop(2).growX();
                inner.row();

                widget.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        propertyWrapper.propertyName = widget.getPropertyName();
                        widget.applyValueToWrapper(propertyWrapper);
                    }
                });

                templateRowArray.add(widget);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        configureDragAndDrop();
    }


    private void configureDragAndDrop() {
        dragAndDrop.clear();
        for (CustomVarWidget row : templateRowArray) {
            dragAndDrop.addSource(new DragAndDrop.Source(row.getFieldContainer()) {
                @Override
                public DragAndDrop.Payload dragStart (InputEvent event, float x, float y, int pointer) {
                    DragAndDrop.Payload payload = new DragAndDrop.Payload();
                    payload.setObject(routineStage.data.getRoutineInstance().getPropertyWrapperWithIndex(row.getIndex()));
                    Table payloadTable = new Table();
                    float width = row.getFieldContainer().getWidth();
                    float height = row.getFieldContainer().getHeight();
                    payloadTable.setSize(width, height);
                    payloadTable.setSkin(routineStage.skin);
                    payloadTable.setBackground("button-over");
                    payloadTable.getColor().a = 0.5f;
                    dragAndDrop.setDragActorPosition(width / 2f, -height / 2f);
                    payload.setDragActor(payloadTable);
                    return payload;
                }
            });
        }

        dragAndDrop.addTarget(new DragAndDrop.Target(routineStage.routineEditorApp.routineStageWrapper) {

            private Vector2 temp = new Vector2();

            @Override
            public boolean drag (DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
                return true;
            }

            @Override
            public void drop (DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {

                RoutineExposedVariableNodeWidget exposedVariable = ((RoutineExposedVariableNodeWidget) routineStage.createNode("ExposedVariableNode", Gdx.input.getX(), Gdx.input.getY()));
                PropertyWrapper<?> propertyWrapper = (PropertyWrapper<?>) payload.getObject();
                if (exposedVariable != null) {
                    NodeListPopup nodeListPopup = routineStage.getNodeListPopup();
                    exposedVariable.constructNode(nodeListPopup.getModuleByName("ExposedVariableNode"));
                    Notifications.fireEvent(Notifications.obtainEvent(NodeCreatedEvent.class).set(exposedVariable));
                    exposedVariable.update(propertyWrapper);
                }
                RoutineInstance routineInstance = routineStage.data.getRoutineInstance();
                RoutineNode nodeById = routineInstance.getNodeById(exposedVariable.getId());
                if (nodeById instanceof ExposedVariableNode) {
                    ExposedVariableNode exposedVariableNode = (ExposedVariableNode) nodeById;
                    exposedVariableNode.updateForPropertyWrapper(propertyWrapper);
                }
            }
        });
    }
}
