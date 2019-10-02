package com.rockbite.tools.talos.editor.widgets.ui;

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
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.kotcrab.vis.ui.FocusManager;
import com.kotcrab.vis.ui.widget.MenuItem;
import com.kotcrab.vis.ui.widget.PopupMenu;
import com.rockbite.tools.talos.TalosMain;
import com.rockbite.tools.talos.editor.Curve;
import com.rockbite.tools.talos.editor.ParticleEmitterWrapper;
import com.rockbite.tools.talos.editor.NodeStage;
import com.rockbite.tools.talos.editor.data.ModuleWrapperGroup;
import com.rockbite.tools.talos.editor.serialization.ConnectionData;
import com.rockbite.tools.talos.editor.serialization.EmitterData;
import com.rockbite.tools.talos.editor.wrappers.*;
import com.rockbite.tools.talos.runtime.*;
import com.rockbite.tools.talos.runtime.modules.*;
import com.rockbite.tools.talos.runtime.modules.Module;

import java.util.Comparator;

public class ModuleBoardWidget extends WidgetGroup {

    ShapeRenderer shapeRenderer;

    public ObjectMap<ParticleEmitterWrapper, Array<ModuleWrapper>> moduleWrappers = new ObjectMap<>();
    public ObjectMap<ParticleEmitterWrapper, Array<NodeConnection>> nodeConnections = new ObjectMap<>();
    private ParticleEmitterWrapper currentEmitterWrapper;
    private ObjectSet<ModuleWrapper> selectedWrappers = new ObjectSet<>();

    private ObjectMap<ParticleEmitterWrapper, Array<ModuleWrapperGroup>> groups = new ObjectMap<>();

    Group groupContainer = new Group();
    Group moduleContainer = new Group();

    public Vector2 gridPos = new Vector2();
    Vector2 tmp = new Vector2();
    Vector2 tmp2 = new Vector2();
    Vector2 prev = new Vector2();


    private Curve activeCurve;

    private Bezier<Vector2> bezier = new Bezier<>();
    private Vector2[] curvePoints = new Vector2[4];

    private ModuleWrapper wasWrapperSelectedOnDown = null;
    private ModuleWrapper wasWrapperDragged = null;


    private NodeStage mainStage;

    public ModuleBoardWidget(NodeStage mainStage) {
        super();
        this.mainStage = mainStage;

        curvePoints[0] = new Vector2();
        curvePoints[1] = new Vector2();
        curvePoints[2] = new Vector2();
        curvePoints[3] = new Vector2();

        registerWrappers();


        addActor(groupContainer);
        addActor(moduleContainer);

        shapeRenderer = new ShapeRenderer();

        addListener(new ClickListener() {

            @Override
            public boolean keyUp(InputEvent event, int keycode) {
                if(event.isHandled()) return super.keyUp(event, keycode);
                if(keycode == Input.Keys.DEL || keycode == Input.Keys.FORWARD_DEL) {
                    deleteSelectedWrappers();
                }
                return super.keyUp(event, keycode);
            }
        });
    }

    public Array<NodeConnection> getCurrentConnections() {
        Array<NodeConnection> arr =  nodeConnections.get(currentEmitterWrapper);
        if(arr == null) {
            arr = new Array<>();
            nodeConnections.put(currentEmitterWrapper, arr);
        }

        return arr;
    }

    public Array<ModuleWrapper> getModuleWrappers() {
        Array<ModuleWrapper> arr = moduleWrappers.get(currentEmitterWrapper);
        if(arr == null) {
            arr = new Array<>();
            moduleWrappers.put(currentEmitterWrapper, arr);
        }

        return arr;
    }

    public NodeConnection findConnection(ModuleWrapper moduleWrapper, boolean isInput, int key) {
        NodeConnection nodeToFind =  null;
        for(NodeConnection nodeConnection: getCurrentConnections()) {
            if((isInput && nodeConnection.toSlot == key && moduleWrapper == nodeConnection.toModule) ||
                    (!isInput && nodeConnection.fromSlot == key && moduleWrapper == nodeConnection.fromModule)) {
                // found the node let's remove it
                nodeToFind = nodeConnection;
            }
        }

        return nodeToFind;
    }

