package ch.bissbert.peakseek.graphics.objects;

import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;

import com.threed.jpct.Camera;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.Light;
import com.threed.jpct.RGBColor;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.World;
import com.threed.jpct.util.MemoryHelper;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import ch.bissbert.peakseek.activities.MainActivity;
import ch.bissbert.peakseek.graphics.rotation.ViewRotation;

/**
 * Manages the {@link GLSurfaceView} to display in the {@link MainActivity}
 *
 * @author Bastian
 */
public class SeekManager {

    public static boolean reloadColor;
    private final GLSurfaceView mGLView;
    private final MainActivity activity;
    private final RGBColor BACKGROUND_COLOR = new RGBColor(0, 0, 0, 0);
    private FrameBuffer frameBuffer = null;
    private World world = null;

    private ViewRotation viewRotation;

    /**
     * @param mGLView  to manage
     * @param activity to give to {@link ViewRotation}
     */
    public SeekManager(GLSurfaceView mGLView, MainActivity activity) {
        this.mGLView = mGLView;
        this.activity = activity;
    }

    /**
     * Loads the seek screen onto the {@link GLSurfaceView}
     */
    public void loadSeekScreen() {
        mGLView.setZOrderOnTop(true);

        mGLView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);

        mGLView.getHolder().setFormat(PixelFormat.RGBA_8888);

        world = new World();
        world.setAmbientLight(20, 20, 20);

        Light sun = new Light(world);
        sun.setIntensity(250, 250, 250);

        Camera cam = world.getCamera();
        cam.moveCamera(Camera.CAMERA_MOVEOUT, 50);

        SimpleVector sv = new SimpleVector(0, 0, 0);
        sun.setPosition(sv);

        MyRenderer renderer = new MyRenderer();
        mGLView.setRenderer(renderer);

        onResume();
    }

    /**
     * Pauses the {@link GLSurfaceView}
     */
    public void onPause() {
        mGLView.onPause();
        viewRotation.onPause();
    }

    /**
     * Resumes the {@link GLSurfaceView}
     */
    public void onResume() {
        mGLView.onResume();
        if (viewRotation == null) viewRotation = new ViewRotation(activity);
        viewRotation.onResume();
    }

    /**
     * Removes all objects from the {@link World}
     */
    public void clearScreen() {
        if (world == null) return;
        world.removeAllObjects();
    }

    /**
     * Adds a {@link Sphere} to be displayed
     *
     * @param sphere to add to the {@link World}
     */
    public void addSphere(Sphere sphere) {
        if (world == null) return;
        world.addObject(sphere);
    }

    /**
     * Renders the 3D space
     */
    class MyRenderer implements GLSurfaceView.Renderer {

        public MyRenderer() { }

        /**
         * Gets called after the Surface is created or the size was changed
         *
         * @param gl     interface
         * @param width  of the surface
         * @param height of the surface
         */
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            if (frameBuffer != null) frameBuffer.dispose();

            frameBuffer = new FrameBuffer(gl, width, height);

            viewRotation.set(world.getCamera(), activity);


            MemoryHelper.compact();
        }

        /**
         * From {@link GLSurfaceView.Renderer}, not used here
         */
        public void onSurfaceCreated(GL10 gl, EGLConfig config) { }

        /**
         * Gets called repeatedly to draw the frame
         *
         * @param gl interface
         */
        public void onDrawFrame(GL10 gl) {
            frameBuffer.clear(BACKGROUND_COLOR);
            if (frameBuffer != null) {
                world.renderScene(frameBuffer);
                world.draw(frameBuffer);
                frameBuffer.display();
            }
        }
    }
}
