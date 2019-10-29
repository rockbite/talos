package com.rockbite.tools.talos.editor.addons.bvb;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.widget.Menu;
import com.kotcrab.vis.ui.widget.MenuItem;
import com.rockbite.tools.talos.TalosMain;
import com.rockbite.tools.talos.editor.addons.IAddon;
import com.rockbite.tools.talos.editor.project.BvbProject;

public class BvBAddon implements IAddon {

    public static BvbProject BVB;

    @Override
    public void init() {
        BVB = new BvbProject(this);

        Menu toolsMenu = TalosMain.Instance().UIStage().getToolsMenu();
        MenuItem newBvbProject = new MenuItem("New Skeletal Bridge");
        toolsMenu.addItem(newBvbProject);

        newBvbProject.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);

                TalosMain.Instance().ProjectController().newProject(BVB);
            }
        });
    }

    @Override
    public void initUIContent() {
        TalosMain.Instance().UIStage().swapToAddonContent(null, null, null);
    }
}
