package com.rockbite.tools.talos.editor.widgets.propertyWidgets;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Selection;
import com.badlogic.gdx.utils.*;
import com.kotcrab.vis.ui.widget.CollapsibleWidget;
import com.rockbite.tools.talos.TalosMain;
import com.rockbite.tools.talos.editor.addons.bvb.AttachmentPoint;
import com.rockbite.tools.talos.editor.wrappers.MutableProperty;

public class GlobalValueWidget extends PropertyWidget<Array<AttachmentPoint>> {

	CollapsibleWidget collapsibleWidget;
	Label titleLabel;
	Button collapseButton;
	Button addRowButton;
	Button deleteRowButton;
	private ListWithCustomRows<GlobalValueRowWidget> globalValueList;

	Array<GlobalValueRowWidget> currentActiveValues = new Array<>();
	Selection<GlobalValueWidget> currentSelection = new Selection<>();
	IntMap<GlobalValueRowWidget> localItems = new IntMap<>();

	Table topTable;
	Table mainTable;

	@Override
	protected void refresh () {

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
		AttachmentPoint attachmentPoint = bondedProperty.getValue().get(nextAvailableIndex);
		GlobalValueRowWidget value = new GlobalValueRowWidget(GlobalValueWidget.this, attachmentPoint);
		globalValueList.addItem(value);
		localItems.put(nextAvailableIndex, value);
	}

	private void removeRow (GlobalValueRowWidget row) {
		localItems.remove(row.getIndex());
		globalValueList.removeSelected();
	}

	public int getNextAvailableIndex () {
		return 0;
	}

	private boolean hasAvailableGlobalPoint () {
		return localItems.size != bondedProperty.getValue().size;
	}

	private int getMaximumIndex () {
		int max = 0;
		for (AttachmentPoint key : bondedProperty.getValue()) {
			if (key.getSlotId() > max) {
				max = key.getSlotId();
			}
		}

		return max;
	}

	public void askForNewPlace () {

	}
}
