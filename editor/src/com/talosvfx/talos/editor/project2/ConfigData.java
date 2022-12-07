package com.talosvfx.talos.editor.project2;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader;
import lombok.Getter;

public class ConfigData {

	@Getter
	private ObjectMap<String, XmlReader.Element> gameObjectConfigurationMap = new ObjectMap<>();

	@Getter
	private String componentClassPath;

	@Getter
	private XmlReader.Element gameObjectConfigurationXMLRoot;

	public ConfigData () {
		loadTemplateConfigData();
	}

	private void loadTemplateConfigData () {
		FileHandle list = Gdx.files.internal("addons/scene/go-templates.xml");

		XmlReader xmlReader = new XmlReader();
		gameObjectConfigurationXMLRoot = xmlReader.parse(list);

		componentClassPath = gameObjectConfigurationXMLRoot.getAttribute("componentClassPath");


		traverseTree(gameObjectConfigurationXMLRoot.getChildByName("templates"));
	}
	private void traverseTree (XmlReader.Element root) {
		Array<XmlReader.Element> templates = root.getChildrenByName("template");

		for(XmlReader.Element template: templates) {
			gameObjectConfigurationMap.put(template.getAttribute("name"), template);
//			boolean hidden = template.getBooleanAttribute("hidden", false);
		}

	}
}
