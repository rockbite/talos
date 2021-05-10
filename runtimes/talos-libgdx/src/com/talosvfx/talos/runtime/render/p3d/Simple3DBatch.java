package com.talosvfx.talos.runtime.render.p3d;

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


    public Simple3DBatch (int size, VertexAttributes vertexAttributes) {
        mesh = new Mesh(false, size, size * 3, vertexAttributes);
        maxVertsInMesh = size;

        final Iterator<VertexAttribute> iter = vertexAttributes.iterator();

        vertexSize = 3 + 1 + 2;

        vertexBuffer = new float[maxVertsInMesh * vertexSize];


        short[] indices = new short[size];
        for (short i = 0; i < size; i++) {
            indices[i] = i;
        }

        mesh.setIndices(indices);
    }



    public void begin (PerspectiveCamera camera, ShaderProgram shaderProgram) {
        shader = shaderProgram;

        shader.begin();
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
        shader.end();//deprrecated
        lastTexture = null;
    }

}
