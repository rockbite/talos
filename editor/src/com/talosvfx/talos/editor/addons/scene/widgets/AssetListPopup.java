package com.talosvfx.talos.editor.addons.scene.widgets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader;
import com.kotcrab.vis.ui.util.ActorUtils;
import com.kotcrab.vis.ui.widget.VisWindow;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.scene.SceneEditorAddon;
import com.talosvfx.talos.editor.widgets.ui.EditableLabel;
import com.talosvfx.talos.editor.widgets.ui.FilteredTree;
import com.talosvfx.talos.editor.widgets.ui.SearchFilteredTree;

import java.io.File;

public class AssetListPopup extends VisWindow {

    private InputListener stageListener;
    FilteredTree<String> tree;
    SearchFilteredTree<String> searchFilteredTree;

    private ObjectMap<String, XmlReader.Element> configurationMap = new ObjectMap<>();
    private FilteredTree.Node rootNode;

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

        add(searchFilteredTree).width(300).height(200).row();
        invalidate(); pack();

        createListeners();
    }

    private void loadTree() {
        String rootPath = SceneEditorAddon.get().workspace.getProjectPath() + File.separator + "assets";
        FileHandle rootHandle = Gdx.files.absolute(rootPath);
        tree.clearChildren();
        FileHandle root = Gdx.files.absolute(rootPath);

        rootNode = new FilteredTree.Node(rootPath,  new Label(rootHandle.name(), TalosMain.Instance().getSkin()));
        rootNode.setObject(rootPath);
        tree.add(rootNode);

        traversePath(root, 0, 10, rootNode);

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

        tree.setItemListener(new FilteredTree.ItemListener() {
            @Override
            public void chosen(FilteredTree.Node node) {
                if(node.children.size == 0) {
                    // do shit
                }
            }

            @Override
            public void selected(FilteredTree.Node node) {

            }
        });
    }

    public void showPopup(Stage stage, Vector2 location, String filter, FilteredTree.ItemListener listener) {
        loadTree();

        setPosition(location.x, location.y - getHeight());
        if (stage.getHeight() - getY() > stage.getHeight()) setY(getY() + getHeight());
        ActorUtils.keepWithinStage(stage, this);
        stage.addActor(this);

        searchFilteredTree.reset();
        getStage().setKeyboardFocus(searchFilteredTree.textField);
        getStage().setScrollFocus(searchFilteredTree.scrollPane);
        tree.collapseAll();

        if(getHeight() < 200) {
            setHeight(200);
        }

        if(filter != null) {
            tree.filter("." + filter, true);
        }

        tree.expandAll();

        tree.setItemListener(listener);
    }

    @Override
    public boolean remove () {
        if (getStage() != null) getStage().removeListener(stageListener);
        tree.setItemListener(null);
        return super.remove();
    }

    private void traversePath(FileHandle path, int currDepth, int maxDepth, FilteredTree.Node node) {
        if(path.isDirectory() && currDepth <= maxDepth) {
            FileHandle[] list = path.list(ProjectExplorerWidget.fileFilter);
            for(int i = 0; i < list.length; i++) {
                FileHandle listItemHandle = list[i];

                ProjectExplorerWidget.RowWidget widget = new ProjectExplorerWidget.RowWidget(listItemHandle, false);
                EditableLabel label = widget.getLabel();
                final FilteredTree.Node newNode = new FilteredTree.Node(listItemHandle.path(),  widget);
                newNode.setObject(listItemHandle.path());
                node.add(newNode);
                if(listItemHandle.isDirectory()) {
                    traversePath(list[i], currDepth++, maxDepth, newNode);
                }
            }
        }
    }
}
