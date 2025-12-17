package com.talosvfx.talos.editor.nodes.widgets;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;
import com.badlogic.gdx.utils.*;
import com.talosvfx.talos.editor.addons.scene.SceneEditorWorkspace;
import com.talosvfx.talos.editor.addons.scene.apps.routines.nodes.RoutineExecuteNodeWidget;
import com.talosvfx.talos.editor.addons.scene.widgets.GameObjectListPopup;
import com.talosvfx.talos.editor.widgets.ui.FilteredTree;
import com.talosvfx.talos.editor.widgets.ui.common.ColorLibrary;
import com.talosvfx.talos.editor.widgets.ui.common.SquareButton;
import com.talosvfx.talos.editor.widgets.ui.common.zoomWidgets.LabelWithZoom;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.runtime.routine.RoutineInstance;
import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.runtime.scene.Scene;


public class GOSelectionWidget extends AbstractWidget<String> {

    // label widget, default mode
    private Table GOsLabelDefaultModeTable;
    private LabelWithZoom GOsNameLabel;
    private LabelWithZoom typeLabel;
    // textField widget, edit mode
    private Table GOsLabelEditingModeTable;
    private TextField GOsNameTextField;
    // connect two modes
    private Stack currentModeWrapper;

    private Button selectGOButton;

    // local state
    private boolean isSelected;
    private boolean isHover;

    private Stage stageRef;
    private EventListener stageListener;

    // go selection widget
    private Predicate<FilteredTree.Node<GameObject>> filter;

    // data
    private String value;
    private RoutineInstance routineInstanceRef;
    private GameAsset<Scene> asset;
    private RoutineExecuteNodeWidget routineExecuteNodeWidget;

    public GOSelectionWidget() {
        super();

        filter = new Predicate<FilteredTree.Node<GameObject>>() {
			@Override
			public boolean evaluate (FilteredTree.Node<GameObject> node) {
				return true;
			}
		};

        GOsLabelDefaultModeTable = new Table();
        GOsLabelEditingModeTable = new Table();
        currentModeWrapper = new Stack(GOsLabelDefaultModeTable, GOsLabelEditingModeTable);
    }

