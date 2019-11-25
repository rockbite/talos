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

package com.talosvfx.talos.editor.widgets;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ObjectMap;
import com.talosvfx.talos.runtime.values.NumericalValue;

public class FloatInputWidget extends Table {


    private NumericalValue.Flavour flavour;

    private ObjectMap<NumericalValue.Flavour, Table> flavourContainers = new ObjectMap<>();

    private ChangeListener listener;

    private TextField regularCarrier;
    private RotatorWidget angleCarrier;

    private Label regularLabel;

    private float value;

    public FloatInputWidget(String text, Skin skin) {
        setSkin(skin);
        Stack stack = new Stack();

        flavourContainers.put(NumericalValue.Flavour.REGULAR, new Table());
        flavourContainers.put(NumericalValue.Flavour.ANGLE, new Table());
        flavourContainers.put(NumericalValue.Flavour.NORMALIZED, new Table());


        for(Table table: flavourContainers.values()) {
            stack.add(table);
            table.setVisible(false);
        }

        buildRegular(text);
        buildAngle();
        buildNormalized();

        add(stack);

        setFlavour(NumericalValue.Flavour.REGULAR);

        setHeight(68);
    }

    private void buildRegular(String text) {
        Table table = flavourContainers.get(NumericalValue.Flavour.REGULAR);

        regularLabel = new Label(text, getSkin());
        regularCarrier = new TextField("", getSkin());

        table.add(regularLabel).left().row();
        table.add(regularCarrier).padTop(5).width(68);

        regularCarrier.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                getValue();
                listener.changed(event, actor);
            }
        });

        regularCarrier.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);

                if(regularCarrier.getSelection().length() == 0) {
                    regularCarrier.selectAll();
                }
            }
        });
    }

    private void buildAngle() {
        Table table = flavourContainers.get(NumericalValue.Flavour.ANGLE);

        angleCarrier = new RotatorWidget(getSkin());

        angleCarrier.setListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                getValue();
                if(listener != null) {
                    listener.changed(event, actor);
                }
            }
        });

        table.add(angleCarrier).size(68);
    }

    private void buildNormalized() {
        //TODO: normalized case
    }

    public void setFlavour(NumericalValue.Flavour newFlavour) {
        for(NumericalValue.Flavour flavour: flavourContainers.keys()) {
            Table table = flavourContainers.get(flavour);
            if(flavour == newFlavour) {
                table.setVisible(true);
            } else {
                table.setVisible(false);
            }
        }
        this.flavour = newFlavour;

        if(flavour == NumericalValue.Flavour.REGULAR) {
            regularCarrier.setText(value + "");
        }
        if(flavour == NumericalValue.Flavour.ANGLE) {
            angleCarrier.setValue(value);
        }
        if(flavour == NumericalValue.Flavour.NORMALIZED) {
            //TODO: normalized case
        }
    }

    public void setListener(ChangeListener listener) {
        this.listener = listener;
    }

    public float getValue() {
        float val = 0;
        if(flavour == NumericalValue.Flavour.REGULAR) {
            String text = regularCarrier.getText();
            if (text.length() > 0) {
                try {
                    val = Float.parseFloat(text);
                } catch (NumberFormatException e) {
                    val = 0;
                }
            }
        } else if(flavour == NumericalValue.Flavour.ANGLE) {
            val = angleCarrier.getValue();
        } else {
            //TODO: normalized case
        }

        value = val;

        return value;
    }

    public void setValue(float val) {
        value = val;
        if(flavour == NumericalValue.Flavour.REGULAR) {
            regularCarrier.setText(value + "");
        } else if(flavour == NumericalValue.Flavour.ANGLE) {
            angleCarrier.setValue(value);
        } else {
            //TODO: normalized case
        }
    }

    public void setText(String text) {
        regularLabel.setText(text);
    }
}
