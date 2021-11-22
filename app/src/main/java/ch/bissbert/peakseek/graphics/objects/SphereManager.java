package ch.bissbert.peakseek.graphics.objects;


import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import ch.bissbert.peakseek.SharedPreferenceManager;
import ch.bissbert.peakseek.dao.LocationFetcher;
import ch.bissbert.peakseek.data.Point;

public class SphereManager implements LocationListener {

    private final SeekManager seekManager;
    private final Context context;
    private final SharedPreferenceManager preferenceManager;

    public SphereManager(SeekManager seekManager, Context context) {
        this.seekManager = seekManager;
        this.preferenceManager = new SharedPreferenceManager(context);
        this.context = context;
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        int radius = preferenceManager.getDistance();
        double[] lv95 = Point.wgs84ToLV95(location.getLongitude(), location.getLatitude(), location.getAltitude());
        List<Point> points = LocationFetcher.getPointsInRadius(location, radius);
        List<Sphere> spheres = new ArrayList<>();
        Log.d("loading spheres", points.toString());
        for (Point point : points){
            spheres.add(point.createSphere(lv95[1], lv95[0], context.getResources()));
        }
        seekManager.setSpheres(spheres);
    }
}
