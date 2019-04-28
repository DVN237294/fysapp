package dk.amavin.projectfysapp.bodymodel;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.SystemClock;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import dk.amavin.projectfysapp.R;

public class MainGLRenderer implements GLSurfaceView.Renderer  {


    private final float[] projectionMatrix = new float[16];

    /**
     * The view matrix. This can be thought of as our camera. This matrix transforms world space to eye space;
     * it vertices things relative to our eye.
     */
    private float[] mViewMatrix = new float[16];

    private final float projectionNearClip = 1.0f;
    private final float projectionFarClip = 20.0f;
    private final float cameraFOV = 100;

    // Position the eye in front of the origin.          (X, Y, Z)
    private float[] cameraPosition = new float[] { 0.0f, 0.0f, 10.5f};

    // The xyz point in world-space that the camera is looking at
    private float[] cameraLookAt = new float[] { 0.0f, 0.0f, 0.0f};

    // Set our up vector. This is where our head would be pointing were we holding the camera.
    private float[] cameraUpDirection = new float[] {0.0f, 1.0f, 0.0f};

    private int viewWidth = 0;
    private int viewHeight = 0;

    //Ray casting vars
    private float[] view;
    private float[] h;
    private float[] v;

    private GLMesh mesh;
    private ObjLoader modelLoader;
    private Context context;
    private ArrayList<GLMesh> hitboxes = new ArrayList<>();
    private int textureHandle = 0;


