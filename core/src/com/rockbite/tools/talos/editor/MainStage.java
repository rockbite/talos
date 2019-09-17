package com.rockbite.tools.talos.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.util.dialog.Dialogs;
import com.kotcrab.vis.ui.widget.*;
import com.rockbite.tools.talos.editor.widgets.ui.ModuleBoardWidget;
import com.rockbite.tools.talos.editor.widgets.ui.PreviewWidget;
import com.rockbite.tools.talos.editor.widgets.ui.TimelineWidget;
import com.rockbite.tools.talos.runtime.ParticleSystem;

public class MainStage extends Stage {

    TextureAtlas atlas;
    public Skin skin;

    public ModuleBoardWidget moduleBoardWidget;

    public ParticleSystem particleSystem;

    PreviewWidget previewWidget;

    TimelineWidget timelineWidget;

    public MainStage() {
        super(new ScreenViewport(),
                new PolygonSpriteBatch());

        Gdx.input.setInputProcessor(this);

        particleSystem = new ParticleSystem();

        atlas = new TextureAtlas(Gdx.files.internal("skin/uiskin.atlas"));
        skin = new Skin(Gdx.files.internal("skin/uiskin.json"));
        skin.addRegions(atlas);

        VisUI.load(skin);

        initActors();
    }

    private void initActors() {
        Table mainTable = new Table();
        mainTable.setSkin(skin);
        mainTable.setFillParent(true);

        moduleBoardWidget = new ModuleBoardWidget();
        moduleBoardWidget.setParticleSystem(particleSystem);

        previewWidget = new PreviewWidget();
        previewWidget.setParticleSystem(particleSystem);

        Table topTable = new Table();
        topTable.setBackground(skin.getDrawable("button-main-menu"));
        Table contentTable = new Table();
        mainTable.add(topTable).left().growX();
        mainTable.row();
        mainTable.add(contentTable).grow();

        MenuBar menuBar = new MenuBar();
        Menu projectMenu = new Menu("File");
        menuBar.addMenu(projectMenu);
        Menu helpMenu = new Menu("Help");
        MenuItem about = new MenuItem("About");
        helpMenu.addItem(about);
        menuBar.addMenu(helpMenu);

        about.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                VisDialog dialog = Dialogs.showOKDialog (MainStage.this, "About Talos 1.0.0", "Add better dialog system later");
                dialog.padTop(32);
                dialog.padLeft(16).setHeight(100);
                dialog.setWidth(320);
            }
        });


        topTable.add(menuBar.getTable()).left().expand();

        Table midTable = new Table();
        Table bottomTable = new Table();

        bottomTable.setSkin(skin);
        Table timelineContainer = new Table();
        Table libraryContainer = new Table();
        VisSplitPane bottomPane = new VisSplitPane(timelineContainer, libraryContainer, false);
        timelineWidget = new TimelineWidget();
        timelineContainer.add(timelineWidget).grow().expand().fill();
        bottomTable.add(bottomPane).expand().grow().fill();

        VisSplitPane verticalPane = new VisSplitPane(midTable, bottomTable, true);
        contentTable.add(verticalPane).expand().grow().fill();
        verticalPane.setMaxSplitAmount(0.8f);
        verticalPane.setMinSplitAmount(0.2f);
        verticalPane.setSplitAmount(0.7f);

        Table leftTable = new Table(); leftTable.setSkin(skin);
        leftTable.add(previewWidget);
        Table rightTable = new Table(); rightTable.setSkin(skin);
        rightTable.add(moduleBoardWidget).grow();
        VisSplitPane horizontalPane = new VisSplitPane(leftTable, rightTable, false);
        midTable.add(horizontalPane).expand().grow().fill();
        horizontalPane.setMaxSplitAmount(0.8f);
        horizontalPane.setMinSplitAmount(0.2f);
        horizontalPane.setSplitAmount(0.3f);

        addActor(mainTable);
    }
}
