package ch.bissbert.peakseek;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;

import com.skydoves.colorpickerview.ColorEnvelope;
import com.skydoves.colorpickerview.ColorPickerDialog;
import com.skydoves.colorpickerview.ColorPickerView;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;
import com.skydoves.colorpickerview.listeners.ColorListener;

public class SettingsActivity extends Activity {

    private View colorView;
    private Switch switchView;
    private EditText distanceEdit;
    private SharedPreferenceManager manager;

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

        distanceEdit.setText(manager.getDistance()+"");
        distanceEdit.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus){
                final String distanceText = distanceEdit.getText().toString();
                if (distanceText.trim().isEmpty()){
                    v.requestFocus();
                }else {
                    int distanceInMeter = Integer.parseInt(distanceText);
                    manager.setDistance(distanceInMeter);
                }
            }
        });
    }

    private void setDefaultColor(int color) {
        manager.setColor(color);
        colorView.setBackgroundColor(color);
    }

    private int getDefaultColor() {
        return manager.getColor();
    }


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

    public void backToMainScreen(View view) {
        this.finish();
    }

    public void errorLogsChange(View view) {
        final boolean isEnabled = switchView.isChecked();
        manager.setErrorEnabled(isEnabled);
    }

    public void openAboutPage(View view) {
        Intent setting = new Intent(this, AboutActivity.class);
        startActivity(setting);
    }
}
