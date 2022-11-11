package com.talosvfx.talos.runtime.render.p3d;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.utils.Pool;

public class Sprite3D extends Renderable implements Pool.Poolable {

    private Sprite sprite;
    private short[] indices;
    private float[] vertices;

    public Sprite3D() {
        sprite = new Sprite();

        sprite.setPosition(-sprite.getWidth() * 0.5f, -sprite.getHeight() * 0.5f);

        indices = new short[] {0, 1, 2, 2, 3, 0, 4, 5, 6, 6, 7, 4 };
        vertices = new float[8 * 4 * 2];
        System.arraycopy(vertices, 0, convert(sprite, 1), 0, 8 * 4);
        System.arraycopy(vertices, 8 * 4, convert(sprite, -1), 0, 8 * 4);

        meshPart.mesh = new Mesh(true, 8, 12, VertexAttribute.Position(), VertexAttribute.Normal(), VertexAttribute.TexCoords(0));
        meshPart.offset = 0;
        meshPart.primitiveType = GL20.GL_TRIANGLES;
        worldTransform.setTranslation(0, 0, 0);
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public void setSize(float width, float height) {
        sprite.setSize(width, height);
        sprite.setPosition(-sprite.getWidth() * 0.5f, -sprite.getHeight() * 0.5f);
        update();
    }

    public void setPosition(float x, float y) {
        worldTransform.setTranslation(x, y, 0);
    }

    private void update() {
        setVertices(sprite);
        meshPart.mesh.setVertices(vertices);
        meshPart.mesh.setIndices(indices);
        meshPart.size = meshPart.mesh.getNumIndices();
        meshPart.update();
    }

    private float[] convert(Sprite sprite, float normal) {
        float[] spriteVertices = sprite.getVertices();
        float[] vertices = {
                spriteVertices[Batch.X2], spriteVertices[Batch.Y2], 0, 0, 0, normal, spriteVertices[Batch.U2], spriteVertices[Batch.V2],
                spriteVertices[Batch.X1], spriteVertices[Batch.Y1], 0, 0, 0, normal, spriteVertices[Batch.U1], spriteVertices[Batch.V1],
                spriteVertices[Batch.X4], spriteVertices[Batch.Y4], 0, 0, 0, normal, spriteVertices[Batch.U4], spriteVertices[Batch.V4],
                spriteVertices[Batch.X3], spriteVertices[Batch.Y3], 0, 0, 0, normal, spriteVertices[Batch.U3], spriteVertices[Batch.V3]};

        return  vertices;
    }

    private void setVertices(Sprite sprite) {
        float[] spriteVertices = sprite.getVertices();
        vertices[0 + 0] = spriteVertices[Batch.X2];
        vertices[1 + 0] = spriteVertices[Batch.Y2];
        vertices[6 + 0] = spriteVertices[Batch.U2];
        vertices[7 + 0] = spriteVertices[Batch.V2];
        vertices[0 + 8] = spriteVertices[Batch.X1];
        vertices[1 + 8] = spriteVertices[Batch.Y1];
        vertices[6 + 8] = spriteVertices[Batch.U1];
        vertices[7 + 8] = spriteVertices[Batch.V1];
        vertices[0 + 16] = spriteVertices[Batch.X4];
        vertices[1 + 16] = spriteVertices[Batch.Y4];
        vertices[6 + 16] = spriteVertices[Batch.U4];
        vertices[7 + 16] = spriteVertices[Batch.V4];
        vertices[0 + 24] = spriteVertices[Batch.X3];
        vertices[1 + 24] = spriteVertices[Batch.Y3];
        vertices[6 + 24] = spriteVertices[Batch.U3];
        vertices[7 + 24] = spriteVertices[Batch.V3];

        vertices[0 + 32] = spriteVertices[Batch.X2];
        vertices[1 + 32] = spriteVertices[Batch.Y2];
        vertices[6 + 32] = spriteVertices[Batch.U2];
        vertices[7 + 32] = spriteVertices[Batch.V2];
        vertices[0 + 40] = spriteVertices[Batch.X1];
        vertices[1 + 40] = spriteVertices[Batch.Y1];
        vertices[6 + 40] = spriteVertices[Batch.U1];
        vertices[7 + 40] = spriteVertices[Batch.V1];
        vertices[0 + 48] = spriteVertices[Batch.X4];
        vertices[1 + 48] = spriteVertices[Batch.Y4];
        vertices[6 + 48] = spriteVertices[Batch.U4];
        vertices[7 + 48] = spriteVertices[Batch.V4];
        vertices[0 + 56] = spriteVertices[Batch.X3];
        vertices[1 + 56] = spriteVertices[Batch.Y3];
        vertices[6 + 56] = spriteVertices[Batch.U3];
        vertices[7 + 56] = spriteVertices[Batch.V3];
    }

    @Override
    public void reset() {

    }

    public Sprite getSprite () {
        return sprite;
    }
}
