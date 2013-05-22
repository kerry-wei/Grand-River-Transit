package com.kerrywei.GrandRiverTransit;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

public class Utilities {
    final static String DEBUG = "GRT Assistant DEBUG";
    
    static Context context;
    
    static NewDatabaseAdapter newDbAdapter;
    
    
    public static void init(Context _context) {
        context = _context;
    }

    public static Context getContext(){
        return context;
    }
    
    static void initDatabaseAdapter() {
        if (newDbAdapter == null) {
            newDbAdapter = new NewDatabaseAdapter(context);
            newDbAdapter.open();
        }
    }
    
    static NewDatabaseAdapter getNewDatabaseAdapter() {
        if (newDbAdapter == null) {
            newDbAdapter = new NewDatabaseAdapter(context);
            newDbAdapter.open();
        }
        return newDbAdapter;
    }
    
    static BufferedWriter getBufferedWriter(String fileName) {
        try {
            File file = new File(Environment.getExternalStorageDirectory(), fileName);
            FileWriter fileWriter = new FileWriter(file, true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            return bufferedWriter;
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(DEBUG, "Failed to create a BufferedWriter.");
            return null;
        }
    }
    
    static BufferedReader getBufferedReader(String fileName) {
        BufferedReader bufferedReader = null;
        try {
            InputStream inputStream = Resources.getSystem().getAssets().open(fileName);
            Reader reader = new InputStreamReader(inputStream);
            bufferedReader = new BufferedReader(reader);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(DEBUG, "ERROR: failed to create BufferedReader for" + fileName);
        } 
        return bufferedReader;
    }
    
    static void updateSharedPreferences(String key, String value) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        Editor sharedPrefEditor = sharedPref.edit();
        sharedPrefEditor.putString(key, value);
        sharedPrefEditor.commit();
    }
    
    static String getCurrentDate() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        return sdf.format(cal.getTime());
    }
    
    
}

