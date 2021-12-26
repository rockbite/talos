package com.talosvfx.talos.editor.addons.scene.widgets;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader;
import com.kotcrab.vis.ui.util.ActorUtils;
import com.kotcrab.vis.ui.widget.VisWindow;
import com.talosvfx.talos.editor.widgets.ui.FilteredTree;
import com.talosvfx.talos.editor.widgets.ui.SearchFilteredTree;

public class TemplateListPopup extends VisWindow {

    private InputListener stageListener;
    FilteredTree<String> tree;
    SearchFilteredTree<String> searchFilteredTree;

    Vector2 createLocation = new Vector2();

    private ObjectMap<String, XmlReader.Element> configurationMap = new ObjectMap<>();
    public String componentClassPath;

    public interface ListListener {
        void chosen(XmlReader.Element template, float x, float y);
    }

    private ListListener listListener;

    public void setListener(ListListener listener) {listListener = listener;
    }

    public TemplateListPopup (XmlReader.Element root) {
        super("Game Object", "module-list");

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

        componentClassPath = root.getAttribute("componentClassPath");
        traverseTree(tree, null, root);

        add(searchFilteredTree).width(300).row();
        add().growY();
        invalidate(); pack();

        createListeners();
    }

    private void traverseTree (FilteredTree<String> tree, Object o, XmlReader.Element root) {

        Array<XmlReader.Element> templates = root.getChildrenByName("template");

        for(XmlReader.Element template: templates) {
            configurationMap.put(template.getAttribute("name"), template);

            FilteredTree.Node node = new FilteredTree.Node(template.getAttribute("name"), new Label(template.getAttribute("title"), getSkin()));
            tree.add(node);
        }

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
                if (!TemplateListPopup.this.contains(x, y) && button == 0) {
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

                    String name = node.getName();
                    XmlReader.Element template = configurationMap.get(name);

                    listListener.chosen(template, createLocation.x, createLocation.y);

                    remove();
                }
            }

            @Override
            public void selected(FilteredTree.Node node) {

            }
        });
    }

    public void showPopup(Stage stage, Vector2 location, Vector2 createLocation) {
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

        this.createLocation.set(createLocation);
    }

    @Override
    public boolean remove () {
        if (getStage() != null) getStage().removeListener(stageListener);
        return super.remove();
    }
}
