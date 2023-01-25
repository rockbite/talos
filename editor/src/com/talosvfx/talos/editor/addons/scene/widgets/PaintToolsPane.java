package com.talosvfx.talos.editor.addons.scene.widgets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Pools;
import com.talosvfx.talos.editor.addons.scene.events.ComponentUpdated;
import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.editor.addons.scene.widgets.gizmos.PaintSurfaceGizmo;
import com.talosvfx.talos.editor.nodes.widgets.ColorWidget;
import com.talosvfx.talos.editor.nodes.widgets.ValueWidget;
import com.talosvfx.talos.editor.notifications.EventHandler;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.Observer;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.widgets.ui.common.SquareButton;
import com.talosvfx.talos.runtime.scene.components.AComponent;
import com.talosvfx.talos.runtime.scene.components.PaintSurfaceComponent;

public class PaintToolsPane extends Table implements Observer {

    public final SquareButton paint;
    public final SquareButton erase;
    public final ColorWidget colorWidget;
    public final ValueWidget sizeWidget;
    public final ValueWidget hardnessWidget;
    public final ValueWidget opacityWidget;
    private final PaintSurfaceGizmo paintSurfaceGizmo;

    private float bracketStartCoolDown = 0f;
    private float bracketCoolDown = 0f;
    private int bracketDown = 0;

    private Tool currentTool = Tool.BRUSH;

    private Color fullColor = new Color(Color.WHITE);

    public enum Tool {
        BRUSH,
        ERASER
    }

    public PaintToolsPane(PaintSurfaceGizmo paintSurfaceGizmo) {
        this.paintSurfaceGizmo = paintSurfaceGizmo;
        Notifications.registerObserver(this);

        setSkin(SharedResources.skin);

        paint = new SquareButton(getSkin(), getSkin().getDrawable("brush_icon"), true, "Paintbrush");
        erase = new SquareButton(getSkin(), getSkin().getDrawable("eraser_icon"), true, "Eraser");
        ButtonGroup<SquareButton> buttonButtonGroup = new ButtonGroup<>();
        buttonButtonGroup.setMaxCheckCount(1);
        buttonButtonGroup.setMinCheckCount(1);
        buttonButtonGroup.add(paint, erase);
        paint.setChecked(true);

        add(paint).padRight(10).size(37);
        add(erase).padRight(10).size(37);

        colorWidget = new ColorWidget();
        colorWidget.init(SharedResources.skin, null);
        colorWidget.setColor(Color.WHITE);
        add(colorWidget).padRight(10);

        sizeWidget = createFloatWidget("size", 1, 100, 5);
        sizeWidget.setStep(1);
        add(sizeWidget).padRight(10);
        hardnessWidget = createFloatWidget("hardness", 0, 100, 100f);
        add(hardnessWidget).padRight(10);
        opacityWidget = createFloatWidget("opacity", 0, 100, 100f);
        add(opacityWidget).padRight(10);

        pack();

        sizeWidget.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                paintSurfaceGizmo.brushTexture = null;
            }
        });
        hardnessWidget.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                paintSurfaceGizmo.brushTexture = null;
            }
        });
        colorWidget.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                paintSurfaceGizmo.brushTexture = null;
                fullColor.set(colorWidget.getValue());
                applyChannelFilterToColor();
            }
        });
        opacityWidget.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                paintSurfaceGizmo.brushTexture = null;
            }
        });

        paint.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                paintSurfaceGizmo.brushTexture = null;
                currentTool = Tool.BRUSH;
            }
        });

        erase.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                paintSurfaceGizmo.brushTexture = null;
                currentTool = Tool.ERASER;
            }
        });

    }

    private void applyChannelFilterToColor() {
        PaintSurfaceComponent surface = paintSurfaceGizmo.getGameObject().getComponent(PaintSurfaceComponent.class);
        if(!surface.redChannel) {
            colorWidget.getValue().r = 0;
        } else {
            colorWidget.getValue().r = fullColor.r;
        }
        if(!surface.greenChannel) {
            colorWidget.getValue().g = 0;
        } else {
            colorWidget.getValue().g = fullColor.g;
        }
        if(!surface.blueChannel) {
            colorWidget.getValue().b = 0;
        } else {
            colorWidget.getValue().b = fullColor.b;
        }
        colorWidget.setColor(colorWidget.getValue()); // to update the view
    }

    @EventHandler
    public void onComponentUpdated(ComponentUpdated event) {
        AComponent component = event.getComponent();
        PaintSurfaceComponent surface = paintSurfaceGizmo.getGameObject().getComponent(PaintSurfaceComponent.class);
        if (component == surface && !event.isRapid()) {
            applyChannelFilterToColor();
        }
    }


    private ValueWidget createFloatWidget(String name, float min, float max, float value) {
        ValueWidget hardnessWidget = new ValueWidget(SharedResources.skin);
        hardnessWidget.setRange(min, max);
        hardnessWidget.setStep(1);
        hardnessWidget.setValue(value);
        hardnessWidget.setShowProgress(true);
        hardnessWidget.setLabel(name);

        return hardnessWidget;
    }


    @Override
    public void act(float delta) {
        super.act(delta);

        Vector2 vec = Pools.get(Vector2.class).obtain();

        setPosition(25, getParent().getHeight() - getHeight() - 25);

        Pools.get(Vector2.class).free(vec);

        if(bracketDown > 0) {
            bracketStartCoolDown -= Gdx.graphics.getDeltaTime();

            if(bracketStartCoolDown <= 0) {
                bracketStartCoolDown = 0f;

                bracketCoolDown -= Gdx.graphics.getDeltaTime();
                if(bracketCoolDown <= 0) {
                    bracketCoolDown = 0.1f;
                    if (bracketDown == Input.Keys.LEFT_BRACKET) {
                        decreaseSize();
                    } else if (bracketDown == Input.Keys.RIGHT_BRACKET) {
                        increaseSize();
                    }
                }
            }
        }
    }

    public void setFrom(GameObject gameObject) {

    }

    public int getSize() {
        return (int)sizeWidget.getValue().floatValue();
    }

    public float getHardness() {
        return hardnessWidget.getValue()/100f;
    }

    public Color getColor() {
        return colorWidget.getValue();
    }

    private float getSizeDiff() {
        float size = getSize();
        float diff = 1;
        if(size >= 10) diff = 5;
        if(size >= 50) diff = 10;

        return diff;
    }

    public void decreaseSize() {
        float size = getSize();
        float diff = getSizeDiff();
        float newSize = (float) (Math.floor(size / diff) * diff);
        if(size == newSize) size -= diff; else size = newSize;
        sizeWidget.setValue(size);
    }

    public void increaseSize() {
        float size = getSize();
        float diff = getSizeDiff();
        float newSize = (float) (Math.ceil(size / diff) * diff);
        if(size == newSize) size += diff; else size = newSize;
        sizeWidget.setValue(size);
    }

    public void bracketDown(int keycode) {
        bracketDown = keycode;
        bracketStartCoolDown = 0.5f;
        bracketCoolDown = 0f;

        if (keycode == Input.Keys.LEFT_BRACKET) {
            decreaseSize();
        } else if (keycode == Input.Keys.RIGHT_BRACKET) {
            increaseSize();
        }
    }

    public void bracketUp(int keycode) {
        bracketDown = 0;
        bracketStartCoolDown = 0;
        bracketCoolDown = 0f;
    }

    public float getOpacity() {
        return opacityWidget.getValue()/100;
    }

    public Tool getCurrentTool() {
        return currentTool;
    }
}
