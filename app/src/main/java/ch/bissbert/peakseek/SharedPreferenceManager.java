package ch.bissbert.peakseek;

import android.content.Context;
import android.content.SharedPreferences;

public final class SharedPreferenceManager {
    private final Context context;
    private final SharedPreferences prefs;

    public SharedPreferenceManager(Context context) {
        this.context = context;
        prefs = context.getSharedPreferences(context.getString(R.string.APP_PREFS), Context.MODE_PRIVATE);
    }

    public int getColor(){
        return prefs.getInt(context.getString(R.string.COLOR_PREF_NAME), context.getColor(R.color.white));
    }

    public void setColor(int color){
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(context.getString(R.string.COLOR_PREF_NAME), color);
        editor.apply();
    }

    public void setErrorEnabled(boolean isEnabled) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(context.getString(R.string.ERROR_LOGS_PREF), isEnabled);
        editor.apply();
    }

    public boolean isErrorEnabled(){
        return prefs.getBoolean(context.getString(R.string.ERROR_LOGS_PREF), false);
    }

    public void setDistance(int distanceInMeter) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(context.getString(R.string.RENDER_DISTANCE), distanceInMeter);
        editor.apply();
    }

    public int getDistance(){
        return prefs.getInt(context.getString(R.string.RENDER_DISTANCE), context.getResources().getInteger(R.integer.DEFAULT_RENDER_DISTANCE));
    }
}
