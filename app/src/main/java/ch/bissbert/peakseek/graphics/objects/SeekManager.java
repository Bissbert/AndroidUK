package ch.bissbert.peakseek.graphics.objects;

import android.content.res.Resources;
import android.graphics.PixelFormat;
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
    private RGBColor backgroundColor = new RGBColor(0,0,0,0);

    private float touchTurn = 0;
    private float touchTurnUp = 0;

    private float xpos = -1;
    private float ypos = -1;

    private Sphere sphere = null;
    private int fps = 0;

    private Light sun = null;

    public SeekManager(Resources resources, GLSurfaceView mGLView) {
        this.resources = resources;
        this.mGLView = mGLView;
    }

    /**
     * loads the seek screen onto the GLSurface view from the main screen
     */
    public void loadSeekScreen() {

        //mGLView = new GLSurfaceView(getApplication());

        /*mGLView.setEGLConfigChooser(new GLSurfaceView.EGLConfigChooser() {
            public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
                // Ensure that we get a 16bit framebuffer. Otherwise, we'll fall
                // back to Pixelflinger on some device (read: Samsung I7500)
                int[] attributes = new int[]{EGL10.EGL_DEPTH_SIZE, 16, EGL10.EGL_NONE};
                EGLConfig[] configs = new EGLConfig[1];
                int[] result = new int[1];
                egl.eglChooseConfig(display, attributes, configs, 1, result);
                return configs[0];
            }
        });*/
        mGLView.setZOrderOnTop(true);

        mGLView.setEGLConfigChooser(8,8,8,8,16,0);

        mGLView.getHolder().setFormat(PixelFormat.RGBA_8888);

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
            return true;
        }

        try {
            Thread.sleep(15);
        } catch (Exception e) {
            // No need for this...
        }

        return false;
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

            Camera cam = world.getCamera();
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
                sphere.rotateY(touchTurn);
                touchTurn = 0;
            }

            if (touchTurnUp != 0) {
                sphere.rotateX(touchTurnUp);
                touchTurnUp = 0;
            }

            fb.clear(backgroundColor);
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
