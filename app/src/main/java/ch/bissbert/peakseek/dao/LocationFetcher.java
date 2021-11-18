package ch.bissbert.peakseek.dao;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Looper;
import android.util.Log;
import android.widget.ProgressBar;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import ch.bissbert.peakseek.R;
import ch.bissbert.peakseek.data.Language;
import ch.bissbert.peakseek.data.Point;
import ch.bissbert.peakseek.data.PointType;

/**
 * This class fetches the new Point data from the database if there is some new or if its the first time the app gets opened
 *
 * @author fabian
 */
public class LocationFetcher extends AsyncTask<String, Void, String> {
    private Connection connection;
    private final SharedPreferences preferences;
    private final Resources resources;
    @SuppressLint("StaticFieldLeak")
    private final Activity context;

    private final String schema;
    private final String table;

    public LocationFetcher(Activity context) throws ClassNotFoundException, SQLException {
        this.context = context;
        this.preferences = context.getPreferences(Context.MODE_PRIVATE);
        this.resources = context.getResources();
        Class.forName("com.mysql.jdbc.Driver");
        this.schema = resources.getString(R.string.DB_SCHEMA);
        Log.d(resources.getString(R.string.LOAD_TAG), "schema=" + schema);
        this.table = resources.getString(R.string.DB_POINT_TABLE);
        Log.d(resources.getString(R.string.LOAD_TAG), "table=" + table);
    }


    @Override
    protected String doInBackground(String... params) {
        int newest;
        try {
            String dbUrl = resources.getString(R.string.DB_URL);
            String user = resources.getString(R.string.DB_USER);
            String password = resources.getString(R.string.DB_PASSWORD);
            Log.d(resources.getString(R.string.LOAD_TAG), "creating connection to DB");
            this.connection = DriverManager.getConnection(dbUrl, user, password);
            Log.d(resources.getString(R.string.LOAD_TAG), "connection to DB created");

            newest = checkIfNewest();
            Log.d(resources.getString(R.string.LOAD_TAG), "newest level: " + newest);

            if (newest == 2) {
                new AlertDialog.Builder(context)
                        .setTitle(resources.getString(R.string.FETCHNEW_TITLE))
                        .setMessage(resources.getString(R.string.FETCHNEW_MESSAGE))
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
                            try {
                                loadNewData();
                            } catch (SQLException throwables) {
                                throwables.printStackTrace();
                            }
                        })
                        .setNegativeButton(android.R.string.no, null).show();
            } else if (newest == 0) {
                loadNewData();
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            try {
                connection.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        return "";
    }

    /**
     * the overall methods for managing when what gets done
     *
     * @throws SQLException thrown when sql query in sub-function goes wrong
     */
    private void loadNewData() throws SQLException {
        final int size = getListSize();
        Log.d(resources.getString(R.string.LOAD_TAG), "size of db: " + size);

        fetchLanguages();
        fetchTypes();
        fetchPoints(size);
    }

    /**
     * fetches the points from the database and saves them locally
     *
     * @param size size of points list to be loaded
     * @throws SQLException thrown when wrong data is read from db or invalid query executed
     */
    private void fetchPoints(int size) throws SQLException {
        String fetchStatement = "Select idLocations as id, name, east, north, height, type, language FROM " + schema + "." + table
                + " WHERE " + pointTypeOrQuery();

        try (PreparedStatement statement = connection.prepareStatement(fetchStatement, ResultSet.TYPE_FORWARD_ONLY)) {
            Point.deleteAll(Point.class);
            ProgressBar progressBar = createDialog();
            //progressBar.setMax(size);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.setFetchSize(resources.getInteger(R.integer.FETCH_SIZE));
                while (resultSet.next()) {
                    Point.save(createPoint(resultSet));
                    //progressBar.incrementProgressBy(1);
                    Log.d(resources.getString(R.string.LOAD_TAG), "current row: " + resultSet.getRow());
                }
            }
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(resources.getString(R.string.DB_LAST_DATE_KEY), LocalDate.now().toString());
            editor.apply();
            closeDialog();
        }
    }

