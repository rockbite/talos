package com.talosvfx.talos.editor.dialogs.preference.widgets;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.XmlReader;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.project2.TalosProjectData;
import com.talosvfx.talos.editor.project2.localprefs.TalosLocalPrefs;
import lombok.Getter;

import java.lang.reflect.Field;

public abstract class APrefWidget extends Table {

    @Getter
    protected String id;
    @Getter
    protected String path;

    protected XmlReader.Element xml;

    @Getter
    private boolean isProject = false;

    @Getter
    private boolean isGlobalProject = false;

    public APrefWidget(String parentPath, XmlReader.Element xml) {

        if(xml != null) {
            id = xml.getAttribute("name");
            isProject = xml.getBooleanAttribute("project", false);
            isGlobalProject = xml.getBooleanAttribute("shared-project", false);
        }
        path = parentPath + "." + id;

        this.xml = xml;

    }

    public void write() {
        String val = writeString();

        if (isGlobalProject) {
            writeGlobalProject();
            return;
        }

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

        if (isGlobalProject) {
            readGlobalProject();
            return;
        }

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

    private void writeGlobalProject () {
        try {
            String val = writeString();

            TalosProjectData currentProject = SharedResources.currentProject;
            Field field = TalosProjectData.class.getDeclaredField(id);
            field.setAccessible(true);
            field.set(currentProject, val);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void readGlobalProject () {
        try {
            TalosProjectData currentProject = SharedResources.currentProject;
            Field field = TalosProjectData.class.getDeclaredField(id);
            field.setAccessible(true);
            Object res =  field.get(currentProject);
            fromString((String)res);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected abstract void fromString(String str);
    protected abstract String writeString();


}
