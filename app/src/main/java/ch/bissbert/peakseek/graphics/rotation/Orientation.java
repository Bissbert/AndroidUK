package ch.bissbert.peakseek.graphics.rotation;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import ch.bissbert.peakseek.R;
import ch.bissbert.peakseek.activities.MainActivity;

/**
 * The class uses the <b>Rotation Vector Sensor</b> to get the orientation of the device
 *
 * @author Bastian
 */
public class Orientation implements SensorEventListener {

    private final SensorManager mSensorManager;
    private final Sensor mRotationSensor;
    private OrientationListener mListener;
    private int mLastAccuracy;
    private float lastYaw = 0;
    private float lastPitch = 0;

    /**
     * @param activity the MainActivity currently running
     */
    public Orientation(MainActivity activity) {
        mSensorManager = (SensorManager) activity.getSystemService(Activity.SENSOR_SERVICE);
        mRotationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        mLastAccuracy = activity.getResources().getInteger(R.integer.ANTI_JITTER);
    }

    /**
     * method to start the listening process of the sensor
     *
     * @param listener to activate when the sensor changes
     */
    public void startListening(OrientationListener listener) {
        if (mListener == listener) return;
        mListener = listener;
        if (mRotationSensor == null) {
            Log.w("Sensor", "Rotation vector sensor not available; will not provide orientation data.");
            return;
        }
        mSensorManager.registerListener(this, mRotationSensor, R.integer.SENSOR_DELAY_MICROS);
    }

    /**
     * method to stop the sensor from checking everything
     */
    public void stopListening() {
        mSensorManager.unregisterListener(this);
        mListener = null;
    }

    /**
     * notifies changes the accuracy for the rotation sensor
     *
     * @param sensor   not used
     * @param accuracy to set the new accuracy
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (mLastAccuracy != accuracy) mLastAccuracy = accuracy;
    }

    /**
     * if the sensor changed, it runs the {@link #updateOrientation(float[])} method
     *
     * @param event of the sensor change
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (mListener == null) return;
        if (mLastAccuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) return;
        if (event.sensor == mRotationSensor) updateOrientation(event.values);
    }

    /**
     * calculates the orientation and calls listener.onOrientationChanged
     *
     * @param rotationVector vector data provided by {@link #onSensorChanged(SensorEvent)}
     */
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

        float antiJitter = (float) 1 / mLastAccuracy;

        if ((pitch - lastPitch < antiJitter && pitch - lastPitch > -antiJitter) && (yaw - lastYaw < antiJitter && yaw - lastYaw > -antiJitter))
            return;

        lastPitch = pitch;
        lastYaw = yaw;

        mListener.onOrientationChanged(yaw, pitch);
    }

    /**
     * Listener interface used to change the orientation
     */
    public interface OrientationListener {
        void onOrientationChanged(float yaw, float pitch);
    }
}
