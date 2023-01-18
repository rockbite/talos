package com.talosvfx.talos.editor.widgets.ui.common;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.Predicate;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.assets.GameAssetType;
import com.talosvfx.talos.editor.addons.scene.widgets.AssetListPopup;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.widgets.ui.FilteredTree;


public class AssetSelector<T> extends Table {

    private LabelWithZoom nameLabel;
    private GameAsset<T> gameAsset;

    private Predicate<FilteredTree.Node<GameAsset<T>>> filter;

    private AssetListPopup<T> assetListPopup;

    public AssetSelector() {
        super();
    }

    public AssetSelector (String name, GameAssetType type) {

        assetListPopup = new AssetListPopup<>();
        this.filter = new Predicate<FilteredTree.Node<GameAsset<T>>>() {
            @Override
            public boolean evaluate (FilteredTree.Node<GameAsset<T>> node) {
                if (node.getObject() == null) return false;
                return node.getObject().type == type;
            }
        };

        add(getSubWidget()).growX().right().expandX();
    }

    public GameAsset<T> getValue () {
        return gameAsset;
    }

    public Actor getSubWidget () {
        Table table = new Table();
        Skin skin = SharedResources.skin;
        final SquareButton button = new SquareButton(skin, skin.getDrawable("ic-file-edit"), "Select asset");

        nameLabel = new LabelWithZoom("", skin);
        nameLabel.setEllipsis(true);
        nameLabel.setAlignment(Align.right);

        table.right();
        table.add(nameLabel).growX().maxWidth(130).minWidth(0).padRight(2);
        table.add(button);

        button.addListener(new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {
                Vector2 pos = new Vector2(button.getWidth()/2f, button.getHeight()/2f);
                button.localToStageCoordinates(pos);

                assetListPopup.showPopup(getStage(), pos, filter, new FilteredTree.ItemListener<GameAsset<T>>() {
                    @Override
                    public void selected(FilteredTree.Node<GameAsset<T>> node) {
                        super.selected(node);
                        GameAsset<T> gameAsset = node.getObject();
                        if (gameAsset == null || gameAsset.isBroken()) {
                            // facing a directory or bad asset
                            assetListPopup.resetSelection();
                            return;
                        }

                        updateWidget(gameAsset);

                        fireChangedEvent();
                        assetListPopup.remove();
                    }
                });
            }
        });
        return table;
    }

    public void updateWidget (GameAsset<T> value) {
        if(value == null) {
            nameLabel.setText("No Asset");
            gameAsset = null;
            return;
        }

        this.gameAsset = value;
        if (gameAsset.isBroken()) {
            this.nameLabel.setText("Broken asset - " + gameAsset.nameIdentifier);
        } else {
            this.nameLabel.setText(value.getRootRawAsset().handle.name());
        }
    }

    protected boolean fireChangedEvent() {
        ChangeListener.ChangeEvent changeEvent = Pools.obtain(ChangeListener.ChangeEvent.class);

        boolean var2 = false;
        try {
            var2 = fire(changeEvent);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Pools.free(changeEvent);
        }

        return var2;
    }

    public void setValue(GameAsset<T> value) {
        gameAsset = value;
        nameLabel.setText(value.nameIdentifier);
    }
}
