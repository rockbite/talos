package com.talosvfx.talos.editor.dialogs;

import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Pools;
import com.talosvfx.talos.editor.dialogs.preference.tabs.*;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.widgets.ui.common.ColorLibrary;
import lombok.Getter;

public class PreferencesWindow extends Table {
    private ScrollPane scrollPane;
    public PreferencesWindow() {
        setSize(500, 500);
        setPosition(300, 300);

        final Table titleSegment = constructTitleSegment();
        final Table contentSegment = constructContentSegment();

        // assemble
        add(titleSegment).growX();
        row();
        add(contentSegment).grow();
    }

    private Table constructTitleSegment () {
        final Label titleLabel = new Label("Preferences", SharedResources.skin);
        final Table titleSegment = new Table();
        titleSegment.setBackground(ColorLibrary.obtainBackground(ColorLibrary.SHAPE_SQUIRCLE_TOP, ColorLibrary.BackgroundColor.ULTRA_DARK_GRAY));
        titleSegment.add(titleLabel).expandX().height(25);
        return titleSegment;
    }

    private Table constructContentSegment () {
        final Table contentSegment = new Table();
        contentSegment.setBackground(ColorLibrary.obtainBackground(ColorLibrary.SHAPE_SQUIRCLE_BOTTOM, ColorLibrary.BackgroundColor.SUPER_DARK_GRAY));
        contentSegment.defaults().space(10);

        // left part where tabs are displayed
        final Table tabsSegment = constructTabsSegment();
        // right part where info of the tabs are displayed
        scrollPane = new ScrollPane(null);

        contentSegment.add(tabsSegment).growY().width(160);
        contentSegment.add(scrollPane).grow();
        return contentSegment;
    }

    private Table constructTabsSegment () {
        final Table tabsSegment = new Table();
        tabsSegment.setBackground(ColorLibrary.obtainBackground(ColorLibrary.SHAPE_SQUIRCLE_LEFT, ColorLibrary.BackgroundColor.SUPER_DARK_GRAY));

        final VerticalTabGroup tabsContent = new VerticalTabGroup();
        tabsContent.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                scrollPane.setActor(tabsContent.getSelectedTab().getContent());
            }
        });

        final ScrollPane scrollPane = new ScrollPane(tabsContent);

        tabsContent.startGroup();
        tabsContent.addTab("Interface", new InterfaceTabContent());
        tabsContent.addTab("Themes", null);
        tabsContent.addTab("Viewport", null);
        tabsContent.addTab("Lights", null);
        tabsContent.addTab("Editing", null);
        tabsContent.addTab("Animation", null);
        tabsContent.endGroup();

        tabsContent.startGroup();
        tabsContent.addTab("Add-ons", new AddOnsTabContent());
        tabsContent.endGroup();

        tabsContent.startGroup();
        tabsContent.addTab("Input", null);
        tabsContent.addTab("Navigation", null);
        tabsContent.addTab("Keymap", new KeymapTabContent());
        tabsContent.endGroup();

        tabsContent.startGroup();
        tabsContent.addTab("System", new SystemTabContent());
        tabsContent.addTab("Save & Load", new SaveAndLoadTabContent());
        tabsContent.addTab("File Paths", new FilePathsTabContent());
        tabsContent.endGroup();

        tabsSegment.add(scrollPane).pad(9).growX();
        tabsSegment.row();
        tabsSegment.add().expandY();

        return tabsSegment;
    }

    public class VerticalTabGroup extends Table {
        private boolean startGroup;

        private final int breakSpace = 8;
        private final int minSpace = 1;

        @Getter
        private VerticalTab selectedTab;

        public VerticalTabGroup () {
            defaults().height(25).growX();
        }

        public void addTab (String title, PreferenceTabContent preferenceTabContent) {
            final VerticalTab tab = new VerticalTab(title, preferenceTabContent);
            tab.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    super.clicked(event, x, y);
                    if (tab == selectedTab) return;

                    if (selectedTab != null) selectedTab.deselect();
                    tab.select();
                    selectedTab = tab;

                    // fire change event
                    final ChangeListener.ChangeEvent changeEvent = Pools.obtain(ChangeListener.ChangeEvent.class);
                    fire(changeEvent);
                    Pools.free(changeEvent);
                }
            });

            if (startGroup) {
                tab.roundTop();
                startGroup = false;
            }
            add(tab).padBottom(minSpace).row();
        }

        public void startGroup () {
            startGroup = true;
        }

        public void endGroup () {
            // if group is ended change the background of the last tab into squircle bottom
            final VerticalTab lastTab = (VerticalTab) getChildren().get(getChildren().size - 1);
            lastTab.roundBottom();
            getCell(lastTab).padBottom(breakSpace);
        }
    }

    public class VerticalTab extends Table {
        private boolean roundTop;
        private boolean roundBottom;

        private final ColorLibrary.BackgroundColor defaultBackgroundColor = ColorLibrary.BackgroundColor.PANEL_GRAY;
        private ColorLibrary.BackgroundColor currentBackgroundColor = defaultBackgroundColor;
        private ColorLibrary.BackgroundColor overBackgroundColor = ColorLibrary.BackgroundColor.BRIGHT_GRAY;
        private ColorLibrary.BackgroundColor selectedBackgroundColor = ColorLibrary.BackgroundColor.LIGHT_BLUE;

        private boolean selected;
        @Getter
        private final PreferenceTabContent content;

        public VerticalTab (String title, PreferenceTabContent content) {
            this.content = content;

            construct(title);
            addListeners();

            updateBackground();
        }

        private void construct (String title) {
            final Label titleLabel = new Label(title, SharedResources.skin, "small");
            titleLabel.setAlignment(Align.left);
            add(titleLabel).expandX().left().padLeft(8);
        }

        private void addListeners () {
            addListener(new ClickListener() {
                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                    super.enter(event, x, y, pointer, fromActor);
                    if (selected) return;

                    currentBackgroundColor = overBackgroundColor;
                    updateBackground();
                }

                @Override
                public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                    super.exit(event, x, y, pointer, toActor);
                    currentBackgroundColor = selected ? selectedBackgroundColor : defaultBackgroundColor;
                    updateBackground();
                }
            });

            setTouchable(Touchable.enabled);
        }

        public void select () {
            selected = true;
            currentBackgroundColor = selectedBackgroundColor;
            updateBackground();
        }

        public void deselect () {
            selected = false;
            currentBackgroundColor = defaultBackgroundColor;
            updateBackground();
        }

        public void updateBackground () {
            if (roundBottom && roundTop) {
                setBackground(ColorLibrary.obtainBackground(ColorLibrary.SHAPE_SQUIRCLE_2, currentBackgroundColor));
            } else if (roundTop) {
                setBackground(ColorLibrary.obtainBackground(ColorLibrary.SHAPE_SQUIRCLE_TOP_2, currentBackgroundColor));
            } else if (roundBottom) {
                setBackground(ColorLibrary.obtainBackground(ColorLibrary.SHAPE_SQUIRCLE_BOTTOM_2, currentBackgroundColor));
            } else {
                setBackground(ColorLibrary.obtainBackground(ColorLibrary.SHAPE_SQUARE, currentBackgroundColor));
            }
        }

        public void roundTop () {
            this.roundTop = true;
            updateBackground();
        }

        public void roundBottom () {
            this.roundBottom = true;
            updateBackground();
        }
    }
 }
