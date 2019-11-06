/*******************************************************************************
 * Copyright 2019 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.rockbite.tools.talos.editor.widgets.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Bezier;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.rockbite.tools.talos.TalosMain;
import com.rockbite.tools.talos.editor.Curve;
import com.rockbite.tools.talos.editor.ParticleEmitterWrapper;
import com.rockbite.tools.talos.editor.NodeStage;
import com.rockbite.tools.talos.editor.data.ModuleWrapperGroup;
import com.rockbite.tools.talos.runtime.serialization.ConnectionData;
import com.rockbite.tools.talos.editor.serialization.EmitterData;
import com.rockbite.tools.talos.editor.wrappers.*;
import com.rockbite.tools.talos.runtime.*;
import com.rockbite.tools.talos.runtime.modules.Module;

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
    private ModuleWrapper ccFromWrapper = null;
    private int ccFromSlot = 0;
    private boolean ccCurrentIsInput = false;
    public boolean ccCurrentlyRemoving = false;

    public ModuleBoardWidget(NodeStage mainStage) {
        super();
        this.mainStage = mainStage;

        curvePoints[0] = new Vector2();
        curvePoints[1] = new Vector2();
        curvePoints[2] = new Vector2();
        curvePoints[3] = new Vector2();

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

        TalosMain.Instance().ProjectController().setDirty();
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

    private Array<ModuleWrapperGroup> getSelectedGroups() {
        Array<ModuleWrapperGroup> groups = getGroups();
        Array<ModuleWrapperGroup> selectedGroups = new Array<>();
        ObjectSet<ModuleWrapper> wrappers = getSelectedWrappers();
        for(ModuleWrapperGroup group: groups) {
            boolean isFullyContained = true;
            for(ModuleWrapper wrapper: group.getModuleWrappers()) {
                if(!wrappers.contains(wrapper)) {
                    isFullyContained = false;
                    break;
                }
            }
            if(isFullyContained) {
                //add this group
                selectedGroups.add(group);
            }
        }

        return selectedGroups;
    }

    private Array<NodeConnection> getSelectedConnections() {
        Array<NodeConnection> arr = new Array<>();
        ObjectSet<ModuleWrapper> wrappers = getSelectedWrappers();
        Array<NodeConnection> connections = getCurrentConnections();
        for(NodeConnection connection: connections) {
            if(wrappers.contains(connection.fromModule) && wrappers.contains(connection.toModule)) {
                arr.add(connection);
            }
        }

        return arr;
    }

    public static class ClipboardPayload{
        Array<NodeConnection> connections;
        ObjectSet<ModuleWrapper> wrappers;
        Array<ModuleWrapperGroup> groups;

        public ClipboardPayload() {

        }

        public ClipboardPayload( ObjectSet<ModuleWrapper> wrappers, Array<NodeConnection> connections, Array<ModuleWrapperGroup> groups) {
            this.wrappers = wrappers;
            this.connections = connections;
            this.groups = groups;
        }
    }

    public void copySelectedModules() {
        Array<NodeConnection> connections = getSelectedConnections();
        ObjectSet<ModuleWrapper> wrappers = getSelectedWrappers();
        Array<ModuleWrapperGroup> groups = getSelectedGroups();

        ClipboardPayload payload = new ClipboardPayload(wrappers, connections, groups);

        Json json = new Json();
        String clipboard = json.toJson(payload);
        Gdx.app.getClipboard().setContents(clipboard);
    }

    public void pasteFromClipboard() {
        String clipboard = Gdx.app.getClipboard().getContents();

        ObjectMap<Integer, ModuleWrapper> previousWrapperIdMap = new ObjectMap<>();

        boolean hasParticleModule = false;
        boolean hasEmitterModule = false;
        for(ModuleWrapper wrapper: getModuleWrappers()) {
            if(wrapper instanceof ParticleModuleWrapper) hasParticleModule = true;
            if(wrapper instanceof EmitterModuleWrapper) hasEmitterModule = true;
        }

        Json json = new Json();
        try {
            ClipboardPayload payload = json.fromJson(ClipboardPayload.class, clipboard);

            ObjectSet<ModuleWrapper> wrappers = payload.wrappers;
            ObjectSet<ModuleWrapper> copiedWrappers = new ObjectSet<>();
            for(ModuleWrapper wrapper: wrappers) {
                if(wrapper instanceof ParticleModuleWrapper && hasParticleModule) {
                    continue;
                }
                if(wrapper instanceof EmitterModuleWrapper && hasEmitterModule) {
                    continue;
                }
                previousWrapperIdMap.put(wrapper.getId(), wrapper); // get old Id
                getModuleWrappers().add(wrapper);
                wrapper.moveBy(20, 20);
                wrapper.setModule(wrapper.getModule());
                int id = getUniqueIdForModuleWrapper();
                wrapper.setId(id);
                wrapper.getModule().setIndex(id);
                wrapper.setBoard(this);
                currentEmitterWrapper.getGraph().addModule(wrapper.getModule());
                wrapper.getModule().setModuleGraph(currentEmitterWrapper.getGraph());
                moduleContainer.addActor(wrapper);

                copiedWrappers.add(wrapper);
            }

            // now let's connect the connections
            for(NodeConnection connection: payload.connections) {
                ModuleWrapper fromWrapper = previousWrapperIdMap.get(connection.fromModule.getId());
                ModuleWrapper toWrapper = previousWrapperIdMap.get(connection.toModule.getId());
                if(fromWrapper == null || toWrapper == null) {
                    continue;
                }
                makeConnection(fromWrapper, toWrapper, connection.fromSlot, connection.toSlot);
            }

            // now add groups
            for(ModuleWrapperGroup group: payload.groups) {
                ObjectSet<ModuleWrapper> newWrappers = new ObjectSet<>();
                for(ModuleWrapper wrapper: group.getModuleWrappers()) {
                    ModuleWrapper newWrapper = previousWrapperIdMap.get(wrapper.getId());
                    if(newWrapper != null) {
                        newWrappers.add(newWrapper);
                    }
                }
                ModuleWrapperGroup newGroup = createGroupForWrappers(newWrappers);
                newGroup.setText(group.getText());
                newGroup.setColor(group.getColor());

            }

            setSelectedWrappers(copiedWrappers);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void clearCC() {
        ccFromWrapper = null;
    }

    public void userSelectionApply(Rectangle rectangle) {
        clearSelection();
        Array<ModuleWrapper> wrappers = getModuleWrappers();
        Rectangle moduleRect = new Rectangle();
        for(int i = 0; i < wrappers.size; i++) {
            ModuleWrapper wrapper = wrappers.get(i);
            tmp.set(wrapper.getX(), wrapper.getY());
            tmp.add(moduleContainer.getX(), moduleContainer.getY());
            localToStageCoordinates(tmp);
            moduleRect.set(tmp.x, tmp.y, wrapper.getWidth(), wrapper.getHeight());
            boolean hit = Intersector.intersectRectangles(rectangle, moduleRect, moduleRect);

            if(hit) {
                // hit
                addWrapperToSelection(wrapper);
            }
        }
    }

    public void selectAllModules() {
        ObjectSet<ModuleWrapper> wrappers = new ObjectSet<>();
        for(ModuleWrapper wrapper: getModuleWrappers()) {
            wrappers.add(wrapper);
        }
        setSelectedWrappers(wrappers);
    }


    public static class NodeConnection {
        public ModuleWrapper fromModule;
        public ModuleWrapper toModule;
        public int fromSlot;
        public int toSlot;
    }

    public void showPopup() {
        ParticleEmitterDescriptor moduleGraph = getModuleGraph();

        if(moduleGraph == null) return;


        final Vector2 vec = new Vector2(Gdx.input.getX(), Gdx.input.getY());
        (TalosMain.Instance().UIStage().getStage().getViewport()).unproject(vec);

        TalosMain.Instance().UIStage().createModuleListAdvancedPopup(vec);
        //PopupMenu menu = TalosMain.Instance().UIStage().createModuleListAdvancedPopup(vec);
        //menu.showMenu(TalosMain.Instance().UIStage().getStage(), vec.x, vec.y);
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

        TalosMain.Instance().ProjectController().setDirty();
    }

    public ModuleWrapper createModule (Class<? extends Module> clazz, float x, float y) {
        final Module module;
        try {
            module = ClassReflection.newInstance(clazz);

            if (TalosMain.Instance().TalosProject().getCurrentModuleGraph().addModule(module)) {
                TalosMain.Instance().ProjectController().setDirty();

                final ModuleWrapper moduleWrapper = createModuleWrapper(module, x, y);
                moduleWrapper.setModuleToDefaults();
                module.setModuleGraph(TalosMain.Instance().TalosProject().getCurrentModuleGraph());

                return moduleWrapper;
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


        // check if there was connect request
        tryAndConnectLasCC(moduleWrapper);


        return moduleWrapper;
    }

    private <T extends Module> void tryAndConnectLasCC(ModuleWrapper<T> moduleWrapper) {
        if(ccFromWrapper != null) {
            Class fromClass;
            Slot fromSlotObject;
            IntMap<Slot> toSlots;
            ModuleWrapper fromModule;
            ModuleWrapper toModule;
            int fromSlot = 0;
            int toSlot = 0;
            if(ccCurrentIsInput) {
                toSlots = moduleWrapper.getModule().getOutputSlots();

                fromModule = moduleWrapper;
                toModule = ccFromWrapper;
                toSlot = ccFromSlot;
                fromSlotObject = ccFromWrapper.getModule().getInputSlot(ccFromSlot);
            } else {
                toSlots = moduleWrapper.getModule().getInputSlots();

                fromModule = ccFromWrapper;
                toModule = moduleWrapper;
                fromSlot = ccFromSlot;
                fromSlotObject = ccFromWrapper.getModule().getOutputSlot(ccFromSlot);
            }

            for(Slot slot: toSlots.values()) {
                if(slot.isCompatable(fromSlotObject)) {
                    // we can connect
                    if(ccCurrentIsInput) {
                        fromSlot = slot.getIndex();
                    } else {
                        toSlot = slot.getIndex();
                    }

                    makeConnection(fromModule, toModule, fromSlot, toSlot);
                    break;
                }
            }

            ccFromWrapper = null;
        }
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

        TalosMain.Instance().ProjectController().setDirty();
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

        ccFromWrapper = null;

        if(targetWrapper == null || currentIsInput == targetIsInput) {
            // removing
            // show popup (but maybe not in case of removing of existing curve)
            if(activeCurve.getFrom().dst(activeCurve.getTo()) > 20 && !ccCurrentlyRemoving) {
                final Vector2 vec = new Vector2(Gdx.input.getX(), Gdx.input.getY());
                (TalosMain.Instance().UIStage().getStage().getViewport()).unproject(vec);
                ccFromWrapper = currentWrapper;
                ccFromSlot = currentSlot;
                ccCurrentIsInput = currentIsInput;
                TalosMain.Instance().UIStage().createModuleListAdvancedPopup(vec);
            }
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
                wrapper.setSelectionState(true);
            } else {
                wrapper.setBackground("window");
                wrapper.setSelectionState(false);
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
        TalosMain.Instance().ProjectController().setDirty();
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

        TalosMain.Instance().ProjectController().setDirty();

        clearSelection();

        return group;
    }


    public void createGroupFromSelectedWrappers() {
        createGroupForWrappers(getSelectedWrappers());
    }

    public void ungroupWrappers(ObjectSet<ModuleWrapper> wrappers) {
        if(wrappers == null || wrappers.size == 0) return;

        for(ModuleWrapperGroup other: getGroups()) {
            other.removeWrappers(wrappers);
        }

        TalosMain.Instance().ProjectController().setDirty();
    }

    public void ungroupSelectedWrappers() {
        ungroupWrappers(getSelectedWrappers());
    }

}
