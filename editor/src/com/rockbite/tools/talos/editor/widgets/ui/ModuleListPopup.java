package com.rockbite.tools.talos.editor.widgets.ui;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Selection;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.kotcrab.vis.ui.FocusManager;
import com.kotcrab.vis.ui.util.ActorUtils;
import com.kotcrab.vis.ui.widget.VisWindow;
import com.rockbite.tools.talos.TalosMain;
import com.rockbite.tools.talos.editor.wrappers.EmitterModuleWrapper;
import com.rockbite.tools.talos.editor.wrappers.WrapperRegistry;
import com.rockbite.tools.talos.runtime.ParticleEmitterDescriptor;
import com.rockbite.tools.talos.runtime.modules.EmitterModule;


public class ModuleListPopup extends VisWindow {

    private InputListener stageListener;
    FilteredTree<String> tree;
    SearchFilteredTree<String> searchFilteredTree;

    Vector2 createLocation = new Vector2();

    private ObjectMap<String, String> nameToModuleClass = new ObjectMap<>();

    public ModuleListPopup(XmlReader.Element root) {
        super("Add Module", "module-list");
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
                if (!ModuleListPopup.this.contains(x, y) && button == 0) {
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
                    try {
                        Class clazz = ClassReflection.forName("com.rockbite.tools.talos.runtime.modules." + nameToModuleClass.get(node.name));
                        if(WrapperRegistry.map.containsKey(clazz)) {
                            TalosMain.Instance().NodeStage().moduleBoardWidget.createModule(clazz, createLocation.x, createLocation.y);
                            remove();
                        }
                    } catch (ReflectionException e) {
                    }
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
            FilteredTree.Node categoryNode = new FilteredTree.Node(category.getAttribute("name"), new Label(category.getAttribute("name"), getSkin()));

            if(parent != null) parent.add(categoryNode);
            else tree.add(categoryNode);

            parseCategory(tree, categoryNode, category);
        }

        // get modules
        Array<XmlReader.Element> modules = element.getChildrenByName("module");
        for(XmlReader.Element module: modules) {
            FilteredTree.Node node = new FilteredTree.Node(module.getAttribute("name"), new Label(module.getAttribute("name"), getSkin()));
            nameToModuleClass.put(module.getAttribute("name"), module.getText());

            registerModule(module);

            if(parent != null) parent.add(node);
            else tree.add(node);
        }
    }

    private void registerModule(XmlReader.Element module) {
        try {
            Class moduleClazz = ClassReflection.forName("com.rockbite.tools.talos.runtime.modules." + module.getText());
            Class wrapperClazz =ClassReflection.forName("com.rockbite.tools.talos.editor.wrappers." + module.getAttribute("wrapper"));
            WrapperRegistry.reg(moduleClazz, wrapperClazz);
        } catch (ReflectionException e) {
            e.printStackTrace();
        }

        WrapperRegistry.reg(EmitterModule.class, EmitterModuleWrapper.class);
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

        createLocation.set(location);
    }

    @Override
    public boolean remove () {
        TalosMain.Instance().NodeStage().moduleBoardWidget.clearCC();
        if (getStage() != null) getStage().removeListener(stageListener);
        return super.remove();
    }
}
