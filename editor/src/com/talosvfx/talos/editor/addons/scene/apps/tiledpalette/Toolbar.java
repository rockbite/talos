package com.talosvfx.talos.editor.addons.scene.apps.tiledpalette;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.OrderedSet;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.widgets.ui.common.ColorLibrary;
import com.talosvfx.talos.editor.widgets.ui.common.SquareButton;

public class Toolbar extends Table {
    private PaletteEditor paletteEditor;

    SquareButton translate;
    SquareButton editGizmo;
    SquareButton editTile;
    SquareButton editFakeHeight;

    public Toolbar(PaletteEditor paletteEditor) {
        this.paletteEditor = paletteEditor;

        Skin skin = TalosMain.Instance().getSkin();

        setBackground(skin.newDrawable("square-bordered"));

        Button.ButtonStyle buttonStyle = new Button.ButtonStyle();
        buttonStyle.up = null;
        buttonStyle.down = ColorLibrary.obtainBackground(skin, "square-bordered-selected", ColorLibrary.BackgroundColor.WHITE);
        buttonStyle.checked = ColorLibrary.obtainBackground(skin, "square-bordered-selected", ColorLibrary.BackgroundColor.WHITE);

        translate = new SquareButton(skin, skin.getDrawable("arrow-icon"), true, "Select mode");
        translate.setStyle(buttonStyle);
        editGizmo = new SquareButton(skin, skin.getDrawable("image-transform-icon"), true, "Gizmo Edit mode");
        editGizmo.setStyle(buttonStyle);
        editTile = new SquareButton(skin, skin.getDrawable("add-remove-tile-icon"), true, "Tile Edit mode");
        editTile.setStyle(buttonStyle);
        editFakeHeight = new SquareButton(skin, skin.getDrawable("set-line-icon"), true, "Line Edit mode");
        editFakeHeight.setStyle(buttonStyle);

        add(translate).size(32);
        add(editGizmo).size(32);
        add(editTile).size(32);
        add(editFakeHeight).size(32);
        addListeners();

        translate.setDisabled(false);
        editGizmo.setDisabled(false);
        editTile.setDisabled(false);
        editFakeHeight.setDisabled(false);

        translate.toggle();
    }

    private void addListeners() {
        // if checked, uncheck everything
        // else, uncheck only if something else is active in its place
        translate.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (translate.isChecked()) {
                    disableAllBesides(translate);
                    startFreeTranslateEditMode();
                } else if (!canUncheck(translate)) {
                    event.cancel();
                }
            }
        });
        // if checked, uncheck everything
        // else, uncheck only if something else is active in its place
        editGizmo.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                OrderedSet<GameObject> selection = paletteEditor.getSelection();
                boolean singleSelection = selection.size == 1;
                boolean selectedStaticTile = singleSelection && selection.first() instanceof TileGameObjectProxy;
                if (editGizmo.isChecked() && (!singleSelection || selectedStaticTile))  {
                    event.cancel();
                    return;
                }

                if (editGizmo.isChecked()) {
                    disableAllBesides(editGizmo);
                    startFreeTransformEditMode();
                } else if (!canUncheck(editGizmo)) {
                    event.cancel();
                } else {
                    endFreeTransformEditMode();
                }
            }
        });
        // if checked, uncheck everything
        // else, uncheck and activate translate mode
        editTile.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                OrderedSet<GameObject> selection = paletteEditor.getSelection();
                boolean singleSelection = selection.size == 1;
                boolean selectedStaticTile = singleSelection && selection.first() instanceof TileGameObjectProxy;
                if (editTile.isChecked() && (!singleSelection || selectedStaticTile))  {
                    event.cancel();
                    return;
                }

                if (editTile.isChecked()) {
                    disableAllBesides(editTile);
                    startParentTileEditMode();
                } else if (!editTile.isChecked() && editTile.isPressed()) {
                    translate.toggle();
                }
            }
        });
        // if checked, uncheck everything
        // else, uncheck and activate translate mode
        editFakeHeight.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                OrderedSet<GameObject> selection = paletteEditor.getSelection();
                boolean singleSelection = selection.size == 1;
                boolean selectedStaticTile = singleSelection && selection.first() instanceof TileGameObjectProxy;
                if (editFakeHeight.isChecked() && (!singleSelection || selectedStaticTile))  {
                    event.cancel();
                    return;
                }

                if (editFakeHeight.isChecked()) {
                    disableAllBesides(editFakeHeight);
                    startFakeHeightEditMode();
                } else if(!editFakeHeight.isChecked() && editFakeHeight.isPressed()){
                    translate.toggle();
                }
            }
        });
    }

    private void disableAllBesides(SquareButton button) {
        if (button != translate && translate.isChecked())
            translate.toggle();
        if (button != editGizmo && editGizmo.isChecked())
            editGizmo.toggle();
        if (button != editTile && editTile.isChecked())
            editTile.toggle();
        if (button != editFakeHeight && editFakeHeight.isChecked())
            editFakeHeight.toggle();
    }

    private boolean canUncheck(SquareButton button) {
        boolean canUncheck = false;
        if (button != translate)
            canUncheck = canUncheck || translate.isChecked();
        if (button != editGizmo)
            canUncheck = canUncheck || editGizmo.isChecked();
        if (button != editTile)
            canUncheck = canUncheck || editTile.isChecked();
        if (button != editFakeHeight)
            canUncheck = canUncheck || editFakeHeight.isChecked();
        return canUncheck;
    }

    private void startFreeTranslateEditMode () {
		paletteEditor.currentEditMode = PaletteEditor.PaletteEditMode.FREE_TRANSLATE;
    }

    private void startFreeTransformEditMode () {
        paletteEditor.currentEditMode = PaletteEditor.PaletteEditMode.FREE_TRANSFORM;
		paletteEditor.unlockGizmos();
    }

    private void startParentTileEditMode () {
        paletteEditor.currentEditMode = PaletteEditor.PaletteEditMode.PARENT_TILE_EDIT;
    }

    private void startFakeHeightEditMode () {
        paletteEditor.currentEditMode = PaletteEditor.PaletteEditMode.FAKE_HEIGHT_EDIT;
    }

    private void endFreeTranslateEditMode () {
        // do nothing
    }

    private void endFreeTransformEditMode () {
		paletteEditor.lockGizmos();
    }

    private void endParentTileEditMode () {
        // do nothing
    }

    private void endFakeHeightEditMode () {
        // do nothing
    }
}
