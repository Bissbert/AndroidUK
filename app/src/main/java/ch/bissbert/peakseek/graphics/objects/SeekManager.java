package ch.bissbert.peakseek.graphics.objects;

import android.content.res.Resources;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;

import com.threed.jpct.Camera;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.Light;
import com.threed.jpct.RGBColor;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.World;
import com.threed.jpct.util.MemoryHelper;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class SeekManager {

    private final Resources resources;
    private final GLSurfaceView mGLView;
    private MyRenderer renderer = null;
    private FrameBuffer fb = null;
    private World world = null;
    private RGBColor back = new RGBColor(50, 50, 100);

    private float touchTurn = 0;
    private float touchTurnUp = 0;

    private float xpos = -1;
    private float ypos = -1;

    private Sphere sphere = null;
    private int fps = 0;

    private Camera cam;

    private Light sun = null;

    public SeekManager(Resources resources, GLSurfaceView mGLView) {
        this.resources = resources;
        this.mGLView = mGLView;
    }

    /**
     * loads the seek screen onto the GLSurface view from the main screen
     */
    public void loadSeekScreen() {


        mGLView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);

        renderer = new MyRenderer();
        mGLView.setRenderer(renderer);
    }

    /**
     * pauses the GLView
     */
    public void onPause() {
        mGLView.onPause();
    }

    /**
     * resumes the GLView
     */
    public void onResume() {
        mGLView.onResume();
    }

    /**
     * triggered when screen gets touched
     * @param me the {@link MotionEvent} containing the motion on the screen
     * @return a boolean whether succeeded
     */
    public boolean onTouchEvent(MotionEvent me) {

        if (me.getAction() == MotionEvent.ACTION_DOWN) {
            xpos = me.getX();
            ypos = me.getY();
            return true;
        }

        if (me.getAction() == MotionEvent.ACTION_UP) {
            xpos = -1;
            ypos = -1;
            touchTurn = 0;
            touchTurnUp = 0;
            return true;
        }

        if (me.getAction() == MotionEvent.ACTION_MOVE) {
            float xd = me.getX() - xpos;
            float yd = me.getY() - ypos;

            xpos = me.getX();
            ypos = me.getY();

            touchTurn = xd / -100f;
            touchTurnUp = yd / -100f;

            camMovementSpeed();
            return true;
        }

        try {
            Thread.sleep(15);
        } catch (Exception e) {
            // No need for this...
        }

        return false;
    }

    private void camMovementSpeed() {
        touchTurnUp /= 10;
        touchTurn /= 10;
    }

    class MyRenderer implements GLSurfaceView.Renderer {

        private long time = System.currentTimeMillis();

        public MyRenderer() {
        }

        public void onSurfaceChanged(GL10 gl, int w, int h) {
            if (fb != null) {
                fb.dispose();
            }

            fb = new FrameBuffer(gl, w, h);

            world = new World();
            world.setAmbientLight(20, 20, 20);

            sun = new Light(world);
            sun.setIntensity(250, 250, 250);

            sphere = new Sphere(resources);

            world.addObject(sphere);

            cam = world.getCamera();
            cam.moveCamera(Camera.CAMERA_MOVEOUT, 50);
            cam.lookAt(sphere.getTransformedCenter());

            SimpleVector sv = new SimpleVector();
            sv.set(sphere.getTransformedCenter());
            sv.y -= 100;
            sv.z -= 100;
            sun.setPosition(sv);
            MemoryHelper.compact();
        }

        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        }

        public void onDrawFrame(GL10 gl) {
            if (touchTurn != 0) {
                cam.rotateY(touchTurn);
                touchTurn = 0;
            }

            if (touchTurnUp != 0) {
                cam.rotateX(touchTurnUp);
                touchTurnUp = 0;
            }

            fb.clear(back);
            world.renderScene(fb);
            world.draw(fb);
            fb.display();

            if (System.currentTimeMillis() - time >= 1000) {
                Log.i("fps", fps + "");
                fps = 0;
                time = System.currentTimeMillis();
            }
            fps++;
        }
    }
}
