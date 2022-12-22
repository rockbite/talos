package com.talosvfx.talos.editor.project2;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.talosvfx.talos.editor.project2.input.InputHandling;
import com.talosvfx.talos.editor.project2.savestate.GlobalSaveStateSystem;
import com.talosvfx.talos.editor.utils.WindowUtils;
import com.talosvfx.talos.editor.widgets.ui.menu.MainMenu;

public class SharedResources {
	public static Skin skin;

	public static Stage stage;

	public static ProjectLoader projectLoader;

	public static TalosProjectData currentProject;

	public static AppManager appManager;

	public static InputHandling inputHandling;

	public static GlobalDragAndDrop globalDragAndDrop;

	public static WindowUtils windowUtils;

	public static ConfigData configData;

	public static GlobalSaveStateSystem globalSaveStateSystem;
	public static MainMenu mainMenu;

	public static TalosControl talosControl;


	public static UIController ui;
}
