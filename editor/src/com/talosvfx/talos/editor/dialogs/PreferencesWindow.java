package com.talosvfx.talos.editor.dialogs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.XmlReader;
import com.talosvfx.talos.editor.dialogs.preference.tabs.*;
import com.talosvfx.talos.editor.dialogs.preference.widgets.APrefWidget;
import com.talosvfx.talos.editor.notifications.EventHandler;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.Observer;
import com.talosvfx.talos.editor.notifications.events.FinishInitializingEvent;
import com.talosvfx.talos.editor.notifications.events.ProjectLoadedEvent;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.widgets.ui.common.ColorLibrary;
import lombok.Getter;

public class PreferencesWindow extends AWindowDialog implements Observer {
    private ScrollPane scrollPane;
    private XmlReader.Element xmlRoot;

    private Array<APrefWidget> widgetArray;
    private VerticalTabGroup tabsContent;

    @Override
    public Table build() {
        widgetArray = new Array<>();
        Notifications.registerObserver(this);

        XmlReader xmlReader = new XmlReader();
        xmlRoot = xmlReader.parse(Gdx.files.internal("preferencesLayout.xml"));

        Table table = constructContentSegment();
        table.pack();
        table.setSize(660, 540);

        return table;
    }

    @Override
    public String getTitle() {
        return "Talos Preferences";
    }

    private Table constructContentSegment () {
        final Table contentSegment = new Table();
        contentSegment.setBackground(ColorLibrary.obtainBackground(ColorLibrary.SHAPE_SQUIRCLE_BOTTOM, ColorLibrary.BackgroundColor.SUPER_DARK_GRAY));
        contentSegment.defaults().space(5);

        scrollPane = new ScrollPane(null);

        // left part where tabs are displayed
        final Table tabsSegment = constructTabsSegment();
        // right part where info of the tabs are displayed

        contentSegment.add(tabsSegment).growY().width(160);
        contentSegment.add(scrollPane).grow().pad(5);
        return contentSegment;
    }

    private Table constructTabsSegment () {
        final Table tabsSegment = new Table();
        tabsSegment.setBackground(ColorLibrary.obtainBackground(ColorLibrary.SHAPE_SQUIRCLE_LEFT, ColorLibrary.BackgroundColor.SUPER_DARK_GRAY));

        tabsContent = new VerticalTabGroup();
        tabsContent.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                scrollPane.setActor(tabsContent.getSelectedTab().getContent());
                tabsContent.getSelectedTab().getContent().expandFirstBlock();
            }
        });

        final ScrollPane scrollPane = new ScrollPane(tabsContent);

        VerticalTab firstTab = null;
        int iterator = 0;

        Array<XmlReader.Element> groups = xmlRoot.getChildrenByName("group");
        for(XmlReader.Element group : groups) {
            tabsContent.startGroup();
            Array<XmlReader.Element> tabs = group.getChildrenByName("tab");
            for(XmlReader.Element tab : tabs) {
                String title = tab.getAttribute("title");
                PreferencesTabContent preferencesTabContent = new PreferencesTabContent(tab);
                widgetArray.addAll(preferencesTabContent.getWidgetArray());
                VerticalTab verticalTab = tabsContent.addTab(title, preferencesTabContent);

                if(iterator == 0) {
                    firstTab = verticalTab;
                }
                iterator++;
            }

            tabsContent.endGroup();
        }

        tabsSegment.add(scrollPane).pad(9).growX();
        tabsSegment.row();
        tabsSegment.add().expandY();

        openTab(firstTab);

        return tabsSegment;
    }

    private void openTab(VerticalTab tab) {
        tabsContent.selectedTab = tab;
        tab.select();
        scrollPane.setActor(tabsContent.getSelectedTab().getContent());
        PreferencesTabContent content = tabsContent.getSelectedTab().getContent();
        content.expandFirstBlock();
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

        public VerticalTab addTab (String title, PreferencesTabContent preferenceTabContent) {
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

            return tab;
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
        private final PreferencesTabContent content;

        public VerticalTab (String title, PreferencesTabContent content) {
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
                setBackground(ColorLibrary.obtainBackground(ColorLibrary.SHAPE_SQUIRCLE, currentBackgroundColor));
            } else if (roundTop) {
                setBackground(ColorLibrary.obtainBackground(ColorLibrary.SHAPE_SQUIRCLE_TOP, currentBackgroundColor));
            } else if (roundBottom) {
                setBackground(ColorLibrary.obtainBackground(ColorLibrary.SHAPE_SQUIRCLE_BOTTOM, currentBackgroundColor));
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

    @EventHandler
    public void onFinishInitializingEvent(FinishInitializingEvent event) {
        for(APrefWidget widget: widgetArray) {
            widget.read();
        }
    }

    @EventHandler
    public void onProjectLoadedEvent(ProjectLoadedEvent event) {
        for(APrefWidget widget: widgetArray) {
            if(widget.isProject()) {
                widget.readLocal();
            }
            if (widget.isGlobalProject()) {
                widget.readGlobalProject();
            }
        }
    }
 }
