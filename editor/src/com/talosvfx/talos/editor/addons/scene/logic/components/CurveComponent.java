package com.talosvfx.talos.editor.addons.scene.logic.components;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;
import com.talosvfx.talos.editor.addons.scene.SceneUtils;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.widgets.propertyWidgets.*;

import java.util.function.Supplier;

public class CurveComponent extends AComponent {

    public Array<Vector2> points = new Array<>();

    public boolean isClosed = false;

    public transient Vector2[] tmpArr;
    public static float[] neighbourDistances = new float[2];

    public boolean automaticControl = false;

    public CurveComponent() {
        setToNew();
        tmpArr = new Vector2[]{new Vector2(), new Vector2(), new Vector2(), new Vector2()};
    }

    public void setToNew() {
        points.clear();
        points.add(new Vector2(-2, 0));
        points.add(new Vector2(-1, 1));
        points.add(new Vector2(1, -1));
        points.add(new Vector2(2, 0));
    }

    public void addSegment(Vector2 anchorPos) {
        Vector2 tmp = Pools.get(Vector2.class).obtain();

        tmp.set(points.get(points.size-1)).scl(2f).sub(points.get(points.size-2));
        points.add(new Vector2(tmp));
        tmp.set(points.get(points.size-1)).add(anchorPos).scl(0.5f);
        points.add(new Vector2(tmp));
        points.add(new Vector2(anchorPos));

        Pools.get(Vector2.class).free(tmp);

        if(automaticControl) {
            autoSetAllAffectedControlPoints(points.size - 1);
        }

        SceneUtils.componentUpdated(this.getGameObject().getGameObjectContainerRoot(), this.getGameObject(), this);
    }

    public void splitSegment(Vector2 point, int segmentIndex) {
        points.insertRange(segmentIndex * 3 + 2, 3);
        points.set(segmentIndex * 3 + 2, new Vector2(0, 0));
        points.set(segmentIndex * 3 + 3, new Vector2(point));
        points.set(segmentIndex * 3 + 4, new Vector2(0, 0));

        if(automaticControl) {
            autoSetAllAffectedControlPoints(segmentIndex * 3 + 3);
        } else {
            autoSetAnchorControlPoints(segmentIndex * 3 + 3);
        }

        SceneUtils.componentUpdated(this.getGameObject().getGameObjectContainerRoot(), this.getGameObject(), this);
    }

    public void deleteSegment(int anchorIndex) {
        int numSegments = getNumSegments();
        if(numSegments > 2 || (!isClosed && numSegments > 1)) {
            if (anchorIndex == 0) {
                if (isClosed) {
                    points.get(points.size - 1).set(points.get(points.size - 2));
                }
                points.removeRange(0, 2);
            } else if (anchorIndex == points.size - 1 && !isClosed) {
                points.removeRange(anchorIndex - 2, anchorIndex);
            } else {
                points.removeRange(anchorIndex - 1, anchorIndex + 1);
            }
        }

        SceneUtils.componentUpdated(this.getGameObject().getGameObjectContainerRoot(), this.getGameObject(), this);
    }

    public void setClosedState(boolean isClosed) {
        this.isClosed = isClosed;
        if(isClosed) {
            Vector2 tmp = Pools.get(Vector2.class).obtain();

            tmp.set(points.get(points.size-1)).scl(2f).sub(points.get(points.size-2));
            points.add(new Vector2(tmp));

            tmp.set(points.get(0)).scl(2f).sub(points.get(1));
            points.add(new Vector2(tmp));

            Pools.get(Vector2.class).free(tmp);

            if(automaticControl) {
                autoSetAnchorControlPoints(0);
                autoSetAnchorControlPoints(points.size - 3);
            }
        } else {
            points.removeRange(points.size - 2, points.size - 1);
            if(automaticControl) {
                autoSetStartAndEndControls();
            }
        }
    }

    public int getNumSegments() {
        return points.size / 3;
    }

    public Vector2[] getPointsInSegment(int index) {
        for(int i = 0; i < 4; i++) {
            if(i == 3) {
                tmpArr[i].set(points.get(loopIndex(index * 3 + i)));
            } else {
                tmpArr[i].set(points.get(index * 3 + i));
            }
        }
        return tmpArr;
    }

    public void autoSetAnchorControlPoints(int anchorIndex) {
        Vector2 anchorPos = points.get(anchorIndex);
        Vector2 direction = Pools.obtain(Vector2.class);
        direction.set(0, 0);

        if(anchorIndex - 3 >= 0 || isClosed) {
            Vector2 offset = Pools.obtain(Vector2.class);
            offset.set(points.get(loopIndex(anchorIndex - 3))).sub(anchorPos);
            float length = offset.len();
            direction.add(offset.nor());
            neighbourDistances[0] = length;
            Pools.free(offset);
        }
        if(anchorIndex + 3 >= 0 || isClosed) {
            Vector2 offset = Pools.obtain(Vector2.class);
            offset.set(points.get(loopIndex(anchorIndex + 3))).sub(anchorPos);
            float length = offset.len();
            direction.sub(offset.nor());
            neighbourDistances[1] = -length;
            Pools.free(offset);
        }

        direction.nor();

        for(int i = 0; i < 2; i++) {
            int controlIndex = anchorIndex + i * 2 - 1;
            if((controlIndex >= 0 && controlIndex < points.size) || isClosed) {
                Vector2 tmp = Pools.obtain(Vector2.class);
                tmp.set(direction).scl(neighbourDistances[i]).scl(0.5f).add(anchorPos);
                points.get(loopIndex(controlIndex)).set(tmp);
                Pools.free(tmp);
            }
        }


        Pools.free(direction);
    }

