package ch.bissbert.peakseek.graphics.rotation;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.WindowManager;

import ch.bissbert.peakseek.R;

public class Orientation implements SensorEventListener {

    private Listener mListener;
    private int mLastAccuracy;

    private float lastYaw = 0;
    private float lastPitch = 0;

    public interface  Listener {
        void onOrientationChanged(float yaw, float pitch);
    }

    private final SensorManager mSensorManager;
    private final Sensor mRotationSensor;

    public Orientation(Activity activity) {
        mSensorManager = (SensorManager) activity.getSystemService(Activity.SENSOR_SERVICE);

        mRotationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
    }

    public void startListening (Listener  listener) {
        if (mListener == listener) return;
        mListener = listener;
        if (mRotationSensor == null) {
            Log.w("Sensor", "Rotation vector sensor not available; will not provide orientation data.");
            return;
        }
        mSensorManager.registerListener(this, mRotationSensor, R.integer.SENSOR_DELAY_MICROS);
    }

    public void stopListening() {
        mSensorManager.unregisterListener(this);
        mListener = null;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (mLastAccuracy != accuracy) mLastAccuracy = accuracy;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (mListener == null) return;
        if (mLastAccuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) return;
        if (event.sensor == mRotationSensor) updateOrientation(event.values);
    }

    private void updateOrientation(float[] rotationVector) {
        float[] rotationMatrix = new float[9];
        SensorManager.getRotationMatrixFromVector(rotationMatrix, rotationVector);

        final int worldAxisForDeviceAxisX = SensorManager.AXIS_X;
        final int worldAxisForDeviceAxisY = SensorManager.AXIS_Z;

        float[] adjustedRotationMatrix = new float[9];
        SensorManager.remapCoordinateSystem(rotationMatrix, worldAxisForDeviceAxisX, worldAxisForDeviceAxisY, adjustedRotationMatrix);

        float[] orientation = new float[3];
        SensorManager.getOrientation(adjustedRotationMatrix, orientation);

        float yaw = orientation[0] * -57;
        float pitch = orientation[1] * -57;

        lastPitch = pitch;
        lastYaw = yaw;

        mListener.onOrientationChanged(yaw, pitch);
    }
}
