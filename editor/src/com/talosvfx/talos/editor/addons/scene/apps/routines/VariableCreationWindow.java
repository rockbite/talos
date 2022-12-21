package com.talosvfx.talos.editor.addons.scene.apps.routines;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.addons.scene.apps.routines.nodes.RoutineExposedVariableNodeWidget;
import com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.RoutineInstance;
import com.talosvfx.talos.editor.addons.scene.apps.routines.ui.CustomVarWidget;
import com.talosvfx.talos.editor.addons.scene.apps.routines.ui.types.CustomVector2Widget;
import com.talosvfx.talos.editor.addons.scene.utils.scriptProperties.PropertyWrapper;
import com.talosvfx.talos.editor.nodes.NodeListPopup;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.events.NodeCreatedEvent;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.widgets.ui.common.ColorLibrary;
import com.talosvfx.talos.editor.widgets.ui.common.ImageButton;
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
                routineStage.routineEditorApp.createNewVariable();
            }
        });

        add(topBar).growX();
        row();
        contentCell = add(content).grow();
    }

    public void reloadWidgets() {
        content.clear();

        Table inner = new Table();
        ScrollPane scrollPane = new ScrollPane(inner);
        scrollPane.setScrollingDisabled(true, false);
        content.add(scrollPane).grow().maxHeight(300).padBottom(10);

        //todo: add this to scroll pane

        RoutineInstance routineInstance = routineStage.routineInstance;
        Array<PropertyWrapper<?>> propertyWrappers = routineInstance.getPropertyWrappers();

        inner.add().padTop(5).row();

        templateRowArray.clear();
        for (int i = 0; i < propertyWrappers.size; i++) {
            PropertyWrapper<?> propertyWrapper = propertyWrappers.get(i);

            CustomVector2Widget innerWidget = new CustomVector2Widget(); // todo, support types
            CustomVarWidget widget = new CustomVarWidget(innerWidget, propertyWrapper.index);
            widget.setValue(propertyWrapper.propertyName);
            inner.add(widget).padTop(2).growX();
            inner.row();

            templateRowArray.add(widget);
        }

        configureDragAndDrop();
    }


    private void configureDragAndDrop() {
        dragAndDrop.clear();
        for (CustomVarWidget row : templateRowArray) {
            dragAndDrop.addSource(new DragAndDrop.Source(row) {
                @Override
                public DragAndDrop.Payload dragStart (InputEvent event, float x, float y, int pointer) {
                    DragAndDrop.Payload payload = new DragAndDrop.Payload();
                    payload.setObject(routineStage.routineInstance.getPropertyWrapperWithIndex(row.getIndex()));
                    Table payloadTable = new Table();
                    float width = row.getWidth();
                    float height = row.getHeight();
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
                temp.set(x, y);
                // x and y are in routineStageWrapper coordinate system
                // we need to convert them to nodeBoard's stage coordinate

                RoutineExposedVariableNodeWidget exposedVariable = ((RoutineExposedVariableNodeWidget) routineStage.createNode("ExposedVariableNode", temp.x, temp.y));
                PropertyWrapper<?> propertyWrapper = (PropertyWrapper<?>) payload.getObject();
                if (exposedVariable != null) {
                    NodeListPopup nodeListPopup = routineStage.getNodeListPopup();
                    exposedVariable.constructNode(nodeListPopup.getModuleByName("ExposedVariableNode"));
                    Notifications.fireEvent(Notifications.obtainEvent(NodeCreatedEvent.class).set(exposedVariable));
                    exposedVariable.update(propertyWrapper);
                }
            }
        });
    }
}