    public void autoSetAllAffectedControlPoints(int updatedAnchorIndex) {
        for(int i = updatedAnchorIndex - 3; i <= updatedAnchorIndex + 3; i+=3) {
            if(i >= 0 && i < points.size || isClosed) {
                autoSetAnchorControlPoints(loopIndex(i));
            }
        }
        autoSetStartAndEndControls();
    }

    public void autoSetAllControlPoints() {
        for(int i = 0; i < points.size; i+=3) {
            autoSetAnchorControlPoints(i);
        }
        autoSetStartAndEndControls();
    }

    public void autoSetStartAndEndControls() {
        if(!isClosed) {
            points.get(1).set(points.get(0)).add(points.get(2)).scl(0.5f);
            points.get(points.size - 2).set(points.get(points.size - 1)).add(points.get(points.size - 3)).scl(0.5f);
        }
    }

    public int loopIndex(int index) {
        return (index + points.size) % points.size;
    }

    @Override
    public Array<PropertyWidget> getListOfProperties() {
        Array<PropertyWidget> properties = new Array<>();

        properties.add(new LabelWidget("segments", new Supplier<String>() {
            @Override
            public String get() {
                return getNumSegments() + "";
            }
        }));

        properties.add(new LabelWidget("points", new Supplier<String>() {
            @Override
            public String get() {
                return points.size + "";
            }
        }));

        ButtonPropertyWidget<String> cleanButton = new ButtonPropertyWidget<String>("Create New", new ButtonPropertyWidget.ButtonListener() {
            @Override
            public void clicked(ButtonPropertyWidget widget) {
                setToNew();
            }
        });

        properties.add(cleanButton);

        CheckboxWidget isClosedWidget = new CheckboxWidget("Toggle Closed", new Supplier<Boolean>() {
            @Override
            public Boolean get() {
                return isClosed;
            }
        }, new PropertyWidget.ValueChanged<Boolean>() {
            @Override
            public void report(Boolean value) {
                if(isClosed != value) {
                    setClosedState(value);
                    SceneUtils.componentUpdated(CurveComponent.this.getGameObject().getGameObjectContainerRoot(), CurveComponent.this.getGameObject(), CurveComponent.this);
                }
            }
        });

        properties.add(cleanButton);
        properties.add(isClosedWidget);

        CheckboxWidget autoSetWidget = new CheckboxWidget("Automatic Control", new Supplier<Boolean>() {
            @Override
            public Boolean get() {
                return automaticControl;
            }
        }, new PropertyWidget.ValueChanged<Boolean>() {
            @Override
            public void report(Boolean value) {
                if(automaticControl != value) {
                    SceneUtils.componentUpdated(CurveComponent.this.getGameObject().getGameObjectContainerRoot(), CurveComponent.this.getGameObject(), CurveComponent.this);

                    automaticControl = value;
                    if(automaticControl) {
                        autoSetAllControlPoints();
                    }
                }
            }
        });
        properties.add(autoSetWidget);

        return properties;
    }

    @Override
    public String getPropertyBoxTitle() {
        return "Curve Component";
    }

    @Override
    public int getPriority() {
        return 2;
    }

    public void movePoint(int touchedPointIndex, float x, float y) {
        Vector2 tmp = Pools.obtain(Vector2.class);
        Vector2 touchedPointRef = points.get(touchedPointIndex);
        tmp.set(x, y).sub(touchedPointRef); // delta move

        if(touchedPointIndex % 3 == 0 || !automaticControl) {
            touchedPointRef.set(x, y);

            if (automaticControl) {
                autoSetAllAffectedControlPoints(touchedPointIndex);
            } else {
                if (touchedPointIndex % 3 == 0) {
                    if (touchedPointIndex + 1 < points.size || isClosed) {
                        points.get(loopIndex(touchedPointIndex + 1)).add(tmp);
                    }
                    if (touchedPointIndex - 1 >= 0 || isClosed) {
                        points.get(loopIndex(touchedPointIndex - 1)).add(tmp);
                    }
                } else {
                    boolean nextPointIsAnchor = (touchedPointIndex + 1) % 3 == 0;
                    int correspondingControlIndex = nextPointIsAnchor ? touchedPointIndex + 2 : touchedPointIndex - 2;
                    int anchorIndex = nextPointIsAnchor ? touchedPointIndex + 1 : touchedPointIndex - 1;

                    if (correspondingControlIndex >= 0 && correspondingControlIndex < points.size || isClosed) {
                        anchorIndex = loopIndex(anchorIndex);
                        correspondingControlIndex = loopIndex(correspondingControlIndex);

                        float distance = tmp.set(points.get(anchorIndex)).sub(points.get(correspondingControlIndex)).len();
                        Vector2 direction = tmp.set(points.get(anchorIndex)).sub(x, y).nor();
                        points.get(correspondingControlIndex).set(points.get(anchorIndex)).add(direction.scl(distance));
                    }
                }
            }

        }

        SceneUtils.componentUpdated(this.getGameObject().getGameObjectContainerRoot(), this.getGameObject(), this);

        Pools.free(tmp);
    }

    @Override
    public void reset() {
        super.reset();
        setToNew();
    }
}
