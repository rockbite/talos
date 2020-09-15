package com.talosvfx.talos.editor.xmldefinitions;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.XmlWriter;

import java.io.*;
import java.util.*;

public class DefinitionWriter {

    private ArrayList<ModuleDefinition> modulesToWrite = new ArrayList<>();
    private ArrayList<LibraryDefinition> librariesToWrite = new ArrayList<>();


    public DefinitionWriter () {
    }


    public void addModuleDefinitionToWrite (ModuleDefinition moduleDefinition) {
        modulesToWrite.add(moduleDefinition);
    }

    public void addLibraryDefinition (LibraryDefinition libraryDefinition) {
        librariesToWrite.add(libraryDefinition);
    }

    public void writeToFile (FileHandle fileHandle) throws IOException {
        XmlWriter xmlWriter = new XmlWriter(new FileWriter(fileHandle.file()));


        try {

            //Header

            xmlWriter.element("modules").attribute("classPath", "com.talosvfx.talos.editor.addons.treedata.nodes");
            xmlWriter.element("category").attribute("name", "general").attribute("title", "General");

            for (ModuleDefinition it : modulesToWrite) {
                writeModule(xmlWriter, it);
            }

            xmlWriter.element("libraries");
            for (LibraryDefinition libraryDefinition : librariesToWrite) {
                writeLibrary(xmlWriter, libraryDefinition);
            }
            xmlWriter.pop();


            //Footer

            xmlWriter.pop();
            xmlWriter.pop();

            xmlWriter.flush();
            xmlWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeLibrary (XmlWriter xmlWriter, LibraryDefinition libraryDefinition) throws IOException {
        xmlWriter.element("library");
        xmlWriter.attribute("data", libraryDefinition.getLibraryName());

        ArrayList<LibraryDefinition.LibraryItem> libraryItems = libraryDefinition.getLibraryItems();
        for (LibraryDefinition.LibraryItem libraryItem : libraryItems) {
            xmlWriter.element("item");
            xmlWriter.attribute("name", libraryItem.getName());
            xmlWriter.text(libraryItem.getDisplayName());
            xmlWriter.pop();
        }

        xmlWriter.pop();
    }

    private void writeModule (XmlWriter xmlWriter, ModuleDefinition moduleDefinition) throws IOException {
        xmlWriter.element("module");

        xmlWriter.attribute("name", moduleDefinition.getModuleName());
        xmlWriter.attribute("title", moduleDefinition.getModuleTitle());
        xmlWriter.attribute("class", moduleDefinition.getObjectClazz().getName());


        writePort(xmlWriter, moduleDefinition, false);


        //Inputs
        xmlWriter.element("group");
        List<ModuleDefinition> inputPorts = moduleDefinition.getInputPorts();
        for (ModuleDefinition inputPort : inputPorts) {
            writePort(xmlWriter, inputPort, true);
        }
        xmlWriter.pop();

        xmlWriter.pop();
    }

    private void writePort (XmlWriter xmlWriter, ModuleDefinition port, boolean input) throws IOException {

        writeType(xmlWriter, port);


        boolean shouldHavePort = shouldHaveConnectionPort(port);

        if (shouldHavePort) {
            xmlWriter.attribute("port", input ? "input" : "output");
        }

        xmlWriter.attribute("name", port.getIdentifierName());
        xmlWriter.attribute("type", port.getObjectClazz().getSimpleName());
        xmlWriter.text(port.getDisplayFriendlyIdentifierName());


        xmlWriter.pop();
    }

    private boolean shouldHaveConnectionPort (ModuleDefinition port) {
        Class objectClazz = port.getObjectClazz();

        if (isBooleanValue(port)) {
            return false;
        }
        return true;
    }

    private void writeType (XmlWriter xmlWriter, ModuleDefinition port) throws IOException {
        if (port.getSelectBoxDataSource() != null) {
            xmlWriter.element("selectbox").attribute("data-source", port.getSelectBoxDataSource());
        } else if (isDynamicValue(port)) {
            xmlWriter.element("dynamicValue").attribute("progress", "true");
        } else if (isBooleanValue(port)) {
            xmlWriter.element("checkbox");
        } else {
            xmlWriter.element("value");
        }
    }

    private boolean isBooleanValue (ModuleDefinition port) {
        if (Boolean.class.isAssignableFrom(port.getObjectClazz())) return true;
        if (port.getObjectClazz() == boolean.class) return true;

        return false;
    }

    private boolean isDynamicValue (ModuleDefinition port) {
        Class objectClazz = port.getObjectClazz();
        if (Number.class.isAssignableFrom(objectClazz)) return true;
        if (port.getObjectClazz().isPrimitive()) {
            if (port.getObjectClazz() == int.class) return true;
            if (port.getObjectClazz() == float.class) return true;
            if (port.getObjectClazz() == long.class) return true;
            if (port.getObjectClazz() == double.class) return true;
            if (port.getObjectClazz() == byte.class) return true;
            if (port.getObjectClazz() == short.class) return true;
        }
        return false;
    }
}
