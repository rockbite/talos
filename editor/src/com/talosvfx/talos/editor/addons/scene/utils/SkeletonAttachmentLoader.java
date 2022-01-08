package com.talosvfx.talos.editor.addons.scene.utils;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.esotericsoftware.spine.Skin;
import com.esotericsoftware.spine.attachments.*;

public class SkeletonAttachmentLoader implements AttachmentLoader {
    public TextureAtlas atlas;

    public SkeletonAttachmentLoader () {
    }

    public void setAtlas(TextureAtlas atlas) {
        this.atlas = atlas;
    }

    public RegionAttachment newRegionAttachment (Skin skin, String name, String path) {
        if(atlas == null) return null;
        TextureAtlas.AtlasRegion region = atlas.findRegion(path);
        if (region == null) throw new RuntimeException("Region not found in atlas: " + path + " (region attachment: " + name + ")");
        RegionAttachment attachment = new RegionAttachment(name);
        attachment.setRegion(region);
        return attachment;
    }

    public MeshAttachment newMeshAttachment (Skin skin, String name, String path) {
        if(atlas == null) return null;
        TextureAtlas.AtlasRegion region = atlas.findRegion(path);
        if (region == null) throw new RuntimeException("Region not found in atlas: " + path + " (mesh attachment: " + name + ")");
        MeshAttachment attachment = new MeshAttachment(name);
        attachment.setRegion(region);
        return attachment;
    }

    public BoundingBoxAttachment newBoundingBoxAttachment (Skin skin, String name) {
        return new BoundingBoxAttachment(name);
    }

    public ClippingAttachment newClippingAttachment (Skin skin, String name) {
        return new ClippingAttachment(name);
    }

    public PathAttachment newPathAttachment (Skin skin, String name) {
        return new PathAttachment(name);
    }

    public PointAttachment newPointAttachment (Skin skin, String name) {
        return new PointAttachment(name);
    }
}
