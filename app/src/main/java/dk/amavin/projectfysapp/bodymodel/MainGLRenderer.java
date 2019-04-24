package dk.amavin.projectfysapp.bodymodel;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.SystemClock;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import dk.amavin.projectfysapp.R;

public class MainGLRenderer implements GLSurfaceView.Renderer  {


    private final float[] projectionMatrix = new float[16];
    private float[] mModelMatrix = new float[16];

    /**
     * The view matrix. This can be thought of as our camera. This matrix transforms world space to eye space;
     * it vertices things relative to our eye.
     */
    private float[] mViewMatrix = new float[16];

    private GLMesh mesh;
    private ObjLoader man;
    private GLShader shader;
    private BMPTextureLoader textureLoader;
    Context context;

    public MainGLRenderer(Context context, ObjLoader obj, BMPTextureLoader texture)
    {
        man = obj;
        textureLoader = texture;
        this.context = context;
    }


    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config)
    {
        shader = new GLShader();

        //Load in the texture coordinates
        GLES20.glEnableVertexAttribArray(shader.getmTextureCoordsHandle());
        float[] textureCoords = man.textureCoordinates;
        ByteBuffer bb = ByteBuffer.allocateDirect(textureCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());

        FloatBuffer textureCoordBuffer = bb.asFloatBuffer();
        textureCoordBuffer.put(textureCoords);
        textureCoordBuffer.position(0);
        GLES20.glVertexAttribPointer(shader.getmTextureCoordsHandle(), 2, GLES20.GL_FLOAT, false, 8, textureCoordBuffer);

        loadTexture(context, R.drawable.texture);

        // Set the background clear color to gray.
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 0.5f);

        // Position the eye behind the origin.
        final float eyeX = 0.0f;
        final float eyeY = 0.0f;
        final float eyeZ = 10.5f;

        // We are looking toward the distance
        final float lookX = 0.0f;
        final float lookY = 0.0f;
        final float lookZ = -5.0f;

        // Set our up vector. This is where our head would be pointing were we holding the camera.
        final float upX = 0.0f;
        final float upY = 1.0f;
        final float upZ = 0.0f;

        // Set the view matrix. This matrix can be said to represent the camera position.
        // NOTE: In OpenGL 1, a ModelView matrix is used, which is a combination of a model and
        // view matrix. In OpenGL 2, we can keep track of these matrices separately if we choose.
        Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);

        mesh = new GLMesh(man.vertices, shader);

        GLES20.glEnable(GLES20.GL_CULL_FACE);

        GLES20.glFrontFace(GLES20.GL_CCW);

        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        GLES20.glEnable (GLES20.GL_DEPTH_TEST);

        GLES20.glUseProgram(shader.getProgramHandle());
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        GLES20.glEnable(GLES20.GL_CCW);

        final float ratio = (float) width / height;
        final float left = -ratio;
        final float right = ratio;
        final float bottom = -1.0f;
        final float top = 1.0f;
        final float near = 1.0f;
        final float far = 20.0f;

        Matrix.frustumM(projectionMatrix, 0, left, right, bottom, top, near, far);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        // Do a complete rotation every 10 seconds.
        long time = SystemClock.uptimeMillis() % 10000L;
        float angleInDegrees = (360.0f / 10000.0f) * ((int) time);

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0.0f, 1.0f, 0.0f);
        mesh.draw(mViewMatrix, mModelMatrix, projectionMatrix);
    }

    public static int loadTexture(final Context context, final int resourceId)
    {
        //Gracefully stolen from:
        //https://www.learnopengles.com/android-lesson-four-introducing-basic-texturing/

        final int[] textureHandle = new int[1];

        GLES20.glGenTextures(1, textureHandle, 0);

        if (textureHandle[0] != 0)
        {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;   // No pre-scaling

            // Read in the resource
            final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);

            // Bind to the texture in OpenGL
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);

            // Set filtering
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

            // Load the bitmap into the bound texture.
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

            // Recycle the bitmap, since its data has been loaded into OpenGL.
            bitmap.recycle();
        }

        if (textureHandle[0] == 0)
        {
            throw new RuntimeException("Error loading texture.");
        }

        return textureHandle[0];
    }
}
