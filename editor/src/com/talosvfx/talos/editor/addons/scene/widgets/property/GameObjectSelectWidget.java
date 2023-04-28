package com.talosvfx.talos.editor.addons.scene.widgets.property;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Predicate;
import com.talosvfx.talos.editor.addons.scene.logic.IPropertyHolder;
import com.talosvfx.talos.editor.addons.scene.widgets.GameObjectListPopup;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.widgets.propertyWidgets.PropertyWidget;
import com.talosvfx.talos.editor.widgets.ui.FilteredTree;
import com.talosvfx.talos.editor.widgets.ui.common.SquareButton;
import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.runtime.scene.GameObjectContainer;
import com.talosvfx.talos.runtime.scene.SavableContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

public class GameObjectSelectWidget extends PropertyWidget<GameObject> {

	private static final Logger logger = LoggerFactory.getLogger(GameObjectSelectWidget.class);

	private Label nameLabel;
	private GameObject gameObject;

	private Predicate<FilteredTree.Node<GameObject>> filter;
	protected GameObjectSelectWidget () {}

	public GameObjectSelectWidget (String name, Supplier<GameObject> supplier, ValueChanged<GameObject> valueChanged, Object parent) {
		super(name, supplier, valueChanged, parent);
		this.filter = new Predicate<FilteredTree.Node<GameObject>>() {
			@Override
			public boolean evaluate (FilteredTree.Node<GameObject> node) {
				return true;
			}
		};
	}

	@Override
	public PropertyWidget clone () {
		GameObjectSelectWidget clone = (GameObjectSelectWidget)super.clone();
		clone.filter = filter;
		return clone;
	}

	@Override
	public GameObject getValue () {
		return gameObject;
	}

	@Override
	public Actor getSubWidget () {
		Table table = new Table();
		Skin skin = SharedResources.skin;
		final SquareButton button = new SquareButton(skin, skin.getDrawable("ic-file-edit"), "Select game object");

		nameLabel = new Label("", skin);
		nameLabel.setEllipsis(true);
		nameLabel.setAlignment(Align.right);

		table.right();
		table.add(nameLabel).growX().maxWidth(130).padRight(2);
		table.add(button);

		button.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				Vector2 pos = new Vector2(button.getWidth() / 2f, button.getHeight() / 2f);
				button.localToStageCoordinates(pos);

				GameObjectListPopup gameObjectListPopup = new GameObjectListPopup();
				IPropertyHolder currentHolder = GameObjectSelectWidget.this.topLevelPropertiesPanel.getCurrentHolder();

				GameObjectContainer rootGO = null;
				if (currentHolder instanceof GameObject) {
					rootGO = ((GameObject)currentHolder).getGameObjectContainerRoot();
				} else if (currentHolder instanceof SavableContainer) {
					rootGO = (GameObjectContainer)currentHolder;
				}

				gameObjectListPopup.showPopup(getStage(), rootGO.getSelfObject(), pos, filter, new FilteredTree.ItemListener<GameObject>() {

					@Override
					public void selected (FilteredTree.Node<GameObject> node) {
						GameObject gameObject = node.getObject();

						updateWidget(gameObject);
						callValueChanged(gameObject);
						gameObjectListPopup.remove();
					}
				});

			}
		});

		return table;
	}

	@Override
	public void updateWidget (GameObject value) {
		if (value == null) {
			nameLabel.setText("No Game Object");
			gameObject = null;
			return;
		}

		this.gameObject = value;
		this.nameLabel.setText(gameObject.getName());
	}

}