    public void removeConnection(NodeConnection connection) {
        getCurrentConnections().removeValue(connection, true);

        connection.fromModule.setSlotInactive(connection.fromSlot, false);
        connection.toModule.setSlotInactive(connection.toSlot, true);

        mainStage.getCurrentModuleGraph().removeNode(connection.fromModule.getModule(), connection.fromSlot, false);
        mainStage.getCurrentModuleGraph().removeNode(connection.toModule.getModule(), connection.toSlot, true);
    }

    public void setCurrentEmitter(ParticleEmitterWrapper currentEmitterWrapper) {
        this.currentEmitterWrapper = currentEmitterWrapper;

        groupContainer.clearChildren();
        moduleContainer.clearChildren();

        if(this.currentEmitterWrapper == null) return;

        for (ModuleWrapper wrapper : getModuleWrappers()) {
                moduleContainer.addActor(wrapper);
        }

        for (ModuleWrapperGroup group : getGroups()) {
            groupContainer.addActor(group);
        }
    }

    public void removeEmitter(ParticleEmitterWrapper wrapper) {
        moduleWrappers.remove(wrapper);
        nodeConnections.remove(wrapper);
    }

    public void clearAll() {
        moduleWrappers.clear();
        nodeConnections.clear();
        groups.clear();
    }

    public void fileDrop(String[] paths, float x, float y) {
        tmp.set(x, y);
        (getStage().getViewport()).unproject(tmp);

        for(ModuleWrapper wrapper: getModuleWrappers()) {
            tmp2.set(tmp);
            wrapper.stageToLocalCoordinates(tmp2);

            if(wrapper.hit(tmp2.x, tmp2.y, false) != null) {
                wrapper.fileDrop(paths, tmp2.x, tmp2.y);
            }
        }
    }


    public void loadEmitterToBoard(ParticleEmitterWrapper emitterWrapper, EmitterData emitterData) {
        IntMap<ModuleWrapper> map = new IntMap<>();
        if(!moduleWrappers.containsKey(emitterWrapper)) {
            moduleWrappers.put(emitterWrapper, new Array<ModuleWrapper>());
        }

        for(ModuleWrapper wrapper: emitterData.modules) {
            moduleWrappers.get(emitterWrapper).add(wrapper);
            wrapper.setModule(wrapper.getModule());
            wrapper.setBoard(this);
            map.put(wrapper.getId(), wrapper);
        }
        for(ConnectionData connectionData: emitterData.connections) {
            // make connections based on ids
            makeConnection(map.get(connectionData.moduleFrom), map.get(connectionData.moduleTo), connectionData.slotFrom, connectionData.slotTo);
        }
    }

    public Array<ModuleWrapperGroup> getGroups(ParticleEmitterWrapper emitterModuleWrapper) {
        return groups.get(emitterModuleWrapper);
    }

    public Array<ModuleWrapperGroup> getGroups() {
        Array<ModuleWrapperGroup> arr = groups.get(currentEmitterWrapper);
        if(arr == null) {
            arr = new Array<>();
            groups.put(currentEmitterWrapper, arr);
        }

        return arr;
    }

    public void removeGroup(ModuleWrapperGroup moduleWrapperGroup) {
        getGroups().removeValue(moduleWrapperGroup, true);
        moduleWrapperGroup.remove();
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
        WrapperRegistry.reg(ScriptModule.class, ScriptModuleWrapper.class);
        WrapperRegistry.reg(GradientColorModule.class, GradientColorModuleWrapper.class);
        WrapperRegistry.reg(TextureModule.class, TextureModuleWrapper.class);
        WrapperRegistry.reg(EmConfigModule.class, EmConfigModuleWrapper.class);
        WrapperRegistry.reg(OffsetModule.class, OffsetModuleWrapper.class);
    }

    public void showPopup() {
        ParticleEmitterDescriptor moduleGraph = getModuleGraph();

        if(moduleGraph == null) return;



        final Vector2 vec = new Vector2(Gdx.input.getX(), Gdx.input.getY());
        (TalosMain.Instance().UIStage().getStage().getViewport()).unproject(vec);


        PopupMenu menu = TalosMain.Instance().UIStage().createModuleListPopup(vec);
        menu.showMenu(TalosMain.Instance().UIStage().getStage(), vec.x, vec.y);
    }

    public void deleteSelectedWrappers() {
       for(ModuleWrapper wrapper : getSelectedWrappers()) {
           deleteWrapper(wrapper);
       }
       clearSelection();
    }

