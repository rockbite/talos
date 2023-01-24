package com.talosvfx.talos.editor.dialogs.preference.widgets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader;
import com.talosvfx.talos.editor.dialogs.preference.widgets.blocks.KeyboardCombinationTypeWidget;
import com.talosvfx.talos.editor.dialogs.preference.widgets.blocks.MouseCombinationTypeWidget;
import com.talosvfx.talos.editor.nodes.widgets.TextValueWidget;
import com.talosvfx.talos.editor.nodes.widgets.ValueWidget;
import com.talosvfx.talos.editor.notifications.commands.CombinationType;
import com.talosvfx.talos.editor.notifications.commands.ModifierKey;
import com.talosvfx.talos.editor.notifications.commands.enums.Commands;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.widgets.ui.common.ColorLibrary;
import com.talosvfx.talos.editor.widgets.ui.common.FileOpenField;
import com.talosvfx.talos.editor.widgets.ui.common.zoomWidgets.LabelWithZoom;
import com.talosvfx.talos.editor.widgets.ui.common.zoomWidgets.SelectBoxWithZoom;
import com.talosvfx.talos.editor.widgets.ui.Styles;
import com.talosvfx.talos.editor.widgets.ui.common.*;
import com.talosvfx.talos.editor.widgets.ui.common.ImageButton;

public class PrefWidgetFactory {


