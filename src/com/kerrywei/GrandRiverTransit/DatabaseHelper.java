package com.kerrywei.GrandRiverTransit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
    
    private String DEBUG = "GRT Assistant DEBUG";
    private  String GRT_DB_PATH = 
            Environment.getExternalStorageDirectory().getAbsolutePath()
            + "/data/data/com.kerrywei.grt/databases/";
    private  String GRT_DB_NAME = "grt.db";
    
    Context context;
    ProgressDialog progressDialog;
    
    // database version:
    //static final int VERSION = 1;
    
    // table names:
    private final String MY_SCHEDULES = "MySchedules";
    
    // create tables:
    private final String CREATE_MY_SCHEDULES = 
            "CREATE TABLE " + MY_SCHEDULES + " "
            + "( _id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "routeID TEXT NOT NULL, "
            + "startStopID TEXT NOT NULL,"
            + "startStopName TEXT NOT NULL, "
            + "endStopID TEXT NOT NULL,"
            + "endStopName TEXT NOT NULL,"
            + "routeShortName INTEGER NOT NULL);";
    
    public class CopyDatabase extends AsyncTask<String, Integer, String> {
        
        @Override
        protected String doInBackground(String... url) {
            int count;
            try {
                File dbDir = createDBFile();
                //Open your local db as the input stream
                InputStream myInput = context.getAssets().open(GRT_DB_NAME);
                //Open the empty db as the output stream
                OutputStream myOutput = new FileOutputStream(dbDir);
                int lengthOfFile = myInput.available();
                
                if (Environment.getExternalStorageDirectory().canWrite()) {
                  //transfer bytes from the inputfile to the outputfile
                    byte[] buffer = new byte[1024];
                    int length;
                    long total = 0;
                    while ( (length = myInput.read(buffer)) > 0) {
                        total += length;
                        publishProgress((int) (total * 100 / lengthOfFile));
                        myOutput.write(buffer, 0, length);
                    }
                    //Close the streams
                    myOutput.flush();
                    myOutput.close();
                    myInput.close();
                }
                Utilities.updateSharedPreferences("local_db_version", "1.0");
            } catch (FileNotFoundException e) {
                Log.d(DEBUG, "FileNotFoundException in during copying the database!");
            } catch (IOException e) {
                Log.d(DEBUG, "Failed to copy database from the assets folder to the sdcard!");
            } 
            return null;
        }
        
        @Override
        public void onProgressUpdate(Integer... args){
            progressDialog.setProgress(args[0]);
            if (args[0] >= 100) {
                progressDialog.dismiss();
            }
        }
    }
    
    public DatabaseHelper(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
        this.context = context;
    }
    
    
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            // create all tables needed:
            db.execSQL(CREATE_MY_SCHEDULES);
            
            // copy database from the assetes folder to sdcard if necessary:
            if (!checkDatabase()) {
                // copy database:
                progressDialog = new ProgressDialog(context);
                progressDialog.setMessage("Initializing databases...");
                progressDialog.setIndeterminate(false);
                progressDialog.setMax(100);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.setCancelable(false);
                CopyDatabase copyDB = new CopyDatabase();
                copyDB.execute();
                progressDialog.show();
                // copyDataBase();
            }
            
        } catch (SQLException e) {
            Log.d(DatabaseHelper.class.getName(), "Failed to create database(s)");
            Log.d(DatabaseHelper.class.getName(), e.getMessage());
            e.printStackTrace();
        } /*catch (IOException e) {
            Log.d(DEBUG, "Failed to copy database from the assets folder to the sdcard!");
        }*/
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String message = "Update " + MY_SCHEDULES + " from version " + oldVersion + " to version" + newVersion;
        Log.v(DatabaseHelper.class.getName(), message);
        
        db.execSQL("DROP TABLE IF EXISTS " + MY_SCHEDULES);
        onCreate(db);
    }
    
    private boolean checkDatabase(){
        SQLiteDatabase checkDB = null;
        try {
            String myPath = GRT_DB_PATH + GRT_DB_NAME;
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
        } catch(SQLiteException e){
            Log.d(DEBUG, "SQLiteException in checkDatabase! Need to copy database from the assets folder to the SDCard.");
        } 
        if (checkDB != null) {
            checkDB.close();
        }
        return checkDB != null ? true : false;
    }
    
    private void copyDataBase() {
        try {
            File dbDir = createDBFile();
            //Open your local db as the input stream
            InputStream myInput = context.getAssets().open(GRT_DB_NAME);
            //Open the empty db as the output stream
            OutputStream myOutput = new FileOutputStream(dbDir);
            if (Environment.getExternalStorageDirectory().canWrite()) {
              //transfer bytes from the inputfile to the outputfile
                byte[] buffer = new byte[1024];
                int length;
                while ( (length = myInput.read(buffer)) > 0){
                    myOutput.write(buffer, 0, length);
                }
                //Close the streams
                myOutput.flush();
                myOutput.close();
                myInput.close();
            }
            Utilities.updateSharedPreferences("local_db_version", "1.0");
        } catch (FileNotFoundException e) {
            Log.d(DEBUG, "FileNotFoundException in during copying the database!");
        } catch (IOException e) {
            Log.d(DEBUG, "Failed to copy database from the assets folder to the sdcard!");
        } 
    }
    
    private File createDBFile() {
        String sdcard = Environment.getExternalStorageDirectory().getAbsolutePath();
        File dbDir = new File(sdcard + "/data/data/com.kerrywei.grt/databases/");
        dbDir.mkdirs();
        dbDir = new File(sdcard + "/data/data/com.kerrywei.grt/databases/grt.db");
        return dbDir;
    }
    
    public synchronized SQLiteDatabase getGRTDatabase() {
        String path = GRT_DB_PATH + GRT_DB_NAME;
        SQLiteDatabase database = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READWRITE);
        return database;
    }
    
    
    
}
