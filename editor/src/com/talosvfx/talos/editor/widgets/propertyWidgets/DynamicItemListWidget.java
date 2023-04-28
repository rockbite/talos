package com.talosvfx.talos.editor.widgets.propertyWidgets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;
import com.badlogic.gdx.scenes.scene2d.utils.Selection;
import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.scene.SceneEditorWorkspace;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.widgets.ui.EditableLabel;
import com.talosvfx.talos.editor.widgets.ui.FilteredTree;
import com.talosvfx.talos.editor.widgets.ui.common.ColorLibrary;
import com.talosvfx.talos.editor.widgets.ui.common.SquareButton;

import java.util.function.Supplier;


public class DynamicItemListWidget<T> extends PropertyWidget<Array<T>> {

    private DynamicItemListInteraction<T> interaction;
    public FilteredTree<T> list;
    private boolean draggableInLayerOnly;

    public interface DynamicItemListInteraction<T> {
        Supplier<T> newInstanceCreator ();
        String getID (T t);

        String updateName (T t, String newText);

        void onUpdate();

        void onDeleteNode(T t);
    }
    protected DynamicItemListWidget () {}


    public DynamicItemListWidget (String name, Supplier<Array<T>> supplier, ValueChanged<Array<T>> valueChanged, DynamicItemListInteraction<T> interaction, Object parent) {
        super(name, supplier, valueChanged, parent);
        this.interaction = interaction;
    }

    public DynamicItemListWidget (String name, Supplier<Array<T>> supplier, ValueChanged<Array<T>> valueChanged, Object parent) {
        super(name, supplier, valueChanged, parent);
    }
    public void setInteraction(DynamicItemListInteraction<T> interaction) {
        this.interaction = interaction;
    }

    public void setDraggableInLayerOnly (boolean draggableInLayerOnly) {
        this.draggableInLayerOnly = draggableInLayerOnly;
    }


    @Override
    protected boolean isFullSize () {
        return true;
    }


    @Override
    public Actor getSubWidget () {
        Table table = new Table();

        Skin skin = SharedResources.skin;
        setSkin(skin);

        Table topBar = new Table();
        topBar.setBackground(ColorLibrary.obtainBackground(getSkin(), ColorLibrary.BackgroundColor.DARK_GRAY));
        SquareButton newBtn = new SquareButton(skin, skin.getDrawable("timeline-btn-icon-new"), "New item");
        SquareButton deleteBtn = new SquareButton(skin, skin.getDrawable("timeline-btn-icon-delete"), "Delete item");
        topBar.add().expandX();
        topBar.add(newBtn).padRight(2);
        topBar.add(deleteBtn).padRight(2);

        list = new FilteredTree<>(skin, "modern");
        list.draggable = true;

        list.addItemListener(new FilteredTree.ItemListener<T>() {
            @Override
            public void selected (FilteredTree.Node<T> node) {
                super.selected(node);
                list.getSelection().clear();
                list.getSelection().add(node);
            }

            @Override
            public void onNodeMove (FilteredTree.Node<T> parentToMoveTo, FilteredTree.Node<T> childThatHasMoved, int indexInParent, int indexOfPayloadInPayloadBefore) {
                callValueChanged(makeDataArray());
            }

            @Override
            public void delete (Array<FilteredTree.Node<T>> nodes) {
                deleteSelection();
            }
        });

        table.add(list).growX();
        table.row();
        table.add(topBar).growX().padLeft(-10).padRight(-10);

        newBtn.addListener(new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {

                T newT = interaction.newInstanceCreator().get();
                Selection<FilteredTree.Node<T>> selection = list.getSelection();
                FilteredTree.Node<T> node;
                if(selection.size() > 0) {
                    int index = 0;
                    Array<FilteredTree.Node<T>> rootNodes = list.getRootNodes();
                    for(index = 0; index < rootNodes.size; index++) {
                        if(rootNodes.get(index) == selection.first()) {
                            break;
                        }
                    }
                    node = addNode(newT, index + 1);
                } else {
                    node = addNode(newT);
                }

                callValueChanged(makeDataArray());

                EditableLabel label = (EditableLabel) node.getActor();
                label.setEditMode();
            }
        });

        deleteBtn.addListener(new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {
                deleteSelection();
            }
        });

        return table;
    }

    public boolean canDelete (T t) {
        return true;
    }

    private void deleteSelection () {
        Selection<FilteredTree.Node<T>> selection = list.getSelection();
        if(selection.size() > 0) {
            FilteredTree.Node<T> item = selection.first();
            T t = item.getObject();
            if(canDelete(t)) {
                int index = 0;
                Array<FilteredTree.Node<T>> rootNodes = list.getRootNodes();
                for (index = 0; index < rootNodes.size; index++) {
                    if (rootNodes.get(index) == selection.first()) {
                        break;
                    }
                }
                list.remove(selection.first());
                interaction.onDeleteNode(selection.first().getObject());
                rootNodes = list.getRootNodes();
                if (rootNodes.size > 0) {
                    selection.clear();
                    index = index - 1;
                    if (index < 0) index = 0;
                    selection.add(rootNodes.get(index));
                }
                callValueChanged(makeDataArray());
            }
        }
    }

    private Array<T> makeDataArray () {
        Array<T> arr = new Array<>();
        Array<FilteredTree.Node<T>> rootNodes = list.getRootNodes();
        for(FilteredTree.Node<T> node: rootNodes) {
            arr.add(node.getObject());
        }
        return arr;
    }

    private FilteredTree.Node<T> createNode(T t) {
        Skin skin = SharedResources.skin;
        EditableLabel editableLabel = new EditableLabel(t.toString(), skin);
        editableLabel.setListener(new EditableLabel.EditableLabelChangeListener() {
            @Override
            public void editModeStarted () {

            }

            @Override
            public void changed (String newText) {
                String checkedName = interaction.updateName(t, newText);
                editableLabel.setText(checkedName);
                callValueChanged(makeDataArray());
            }
        });
        editableLabel.addListener(new FocusListener() {
            @Override
            public void keyboardFocusChanged (FocusEvent event, Actor actor, boolean focused) {
                super.keyboardFocusChanged(event, actor, focused);
                if (!focused) {
                    if (editableLabel.isEditMode()) {
                        Gdx.app.postRunnable(new Runnable() {
                            @Override
                            public void run () {
                                editableLabel.finishTextEdit(true);

                            }
                        });
                    }
                }
            }
        });

        FilteredTree.Node<T> node = new FilteredTree.Node<T>(interaction.getID(t), editableLabel);
        node.draggable = true;
        node.draggableInLayerOnly = draggableInLayerOnly;
        node.setObject(t);

        return node;
    }

    private FilteredTree.Node<T> addNode(T t, int index) {
        FilteredTree.Node<T> node = createNode(t);
        list.insert(index, node);

        return node;
    }

    private FilteredTree.Node<T> addNode(T t) {
        FilteredTree.Node<T> node = createNode(t);
        list.add(node);

        return node;
    }

    @Override
    public void updateWidget (Array<T> value) {
        list.clearChildren();
        for(T item: value) {
            addNode(item);
        }
        interaction.onUpdate();
    }


}
