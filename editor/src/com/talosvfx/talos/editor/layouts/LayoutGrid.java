package com.talosvfx.talos.editor.layouts;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.ObjectMap;
import com.talosvfx.talos.runtime.assets.GameAssetType;
import com.talosvfx.talos.runtime.assets.RawAsset;
import com.talosvfx.talos.editor.project2.AppManager;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.utils.Toasts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class LayoutGrid extends WidgetGroup implements Json.Serializable {

    private static final Logger logger = LoggerFactory.getLogger(LayoutGrid.class);

    private DragAndDrop dragAndDrop;

    LayoutItem root;

    LayoutContent overItem;
    LayoutContent startItem;
    private Skin skin;

    private DragHitResult dragHitResult = new DragHitResult();

    private ObjectMap<LayoutContentAppPair, DragAndDrop.Source> sources = new ObjectMap<>();
    private ObjectMap<LayoutContent, DragAndDrop.Target> targets = new ObjectMap<>();

    float horizontalPercent = 0.3f;
    float verticalPercent = 0.3f;

    float rootHorizontalPercent = 0.03f;
    float rootVerticalPercent = 0.03f;

    public LayoutGrid (Skin skin) {
        this.skin = skin;

        dragAndDrop = new DragAndDrop();
        dragAndDrop.setKeepWithinStage(false);
    }

    public void removeContent (LayoutContent content) {
        removeContent(content, true);
    }


    public void removeApp (LayoutContent layoutContent, LayoutApp layoutApp) {
        //Find the content that has it and remove it
        layoutContent.removeContent(layoutApp);
        SharedResources.appManager.onAppRemoved(layoutApp);

        if (layoutContent.isEmpty()) {
            removeContent(layoutContent);
        }

    }

    public void removeContent (LayoutContent content, boolean removeEmptyParent) {
        removeDragTarget(content);

        if (content == root) {
            root = null;
            removeActor(content);
        } else {
            removeRecursive(content, removeEmptyParent);
        }
    }

    private void removeRecursive (LayoutItem content, boolean removeEmpty) {
        if (content.getParent() instanceof LayoutGrid) {
            reset();
            return;
        }

        LayoutItem parent = (LayoutItem) content.getParent(); //Its always going to be a LayoutItem

        parent.removeItem(content);

        if (removeEmpty && parent.isEmpty()) {
            removeRecursive(parent, removeEmpty);
        }

    }

    public void reset () {
        if (this.root != null) {
            this.root.remove();
            this.root = null;
        }
        overItem = null;
        startItem = null;
        sources.clear();
        targets.clear();
        dragAndDrop = new DragAndDrop();
    }

    @Override
    public void draw (Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
    }

    @Override
    public void write (Json json) {
        LayoutJsonStructure layoutJsonStructure = buildJsonFromObject(root);
        json.writeValue("structure", layoutJsonStructure);
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        LayoutJsonStructure layoutJsonStructure = json.readValue(LayoutJsonStructure.class, jsonData.get("structure"));
        System.out.println("got structure");
    }

    public void setLayoutActive (LayoutContent layoutContent) {
        Array<LayoutContent> out = new Array<>();
        getAllLayoutContentsFlat(root, out);

        for (LayoutContent content : out) {
            content.setLayoutFocused(false);
        }
        layoutContent.setLayoutFocused(true);
    }

    public void getAllLayoutContentsFlat (Array<LayoutContent> out) {
        getAllLayoutContentsFlat(root, out);
    }

    private void getAllLayoutContentsFlat (LayoutItem root, Array<LayoutContent> out) {
        if (root == null) return;
        if (root instanceof LayoutContent) {
            out.add((LayoutContent) root);
        } else if (root instanceof LayoutRow) {
            Array<LayoutItem> columns = ((LayoutRow) root).getColumns();
            for (int i = 0; i < columns.size; i++) {
                LayoutItem layoutItem = columns.get(i);
                getAllLayoutContentsFlat(layoutItem, out);
            }
        } else if (root instanceof LayoutColumn) {
            Array<LayoutItem> rows = ((LayoutColumn) root).getRows();
            for (int i = 0; i < rows.size; i++) {
                LayoutItem layoutItem = rows.get(i);
                getAllLayoutContentsFlat(layoutItem, out);
            }
        }
    }


    public enum LayoutDirection {
        UP,
        RIGHT,
        DOWN,
        LEFT,
        TAB,
        POP
    }

    public void addContent (LayoutItem content) {
        if (root == null) {
            root = content;
            addActor(root);
        } else {
            if (root instanceof LayoutRow) {
                ((LayoutRow) root).addColumnContainer(content, false);
            } else {
                //Exchange root
                LayoutItem oldRoot = root;
                removeActor(oldRoot);

                LayoutRow newRow = new LayoutRow(skin, this);
                newRow.addColumnContainer(oldRoot, false);
                newRow.addColumnContainer(content, false);

                root = newRow;
                addActor(root);
            }
        }

        registerDragTargetRecursive(content);


    }

    public void registerDragTargetRecursive (LayoutItem item) {
        if (item instanceof LayoutContent) {
            registerDragTarget((LayoutContent) item);
        } else if (item instanceof LayoutRow) {
            Array<LayoutItem> columns = ((LayoutRow) item).getColumns();
            for (int i = 0; i < columns.size; i++) {
                LayoutItem layoutItem = columns.get(i);
                registerDragTargetRecursive(layoutItem);
            }
        } else if (item instanceof LayoutColumn) {
            Array<LayoutItem> rows = ((LayoutColumn) item).getRows();
            for (int i = 0; i < rows.size; i++) {
                LayoutItem layoutItem = rows.get(i);
                registerDragTargetRecursive(layoutItem);
            }
        }
    }

    void registerDragTarget (LayoutContent layoutContent) {
        if (targets.containsKey(layoutContent)) return; //Don't register twice
        DragAndDrop.Target target = new DragAndDrop.Target(layoutContent) {
            @Override
            public boolean drag (DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {

                //should just always be true

                return true;
            }

            @Override
            public void drop (DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
                DragHitResult hitResult = dragHitResult;

                Object payloadObject = payload.getObject();
                LayoutContentAppPair layoutContentAppPair = (LayoutContentAppPair) payloadObject;

                if (dragHitResult.direction == LayoutDirection.POP) {

                    if (Gdx.app.getType() == Application.ApplicationType.Desktop) {
                        removeApp(layoutContentAppPair.layoutContent, layoutContentAppPair.app);
                        SharedResources.windowUtils.openWindow(layoutContentAppPair.app);
                    }

                    return;
                }

                if (hitResult.hit == null)
                    return;
                if (hitResult.hit == startItem)
                    return;


                if (hitResult.root) {
                    dropContainer(layoutContentAppPair, null, dragHitResult.direction);
                } else {
                    dropContainer(layoutContentAppPair, dragHitResult.hit, dragHitResult.direction);
                }

            }
        };
        dragAndDrop.addTarget(target);
        targets.put(layoutContent, target);
    }

    public void removeDragTarget (LayoutContent content) {
        DragAndDrop.Target remove = targets.remove(content);
        dragAndDrop.removeTarget(remove);
    }

    /*
    target is null when root should be used
     */
    private void dropContainer (LayoutContentAppPair source, @Null LayoutContent target, LayoutDirection direction) {
        //Here comes the logic

        LayoutContent parent = source.layoutContent;
        LayoutApp app = source.app;

        //Source
        //Remove drag and drop
        DragAndDrop.Source dragAndDropSource = sources.remove(source);
        dragAndDrop.removeSource(dragAndDropSource);

        //Potentially removes all the shit from hierarchy if its last one
        parent.removeContent(app);

        if (parent.isEmpty()) {
            removeRecursive(parent, true);
        }

        if (dragHitResult.root) {
            placeContentInRoot(direction, app);
        } else {
            placeContentRelative(target, direction, app);
        }

    }

    public void placeContentRelative (LayoutContent target, LayoutDirection direction, LayoutApp app) {
        //Target
        switch (direction) {

            //If its up or down we get the parent of the target and wrap it in a row, then add our content to the top or bottom
            case UP:
            case DOWN: {
                LayoutItem parentItem = (LayoutItem) target.getParent();

                //Check the parent item for the target. If its already a layout row, we can just add at top or bottom

                LayoutColumn colTarget;

                boolean isExistingColumn = false;
                if (parentItem instanceof LayoutColumn) {
                    colTarget = (LayoutColumn) parentItem;
                    isExistingColumn = true;
                } else {
                    //Its TIME TO WRAP

                    LayoutColumn newColumn = new LayoutColumn(skin, this);

                    //Remove the target
                    exchangeAndWrapToColumn(newColumn, target);

                    colTarget = newColumn;

                }

                LayoutContent newLayoutContent = new LayoutContent(skin, this);
                registerDragTarget(newLayoutContent);
                newLayoutContent.addContent(app);
                colTarget.addRowContainer(newLayoutContent, direction == LayoutDirection.UP, isExistingColumn ? target : null);
            }
            break;

            case RIGHT:
            case LEFT: {

                LayoutItem parentItem = (LayoutItem) target.getParent();

                //Check the parent item for the target. If its already a layout row, we can just add at top or bottom

                LayoutRow rowTarget;

                boolean isExistingRow = false;
                if (parentItem instanceof LayoutRow) {
                    rowTarget = (LayoutRow) parentItem;
                    isExistingRow = true;
                } else {
                    //Its TIME TO WRAP

                    LayoutRow newRow = new LayoutRow(skin, this);

                    //Remove the target
                    exchangeAndWrapToRow(newRow, target);

                    rowTarget = newRow;

                }

                LayoutContent newLayoutContent = new LayoutContent(skin, this);
                registerDragTarget(newLayoutContent);
                newLayoutContent.addContent(app);
                rowTarget.addColumnContainer(newLayoutContent, direction == LayoutDirection.LEFT, isExistingRow ? target : null);
            }

            break;

            case TAB:

                //If its a tab, we add it to the target
                target.addContent(app);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + direction);
        }
    }

    public void placeContentInRoot (LayoutDirection direction, LayoutApp app) {
        //Target
        switch (direction) {

            case UP:
            case DOWN: {

                //Check if root is row, if its not wrap
                if (root instanceof LayoutColumn) {
                    //Just add it to the column

                    LayoutContent newLayoutContent = new LayoutContent(skin, this);
                    newLayoutContent.addContent(app);

                    ((LayoutColumn) root).addRowContainer(newLayoutContent, direction == LayoutDirection.UP);

                } else {
                    LayoutItem oldRoot = root;
                    removeActor(oldRoot);

                    LayoutColumn newColumn = new LayoutColumn(skin, this);

                    newColumn.setRelativeWidth(1f);
                    newColumn.setRelativeHeight(1f);

                    oldRoot.setRelativeWidth(1f);
                    oldRoot.setRelativeHeight(1f);

                    newColumn.addRowContainer(oldRoot, true);

                    LayoutContent newLayoutContent = new LayoutContent(skin, this);

                    registerDragTarget(newLayoutContent);

                    newLayoutContent.addContent(app);
                    newLayoutContent.setRelativeWidth(1f);
                    newLayoutContent.setRelativeHeight(1f);

                    newColumn.addRowContainer(newLayoutContent, direction == LayoutDirection.UP);
                    addActor(newColumn);

                    root = newColumn;
                }

            }
            break;

            case RIGHT:
            case LEFT: {

                //Check if root is row, if its not wrap
                if (root instanceof LayoutRow) {
                    //Just add it to the column

                    LayoutContent newLayoutContent = new LayoutContent(skin, this);
                    newLayoutContent.addContent(app);

                    ((LayoutRow) root).addColumnContainer(newLayoutContent, direction == LayoutDirection.LEFT);

                } else {
                    LayoutItem oldRoot = root;
                    removeActor(oldRoot);

                    LayoutRow newLayoutRow = new LayoutRow(skin, this);

                    newLayoutRow.setRelativeWidth(1f);
                    newLayoutRow.setRelativeHeight(1f);

                    oldRoot.setRelativeWidth(1f);
                    oldRoot.setRelativeHeight(1f);

                    newLayoutRow.addColumnContainer(oldRoot, true);

                    LayoutContent newLayoutContent = new LayoutContent(skin, this);

                    registerDragTarget(newLayoutContent);

                    newLayoutContent.addContent(app);
                    newLayoutContent.setRelativeWidth(1f);
                    newLayoutContent.setRelativeHeight(1f);

                    newLayoutRow.addColumnContainer(newLayoutContent, direction == LayoutDirection.LEFT);
                    addActor(newLayoutRow);

                    root = newLayoutRow;
                }
            }

            break;
            default:
                throw new IllegalStateException("Unexpected value: " + direction);
        }
    }

    private void exchangeAndWrapToColumn (LayoutColumn newColumn, LayoutContent target) {

        //We need to swap this column with the parent
        LayoutItem parent = (LayoutItem) target.getParent();
        parent.exchangeItem(target, newColumn);
        newColumn.setRelativeWidth(target.getRelativeWidth());
        newColumn.setRelativeHeight(target.getRelativeHeight());

        target.setRelativeWidth(1f);
        target.setRelativeHeight(1f);

        newColumn.addRowContainer(target, false);
    }

    private void exchangeAndWrapToRow (LayoutRow newRow, LayoutContent target) {

        //We need to swap this column with the parent
        LayoutItem parent = (LayoutItem) target.getParent();
        parent.exchangeItem(target, newRow);

        newRow.setRelativeWidth(target.getRelativeWidth());
        newRow.setRelativeHeight(target.getRelativeHeight());

        target.setRelativeWidth(1f);
        target.setRelativeHeight(1f);

        newRow.addColumnContainer(target, false);
    }

    //Add each LayoutContent for drag and drop as a source
    //Add each LayoutContent for drag and drop as a target
    void registerDragSource (LayoutContent parent, LayoutApp layoutApp, Actor actorToDrag) {

        LayoutContentAppPair layoutContentAppObject = new LayoutContentAppPair(parent, layoutApp);

        DragAndDrop.Source source = new DragAndDrop.Source(actorToDrag) {
            @Override
            public DragAndDrop.Payload dragStart (InputEvent event, float x, float y, int pointer) {
                DragAndDrop.Payload payload = new DragAndDrop.Payload();

                LayoutContent dummy = new LayoutContent(skin, LayoutGrid.this);
                dummy.setSize(200, 200);

                dummy.addContent(layoutApp, true);

                payload.setDragActor(dummy);

                payload.setObject(layoutContentAppObject);

                startItem = parent;

                return payload;
            }

            @Override
            public void drag (InputEvent event, float x, float y, int pointer) {
                super.drag(event, x, y, pointer);

                float unhitSize = 200;

                Actor dragActor = dragAndDrop.getDragActor();
                if (dragActor != null) {

                    //Find out if we hit something, and if so what side

                    getDragHit(dragHitResult);


                    Vector2 vector2 = new Vector2();
                    float hitInStageX = vector2.x;
                    float hitInStageY = vector2.y;

                    LayoutContent hitResult = dragHitResult.hit;
                    if (hitResult != null) {
                        if (hitResult == startItem) {
                            return;
                        }

                        float horizontalPercentToUse = dragHitResult.root ? rootHorizontalPercent : horizontalPercent;
                        float vertPercentToUse = dragHitResult.root ? rootVerticalPercent : verticalPercent;

                        Actor targetActor = dragHitResult.root ? LayoutGrid.this : dragHitResult.hit;

                        switch (dragHitResult.direction) {
                            case UP:

                                dragActor.setSize(targetActor.getWidth(), targetActor.getHeight() * verticalPercent);

                                //The offset needs to be the difference between the drag x and y and the target x and y
                                vector2.setZero();
                                targetActor.localToStageCoordinates(vector2);
                                vector2.sub(x, y);

                                hitInStageX = vector2.x;
                                hitInStageY = vector2.y;

                                vector2.set(Gdx.input.getX(), Gdx.input.getY());
                                screenToLocalCoordinates(vector2);

                                dragAndDrop.setDragActorPosition(x - (vector2.x - hitInStageX) + targetActor.getWidth(), y - (vector2.y - hitInStageY) + targetActor.getHeight() - dragActor.getHeight());

                                break;

                            case DOWN:
                                dragActor.setSize(targetActor.getWidth(), targetActor.getHeight() * verticalPercent);

                                //The offset needs to be the difference between the drag x and y and the target x and y
                                vector2.setZero();
                                targetActor.localToStageCoordinates(vector2);
                                vector2.sub(x, y);

                                hitInStageX = vector2.x;
                                hitInStageY = vector2.y;

                                vector2.set(Gdx.input.getX(), Gdx.input.getY());
                                screenToLocalCoordinates(vector2);

                                dragAndDrop.setDragActorPosition(x - (vector2.x - hitInStageX) + targetActor.getWidth(), y - (vector2.y - hitInStageY));
                                break;

                            case RIGHT:
                                dragActor.setSize(horizontalPercent * targetActor.getWidth(), targetActor.getHeight());

                                //The offset needs to be the difference between the drag x and y and the target x and y
                                vector2.setZero();
                                targetActor.localToStageCoordinates(vector2);
                                vector2.sub(x, y);

                                hitInStageX = vector2.x;
                                hitInStageY = vector2.y;

                                vector2.set(Gdx.input.getX(), Gdx.input.getY());
                                screenToLocalCoordinates(vector2);

                                dragAndDrop.setDragActorPosition(x - (vector2.x - hitInStageX) + targetActor.getWidth(), y - (vector2.y - hitInStageY));
                                break;

                            case LEFT:
                                dragActor.setSize(horizontalPercent * targetActor.getWidth(), targetActor.getHeight());

                                //The offset needs to be the difference between the drag x and y and the target x and y
                                vector2.setZero();
                                targetActor.localToStageCoordinates(vector2);
                                vector2.sub(x, y);

                                hitInStageX = vector2.x;
                                hitInStageY = vector2.y;

                                vector2.set(Gdx.input.getX(), Gdx.input.getY());
                                screenToLocalCoordinates(vector2);

                                dragAndDrop.setDragActorPosition(x - (vector2.x - hitInStageX) + dragActor.getWidth(), y - (vector2.y - hitInStageY));
                                break;

                            case TAB:
                                Table tabTable = hitResult.getTabTable();
                                dragActor.setSize(200, tabTable.getHeight());

                                vector2.setZero();
                                targetActor.localToStageCoordinates(vector2);
                                vector2.sub(x, y);

                                hitInStageX = vector2.x;
                                hitInStageY = vector2.y;

                                vector2.set(Gdx.input.getX(), Gdx.input.getY());
                                screenToLocalCoordinates(vector2);

                                dragAndDrop.setDragActorPosition(+dragActor.getWidth() / 2f, y - (vector2.y - hitInStageY) + targetActor.getHeight() - dragActor.getHeight());

                                break;
                        }
                    } else {
                        dragActor.setSize(unhitSize, unhitSize);
                        dragAndDrop.setDragActorPosition(dragActor.getWidth() / 2f, -dragActor.getHeight() / 2f);
                    }

                }

            }

            @Override
            public void dragStop (InputEvent event, float x, float y, int pointer, DragAndDrop.Payload payload, DragAndDrop.Target target) {
                super.dragStop(event, x, y, pointer, payload, target);
                startItem = null;
                dragHitResult.reset();
            }
        };
        dragAndDrop.addSource(source);

        sources.put(layoutContentAppObject, source);
    }

    private void getDragHit (DragHitResult dragHitResult) {
        dragHitResult.reset();

        int x = Gdx.input.getX();
        int y = Gdx.input.getY();
        Vector2 universalCoords = new Vector2(x, y);

        if (overItem != null) {

            overItem.screenToLocalCoordinates(universalCoords);
            Vector2 copyOfLocalCoords = new Vector2(universalCoords.x, universalCoords.y);
            universalCoords.scl(1f / overItem.getWidth(), 1f / overItem.getHeight());

            //Prioritize tab
            Actor hit = null;
            if ((hit = overItem.hitTabTable(copyOfLocalCoords)) != null) {
                dragHitResult.hit = overItem;
                dragHitResult.direction = LayoutDirection.TAB;
                return;
            }
        }

        //Check for root
        if (root != null) {
            //Check edges of root
            Vector2 vecForMainGrid = new Vector2(Gdx.input.getX(), Gdx.input.getY());
            screenToLocalCoordinates(vecForMainGrid);

            vecForMainGrid.scl(1f / getWidth(), 1f / getHeight());

            float distanceFromMiddleX = Math.abs(0.5f - vecForMainGrid.x);
            float distanceFromMiddleY = Math.abs(0.5f - vecForMainGrid.y);

            if (distanceFromMiddleX >= distanceFromMiddleY) {
                //Its going to be an X if it exists

                if (vecForMainGrid.x < rootHorizontalPercent) {
                    //Left edge
                    dragHitResult.root = true;
                    dragHitResult.hit = overItem;
                    dragHitResult.direction = LayoutDirection.LEFT;
                    return;
                } else if (vecForMainGrid.x > (1 - rootHorizontalPercent)) {
                    //Right edge
                    dragHitResult.root = true;
                    dragHitResult.hit = overItem;
                    dragHitResult.direction = LayoutDirection.RIGHT;
                    return;
                }

            } else {
                //its going to be Y if it exists
                if (vecForMainGrid.y < rootVerticalPercent) {
                    dragHitResult.root = true;
                    dragHitResult.hit = overItem;
                    dragHitResult.direction = LayoutDirection.DOWN;
                    return;
                } else if (vecForMainGrid.y > (1 - rootVerticalPercent)) {
                    //top edge
                    dragHitResult.root = true;
                    dragHitResult.hit = overItem;
                    dragHitResult.direction = LayoutDirection.UP;
                    return;
                }
            }
        }

        if (overItem == null) {
            return;
        }

        //UNiversal coordinates

        float distanceFromMiddleX = Math.abs(0.5f - universalCoords.x);
        float distanceFromMiddleY = Math.abs(0.5f - universalCoords.y);

        if (distanceFromMiddleX >= distanceFromMiddleY) {
            //Its going to be an X if it exists

            if (universalCoords.x < horizontalPercent) {
                //Left edge
                dragHitResult.hit = overItem;
                dragHitResult.direction = LayoutDirection.LEFT;
            } else if (universalCoords.x > (1 - horizontalPercent)) {
                //Right edge
                dragHitResult.hit = overItem;
                dragHitResult.direction = LayoutDirection.RIGHT;
            }

        } else {

            //its going to be Y if it exists
            if (universalCoords.y < verticalPercent) {

                dragHitResult.hit = overItem;
                dragHitResult.direction = LayoutDirection.DOWN;
            } else if (universalCoords.y > (1 - verticalPercent)) {
                //top edge

                dragHitResult.hit = overItem;
                dragHitResult.direction = LayoutDirection.UP;
            }
        }


        if (dragHitResult.direction == null) { //Disable pop, not ready
//			dragHitResult.direction = LayoutDirection.POP;
        }

    }

    static class LayoutContentAppPair {
        public LayoutContent layoutContent;
        public LayoutApp app;

        public LayoutContentAppPair (LayoutContent parent, LayoutApp app) {
            this.layoutContent = parent;
            this.app = app;
        }

        @Override
        public boolean equals (Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            LayoutContentAppPair that = (LayoutContentAppPair) o;
            return Objects.equals(layoutContent, that.layoutContent) && Objects.equals(app, that.app);
        }

        @Override
        public int hashCode () {
            return Objects.hash(layoutContent, app);
        }
    }

    static class DragHitResult {
        public LayoutContent hit;
        public boolean root;
        public LayoutDirection direction;

        void reset () {
            hit = null;
            root = false;
            direction = null;
        }

        @Override
        public String toString () {
            String hitName = hit != null ? hit.getClass().getSimpleName() : "null";
            return "DragHitResult{" + "hit=" + hitName + ", root=" + root + ", direction=" + direction + '}';
        }
    }

    @Override
    public void layout () {
        super.layout();
        if (root == null)
            return;

        root.setBounds(0, 0, getWidth(), getHeight());
    }

    @Override
    public void act (float delta) {
        super.act(delta);
        if (root == null) {
            return;
        }

        int x = Gdx.input.getX();
        int y = Gdx.graphics.getHeight() - Gdx.input.getY();
        Vector2 screenCoords = new Vector2(x, y);
        screenToLocalCoordinates(screenCoords);

        Actor hit = hit(x, y, true);

        hit = getLayoutFromHit(hit);


        if (hit instanceof LayoutContent) {
            overItem = (LayoutContent) hit;
        } else if (hit != null) {
            if (hit.getParent() instanceof LayoutContent) {
                overItem = (LayoutContent) hit.getParent();
            } else if (hit.getParent() != null) {
                if (hit.getParent().getParent() instanceof LayoutContent) {
                    overItem = (LayoutContent) hit.getParent().getParent();
                }
            }
        } else {
            overItem = null;
        }
    }

    private Actor getLayoutFromHit (Actor hit) {
        if (hit == null) return null;

        if (hit instanceof LayoutContent) return hit;


        LayoutItem layoutItem = hit.firstAscendant(LayoutContent.class);

        if (layoutItem != null) {
            return layoutItem;
        }

        return hit;
    }

    enum LayoutType {
        ROW,
        COLUMN,
        CONTENT,
        APP
    }

    public static class LayoutJsonStructure {
        LayoutType type;
        String appID;

        boolean tabActive;

        String baseAppClazz;
        String gameAssetIdentifier;
        String gameAssetUniqueIdentifier;
        GameAssetType gameAssetType;

        float relativeWidth;
        float relativeHeight;
        Array<LayoutJsonStructure> children = new Array<>();
    }

    public String writeToJsonString () {
        Json json = new Json();
        LayoutJsonStructure layoutJsonStructure = buildJsonFromObject(root);
        return json.prettyPrint(layoutJsonStructure);
    }


    public void writeToJson (FileHandle handle) {
        Json json = new Json();

        LayoutJsonStructure rootJson = buildJsonFromObject(root);

        String result = json.prettyPrint(rootJson);
        handle.writeString(result, false);

    }

    public void readFromJsonStructure (LayoutJsonStructure layoutJsonStructure) {
        LayoutItem parent = null;

        if (layoutJsonStructure.type == LayoutType.COLUMN) {
            LayoutColumn layoutColumn = new LayoutColumn(skin, this);
            parent = layoutColumn;
        } else if (layoutJsonStructure.type == LayoutType.ROW) {
            LayoutRow layoutRow = new LayoutRow(skin, this);
            parent = layoutRow;
        } else if (layoutJsonStructure.type == LayoutType.CONTENT) {
            LayoutContent layoutContent = new LayoutContent(skin, this);
            parent = layoutContent;
        } else if (layoutJsonStructure.type == LayoutType.APP) {
            throw new GdxRuntimeException("Root can't be an APP");
        }

        //Load the children and this is recursive
        loadChildren(parent, layoutJsonStructure);


        addContent(parent);
    }

    public void readFromJson (JsonValue jsonLayoutRepresentation) {
        Json json = new Json();
        LayoutJsonStructure layoutJsonStructure = json.readValue(LayoutJsonStructure.class, jsonLayoutRepresentation);
        readFromJsonStructure(layoutJsonStructure);
    }

    private void loadChildren (LayoutItem parent, LayoutJsonStructure layoutJsonStructure) {
        Array<LayoutJsonStructure> children = layoutJsonStructure.children;

        if (children != null && !children.isEmpty()) {
            for (int i = 0; i < children.size; i++) {
                LayoutJsonStructure child = children.get(i);

                LayoutItem layoutItem = null;

                if (child.type == LayoutType.COLUMN) {
                    LayoutColumn layoutColumn = new LayoutColumn(skin, this);
                    layoutColumn.setRelativeWidth(child.relativeWidth);
                    layoutColumn.setRelativeHeight(child.relativeHeight);

                    layoutItem = layoutColumn;

                } else if (child.type == LayoutType.ROW) {
                    LayoutRow layoutRow = new LayoutRow(skin, this);
                    layoutRow.setRelativeWidth(child.relativeWidth);
                    layoutRow.setRelativeHeight(child.relativeHeight);

                    layoutItem = layoutRow;
                } else if (child.type == LayoutType.CONTENT) {
                    LayoutContent layoutContent = new LayoutContent(skin, this);
                    layoutContent.setRelativeWidth(child.relativeWidth);
                    layoutContent.setRelativeHeight(child.relativeHeight);

                    layoutItem = layoutContent;

                } else if (child.type == LayoutType.APP) {

                    String appID = child.appID;
                    boolean tabActive = child.tabActive;
                    String baseAppClazz = child.baseAppClazz;
                    String gameAssetIdentifier = child.gameAssetIdentifier;
                    GameAssetType gameAssetType = child.gameAssetType;
                    String gameAssetUniqueIdentifier = child.gameAssetUniqueIdentifier;

                    //We need to make the app

                    try {
                        AppManager.BaseApp baseApp = SharedResources.appManager.createAndRegisterAppExternal(appID, baseAppClazz, gameAssetType, gameAssetIdentifier, gameAssetUniqueIdentifier);

                        //We skip and just add it to the parent
                        if (!(parent instanceof LayoutContent)) {
                            logger.error("Parent is not layout content, invalid layout, ignoring");
                        }

                        LayoutApp gridAppReference = baseApp.getGridAppReference();

                        gridAppReference.setTabActive(tabActive);

                        LayoutContent parent1 = (LayoutContent) parent;
                        parent1.addContent(gridAppReference, false, false);

                    } catch (Exception e) {
                        e.printStackTrace();
                        logger.error("Error creating app from layout", e);
                        Toasts.getInstance().showErrorToast("Error creating app from layout " + e.getMessage());
                    }
                }

                if (layoutItem != null) {

                    //Add this item to the parent if parent is rows or layout
                    if (parent instanceof LayoutRow) {
                        ((LayoutRow) parent).addColumnContainer(layoutItem, false, false);
                    } else if (parent instanceof LayoutColumn) {
                        ((LayoutColumn) parent).addRowContainer(layoutItem, true, false);
                    }

                    //Load the children

                    loadChildren(layoutItem, child);

                }
            }
        }

        if (parent instanceof LayoutContent)
            ((LayoutContent) parent).sortToActiveTab();
    }


    private LayoutJsonStructure buildJsonFromObject (LayoutItem root) {
        LayoutJsonStructure jsonStructure = new LayoutJsonStructure();

        if (root instanceof LayoutColumn) {
            jsonStructure.type = LayoutType.COLUMN;
            jsonStructure.relativeWidth = root.getRelativeWidth();
            jsonStructure.relativeHeight = root.getRelativeHeight();
            Array<LayoutItem> rows = ((LayoutColumn) root).getRows();
            for (LayoutItem row : rows) {
                LayoutJsonStructure child = buildJsonFromObject(row);
                jsonStructure.children.add(child);
            }
        } else if (root instanceof LayoutRow) {
            jsonStructure.type = LayoutType.ROW;
            jsonStructure.relativeWidth = root.getRelativeWidth();
            jsonStructure.relativeHeight = root.getRelativeHeight();
            Array<LayoutItem> columns = ((LayoutRow) root).getColumns();
            for (LayoutItem column : columns) {
                LayoutJsonStructure child = buildJsonFromObject(column);
                jsonStructure.children.add(child);
            }
        } else if (root instanceof LayoutContent) {
            jsonStructure.type = LayoutType.CONTENT;
            jsonStructure.relativeWidth = root.getRelativeWidth();
            jsonStructure.relativeHeight = root.getRelativeHeight();
            ObjectMap<String, LayoutApp> apps = ((LayoutContent) root).getApps();

            for (ObjectMap.Entry<String, LayoutApp> app : apps) {
                LayoutJsonStructure child = new LayoutJsonStructure();
                child.type = LayoutType.APP;
                child.appID = app.key;
                child.tabActive = app.value.isTabActive();
                AppManager.BaseApp<?> appy = SharedResources.appManager.getAppForLayoutApp(app.value);
                if (appy != null) {
                    child.baseAppClazz = appy.getClass().getSimpleName();
                    child.gameAssetIdentifier = appy.getAssetIdentifier();

                    RawAsset first = appy.getGameAsset().getRootRawAsset();
                    String uuid = first.metaData.uuid.toString();
                    child.gameAssetUniqueIdentifier = uuid;
                    child.gameAssetType = appy.getGameAssetType();
                }
                jsonStructure.children.add(child);
            }
        }

        return jsonStructure;
    }

}
