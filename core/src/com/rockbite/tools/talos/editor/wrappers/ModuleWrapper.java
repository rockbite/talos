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
import com.badlogic.gdx.utils.JsonValue;
import com.kotcrab.vis.ui.widget.*;
import com.rockbite.tools.talos.editor.widgets.ui.ModuleBoardWidget;
import com.rockbite.tools.talos.runtime.Slot;
import com.rockbite.tools.talos.runtime.modules.Module;

public abstract class ModuleWrapper<T extends Module> extends VisWindow {

    protected T module;
    protected VisTable leftWrapper, rightWrapper, contentWrapper;
    protected Table content;

    protected IntMap<Image> inputSlotMap = new IntMap<>();
    protected IntMap<Image> outputSlotMap = new IntMap<>();

    private ModuleBoardWidget moduleBoardWidget;

    private int hoveredSlot = -1;
    private boolean hoveredSlotIsInput = false;

    private Vector2 tmp = new Vector2();
    private Vector2 tmp2 = new Vector2();

    private int id;

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

        leftWrapper = new VisTable();
        rightWrapper = new VisTable();
        contentWrapper = new VisTable();

        stack.add(leftWrapper);
        stack.add(rightWrapper);
        stack.add(contentWrapper);

        configureSlots();

        content.add(stack).grow().fill().width(reportPrefWidth());

        invalidateHierarchy();
        pack();

        addCaptureListener(new InputListener() {
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                moduleBoardWidget.selectWrapper(ModuleWrapper.this);
                return false;
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

    protected void addInputSlot(String title, int key) {
        Table slotRow = new Table();
        Image icon = new Image(getSkin().getDrawable("node-connector-off"));
        VisLabel label = new VisLabel(title, "small");
        slotRow.add(icon).left();
        slotRow.add(label).left().padBottom(4).padLeft(5).padRight(10);

        leftWrapper.add(slotRow).left().expandX();
        leftWrapper.row();

        configureNodeActions(icon, key, true);
    }

    protected void addOutputSlot(String title, int key) {
        Table slotRow = new Table();
        Image icon = new Image(getSkin().getDrawable("node-connector-off"));
        VisLabel label = new VisLabel(title, "small");
        slotRow.add(label).right().padBottom(4).padLeft(10).padRight(5);
        slotRow.add(icon).right();

        rightWrapper.add(slotRow).right().expandX();
        rightWrapper.row();

        configureNodeActions(icon, key, false);
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
        }
    }

    protected VisTextField addInputSlotWithTextField(String title, int key) {
        return addInputSlotWithTextField(title, key, 60);
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

    protected VisTextField addInputSlotWithTextField(String title, int key, float size) {
        Table slotRow = new Table();
        Image icon = new Image(getSkin().getDrawable("node-connector-off"));
        VisLabel label = new VisLabel(title, "small");
        slotRow.add(icon).left();
        slotRow.add(label).left().padBottom(4).padLeft(5).padRight(10);

        VisTextField textField = new VisTextField();
        slotRow.add(textField).width(size);

        leftWrapper.add(slotRow).left().expandX().pad(3);
        leftWrapper.row();

        configureNodeActions(icon, key, true);

        return textField;
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

    public abstract void write(JsonValue value);

    public abstract void read(JsonValue value);
}
