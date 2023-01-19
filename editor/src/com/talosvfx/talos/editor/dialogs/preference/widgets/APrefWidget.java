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

    @Getter
    private boolean isProject = false;

    public APrefWidget(String parentPath, XmlReader.Element xml) {

        if(xml != null) {
            id = xml.getAttribute("name");
            isProject = xml.getBooleanAttribute("project", false);
        }
        path = parentPath + "." + id;

        this.xml = xml;

    }

    public void write() {
        String val = writeString();
        if(!isProject) {
            TalosLocalPrefs.Instance().setGlobalData(path, val);
            TalosLocalPrefs.Instance().save(); // TODO: this needs changing, too many write operations
        } else {
            TalosLocalPrefs.Instance().setProjectPrefs(path, val);
            TalosLocalPrefs.savePrefs();
        }
    }

    public void read() {
        String str = null;
        if (!isProject) {
            str = TalosLocalPrefs.Instance().getGlobalData(path);
        }
        if((str == null || str.isEmpty()) && xml != null) {
            str = xml.getAttribute("default", "0");
        }
        fromString(str);
    }

    public void readLocal() {
        String str = null;
        str = TalosLocalPrefs.Instance().getProjectPrefs().getString(path);
        if((str == null || str.isEmpty()) && xml != null) {
            str = xml.getAttribute("default", "0");
        }
        fromString(str);
    }

    protected abstract void fromString(String str);
    protected abstract String writeString();
}
