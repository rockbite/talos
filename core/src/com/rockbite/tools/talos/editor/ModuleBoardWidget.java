package com.rockbite.tools.talos.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Bezier;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.kotcrab.vis.ui.FocusManager;
import com.kotcrab.vis.ui.widget.MenuItem;
import com.kotcrab.vis.ui.widget.PopupMenu;
import com.rockbite.tools.talos.editor.wrappers.*;
import com.rockbite.tools.talos.runtime.*;
import com.rockbite.tools.talos.runtime.modules.*;

public class ModuleBoardWidget extends WidgetGroup {

    ShapeRenderer shapeRenderer;

    Array<ModuleWrapper> moduleWrapperArray = new Array<>();

    Group moduleContainer = new Group();

    Vector2 gridPos = new Vector2();
    Vector2 tmp = new Vector2();
    Vector2 prev = new Vector2();

    private Array<NodeConnection> connections = new Array<>();
    private Curve activeCurve;

    private Bezier<Vector2> bezier = new Bezier<>();
    private Vector2[] curvePoints = new Vector2[4];

    private ModuleWrapper selectedWrapper;

    private MainStage mainStage;

    public ModuleBoardWidget(MainStage mainStage) {
        super();
        this.mainStage = mainStage;

        curvePoints[0] = new Vector2();
        curvePoints[1] = new Vector2();
        curvePoints[2] = new Vector2();
        curvePoints[3] = new Vector2();

        registerWrappers();

        addActor(moduleContainer);

        shapeRenderer = new ShapeRenderer();

        addListener(new ClickListener() {
            Vector2 prev = new Vector2();
            Vector2 tmp = new Vector2();

            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                prev.set(x, y);
                return true;
            }
            public void touchDragged (InputEvent event, float x, float y, int pointer) {
                if(event.isHandled()) return;
                tmp.set(x, y);
                tmp.sub(prev);

                //setX(getX() - tmp.x);
                //setY(getY() - tmp.y);
                gridPos.x += tmp.x;
                gridPos.y += tmp.y;

                prev.set(x, y);
                super.touchDragged(event, x, y, pointer);
            }
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);

                if(!event.isHandled() && button == 0) {
                    FocusManager.resetFocus(getStage());

                    if(selectedWrapper != null) {
                        selectedWrapper.setBackground("window");
                        selectedWrapper = null;
                    }
                }

                if(button == 1) {
                    showPopup();
                }
            }