    public void deleteWrapper(ModuleWrapper wrapper) {
        getModuleWrappers().removeValue(wrapper, true);
        for(int i = getCurrentConnections().size-1; i >= 0; i--) {
            if(getCurrentConnections().get(i).toModule == wrapper || getCurrentConnections().get(i).fromModule == wrapper) {
                removeConnection(getCurrentConnections().get(i));
            }
        }
        mainStage.getCurrentModuleGraph().removeModule(wrapper.getModule());
        moduleContainer.removeActor(wrapper);
        for(ModuleWrapperGroup group: getGroups()) {
            group.removeWrapper(wrapper);
        }
    }

    public ModuleWrapper createModule (Class<? extends Module> clazz, float x, float y) {
        final Module module;
        try {
            module = ClassReflection.newInstance(clazz);
            module.setModuleGraph(TalosMain.Instance().Project().getCurrentModuleGraph());

            if (TalosMain.Instance().Project().getCurrentModuleGraph().addModule(module)) {
                return createModuleWrapper(module, x, y);
            } else {
                System.out.println("Did not create module: " + clazz.getSimpleName());
                return null;
            }
        } catch (ReflectionException e) {
            throw new GdxRuntimeException(e);
        }
    }

    public <T extends Module> ModuleWrapper createModuleWrapper (T module, float x, float y) {
        ModuleWrapper<T> moduleWrapper = null;

        if (module == null) return null;

        Class<T> moduleClazz = (Class<T>)module.getClass();

        try {
            moduleWrapper = ClassReflection.newInstance(WrapperRegistry.get(moduleClazz));
            int id = getUniqueIdForModuleWrapper();
            moduleWrapper.setModule(module);
            moduleWrapper.setId(id);
            module.setIndex(id);
            moduleWrapper.setBoard(this);

            tmp.set(x, Gdx.graphics.getHeight() - y);
            moduleContainer.screenToLocalCoordinates(tmp);

            moduleWrapper.setPosition(tmp.x - moduleWrapper.getWidth()/2f, tmp.y - moduleWrapper.getHeight()/2f);
            getModuleWrappers().add(moduleWrapper);
            moduleContainer.addActor(moduleWrapper);

            selectWrapper(moduleWrapper);
        } catch (ReflectionException e) {
            e.printStackTrace();
        }


        return moduleWrapper;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.end();
        shapeRenderer.setProjectionMatrix(getStage().getCamera().combined);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        drawCurves();
        shapeRenderer.end();
        batch.begin();

        super.draw(batch, parentAlpha);
    }

