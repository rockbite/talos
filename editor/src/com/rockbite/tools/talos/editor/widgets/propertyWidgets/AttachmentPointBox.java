package com.rockbite.tools.talos.editor.widgets.propertyWidgets;

import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.spine.Bone;
import com.rockbite.tools.talos.editor.addons.bvb.AttachmentPoint;
import com.rockbite.tools.talos.editor.widgets.ui.NumericalValueField;

public class AttachmentPointBox extends Table {

    AttachmentPoint point;

    // Scene2D stuff
    Stack stack;
    BoneWidget boneWidget;

    SlotWidget slotWidget;
    ImageButton typeToggleButton;

    NumericalValueField numericalValueField;

    protected class BoneWidget extends Table {

        SelectBox<TruncatedString> boneList;
        ImageButton attachmentTypeToggle;

        public BoneWidget(Skin skin) {
            setSkin(skin);
            boneList = new SelectBox<>(getSkin());
            attachmentTypeToggle = new ImageButton(getSkin());

            add(boneList);
            add(attachmentTypeToggle);
        }

        public BoneWidget(Skin skin, AttachmentPoint.AttachmentType attachmentType) {
            setSkin(skin);
            boneList = new SelectBox<>(getSkin(), "propertyValue");
            attachmentTypeToggle = new ImageButton(getSkin());

            add(boneList).height(25f).growX();
            attachmentTypeToggle.setChecked(attachmentType == AttachmentPoint.AttachmentType.POSITION);
        }

        public void setBoneList(Array<Bone> bones) {
            Array<TruncatedString> list = new Array<>();
            for(Bone bone: bones) {
                list.add(new TruncatedString(bone.getData().getName(), 32));
            }
            boneList.setItems(list);
        }

        public void setSelectedBone(String boneName) {
            boneList.setSelected(new TruncatedString(boneName, 32));
        }
    }

    protected class SlotWidget extends Table {

        public SlotWidget(CharSequence text, Skin skin) {
            setSkin(skin);

            setBackground(skin.getDrawable("panel_button_bg"));

            Label label = new Label(text, skin);
            label.setAlignment(Align.center);
            Label.LabelStyle style = new Label.LabelStyle();
            style.fontColor = label.getStyle().fontColor;
            style.font = label.getStyle().font;
            label.setStyle(style);

            add(label).center().height(25f).padLeft(6f).padRight(6f);
        }
    }

    public AttachmentPointBox(Skin skin) {
        setSkin(skin);
        stack = new Stack();
        stack.add(numericalValueField);
        stack.add(boneWidget);

        slotWidget = new SlotWidget("", getSkin());
        boneWidget = new BoneWidget(getSkin());
        typeToggleButton = new ImageButton(getSkin());

        add(slotWidget).padRight(6f);
        add(stack);
        add(typeToggleButton);
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


    public void setData(AttachmentPoint point) {
        this.point = point;
        if(point.getType() == AttachmentPoint.Type.ATTACHED) {
            boneWidget.setSelectedBone(point.getBoneName());
        } else {

        }
    }


    public void setBoneList(Array<Bone> bones) {
        boneWidget.setBoneList(bones);
    }

}
