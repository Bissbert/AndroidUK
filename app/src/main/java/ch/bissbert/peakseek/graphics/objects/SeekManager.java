package ch.bissbert.peakseek.graphics.objects;

import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;

import com.threed.jpct.Camera;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.Light;
import com.threed.jpct.RGBColor;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.World;
import com.threed.jpct.util.MemoryHelper;

import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import ch.bissbert.peakseek.activities.MainActivity;
import ch.bissbert.peakseek.graphics.rotation.ViewRotation;

public class SeekManager {

    public static boolean reloadColor;
    private final Resources resources;
    private final GLSurfaceView mGLView;
    private final MainActivity activity;
    private final RGBColor BACKGROUND_COLOR = new RGBColor(0,0,0,0);
    private MyRenderer renderer;
    private FrameBuffer fb = null;
    private World world = null;
    private int fps = 0;

    private ViewRotation viewRotation;
    private Light sun;
    private Camera cam;

    public SeekManager(Resources resources, GLSurfaceView mGLView, MainActivity activity) {
        this.resources = resources;
        this.mGLView = mGLView;
        this.activity = activity;
    }

    /**
     * loads the seek screen onto the GLSurface view from the main screen
     */
    public void loadSeekScreen() {
        mGLView.setZOrderOnTop(true);

        mGLView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);

        mGLView.getHolder().setFormat(PixelFormat.RGBA_8888);

        world = new World();
        world.setAmbientLight(20, 20, 20);

        sun = new Light(world);
        sun.setIntensity(250, 250, 250);

        cam = world.getCamera();
        cam.moveCamera(Camera.CAMERA_MOVEOUT, 50);
        //cam.lookAt(sphere.getTransformedCenter());

        SimpleVector sv = new SimpleVector(0,-20,0);
        sun.setPosition(sv);

        renderer = new MyRenderer();
        mGLView.setRenderer(renderer);

        onResume();
    }

    /**
     * pauses the GLView
     */
    public void onPause() {
        mGLView.onPause();
        viewRotation.onPause();
    }

    /**
     * resumes the GLView
     */
    public void onResume() {
        mGLView.onResume();
        if (viewRotation == null) viewRotation = new ViewRotation(activity);
        viewRotation.onResume();
    }

    public void onOrientationChanged(float yaw, float pitch) {
        viewRotation.onOrientationChanged(yaw, pitch);
    }

    public void setSpheres(List<Sphere> spheres) {
        if (world == null) return;
        Sphere[] sphereArray = new Sphere[spheres.size()];
        spheres.toArray(sphereArray);
        world.removeAllObjects();
        System.out.println("Spheres: " + spheres.toString());
        world.addObjects(sphereArray);
    }

    public void clearScreen() {
        if (world == null) return;
        world.removeAllObjects();
    }

    public void addSphere(Sphere sphere) {
        if (world == null) return;
        world.addObject(sphere);
    }

    class MyRenderer implements GLSurfaceView.Renderer {

        private long time = System.currentTimeMillis();

        public MyRenderer() { }

        public void onSurfaceChanged(GL10 gl, int w, int h) {
            if (fb != null) fb.dispose();

            fb = new FrameBuffer(gl, w, h);

            viewRotation.set(world.getCamera(), activity);
            //viewRotation.moveCamera(Camera.CAMERA_MOVEOUT, 50);

            MemoryHelper.compact();
        }

        public void onSurfaceCreated(GL10 gl, EGLConfig config) { }

        public void onDrawFrame(GL10 gl) {
            fb.clear(BACKGROUND_COLOR);
            if (fb != null) {
                world.renderScene(fb);
                world.draw(fb);
                fb.display();
            }
/*
            if (System.currentTimeMillis() - time >= 1000) {
                Log.i("fps", fps + "");
                fps = 0;
                time = System.currentTimeMillis();
            }
            fps++;*/
        }
    }
}