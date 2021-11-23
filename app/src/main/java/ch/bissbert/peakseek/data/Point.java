package ch.bissbert.peakseek.data;


import android.content.Context;
import android.content.res.Resources;
import android.location.Location;

import androidx.core.util.Pair;

import com.orm.SugarRecord;

import ch.bissbert.peakseek.R;
import ch.bissbert.peakseek.graphics.objects.Sphere;

/**
 * Point containing the location
 *
 * @author Bissbert
 */
public class Point extends SugarRecord {
    private String name;
    private double altitude;

    private long east;
    private long north;

    private Double longitude;
    private Double latitude;


    private Language language;
    private PointType type;

    public Point(String name, double altitude, long east, long north, double longitude, double latitude, Language language, PointType type) {
        this(name, altitude, east, north, language, type);
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Point(String name, double altitude, long east, long north, Language language, PointType type) {
        this();
        this.name = name;
        this.altitude = altitude;
        this.east = east;
        this.north = north;
        this.language = language;
        this.type = type;
    }

    public Point() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public Double getLongitude() {
        if (longitude == null) {
            generateWGS84();
        }
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public Double getLatitude() {
        if (latitude == null) {
            generateWGS84();
        }
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public long getEast() {
        return east;
    }

    public void setEast(long east) {
        this.east = east;
    }

    public long getNorth() {
        return north;
    }

    public void setNorth(long north) {
        this.north = north;
    }

    public PointType getType() {
        return type;
    }

    public void setType(PointType type) {
        this.type = type;
    }

    /**
     * calculates the distance between entered location and this point node
     *
     * @param location location to measure distance to
     * @return the distance between the 2 points in meters
     */
    public double getDistanceBetween(Location location) {
        return getDistanceBetween(location.getLatitude(), location.getLongitude());
    }

    /**
     * calculates the distance between given point and this point node
     *
     * @param point point to measure distance to
     * @return the distance between the 2 nodes in meters
     */
    public double getDistanceBetween(Point point) {
        return getDistanceBetween(point.getLatitude(), point.getLongitude());
    }

    /**
     * generates a sphere in relation to the location given as a param
     *
     * @param north
     * @param east
     * @param resources
     * @return
     */
    public Sphere createSphere(double north, double east, Resources resources, Context context) {
        int relationScale = resources.getInteger(R.integer.RENDER_SCALE);
        double x = (this.getNorth() - north), y = (this.getEast() - east);
        Sphere sphere = new Sphere((float) x / relationScale, (float) this.getAltitude() / relationScale, (float) y / relationScale, this, resources, context);
        //sphere.setCollisionMode(Object3D.COLLISION_CHECK_OTHERS);
        //sphere.setCollisionOptimization(Object3D.COLLISION_DETECTION_OPTIMIZED);
        return sphere;
    }

    /**
     * calculates the distance between the current node and the given WGS-84 coordinates
     *
     * @param latitude  latitude of the point to measure the distance to
     * @param longitude longitude of the point to measure the distance to
     * @return the distance between the given coordinates and this point node in meters
     */
    public double getDistanceBetween(Double latitude, Double longitude) {
        int R = 6378137; // Radius of the earth in meter
        double dLat = Math.toRadians(latitude - getLatitude());
        double dLon = Math.toRadians(longitude - getLongitude());
        double a =
                Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                        Math.cos(Math.toRadians(getLatitude())) * Math.cos(Math.toRadians(latitude)) *
                                Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    @Override
    public String toString() {
        return "Point{" +
                "name='" + name + '\'' +
                ", altitude=" + altitude +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", language=" + language +
                ", type=" + type +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Point)) return false;

        Point point = (Point) o;

        if (Double.compare(point.getAltitude(), getAltitude()) != 0) return false;
        if (getLongitude() != null ? !getLongitude().equals(point.getLongitude()) : point.getLongitude() != null)
            return false;
        if (getLatitude() != null ? !getLatitude().equals(point.getLatitude()) : point.getLatitude() != null)
            return false;
        return type != null ? type.equals(point.type) : point.type == null;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(getAltitude());
        result = (int) (temp ^ (temp >>> 32));
        result = 31 * result + (getLongitude() != null ? getLongitude().hashCode() : 0);
        result = 31 * result + (getLatitude() != null ? getLatitude().hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }

    /**
     * generates the WGS84 coordinates from the required LV95 coordinates
     */
    private void generateWGS84() {
        Pair<Double, Double> coords = lv95ToWGS84(this.east, this.north);
        setLongitude(coords.first);
        setLongitude(coords.second);
    }

    /**
     * creates a point using the LV95 system and pregenerates the WGS84 cords
     *
     * @param name       name of the point
     * @param swissEast  east in LV95
     * @param swissNorth north in LV95
     * @param altitude   altitude in meter
     * @param language   language the name is in
     * @param type       type of the point
     * @return a point with pregenerated WGS84
     */
    public static Point generatedFromLV95(String name, long swissEast, long swissNorth, int altitude, Language language, PointType type) {

        Pair<Double, Double> convPair = lv95ToWGS84(swissEast, swissNorth);

        double longitude = convPair.first, latitude = convPair.second;
        return new Point(name, altitude, swissEast, swissNorth, longitude, latitude, language, type);
    }

    /**
     * converts LV95 coordinates to WGS84
     *
     * @param east  east in LV95
     * @param north north in LV95
     * @return a pair containing the longitude in the first and the latitude in the second slot.
     */
    public static Pair<Double, Double> lv95ToWGS84(long east, long north) {
        double y = (double) (east - 2600000) / 1000000;
        double x = (double) (north - 1200000) / 1000000;

        double longitudeM = 2.6779094
                + (4.728982 * y)
                + (0.791484 * y * x)
                + (0.1306 * y * Math.pow(x, 2))
                - (0.0436 * Math.pow(x, 3));

        double latitudeM = 16.9023892
                + (3.238272 * x)
                - (0.270978 * Math.pow(y, 2))
                - (0.002528 * Math.pow(x, 2))
                - (0.0447 * Math.pow(y, 2) * x)
                - (0.0140 * Math.pow(x, 3));

        double longitude = longitudeM * 100 / 36;
        double latitude = latitudeM * 100 / 36;

        return Pair.create(longitude, latitude);
    }

    /**
     * creates the LV95 coordinates from the WGS84 data
     *
     * @param longitude longitude in WGS84 in sexagismalseconds
     * @param latitude  latitude in WGS84 in sexagismalseconds
     * @param height    heigth in WGS84
     * @return an array with east in 0, north in 1 and height in 2
     */
    public static double[] wgs84ToLV95(double longitude, double latitude, double height) {

        double[] data = new double[3];

        double x = (latitude * 3600 - 169028.66) / 10000;
        double y = (longitude * 3600 - 26782.5) / 10000;

        //east
        data[0] = 2600072.37d
                + (211455.93d * y)
                - (10938.51d * y * x)
                - (0.36d * y * Math.pow(x, 2))
                - (44.54d * Math.pow(y, 3));

        //north
        data[1] = 1200147.07d
                + (308807.95d * x)
                + (3745.25d * Math.pow(y, 2))
                + (76.63d * Math.pow(x, 2))
                - (194.56d * Math.pow(y, 2) * x)
                + (119.79d * Math.pow(x, 3));

        data[2] = height - 49.55
                + (2.73 * y)
                + (6.94 * x);

        return data;
    }
}
