package com.talosvfx.talos.editor.addons.scene.widgets;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.OrderedSet;
import com.talosvfx.talos.editor.addons.scene.utils.AligningUtils;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.widgets.ui.common.ColorLibrary;
import com.talosvfx.talos.runtime.scene.GameObject;


public class AligningToolsPane extends Table {

    public AligningToolsPane(OrderedSet<GameObject> selection) {
        pad(10);
        setBackground(SharedResources.skin.newDrawable("white", ColorLibrary.BackgroundColor.MID_GRAY.getColor()));

        ImageButton verticalAlignToCenter = new ImageButton(SharedResources.skin.getDrawable("align-center-vertical"));
        ImageButton verticalAlignToLeft = new ImageButton(SharedResources.skin.getDrawable("align-left"));
        ImageButton verticalAlignToRight = new ImageButton(SharedResources.skin.getDrawable("align-right"));

        ImageButton horizontalAlignToCenter = new ImageButton(SharedResources.skin.getDrawable("align-center-horizontal"));
        ImageButton horizontalAlignToTop = new ImageButton(SharedResources.skin.getDrawable("align-top"));
        ImageButton horizontalAlignToBottom = new ImageButton(SharedResources.skin.getDrawable("align-bottom"));

        defaults().space(15);
        int iconSize = 20;

        add(verticalAlignToCenter).size(iconSize);
        add(verticalAlignToLeft).size(iconSize);
        add(verticalAlignToRight).size(iconSize);
        add(horizontalAlignToCenter).size(iconSize);
        add(horizontalAlignToTop).size(iconSize);
        add(horizontalAlignToBottom).size(iconSize);

        Image separator = new Image(SharedResources.skin.newDrawable("white", ColorLibrary.BackgroundColor.BRIGHT_GRAY.getColor()));
        add(separator).size(1,25);
//        row();
        ImageButton distributeHorizontalCenter = new ImageButton(SharedResources.skin.getDrawable("distribute-horizontal-center"));
        ImageButton distributeVerticalCenter = new ImageButton(SharedResources.skin.getDrawable("distribute-vertical-center"));

//        ImageButton distributeHorizontalRight = new ImageButton(SharedResources.skin.getDrawable("distribute-horizontal-right"));
//        ImageButton distributeHorizontalLeft = new ImageButton(SharedResources.skin.getDrawable("distribute-horizontal-left"));

//        ImageButton distributeVerticalDown = new ImageButton(SharedResources.skin.getDrawable("distribute-vertical-down"));
//        ImageButton distributeVerticalUp = new ImageButton(SharedResources.skin.getDrawable("distribute-vertical-up"));
//        add(distributeHorizontalRight).size(iconSize);
        add(distributeHorizontalCenter).size(iconSize);
//        add(distributeHorizontalLeft).size(iconSize);
//        add(distributeVerticalDown).size(iconSize);
        add(distributeVerticalCenter).size(iconSize);
//        add(distributeVerticalUp).size(iconSize);
        pack();

        addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                event.cancel();
                return true;
            }
        });

        verticalAlignToCenter.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                AligningUtils.alignVerticalCenter(selection.orderedItems());
            }
        });

        verticalAlignToLeft.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                AligningUtils.alignVerticalLeft(selection.orderedItems());
            }
        });

        verticalAlignToRight.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                AligningUtils.alignVerticalRight(selection.orderedItems());
            }
        });

        horizontalAlignToCenter.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                AligningUtils.alignHorizontalCenter(selection.orderedItems());
            }
        });

        horizontalAlignToTop.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                AligningUtils.alignHorizontalTop(selection.orderedItems());
            }
        });

        horizontalAlignToBottom.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                AligningUtils.alignHorizontalBottom(selection.orderedItems());
            }
        });


        distributeVerticalCenter.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                AligningUtils.distributeItemsVerticalCenter(selection.orderedItems());
            }
        });

        distributeHorizontalCenter.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                AligningUtils.distributeItemsHorizontalCenter(selection.orderedItems());
            }
        });

 /*       distributeVerticalDown.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {

                AligningUtils.distributeItemsVerticalDown(selection.orderedItems());
            }
        });

        distributeVerticalUp.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                AligningUtils.distributeItemsVerticalUp(selection.orderedItems());
            }
        });

          distributeHorizontalRight.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {

                AligningUtils.distributeItemsHorizontalRight(selection.orderedItems());
            }
        });

        distributeHorizontalLeft.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                AligningUtils.distributeItemsHorizontalLeft(selection.orderedItems());
            }
        });*/
    }


    @Override
    public void act(float delta) {
        super.act(delta);
        setPosition(getParent().getWidth() - getWidth() - 25, getParent().getHeight() - getHeight() - 25);
    }

}
