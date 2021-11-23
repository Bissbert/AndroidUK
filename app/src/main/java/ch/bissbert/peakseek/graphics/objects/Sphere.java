package ch.bissbert.peakseek.graphics.objects;

import android.content.res.Resources;

import com.threed.jpct.Object3D;
import com.threed.jpct.Primitives;
import com.threed.jpct.RGBColor;
import com.threed.jpct.Texture;
import com.threed.jpct.TextureManager;

import ch.bissbert.peakseek.R;

public class Sphere extends Object3D {

    private static final int TEXTURE_WIDTH = 10;
    private static final int TEXTURE_HEIGHT = 10;
    private static final String TEXTURE_NAME = "sphereTexture";

    private final Resources resources;

    public Sphere(float x, float y, float z, Resources resources) {
        super(Primitives.getSphere(resources.getInteger(R.integer.amount_of_faces), resources.getInteger(R.integer.size_of_sphere)));
        this.resources = resources;

        setTexture();

        this.calcTextureWrapSpherical();
        this.strip();
        this.build();

        this.translate(x, y, z);
    }

    public Sphere(Resources resources) {
        this(0, 0, 0, resources);
    }

    private void setTexture() {
        if (!TextureManager.getInstance().containsTexture(TEXTURE_NAME)) {
            Texture sphereTexture = new Texture(TEXTURE_WIDTH, TEXTURE_HEIGHT, getColor());
            TextureManager.getInstance().addTexture(TEXTURE_NAME, sphereTexture);
        }

        this.setTexture(TEXTURE_NAME);
    }

    private RGBColor getColor() {
        String colorStr = Integer.toHexString(resources.getColor(R.color.default_sphere_color, null));
        return new RGBColor(Integer.valueOf(colorStr.substring(2, 4), 16),
                            Integer.valueOf(colorStr.substring(4, 6), 16),
                            Integer.valueOf(colorStr.substring(6, 8), 16));
    }

    private int getFacesAmount() {
        return resources.getInteger(R.integer.amount_of_faces);
    }
}