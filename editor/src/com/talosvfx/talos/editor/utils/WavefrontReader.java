package com.talosvfx.talos.editor.utils;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;


public class WavefrontReader {

    private Mesh mesh;

    public WavefrontReader() {

    }

    public Mesh getMesh() {
        return mesh;
    }


    public void parseFile(FileHandle fileHandle) {
        String data = fileHandle.readString();

        String[] lines = data.split("\n");

        Array<Vector3> vertices = new Array<>();
        Array<Face> faces = new Array<>();

        for(int i = 0; i < lines.length; i++) {
            lines[i] = lines[i].replace("\r", "");

            String[] words = lines[i].split(" ");

            if (words.length > 0) {
                if (words[0].equals("v")) {
                    Vector3 vec = new Vector3();
                    vec.x = Float.parseFloat(words[1]);
                    vec.y = Float.parseFloat(words[2]);
                    vec.z = Float.parseFloat(words[3]);
                    vertices.add(vec);
                } else if (words[0].equals("f")) {
                    Face face = new Face(words[1], words[2], words[3]);
                    faces.add(face);
                }
            }
        }

        VertexAttributes vertexAttributes = new VertexAttributes(VertexAttribute.Position(), VertexAttribute.ColorPacked(), VertexAttribute.TexCoords(0));
        int attribCount = 6;

        float[] verts = new float[faces.size * 3 * attribCount];

        int index = 0;
        for(int i = 0; i < faces.size; i++) {
            Face face = faces.get(i);

            for(int j = 0; j < 3; j++) {
                verts[index++] = vertices.get(face.points[j].vIndex - 1).x;
                verts[index++] = vertices.get(face.points[j].vIndex - 1).y;
                verts[index++] = vertices.get(face.points[j].vIndex - 1).z;

                verts[index++] = Color.WHITE_FLOAT_BITS;
                verts[index++] = 0;
                verts[index++] = 0;
            }
        }


        mesh = new Mesh(false, verts.length, faces.size * 3, vertexAttributes);

        /*
        short[] indices = new short[faces.size * 3];
        for (short i = 0; i < faces.size * 3; i++) {
            indices[i] = i;
        }
        mesh.setIndices(indices);*/
        mesh.setVertices(verts);
    }

    private class Face {
        public PointData[] points = new PointData[3];

        public Face(String point1, String point2, String point3) {
            points[0] = parsePoint(point1);
            points[1] = parsePoint(point2);
            points[2] = parsePoint(point3);
        }

        private PointData parsePoint (String point) {
            PointData pointData = new PointData(point);
            return pointData;
        }
    }

    private class PointData {
        int vIndex;
        int tIndex;
        int nIndex;

        public PointData(String data) {
            String parts[] = data.split("/");

            if(parts[1].equals("")) parts[1] = "0";

            vIndex = Integer.parseInt(parts[0]);
            tIndex = Integer.parseInt(parts[1]);
            nIndex = Integer.parseInt(parts[2]);
        }
    }
}
