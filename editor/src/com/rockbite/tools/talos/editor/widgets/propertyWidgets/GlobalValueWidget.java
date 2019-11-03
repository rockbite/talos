package com.rockbite.tools.talos.editor.widgets.propertyWidgets;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Selection;
import com.badlogic.gdx.utils.*;
import com.esotericsoftware.spine.Bone;
import com.kotcrab.vis.ui.widget.CollapsibleWidget;
import com.rockbite.tools.talos.TalosMain;
import com.rockbite.tools.talos.editor.addons.bvb.AttachmentPoint;
import com.rockbite.tools.talos.editor.wrappers.MutableProperty;
import com.rockbite.tools.talos.editor.wrappers.Property;

public class GlobalValueWidget extends PropertyWidget<Array<AttachmentPoint>> {

	CollapsibleWidget collapsibleWidget;
	Label titleLabel;
	Button collapseButton;
	Button addRowButton;
	Button deleteRowButton;

	private ListWithCustomRows<GlobalValueRowWidget> globalValueList;
	private Array<AttachmentPoint> tempArray = new Array<>();
	private Array<String> boneNames = new Array<>();

	Table topTable;
	Table mainTable;

	@Override
	public void refresh () {

	}

	public GlobalValueWidget () {
		super();

		Skin skin = TalosMain.Instance().getSkin();
		topTable = new Table();
		titleLabel = new Label("attachments", skin);
		collapseButton = new ImageButton(skin.getDrawable("panel-collapse-down"));
		topTable.add(collapseButton);
		titleLabel.setAlignment(Align.left);
		topTable.add(titleLabel).expandX().left().padLeft(10);

		mainTable = new Table();
		mainTable.setFillParent(true);
		mainTable.setBackground(skin.getDrawable("panel_button_bg"));

		globalValueList = new ListWithCustomRows<>(skin);
		globalValueList.setBackground(skin.getDrawable("panel_input_bg"));
		addRowButton = new ImageButton(skin.getDrawable("ic-input-file-add"));
		deleteRowButton = new ImageButton(skin.getDrawable("ic-input-file-delete"));
		mainTable.add(globalValueList).grow();

		mainTable.row();
		Table bottomButtons = new Table();
		bottomButtons.add(addRowButton).left();
		bottomButtons.add(deleteRowButton).left();
		mainTable.add(bottomButtons).left();

		collapsibleWidget = new CollapsibleWidget(mainTable, false);
		add(topTable).growX();
		row();
		add(collapsibleWidget).grow().padTop(10);

		addListeners();
	}

	private void addListeners () {
		collapseButton.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				boolean checked = collapseButton.isChecked();
				Skin skin = TalosMain.Instance().getSkin();
				collapseButton.setBackground(checked ? skin.getDrawable("panel-collapse-down") : skin.getDrawable("panel-collapse-right"));
				collapsibleWidget.setCollapsed(checked);
			}
		});

		addRowButton.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				addNewRow();
			}
		});

		deleteRowButton.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				globalValueList.removeSelected();
			}
		});
	}

	private void addNewRow () {
		int nextAvailableIndex = getNextAvailableIndex();
		GlobalValueRowWidget value = new GlobalValueRowWidget(GlobalValueWidget.this, nextAvailableIndex);
		globalValueList.addItem(value);

		refreshChanges();
	}

	public int getNextAvailableIndex () {
		int maximumIndex = getMaximumIndex();
		for (int i = 0; i <= maximumIndex; i++) {
			if (!hasItemWithIndex(i)) {
				return i;
			}
		}

		return maximumIndex + 1;
	}

	private int getMaximumIndex () {
		int max = 0;
		for (GlobalValueRowWidget item : globalValueList.getItems()) {
			if (item.getIndex() > max) {
				max = item.getIndex();
			}
		}
		return max;
	}

	private boolean hasItemWithIndex (int index) {
		for (GlobalValueRowWidget item : globalValueList.getItems()) {
			if (item.getIndex() == index) {
				return true;
			}
		}

		return false;
	}

	public void askForNewPlace (GlobalValueRowWidget widget) {
		int nextAvailableIndex = getNextAvailableIndex();
		widget.setIndex(nextAvailableIndex);
		refreshChanges();
	}

	@Override
	public void configureForProperty (Property property) {
		super.configureForProperty(property);
		Array<Bone> bones = (Array<Bone>) property.getAdditionalProperty("boneNames");
		for (Bone bone : bones) {
			boneNames.add(bone.toString());
		}
	}

	private void refreshChanges () {
		Array<GlobalValueRowWidget> widgets = globalValueList.getItems();
		tempArray.clear();

		for (GlobalValueRowWidget widget : widgets) {
			AttachmentPoint attachmentPoint = new AttachmentPoint();

			widget.exportTo(attachmentPoint);
			tempArray.add(attachmentPoint);
		}

		((MutableProperty<Array<AttachmentPoint>>) bondedProperty).changed(tempArray);
	}

	public Array<String> getBoneNames () {
		return boneNames;
	}
}
