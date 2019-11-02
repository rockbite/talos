package com.rockbite.tools.talos.editor.widgets.propertyWidgets;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.rockbite.tools.talos.TalosMain;
import com.rockbite.tools.talos.editor.addons.bvb.AttachmentPoint;

public class GlobalValueRowWidget extends ListCustomRow {

	private GlobalValueWidget parentWidget;

	AttachmentPoint attachmentPoint;

	Label identifier;
	GlobalValueRowWidgetType type = GlobalValueRowWidgetType.BONE;
	ImageButtonWithBackground typeToggleButton;

	// BONE TYPE WIDGETS
	SelectBox<String> boneNames;
	ImageButtonWithBackground boneTypeChange;

	// STATIC TYPE WIDGETS
	TextField first;
	TextField second;
	TextField third;

	public GlobalValueRowWidget (final GlobalValueWidget parentWidget, AttachmentPoint attachmentPoint) {
		this.parentWidget = parentWidget;

		this.attachmentPoint = attachmentPoint;

		final Skin skin = TalosMain.Instance().getSkin();

		identifier = new Label(String.valueOf(attachmentPoint.getSlotId()), skin);
		identifier.setAlignment(Align.center);
		Label.LabelStyle style = new Label.LabelStyle();
		style.background = skin.getDrawable("panel_button_bg");
		style.fontColor = identifier.getStyle().fontColor;
		style.font = identifier.getStyle().font;
		identifier.setStyle(style);
		identifier.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				super.clicked(event, x, y);
				parentWidget.askForNewPlace();
			}
		});

		ImageButton typeToggleImageButton = new ImageButton(skin.getDrawable("ic-chain"));
		typeToggleImageButton.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				super.clicked(event, x, y);
				type = type == GlobalValueRowWidgetType.BONE ? GlobalValueRowWidgetType.STATIC : GlobalValueRowWidgetType.BONE;
				reconstruct();
			}
		});

		typeToggleButton = new ImageButtonWithBackground(typeToggleImageButton, skin);

		boneNames = new SelectBox<>(skin);
		boneNames.getStyle().backgroundOver = skin.getDrawable("panel_select_over");
		boneNames.getStyle().background = skin.getDrawable("panel_select");

		final ImageButton boneTypeChangeButton = new ImageButton(skin.getDrawable("ic-position"));
		boneTypeChangeButton.getStyle().imageUp = skin.getDrawable("ic-position");
		boneTypeChangeButton.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				super.clicked(event, x, y);
				boneTypeChangeButton.getStyle().imageUp = boneTypeChangeButton.isChecked() ? skin.getDrawable("ic-position") : skin.getDrawable("ic-refresh");
			}
		});


		boneTypeChange = new ImageButtonWithBackground(boneTypeChangeButton, skin);

		first = new TextField("0", skin) {
			@Override
			public float getPrefWidth () {
				return 30f;
			}
		};
		second = new TextField("0", skin) {
			@Override
			public float getPrefWidth () {
				return 30;
			}
		};
		third = new TextField("0", skin) {
			@Override
			public float getPrefWidth () {
				return 30;
			}
		};

		reconstruct();
	}

	public int getIndex () {
		return attachmentPoint.getSlotId();
	}

	public void setSelected (boolean selected) {
	}

	private void reconstruct () {
		clearChildren();
		pad(5);
		defaults().pad(5);
		left();
		switch (type) {
			case BONE:
				add(identifier).width(30f).height(25);
				add(boneNames).growX().height(25);
				add(boneTypeChange).width(30f).height(25);
				add(typeToggleButton).width(30f).height(25);
				break;
			case STATIC:
				add(identifier).width(30f).height(25);
				add(first).growX().height(25);
				add(second).growX().height(25);
				add(third).growX().height(25);
				add(typeToggleButton).width(30).height(25);
				break;
		}
	}

	public enum GlobalValueRowWidgetType {
		BONE, STATIC
	}

	public static class ImageButtonWithBackground extends Table {
		ImageButton imageButton;

		public ImageButtonWithBackground (ImageButton imageButton, Skin skin) {
			setSkin(skin);
			this.imageButton = imageButton;
			add(imageButton).grow();
			setBackground(getSkin().getDrawable("panel_button_bg"));

			addListener(new ClickListener() {
				@Override
				public void enter (InputEvent event, float x, float y, int pointer, Actor fromActor) {
					super.enter(event, x, y, pointer, fromActor);
					setBackground(getSkin().getDrawable("panel_button_bg_over"));
				}

				@Override
				public void exit (InputEvent event, float x, float y, int pointer, Actor toActor) {
					super.exit(event, x, y, pointer, toActor);
					setBackground(getSkin().getDrawable("panel_button_bg"));
				}
			});
		}

		public ImageButton getImageButton () {
			return imageButton;
		}
	}

}
