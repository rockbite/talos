package com.rockbite.tools.talos.editor.wrappers;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.kotcrab.vis.ui.widget.*;
import com.rockbite.tools.talos.editor.widgets.ui.DynamicTable;
import com.rockbite.tools.talos.editor.widgets.ui.ModuleBoardWidget;
import com.rockbite.tools.talos.runtime.Slot;
import com.rockbite.tools.talos.runtime.modules.ColorModule;
import com.rockbite.tools.talos.runtime.modules.Module;
import com.rockbite.tools.talos.runtime.values.NumericalValue;

public abstract class ModuleWrapper<T extends Module> extends VisWindow implements Json.Serializable {

    protected T module;
    protected DynamicTable leftWrapper, rightWrapper, contentWrapper;
    protected Table content;

    protected IntMap<Image> inputSlotMap = new IntMap<>();
    protected IntMap<Image> outputSlotMap = new IntMap<>();

    private ModuleBoardWidget moduleBoardWidget;

    private int hoveredSlot = -1;
    private boolean hoveredSlotIsInput = false;

    private Vector2 tmp = new Vector2();
    private Vector2 tmp2 = new Vector2();

    private IntMap<String> leftSlotNames = new IntMap<>();
    private IntMap<String> rightSlotNames = new IntMap<>();

    private int id;

    private boolean isSelected = false;

    public void setSelectionState(boolean selected) {
        if(isSelected != selected) {
            if(selected) {
                wrapperSelected();
            } else {
                wrapperDeselected();
            }
        }
        isSelected = selected;
    }

    protected void wrapperSelected() {

    }

    protected void wrapperDeselected() {

    }

    class SlotRowData {
        String title;
        int key;

        public SlotRowData(String title, int key) {
            this.title = title;
            this.key = key;
        }
    }

