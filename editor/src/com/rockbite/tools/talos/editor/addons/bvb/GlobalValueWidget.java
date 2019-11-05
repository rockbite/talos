package com.rockbite.tools.talos.editor.addons.bvb;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.*;
import com.kotcrab.vis.ui.widget.CollapsibleWidget;
import com.rockbite.tools.talos.TalosMain;
import com.rockbite.tools.talos.editor.widgets.propertyWidgets.*;

public abstract class GlobalValueWidget extends PropertyWidget<Array<AttachmentPoint>> {

	public GlobalValueWidget(String name) {
		super(name);
	}
}
