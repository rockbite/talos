package com.rockbite.tools.talos.editor.widgets.propertyWidgets;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.spine.Bone;
import com.rockbite.tools.talos.editor.addons.bvb.AttachmentPoint;

public class GlobalValueListContainer extends Table {

    Array<AttachmentPoint> attachmentPoints = new Array<>();
    private CustomList<AttachmentPointBox> list;

    Array<String> boneNameList = new Array<>();

    public GlobalValueListContainer(Skin skin) {
        setSkin(skin);
        build();
    }

    private void build() {

        Table main = new Table();
        list = new CustomList<>(getSkin());
        list.setBackground(getSkin().getDrawable("dynamic_list_bg"));

        Stack stack = new Stack();
        Table listContainer = new Table();
        Table bottomPanelContainer = new Table();
        Table bottomPanel = new Table();
        stack.add(listContainer);
        stack.add(bottomPanelContainer);

        bottomPanelContainer.add().expandY().row();
        bottomPanelContainer.add(bottomPanel).padBottom(2f).left().expandX();

        ImageButton newRowButton = new ImageButton(getSkin().getDrawable("ic-input-file-add"));
        bottomPanel.add(newRowButton).left().padLeft(2);
        ImageButton deleteRowButton = new ImageButton(getSkin().getDrawable("ic-input-file-delete"));
        bottomPanel.add(deleteRowButton).left().padLeft(2);
        bottomPanel.add().expandX();

        listContainer.add(list).grow().minHeight(90);
        main.add(stack).grow();
        add(main).growX();

        newRowButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                addNewRow();
            }
        });

        deleteRowButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                deleteSelectedRow();
            }
        });

    }

    private void addNewRow() {
        AttachmentPoint newPoint = new AttachmentPoint();
        AttachmentPointBox attachmentPointBox = createBoxWidget(newPoint, false);
        attachmentPoints.add(newPoint);
        list.addItem(attachmentPointBox);
    }

    private void deleteSelectedRow() {

    }

    public void setData(Array<AttachmentPoint> attachmentPoints) {
        list.clearItems(false);
        for(AttachmentPoint attachmentPoint: attachmentPoints) {
            AttachmentPointBox attachmentPointBox = createBoxWidget(attachmentPoint, true);
            list.addItem(attachmentPointBox);
        }
        this.attachmentPoints = attachmentPoints;
    }

    public void setBoneList(Array<Bone> boneList) {
        boneNameList.clear();
        for(Bone bone: boneList) {
            boneNameList.add(bone.getData().getName());
        }
    }

    private AttachmentPointBox createBoxWidget(AttachmentPoint point, boolean skipSlotData) {
        final AttachmentPointBox attachmentPointBox = new AttachmentPointBox(getSkin());
        attachmentPointBox.setBoneList(boneNameList);
        attachmentPointBox.setData(point);

        if(!skipSlotData) {
            int index = getAvailableSlotIndex();
            attachmentPointBox.setSlotIndex(index);
        }

        attachmentPointBox.getSlotWidget().addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
               int index = getAvailableSlotIndex();
                attachmentPointBox.setSlotIndex(index);
            }
        });

        return attachmentPointBox;
    }

    private int getAvailableSlotIndex() {
        int indexToTry = 0;
        boolean found = false;
        // some aids going on here. better implementations from good samaritans are welcome
        while(!found) {
            boolean contains = false;
            for (AttachmentPoint point : attachmentPoints) {
                if(point.getSlotId() == indexToTry) {
                    contains = true;
                    break;
                }
            }
            if(!contains) {
                found = true;
                return indexToTry;
            } else {
                indexToTry++;
                if(indexToTry > 10) {
                    return -1;
                }
            }
        }

        return indexToTry;
    }
}