    public ModuleWrapper() {
        super("", "panel");

        setModal(false);
        setMovable(true);
        setKeepWithinParent(false);
        setKeepWithinStage(false);

        padTop(32);
        padLeft(16);

        content = new Table();
        add(content).grow().fill().padRight(13).padLeft(-2).padBottom(17);


        Stack stack = new Stack();

        leftWrapper = new DynamicTable();
        rightWrapper = new DynamicTable();
        contentWrapper = new DynamicTable();

        stack.add(leftWrapper);
        stack.add(rightWrapper);
        stack.add(contentWrapper);

        configureSlots();

        content.add(stack).grow().fill().width(reportPrefWidth());

        invalidateHierarchy();
        pack();

        addCaptureListener(new InputListener() {

            Vector2 tmp = new Vector2();
            Vector2 prev = new Vector2();

            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                prev.set(x, y);
                ModuleWrapper.this.localToStageCoordinates(prev);
                moduleBoardWidget.wrapperClicked(ModuleWrapper.this);
                return true;
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                tmp.set(x, y);
                ModuleWrapper.this.localToStageCoordinates(tmp);
                super.touchDragged(event, x, y, pointer);
                moduleBoardWidget.wrapperMovedBy(ModuleWrapper.this, tmp.x - prev.x, tmp.y - prev.y);

                prev.set(tmp);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);
                moduleBoardWidget.wrapperClickedUp(ModuleWrapper.this);
            }
        });

    }

    protected abstract void configureSlots();

    protected float reportPrefWidth() {
        return 300;
    }

    protected void addSeparator(boolean input) {
        if(input) {
            leftWrapper.addSeparator();
        } else {
            leftWrapper.addSeparator();
        }
    }

    protected Cell addInputSlot(String title, int key) {
        Table slotRow = new Table();
        Image icon = new Image(getSkin().getDrawable("node-connector-off"));
        VisLabel label = new VisLabel(title, "small");
        slotRow.add(icon).left();
        slotRow.add(label).left().padBottom(4).padLeft(5).padRight(10);

        Cell cell = leftWrapper.addRow(slotRow, true);

        leftSlotNames.put(key, title);

        configureNodeActions(icon, key, true);

        return cell;
    }

    protected Cell addOutputSlot(String title, int key) {
        Table slotRow = new Table();
        Image icon = new Image(getSkin().getDrawable("node-connector-off"));
        VisLabel label = new VisLabel(title, "small");
        slotRow.add(label).right().padBottom(4).padLeft(10).padRight(5);
        slotRow.add(icon).right();

        Cell cell = rightWrapper.addRow(slotRow, false);

        rightSlotNames.put(key, title);

        configureNodeActions(icon, key, false);

        return cell;
    }


    protected VisTextField addTextField(String title) {
        Table slotRow = new Table();
        VisTextField textField = new VisTextField(title);
        slotRow.add(textField).left().padBottom(4).padLeft(5).padRight(10);

        leftWrapper.add(slotRow).left().expandX();
        leftWrapper.row();

        return textField;
    }

    protected VisSelectBox addSelectBox(Array<String> values) {
        Table slotRow = new Table();
        VisSelectBox selectBox = new VisSelectBox();

        selectBox.setItems(values);

        slotRow.add(selectBox).left().padBottom(4).padLeft(5).padRight(10);

        leftWrapper.add(slotRow).left().expandX();
        leftWrapper.row();

        return selectBox;
    }

    protected VisSelectBox addSelectBox(IntMap.Values<String> values) {
        return addSelectBox(values.toArray());
    }

    protected void  configureNodeActions(final Image icon, final int key, final boolean isInput) {

        if(isInput) {
            inputSlotMap.put(key, icon);
        } else {
            outputSlotMap.put(key, icon);
        }

        icon.addListener(new ClickListener() {

            private Vector2 tmp = new Vector2();
            private Vector2 tmp2 = new Vector2();

            private ModuleWrapper currentWrapper;

            private boolean currentIsInput = false;

            private int currentSlot;

            private boolean dragged;

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                currentIsInput = isInput;
                currentWrapper = ModuleWrapper.this;
                tmp.set(x, y);
                icon.localToStageCoordinates(tmp);
                tmp2.set(icon.getWidth()/2f, icon.getHeight()/2f);
                icon.localToStageCoordinates(tmp2);

                currentSlot = key;

                dragged = false;

                ModuleBoardWidget.NodeConnection connection = moduleBoardWidget.findConnection(ModuleWrapper.this, isInput, key);

                if(isInput && connection!= null) {
                    moduleBoardWidget.removeConnection(connection);
                    moduleBoardWidget.ccCurrentlyRemoving = true;

                    connection.fromModule.getOutputSlotPos(connection.fromSlot, tmp2);
                    currentIsInput = false;
                    currentWrapper = connection.fromModule;
                    currentSlot = connection.fromSlot;
                    moduleBoardWidget.setActiveCurve(tmp2.x, tmp2.y, tmp.x, tmp.y, false);
                } else {
                    // we are creating new connection
                    moduleBoardWidget.setActiveCurve(tmp2.x, tmp2.y, tmp.x, tmp.y, isInput);
                }

                return true;
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                super.touchDragged(event, x, y, pointer);
                tmp.set(x, y);
                icon.localToStageCoordinates(tmp);
                moduleBoardWidget.updateActiveCurve(tmp.x, tmp.y);

                dragged = true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);
                moduleBoardWidget.connectNodeIfCan(currentWrapper, currentSlot, currentIsInput);
                moduleBoardWidget.ccCurrentlyRemoving = false;

                if(!dragged) {
                    // clicked
                    slotClicked(currentSlot, currentIsInput);
                }
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                super.enter(event, x, y, pointer, fromActor);
                hoveredSlot = key;
                hoveredSlotIsInput = isInput;
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                super.exit(event, x, y, pointer, toActor);
                hoveredSlot = -1;
            }
        });

    }

    public void slotClicked(int slotId, boolean isInput) {

        Slot slot = module.getInputSlot(slotId);
        if(!isInput) {
            slot = module.getOutputSlot(slotId);
        }

        if(slot == null) return;

        if(slot.isInput()) {
            Class<? extends Module> clazz = getSlotsPreferredModule(slot);

            if (clazz != null) {
                ModuleWrapper newWrapper = moduleBoardWidget.createModule(clazz, getX(), getY());

                //connecting
                //Slot newOutSlot = newWrapper.getModule().getOutputSlot(0);
                moduleBoardWidget.makeConnection(newWrapper, this, 0, slotId);

                // now tricky positioning
                float offset = MathUtils.random(100, 300);
                newWrapper.getOutputSlotPos(0, tmp);
                getInputSlotPos(slotId, tmp2);
                tmp2.x -= offset;
                tmp2.sub(tmp);
                tmp2.add(newWrapper.getX(), newWrapper.getY()); // new target
                tmp.set(tmp2).add(offset, 0); // starting position
                newWrapper.setPosition(tmp.x, tmp.y);

                // now the animation
                float duration = 0.2f;
                newWrapper.addAction(Actions.fadeIn(duration));
                newWrapper.addAction(Actions.moveTo(tmp2.x, tmp2.y, duration, Interpolation.swingOut));
            }
        }
    }

    public Class<? extends Module> getSlotsPreferredModule(Slot slot) {
        return null;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
    }

    public void setModule(T module) {
        this.module = module;
        getTitleLabel().setText(module.getClass().getSimpleName());
    }

    public T getModule() {
        return module;
    }

    public void setBoard(ModuleBoardWidget moduleBoardWidget) {
        this.moduleBoardWidget = moduleBoardWidget;
    }

    public boolean findHoveredSlot(int[] result) {
        if(hoveredSlot >= 0) {
            result[0] = hoveredSlot;
            if(hoveredSlotIsInput) {
                result[1] = 0;
            } else {
                result[1] = 1;
            }

            return true;
        }

        result[0] = -1;
        result[1] = -1;
        return false;
    }

    public void getInputSlotPos(int slot, Vector2 tmp) {
        if(inputSlotMap.get(slot) == null) return;
        tmp.set(inputSlotMap.get(slot).getWidth()/2f, inputSlotMap.get(slot).getHeight()/2f);
        inputSlotMap.get(slot).localToStageCoordinates(tmp);
    }

    public void getOutputSlotPos(int slot, Vector2 tmp) {
        if(outputSlotMap.get(slot) == null) return;
        tmp.set(outputSlotMap.get(slot).getWidth()/2f, outputSlotMap.get(slot).getHeight()/2f);
        outputSlotMap.get(slot).localToStageCoordinates(tmp);
    }

    public void setSlotActive(int slotTo, boolean isInput) {
        if(isInput) {
            if(inputSlotMap.get(slotTo) == null) return;
            inputSlotMap.get(slotTo).setDrawable(getSkin().getDrawable("node-connector-on"));
        } else {
            if(outputSlotMap.get(slotTo) == null) return;
            outputSlotMap.get(slotTo).setDrawable(getSkin().getDrawable("node-connector-on"));
        }
    }

    public void setSlotInactive(int slotTo, boolean isInput) {
        if(isInput) {
            inputSlotMap.get(slotTo).setDrawable(getSkin().getDrawable("node-connector-off"));
        } else {
            outputSlotMap.get(slotTo).setDrawable(getSkin().getDrawable("node-connector-off"));

            getTitleLabel().setText(module.getClass().getSimpleName());
        }
    }

    protected VisTextField addInputSlotWithTextField(String title, int key) {
        return addInputSlotWithTextField(title, key, 60, false);
    }

    protected VisTextField addInputSlotWithTextField(String title, int key, float size) {
        return addInputSlotWithTextField(title, key, size, false);
    }

    protected VisTextArea addInputSlotWithTextArea (String title, int key) {
        Table slotRow = new Table();
        Image icon = new Image(getSkin().getDrawable("node-connector-off"));
        VisLabel label = new VisLabel(title, "small");
        slotRow.add(icon).left();
        slotRow.add(label).left().padBottom(4).padLeft(5).padRight(10);

        VisTextArea textArea = new VisTextArea();
        slotRow.add(textArea).width(60);

        contentWrapper.add(slotRow).left().expandX().pad(3);

        configureNodeActions(icon, key, true);

        return textArea;
    }

    protected VisTextField addInputSlotWithTextField(String title, int key, float size, boolean grow) {
        Table slotRow = new Table();
        Image icon = new Image(getSkin().getDrawable("node-connector-off"));
        VisLabel label = new VisLabel(title, "small");
        slotRow.add(icon).left();
        slotRow.add(label).left().padBottom(4).padLeft(5).padRight(10);

        final VisTextField textField = new VisTextField();
        slotRow.add().fillX().expandX().growX();
        slotRow.add(textField).right().width(size);

        Cell cell = leftWrapper.add(slotRow).pad(3).expandX().left();
        if(grow) {
            cell.growX();
        }

        leftWrapper.row();

        textField.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);

                if(textField.getSelection().length() == 0) {
                    textField.selectAll();
                }
            }
        });

        configureNodeActions(icon, key, true);

        return textField;
    }

    protected float floatFromText(String text) {
        float value = 0;
        try {
            if (text.length() > 0) {
                value = Float.parseFloat(text);
            }
        } catch (NumberFormatException e) {

        }

        return value;
    }

    protected float floatFromText(VisTextField textField) {
        float value = 0;
        try {
            if (textField.getText().length() > 0) {
                value = Float.parseFloat(textField.getText());
            }
        } catch (NumberFormatException e) {

        }

        return value;
    }

    public int getId() {
        return id;
    }


    public void setId(int id) {
        this.id = id;
    }



    public void fileDrop(String[] paths, float x, float y) {
        // do nothing
    }

    public void attachModuleToMyInput(ModuleWrapper moduleWrapper, int mySlot, int targetSlot) {

    }

    public void attachModuleToMyOutput(ModuleWrapper moduleWrapper, int mySlot, int targetSlot) {
        // find the flavour
        Slot mySlotObject = getModule().getOutputSlot(mySlot);
        Slot toSlotObject = moduleWrapper.getModule().getInputSlot(targetSlot);
        if(mySlotObject == null || toSlotObject == null) return;
        if(mySlotObject.getValue() instanceof NumericalValue && toSlotObject.getValue() instanceof NumericalValue) {
            NumericalValue myValue = (NumericalValue) mySlotObject.getValue();
            NumericalValue toValue = (NumericalValue) toSlotObject.getValue();

            myValue.setFlavour(toValue.getFlavour());
        }

        // change the name
        getTitleLabel().setText(moduleWrapper.getLeftSlotName(targetSlot));
    }

    private String getLeftSlotName(int targetSlot) {
        return leftSlotNames.get(targetSlot);
    }

    @Override
    public void write (Json json) {
		json.writeValue("id", getId());
		json.writeValue("x", getX());
		json.writeValue("y", getY());

		json.writeObjectStart("module", module.getClass(), module.getClass());
		json.writeValue("data", module, null);
		json.writeObjectEnd();
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
		setId(jsonData.getInt("id"));
		setX(jsonData.getFloat("x"));
 		setY(jsonData.getFloat("y"));

        module = (T)json.readValue(Module.class, jsonData.get("module").get("data"));
        //TODO: this has to be create through module graph to go with properr creation channels

        setModule(module);
    }
}


