package com.talosvfx.talos.editor.dialogs.preference.widgets;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.XmlReader;
import com.talosvfx.talos.editor.project2.localprefs.TalosLocalPrefs;
import lombok.Getter;

public abstract class APrefWidget extends Table {

    @Getter
    protected String id;
    @Getter
    protected String path;

    protected XmlReader.Element xml;

    public APrefWidget(String parentPath, XmlReader.Element xml) {

        if(xml != null) {
            id = xml.getAttribute("name");
        }
        path = parentPath + "." + id;

        this.xml = xml;

    }

    public void write() {
        String val = writeString();
        TalosLocalPrefs.Instance().setGlobalData(path, val);

        TalosLocalPrefs.Instance().save(); // TODO: this needs changing, too many write operations
    }

    public void read() {
        String str = TalosLocalPrefs.Instance().getGlobalData(path);
        if((str == null || str.isEmpty()) && xml != null) {
            str = xml.getAttribute("default", "0");
        }
        fromString(str);
    }

    protected abstract void fromString(String str);
    protected abstract String writeString();
}
