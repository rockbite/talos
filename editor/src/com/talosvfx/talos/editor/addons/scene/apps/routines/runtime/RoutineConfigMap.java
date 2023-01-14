package com.talosvfx.talos.editor.addons.scene.apps.routines.runtime;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader;

public class RoutineConfigMap {

    ObjectMap<String, XmlReader.Element> map = new ObjectMap<>();

    public void loadFrom(FileHandle handle) {
        XmlReader xmlReader = new XmlReader();
        XmlReader.Element root = xmlReader.parse(handle);

        Array<XmlReader.Element> categories = root.getChildrenByName("category");

        for(XmlReader.Element category: categories) {
            Array<XmlReader.Element> modules = category.getChildrenByName("module");
            for(XmlReader.Element module: modules) {
                String name = module.getAttribute("name");

                map.put(name, module);
            }
        }
    }

    public XmlReader.Element getConfig(String name) {
        return map.get(name);
    }
}
