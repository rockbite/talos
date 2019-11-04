package com.rockbite.tools.talos.editor.widgets.propertyWidgets;

import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.CollapsibleWidget;
import com.rockbite.tools.talos.editor.addons.bvb.AttachmentPoint;

public class GlobalValueListContainer extends Table {

    CustomList listContainer;
    CollapsibleWidget collapsibleWidget;
    Label titleLabel;
    Button collapseButton;
    Button addRowButton;
    Button deleteRowButton;

    private Array<AttachmentPoint> tempArray = new Array<>();
    private Array<String> boneNames = new Array<>();

    Table topTable;
    Table mainTable;


    public GlobalValueListContainer(Skin skin) {
        setSkin(skin);
        build();
    }

    private void build() {
        Table topTable = new Table();
        titleLabel = new Label("attachments", getSkin());
        collapseButton = new ImageButton(getSkin().getDrawable("panel-collapse-down"));
        topTable.add(collapseButton);
        titleLabel.setAlignment(Align.left);
        topTable.add(titleLabel).expandX().left().padLeft(10);

        mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.setBackground(getSkin().getDrawable("panel_button_bg"));

        listContainer = new CustomList(getSkin());
        mainTable.add(listContainer).growX();

        addRowButton = new ImageButton(getSkin().getDrawable("ic-input-file-add"));
        deleteRowButton = new ImageButton(getSkin().getDrawable("ic-input-file-delete"));

        mainTable.row();
        Table bottomButtons = new Table();
        bottomButtons.add(addRowButton).left();
        bottomButtons.add(deleteRowButton).left();
        mainTable.add(bottomButtons).left();

        collapsibleWidget = new CollapsibleWidget(mainTable, false);
        add(topTable).growX();
        row();
        add(collapsibleWidget).grow().padTop(10);

        addListeners();
    }

    private void addListeners () {
        /*
        collapseButton.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                boolean checked = collapseButton.isChecked();
                Skin skin = TalosMain.Instance().getSkin();
                collapseButton.setBackground(checked ? skin.getDrawable("panel-collapse-down") : skin.getDrawable("panel-collapse-right"));
                collapsibleWidget.setCollapsed(checked);
            }
        });

        addRowButton.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                addNewRow();
            }
        });

        deleteRowButton.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                globalValueList.removeSelected();
            }
        });
         */
    }

    public void setData(Array<AttachmentPoint> attachmentPoints) {
        for(AttachmentPoint attachmentPoint: attachmentPoints) {
            AttachmentPointBox attachmentPointBox = new AttachmentPointBox(getSkin());
            listContainer.addItem(attachmentPointBox);
        }
    }
}
