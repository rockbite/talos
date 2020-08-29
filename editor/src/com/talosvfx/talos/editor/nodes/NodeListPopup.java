package com.talosvfx.talos.editor.nodes;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.kotcrab.vis.ui.util.ActorUtils;
import com.kotcrab.vis.ui.widget.VisWindow;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.widgets.ui.FilteredTree;
import com.talosvfx.talos.editor.widgets.ui.SearchFilteredTree;

public class NodeListPopup extends VisWindow {

    private InputListener stageListener;
    FilteredTree<String> tree;
    SearchFilteredTree<String> searchFilteredTree;

    Vector2 createLocation = new Vector2();

    private ObjectMap<String, String> nameToNodeClass = new ObjectMap<>();
    private ObjectMap<Class, XmlReader.Element> registry = new ObjectMap<>();

    private String classPath;

    public Class getNodeClassByName (String className) {
        Class nodeClazz = null;
        try {
            nodeClazz = ClassReflection.forName(classPath + "." + className);
        } catch (ReflectionException e) {
            e.printStackTrace();
        }

        return nodeClazz;
    }

    public XmlReader.Element getModuleByClassName (String className) {
        return registry.get(getNodeClassByName(className));
    }

    interface NodeListListener {
        void chosen(Class clazz, XmlReader.Element module, float x, float y);
    }

    private NodeListListener nodeListListener;

    public void setListener(NodeListListener listener) {
        nodeListListener = listener;
    }

    public NodeListPopup (XmlReader.Element root) {
        super("Add Node", "module-list");

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

        TalosMain.Instance().moduleNames.clear();

        classPath = root.getAttribute("classPath");
        parseCategory(tree, null, root);

        add(searchFilteredTree).width(300).row();
        add().growY();

        invalidate(); pack();

        createListeners();
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
                if (!NodeListPopup.this.contains(x, y) && button == 0) {
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
                    String nodeClazzName = nameToNodeClass.get(node.name);

                    if(nodeListListener != null) {
                        try {
                            Class clazz = ClassReflection.forName(classPath + "." + nodeClazzName);
                            nodeListListener.chosen(clazz, registry.get(clazz), createLocation.x, createLocation.y);
                        } catch (ReflectionException e) {
                            e.printStackTrace();
                        }
                    }

                    remove();
                }
            }

            @Override
            public void selected(FilteredTree.Node node) {

            }
        });
    }

    private void parseCategory(FilteredTree<String> tree, FilteredTree.Node parent, XmlReader.Element element) {
        Array<XmlReader.Element> categories = element.getChildrenByName("category");
        for(XmlReader.Element category: categories) {
            FilteredTree.Node categoryNode = new FilteredTree.Node(category.getAttribute("title"), new Label(category.getAttribute("title"), getSkin()));

            if(parent != null) parent.add(categoryNode);
            else tree.add(categoryNode);

            parseCategory(tree, categoryNode, category);
        }

        // get modules
        Array<XmlReader.Element> modules = element.getChildrenByName("module");
        for(XmlReader.Element module: modules) {
            FilteredTree.Node node = new FilteredTree.Node(module.getAttribute("title"), new Label(module.getAttribute("title"), getSkin()));
            nameToNodeClass.put(module.getAttribute("title"), module.getAttribute("name"));

            registerNode(module);

            if(parent != null) parent.add(node);
            else tree.add(node);
        }
    }

    private void registerNode(XmlReader.Element module) {
        try {
            Class nodeClazz = ClassReflection.forName(classPath + "." + module.getAttribute("name"));
            registry.put(nodeClazz, module);
        } catch (ReflectionException e) {
            e.printStackTrace();
        }
    }

    public void showPopup(Stage stage, Vector2 location) {
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

        createLocation.set(location);
    }

    @Override
    public boolean remove () {
        if (getStage() != null) getStage().removeListener(stageListener);
        return super.remove();
    }

    public XmlReader.Element getConfigFor (Class clazz) {
        return registry.get(clazz);
    }
}
