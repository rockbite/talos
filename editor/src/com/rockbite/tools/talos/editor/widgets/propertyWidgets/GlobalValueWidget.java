package com.rockbite.tools.talos.editor.widgets.propertyWidgets;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Selection;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.ObjectMap;
import com.kotcrab.vis.ui.widget.CollapsibleWidget;
import com.rockbite.tools.talos.TalosMain;
import com.rockbite.tools.talos.editor.addons.bvb.AttachmentPoint;
import com.rockbite.tools.talos.runtime.values.NumericalValue;

public class GlobalValueWidget extends PropertyWidget<ObjectMap<Integer, AttachmentPoint>> {

	CollapsibleWidget collapsibleWidget;
	Label titleLabel;
	Button collapseButton;
	Button addRowButton;
	Button deleteRowButton;
	private ListWithCustomRows<GlobalValueRowWidget> globalValueList;

	int index;

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
		debugAll();

		Skin skin = TalosMain.Instance().getSkin();
		topTable = new Table();
		titleLabel = new Label("COLLAPSE THIS SHIT", skin);
		collapseButton = new ImageButton(skin.getDrawable("ic-down"));
		topTable.add(titleLabel).expandX();
		topTable.add(collapseButton);

		mainTable = new Table();
		globalValueList = new ListWithCustomRows<>(skin);
		addRowButton = new ImageButton(skin.getDrawable("ic-folder-aster"));
		deleteRowButton = new ImageButton(skin.getDrawable("ic-file-delete"));
		mainTable.add(globalValueList).grow();

		mainTable.row();
		Table bottomButtons = new Table();
		bottomButtons.add(addRowButton).left();
		bottomButtons.add(deleteRowButton).left();
		mainTable.add(bottomButtons).left();

		collapsibleWidget = new CollapsibleWidget(mainTable, false);
		add(topTable).growX();
		row();
		add(collapsibleWidget).grow();

		addListeners();
	}

	private void addListeners () {
		collapseButton.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				collapsibleWidget.setCollapsed(collapseButton.isChecked());
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
		if (hasAvailableGlobalPoint()) {
			int nextAvailableIndex = getNextAvailableIndex();
			GlobalValueRowWidget value = new GlobalValueRowWidget(GlobalValueWidget.this, nextAvailableIndex);
			globalValueList.addItem(value);
			localItems.put(nextAvailableIndex, value);
		}
	}

	private void removeRow (GlobalValueRowWidget row) {
		localItems.remove(row.getIndex());
		globalValueList.removeSelected();
	}

	public int getNextAvailableIndex () {
		ObjectMap<Integer, AttachmentPoint> value = bondedProperty.getValue();
		int maximumIndex = getMaximumIndex();
		for (int i = 0; i <= maximumIndex; i++) {
			if (value.get(i) != null && localItems.get(i) == null) {
				return i;
			}
		}

		throw new GdxRuntimeException("Make Sure there is available binding point");
	}

	private boolean hasAvailableGlobalPoint () {
		return localItems.size != bondedProperty.getValue().size;
	}

	private int getMaximumIndex () {
		int max = 0;
		for (Integer key : bondedProperty.getValue().keys()) {
			if (key > max) {
				max = key;
			}
		}

		return max;
	}

}
