package com.talosvfx.talos.runtime.render.p3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.PerspectiveCamera;
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
        mesh = new Mesh(false, size * 4, size * 6, vertexAttributes);
        maxVertsInMesh = size;

        final Iterator<VertexAttribute> iter = vertexAttributes.iterator();

        vertexSize = 3 + 1 + 2;

        vertexBuffer = new float[maxVertsInMesh * vertexSize];


        int len = size * 6;
        short[] indices = new short[len];
        short j = 0;
        for (int i = 0; i < len; i += 6, j += 4) {
            indices[i] = j;
            indices[i + 1] = (short)(j + 1);
            indices[i + 2] = (short)(j + 2);
            indices[i + 3] = (short)(j + 2);
            indices[i + 4] = (short)(j + 3);
            indices[i + 5] = j;
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

        shader.begin();
        shader.setUniformMatrix("u_projTrans", camera.combined);

    }

    public void flush () {
        if (vertsInBuffer > 0) {
            lastTexture.bind(0);
            mesh.setVertices(vertexBuffer);
            mesh.render(shader, GL20.GL_TRIANGLES, 0, vertsInBuffer/4 * 6);

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
        shader.end();//deprrecated
        lastTexture = null;
    }

    public void setBlendFunction (int src, int dst) {
        if (blendSrc != src || blendDst != dst) {
            flush();
        }


        Gdx.gl.glBlendFuncSeparate(src, dst, src, dst);
    }
}
