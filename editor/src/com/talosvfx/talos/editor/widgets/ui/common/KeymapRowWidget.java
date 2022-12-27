package com.talosvfx.talos.editor.widgets.ui.common;

import com.badlogic.gdx.*;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.talosvfx.talos.editor.project2.SharedResources;

public class KeymapRowWidget extends CollapsableWidget {
    private CheckBox checkBox;
    private SelectionBoxWidget selectionBoxWidget;
    private KeymapBox keymapBox;

    public KeymapRowWidget (String name) {
        super(name);

        background((Drawable) null);

        // TODO: 26.12.22 DONE FOR TEST LATER REMOVE
        selectionBoxWidget.addSelection(new SelectionBoxWidget.SelectionBox("selection 1"));
        selectionBoxWidget.addSelection(new SelectionBoxWidget.SelectionBox("selection 2"));
        selectionBoxWidget.addSelection(new SelectionBoxWidget.SelectionBox("selection 3"));
        selectionBoxWidget.addSelection(new SelectionBoxWidget.SelectionBox("selection 4"));
    }

    public void addSelection (SelectionBoxWidget.SelectionBox selectionBox) {
        selectionBoxWidget.addSelection(selectionBox);
    }

    @Override
    protected void addListeners() {
        arrowButton.setTouchable(Touchable.enabled);
        arrowButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                isCollapsed = !isCollapsed;

                if (isCollapsed) contentCell.setActor(null).pad(0);
                else contentCell.setActor(content).padLeft(topSegment.getPadLeft()).padRight(topSegment.getPadRight()).padBottom(8);

                arrowButton.toggle();
            }
        });
    }

    @Override
    public Table constructTopSegment(String title) {
        // init components
        this.arrowButton = new ArrowButton(false);
        this.arrowButton.getCell(arrowButton.getArrowIcon()).pad(0);

        this.checkBox = new CheckBox("checkboxtext", SharedResources.skin);
        this.selectionBoxWidget = new SelectionBoxWidget();
        this.keymapBox = new KeymapBox();

        final Table topSegment = new Table();
        // NOTE: pads are added to top segment not the entire panel so the click listener also registered paddings
        topSegment.pad(5, 10, 5, 8);

        // assemble top segment
        topSegment.defaults().space(8);
        topSegment.add(arrowButton);
        topSegment.add(checkBox).expandX().left();
        topSegment.add(selectionBoxWidget).width(90);
        topSegment.add(keymapBox).width(90);

        return topSegment;
    }


    // TODO: 26.12.22 implement
    public class KeymapBox extends Table {
        private String keyName;
        private Label keyLabel;

        private boolean inWaitingMode;

        private Drawable defaultBackgroundDrawable = ColorLibrary.obtainBackground(ColorLibrary.SHAPE_SQUIRCLE_2, ColorLibrary.BackgroundColor.PANEL_GRAY);
        private Drawable clickedBackgroundDrawable = ColorLibrary.obtainBackground(ColorLibrary.SHAPE_SQUIRCLE_2, ColorLibrary.BackgroundColor.LIGHT_BLUE);

        public KeymapBox () {
            constructContent();
            addListeners();
        }

        private void constructContent () {
            setBackground(defaultBackgroundDrawable);
            keyLabel = new Label("---", SharedResources.skin, "small");
            keyLabel.setAlignment(Align.center);
            add(keyLabel).growX().center();
        }


        private void addListeners() {
            setTouchable(Touchable.enabled);
            addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    super.clicked(event, x, y);
                    inWaitingMode = !inWaitingMode;

                    if (inWaitingMode) {
                        enterWaitingForInputMode();
                    } else {
                        resetDefaults();
                    }
                }
            });

            // listen to key input
//            // TODO: 27.12.22 do key binding
//            if (inWaitingMode) {
//                setKey(character);
//                inWaitingMode = false;
//            }
//
//            System.out.println(character);
        }

        private void setKey (char keycode) {
            setBackground(defaultBackgroundDrawable);
            keyName = String.valueOf(keycode);
            keyLabel.setText(keyName);
        }

        private void enterWaitingForInputMode () {
            setBackground(clickedBackgroundDrawable);
            keyLabel.setText("Press a key");
        }

        private void resetDefaults () {
            setBackground(defaultBackgroundDrawable);
            keyLabel.setText(keyName);
        }
    }
}
