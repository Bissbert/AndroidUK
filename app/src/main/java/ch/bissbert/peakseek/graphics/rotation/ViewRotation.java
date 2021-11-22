package ch.bissbert.peakseek.graphics.rotation;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.threed.jpct.Camera;
import com.threed.jpct.SimpleVector;

import ch.bissbert.peakseek.MainActivity;

public class ViewRotation {
    private Camera camera;
    private MainActivity activity;

    private Orientation orientation;

    public ViewRotation(MainActivity activity) {
        this.activity = activity;
    }

    public void set(Camera camera, MainActivity activity) {
        this.activity = activity;
        this.camera = camera;
    }

    public void moveCamera(int cameraMoveout, int i) {
        camera.moveCamera(cameraMoveout, i);
    }

    public void lookAt(SimpleVector direction) {
        camera.lookAt(direction);
    }

    public void onOrientationChanged(float yaw, float pitch) {
        if (camera == null) return;
        camera.lookAt(new SimpleVector(-yaw, -pitch, 0));
    }

    public void onResume() {
        if (orientation == null)  orientation = new Orientation(activity);
        orientation.startListening(activity);
    }

    public void onPause() {
        orientation.stopListening();
    }
}
