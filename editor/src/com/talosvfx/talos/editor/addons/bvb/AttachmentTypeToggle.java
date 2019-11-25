package com.talosvfx.talos.editor.addons.bvb;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.OrderedMap;

public class AttachmentTypeToggle extends Table {

    public ImageButton button;

    public OrderedMap<AttachmentPoint.AttachmentType, Drawable> drawableMap = new OrderedMap<>();

    private AttachmentPoint.AttachmentType currentType = AttachmentPoint.AttachmentType.POSITION;

    public AttachmentTypeToggle(Skin skin) {
        setSkin(skin);
        setBackground(getSkin().getDrawable("panel_button_bg"));

        drawableMap.put(AttachmentPoint.AttachmentType.POSITION, getSkin().getDrawable("icon-target"));
        drawableMap.put(AttachmentPoint.AttachmentType.ROTATION, getSkin().getDrawable("icon-angle"));
        drawableMap.put(AttachmentPoint.AttachmentType.TRANSPARENCY, getSkin().getDrawable("icon-transparency"));
        drawableMap.put(AttachmentPoint.AttachmentType.COLOR, getSkin().getDrawable("icon-color"));

        button = new ImageButton(drawableMap.get(AttachmentPoint.AttachmentType.POSITION));

        addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                currentType = currentType.next();
                setButtonDrawable();
            }
        });

        setButtonDrawable();
    }

    private void setButtonDrawable() {
        button = new ImageButton(drawableMap.get(currentType));
        clearChildren();
        add(button);
    }

    public AttachmentPoint.AttachmentType getAttachmentType() {
        return currentType;
    }

    public void setAttachmentType(AttachmentPoint.AttachmentType attachmentType) {
        currentType = attachmentType;
        setButtonDrawable();
    }
}
