package com.talosvfx.talos.editor.widgets.ui.common;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Pools;
import com.talosvfx.talos.editor.project2.SharedResources;
import lombok.Getter;

import java.util.ArrayList;

public class SelectionBoxWidget extends Table {
    private SelectionBoxGroup selectionBoxGroup;
    private SelectionBox currentSelection;
    private Label selectionNameLabel;
    private boolean isCollapsed = true;

    public SelectionBoxWidget () {
        constructContent();
        addListeners();

        selectionBoxGroup = new SelectionBoxGroup();
    }

    private void constructContent () {
        pad(2, 5, 2, 2);

        selectionNameLabel = new Label("---", SharedResources.skin, "small");
        final Image downPointerArrow = new Image(SharedResources.skin.getDrawable("mini-arrow-down"));

        add(selectionNameLabel).grow().left();
        add(downPointerArrow);
        setBackground(ColorLibrary.obtainBackground(ColorLibrary.SHAPE_SQUIRCLE_2, ColorLibrary.BackgroundColor.ULTRA_DARK_GRAY));
    }

    private void addListeners () {
        // open/close listener
        addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                isCollapsed = !isCollapsed;
                if (isCollapsed) {
                    removeActor(selectionBoxGroup);
                } else {
                    addActor(selectionBoxGroup);
                }
            }
        });

        // update listener
        addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                final SelectionBox selectedSelectionBox = (SelectionBox) actor;
                setCurrentSelection(selectedSelectionBox);
            }
        });
    }

    private void setCurrentSelection (SelectionBox selectionBox) {
        currentSelection = selectionBox;
        selectionNameLabel.setText(currentSelection.getName());
    }

    public void addSelection (SelectionBox selectionBox) {
        selectionBoxGroup.addSelection(selectionBox);
    }

    public class SelectionBoxGroup extends Table {
        private final ArrayList<SelectionBox> selectionBoxes;

        public SelectionBoxGroup () {
            this.selectionBoxes = new ArrayList<>();

            pad(4).defaults().left().grow();

            setBackground(ColorLibrary.obtainBackground(ColorLibrary.SHAPE_SQUIRCLE_BOTTOM_2, ColorLibrary.BackgroundColor.ULTRA_DARK_GRAY));
        }

        @Override
        public void act(float delta) {
            super.act(delta);

            if (getParent() != null) {
                // show selection box group as drop down
                setY(-getHeight());
            }
        }

        public void addSelection (SelectionBox selectionBox) {
            selectionBoxes.add(selectionBox);
            add(selectionBox).row();
            pack();
        }
    }

    public static class SelectionBox extends Table {
        private String backgroundShape = ColorLibrary.SHAPE_SQUIRCLE_2;
        private ColorLibrary.BackgroundColor overBackgroundColor = ColorLibrary.BackgroundColor.LIGHT_BLUE;
        private Label selectionNameLabel;
        @Getter
        private String name;

        public SelectionBox (String name) {
            this.name = name;
            constructContent();
            addListeners();
        }

        private void constructContent () {
            pad(1, 5, 1, 5);

            selectionNameLabel = new Label(name, SharedResources.skin, "small");
            add(selectionNameLabel).height(15).growX().minWidth(95).left();
        }

        private void addListeners () {
            setTouchable(Touchable.enabled);
            addListener(new ClickListener() {
                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                    super.enter(event, x, y, pointer, fromActor);
                    setBackground(ColorLibrary.obtainBackground(backgroundShape, overBackgroundColor));
                }

                @Override
                public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                    super.exit(event, x, y, pointer, toActor);
                    setBackground((Drawable) null);
                }

                @Override
                public void clicked(InputEvent event, float x, float y) {
                    super.clicked(event, x, y);

                    // fire change event
                    final ChangeListener.ChangeEvent changeEvent =  Pools.obtain(ChangeListener.ChangeEvent.class);
                    fire(changeEvent);
                    Pools.free(changeEvent);
                }
            });
        }
    }
}
