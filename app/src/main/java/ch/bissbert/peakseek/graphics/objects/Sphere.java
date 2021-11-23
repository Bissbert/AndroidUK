package ch.bissbert.peakseek.graphics.objects;

import android.content.Context;
import android.content.res.Resources;

import com.threed.jpct.CollisionEvent;
import com.threed.jpct.CollisionListener;
import com.threed.jpct.Object3D;
import com.threed.jpct.Primitives;
import com.threed.jpct.RGBColor;
import com.threed.jpct.Texture;
import com.threed.jpct.TextureManager;

import ch.bissbert.peakseek.R;
import ch.bissbert.peakseek.SharedPreferenceManager;
import ch.bissbert.peakseek.data.Point;

public class Sphere extends Object3D implements CollisionListener {

    private static final int TEXTURE_WIDTH = 10;
    private static final int TEXTURE_HEIGHT = 10;
    private static final String TEXTURE_NAME = "sphereTexture";
    private final Point point;
    private final Resources resources;
    private final Context context;


    public Sphere(float x, float y, float z, Point point, Resources resources, Context context) {
        super(Primitives.getSphere(resources.getInteger(R.integer.amount_of_faces), (resources.getInteger(R.integer.size_of_sphere)/resources.getInteger(R.integer.RENDER_SCALE))));
        this.point = point;
        this.resources = resources;
        this.context = context;
        setTexture();

        this.calcTextureWrapSpherical();

        this.strip();
        this.build();

        this.translate(x, y, z);
    }

    public Sphere(Resources resources, Point point, Context context) {
        this(0, 0, 0, point, resources, context);
    }

    private void setTexture() {
        if (!TextureManager.getInstance().containsTexture(TEXTURE_NAME)) {
            Texture sphereTexture = new Texture(TEXTURE_WIDTH, TEXTURE_HEIGHT, getColor());
            TextureManager.getInstance().addTexture(TEXTURE_NAME, sphereTexture);
        }
        this.setTexture(TEXTURE_NAME);
    }

    private RGBColor getColor(){
        String colorStr = Integer.toHexString(new SharedPreferenceManager(context).getColor());
        return new RGBColor(
                Integer.valueOf(colorStr.substring(2, 4), 16),
                Integer.valueOf(colorStr.substring(4, 6), 16),
                Integer.valueOf(colorStr.substring(6, 8), 16));
    }

    private int getFacesAmount() {
        return resources.getInteger(R.integer.amount_of_faces);
    }

    public Point getPoint() {
        return point;
    }

    @Override
    public String toString() {
        return "Sphere{" +
                "x=" + getTranslation().x + ", " +
                "y=" + getTranslation().y + ", " +
                "x=" + getTranslation().z +
                '}';
    }

    @Override
    public void collision(CollisionEvent collisionEvent) {
        System.out.println("Test: "+collisionEvent.toString());
    }

    @Override
    public boolean requiresPolygonIDs() {
        return false;
    }
}
