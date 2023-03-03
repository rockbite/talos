package com.talosvfx.talos.editor.project2;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader;
import com.talosvfx.talos.editor.widgets.ui.ModuleListPopup;
import com.talosvfx.talos.editor.wrappers.WrapperRegistry;
import lombok.Getter;

public class TalosVFXUtils {

	public static ObjectMap<Class, String> moduleNames = new ObjectMap<>();

	@Getter
	private static ModuleListPopup moduleListPopup;

	public static void init () {
		FileHandle list = Gdx.files.internal("modules.xml");
		XmlReader xmlReader = new XmlReader();
		XmlReader.Element root = xmlReader.parse(list);
		WrapperRegistry.map.clear();
		moduleListPopup = new ModuleListPopup(root);
	}
}
