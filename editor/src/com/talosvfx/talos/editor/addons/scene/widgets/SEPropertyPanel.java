package com.talosvfx.talos.editor.addons.scene.widgets;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.XmlReader;
import com.kotcrab.vis.ui.util.ActorUtils;
import com.kotcrab.vis.ui.widget.VisWindow;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.scene.SceneEditorAddon;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.logic.IPropertyHolder;
import com.talosvfx.talos.editor.addons.scene.logic.components.ScriptComponent;
import com.talosvfx.talos.editor.addons.scene.utils.importers.AssetImporter;
import com.talosvfx.talos.editor.widgets.propertyWidgets.IPropertyProvider;
import com.talosvfx.talos.editor.widgets.ui.FilteredTree;
import com.talosvfx.talos.editor.widgets.ui.SearchFilteredTree;
import com.talosvfx.talos.editor.widgets.ui.common.SquareButton;

public class SEPropertyPanel extends PropertyPanel{

    public SEPropertyPanel() {
        super();
    }

    @Override
    public void showPanel (IPropertyHolder target, Iterable<IPropertyProvider> propertyProviders) {
        super.showPanel(target, propertyProviders);

        if(target instanceof GameObject) {
            // add part with custom components

            container.row();

            Table table = new Table();
            Label label = new Label("Add Component", TalosMain.Instance().getSkin());
            SquareButton button = new SquareButton(TalosMain.Instance().getSkin(), label, "Add Component to entity");

            button.addListener(new ClickListener() {
                @Override
                public void clicked (InputEvent event, float x, float y) {
                    Popup popup = new Popup();
                    popup.show(button, (GameObject)target);
                }
            });

            table.add(button).height(30).growX();

            container.add(button).pad(10).growX();
        }
    }

    private class Popup extends VisWindow {

        private final FilteredTree<Object> tree;
        private InputListener stageListener;

        private GameObject gameObject;

        public Popup() {
            super("New Component", "module-list");

            setModal(false);
            setMovable(false);
            setKeepWithinParent(false);
            setKeepWithinStage(false);

            padTop(42);
            padBottom(16);
            padLeft(16);
            padRight(16);

            tree = new FilteredTree<>(getSkin());
            SearchFilteredTree searchFilteredTree = new SearchFilteredTree<>(getSkin(), tree, null);

            // add list items here
            tree.add(new FilteredTree.Node<>("script", new Label("Script Component", getSkin())));

            add(searchFilteredTree).width(300).row();
            add().growY();
            invalidate(); pack();

            createListeners();
        }

        public void show(Actor source, GameObject gameObject) {
            this.gameObject = gameObject;
            Vector2 tmp = new Vector2();
            source.localToStageCoordinates(tmp);

            Stage stage = TalosMain.Instance().UIStage().getStage();
            setPosition(tmp.x, tmp.y - getHeight());
            if (stage.getHeight() - getY() > stage.getHeight()) setY(getY() + getHeight());
            ActorUtils.keepWithinStage(stage, this);
            stage.addActor(this);
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
                    if (!Popup.this.contains(x, y) && button == 0) {
                        remove();
                        return false;
                    }
                    return false;
                }
            };

            tree.addItemListener(new FilteredTree.ItemListener() {
                @Override
                public void selected (FilteredTree.Node node) {
                    super.selected(node);
                    String name = node.getName();

                    if(name.equals("script")) {
                        if (!gameObject.hasComponent(ScriptComponent.class)) {
                            ScriptComponent scriptComponent = new ScriptComponent();
                            gameObject.addComponent(scriptComponent);
                            ProjectExplorerWidget projectExplorer = SceneEditorAddon.get().projectExplorer;
                            projectExplorer.reload();

                            SceneEditorAddon.get().propertyPanel.notifyPropertyHolderRemoved(gameObject);
                            SceneEditorAddon.get().workspace.selectPropertyHolder(gameObject);
                        } else {
                            System.out.println("Trying to add duplicate");
                        }
                    }

                    remove();
                }
            });
        }
    }
}
