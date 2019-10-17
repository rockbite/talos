package com.rockbite.tools.talos.editor.widgets.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.VisTable;

public class DynamicTable extends VisTable {

    Array<Actor> rows = new Array<>();
    Array<Boolean> align = new Array<>();

    public Cell addRow(Actor actor, boolean left) {
        Cell cell;
        if(left) {
            cell = add(actor).left().expandX();
        } else {
            cell = add(actor).right().expandX();
        }
        row();

        if(!rows.contains(actor, true)) {
            rows.add(actor);
            align.add(left);
        }

        return cell;
    }

    public void removeRow(int index) {
        clearChildren();

        rows.removeIndex(index);
        align.removeIndex(index);

        for(int i = 0; i < rows.size; i++) {
            addRow(rows.get(i), align.get(i));
        }
    }

}
