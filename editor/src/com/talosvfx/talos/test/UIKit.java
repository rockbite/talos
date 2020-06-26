package com.talosvfx.talos.test;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

public class UIKit extends ApplicationAdapter {

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setWindowedMode(1200, 700);
        config.setTitle("UIKit Testing Suite");
        config.useVsync(false);

        UIKit uiKit = new UIKit();

        new Lwjgl3Application(uiKit, config);
    }

    @Override
    public void create() {
        super.create();
    }


    @Override
    public void render() {
        super.render();
    }


    @Override
    public void dispose() {
        super.dispose();
    }
}
