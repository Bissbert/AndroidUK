package ch.bissbert.peakseek.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.orm.SugarContext;
import com.threed.jpct.Logger;

import java.sql.SQLException;
import java.util.concurrent.ExecutionException;

import ch.bissbert.peakseek.CameraManager;
import ch.bissbert.peakseek.R;
import ch.bissbert.peakseek.dao.LocationFetcher;
import ch.bissbert.peakseek.graphics.objects.SeekManager;
import ch.bissbert.peakseek.graphics.objects.SphereManager;
import ch.bissbert.peakseek.graphics.rotation.Orientation;

/**
 * Main activity containing the find screen as well as the button to switch to the settings menu
 *
 * @author Bissbert, BeeTheKay
 * @see SettingsActivity
 */
public class MainActivity extends AppCompatActivity implements LifecycleOwner, Orientation.Listener {

    private static boolean run = false;
    private final int REQUEST_CODE_PERMISSIONS = 1001;
    private final String[] permissions = {android.Manifest.permission.CAMERA, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};

    private SphereManager sphereManager;
    private LocationManager locationManager;
    private boolean firstTriggerHappened;
    private SeekManager seekManager;
    private CameraManager cameraManager;

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
                    locationFetcher.execute("").get();
                } catch (ClassNotFoundException | SQLException | InterruptedException | ExecutionException e) {
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
     * <p>
     * sets activity as main activity and loads the screen. Also loads the database({@link #runOnce()}),
     * initiates the SugarContext and loads the seekScreen({@link #loadSeekScreen()})
     *
     * @param savedInstanceState instance of saved state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Logger.log("onCreate");

        super.onCreate(savedInstanceState);

        SugarContext.init(this);
        runOnce();

        setContentView(R.layout.activity_main);

        if (hasNoPermissions()) {
            requestPermission();
        } else {
            afterRights();
        }

        cameraManager = new CameraManager(this, findViewById(R.id.cameraView));
        cameraManager.onCreate();

        if (seekManager == null) loadSeekScreen();

    }

    private boolean hasNoPermissions() {
        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(this,
                    perm) != PackageManager.PERMISSION_GRANTED) {
                return true;
            }
        }
        return false;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_PERMISSIONS);
    }

    /**
     * loads the seek screen onto the GLSurface view from the main screen
     */
    private void loadSeekScreen() {
        GLSurfaceView surfaceView = findViewById(R.id.peakSeekGLView);
        seekManager = new SeekManager(getResources(), surfaceView, this);
        seekManager.loadSeekScreen();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (hasNoPermissions()) {
            return;
        }
        afterRights();
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void afterRights() {
        cameraManager = new CameraManager(this, findViewById(R.id.cameraView));
        cameraManager.onCreate();
        loadByGPS();
    }

    public void loadByGPS() {
        Log.i(getString(R.string.GPS), "loading gps data");

        locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        Log.i(getString(R.string.GPS), "creating the sphere manager");

        if (seekManager == null) loadSeekScreen();

        sphereManager = new SphereManager(seekManager, this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.i(getString(R.string.GPS), "setting GPS listener");
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 3, sphereManager);
        }
    }

    /**
     * pauses the GLView
     */
    @Override
    protected void onPause() {
        super.onPause();
        seekManager.onPause();
    }

    /**
     * resumes the GLView
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (seekManager == null) loadSeekScreen();
        seekManager.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    /**
     * triggered when screen gets touched
     *
     * @param me the {@link MotionEvent} containing the motion on the screen
     * @return a boolean whether succeeded
     */
    public boolean onTouchEvent(MotionEvent me) {
        return super.onTouchEvent(me);
    }

    /**
     * opens the Settings menu using an intent
     *
     * @param view button that triggered the method
     */
    public void openSettingsMenu(View view) {
        Intent setting = new Intent(this, SettingsActivity.class);
        startActivity(setting);
    }

    @Override
    public void onOrientationChanged(float yaw, float pitch) {
        seekManager.onOrientationChanged(yaw, pitch);
    }
}