            @Override
            public boolean keyUp(InputEvent event, int keycode) {
                if(event.isHandled()) return super.keyUp(event, keycode);
                if(keycode == Input.Keys.DEL || keycode == Input.Keys.FORWARD_DEL) {
                    deleteSelectedWrapper();
                }
                return super.keyUp(event, keycode);
            }
        });
    }

    public NodeConnection findConnection(ModuleWrapper moduleWrapper, boolean isInput, int key) {
        NodeConnection nodeToFind =  null;
        for(NodeConnection nodeConnection: connections) {
            if((isInput && nodeConnection.toSlot == key && moduleWrapper == nodeConnection.toModule) ||
                    (!isInput && nodeConnection.fromSlot == key && moduleWrapper == nodeConnection.fromModule)) {
                // found the node let's remove it
                nodeToFind = nodeConnection;
            }
        }

        return nodeToFind;
    }

    public void removeConnection(NodeConnection connection) {
        connections.removeValue(connection, true);

        connection.fromModule.setSlotInactive(connection.fromSlot, false);
        connection.toModule.setSlotInactive(connection.toSlot, true);

        mainStage.getCurrentModuleGraph().removeNode(connection.toModule.getModule(), connection.toSlot);
    }

    public void selectWrapper(ModuleWrapper selectWrapper) {
        for(ModuleWrapper wrapper : moduleWrapperArray) {
            if(selectWrapper == wrapper) {
                wrapper.setBackground("window-blue");
                selectedWrapper = wrapper;
            } else {
                wrapper.setBackground("window");
            }
        }
    }

    public class NodeConnection {
        public ModuleWrapper fromModule;
        public ModuleWrapper toModule;
        public int fromSlot;
        public int toSlot;
    }

    private void registerWrappers() {
        WrapperRegistry.reg(EmitterModule.class, EmitterModuleWrapper.class);
        WrapperRegistry.reg(InterpolationModule.class, InterpolationWrapper.class);
        WrapperRegistry.reg(InputModule.class, InputModuleWrapper.class);
        WrapperRegistry.reg(ParticleModule.class, ParticleModuleWrapper.class);
        WrapperRegistry.reg(StaticValueModule.class, StaticValueModuleWrapper.class);
        WrapperRegistry.reg(RandomRangeModule.class, RandomRangeModuleWrapper.class);
        WrapperRegistry.reg(MixModule.class, MixModuleWrapper.class);
        WrapperRegistry.reg(MathModule.class, MathModuleWrapper.class);
        WrapperRegistry.reg(CurveModule.class, CurveModuleWrapper.class);
        WrapperRegistry.reg(Vector2Module.class, Vector2ModuleWrapper.class);
        WrapperRegistry.reg(ColorModule.class, ColorModuleWrapper.class);
        WrapperRegistry.reg(DynamicRangeModule.class, DynamicRangeModuleWrapper.class);
    }

    private void showPopup() {
        ModuleGraph moduleGraph = getModuleGraph();

        if(moduleGraph == null) return;

        PopupMenu menu = new PopupMenu();
        for(final Class clazz : ModuleGraph.getModules()) {
            String className = clazz.getSimpleName();
            MenuItem menuItem = new MenuItem(className);
            menu.addItem(menuItem);

            final Vector2 vec = new Vector2(Gdx.input.getX(), Gdx.input.getY());
            (getStage().getViewport()).unproject(vec);

            menu.showMenu(getStage(), vec.x, vec.y);

            menuItem.addListener(new ClickListener() {
                @Override
                public void clicked (InputEvent event, float x, float y) {
                    createModuleWidget(clazz, vec.x, vec.y);
                }
            });
        }
    }

    private void deleteSelectedWrapper() {
        if(selectedWrapper != null) {
            moduleWrapperArray.removeValue(selectedWrapper, true);
            for(int i = connections.size-1; i >= 0; i--) {
                if(connections.get(i).toModule == selectedWrapper || connections.get(i).fromModule == selectedWrapper) {
                    removeConnection(connections.get(i));
                }
            }
            mainStage.getCurrentModuleGraph().removeModule(selectedWrapper.getModule());
            moduleContainer.removeActor(selectedWrapper);
        }
    }

    public ModuleWrapper createModuleWidget(Class clazz, float x, float y) {
        Module module = mainStage.getCurrentModuleGraph().createModule(clazz);
        ModuleWrapper moduleWrapper = null;

        if(module == null) return moduleWrapper;

        try {
            moduleWrapper = (ModuleWrapper) ClassReflection.newInstance(WrapperRegistry.map.get(clazz));


            moduleWrapper.setModule(module);
            moduleWrapper.setBoard(this);

            tmp.set(x, Gdx.graphics.getHeight() - y);
            moduleContainer.screenToLocalCoordinates(tmp);

            moduleWrapper.setPosition(tmp.x - moduleWrapper.getWidth()/2f, tmp.y - moduleWrapper.getHeight()/2f);
            moduleWrapperArray.add(moduleWrapper);
            moduleContainer.addActor(moduleWrapper);

            selectWrapper(moduleWrapper);
        } catch (ReflectionException e) {
            e.printStackTrace();
        }

        if(module != null) {
            if(module instanceof EmitterModule) {
                //particleSystem.setEmitterModule((EmitterModule) module);
            }
        }

        return moduleWrapper;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        Gdx.gl.glClearColor(0.15f, 0.15f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.end();
        shapeRenderer.setProjectionMatrix(getStage().getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        drawGrid(batch);
        drawCurves();
        shapeRenderer.end();
        batch.begin();

        super.draw(batch, parentAlpha);
    }

    private void drawCurves() {

        // draw active curve
        if(activeCurve != null) {
            drawCurve(activeCurve.getFrom().x, activeCurve.getFrom().y, activeCurve.getTo().x, activeCurve.getTo().y);
        }

        // draw nodes
        for(NodeConnection connection: connections) {
            connection.fromModule.getOutputSlotPos(connection.fromSlot, tmp);
            float x = tmp.x;
            float y = tmp.y;
            connection.toModule.getInputSlotPos(connection.toSlot, tmp);
            float toX = tmp.x;
            float toY = tmp.y;
            drawCurve(x, y, toX, toY);
        }
    }

    private void drawCurve(float x, float y, float toX, float toY) {
        //shapeRenderer.setColor(1, 1, 1, 1f);
        //shapeRenderer.rectLine(x, y, toX, toY, 2f);

        float minOffset = 10f;
        float maxOffset = 150f;

        float deltaX = Math.abs(toX - x);
        if(deltaX > maxOffset) deltaX = maxOffset;
        deltaX = deltaX/maxOffset;

        float offset = minOffset + (maxOffset-minOffset) * deltaX;

        curvePoints[0].set(x, y);
        curvePoints[1].set(x+offset, y);
        curvePoints[2].set(toX - offset, toY);
        curvePoints[3].set(toX + 20f, toY);

        bezier.set(curvePoints, 0, curvePoints.length);
        shapeRenderer.setColor(1, 1, 1, 1f);

        float resolution = 1f/20f;

        for(float i = 0; i < 1f; i+=resolution) {
            bezier.valueAt(tmp, i);
            if(i > 0) {
                shapeRenderer.rectLine(prev.x, prev.y, tmp.x, tmp.y, 2f);
            }
            prev.set(tmp);
        }
    }

    private void drawGrid(Batch batch) {

        //center pos
        tmp.x = gridPos.x+getStage().getWidth()/2f;
        tmp.y = gridPos.y+getStage().getHeight()/2f;

        float tileSize = 15f;

        int lineCount = (int) (getStage().getWidth()/tileSize);
        int blackLineCount = (int) (getStage().getWidth()/(tileSize*10));
        float width = getStage().getWidth();
        float height = getStage().getHeight();

        int iter = 0;
        float thickness = 3f;

        for(int i = -lineCount/2 - 1; i < lineCount/2 + 1; i++) {
            float spacing = width/lineCount;
            float offsetX = (int)(gridPos.x /spacing);
            float offsetY = (int)(gridPos.y /spacing);
            shapeRenderer.setColor(0.17f, 0.17f, 0.17f, 1f);
            thickness = 2f;
            float posX = tmp.x + (i - offsetX) * spacing;
            float posY = tmp.y + (i - offsetY) * spacing;
            shapeRenderer.rectLine(posX, 0, posX, getStage().getHeight(), thickness);
            shapeRenderer.rectLine(0, posY, getStage().getWidth(), posY, thickness);
            iter++;
        }

        iter = 0;
        for(int i = -blackLineCount/2 - 1; i < blackLineCount/2 + 1; i++) {
            float spacing = width/blackLineCount;
            float offsetX = (int)(gridPos.x /spacing);
            float offsetY = (int)(gridPos.y /spacing);
            shapeRenderer.setColor(0.12f, 0.12f, 0.12f, 1f);
            thickness = 3f;
            float posX = tmp.x + (i - offsetX) * spacing;
            float posY = tmp.y + (i - offsetY) * spacing;
            shapeRenderer.rectLine(posX, 0, posX, getStage().getHeight(), thickness);
            shapeRenderer.rectLine(0, posY, getStage().getWidth(), posY, thickness);
            iter++;
        }

/*
// white cross at center
        shapeRenderer.setColor(1f, 1f, 1f, 1f);
        shapeRenderer.rectLine(tmp.x-10, gridPos.y + getStage().getHeight()/2f, tmp.x+10, gridPos.y+getStage().getHeight()/2f, 2f);
        shapeRenderer.rectLine(gridPos.x+getStage().getWidth()/2f, tmp.y-10, gridPos.x+getStage().getWidth()/2f, tmp.y+10, 2f);
*/
    }

    @Override
    public void act(float delta) {

        //center pos
        tmp.x = gridPos.x+getStage().getWidth()/2f;
        tmp.y = gridPos.y+getStage().getHeight()/2f;

        // now we need to figure out how to project that pos from stage to this widget
        this.stageToLocalCoordinates(tmp);

        moduleContainer.setPosition(tmp.x, tmp.y);

        super.act(delta);
    }

    @Override
    public void layout() {
        super.layout();
    }

    public ModuleGraph getModuleGraph() {
        return mainStage.getCurrentModuleGraph();
    }

    public void setActiveCurve(float x, float y, float toX, float toY, boolean isInput) {
        activeCurve = new Curve(x, y, toX, toY, isInput);
    }

    public void updateActiveCurve(float toX, float toY) {
        if(activeCurve != null) {
            activeCurve.setTo(toX, toY);
        }
    }

    public void makeConnection(ModuleWrapper from, ModuleWrapper to, int slotForm, int slotTo) {
        NodeConnection connection = new NodeConnection();
        connection.fromModule = from;
        connection.toModule = to;
        connection.fromSlot = slotForm;
        connection.toSlot = slotTo;

        connections.add(connection);

        mainStage.getCurrentModuleGraph().connectNode(from.getModule(), to.getModule(), slotForm, slotTo);

        from.setSlotActive(slotForm, false);
        to.setSlotActive(slotTo, true);
    }

    public void connectNodeIfCan(ModuleWrapper currentWrapper, int currentSlot, boolean currentIsInput) {
        int[] result = new int[2];
        ModuleWrapper targetWrapper = null;
        boolean targetIsInput = false;
        // iterate over all widgets that are not current and see if mouse is over any of their slots, need to only connect input to output or output to input
        for(ModuleWrapper moduleWrapper: moduleWrapperArray) {
            if(moduleWrapper != currentWrapper) {
                moduleWrapper.findHoveredSlot(result);

                if(result[0] >= 0 ) {
                    // found match
                    targetWrapper = moduleWrapper;
                    if(result[1] == 0) {
                        targetIsInput = true;
                    } else {
                        targetIsInput = false;
                    }
                    break;
                }
            }
        }

        if(targetWrapper == null || currentIsInput == targetIsInput) {
            // removing
        } else {
            // yay we are connecting
            ModuleWrapper fromWrapper, toWrapper;
            int fromSlot, toSlot;

            if(targetIsInput) {
                fromWrapper = currentWrapper;
                toWrapper = targetWrapper;
                fromSlot = currentSlot;
                toSlot = result[0];
            } else {
                fromWrapper = targetWrapper;
                toWrapper = currentWrapper;
                fromSlot = result[0];
                toSlot = currentSlot;
            }

            //what if this already exists?
            if(findConnection(toWrapper, true, toSlot) == null) {
                makeConnection(fromWrapper, toWrapper, fromSlot, toSlot);
            }
        }

        removeActiveCurve();
    }

    public void removeActiveCurve() {
        activeCurve = null;
    }
}
