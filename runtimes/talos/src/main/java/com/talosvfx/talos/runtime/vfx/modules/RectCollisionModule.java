/*******************************************************************************
 * Copyright 2019 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.talosvfx.talos.runtime.vfx.modules;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.runtime.vfx.values.ModuleValue;
import com.talosvfx.talos.runtime.vfx.values.NumericalValue;

public class RectCollisionModule extends AbstractModule {

    public static final int X = 0;
    public static final int Y = 1;
    public static final int WIDTH = 2;
    public static final int HEIGHT = 3;
    public static final int MODULE = 0;

    NumericalValue x;
    NumericalValue y;
    NumericalValue width;
    NumericalValue height;
    ModuleValue<RectCollisionModule> moduleOutput;

    float defaultX, defaultY, defaultWidth, defaultHeight;

    // Collision physics parameters
    float restitution = 1.0f;      // 0 = no bounce, 1 = perfect bounce
    float friction = 0.0f;          // 0 = no friction, 1 = full stop on tangent
    boolean localSpace = false;     // false = world space, true = local particle space
    float lifetimeReduction = 1.0f; // 0 = die on collision, 1 = no reduction, 0.5 = halve lifetime

    @Override
    protected void defineSlots() {
        x = createInputSlot(X);
        y = createInputSlot(Y);
        width = createInputSlot(WIDTH);
        height = createInputSlot(HEIGHT);

        moduleOutput = new ModuleValue<>();
        moduleOutput.setModule(this);
        createOutputSlot(MODULE, moduleOutput);
    }

    @Override
    public void processCustomValues() {
        if (x.isEmpty()) x.set(defaultX);
        if (y.isEmpty()) y.set(defaultY);
        if (width.isEmpty()) width.set(defaultWidth);
        if (height.isEmpty()) height.set(defaultHeight);
    }

    public float getX() {
        fetchInputSlotValue(X);
        return x.isEmpty() ? defaultX : x.getFloat();
    }

    public float getY() {
        fetchInputSlotValue(Y);
        return y.isEmpty() ? defaultY : y.getFloat();
    }

    public float getWidth() {
        fetchInputSlotValue(WIDTH);
        return width.isEmpty() ? defaultWidth : width.getFloat();
    }

    public float getHeight() {
        fetchInputSlotValue(HEIGHT);
        return height.isEmpty() ? defaultHeight : height.getFloat();
    }

    public void setX(float x) {
        defaultX = x;
    }

    public void setY(float y) {
        defaultY = y;
    }

    public void setWidth(float width) {
        defaultWidth = width;
    }

    public void setHeight(float height) {
        defaultHeight = height;
    }

    public float getDefaultX() {
        return defaultX;
    }

    public float getDefaultY() {
        return defaultY;
    }

    public float getDefaultWidth() {
        return defaultWidth;
    }

    public float getDefaultHeight() {
        return defaultHeight;
    }

    public float getRestitution() {
        return restitution;
    }

    public void setRestitution(float restitution) {
        this.restitution = restitution;
    }

    public float getFriction() {
        return friction;
    }

    public void setFriction(float friction) {
        this.friction = friction;
    }

    public boolean isLocalSpace() {
        return localSpace;
    }

    public void setLocalSpace(boolean localSpace) {
        this.localSpace = localSpace;
    }

    public float getLifetimeReduction() {
        return lifetimeReduction;
    }

    public void setLifetimeReduction(float lifetimeReduction) {
        this.lifetimeReduction = lifetimeReduction;
    }

    @Override
    public void write(Json json) {
        super.write(json);
        json.writeValue("x", getDefaultX());
        json.writeValue("y", getDefaultY());
        json.writeValue("width", getDefaultWidth());
        json.writeValue("height", getDefaultHeight());
        json.writeValue("restitution", restitution);
        json.writeValue("friction", friction);
        json.writeValue("localSpace", localSpace);
        json.writeValue("lifetimeReduction", lifetimeReduction);
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        defaultX = jsonData.getFloat("x", 0);
        defaultY = jsonData.getFloat("y", 0);
        defaultWidth = jsonData.getFloat("width", 1);
        defaultHeight = jsonData.getFloat("height", 1);
        restitution = jsonData.getFloat("restitution", 1.0f);
        friction = jsonData.getFloat("friction", 0.0f);
        localSpace = jsonData.getBoolean("localSpace", false);
        lifetimeReduction = jsonData.getFloat("lifetimeReduction", 1.0f);
    }
}