    private void drawCurves() {
        if(currentEmitterWrapper == null) return;

        // draw active curve
        if(activeCurve != null) {
            shapeRenderer.setColor(0, 203/255f, 124/255f, 1f);
            drawCurve(activeCurve.getFrom().x, activeCurve.getFrom().y, activeCurve.getTo().x, activeCurve.getTo().y);
        }

        shapeRenderer.setColor(1, 1, 1, 0.4f);
        // draw nodes
        for(NodeConnection connection: getCurrentConnections()) {
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

        float resolution = 1f/20f;

        for(float i = 0; i < 1f; i+=resolution) {
            bezier.valueAt(tmp, i);
            if(i > 0) {
                shapeRenderer.rectLine(prev.x, prev.y, tmp.x, tmp.y, 2f);
            }
            prev.set(tmp);
        }
    }

    @Override
    public void act(float delta) {

        //center pos
        tmp.x = gridPos.x+getStage().getWidth()/2f;
        tmp.y = gridPos.y+getStage().getHeight()/2f;

        // now we need to figure out how to project that pos from stage to this widget
        this.stageToLocalCoordinates(tmp);

        groupContainer.setPosition(tmp.x, tmp.y);
        moduleContainer.setPosition(tmp.x, tmp.y);

        super.act(delta);
    }

    @Override
    public void layout() {
        super.layout();
    }

    public ParticleEmitterDescriptor getModuleGraph() {
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

    public void addConnectionCurve(ModuleWrapper from, ModuleWrapper to, int slotForm, int slotTo) {
        NodeConnection connection = new NodeConnection();
        connection.fromModule = from;
        connection.toModule = to;
        connection.fromSlot = slotForm;
        connection.toSlot = slotTo;

        getCurrentConnections().add(connection);

        from.setSlotActive(slotForm, false);
        to.setSlotActive(slotTo, true);
    }

    public void makeConnection(ModuleWrapper from, ModuleWrapper to, int slotFrom, int slotTo) {
        mainStage.getCurrentModuleGraph().connectNode(from.getModule(), to.getModule(), slotFrom, slotTo);
        addConnectionCurve(from, to, slotFrom, slotTo);

        from.attachModuleToMyOutput(to, slotFrom, slotTo);
        to.attachModuleToMyInput(from, slotTo, slotFrom);
    }

    public void connectNodeIfCan(ModuleWrapper currentWrapper, int currentSlot, boolean currentIsInput) {
        int[] result = new int[2];
        ModuleWrapper targetWrapper = null;
        boolean targetIsInput = false;
        // iterate over all widgets that are not current and see if mouse is over any of their slots, need to only connect input to output or output to input
        for(ModuleWrapper moduleWrapper: getModuleWrappers()) {
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

    public int getUniqueIdForModuleWrapper() {
        int maxId = -1;
        for (ModuleWrapper wrapper: moduleWrappers.get(currentEmitterWrapper)) {
            if(wrapper.getId() > maxId) {
                maxId = wrapper.getId();
            }
        }

        return maxId + 1;
    }

    public void selectWrapper(ModuleWrapper wrapper) {
        clearSelection();
        addWrapperToSelection(wrapper);
    }

    public void addWrapperToSelection(ModuleWrapper wrapper) {
        selectedWrappers.add(wrapper);
        updateSelectionBackgrounds();
    }

    public void removeWrapperFromSelection(ModuleWrapper wrapper) {
        selectedWrappers.remove(wrapper);
        updateSelectionBackgrounds();
    }

    public ObjectSet<ModuleWrapper> getSelectedWrappers() {
        return selectedWrappers;
    }

    public void setSelectedWrappers(ObjectSet<ModuleWrapper> wrappers) {
        selectedWrappers.clear();
        selectedWrappers.addAll(wrappers);
        updateSelectionBackgrounds();
    }

    public void clearSelection() {
        selectedWrappers.clear();
        updateSelectionBackgrounds();
    }

    public void updateSelectionBackgrounds() {
        for(ModuleWrapper wrapper : getModuleWrappers()) {
            if(getSelectedWrappers().contains(wrapper)) {
                wrapper.setBackground("window-blue");
            } else {
                wrapper.setBackground("window");
            }
        }
    }

    public void wrapperClicked(ModuleWrapper wrapper) {
        wasWrapperDragged = null;
        if(selectedWrappers.contains(wrapper)) {
            wasWrapperSelectedOnDown = wrapper;
        } else {
            wasWrapperSelectedOnDown = null;
        }

        if(Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
            addWrapperToSelection(wrapper);
        } else {
            if(!selectedWrappers.contains(wrapper)) {
                selectWrapper(wrapper);
            }
        }
    }

    public void wrapperMovedBy(ModuleWrapper wrapper, float x, float y) {
        wasWrapperDragged = wrapper;
        if(selectedWrappers.size > 1) {
            for(ModuleWrapper other: selectedWrappers) {
                if(other != wrapper) {
                    other.moveBy(x, y);
                }
            }
        }
    }

    public void wrapperClickedUp(ModuleWrapper wrapper) {

        if(wasWrapperDragged != null) {

        } else {
            // on mouse up when no drag happens this wrapper should be selected unless shift was pressed
            if(!Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
                selectWrapper(wrapper);
            } else {
                if(wasWrapperSelectedOnDown == wrapper) {
                    removeWrapperFromSelection(wrapper);
                } else {
                    addWrapperToSelection(wrapper);
                }
            }
        }
    }

    public ModuleWrapperGroup createGroupForWrappers(ObjectSet<ModuleWrapper> wrappers) {
        if(wrappers == null || wrappers.size == 0) return null;

        for(ModuleWrapperGroup other: getGroups()) {
            other.removeWrappers(wrappers);
        }

        ModuleWrapperGroup group = new ModuleWrapperGroup(mainStage.getSkin());
        group.setWrappers(wrappers);
        getGroups().add(group);

        groupContainer.addActor(group);

        clearSelection();

        return group;
    }


    public void createGroupFromSelectedWrappers() {
        createGroupForWrappers(getSelectedWrappers());
    }


}
