package ch.bissbert.peakseek.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;

import androidx.annotation.Nullable;

import com.skydoves.colorpickerview.ColorPickerDialog;
import com.skydoves.colorpickerview.listeners.ColorListener;

import ch.bissbert.peakseek.R;
import ch.bissbert.peakseek.SharedPreferenceManager;
import ch.bissbert.peakseek.graphics.objects.SeekManager;

/**
 * Activity controlling changes on the settings page
 *
 * @author Bissbert
 */
public class SettingsActivity extends Activity {

    private View colorView;
    private Switch switchView;
    private EditText distanceEdit;
    private SharedPreferenceManager manager;

    /**
     * executed on creation of the activity.
     * <p>
     * - loads the settings page
     * - loads views from page
     * - creates preference manager
     * - sets distance event
     *
     * @param savedInstanceState saved instance state
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        manager = new SharedPreferenceManager(this);

        colorView = findViewById(R.id.display_color);
        switchView = findViewById(R.id.sendErrorSwitch);
        distanceEdit = findViewById(R.id.distanceTextEdit);


        colorView.setBackgroundColor(getDefaultColor());

        switchView.setChecked(manager.isErrorEnabled());

        distanceEdit.setText(manager.getDistance() + "");
        distanceEdit.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                final String distanceText = distanceEdit.getText().toString();
                if (distanceText.trim().isEmpty()) {
                    v.requestFocus();
                } else {
                    int distanceInMeter = Integer.parseInt(distanceText);
                    manager.setDistance(distanceInMeter);
                }
            }
        });
    }

    /**
     * sets the color in the manager and applies it to the display rectangle
     *
     * @param color color in {@link androidx.annotation.ColorInt} format
     */
    private void setDefaultColor(int color) {
        manager.setColor(color);
        colorView.setBackgroundColor(color);
        SeekManager.reloadColor = true;
    }

    /**
     * fetches the color from the manager
     *
     * @return color from the manager in {@link androidx.annotation.ColorInt} format
     */
    private int getDefaultColor() {
        return manager.getColor();
    }

    /**
     * opens the color picker dialog to choose a new theme color
     *
     * @param view {@link View} that triggered the method
     */
    public void openColorPicker(View view) {
        ColorPickerDialog.Builder builder = new ColorPickerDialog.Builder(this);
        builder.setMessage(R.string.COLOR_PREF_MESSAGE);
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.SAVE_COLOR_POSITIVE, (ColorListener) (color, fromUser) -> setDefaultColor(color))
                .setNegativeButton(getString(R.string.CANCEL),
                        (dialogInterface, i) -> dialogInterface.dismiss());
        builder.attachAlphaSlideBar(false);
        builder.attachBrightnessSlideBar(false);

        builder.create().show();
    }

    /**
     * finishes the current action and returns to the last view(view that called this one)
     *
     * @param view {@link View} that triggered the method
     */
    public void backToMainScreen(View view) {
        this.finish();
    }

    /**
     * sets the error logs flag in the manager
     *
     * @param view {@link View} that triggered the method
     */
    public void errorLogsChange(View view) {
        final boolean isEnabled = switchView.isChecked();
        manager.setErrorEnabled(isEnabled);
    }

    /**
     * opens the about page by opening an {@link Intent} to the {@link AboutActivity}
     *
     * @param view {@link View} that triggered the method
     */
    public void openAboutPage(View view) {
        Intent setting = new Intent(this, AboutActivity.class);
        startActivity(setting);
    }
}
