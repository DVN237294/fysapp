package dk.amavin.projectfysapp.bodymodel;

import android.opengl.GLSurfaceView;
import android.content.Context;
import android.view.MotionEvent;

import java.util.ArrayList;


public class MainGLView extends GLSurfaceView {

    private final MainGLRenderer renderer;
    private final Context context;
    private ArrayList<OnRayCastModelInterceptHandler> listeners = new ArrayList<>();
    private final float TOUCH_SCALE_FACTOR = 180.0f / 320;
    private float previousX;
    private float previousY;
    private static final int MAX_CLICK_DURATION = 200;
    private long startClickTime;

    public MainGLView(Context context){

        super(context);
        this.context = context;
        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2);

        //My renderer only renders one object/mesh (man2.6.obj)
        renderer = new MainGLRenderer(context);

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(renderer);

        // Render the view only when there is a change in the drawing data
        //setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        float x = e.getX();
        float y = e.getY();
        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:
                float dx = x - previousX;
                float dy = y - previousY;

                // reverse direction of rotation above the mid-line
                if (y > getHeight() / 2) {
                    dx = dx * -1 ;
                }

                // reverse direction of rotation to left of the mid-line
                if (x < getWidth() / 2) {
                    dy = dy * -1 ;
                }

                float[] axis = new float[3];
                axis[0] = -dy/dx;
                axis[1] = 1;
                renderer.applyRotation((dx + dy) * TOUCH_SCALE_FACTOR, new float[] { 0, 1, 0});
                requestRender();
                break;

            case MotionEvent.ACTION_DOWN: {
                startClickTime = e.getEventTime();
                break;
            }
            case MotionEvent.ACTION_UP: {
                if(e.getEventTime() - startClickTime < MAX_CLICK_DURATION) {
                    handleClickEvent(e);
                }
            }
        }

        previousX = x;
        previousY = y;
        return true;

    }

    public void addOnRayCastInterceptionListener(OnRayCastModelInterceptHandler listener)
    {
        listeners.add(listener);
    }

    private void notifyListeners(GLMesh object)
    {
        for(OnRayCastModelInterceptHandler listener : listeners)
            listener.handleRayCastModelIntercept(object);
    }

    private void handleClickEvent(MotionEvent e)
    {
        float x = e.getX();
        float y = e.getY();

        GLMesh objectHit = renderer.rayCast(x, y);
        if(objectHit != null)
            notifyListeners(objectHit);
    }
}
