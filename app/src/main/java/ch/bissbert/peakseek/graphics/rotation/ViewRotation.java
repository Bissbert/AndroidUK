package ch.bissbert.peakseek.graphics.rotation;

import com.threed.jpct.Camera;
import com.threed.jpct.SimpleVector;

import ch.bissbert.peakseek.activities.MainActivity;

/**
 * This class contains the camera of the 3D view and gets the phone's orientation to change the camera angle
 *
 * @author Bastian
 */
public class ViewRotation implements Orientation.OrientationListener {
    private Camera camera;
    private MainActivity activity;

    private Orientation orientation;

    public ViewRotation(MainActivity activity) {
        this.activity = activity;
    }

    /**
     * Sets the attributes {@link Camera} and {@link MainActivity}
     *
     * @param camera   to use
     * @param activity to use
     */
    public void set(Camera camera, MainActivity activity) {
        this.activity = activity;
        this.camera = camera;
    }

    /**
     * Moves the {@link Camera} in the view to a specific position
     *
     * @param x is to set the x position of the camera
     * @param y is to set the y position of the camera
     * @param z is to set the z position of the camera
     */
    public void moveCamera(float x, float y, float z) {
        camera.setPosition(new SimpleVector(x, y, z));
    }

    /**
     * Changes the {@link Camera} looking angle to the specified position
     *
     * @param x to look at
     * @param y to look at
     * @param z to look at
     */
    public void lookAt(float x, float y, float z) {
        if (camera == null) return;
        camera.lookAt(new SimpleVector(x, y, z));
    }

    /**
     * Receives the phone's orientation data from {@link Orientation} and calls {@link #lookAt(float, float, float)}
     *
     * @param yaw   yaw / x position
     * @param pitch pitch / y position
     */
    @Override
    public void onOrientationChanged(float yaw, float pitch) {
        lookAt(-yaw, -pitch, 0);
    }

    /**
     * Starts the listening process in {@link Orientation}
     */
    public void onResume() {
        if (orientation == null) orientation = new Orientation(activity);
        orientation.startListening(activity);
    }

    /**
     * Stops the listening process in {@link Orientation}
     */
    public void onPause() {
        orientation.stopListening();
    }
}
