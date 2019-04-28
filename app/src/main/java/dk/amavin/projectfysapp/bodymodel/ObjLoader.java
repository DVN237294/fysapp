package dk.amavin.projectfysapp.bodymodel;

import android.content.Context;

import com.google.common.io.Files;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Vector;
import java.util.regex.Pattern;

public final class ObjLoader {
    //ObjLoader code gracefully stolen from Björn Kechel on stackoverflow (and modified quite a bit by me):
    //https://stackoverflow.com/questions/41012719/how-to-load-and-display-obj-file-in-android-with-opengl-es-2

    private Context context;

    public ObjLoader(Context context) {

        this.context = context;
    }

    public GLMesh getMesh(String file)
    {
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
        final int numFaces = faces.size();
        final int triangles = quads*2 + (numFaces-quads);
        final int coordCount = triangles * 9;
        final float[] finalNormals = new float[coordCount];
        final float[] textureCoordinates = new float[(numFaces-quads) * 2 * 3 + quads * 2 * 6];
        final float[] finalVertices = new float[coordCount];
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
                    String[] faceParts = indexPairs[k + (k > 0 ? j : 0)].split("/");
                    int index = 3 * (Short.valueOf(faceParts[0]) - 1);
                    finalVertices[positionIndex++] = vertices.get(index++);
                    finalVertices[positionIndex++] = vertices.get(index++);
                    finalVertices[positionIndex++] = vertices.get(index);

                    if(faceParts.length >= 2) {
                        index = 2 * (Short.valueOf(faceParts[1]) - 1);
                        textureCoordinates[normalIndex++] = textures.get(index++);
                        // NOTE: Bitmap gets y-inverted
                        textureCoordinates[normalIndex++] = 1 - textures.get(index);
                    }

                    if(faceParts.length >= 3) {
                        index = 3 * (Short.valueOf(faceParts[2]) - 1);
                        finalNormals[textureIndex++] = normals.get(index++);
                        finalNormals[textureIndex++] = normals.get(index++);
                        finalNormals[textureIndex++] = normals.get(index);
                    }
                }
            }
        }
        GLMesh m = new GLMesh(finalVertices, textureCoordinates, finalNormals, numFaces, triangles);
        m.setName(Files.getNameWithoutExtension(file));
        return m;
    }
}