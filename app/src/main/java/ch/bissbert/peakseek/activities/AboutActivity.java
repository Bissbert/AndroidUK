package ch.bissbert.peakseek.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import ch.bissbert.peakseek.R;

/**
 * Just the activity for the about page
 *
 * Therefore only contains the back function
 *
 * @author Bissbert
 */
public class AboutActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
    }

    public void back(View view) {
        this.finish();
    }
}
