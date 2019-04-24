package dk.amavin.projectfysapp.bodymodel;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Vector;
import java.util.regex.Pattern;

public final class ObjLoader {
    //ObjLoader code gracefully stolen from Björn Kechel on stackoverflow (and modified by me to support quads):
    //https://stackoverflow.com/questions/41012719/how-to-load-and-display-obj-file-in-android-with-opengl-es-2

    public final int numFaces;
    public final int triangles;
    public final float[] normals;
    public final float[] textureCoordinates;
    public final float[] vertices;

    public ObjLoader(Context context, String file) {

        Vector<Float> vertices = new Vector<>();
        Vector<Float> normals = new Vector<>();
        Vector<Float> textures = new Vector<>();
        ArrayList<String> faces = new ArrayList<>();

        BufferedReader reader = null;
        int quads = 0;
        try {
            InputStreamReader in = new InputStreamReader(context.getAssets().open(file));
            reader = new BufferedReader(in);

            // read file until EOF
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" ");
                switch (parts[0]) {
                    case "v":
                        // vertices
                        vertices.add(Float.valueOf(parts[1]));
                        vertices.add(Float.valueOf(parts[2]));
                        vertices.add(Float.valueOf(parts[3]));
                        break;
                    case "vt":
                        // textures
                        textures.add(Float.valueOf(parts[1]));
                        textures.add(Float.valueOf(parts[2]));
                        break;
                    case "vn":
                        // normals
                        normals.add(Float.valueOf(parts[1]));
                        normals.add(Float.valueOf(parts[2]));
                        normals.add(Float.valueOf(parts[3]));
                        break;
                    case "f":
                        // faces: vertex/texture/normal
                        faces.add(parts[1] + "|" + parts[2] + "|" + parts[3]);
                        if(parts.length > 4)
                        {
                            quads++;
                            faces.set(faces.size()-1, faces.get(faces.size() - 1) + "|" + parts[4]);
                        }
                        break;
                }
            }
        } catch (IOException e) {
            // cannot load or read file
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    //log the exception
                }
            }
        }
        numFaces = faces.size();
        triangles = quads*2 + (numFaces-quads);
        int coordCount = triangles * 9;
        this.normals = new float[coordCount];
        textureCoordinates = new float[(numFaces-quads) * 2 * 3 + quads * 2 * 6];
        this.vertices = new float[coordCount];
        int positionIndex = 0;
        int normalIndex = 0;
        int textureIndex = 0;
        for(String face : faces)
        {
            String[] indexPairs = face.split(Pattern.quote("|"));
            for(int j = 0; j < (indexPairs.length > 3 ? 2 : 1); j++)
            {
                for(int k = 0; k < 3; k++)
                {
                    //Bad naming: Actually not an entire face, just a vertex
                    String[] faceParts = indexPairs[k + (k > 0 ? j : 0)].split("/");
                    int index = 3 * (Short.valueOf(faceParts[0]) - 1);
                    this.vertices[positionIndex++] = vertices.get(index++);
                    this.vertices[positionIndex++] = vertices.get(index++);
                    this.vertices[positionIndex++] = vertices.get(index);

                    index = 2 * (Short.valueOf(faceParts[1]) - 1);
                    textureCoordinates[normalIndex++] = textures.get(index++);
                    // NOTE: Bitmap gets y-inverted
                    textureCoordinates[normalIndex++] = 1 - textures.get(index);

                    if(faceParts.length >= 3) {
                        index = 3 * (Short.valueOf(faceParts[2]) - 1);
                        this.normals[textureIndex++] = normals.get(index++);
                        this.normals[textureIndex++] = normals.get(index++);
                        this.normals[textureIndex++] = normals.get(index);
                    }
                }
            }
        }
    }
}