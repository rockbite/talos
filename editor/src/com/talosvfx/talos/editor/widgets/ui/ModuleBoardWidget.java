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

package com.talosvfx.talos.editor.widgets.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.talosvfx.talos.editor.Curve;
import com.talosvfx.talos.editor.ParticleEmitterWrapper;
import com.talosvfx.talos.editor.data.ModuleWrapperGroup;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.project2.TalosVFXUtils;
import com.talosvfx.talos.editor.project2.apps.ParticleNodeEditorApp;
import com.talosvfx.talos.editor.render.Render;
import com.talosvfx.talos.editor.utils.InputUtils;
import com.talosvfx.talos.runtime.vfx.serialization.ConnectionData;
import com.talosvfx.talos.editor.serialization.EmitterData;
import com.talosvfx.talos.editor.wrappers.*;
import com.talosvfx.talos.runtime.vfx.modules.AbstractModule;
import com.talosvfx.talos.runtime.vfx.ParticleEmitterDescriptor;
import com.talosvfx.talos.runtime.vfx.Slot;
import lombok.Getter;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class ModuleBoardWidget extends WidgetGroup {

    private static Logger logger = LoggerFactory.getLogger(ModuleBoardWidget.class);
    public ParticleNodeEditorApp app;
    ShapeRenderer shapeRenderer;

    public ObjectMap<ParticleEmitterWrapper, Array<ModuleWrapper>> moduleWrappers = new ObjectMap<>();
    public ObjectMap<ParticleEmitterWrapper, Array<NodeConnection>> nodeConnections = new ObjectMap<>();
    public ParticleEmitterWrapper currentEmitterWrapper;
    @Getter
    private ParticleEmitterDescriptor currentEmitterGraph;
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

    private ModuleWrapper ccFromWrapper = null;
    private int ccFromSlot = 0;
    private boolean ccCurrentIsInput = false;
    public boolean ccCurrentlyRemoving = false;

    private Image selectionRect;

    private Stage uiStage;
    public ModuleBoardWidget (ParticleNodeEditorApp app) {
        super();

        this.app = app;

        setTouchable(Touchable.enabled);

        curvePoints[0] = new Vector2();
        curvePoints[1] = new Vector2();
        curvePoints[2] = new Vector2();
        curvePoints[3] = new Vector2();

        addActor(groupContainer);
        addActor(moduleContainer);

        shapeRenderer = Render.instance().shapeRenderer();
    }

    public void init () {
        selectionRect = new Image(SharedResources.skin.getDrawable("orange_row"));
        selectionRect.setSize(0, 0);
        selectionRect.setVisible(false);
        addActor(selectionRect);
    }

    public Array<NodeConnection> getCurrentConnections () {
        // TODO: 23.02.23 dummmy refactor
        if (currentEmitterWrapper == null) {
            return new Array<>();
        }

        Array<NodeConnection> arr = nodeConnections.get(currentEmitterWrapper);
        if (arr == null) {
            arr = new Array<>();
            nodeConnections.put(currentEmitterWrapper, arr);
        }

        return arr;
    }

    public Array<ModuleWrapper> getModuleWrappers () {
        // TODO: 23.02.23 dummy refactor
        if (currentEmitterWrapper == null) {
            return new Array<>();
        }

        Array<ModuleWrapper> arr = moduleWrappers.get(currentEmitterWrapper);
        if (arr == null) {
            arr = new Array<>();
            moduleWrappers.put(currentEmitterWrapper, arr);
        }

        return arr;
    }

    public NodeConnection findConnection (ModuleWrapper moduleWrapper, boolean isInput, int key) {
        NodeConnection nodeToFind = null;
        for (NodeConnection nodeConnection : getCurrentConnections()) {
            if ((isInput && nodeConnection.toSlot == key && moduleWrapper == nodeConnection.toModule) ||
                    (!isInput && nodeConnection.fromSlot == key && moduleWrapper == nodeConnection.fromModule)) {
                // found the node let's remove it
                nodeToFind = nodeConnection;
            }
        }

        return nodeToFind;
    }

    public void removeConnection (NodeConnection connection, boolean shouldSave) {
        getCurrentConnections().removeValue(connection, true);

        connection.fromModule.setSlotInactive(connection.fromSlot, false);
        connection.toModule.setSlotInactive(connection.toSlot, true);

        currentEmitterGraph.removeNode(connection.fromModule.getModule(), connection.fromSlot, false);
        currentEmitterGraph.removeNode(connection.toModule.getModule(), connection.toSlot, true);

        if (shouldSave) {
            app.dataModified();
        }
    }

    public void setCurrentEmitter (ParticleEmitterWrapper currentEmitterWrapper) {
        this.currentEmitterWrapper = currentEmitterWrapper;
        this.currentEmitterGraph = currentEmitterWrapper.getGraph();

        groupContainer.clearChildren();
        moduleContainer.clearChildren();

        if (this.currentEmitterWrapper == null) return;

        for (ModuleWrapper wrapper : getModuleWrappers()) {
            moduleContainer.addActor(wrapper);
        }

        for (ModuleWrapperGroup group : getGroups()) {
            groupContainer.addActor(group);
        }

        resetCameraToWorkspace();
    }

    public void removeEmitter (ParticleEmitterWrapper wrapper) {
        moduleWrappers.remove(wrapper);
        nodeConnections.remove(wrapper);
    }

    public void clearAll () {
        moduleWrappers.clear();
        nodeConnections.clear();
        groups.clear();
    }

    public void loadEmitterToBoard (ParticleEmitterWrapper emitterWrapper, EmitterData emitterData) {
        IntMap<ModuleWrapper> map = new IntMap<>();
        if (!moduleWrappers.containsKey(emitterWrapper)) {
            moduleWrappers.put(emitterWrapper, new Array<ModuleWrapper>());
        }

        for (ModuleWrapper wrapper : emitterData.modules) {
            moduleWrappers.get(emitterWrapper).add(wrapper);
            wrapper.setModule(wrapper.getModule());
            wrapper.setBoard(this);
            map.put(wrapper.getId(), wrapper);
        }
        for (ConnectionData connectionData : emitterData.connections) {
            // make connections based on ids
            makeConnection(map.get(connectionData.moduleFrom), map.get(connectionData.moduleTo), connectionData.slotFrom, connectionData.slotTo);
        }
    }

    public Array<ModuleWrapperGroup> getGroups (ParticleEmitterWrapper emitterModuleWrapper) {
        return groups.get(emitterModuleWrapper);
    }

    public Array<ModuleWrapperGroup> getGroups () {
        // TODO: 23.02.23 dummy refactor
        if (currentEmitterWrapper == null) {
            return new Array<>();
        }

        Array<ModuleWrapperGroup> arr = groups.get(currentEmitterWrapper);
        if (arr == null) {
            arr = new Array<>();
            groups.put(currentEmitterWrapper, arr);
        }

        return arr;
    }

    public void removeGroup (ModuleWrapperGroup moduleWrapperGroup) {
        getGroups().removeValue(moduleWrapperGroup, true);
        moduleWrapperGroup.remove();
    }

    private Array<ModuleWrapperGroup> getSelectedGroups () {
        Array<ModuleWrapperGroup> groups = getGroups();
        Array<ModuleWrapperGroup> selectedGroups = new Array<>();
        ObjectSet<ModuleWrapper> wrappers = getSelectedWrappers();
        for (ModuleWrapperGroup group : groups) {
            boolean isFullyContained = true;
            for (ModuleWrapper wrapper : group.getModuleWrappers()) {
                if (!wrappers.contains(wrapper)) {
                    isFullyContained = false;
                    break;
                }
            }
            if (isFullyContained) {
                //add this group
                selectedGroups.add(group);
            }
        }

        return selectedGroups;
    }

    private Array<NodeConnection> getSelectedConnections () {
        Array<NodeConnection> arr = new Array<>();
        ObjectSet<ModuleWrapper> wrappers = getSelectedWrappers();
        Array<NodeConnection> connections = getCurrentConnections();
        for (NodeConnection connection : connections) {
            if (wrappers.contains(connection.fromModule) && wrappers.contains(connection.toModule)) {
                arr.add(connection);
            }
        }

        return arr;
    }

    public void sendInStage (Stage stage) {

        stage.addListener(new InputListener() {
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                super.touchDown(event, x, y, pointer, button);

                if (!event.isHandled()) {
                    if (button == 1) {
                        clearSelection();
                        showPopup();
                        return true;
                    } else {
                        clearSelection();
                        stage.unfocusAll();
                        return false;
                    }
                }
                return false;
            }
        });

        stage.addListener(new InputListener() {
            boolean dragged = false;
            final Vector2 startPos = new Vector2();
            final Vector2 tmp = new Vector2();
            final Rectangle rectangle = new Rectangle();

            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                dragged = false;
                boolean shouldHandle = false;

                if (button == 2 || InputUtils.ctrlPressed()) {
                    selectionRect.setVisible(true);
                    selectionRect.setSize(0, 0);
                    startPos.set(x, y);
                    shouldHandle = true;
                }

                return shouldHandle;
            }

            @Override
            public void touchDragged (InputEvent event, float x, float y, int pointer) {
                super.touchDragged(event, x, y, pointer);

                dragged = true;

                if(selectionRect.isVisible()) {
                    tmp.set(x, y);
                    tmp.sub(startPos);
                    if(tmp.x < 0) {
                        rectangle.setX(x);
                    } else {
                        rectangle.setX(startPos.x);
                    }
                    if(tmp.y < 0) {
                        rectangle.setY(y);
                    } else {
                        rectangle.setY(startPos.y);
                    }
                    rectangle.setWidth(Math.abs(tmp.x));
                    rectangle.setHeight(Math.abs(tmp.y));

                    selectionRect.setPosition(rectangle.x, rectangle.y);
                    selectionRect.setSize(rectangle.getWidth(), rectangle.getHeight());
                }
            }

            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                if (selectionRect.isVisible()) {
                    userSelectionApply(rectangle);
                    selectionRect.setVisible(false);
                }
            }
        });
    }

    public void sendInUIStage (Stage stage) {
        uiStage = stage;

    }

    public static class ClipboardPayload {
        Array<NodeConnection> connections;
        ObjectSet<ModuleWrapper> wrappers;
        Array<ModuleWrapperGroup> groups;

        public Vector2 cameraPositionAtCopy = new Vector2();

        public ClipboardPayload () {

        }

        public ClipboardPayload (ObjectSet<ModuleWrapper> wrappers, Array<NodeConnection> connections, Array<ModuleWrapperGroup> groups) {
            this.wrappers = wrappers;
            this.connections = connections;
            this.groups = groups;
        }
    }

    public void copySelectedModules () {
        Array<NodeConnection> connections = getSelectedConnections();
        ObjectSet<ModuleWrapper> wrappers = getSelectedWrappers();
        Array<ModuleWrapperGroup> groups = getSelectedGroups();

        ClipboardPayload payload = new ClipboardPayload(wrappers, connections, groups);

        logger.info("copy select todo");
//        Vector3 camPos = TalosMain.Instance().NodeStage().getStage().getCamera().position;
//        payload.cameraPositionAtCopy.set(camPos.x, camPos.y);

        Json json = new Json();
        String clipboard = json.toJson(payload);
        Gdx.app.getClipboard().setContents(clipboard);
    }

    public void cutSelectionModules () {
        Array<NodeConnection> connections = getSelectedConnections();
        ObjectSet<ModuleWrapper> wrappers = getSelectedWrappers();
        Array<ModuleWrapperGroup> groups = getSelectedGroups();

        ClipboardPayload payload = new ClipboardPayload(wrappers, connections, groups);

        logger.info("copy select todo");
//        Vector3 camPos = TalosMain.Instance().NodeStage().getStage().getCamera().position;
//        payload.cameraPositionAtCopy.set(camPos.x, camPos.y);

        Json json = new Json();
        String clipboard = json.toJson(payload);
        Gdx.app.getClipboard().setContents(clipboard);

        deleteSelectedWrappers();
    }

    public void pasteFromClipboard () {
        String clipboard = Gdx.app.getClipboard().getContents();

        ObjectMap<Integer, ModuleWrapper> previousWrapperIdMap = new ObjectMap<>();

        boolean hasParticleModule = false;
        boolean hasEmitterModule = false;
        for (ModuleWrapper wrapper : getModuleWrappers()) {
            if (wrapper instanceof ParticleModuleWrapper) hasParticleModule = true;
            if (wrapper instanceof EmitterModuleWrapper) hasEmitterModule = true;
        }

        Json json = new Json();
        try {
            ClipboardPayload payload = json.fromJson(ClipboardPayload.class, clipboard);

            logger.info("Copy paste todo");
//            Vector3 camPosAtPaste = TalosMain.Instance().NodeStage().getStage().getCamera().position;
//            Vector2 offset = new Vector2(camPosAtPaste.x, camPosAtPaste.y);
            Vector2 offset = new Vector2(0, 0);
            offset.sub(payload.cameraPositionAtCopy);

            ObjectSet<ModuleWrapper> wrappers = payload.wrappers;
            ObjectSet<ModuleWrapper> copiedWrappers = new ObjectSet<>();
            for (ModuleWrapper wrapper : wrappers) {
                if (wrapper instanceof ParticleModuleWrapper && hasParticleModule) {
                    continue;
                }
                if (wrapper instanceof EmitterModuleWrapper && hasEmitterModule) {
                    continue;
                }
                previousWrapperIdMap.put(wrapper.getId(), wrapper); // get old Id
                getModuleWrappers().add(wrapper);
                wrapper.moveBy(offset.x, offset.y);
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
            for (NodeConnection connection : payload.connections) {
                ModuleWrapper fromWrapper = previousWrapperIdMap.get(connection.fromModule.getId());
                ModuleWrapper toWrapper = previousWrapperIdMap.get(connection.toModule.getId());
                if (fromWrapper == null || toWrapper == null) {
                    continue;
                }
                makeConnection(fromWrapper, toWrapper, connection.fromSlot, connection.toSlot);
            }

            // now add groups
            for (ModuleWrapperGroup group : payload.groups) {
                ObjectSet<ModuleWrapper> newWrappers = new ObjectSet<>();
                for (ModuleWrapper wrapper : group.getModuleWrappers()) {
                    ModuleWrapper newWrapper = previousWrapperIdMap.get(wrapper.getId());
                    if (newWrapper != null) {
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

    public void clearCC () {
        ccFromWrapper = null;
    }

    public void userSelectionApply (Rectangle rectangle) {
        clearSelection();
        Array<ModuleWrapper> wrappers = getModuleWrappers();
        Rectangle moduleRect = new Rectangle();
        for (int i = 0; i < wrappers.size; i++) {
            ModuleWrapper wrapper = wrappers.get(i);
            tmp.set(wrapper.getX(), wrapper.getY());
            tmp.add(moduleContainer.getX(), moduleContainer.getY());
            localToStageCoordinates(tmp);
            moduleRect.set(tmp.x, tmp.y, wrapper.getWidth(), wrapper.getHeight());
            boolean hit = Intersector.intersectRectangles(rectangle, moduleRect, moduleRect);

            if (hit) {
                // hit
                addWrapperToSelection(wrapper);
            }
        }
    }

    public void selectAllModules () {
        ObjectSet<ModuleWrapper> wrappers = new ObjectSet<>();
        for (ModuleWrapper wrapper : getModuleWrappers()) {
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

    public void showPopup () {
        ParticleEmitterDescriptor moduleGraph = getCurrentEmitterGraph();

        if (moduleGraph == null) return;


        final Vector2 vec = new Vector2(Gdx.input.getX(), Gdx.input.getY());

        if (uiStage != null) {
            TalosVFXUtils.getModuleListPopup().showPopup(uiStage, vec, this);
        }
    }

    public void deleteSelectedWrappers () {
        try {
            deleteWrappers(getSelectedWrappers());
        } catch (Exception e) {
            e.printStackTrace();
        }

        clearSelection();
    }

    public void deleteWrappers (ObjectSet<ModuleWrapper> selectedWrappers) {
        final Array<ModuleWrapper> moduleWrappers = getModuleWrappers();
        final Array<NodeConnection> currentConnections = getCurrentConnections();

        for (ModuleWrapper selectedWrapper : selectedWrappers) {
            moduleWrappers.removeValue(selectedWrapper, true);

            for (int i = currentConnections.size - 1; i >= 0; i--) {
                if (currentConnections.get(i).toModule == selectedWrapper || currentConnections.get(i).fromModule == selectedWrapper) {
                    removeConnection(currentConnections.get(i), false);
                }
            }

            currentEmitterGraph.removeModule(selectedWrapper.getModule());
            moduleContainer.removeActor(selectedWrapper);

            for (ModuleWrapperGroup group : getGroups()) {
                group.removeWrapper(selectedWrapper);
            }
        }

        app.dataModified();
    }

    public <T extends AbstractModule, U extends ModuleWrapper<T>> U createModule (Class<T> clazz, float screenX, float screenY) {
        final T module;
        try {
            module = ClassReflection.newInstance(clazz);

            if (currentEmitterGraph.addModule(module)) {
                final U moduleWrapper = createModuleWrapper(module, screenX, screenY);
                moduleWrapper.setModuleToDefaults();
                module.setModuleGraph(currentEmitterGraph);
                moduleWrapper.onGraphSet();

                selectWrapper(moduleWrapper);

                // save here
                app.dataModified();

                return (U) moduleWrapper;
            } else {
                System.out.println("Did not create module: " + clazz.getSimpleName());
                return null;
            }
        } catch (ReflectionException e) {
            throw new GdxRuntimeException(e);
        }
    }


    /**
     * @param module
     * @param screenX in screen coordinate space
     * @param screenY in screen coordinate space
     * @param <T>
     * @param <U>
     * @return
     */
    public <T extends AbstractModule, U extends ModuleWrapper<T>> U createModuleWrapper (T module, float screenX, float screenY) {
        ModuleWrapper<T> moduleWrapper = null;

        if (module == null) return null;

        Class<T> moduleClazz = (Class<T>) module.getClass();

        try {
            Class<ModuleWrapper<T>> c = WrapperRegistry.get(moduleClazz);
            if (c == null) {
                throw new GdxRuntimeException("No wrapper found for clazz " + moduleClazz);
            }
            moduleWrapper = ClassReflection.newInstance(c);
            int id = getUniqueIdForModuleWrapper();
            moduleWrapper.setModule(module);
            moduleWrapper.setId(id);
            module.setIndex(id);
            moduleWrapper.setBoard(this);

            tmp.set(screenX, screenY);
            moduleContainer.screenToLocalCoordinates(tmp);

            moduleWrapper.setPosition(tmp.x - moduleWrapper.getWidth() / 2f, tmp.y - moduleWrapper.getHeight() / 2f);
            getModuleWrappers().add(moduleWrapper);
            moduleContainer.addActor(moduleWrapper);

        } catch (ReflectionException e) {
            e.printStackTrace();
        }


        // check if there was connect request
        tryAndConnectLasCC(moduleWrapper);


        return (U) moduleWrapper;
    }

    private <T extends AbstractModule> void tryAndConnectLasCC (ModuleWrapper<T> moduleWrapper) {
        if (ccFromWrapper != null) {
            Class fromClass;
            Slot fromSlotObject;
            IntMap<Slot> toSlots;
            ModuleWrapper fromModule;
            ModuleWrapper toModule;
            int fromSlot = 0;
            int toSlot = 0;
            if (ccCurrentIsInput) {
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

            for (Slot slot : toSlots.values()) {
                if (slot.isCompatable(fromSlotObject)) {
                    // we can connect
                    if (ccCurrentIsInput) {
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
    public void draw (Batch batch, float parentAlpha) {
        batch.end();
        shapeRenderer.setProjectionMatrix(getStage().getCamera().combined);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        drawCurves();
        shapeRenderer.end();
        batch.begin();

        super.draw(batch, parentAlpha);
    }

    private void drawCurves () {
        if (currentEmitterWrapper == null) return;

        // draw active curve
        if (activeCurve != null) {
            shapeRenderer.setColor(0, 203 / 255f, 124 / 255f, 1f);
            drawCurve(activeCurve.getFrom().x, activeCurve.getFrom().y, activeCurve.getTo().x, activeCurve.getTo().y);
        }

        shapeRenderer.setColor(1, 1, 1, 0.4f);
        // draw nodes
        for (NodeConnection connection : getCurrentConnections()) {
            connection.fromModule.getOutputSlotPos(connection.fromSlot, tmp);
            float x = tmp.x;
            float y = tmp.y;
            connection.toModule.getInputSlotPos(connection.toSlot, tmp);
            float toX = tmp.x;
            float toY = tmp.y;
            drawCurve(x, y, toX, toY);
        }
    }

    private void drawCurve (float x, float y, float toX, float toY) {
        //shapeRenderer.setColor(1, 1, 1, 1f);
        //shapeRenderer.rectLine(x, y, toX, toY, 2f);

        float minOffset = 10f;
        float maxOffset = 150f;

        float deltaX = Math.abs(toX - x);
        if (deltaX > maxOffset) deltaX = maxOffset;
        deltaX = deltaX / maxOffset;

        float offset = minOffset + (maxOffset - minOffset) * deltaX;

        curvePoints[0].set(x, y);
        curvePoints[1].set(x + offset, y);
        curvePoints[2].set(toX - offset, toY);
        curvePoints[3].set(toX + 20f, toY);

        bezier.set(curvePoints, 0, curvePoints.length);

        float resolution = 1f / 20f;

        for (float i = 0; i < 1f; i += resolution) {
            bezier.valueAt(tmp, i);
            if (i > 0) {
                shapeRenderer.rectLine(prev.x, prev.y, tmp.x, tmp.y, 2f);
            }
            prev.set(tmp);
        }
    }

    @Override
    public void act (float delta) {

        //center pos
//        tmp.x = gridPos.x+getStage().getWidth()/2f;
//        tmp.y = gridPos.y+getStage().getHeight()/2f;

        // now we need to figure out how to project that pos from stage to this widget
//        this.stageToLocalCoordinates(tmp);

//        groupContainer.setPosition(tmp.x, tmp.y);
//        moduleContainer.setPosition(tmp.x, tmp.y);

        super.act(delta);
    }

    @Override
    public void layout () {
        super.layout();
    }

    public void setActiveCurve (float x, float y, float toX, float toY, boolean isInput) {
        activeCurve = new Curve(x, y, toX, toY, isInput);
    }

    public void updateActiveCurve (float toX, float toY) {
        if (activeCurve != null) {
            activeCurve.setTo(toX, toY);
        }
    }

    public void addConnectionCurve (ModuleWrapper from, ModuleWrapper to, int slotForm, int slotTo) {
        NodeConnection connection = new NodeConnection();
        connection.fromModule = from;
        connection.toModule = to;
        connection.fromSlot = slotForm;
        connection.toSlot = slotTo;

        getCurrentConnections().add(connection);

        from.setSlotActive(slotForm, false);
        to.setSlotActive(slotTo, true);
    }

    public void makeConnection (ModuleWrapper from, ModuleWrapper to, int slotFrom, int slotTo) {
        currentEmitterGraph.connectNode(from.getModule(), to.getModule(), slotFrom, slotTo);
        addConnectionCurve(from, to, slotFrom, slotTo);

        from.attachModuleToMyOutput(to, slotFrom, slotTo);
        to.attachModuleToMyInput(from, slotTo, slotFrom);

    }

    public void connectNodeIfCan (ModuleWrapper currentWrapper, int currentSlot, boolean currentIsInput) {
        int[] result = new int[2];
        ModuleWrapper targetWrapper = null;
        boolean targetIsInput = false;
        // iterate over all widgets that are not current and see if mouse is over any of their slots, need to only connect input to output or output to input
        for (ModuleWrapper moduleWrapper : getModuleWrappers()) {
            if (moduleWrapper != currentWrapper) {
                moduleWrapper.findHoveredSlot(result);

                if (result[0] >= 0) {
                    // found match
                    targetWrapper = moduleWrapper;
                    if (result[1] == 0) {
                        targetIsInput = true;
                    } else {
                        targetIsInput = false;
                    }
                    break;
                }
            }
        }

        ccFromWrapper = null;

        if (targetWrapper == null || currentIsInput == targetIsInput) {
            // removing
            // show popup (but maybe not in case of removing of existing curve)
            if (activeCurve.getFrom().dst(activeCurve.getTo()) > 20 && !ccCurrentlyRemoving) {
                final Vector2 vec = new Vector2(Gdx.input.getX(), Gdx.input.getY());



                ccFromWrapper = currentWrapper;
                ccFromSlot = currentSlot;
                ccCurrentIsInput = currentIsInput;

                showPopup();
                //TalosMain.Instance().UIStage().createModuleListAdvancedPopup(vec);
            }
        } else {
            // yay we are connecting
            ModuleWrapper fromWrapper, toWrapper;
            int fromSlot, toSlot;

            if (targetIsInput) {
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
            if (findConnection(toWrapper, true, toSlot) == null) {
                makeConnection(fromWrapper, toWrapper, fromSlot, toSlot);
            }
        }
        removeActiveCurve();
    }

    public void removeActiveCurve () {
        activeCurve = null;
    }

    public int getUniqueIdForModuleWrapper () {
        int maxId = -1;
        for (ModuleWrapper wrapper : moduleWrappers.get(currentEmitterWrapper)) {
            if (wrapper.getId() > maxId) {
                maxId = wrapper.getId();
            }
        }

        return maxId + 1;
    }

    public void selectWrapper (ModuleWrapper wrapper) {
        clearSelection();
        addWrapperToSelection(wrapper);
    }

    public void addWrapperToSelection (ModuleWrapper wrapper) {
        selectedWrappers.add(wrapper);
        updateSelectionBackgrounds();
    }

    public void removeWrapperFromSelection (ModuleWrapper wrapper) {
        selectedWrappers.remove(wrapper);
        updateSelectionBackgrounds();
    }

    public ObjectSet<ModuleWrapper> getSelectedWrappers () {
        return selectedWrappers;
    }

    public void setSelectedWrappers (ObjectSet<ModuleWrapper> wrappers) {
        selectedWrappers.clear();
        selectedWrappers.addAll(wrappers);
        updateSelectionBackgrounds();
    }

    public void clearSelection () {
        selectedWrappers.clear();
        updateSelectionBackgrounds();
    }

    public void updateSelectionBackgrounds () {
        for (ModuleWrapper wrapper : getModuleWrappers()) {
            if (getSelectedWrappers().contains(wrapper)) {
                wrapper.setBackground("window-blue");
                wrapper.setSelectionState(true);
            } else {
                wrapper.setBackground("window");
                wrapper.setSelectionState(false);
            }
        }
    }

    public void wrapperClicked (ModuleWrapper wrapper) {
        wasWrapperDragged = null;
        if (selectedWrappers.contains(wrapper)) {
            wasWrapperSelectedOnDown = wrapper;
        } else {
            wasWrapperSelectedOnDown = null;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
            addWrapperToSelection(wrapper);
        } else {
            if (!selectedWrappers.contains(wrapper)) {
                selectWrapper(wrapper);
            }
        }
    }

    public void wrapperMovedBy (ModuleWrapper wrapper, float x, float y) {
        wasWrapperDragged = wrapper;
        if (selectedWrappers.size > 1) {
            for (ModuleWrapper other : selectedWrappers) {
                if (other != wrapper) {
                    other.moveBy(x, y);
                }
            }
        }
    }

    public void wrapperClickedUp (ModuleWrapper wrapper) {

        if (wasWrapperDragged != null) {
              app.dataModified();
        } else {
            // on mouse up when no drag happens this wrapper should be selected unless shift was pressed
            if (!Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
                selectWrapper(wrapper);
            } else {
                if (wasWrapperSelectedOnDown == wrapper) {
                    removeWrapperFromSelection(wrapper);
                } else {
                    addWrapperToSelection(wrapper);
                }
            }
        }
    }

    public ModuleWrapperGroup createGroupForWrappers (ObjectSet<ModuleWrapper> wrappers) {
        if (wrappers == null || wrappers.size == 0) return null;

        for (ModuleWrapperGroup other : getGroups()) {
            other.removeWrappers(wrappers);
        }

        ModuleWrapperGroup group = new ModuleWrapperGroup(SharedResources.skin);
        group.setModuleBoardReference(this);
        group.setWrappers(wrappers);
        getGroups().add(group);

        groupContainer.addActor(group);


        clearSelection();

        return group;
    }


    public void createGroupFromSelectedWrappers () {
        createGroupForWrappers(getSelectedWrappers());
    }

    public void ungroupWrappers (ObjectSet<ModuleWrapper> wrappers) {
        if (wrappers == null || wrappers.size == 0) return;

        for (ModuleWrapperGroup other : getGroups()) {
            other.removeWrappers(wrappers);
        }


        app.dataModified();
    }

    public void ungroupSelectedWrappers () {
        ungroupWrappers(getSelectedWrappers());
    }

    public void resetCameraToWorkspace () {
        Array<ModuleWrapper> wrappers = getModuleWrappers();
        ModuleWrapper particleWrapper = null;
        ModuleWrapper emitterWrapper = null;
        ModuleWrapper otherWrapper = null;
        for (ModuleWrapper wrapper : wrappers) {
            if (wrapper instanceof ParticleModuleWrapper) {
                particleWrapper = wrapper;
            }
            if (wrapper instanceof EmitterModuleWrapper) {
                emitterWrapper = wrapper;
            }
            otherWrapper = wrapper;
        }

        ModuleWrapper finalWrapper;

        if (particleWrapper != null) finalWrapper = particleWrapper;
        else if (emitterWrapper != null) finalWrapper = emitterWrapper;
        else finalWrapper = otherWrapper;

        if (finalWrapper != null) {

            tmp.set(finalWrapper.getX() + finalWrapper.getWidth() / 2f, finalWrapper.getY() + finalWrapper.getHeight() / 2f);
            tmp.add(moduleContainer.getX(), moduleContainer.getY());
            localToStageCoordinates(tmp);

        }
    }

}