    public static APrefWidget generateWidget(String parentPath, XmlReader.Element item) {

        APrefWidget widget = null;

        if(item.getName().equals("boolean")) {
            widget = new BooleanWidget(parentPath, item);
        }
        if(item.getName().equals("number")) {
            widget = new NumberWidget(parentPath, item);
        }
        if(item.getName().equals("string")) {
            widget = new StringWidget(parentPath, item);
        }
        if(item.getName().equals("path")) {
            widget = new PathWidget(parentPath, item);
        }

        if(widget != null) {

            APrefWidget finalWidget = widget;
            widget.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    finalWidget.write();
                }
            });

            return widget;
        }

        return null;
    }


    public static class KeyInputWidget extends APrefWidget {
        // buttons
        private ObjectMap<ModifierKey, Button> buttonLabelsMap;
        private SquareButton anyButton;

        private CollapsableWidget collapsableWidget;
        private Cell combinationTypeCell;
        private Label configurationNameLabel;
        private SelectBox inputCombinationTypeSelectionBox;
        private Label finalCombination;

        public KeyInputWidget(String parentPath, XmlReader.Element xml) {
            super(parentPath, xml);
        }

        public KeyInputWidget(String parentPath) {
            super(parentPath, null);

            collapsableWidget = new CollapsableWidget() {
                @Override
                protected Table constructContent() {
                    pad(0);

                    // remove background
                    setBackground((Drawable) null);

                    // construct rows
                    final Table modifierButtonsRow = constructModifierButtonsRow();

                    // assemble content
                    content = new Table();
                    content.padLeft(5).padRight(8).defaults().space(6);
                    combinationTypeCell = content.add().growX();
                    content.row();
                    content.add(modifierButtonsRow).growX();
                    return content;
                }

                @Override
                public Table constructTopSegment() {
                    // init components
                    arrowButton = new ArrowButton(false);
                    arrowButton.getCell(arrowButton.getArrowIcon()).pad(0);

                    widgetLabel = new Label("", SharedResources.skin, "small");

            this.selectBox = new SelectBoxWithZoom<>(keyInputWidgetSelectBoxStyle);
            this.keymapBox = new KeymapBox();
                    configurationNameLabel = new Label("", SharedResources.skin, "small");

                    inputCombinationTypeSelectionBox = new SelectBox<>(Styles.keyInputWidgetSelectBoxStyle);

                    finalCombination = new Label("", SharedResources.skin, "small");
                    final Label.LabelStyle labelStyle = new Label.LabelStyle(finalCombination.getStyle());
                    labelStyle.background = ColorLibrary.obtainBackground(ColorLibrary.SHAPE_SQUIRCLE_2, ColorLibrary.BackgroundColor.LIGHT_GRAY);
                    finalCombination.setStyle(labelStyle);
                    finalCombination.setAlignment(Align.center);
                    // only showing visual combination (cant be changed directly)
                    finalCombination.setTouchable(Touchable.disabled);

                    final ImageButton resetToDefaultsButton = new ImageButton(ColorLibrary.obtainBackground(ColorLibrary.SHAPE_SQUIRCLE_2,  ColorLibrary.BackgroundColor.LIGHT_GRAY), SharedResources.skin.getDrawable("icon-arrow-left"));
                    resetToDefaultsButton.getIconCell().pad(1);
                    resetToDefaultsButton.addListener(new ClickListener(){
                        @Override
                        public void clicked(InputEvent event, float x, float y) {
                            super.clicked(event, x, y);
                            // TODO: 05.01.23 reset default settings
                        }
                    });

                    // assemble top segment
                    final Table topSegment = new Table();
                    topSegment.defaults().space(6);
                    topSegment.add(arrowButton).expandY().top();
                    topSegment.add(configurationNameLabel).expand().left().top();
                    topSegment.add(inputCombinationTypeSelectionBox).minWidth(90);
                    topSegment.add(finalCombination).width(90);
                    topSegment.add(resetToDefaultsButton).size(inputCombinationTypeSelectionBox.getHeight());
                    return topSegment;
                }

                @Override
                protected void addListeners() {
                    arrowButton.addListener(initClickListener());
                    arrowButton.addListener(new ClickListener() {
                        @Override
                        public void clicked(InputEvent event, float x, float y) {
                            super.clicked(event, x, y);
                            if (arrowButton.isCollapsed()) {
                                collapsableWidget.setBackground((Drawable) null);
                                getCell(topSegment).pad(0);
                            } else {
                                collapsableWidget.setBackground(ColorLibrary.obtainBackground(ColorLibrary.SHAPE_SQUIRCLE_2, ColorLibrary.BackgroundColor.DARK_GRAY));
                                getCell(topSegment).pad(5).padBottom(3);
                            }
                        }
                    });
                }
            };
            add(collapsableWidget).growX();

            inputCombinationTypeSelectionBox.setItems(CombinationType.values());
            // update combination type when type selected
            inputCombinationTypeSelectionBox.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    setInputCombinationType((CombinationType) inputCombinationTypeSelectionBox.getSelection().first());
                }
            });
            setInputCombinationType((CombinationType) inputCombinationTypeSelectionBox.getSelection().first());
        }

        private void setInputCombinationType (CombinationType combinationType) {
            if (combinationType == CombinationType.MOUSE) combinationTypeCell.setActor(new MouseCombinationTypeWidget());
            if (combinationType == CombinationType.KEYBOARD) combinationTypeCell.setActor(new KeyboardCombinationTypeWidget());
        }

        private Table constructModifierButtonsRow () {
            buttonLabelsMap = new ObjectMap<>();

            final Table modifierButtonsRow = new Table();
            modifierButtonsRow.defaults().space(10);

            // init button labels
            // adding "any button" for activating all other buttons but the empty one
            final Label anyButtonLabel = new Label("Any", SharedResources.skin, "small");
            anyButton = new SquareButton(SharedResources.skin, anyButtonLabel, "");
            anyButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    super.clicked(event, x, y);
                    anyPressed();
                }
            });
            modifierButtonsRow.add(anyButton);

            // adding modifier key buttons
            for (ModifierKey modifierKey : ModifierKey.values()) {
                final Label buttonLabel = new Label(modifierKey.name(), SharedResources.skin, "small");
                final SquareButton button = new SquareButton(SharedResources.skin, buttonLabel, "");
                button.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        super.clicked(event, x, y);
                        buttonPressed(modifierKey);
                    }
                });
                buttonLabelsMap.put(modifierKey, button);
                modifierButtonsRow.add(button);
            }

            modifierButtonsRow.add().expandX();
            return modifierButtonsRow;
        }

        // button
        private void anyPressed () {
            for (ObjectMap.Entry<ModifierKey, Button> modifierKeySquareButtonEntry : buttonLabelsMap) {
                modifierKeySquareButtonEntry.value.setChecked(anyButton.isChecked());
                buttonPressed(modifierKeySquareButtonEntry.key);
            }
        }

        private void buttonPressed (ModifierKey modifierKey) {

        }

        @Override
        protected void fromString(String str) {

        }

        @Override
        protected String writeString() {
            return null;
        }

        public void configure(Commands.CommandType commandType) {
            configurationNameLabel.setText(commandType.name);
            // TODO: 05.01.23 get command combination
            finalCombination.setText(commandType.toString());
        }
    }

    public static class BooleanWidget extends PrefRowWidget {

        private final CheckBox checkBoxWidget;

        public BooleanWidget(String parentPath, XmlReader.Element xml) {
            super(parentPath, xml);
            if(xml.hasAttribute("label")) {
                Label label = new LabelWithZoom(xml.getAttribute("label"), SharedResources.skin);
                leftContent.add(label).right().expandX();
            }
            checkBoxWidget = new CheckBox(xml.getText(), SharedResources.skin, "rounded-checkbox");
            checkBoxWidget.setProgrammaticChangeEvents(false);
            rightContent.add(checkBoxWidget).left().expandX();
        }

        @Override
        protected void fromString(String str) {
            checkBoxWidget.setChecked(Boolean.parseBoolean(str));
        }

        @Override
        protected String writeString() {
            return Boolean.toString(checkBoxWidget.isChecked());
        }
    }

    public static class NumberWidget extends PrefRowWidget {
        private final ValueWidget valueWidget;

        public NumberWidget(String parentPath, XmlReader.Element xml) {
            super(parentPath, xml);
            Label label = new LabelWithZoom(xml.getText(), SharedResources.skin);
            leftContent.add(label).right().expandX();

            valueWidget = new ValueWidget(SharedResources.skin);
            rightContent.add(valueWidget).left().expandX().padLeft(7).padBottom(5);

            float min = 0;
            float max = 9999;
            if(xml.hasAttribute("min")) {
                min = Float.parseFloat(xml.getAttribute("min"));
            }
            if(xml.hasAttribute("max")) {
                max = Float.parseFloat(xml.getAttribute("max"));
            }
            if(xml.hasAttribute("step")) {
                float step = Float.parseFloat(xml.getAttribute("step"));
                valueWidget.setStep(step);
            }
            if(xml.hasAttribute("min") || xml.hasAttribute("max")) {
                valueWidget.setRange(min, max);
                valueWidget.setShowProgress(xml.getBooleanAttribute("progress", true));
            }
        }

        @Override
        protected void fromString(String str) {
            valueWidget.setValue(Float.parseFloat(str));
        }

        @Override
        protected String writeString() {
            return Float.toString(valueWidget.getValue());
        }
    }

    public static class StringWidget extends PrefRowWidget {
        private final TextValueWidget widget;

        public StringWidget(String parentPath, XmlReader.Element xml) {
            super(parentPath, xml);
            Label label = new LabelWithZoom(xml.getText(), SharedResources.skin);
            leftContent.add(label).right().expandX();

            widget = new TextValueWidget(SharedResources.skin);
            rightContent.add(widget).left().expandX().padLeft(7).padBottom(5);
        }

        @Override
        protected void fromString(String str) {
            widget.setValue(str, false);
        }

        @Override
        protected String writeString() {
            return widget.getValue();
        }
    }

    public static class PathWidget extends PrefRowWidget {


        private final FileOpenField fileOpener;

        public PathWidget(String parentPath, XmlReader.Element xml) {
            super(parentPath, xml);

            Label label = new LabelWithZoom(xml.getText(), SharedResources.skin);
            leftContent.add(label).right().expandX();

            fileOpener = new FileOpenField();
            fileOpener.getInputContainer().setBackground(ColorLibrary.obtainBackground(SharedResources.skin, ColorLibrary.SHAPE_SQUIRCLE_LEFT, ColorLibrary.BackgroundColor.SUPER_DARK_GRAY));
            rightContent.add(fileOpener).left().expand().growX().padLeft(5).padRight(5).padBottom(5);
        }

        @Override
        protected void fromString(String str) {
            if(str.equals("{usr}")) {
                str = Gdx.files.absolute(System.getProperty("user.home")).file().getAbsolutePath();
            }
            fileOpener.setPath(str);
        }

        @Override
        protected String writeString() {
            return fileOpener.getPath();
        }
    }
}
