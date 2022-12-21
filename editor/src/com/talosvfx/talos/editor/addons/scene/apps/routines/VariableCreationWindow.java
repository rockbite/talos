package com.talosvfx.talos.editor.addons.scene.apps.routines;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.addons.scene.apps.routines.nodes.RoutineExposedVariableNodeWidget;
import com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.RoutineInstance;
import com.talosvfx.talos.editor.addons.scene.apps.routines.ui.CustomVarWidget;
import com.talosvfx.talos.editor.addons.scene.apps.routines.ui.types.CustomFloatWidget;
import com.talosvfx.talos.editor.addons.scene.apps.routines.ui.types.CustomVector2Widget;
import com.talosvfx.talos.editor.addons.scene.utils.scriptProperties.PropertyWrapper;
import com.talosvfx.talos.editor.nodes.NodeListPopup;
import com.talosvfx.talos.editor.nodes.widgets.TextValueWidget;
import com.talosvfx.talos.editor.nodes.widgets.ValueWidget;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.events.NodeCreatedEvent;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.widgets.ui.common.ColorLibrary;
import com.talosvfx.talos.editor.widgets.ui.common.SquareButton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VariableCreationWindow extends Table {

    private static final Logger logger = LoggerFactory.getLogger(VariableCreationWindow.class);
    private final Table content;
    private final Cell<Table> contentCell;
    private DragAndDrop dragAndDrop;
    private Array<VariableTemplateRow<?>> templateRowArray = new Array<>();

    private RoutineStage routineStage;

    class TopBarButton extends Table {
        private final ClickListener clickListener;
        private final Image icon;

        public TopBarButton() {
            icon = new Image(SharedResources.skin.getDrawable("ic-plus"));
            icon.setTouchable(Touchable.enabled);
            add(icon).pad(5);
            setTouchable(Touchable.enabled);

            clickListener = new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    super.clicked(event, x, y);
                }
            };

            addListener(clickListener);
        }

        @Override
        public void act(float delta) {
            super.act(delta);

            if(clickListener.isOver()) {

            } else {

            }

            setBackground(SharedResources.skin.getDrawable("mini-btn-bg"));
        }
    }

    public VariableCreationWindow (RoutineStage routineStage) {
        this.routineStage = routineStage;
        setBackground(ColorLibrary.obtainBackground(SharedResources.skin, ColorLibrary.SHAPE_SQUIRCLE, ColorLibrary.BackgroundColor.DARK_GRAY));
        dragAndDrop = new DragAndDrop();

        content = new Table();

        Skin skin = SharedResources.skin;

        Table topBar = new Table();
        topBar.setBackground(ColorLibrary.obtainBackground(SharedResources.skin, ColorLibrary.SHAPE_SQUIRCLE_TOP, ColorLibrary.BackgroundColor.LIGHT_GRAY));
        Label label = new Label("gavno.rt", skin);

        topBar.add(label).left().pad(5).expandX();




        add(topBar).growX();
        row();
        contentCell = add(content).grow();
    }

    public void reloadWidgets() {
        content.clear();

        CustomVector2Widget widget = new CustomVector2Widget();

        CustomVarWidget test = new CustomVarWidget(widget);
        test.setValue("name");

        content.add(test).padTop(10).growX();



        /*
        clear();
        setSize(420, 300);
        defaults().pad(5);
        bottom().left();

        Table mainContent = new Table();
        mainContent.setSkin(routineStage.skin);
        mainContent.setBackground("background-fill");

        Table contentTable = new Table();
        contentTable.top().left();
        ScrollPane scrollPane = new ScrollPane(contentTable);
        scrollPane.setScrollingDisabled(true, false);

        RoutineInstance routineInstance = routineStage.routineInstance;
        Array<PropertyWrapper<?>> propertyWrappers = routineInstance.getPropertyWrappers();
        templateRowArray.clear();
        for (int i = 0; i < propertyWrappers.size; i++) {
            PropertyWrapper<?> propertyWrapper = propertyWrappers.get(i);
            VariableTemplateRow<?> variableTemplateRow = new VariableTemplateRow<>(propertyWrapper, routineStage);
            if (i == 0) {
                variableTemplateRow.textValueWidget.setType(ValueWidget.Type.TOP);
            } else if (i == propertyWrappers.size - 1) {
                variableTemplateRow.textValueWidget.setType(ValueWidget.Type.BOTTOM);
            } else {
                variableTemplateRow.textValueWidget.setType(ValueWidget.Type.MID);
            }

            if (propertyWrappers.size == 1) {
                variableTemplateRow.textValueWidget.setType(ValueWidget.Type.NORMAL);
            }

            contentTable.add(variableTemplateRow).growX();
            contentTable.row();
            templateRowArray.add(variableTemplateRow);
        }

        mainContent.add(scrollPane).grow();
        add(mainContent).grow();
        row();

        addButton();

        configureDragAndDrop();

         */
    }

    private void configureDragAndDrop() {
        dragAndDrop.clear();
        for (VariableTemplateRow variableTemplateRow : templateRowArray) {
            dragAndDrop.addSource(new DragAndDrop.Source(variableTemplateRow) {
                @Override
                public DragAndDrop.Payload dragStart (InputEvent event, float x, float y, int pointer) {
                    DragAndDrop.Payload payload = new DragAndDrop.Payload();
                    payload.setObject(routineStage.routineInstance.getPropertyWrapperWithIndex(variableTemplateRow.propertyWrapper.index));
                    Table payloadTable = new Table();
                    float width = variableTemplateRow.getWidth();
                    float height = variableTemplateRow.getHeight();
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

    private void addButton() {
        Skin skin = routineStage.skin;
        SquareButton squareButton = new SquareButton(skin, new Label("New Variable", skin), "Add a new Variable");
        squareButton.addListener(new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                squareButton.setChecked(false);
                routineStage.routineEditorApp.createNewVariable();
            }
        });
        add(squareButton).growX();
    }
}
