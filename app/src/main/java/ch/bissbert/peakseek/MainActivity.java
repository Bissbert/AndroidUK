package ch.bissbert.peakseek;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;

import com.orm.SugarContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import ch.bissbert.peakseek.dao.LocationFetcher;
import ch.bissbert.peakseek.dao.NoTimestampException;
import ch.bissbert.peakseek.data.Point;

public class MainActivity extends AppCompatActivity {

    private static boolean run = false;

    public MainActivity() {
        super();
    }

    public void runOnce(){
        if (!run) {
            Log.i(getString(R.string.LOAD_TAG), "starting app");
            Log.i(getString(R.string.LOAD_TAG), "query if network is available");
            if (isNetworkAvailable()) {
                Log.i(getString(R.string.LOAD_TAG), "fetching data...");
                try {
                    LocationFetcher locationFetcher = new LocationFetcher(this);
                    locationFetcher.execute("");
                } catch (ClassNotFoundException | SQLException e) {
                    e.printStackTrace();
                }
            }
            run = true;
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SugarContext.init(this);
        runOnce();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SugarContext.terminate();
    }
}