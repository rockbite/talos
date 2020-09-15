package com.talosvfx.talos.editor.xmldefinitions;

import java.util.ArrayList;

public class LibraryDefinition {
   //<library>
    //	<data name=“itemModelList”>
    //		<item name=“IRONOREEC”>Iron Ore</item>
    //		<item name=“IRONOREEC”>Iron Ore</item>
    //	</data>
    //</library>

    public static class LibraryItem {
        private String name;
        private String displayName;

        public String getName () {
            return name;
        }

        public void setName (String name) {
            this.name = name;
        }

        public String getDisplayName () {
            return displayName;
        }

        public void setDisplayName (String displayName) {
            this.displayName = displayName;
        }
    }

    private String libraryName;
    private ArrayList<LibraryItem> libraryItems;

    public ArrayList<LibraryItem> getLibraryItems () {
        return libraryItems;
    }

    public void setLibraryItems (ArrayList<LibraryItem> libraryItems) {
        this.libraryItems = libraryItems;
    }

    public String getLibraryName () {
        return libraryName;
    }

    public void setLibraryName (String libraryName) {
        this.libraryName = libraryName;
    }
}
