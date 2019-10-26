package com.rockbite.tools.talos.editor.wrappers;

import com.rockbite.tools.talos.editor.widgets.ui.DragPoint;

public interface IDragPointProvider {

    DragPoint[] fetchDragPoints();

    void dragPointChanged(DragPoint point);
}
