package com.talosvfx.talos.editor.library;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;
import com.talosvfx.talos.editor.xmldefinitions.LibraryDefinition;

import java.util.ArrayList;
import java.util.HashMap;

public class DynamicLibrary {

    private HashMap<String, LibraryDefinition> libraryDefinitions = new HashMap<>();

    public void addLibraryDefinition (LibraryDefinition libraryDefinition) {
        libraryDefinitions.put(libraryDefinition.getLibraryName(), libraryDefinition);
    }

    public HashMap<String, LibraryDefinition> getLibraryDefinitions () {
        return libraryDefinitions;
    }

    public void loadWorkspaceLibraries (XmlReader.Element root) {
        XmlReader.Element libraries = root.getChildByNameRecursive("libraries");
        if (libraries != null) {
            for (int i = 0; i < libraries.getChildCount(); i++) {
                XmlReader.Element library = libraries.getChild(i);

                String libraryName = library.getAttribute("data");

                LibraryDefinition libraryDefinition = new LibraryDefinition();
                libraryDefinition.setLibraryName(libraryName);

                ArrayList<LibraryDefinition.LibraryItem> libraryItems = new ArrayList<>();

                Array<XmlReader.Element> items = library.getChildrenByName("item");

                for (XmlReader.Element item : items) {
                    LibraryDefinition.LibraryItem libraryItem = new LibraryDefinition.LibraryItem();
                    libraryItem.setName(item.getAttribute("name"));
                    libraryItem.setDisplayName(item.getText());

                    libraryItems.add(libraryItem);
                }

                libraryDefinition.setLibraryItems(libraryItems);


                addLibraryDefinition(libraryDefinition);

            }
        }
    }

    public boolean hasLibrary (String libraryName) {
        return libraryDefinitions.containsKey(libraryName);
    }

    public LibraryDefinition getLibrary (String libraryName) {
        return libraryDefinitions.get(libraryName);
    }
}
