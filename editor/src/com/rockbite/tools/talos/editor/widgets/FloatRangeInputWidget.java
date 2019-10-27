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

package com.rockbite.tools.talos.editor.widgets;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.rockbite.tools.talos.runtime.values.NumericalValue;

public class FloatRangeInputWidget extends Table {

    private FloatInputWidget minInput;
    private FloatInputWidget maxInput;

    private ImageButton equalsButton;
    private ImageButton mirrorButton;

    private Skin skin;

    private ChangeListener listener;

    public FloatRangeInputWidget(String textMin, String textMax, Skin skin) {
        this.skin = skin;

        minInput = new FloatInputWidget(textMin, skin);
        maxInput = new FloatInputWidget(textMax, skin);

        Table midTable = new Table();

        equalsButton = new ImageButton(skin, "chain");
        mirrorButton = new ImageButton(skin, "mirror");

        equalsButton.setChecked(true);

        equalsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if(equalsButton.isChecked()) {
                    mirrorButton.setChecked(false);
                }
                checkStatus();
            }
        });

        mirrorButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if(mirrorButton.isChecked()) {
                    equalsButton.setChecked(false);
                }
                checkStatus();
            }
        });

        minInput.setListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if(mirrorButton.isChecked()) {
                    float angle = 180f - minInput.getValue();
                    if(angle < 0) angle = 360 + angle;
                    maxInput.setValue(angle);
                } else if(equalsButton.isChecked()) {
                    maxInput.setValue(minInput.getValue());
                }

                if(listener != null) {
                    listener.changed(event, FloatRangeInputWidget.this);
                }
            }
        });

        maxInput.setListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if(mirrorButton.isChecked()) {
                    float angle = 180f - maxInput.getValue();
                    if(angle < 0) angle = 360 + angle;
                    minInput.setValue(angle);
                } else if(equalsButton.isChecked()) {
                    minInput.setValue(maxInput.getValue());
                }

                if(listener != null) {
                    listener.changed(event, FloatRangeInputWidget.this);
                }
            }
        });

        midTable.add(mirrorButton).row();
        midTable.add().height(8).row();
        midTable.add(equalsButton);

        add(minInput);
        add(midTable).width(41);
        add(maxInput);

    }

    public void setListener(ChangeListener listener) {
        this.listener = listener;
    }


    private void checkStatus() {
       if(mirrorButton.isChecked()) {
           maxInput.setValue(180f - minInput.getValue());
       } else if(equalsButton.isChecked()) {
           maxInput.setValue(minInput.getValue());
       }
    }

    public Skin getSkin () {
        return skin;
    }

    public void setFlavour(NumericalValue.Flavour flavour) {
        minInput.setFlavour(flavour);
        maxInput.setFlavour(flavour);

        if(flavour == NumericalValue.Flavour.ANGLE) {
            mirrorButton.setVisible(true);
        } else {
            mirrorButton.setVisible(false);
            mirrorButton.setChecked(false);
        }
    }

    public void setText(String min, String max) {
        minInput.setText(min);
        minInput.setText(max);
    }

    public void setValue(float min, float max) {
        minInput.setValue(min);
        maxInput.setValue(max);
    }

    public float getMinValue() {
        return minInput.getValue();
    }

    public float getMaxValue() {
        return maxInput.getValue();
    }

    public ImageButton getEqualsButton() {
        return equalsButton;
    }

    public ImageButton getMirrorButton() {
        return mirrorButton;
    }
}
