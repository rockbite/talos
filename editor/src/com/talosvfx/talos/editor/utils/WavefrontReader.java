package com.talosvfx.talos.editor.utils;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class WavefrontReader {

    public WavefrontReader() {

    }


    public void parseFile(FileHandle fileHandle) {
        String data = fileHandle.readString();

        String[] lines = data.split("\n");

        Array<Vector3> vertices = new Array<>();
        Array<Face> faces = new Array<>();

        for(int i = 0; i < lines.length; i++) {
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

        float[] verts = new float[faces.size];

        int index = 0;
        for(int i = 0; i < faces.size; i++) {
            Face face = faces.get(i);

            for(int j = 0; j < 3; j++) {
                verts[index++] = vertices.get(face.points[j].vIndex).x;
                verts[index++] = vertices.get(face.points[j].vIndex).y;
                verts[index++] = vertices.get(face.points[j].vIndex).z;
            }
        }
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

            vIndex = Integer.parseInt(parts[0]);
            tIndex = Integer.parseInt(parts[1]);
            nIndex = Integer.parseInt(parts[2]);
        }
    }
}
