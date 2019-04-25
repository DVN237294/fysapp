package dk.amavin.projectfysapp.bodymodel;

import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class GLMesh {

    private FloatBuffer vertexBuffer;
    private float[] modelMatrix;

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex
    private final int vertexCount;
    private GLShader shader;
    public GLMesh(float[] modelVertices, GLShader shader, float[] modelMatrix)
    {
        this.shader = shader;
        this.modelMatrix = modelMatrix;

        vertexCount = modelVertices.length / COORDS_PER_VERTEX;
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(modelVertices.length * 4); //4 bytes per float
        bb.order(ByteOrder.nativeOrder());

        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(modelVertices);
        vertexBuffer.position(0);
    }

    public GLMesh(float[] modelVertices, GLShader shader) {
        this(modelVertices, shader, new float[16]);
        Matrix.setIdentityM(modelMatrix, 0);
    }

    public void draw(float[] mViewMatrix, float[] mProjectionMatrix) {
        // get handle to vertex shader's a_Position member
        int positionHandle = shader.getmPositionHandle();

        // Enable a handle to the vertices
        GLES20.glEnableVertexAttribArray(positionHandle);

        // Prepare the coordinate data
        GLES20.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        float[] mMVPMatrix = new float[16];
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, modelMatrix, 0);

        // This multiplies the modelview matrix by the projection matrix, and stores the result in the MVP matrix
        // (which now contains model * view * projection).
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);

        GLES20.glUniformMatrix4fv(shader.getmMVPMatrixHandle(), 1, false, mMVPMatrix, 0);

        // Draw the mesh
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(positionHandle);

    }

    public float[] getModelMatrix() {
        return modelMatrix;
    }

}
