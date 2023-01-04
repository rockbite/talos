package com.talosvfx.talos.editor.widgets.ui.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.utils.UIUtils;

public class BasicPopup<T> extends Table {

    private final ClickListener stageListener;
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

        stageListener = new ClickListener() {

            private Vector2 tmp = new Vector2();

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {

                tmp.set(x, y);
                BasicPopup.this.stageToLocalCoordinates(tmp);
                if(BasicPopup.this.hit(tmp.x, tmp.y, true) == null) {
                    hide();
                }

                return super.touchDown(event, x, y, pointer, button);
            }
        };
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

                    hide();
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

    public static <T> BasicPopup<T> build(Class<T> payloadClazz) {
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

    public BasicPopup<T> separator() {
        Table table = UIUtils.makeSeparator();
        container.add(table).growX().height(1).pad(10).padTop(4).padBottom(4);
        container.row();

        return this;
    }

    public BasicPopup<T> show(Actor actor, float x, float y) {
        Vector2 tmp = new Vector2(x, y);
        actor.localToStageCoordinates(tmp);
        return show(tmp.x, tmp.y);
    }

    public BasicPopup<T> show(float x, float y) {
        pack();

        if(x + getWidth() > SharedResources.stage.getWidth()) {
            x -= getWidth();
        }

        setPosition(x, y - this.getHeight());
        SharedResources.stage.addActor(this);

        SharedResources.stage.addListener(stageListener);

        return this;
    }

    public void hide() {
        remove();

        SharedResources.stage.removeListener(stageListener);
    }
}
