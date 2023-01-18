package com.talosvfx.talos.editor.widgets.propertyWidgets;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.addons.bvb.AttachmentPoint;
import com.talosvfx.talos.editor.addons.bvb.AttachmentTypeToggle;
import com.talosvfx.talos.editor.widgets.ui.BackgroundButton;
import com.talosvfx.talos.editor.widgets.ui.NumericalValueField;
import com.talosvfx.talos.editor.widgets.ui.common.zoomWidgets.LabelWithZoom;
import com.talosvfx.talos.editor.widgets.ui.common.zoomWidgets.SelectBoxWithZoom;

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
        AttachmentTypeToggle attachmentTypeToggle;

        public BoneWidget(Skin skin) {
            setSkin(skin);
            boneList = new SelectBoxWithZoom<>(getSkin(), "propertyValue");
            attachmentTypeToggle = new AttachmentTypeToggle(getSkin());

            add(boneList).height(25f).growX().minWidth(10).prefWidth(52).padRight(6f);
            add(attachmentTypeToggle);
            addListeners();
        }

        public BoneWidget(Skin skin, AttachmentPoint.AttachmentType attachmentType) {
            setSkin(skin);
            boneList = new SelectBoxWithZoom<>(getSkin(), "propertyValue");
            attachmentTypeToggle = new AttachmentTypeToggle(getSkin());

            add(boneList).height(25f).growX().minWidth(10).prefWidth(52);
            addListeners();
        }

        private void addListeners() {
            attachmentTypeToggle.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if(point != null) {
                        point.setAttachmentType(attachmentTypeToggle.getAttachmentType());
                    }
                }
            });


            boneList.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    // change the bone
                    if(point == null) return;
                    Vector2 pos = new Vector2();
//                    Bone bone = ((BvBAddon) TalosMain.Instance().Addons().getAddon(BvBAddon.class)).getWorkspace().getSkeletonContainer().getBoneByName(boneList.getSelected());
//                    pos.sub(bone.getWorldX(), bone.getWorldY());
//                    float boneWorldScale = bone.getWorldScaleX();
//                    point.setOffset(pos.x / boneWorldScale, pos.y / boneWorldScale);
//                    point.setBone(bone.getData().getName());
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
            attachmentTypeToggle.setAttachmentType(attachmentType);
        }

        public void setPoint(AttachmentPoint point) {
            this.point = point;
        }
    }

    protected class SlotWidget extends Table {

        LabelWithZoom label;

        public SlotWidget(CharSequence text, Skin skin) {
            setSkin(skin);

            setBackground(skin.getDrawable("panel_button_bg"));

            label = new LabelWithZoom(text, skin);
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
        typeToggleButton = new BackgroundButton(getSkin(), getSkin().getDrawable("icon-chain"), getSkin().getDrawable("icon-edit"));
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


    public void setData (AttachmentPoint point) {
        this.point = point;
        if(point.getType() == AttachmentPoint.Type.ATTACHED) {
            boneWidget.setVisible(true);
            if(numericalValueField != null) {
                numericalValueField.setVisible(false);
                numericalValueField.setNumericalValue(point.getStaticValue());
            }
            boneWidget.setSelectedBone(point.getBoneName());
        } else {
            boneWidget.setVisible(false);
            numericalValueField.setVisible(true);
            numericalValueField.setNumericalValue(point.getStaticValue());
        }

        slotWidget.setSlotId(point.getSlotId());
        boneWidget.setPoint(point);
        boneWidget.setAttachmentType(point.getAttachmentType());
    }


    public SlotWidget getSlotWidget () {
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
