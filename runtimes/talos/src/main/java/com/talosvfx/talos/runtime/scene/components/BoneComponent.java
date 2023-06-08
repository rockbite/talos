package com.talosvfx.talos.runtime.scene.components;

import com.esotericsoftware.spine.Bone;

/**
 * Tag component to flag fake game objects generated for bvb.
 */
public class BoneComponent extends AComponent {
    private final Bone bone;

    public BoneComponent(Bone bone) {
        this.bone = bone;
    }

    public Bone getBone() {
        return bone;
    }
}
