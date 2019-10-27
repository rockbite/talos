/*******************************************************************************
 * Copyright 2019 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.rockbite.tools.talos.editor.wrappers;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTextField;
import com.rockbite.tools.talos.TalosMain;
import com.rockbite.tools.talos.editor.widgets.ui.DragPoint;
import com.rockbite.tools.talos.editor.widgets.ui.PreviewWidget;
import com.rockbite.tools.talos.runtime.modules.Vector2Module;

public class Vector2ModuleWrapper extends ModuleWrapper<Vector2Module> implements IDragPointProvider {

	private VisTextField xField;
	private VisTextField yField;

	private DragPoint dragPoint;

	@Override
	public void setModule(Vector2Module module) {
		super.setModule(module);
		xField.setText(module.getDefaultX() + "");
		yField.setText(module.getDefaultY() + "");
	}

	@Override
	protected void configureSlots () {

		xField = addInputSlotWithTextField("X: ", 0);
		yField = addInputSlotWithTextField("Y: ", 1);

		xField.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				float x = floatFromText(xField);
				module.setX(x);
			}
		});

		yField.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				float y = floatFromText(yField);
				module.setY(y);
			}
		});

		dragPoint = new DragPoint(0, 0);

		addOutputSlot("position", 0);
	}

	@Override
	protected void wrapperSelected() {
		PreviewWidget previewWidget = TalosMain.Instance().UIStage().PreviewWidget();
		previewWidget.registerForDragPoints(this);
	}

	@Override
	protected void wrapperDeselected() {
		PreviewWidget previewWidget = TalosMain.Instance().UIStage().PreviewWidget();
		previewWidget.unregisterDragPoints(this);
	}

	@Override
	protected float reportPrefWidth () {
		return 210;
	}

	@Override
	public void read (Json json, JsonValue jsonData) {
		super.read(json, jsonData);
		xField.setText(module.getDefaultX() + "");
		yField.setText(module.getDefaultY() + "");
		dragPoint.set(module.getDefaultX(), module.getDefaultY());
	}

	@Override
	public DragPoint[] fetchDragPoints() {
		return new DragPoint[]{dragPoint};
	}

	@Override
	public void dragPointChanged(DragPoint point) {
		module.setX(point.position.x);
		module.setY(point.position.y);
		xField.setText(module.getDefaultX() + "");
		yField.setText(module.getDefaultY() + "");
	}
}
