package ch.bissbert.peakseek.graphics.objects;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.location.LocationManager;
import android.opengl.GLSurfaceView;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;

import androidx.core.app.ActivityCompat;

import com.threed.jpct.Camera;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.Light;
import com.threed.jpct.Object3D;
import com.threed.jpct.RGBColor;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.World;
import com.threed.jpct.util.MemoryHelper;

import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class SeekManager {

    private final Resources resources;
    private final GLSurfaceView mGLView;
    private MyRenderer renderer = null;
    private FrameBuffer fb = null;
    private World world = null;
    private RGBColor backgroundColor = new RGBColor(0, 0, 0, 0);

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
     *
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

        /*try {
            SystemClock.sleep(15);
        } catch (Exception e) {
            // No need for this...
        }*/

        return false;
    }

    private void camMovementSpeed() {
        touchTurnUp /= 10;
        touchTurn /= 10;
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
        private boolean firstTriggerHappened;

        public MyRenderer() {
        }

        public void onSurfaceChanged(GL10 gl, int w, int h) {
            if (fb != null) {
                fb.dispose();
            }

            fb = new FrameBuffer(gl, w, h);
            MemoryHelper.compact();
        }

        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            /*if (SeekManager.this.sphereManager != null && SeekManager.this.locationManager != null && !firstTriggerHappened) {
                firstTriggerHappened = true;
                if (ActivityCompat.checkSelfPermission(SeekManager.this.mGLView.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(SeekManager.this.mGLView.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    sphereManager.onLocationChanged(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
                }
            }*/
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
