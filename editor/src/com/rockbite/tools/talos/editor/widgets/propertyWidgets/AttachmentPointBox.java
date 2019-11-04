package com.rockbite.tools.talos.editor.widgets.propertyWidgets;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.spine.Bone;
import com.rockbite.tools.talos.editor.addons.bvb.AttachmentPoint;
import com.rockbite.tools.talos.editor.widgets.ui.BackgroundButton;
import com.rockbite.tools.talos.editor.widgets.ui.NumericalValueField;

import javax.swing.event.ChangeEvent;

public class AttachmentPointBox extends Table {

    AttachmentPoint point;

    // Scene2D stuff
    Stack stack;
    BoneWidget boneWidget;

    SlotWidget slotWidget;
    BackgroundButton typeToggleButton;
    BackgroundButton deleteButton;

    NumericalValueField numericalValueField;

    protected class BoneWidget extends Table {

        AttachmentPoint point;

        SelectBox<String> boneList;
        BackgroundButton attachmentTypeToggle;

        public BoneWidget(Skin skin) {
            setSkin(skin);
            boneList = new SelectBox<>(getSkin(), "propertyValue");
            attachmentTypeToggle = new BackgroundButton(getSkin(), getSkin().getDrawable("ic-target"), getSkin().getDrawable("ic-refresh"));

            add(boneList).height(25f).growX().minWidth(10).prefWidth(52).padRight(6f);
            add(attachmentTypeToggle);
            addListeners();
        }

        public BoneWidget(Skin skin, AttachmentPoint.AttachmentType attachmentType) {
            setSkin(skin);
            boneList = new SelectBox<>(getSkin(), "propertyValue");
            attachmentTypeToggle = new BackgroundButton(getSkin(), getSkin().getDrawable("ic-target"), getSkin().getDrawable("ic-refresh"));

            add(boneList).height(25f).growX().minWidth(10).prefWidth(52);
            attachmentTypeToggle.button.setChecked(attachmentType != AttachmentPoint.AttachmentType.POSITION);
            addListeners();
        }

        private void addListeners() {
            attachmentTypeToggle.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if(point != null) {
                        if(attachmentTypeToggle.button.isChecked()) {
                            point.setATAngle();
                        } else {
                            point.setATPosition();
                        }
                    }
                }
            });
        }

        public void setBoneList(Array<String> list) {
            boneList.setItems(list);
        }

        public void setSelectedBone(String boneName) {
            boneList.setSelected(boneName);
        }

        public void setAttachmentType(AttachmentPoint.AttachmentType attachmentType) {
            attachmentTypeToggle.button.setChecked(attachmentType != AttachmentPoint.AttachmentType.POSITION);
        }

        public void setPoint(AttachmentPoint point) {
            this.point = point;
        }
    }

    protected class SlotWidget extends Table {

        Label label;

        public SlotWidget(CharSequence text, Skin skin) {
            setSkin(skin);

            setBackground(skin.getDrawable("panel_button_bg"));

            label = new Label(text, skin);
            label.setAlignment(Align.center);
            Label.LabelStyle style = new Label.LabelStyle();
            style.fontColor = label.getStyle().fontColor;
            style.font = label.getStyle().font;
            label.setStyle(style);

            add(label).center().height(25f).padLeft(6f).padRight(6f);
        }

        public void setSlotId(int id) {
            if(id == -1) {
                label.setText("position");
            } else {
                label.setText(id);
            }
        }
    }

    public AttachmentPointBox(Skin skin, final GlobalValueListContainer container) {
        setSkin(skin);

        numericalValueField = new NumericalValueField(getSkin());
        slotWidget = new SlotWidget("0", getSkin());
        boneWidget = new BoneWidget(getSkin());
        typeToggleButton = new BackgroundButton(getSkin(), getSkin().getDrawable("ic-chain"), getSkin().getDrawable("ic-settings"));
        deleteButton = new BackgroundButton(getSkin(), getSkin().getDrawable("ic-trash-red"));

        stack = new Stack();
        stack.add(numericalValueField);
        stack.add(boneWidget);

        add(slotWidget).padRight(6f).width(32);
        add(stack).expandX().growX();
        add(typeToggleButton).padLeft(6f).height(25);
        add(deleteButton).padLeft(6f).height(25);

        boneWidget.setVisible(false);

        typeToggleButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                toggleAttachType();
            }
        });

        deleteButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                container.deletePoint(point);
            }
        });
    }

    /**
     * bone only version
     * @param skin
     * @param name
     */
    public AttachmentPointBox(Skin skin, String name) {
        setSkin(skin);

        slotWidget = new SlotWidget(name, getSkin());
        boneWidget = new BoneWidget(getSkin(), AttachmentPoint.AttachmentType.POSITION);

        add(slotWidget).padRight(6f);
        add(boneWidget).growX();
    }

    private void toggleAttachType() {
        if(point.getType() == AttachmentPoint.Type.STATIC) {
            point.setTypeAttached();
            boneWidget.setVisible(true);
            numericalValueField.setVisible(false);
        } else {
            point.setTypeStatic();
            boneWidget.setVisible(false);
            numericalValueField.setVisible(true);
        }
    }


    public void setData(AttachmentPoint point) {
        this.point = point;
        if(point.getType() == AttachmentPoint.Type.ATTACHED) {
            boneWidget.setVisible(true);
            if(numericalValueField != null) {
                numericalValueField.setVisible(false);
            }
            boneWidget.setSelectedBone(point.getBoneName());
        } else {
            boneWidget.setVisible(false);
            numericalValueField.setVisible(true);
        }

        slotWidget.setSlotId(point.getSlotId());
        boneWidget.setPoint(point);
        boneWidget.setAttachmentType(point.getAttachmentType());
    }


    public SlotWidget getSlotWidget() {
        return slotWidget;
    }

    public void setSlotIndex(int index) {
        point.setSlotId(index);
        slotWidget.setSlotId(index);
    }

    public void setBoneList(Array<String> boneNames) {
        boneWidget.setBoneList(boneNames);
    }

}
