package com.talosvfx.talos.editor.addons.scene.widgets;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Predicate;
import com.badlogic.gdx.utils.XmlReader;
import com.kotcrab.vis.ui.util.ActorUtils;
import com.kotcrab.vis.ui.widget.VisWindow;
import com.talosvfx.talos.editor.addons.scene.assets.AssetRepository;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.widgets.ui.EditableLabel;
import com.talosvfx.talos.editor.widgets.ui.FilteredTree;
import com.talosvfx.talos.editor.widgets.ui.SearchFilteredTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AssetListPopup<T> extends VisWindow {

    private static final Logger logger = LoggerFactory.getLogger(AssetListPopup.class);

    private InputListener stageListener;
    FilteredTree<GameAsset<T>> tree;
    SearchFilteredTree<GameAsset<T>> searchFilteredTree;

    private ObjectMap<String, XmlReader.Element> configurationMap = new ObjectMap<>();
    private FilteredTree.Node<GameAsset<T>> rootNode;
    private FilteredTree.ItemListener<GameAsset<T>> filterTreeListener;

    private static final float MIN_HEIGHT = 400f;

    public void resetSelection () {
        tree.getSelection().clear();
    }

    public interface ListListener {
        void chosen(XmlReader.Element template, float x, float y);
    }

    private TemplateListPopup.ListListener listListener;

    public void setListener(TemplateListPopup.ListListener listener) {listListener = listener;
    }

    public AssetListPopup () {
        super("Choose Asset", "module-list");

        setModal(false);
        setMovable(false);
        setKeepWithinParent(false);
        setKeepWithinStage(false);

        padTop(42);
        padBottom(16);
        padLeft(16);
        padRight(16);

        tree = new FilteredTree<>(getSkin());
        searchFilteredTree = new SearchFilteredTree<>(getSkin(), tree, null);

        add(searchFilteredTree).width(300).height(MIN_HEIGHT).row();
        invalidate(); pack();

        createListeners();
    }

    private void loadTree(Predicate<FilteredTree.Node<GameAsset<T>>> predicate) {
        FileHandle rootHandle = SharedResources.currentProject.rootProjectDir();
        rootNode = new FilteredTree.Node<>(rootHandle.path(), new Label(rootHandle.name(), SharedResources.skin));

        tree.clearChildren();
        tree.add(rootNode);

        traversePath(rootHandle, 0, 10, rootNode, predicate);
        rootNode.setExpanded(true);
    }

    public boolean contains (float x, float y) {
        return getX() < x && getX() + getWidth() > x && getY() < y && getY() + getHeight() > y;
    }

    @Override
    protected void setStage (Stage stage) {
        super.setStage(stage);
        if (stage != null) stage.addListener(stageListener);
    }

    private void createListeners() {
        stageListener = new InputListener() {
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                if (!AssetListPopup.this.contains(x, y) && button == 0) {
                    remove();
                    return false;
                }
                return false;
            }
        };

        tree.addItemListener(new FilteredTree.ItemListener() {
            @Override
            public void selected(FilteredTree.Node node) {
                if(node.children.size == 0) {
                    // do shit
                }
            }

            @Override
            public void addedIntoSelection (FilteredTree.Node node) {
                super.addedIntoSelection(node);
            }
        });
    }

    public void showPopup(Stage stage, Vector2 location, Predicate<FilteredTree.Node<GameAsset<T>>> filter, FilteredTree.ItemListener<GameAsset<T>> listener) {
        this.filterTreeListener = listener;

        loadTree(filter);

        setPosition(location.x, location.y - getHeight());
        if (stage.getHeight() - getY() > stage.getHeight()) setY(getY() + getHeight());
        ActorUtils.keepWithinStage(stage, this);
        stage.addActor(this);

        searchFilteredTree.reset();
        getStage().setKeyboardFocus(searchFilteredTree.textField);
        getStage().setScrollFocus(searchFilteredTree.scrollPane);
        tree.collapseAll();

        if(getHeight() < MIN_HEIGHT) {
            setHeight(MIN_HEIGHT);
        }

        tree.expandAll();

        tree.addItemListener(listener);
    }

    public void showPopup(Stage stage, Vector2 location, Predicate<FilteredTree.Node<GameAsset<T>>> filter, FilteredTree.ItemListener<GameAsset<T>> listener, @Null GameAsset<T> defaultSelection) {
        this.showPopup(stage, location, filter, listener);

        if (defaultSelection == null) {
            // skip
            return;
        }
        FilteredTree.Node<GameAsset<T>> selectedNode = tree.findNode(defaultSelection);
        if (selectedNode != null) {
            tree.clearSelection(false);
            tree.addNodeToSelection(selectedNode);
        }
    }

    @Override
    public boolean remove () {
        tree.removeItemListener(filterTreeListener);

        if (getStage() != null) getStage().removeListener(stageListener);
        return super.remove();
    }

    private void traversePath(FileHandle path, int currDepth, int maxDepth, FilteredTree.Node<GameAsset<T>> node, Predicate<FilteredTree.Node<GameAsset<T>>> predicate) {
        if(path.isDirectory() && currDepth <= maxDepth) {
            FileHandle[] list = path.list(ProjectExplorerWidget.fileFilter);
            int nextDepth = currDepth++;
            for(int i = 0; i < list.length; i++) {
                FileHandle listItemHandle = list[i];

                ProjectExplorerWidget.RowWidget widget = new ProjectExplorerWidget.RowWidget(listItemHandle, false);
                EditableLabel label = widget.getLabel();
                final FilteredTree.Node<GameAsset<T>> newNode = new FilteredTree.Node<>(listItemHandle.path(), widget);
                GameAsset<T> assetForPath = (GameAsset<T>)AssetRepository.getInstance().getAssetForPath(listItemHandle, false);
                newNode.setObject(assetForPath);
                if (predicate.evaluate(newNode)) {
                    node.add(newNode);
                }
                if(listItemHandle.isDirectory()) {
                    node.add(newNode);
                    traversePath(listItemHandle, nextDepth, maxDepth, newNode, predicate);
                }
            }
        } else {
            System.out.println();
        }
    }
}
