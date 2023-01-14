package com.talosvfx.talos.runtime.render.p3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

import java.util.Iterator;

public class Simple3DBatch {


    private Mesh mesh;
    private ShaderProgram shader;

    private float[] vertexBuffer;

    private int maxVertsInMesh;
    private int vertsInBuffer;
    private int vertexSize;

    int blendSrc;
    int blendDst;


    public Simple3DBatch (int size, VertexAttributes vertexAttributes) {
        mesh = new Mesh(false, size * 6, size * 6, vertexAttributes);
        maxVertsInMesh = size;

        final Iterator<VertexAttribute> iter = vertexAttributes.iterator();

        vertexSize = 3 + 1 + 2;

        vertexBuffer = new float[maxVertsInMesh * vertexSize];


        int len = size * 6;
        short[] indices = new short[len];
        for (short i = 0; i < len; i++) {
            indices[i] = i;
        }
        mesh.setIndices(indices);
    }

    public ShaderProgram getShader () {
        return shader;
    }

    public Mesh getMesh () {
        return mesh;
    }



    public void begin (Camera camera, ShaderProgram shaderProgram) {
        shader = shaderProgram;

        shader.bind();
        shader.setUniformMatrix("u_projTrans", camera.combined);

    }

    public void flush () {
        if (vertsInBuffer > 0) {
            lastTexture.bind(0);
            mesh.setVertices(vertexBuffer);
            mesh.render(shader, GL20.GL_TRIANGLES, 0, vertsInBuffer);

            vertsInBuffer = 0;

        }
    }

    private Texture lastTexture = null;
    public void render (float[] subMesh, Texture texture) {

        if (lastTexture != null) {
            if (lastTexture != texture) {
                flush();
            }
        }

        lastTexture = texture;

        int incomingVertCount = subMesh.length/vertexSize;

        if (vertsInBuffer + incomingVertCount > maxVertsInMesh) {
            flush();
        }

        System.arraycopy(subMesh, 0, vertexBuffer, vertsInBuffer * vertexSize, subMesh.length);
        vertsInBuffer += incomingVertCount;

    }


    public void end () {
        flush();
        lastTexture = null;
    }

    public void setBlendFunction (int src, int dst) {
        if (blendSrc != src || blendDst != dst) {
            flush();
        }


        Gdx.gl.glBlendFuncSeparate(src, dst, src, dst);
    }

    public void render (float[] verts, int vertCount, short[] tris, int triCount, Texture texture) {
        if (lastTexture != null) {
            if (lastTexture != texture) {
                flush();
            }
        }

        lastTexture = texture;

        int incomingVertCount = verts.length/vertexSize;

        if (vertsInBuffer + incomingVertCount > maxVertsInMesh) {
            flush();
        }

        System.arraycopy(verts, 0, vertexBuffer, vertsInBuffer * vertexSize, verts.length);
        vertsInBuffer += incomingVertCount;


    }
}