    /**
     * fetches the languages from the database and stores them locally
     *
     * @throws SQLException thrown when wrong data is read from db or invalid query executed
     */
    private void fetchLanguages() throws SQLException {
        String fetchStatement = "Select Short_Name as short, name FROM " + schema + "." + resources.getString(R.string.LANGUAGE_TABLE);
        Language.deleteAll(Language.class);
        try (Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery(fetchStatement)) {
                while (resultSet.next()) {
                    Language.save(createLanguage(resultSet));
                }
            }
        }
    }

    /**
     * creates a Language object from the data contained within the ResultSet
     *
     * @param resultSet a resultset containing the data needed for a Language instance
     * @return the instanced Language object
     * @throws SQLException thrown when wrong data is read from db
     */
    private Language createLanguage(ResultSet resultSet) throws SQLException {
        Language language = new Language();
        language.setShortName(resultSet.getString("short"));
        language.setName(resultSet.getString("name"));
        return language;
    }

    /**
     * fetches the types of points from the database and saves them locally
     *
     * @throws SQLException thrown when wrong data is read from db or invalid query executed
     */
    private void fetchTypes() throws SQLException {
        String fetchStatement = "Select idtypes as id, name FROM " + schema + "." + resources.getString(R.string.TYPE_TABLE);
        PointType.deleteAll(PointType.class);
        try (Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery(fetchStatement)) {
                while (resultSet.next()) {
                    PointType.save(createType(resultSet));
                }
            }
        }
    }

    /**
     * creates a PointType instance from the data within the ResultSet
     *
     * @param resultSet a ResultSet containing the data needed to instance a PointType object
     * @return an instance of PointType from the data in the ResultSet
     * @throws SQLException thrown when wrong data is read from db
     */
    private PointType createType(ResultSet resultSet) throws SQLException {
        PointType type = new PointType();
        type.setId(resultSet.getLong("id"));
        type.setName(resultSet.getString("name"));
        return type;
    }

    private AlertDialog.Builder builder;
    private Dialog dialog;

    /**
     * creates a dialog with a progressbar and returns the bar
     *
     * @return the progressbar from the dialog
     */
    private ProgressBar createDialog() {
        builder = new AlertDialog.Builder(context);
        builder.setView(R.layout.progress);
        builder.setCancelable(false);
        builder.setTitle(resources.getText(R.string.LOAD_TITLE));

        Looper.prepare();

        dialog = builder.create();
        dialog.show();
        return context.findViewById(R.id.loader);
    }

    /**
     * closes the last created dialog
     */
    private void closeDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    /**
     * creates a Point instance from the data within the ResultSet
     *
     * @param resultSet a ResultSet containing the data needed to instance a Point object
     * @return an instance of Point from the data in the ResultSet
     * @throws SQLException thrown when wrong data is read from db
     */
    private static Point createPoint(ResultSet resultSet) throws SQLException {
        //id, name, east, north, height, type, language
        Point point = new Point();
        point.setId(resultSet.getLong("id"));
        point.setName(resultSet.getString("name"));
        point.setEast(resultSet.getLong("east"));
        point.setNorth(resultSet.getLong("north"));
        point.setAltitude(resultSet.getDouble("height"));
        point.setType(PointType.findById(PointType.class, resultSet.getInt("type")));
        point.setLanguage(
                Language.findWithQuery(
                        Language.class,
                        "SELECT * FROM Language WHERE shortName = ?",
                        resultSet.getString("language")
                ).get(0)
        );
        return point;
    }

    /**
     * contains the ids of types to be loaded form the database
     *
     * @return array containing the ids of
     */
    private static int[] getPointTypesToLoad() {
        return new int[]{15, 16, 17, 18, 19, 20};
    }

    private static String pointTypeOrQuery() {
        int[] pointTypes = getPointTypesToLoad();
        StringJoiner typeJoiner = new StringJoiner(" OR ");
        for (int i = 0; i < pointTypes.length; i++) {
            typeJoiner.add("type=" + pointTypes[i]);
        }
        return typeJoiner.toString();
    }

    /**
     * fetches the size of the points table
     *
     * @return size of the points table as an int
     * @throws SQLException thrown if query is wrong
     */
    private int getListSize() throws SQLException {
        String sizeQuery = "SELECT count(*) FROM " + schema + "." + table;
        try (Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery(sizeQuery)) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
        }
        return 0;
    }

    private int checkIfNewest() throws SQLException {
        Log.d(resources.getString(R.string.LOAD_TAG), "checking if newest data is saved");
        String lastDateAsString = preferences.getString(resources.getString(R.string.DB_LAST_DATE_KEY), null);
        LocalDate lastDate;
        if (lastDateAsString == null) {
            return 0;
        } else {
            lastDate = LocalDate.parse(lastDateAsString);
        }

        String checkQuery = "SELECT last_update as timestamp " +
                "FROM mysql.innodb_table_stats " +
                "WHERE database_name=? AND TABLE_NAME=?";

        try (PreparedStatement statement = connection.prepareStatement(checkQuery)) {

            Log.d(resources.getString(R.string.LOAD_TAG), "setting first param to '" + schema + "'");
            statement.setString(1, schema);
            Log.d(resources.getString(R.string.LOAD_TAG), "setting second param to '" + table + "'");
            statement.setString(2, table);
            Log.d(resources.getString(R.string.LOAD_TAG), "prep statement resulting = " + statement.toString());

            try (ResultSet resultSet = statement.executeQuery()) {

                LocalDate lastUpdateDB;

                if (resultSet.next()) {
                    Date date = resultSet.getDate("timestamp");
                    lastUpdateDB = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                } else {
                    return 1;
                }

                if (lastDate.isAfter(lastUpdateDB)) {
                    return 2;
                }
            }
        }
        return 1;
    }

    public static List<Point> getPointsInRadius(int radiusInMeter, int north, int east, Connection connection) throws SQLException {
        //todo implement for working with sugar

        String query = "SELECT idLocations as id, name, east, north, height, type, language ,(SQRT(POW(east-?,2)+POW(north-?, 2))) as distance\n" +
                "FROM peakseek.locations\n" +
                "WHERE " + pointTypeOrQuery() + "\n" +
                "HAVING distance <= ? AND distance > 0\n" +
                "ORDER BY distance";

        List<Point> points = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, east);
            statement.setLong(2, north);
            statement.setInt(3, radiusInMeter);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    points.add(createPoint(resultSet));
                }
            }
        }
        return points;
    }
}
