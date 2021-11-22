package ch.bissbert.peakseek;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import ch.bissbert.peakseek.objects.Sphere;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.orm.SugarContext;
import com.threed.jpct.Camera;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.Light;
import com.threed.jpct.Logger;
import com.threed.jpct.Object3D;
import com.threed.jpct.Primitives;
import com.threed.jpct.RGBColor;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.Texture;
import com.threed.jpct.TextureManager;
import com.threed.jpct.World;
import com.threed.jpct.util.MemoryHelper;

import java.lang.reflect.Field;
import java.sql.SQLException;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.opengles.GL10;

import ch.bissbert.peakseek.dao.LocationFetcher;

/**
 * Main activity containing the find screen as well as the button to switch to the settings menu
 *
 * @author Bissbert, BeeTheKay
 * @see SettingsActivity
 */
public class MainActivity extends AppCompatActivity {

    private static boolean run = false;
    // Used to handle pause and resume...
    private static MainActivity master = null;

    private GLSurfaceView mGLView;
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

    private Light sun = null;

    public MainActivity() {
        super();
    }

    /**
     * method that only runs once per app start.
     * <p>
     * Loads the data from the database if first time open or else asks if to load when new data is available
     */
    public void runOnce() {
        if (!run) {
            Log.i(getString(R.string.LOAD_TAG), "starting app");
            Log.i(getString(R.string.LOAD_TAG), "query if network is available");
            if (isNetworkAvailable()) {
                Log.i(getString(R.string.LOAD_TAG), "fetching data...");
                try {
                    LocationFetcher locationFetcher = new LocationFetcher(this);
                    locationFetcher.execute("");
                } catch (ClassNotFoundException | SQLException e) {
                    e.printStackTrace();
                }
            }
            run = true;
        }
    }

    /**
     * checks whether an internet connection is available.
     * <p>
     * needed to check if connections to database can be made
     *
     * @return whether device is connected to internet
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * on closing of app/screen closes the current SugarContext
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        SugarContext.terminate();
    }

    /**
     * run on creation of app
     *
     * sets activity as main activity and loads the screen. Also loads the database({@link #runOnce()}),
     * initiates the SugarContext and loads the seekScreen({@link #loadSeekScreen()})
     * @param savedInstanceState instance of saved state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Logger.log("onCreate");

        if (master != null) {
            copy(master);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SugarContext.init(this);
        runOnce();

        loadSeekScreen();

    }

    /**
     * loads the seek screen onto the GLSurface view from the main screen
     */
    private void loadSeekScreen() {
        mGLView = findViewById(R.id.peakSeekGLView);

        //mGLView = new GLSurfaceView(getApplication());

        mGLView.setEGLConfigChooser(new GLSurfaceView.EGLConfigChooser() {
            public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
                // Ensure that we get a 16bit framebuffer. Otherwise, we'll fall
                // back to Pixelflinger on some device (read: Samsung I7500)
                int[] attributes = new int[]{EGL10.EGL_DEPTH_SIZE, 16, EGL10.EGL_NONE};
                EGLConfig[] configs = new EGLConfig[1];
                int[] result = new int[1];
                egl.eglChooseConfig(display, attributes, configs, 1, result);
                return configs[0];
            }
        });

        renderer = new MyRenderer();
        mGLView.setRenderer(renderer);
    }

    /**
     * pauses the GLView
     */
    @Override
    protected void onPause() {
        super.onPause();
        mGLView.onPause();
    }

    /**
     * resumes the GLView
     */
    @Override
    protected void onResume() {
        super.onResume();
        mGLView.onResume();
    }

    /**
     * copys fields from object given //TODO @BeeTheKey please write an explanation to this method
     * @param src object of which the fields should be copied
     */
    private void copy(Object src) {
        try {
            Logger.log("Copying data from master Activity!");
            Field[] fs = src.getClass().getDeclaredFields();
            for (Field f : fs) {
                f.setAccessible(true);
                f.set(this, f.get(src));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

        return super.onTouchEvent(me);
    }

    //TODO @BeeTheKay write a description
    protected boolean isFullscreenOpaque() {
        return true;
    }

    /**
     * opens the Settings menu using an intent
     * @param view button that triggered the method
     */
    public void openSettingsMenu(View view) {
        Intent setting = new Intent(this, SettingsActivity.class);
        startActivity(setting);
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

            if (master == null) {

                world = new World();
                world.setAmbientLight(20, 20, 20);

                sun = new Light(world);
                sun.setIntensity(250, 250, 250);

                sphere = new Sphere(getResources());

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

                if (master == null) {
                    Logger.log("Saving master Activity!");
                    master = MainActivity.this;
                }
            }
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

            fb.clear(back);
            world.renderScene(fb);
            world.draw(fb);
            fb.display();

            if (System.currentTimeMillis() - time >= 1000) {
                Logger.log(fps + "fps");
                fps = 0;
                time = System.currentTimeMillis();
            }
            fps++;
        }
    }
}