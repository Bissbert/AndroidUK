package ch.bissbert.peakseek.graphics.objects;


import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.List;

import ch.bissbert.peakseek.SharedPreferenceManager;
import ch.bissbert.peakseek.dao.LocationFetcher;
import ch.bissbert.peakseek.data.Point;

/**
 * Manages the creation and deletion of {@link Sphere}s to the view in {@link SeekManager}
 *
 * @author Bissbert
 */
public class SphereManager implements LocationListener {

    private static boolean isRunning;
    private final SeekManager seekManager;
    private final Context context;
    private final SharedPreferenceManager preferenceManager;

    /**
     * @param seekManager to manage the spheres on
     * @param context     reference to the screen
     */
    public SphereManager(SeekManager seekManager, Context context) {
        this.seekManager = seekManager;
        this.preferenceManager = new SharedPreferenceManager(context);
        this.context = context;
    }

    /**
     * Loads all {@link Sphere}s in the range of the phone's location
     *
     * @param location geolocation of the phone
     */
    @Override
    public void onLocationChanged(@NonNull Location location) {
        if (!isRunning) {
            isRunning = true;
            int radius = preferenceManager.getDistance();
            double[] lv95 = Point.wgs84ToLV95(location.getLongitude(), location.getLatitude(), location.getAltitude());
            List<Point> points = LocationFetcher.getPointsInRadius(location, radius);
            Log.d("loading spheres", points.toString());
            seekManager.clearScreen();
            for (Point point : points) {
                seekManager.addSphere(point.createSphere(lv95[1], lv95[0], context.getResources(), context));
            }
            isRunning = false;
        }
    }
}