    @Override
    public void init (Skin skin) {
        super.init(skin);

        typeLabel = new LabelWithZoom("Target", skin);
        GOsNameLabel = new LabelWithZoom("Sadge", skin);
        GOsNameLabel.setEllipsis(true);
        GOsNameLabel.setAlignment(Align.right);
        GOsLabelDefaultModeTable.add(typeLabel).padLeft(12);
        GOsLabelDefaultModeTable.add(GOsNameLabel).width(100).padLeft(10).padRight(12);

        GOsNameTextField = new TextField("", skin, "no-bg");
        GOsLabelEditingModeTable.add(GOsNameTextField);

        selectGOButton = new SquareButton(skin, skin.getDrawable("ic-file-edit"), "Select game object");

        content.add(currentModeWrapper).growX().height(32);
        content.add(selectGOButton).padLeft(20);

        hideEditMode();
        setTouchable(Touchable.enabled);

        addListener(new ClickListener() {

            private boolean dragged = false;

            private float lastPos = 0;

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                super.enter(event, x, y, pointer, fromActor);
                isHover = true;
                setBgs();
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                super.exit(event, x, y, pointer, toActor);
                isHover = false;
                if(pointer == -1) {
                    setBgs();
                }
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                dragged = false;
                lastPos = x;
                return super.touchDown(event, x, y, pointer, button);
            }


            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);
                setBgs();
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                if(!dragged) {
                    showEditMode();
                }
            }
        });

        GOsNameTextField.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
            if (SceneEditorWorkspace.isEnterPressed(keycode)) {
                hideEditMode();
            }

            return super.keyDown(event, keycode);
            }
        });

        GOsNameTextField.addListener(new FocusListener() {
            @Override
            public void keyboardFocusChanged(FocusEvent event, Actor actor, boolean focused) {
            super.keyboardFocusChanged(event, actor, focused);
            if(!focused) {
                hideEditMode();
            }
            }
        });

        stageListener = new InputListener() {
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                Vector2 tmpVec = new Vector2();
                tmpVec.set(x, y);
                GOSelectionWidget.this.stageToLocalCoordinates(tmpVec);
                Actor touchTarget = GOSelectionWidget.this.hit(tmpVec.x, tmpVec.y, false);
                if (touchTarget == null) {
                    if (getStage() != null) {
                        getStage().setKeyboardFocus(null);
                    }
                }

                return false;
            }
        };

        selectGOButton.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				Vector2 pos = new Vector2(selectGOButton.getWidth() / 2f, selectGOButton.getHeight() / 2f);
                selectGOButton.localToStageCoordinates(pos);

                GameAssetWidget assetWidget = (GameAssetWidget) routineExecuteNodeWidget.getWidget("scene");
                GameAsset<Scene> sceneAsset = assetWidget.getValue();

                if (sceneAsset == null || sceneAsset.getResource() == null) {
                    return;
                }

                GameObjectListPopup gameObjectListPopup = new GameObjectListPopup();
                GameObject rootGO = sceneAsset.getResource().root.getSelfObject();
                gameObjectListPopup.showPopup(getStage(), rootGO, pos, filter, new FilteredTree.ItemListener<GameObject>() {

					@Override
					public void selected (FilteredTree.Node<GameObject> node) {
						GameObject gameObject = node.getObject();

                        if (gameObject == rootGO) {
                            return;
                        }

                        // construct path
                        CharArray path = new CharArray();
                        while (gameObject != null && rootGO != gameObject) {
                            path.insert(0, gameObject.getName());
                            path.insert(0, ".");
                            gameObject = gameObject.parent;
                        }

                        // remove last dot
                        path.deleteCharAt(0);

                        setValue(path.toString(), true);

						gameObjectListPopup.remove();
					}
				});

			}
		});
    }

    private void setBgs() {
        ColorLibrary.BackgroundColor color = ColorLibrary.BackgroundColor.LIGHT_GRAY;
        if (isSelected) {
            color = ColorLibrary.BackgroundColor.MID_GRAY;
        } else if (isHover) {
            color = ColorLibrary.BackgroundColor.BRIGHT_GRAY;
        }

        GOsLabelDefaultModeTable.setBackground(ColorLibrary.obtainBackground(getSkin(), ColorLibrary.SHAPE_SQUIRCLE, color));
        GOsLabelEditingModeTable.setBackground(ColorLibrary.obtainBackground(getSkin(), ColorLibrary.SHAPE_SQUIRCLE, color));
    }

    @Override
    protected void setStage(Stage stage) {
        super.setStage(stage);
        if (stage != null) {
            getStage().getRoot().addCaptureListener(stageListener);
            stageRef = getStage();
        } else {
            if(stageRef != null) {
                stageRef.getRoot().removeCaptureListener(stageListener);
                stageRef = null;
            }
        }
    }

    private void showEditMode() {
        if(getStage() == null) return;

        GOsLabelDefaultModeTable.setVisible(false);
        GOsLabelEditingModeTable.setVisible(true);

        getStage().setKeyboardFocus(GOsNameTextField);
        GOsNameTextField.selectAll();

        isSelected = true;
        setBgs();
    }

    private void hideEditMode() {
        GOsLabelDefaultModeTable.setVisible(true);
        GOsLabelEditingModeTable.setVisible(false);

        GOsNameTextField.clearSelection();

        setValue(GOsNameTextField.getText());

        isSelected = false;
        setBgs();
    }

    @Override
    public boolean isFastChange() {
        return isSelected;
    }

    public void setGoName(String name) {
        GOsNameLabel.setText(name);
    }

    public void setValue(String text) {
        setValue(text, isChanged(text));
    }

    public void setRoutineExecuteNodeWidget(RoutineExecuteNodeWidget routineExecuteNodeWidget) {
        this.routineExecuteNodeWidget = routineExecuteNodeWidget;
    }

    public void setValue(String text, boolean notify) {
        GOsNameLabel.setText(text);
        GOsNameTextField.setText(text);

        value = text;

        if (notify) {
            fireChangedEvent();
        }
    }

    @Override
    public void loadFromXML(XmlReader.Element element) {
        String text = element.getText();
        String defaultValue = element.getAttribute("default", "");

        setValue(defaultValue);

        setGoName(text);
    }

    @Override
    public String getValue () {
        return value;
    }

    @Override
    public void read (Json json, JsonValue jsonValue) {
        setValue(jsonValue.asString(), false);
    }

    @Override
    public void write (Json json, String name) {
        json.writeValue(name, getValue());
    }
}
