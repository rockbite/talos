package com.talosvfx.talos.editor.addons.scene.logic.components;

public interface RendererComponent extends IComponent {

    public String getSortingLayer();

    void setSortingLayer (String name);
}
