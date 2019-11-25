package com.talosvfx.talos.editor.addons.bvb;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.esotericsoftware.spine.Skin;
import com.esotericsoftware.spine.attachments.*;

public class EmptyAttachmentLoader implements AttachmentLoader {

    public EmptyAttachmentLoader () {

    }

    public RegionAttachment newRegionAttachment (Skin skin, String name, String path) {
        RegionAttachment attachment = new RegionAttachment(name);
        TextureRegion region = new TextureRegion();
        attachment.setRegion(region);
        return attachment;
    }

    public MeshAttachment newMeshAttachment (Skin skin, String name, String path) {
        MeshAttachment attachment = new MeshAttachment(name);
        TextureRegion region = new TextureRegion();
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
