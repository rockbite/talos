package com.talosvfx.talos.editor.dialogs.preference.widgets;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.XmlReader;
import com.talosvfx.talos.editor.nodes.widgets.ValueWidget;
import com.talosvfx.talos.editor.notifications.GlobalActions;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.widgets.ui.common.CollapsableWidget;
import com.talosvfx.talos.editor.widgets.ui.common.ColorLibrary;

public class PrefWidgetFactory {


    public static APrefWidget generateWidget(String parentPath, XmlReader.Element item) {

        APrefWidget widget = null;

        if(item.getName().equals("boolean")) {
            widget = new BooleanWidget(parentPath, item);
        }
        if(item.getName().equals("number")) {
            widget = new NumberWidget(parentPath, item);
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
        private CheckBox checkBox;
        private SelectBox selectBox;
        private KeymapBox keymapBox;
        public KeyInputWidget(String parentPath, XmlReader.Element xml) {
            super(parentPath, xml);
        }

        public KeyInputWidget(String parentPath) {
            super(parentPath, null);

            build();

            // TODO: 27.12.22 remove later, added for testing
            selectBox.setItems("selection 1", "selection 2", "selection 3", "selection 4");
        }

        private void build() {
            this.checkBox = new CheckBox("checkboxtext", SharedResources.skin);

            final SelectBox.SelectBoxStyle keyInputWidgetSelectBoxStyle = new SelectBox.SelectBoxStyle(SharedResources.skin.get(SelectBox.SelectBoxStyle.class));
            keyInputWidgetSelectBoxStyle.font = SharedResources.skin.getFont("small-font");
            keyInputWidgetSelectBoxStyle.listStyle.font = SharedResources.skin.getFont("small-font");
            keyInputWidgetSelectBoxStyle.background = ColorLibrary.obtainBackground(ColorLibrary.SHAPE_SQUIRCLE_2, ColorLibrary.BackgroundColor.ULTRA_DARK_GRAY);
            keyInputWidgetSelectBoxStyle.backgroundOver = ColorLibrary.obtainBackground(ColorLibrary.SHAPE_SQUIRCLE_2, ColorLibrary.BackgroundColor.SUPER_DARK_GRAY);
            keyInputWidgetSelectBoxStyle.backgroundOpen = ColorLibrary.obtainBackground(ColorLibrary.SHAPE_SQUIRCLE_TOP_2, ColorLibrary.BackgroundColor.LIGHT_BLUE);
            keyInputWidgetSelectBoxStyle.listStyle.selection = ColorLibrary.obtainBackground(ColorLibrary.SHAPE_SQUIRCLE_2, ColorLibrary.BackgroundColor.LIGHT_BLUE);
            keyInputWidgetSelectBoxStyle.listStyle.background = ColorLibrary.obtainBackground(ColorLibrary.SHAPE_SQUIRCLE_2, ColorLibrary.BackgroundColor.ULTRA_DARK_GRAY);
            keyInputWidgetSelectBoxStyle.scrollStyle.background =ColorLibrary.obtainBackground(ColorLibrary.SHAPE_SQUIRCLE_BOTTOM_2, ColorLibrary.BackgroundColor.ULTRA_DARK_GRAY);

            this.selectBox = new SelectBox<>(keyInputWidgetSelectBoxStyle);
            this.keymapBox = new KeymapBox();

            // NOTE: pads are added to top segment not the entire panel so the click listener also registered paddings
            pad(5, 10, 5, 8).defaults().space(8);

            // assemble top segment
            add(checkBox).expandX().left();
            add(selectBox).minWidth(90);
            add(keymapBox).width(90);
        }

        @Override
        protected void fromString(String str) {

        }

        @Override
        protected String writeString() {
            return null;
        }

        public void configure(GlobalActions action) {
            keymapBox.setKey(action.name());
        }
    }

    public static class BooleanWidget extends PrefRowWidget {

        private final CheckBox checkBoxWidget;

        public BooleanWidget(String parentPath, XmlReader.Element xml) {
            super(parentPath, xml);
            if(xml.hasAttribute("label")) {
                Label label = new Label(xml.getAttribute("label"), SharedResources.skin);
                leftContent.add(label).right().expandX();
            }
            checkBoxWidget = new CheckBox(xml.getText(), SharedResources.skin, "rounded-checkbox");
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
            Label label = new Label(xml.getText(), SharedResources.skin);
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
                valueWidget.setShowProgress(true);
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
}
