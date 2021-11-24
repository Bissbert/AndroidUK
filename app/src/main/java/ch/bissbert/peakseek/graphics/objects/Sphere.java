package ch.bissbert.peakseek.graphics.objects;

import android.content.Context;
import android.content.res.Resources;

import com.threed.jpct.Object3D;
import com.threed.jpct.Primitives;
import com.threed.jpct.RGBColor;
import com.threed.jpct.Texture;
import com.threed.jpct.TextureManager;

import ch.bissbert.peakseek.R;
import ch.bissbert.peakseek.SharedPreferenceManager;
import ch.bissbert.peakseek.data.Point;

/**
 * Sphere object to display {@link Point}
 *
 * @author Bastian
 */
public class Sphere extends Object3D {

    private static final int TEXTURE_WIDTH = 10;
    private static final int TEXTURE_HEIGHT = 10;
    private static final String TEXTURE_NAME = "sphereTexture";
    private final Point point;

    private final Context context;


    /**
     * @param x         to be placed at
     * @param y         to be placed at
     * @param z         to be placed at
     * @param point     object to be linked to the sphere
     * @param resources to get data
     * @param context   reference to the screen
     */
    public Sphere(float x, float y, float z, Point point, Resources resources, Context context) {
        super(Primitives.getSphere(resources.getInteger(R.integer.amount_of_faces), resources.getInteger(R.integer.size_of_sphere) / resources.getInteger(R.integer.RENDER_SCALE)));
        this.point = point;
        this.context = context;
        setTexture();

        this.calcTextureWrapSpherical();
        this.translate(x, y, z);

        this.strip();
        this.build();
    }

    /**
     * Creates a Sphere with the default position [0, 0, 0]
     *
     * @param point     object to be linked to the sphere
     * @param resources to get data
     * @param context   reference to the screen
     */
    public Sphere(Point point, Resources resources, Context context) {
        this(0, 0, 0, point, resources, context);
    }

    /**
     * Changes the Sphere color to the color from {@link #getColor()}
     */
    private void setTexture() {
        if (!TextureManager.getInstance().containsTexture(TEXTURE_NAME)) {
            Texture sphereTexture = new Texture(TEXTURE_WIDTH, TEXTURE_HEIGHT, getColor());
            TextureManager.getInstance().addTexture(TEXTURE_NAME, sphereTexture);
        }

        this.setTexture(TEXTURE_NAME);
    }

    /**
     * Gets the color from the context as a Hex String and converts it to {@link RGBColor}
     *
     * @return the color as {@link RGBColor}
     */
    private RGBColor getColor() {
        String colorStr = Integer.toHexString(new SharedPreferenceManager(context).getColor());
        return new RGBColor(
                Integer.valueOf(colorStr.substring(2, 4), 16),
                Integer.valueOf(colorStr.substring(4, 6), 16),
                Integer.valueOf(colorStr.substring(6, 8), 16));

    }

    /**
     * @return the {@link Point} linked to this sphere
     */
    public Point getPoint() {
        return point;
    }

    /**
     * @return the x, y, z coordinates of the sphere
     */
    @Override
    public String toString() {
        return "Sphere{" +
                "x=" + getTranslation().x + ", " +
                "y=" + getTranslation().y + ", " +
                "x=" + getTranslation().z +
                '}';
    }
}
