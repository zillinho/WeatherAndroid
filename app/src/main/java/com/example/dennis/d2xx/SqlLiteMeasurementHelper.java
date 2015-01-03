package com.example.dennis.d2xx;

import android.content.ContentValues;
import android.content.Context;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Dennis on 12.11.2014.
 * Helper class which enables Database interaction
 */
class SqlLiteMeasurementHelper extends SQLiteOpenHelper {

    private static  final String DATABASE_NAME = "measurements";
    private static final int DATABASE_VERSION = 1;

    private static final String CREATE =  "CREATE TABLE IF NOT EXISTS measurements (" +
            " measureID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "deviceId INTEGER, date INTEGER, " +
            "bmp180Temp REAL, bmp180Pressure REAL, " +
            "sht21Temp REAL, sht21Humidity REAL, " +
            "lm73Temp REAL, medianTemp REAL);";

    public SqlLiteMeasurementHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS measurements");
        onCreate(db);
    }

    /**
     * Inserts a Measurement into to the SQL Lite Table.
     * @param m The measurement to get inserted into the table.
     */
    public void insertMeasurement(Measurement m) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("deviceId", m.getId());
        values.put("date", m.getMeasuredTime());
        values.put("bmp180Temp", m.getBmp180Temperature());
        values.put("bmp180Pressure", m.getBmp180Pressure());
        values.put("sht21Temp", m.getSht21Temperature());
        values.put("sht21Humidity", m.getSht21Humidity());
        values.put("lm73Temp", m.getLm73Temperature());
        values.put("medianTemp", m.getMedianTemperature());

        db.insert("measurements", null, values);
        db.close();
    }

    /**
     * Get number of rows in Database
     * @return Number of rows in DB
     */
    public long countRows() {
        SQLiteDatabase db = this.getReadableDatabase();
        return DatabaseUtils.queryNumEntries(db ,"measurements");
    }
}
