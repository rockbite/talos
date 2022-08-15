package com.talosvfx.talos.editor.project;

import com.badlogic.gdx.utils.Array;

public class SnapshotTracker {

    private int cursor = 0;

    Array<String> snapshots = new Array<>();

    public SnapshotTracker() {

    }

    public void addSnapshot(String data) {
        if(cursor + 1 <= snapshots.size - 1) {
            snapshots.removeRange(cursor + 1, snapshots.size - 1);
        }

        snapshots.add(data);
        cursor = snapshots.size - 1;

        System.out.println("Snapshot was added");
    }

    public boolean moveBack() {
        if(cursor > 0) {
            cursor--;
            System.out.println("Snapshot cursor moved back");
            return true;
        } else {
            return false;
        }
    }

    public boolean moveForward() {
        if(cursor < snapshots.size - 1) {
            cursor++;
            System.out.println("Snapshot cursor moved forward");

            return true;
        } else {
            return false;
        }
    }

    public String getCurrentSnapshot() {
        return snapshots.get(cursor);
    }

    public String getSnapshot(int index) {
        return snapshots.get(index);
    }

    public void reset(String data) {
        snapshots.clear();
        cursor = 0;
        addSnapshot(data);
    }
}
