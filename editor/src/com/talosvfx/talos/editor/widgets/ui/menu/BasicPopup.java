package com.talosvfx.talos.editor.widgets.ui.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.talosvfx.talos.editor.project2.SharedResources;

public class BasicPopup<T> extends Table {

    private Table container;

    public interface PopupListener<T> {
        void itemClicked(T payload);
    }

    PopupListener<T> listener = new PopupListener<T>() {
        @Override
        public void itemClicked(T payload) {

        }
    };

    public BasicPopup() {
        setBackground(SharedResources.skin.getDrawable("top-menu-popup-sub-bg"));

        container = new Table();
        add(container).grow().pad(10).padLeft(0).padRight(0);
    }

    public class BasicRow extends Table {

        private final Drawable selectedDrawable;
        private final Label label;
        private final ClickListener clickListener;
        private final T payload;

        public BasicRow(String title, T payload) {
            selectedDrawable = SharedResources.skin.getDrawable("menu-selection-bg-blue");

            this.payload = payload;

            label = new Label(title, SharedResources.skin);
            add(label).pad(2).padLeft(15).padRight(15).left().expandX();

            clickListener = new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    super.clicked(event, x, y);

                    listener.itemClicked(payload);
                }
            };
            addListener(clickListener);

            setTouchable(Touchable.enabled);
        }

        @Override
        public void act(float delta) {
            super.act(delta);

            if(clickListener.isOver()) {
                setBackground(selectedDrawable);
            } else {
                setBackground((Drawable) null);
            }
        }
    }

    public static <T> BasicPopup<T> build(Class<T> clazz) {
        BasicPopup <T> popup = new BasicPopup();

        return popup;
    }

    public BasicPopup<T> onClick(PopupListener<T> listener) {
        this.listener = listener;

        return this;
    }

    public BasicPopup<T> addItem(String title, T payload) {

        BasicRow row = new BasicRow(title, payload);

        container.add(row).growX().pad(0).padLeft(10).padRight(10);
        container.row();

        return this;
    }

    public void show(float x, float y) {
        pack();

        setPosition(x, y - this.getHeight());
        SharedResources.stage.addActor(this);
    }

    public void hide() {
        remove();
    }
}