    public MainGLRenderer(Context context)
    {
        modelLoader = new ObjLoader(context);
        this.context = context;
    }

    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config)
    {
        GLShader shader = new GLShader();
        mesh = modelLoader.getMesh("man2.6.obj");
        mesh.setShader(shader);

        String[] hitboxePaths = null;
        try {
            hitboxePaths = context.getAssets().list("hitboxes/");
        }
        catch(IOException ex)
        {
            //this should actually end the activity?
        }
        for(String hitbox : hitboxePaths)
        {
            GLMesh boxMesh = modelLoader.getMesh("hitboxes/" + hitbox);
            boxMesh.setShader(shader);
            boxMesh.setModelMatrix(mesh.getModelMatrix());
            hitboxes.add(boxMesh);
        }

        loadTextureCoords(mesh, shader);

        textureHandle = loadTexture(context, R.drawable.texture);

        // Set the background clear color to gray.
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 0.5f);

        // Set the view matrix. This matrix can be said to represent the camera position.
        // NOTE: In OpenGL 1, a ModelView matrix is used, which is a combination of a model and
        // view matrix. In OpenGL 2, we can keep track of these matrices separately if we choose.
        Matrix.setLookAtM(mViewMatrix, 0, cameraPosition[0], cameraPosition[1],
                cameraPosition[2], cameraLookAt[0], cameraLookAt[1], cameraLookAt[2],
                cameraUpDirection[0], cameraUpDirection[1], cameraUpDirection[2]);


        GLES20.glEnable(GLES20.GL_CULL_FACE);

        GLES20.glFrontFace(GLES20.GL_CCW);

        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        GLES20.glEnable (GLES20.GL_DEPTH_TEST);

        GLES20.glUseProgram(shader.getProgramHandle());


    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        viewHeight = height;
        viewWidth = width;
        GLES20.glViewport(0, 0, width, height);
        GLES20.glEnable(GLES20.GL_CCW);

        final float ratio = (float) width / height;
        Matrix.perspectiveM(projectionMatrix, 0, cameraFOV, ratio, projectionNearClip, projectionFarClip);

        //Ray casting math inspired by:
        //http://schabby.de/picking-opengl-ray-tracing/
        view = vector_normalize(vector_subtract(cameraLookAt, cameraPosition));
        h = vector_normalize(vector_crossP(view, cameraUpDirection));

        float vLength = (float)Math.tan(Math.toRadians(cameraFOV) / 2) * projectionNearClip;
        float hLength = vLength * ratio;

        v = vector_scale(vector_normalize(vector_crossP(h, view)), vLength);
        h = vector_scale(h, hLength);
    }

    public void applyRotation(float angle, float[] axis)
    {
        float[] rotation = new float[16];
        Matrix.setRotateM(rotation, 0, angle , axis[0], axis[1], axis[2]);
        Matrix.multiplyMM(mesh.getModelMatrix(), 0, mesh.getModelMatrix(), 0, rotation, 0);
    }

    long lastTime = SystemClock.uptimeMillis();
    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        long time = SystemClock.uptimeMillis() - lastTime;
        lastTime = SystemClock.uptimeMillis();
        float sec = (float)time/1000;


        applyRotation(sec*36, new float[] {0, 1, 0});
        //for(GLMesh hitb : hitboxes)
        //    hitb.draw(mViewMatrix, projectionMatrix);
        mesh.draw(mViewMatrix, projectionMatrix);
    }

    private void loadTextureCoords(GLMesh mesh, GLShader shader)
    {
        //Load in the texture coordinates
        GLES20.glEnableVertexAttribArray(shader.getmTextureCoordsHandle());
        float[] textureCoords = mesh.getTextureCoordinates();
        ByteBuffer bb = ByteBuffer.allocateDirect(textureCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());

        FloatBuffer textureCoordBuffer = bb.asFloatBuffer();
        textureCoordBuffer.put(textureCoords);
        textureCoordBuffer.position(0);
        GLES20.glVertexAttribPointer(shader.getmTextureCoordsHandle(), 2, GLES20.GL_FLOAT, false, 8, textureCoordBuffer);
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
            // Load the bitmap into the bound texture.
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

            // Recycle the bitmap, since its data has been loaded into OpenGL.
            bitmap.recycle();

            // Set filtering
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR);
            GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
        }

        if (textureHandle[0] == 0)
        {
            throw new RuntimeException("Error loading texture.");
        }

        return textureHandle[0];
    }

    public GLMesh rayCast(float x, float y)
    {
        x -= viewWidth / 2;
        x /= viewWidth / 2;
        y = viewHeight - y;
        y -= viewHeight / 2;
        y /= viewHeight / 2;

        float[] pos = new float[3];
        vector3_add(pos, cameraPosition);
        vector3_add(pos, vector_scale(view, projectionNearClip));
        vector3_add(pos, vector_scale(h, x));
        vector3_add(pos, vector_scale(v, y));
        float[] dir = vector_subtract(pos, cameraPosition);

        for(GLMesh obj : hitboxes)
        for(int i = 0; i < obj.getTriangleCount(); i++)
        {
            float[] vtx0 = new float[] { obj.getModelVertices()[i * 9], obj.getModelVertices()[i * 9 + 1], obj.getModelVertices()[i * 9 + 2], 0};
            float[] vtx1 = new float[] { obj.getModelVertices()[i * 9 + 3], obj.getModelVertices()[i * 9 + 4], obj.getModelVertices()[i * 9 + 5], 0};
            float[] vtx2 = new float[] { obj.getModelVertices()[i * 9 + 6], obj.getModelVertices()[i * 9 + 7], obj.getModelVertices()[i * 9 + 8], 0};

            Matrix.multiplyMV(vtx0, 0, obj.getModelMatrix(), 0, vtx0, 0);
            Matrix.multiplyMV(vtx1, 0, obj.getModelMatrix(), 0, vtx1, 0);
            Matrix.multiplyMV(vtx2, 0, obj.getModelMatrix(), 0, vtx2, 0);

            if(rayIntersectTriangle(pos, dir, new float[] {vtx0[0], vtx0[1], vtx0[2]},
                    new float[] {vtx1[0], vtx1[1], vtx1[2]},
                    new float[] {vtx2[0], vtx2[1], vtx2[2]}))
            {
                //we have a hit! (hurray!)
                return obj;
            }
        }
        return null;
    }

    private boolean rayIntersectTriangle(float[] rPoint, float[] rDirection, float[] vtx0, float[] vtx1, float[] vtx2)
    {
        //Ray intersection math gracefully stolen from:
        //http://www.lighthouse3d.com/tutorials/maths/ray-triangle-intersection/
        float[] e1;
        float[] e2;
        float[] h;
        float[] s;
        float[] q;
        float a, f, u, v;

        e1 = vector_subtract(vtx1, vtx0);
        e2 = vector_subtract(vtx2, vtx0);

        h = vector_crossP(rDirection, e2);
        a = vector_DotP(e1, h);

        if (a > -0.00001 && a < 0.00001)
            return false;

        f = 1/a;
        s = vector_subtract(rPoint, vtx0);

        u = f * vector_DotP(s, h);

        if (u < 0.0 || u > 1.0)
            return false;

        q = vector_crossP(s, e1);
        v = f * vector_DotP(rDirection, q);

        if (v < 0.0 || u + v > 1.0)
            return false;

        return f * vector_DotP(e2, q) > 0.00001f;
    }

    private static float[] vector_add(float[]... a)
    {
        float[] c = new float[a[0].length];
        for(int i = 0; i < a.length; i++)
            for(int k = 0; k < a[i].length; k++)
                c[k] += a[i][k];

        return c;
    }
    private static void vector3_add(float[] a, float[] b)
    {
        a[0] += b[0];
        a[1] += b[1];
        a[2] += b[2];
    }
    private static float[] vector_subtract(float[] a, float[] b)
    {
        float[] c = new float[a.length];
        for(int i = 0; i < c.length; i++)
            c[i] = a[i] - b[i];

        return c;
    }
    private static float[] vector_crossP(float[] a, float[] b)
    {
        float[] c = new float[3];

        c[0] = a[1] * b[2] - a[2] * b[1];
        c[1] = a[2] * b[0] - a[0] * b[2];
        c[2] = a[0] * b[1] - a[1] * b[0];

        return c;
    }
    private static float[] vector_normalize(float[] a)
    {
        double sum = 0;
        for (float v : a) sum += v * v;

        double mag = Math.sqrt(sum);
        float[] c = new float[a.length];
        for(int i = 0; i < c.length; i++)
            c[i] = (float)(a[i] / mag);

        return c;
    }
    private static float[] vector_scale(float[] a, float scale)
    {
        float[] c = new float[a.length];
        for(int i = 0; i < a.length; i++)
            c[i] = a[i] * scale;

        return c;
    }
    private static float vector_DotP(float[] a, float[] b)
    {
        float c = 0;
        for(int i = 0; i < a.length; i++)
            c += a[i] * b[i];

        return c;
    }
}
