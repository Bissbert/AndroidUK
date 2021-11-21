package ch.bissbert.peakseek;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * manager for {@link SharedPreferences} in this application.
 * gives some methods to edit an fetch data needed by the application
 */
public final class SharedPreferenceManager {
    private final Context context;
    private final SharedPreferences prefs;

    /**
     * creates a new Manager from the context given
     *
     * @param context context of activity opening the manager
     */
    public SharedPreferenceManager(Context context) {
        this.context = context;
        prefs = context.getSharedPreferences(context.getString(R.string.APP_PREFS), Context.MODE_PRIVATE);
    }

    /**
     * fetches the color used to draw the spheres on the gui in {@link androidx.annotation.ColorInt} format
     *
     * @return color of spheres in {@link androidx.annotation.ColorInt} format
     */
    public int getColor() {
        return prefs.getInt(context.getString(R.string.COLOR_PREF_NAME), context.getColor(R.color.white));
    }

    /**
     * sets the color in the {@link SharedPreferences} in {@link androidx.annotation.ColorInt} format
     *
     * @param color color in {@link androidx.annotation.ColorInt} format
     */
    public void setColor(int color) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(context.getString(R.string.COLOR_PREF_NAME), color);
        editor.apply();
    }

    /**
     * sets whether the error data collection is allowed
     *
     * @param isEnabled whether the collection of error data is allowed
     */
    public void setErrorEnabled(boolean isEnabled) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(context.getString(R.string.ERROR_LOGS_PREF), isEnabled);
        editor.apply();
    }

    /**
     * fetches whether error data may be sent to the devs
     *
     * @return whether error data may be sent to the devs
     */
    public boolean isErrorEnabled() {
        return prefs.getBoolean(context.getString(R.string.ERROR_LOGS_PREF), false);
    }

    /**
     * sets the {@link SharedPreferences} preference for the render distance of the seeker in meter
     *
     * @param distanceInMeter distance to render in meter
     */
    public void setDistance(int distanceInMeter) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(context.getString(R.string.RENDER_DISTANCE), distanceInMeter);
        editor.apply();
    }

    /**
     * fetches the distance the Points will be rendered to in meter
     *
     * @return render distance in meter
     */
    public int getDistance() {
        return prefs.getInt(context.getString(R.string.RENDER_DISTANCE), context.getResources().getInteger(R.integer.DEFAULT_RENDER_DISTANCE));
    }
}
