package com.kerrywei.GrandRiverTransit;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

/*
 * Once launched, the app will check for database updates from my host in UW CSClub.
 * I commented out update-check code because my UW CSClub account has expired.
 * I'll Update related part of code once I reactivate my account.
 */
public class GrandRiverTransitActivity extends ListActivity {
    final String DEBUG = "GRT Assistant DEBUG";
    final String DB_UPDATE_URL = "http://csclub.uwaterloo.ca/~x9wei/grt/grt_db_update.xml";
    final String NEW_DB_DIRECTORY = "http://csclub.uwaterloo.ca/~x9wei/grt/";

    private String GRT_DB_NAME = "grt.db";
    private String GRT_DB_FOLDER = Environment.getExternalStorageDirectory().getAbsolutePath()
            + "/data/data/com.kerrywei.grt/databases/";

    NewDatabaseAdapter databaseAdapter;

    // activity code:
    private final int NEW_SCHEDULE = 0;
    private final int FULL_SCHEDULE = 1;
    private final int APP_SETTINGS = 2;
    private final int ABOUT = 3;

    ProgressDialog progressDialog;

    public class UpdateDatabase extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... url) {
            int count;
            try {
                URL newUrl = new URL(url[0]);
                URLConnection conexion = newUrl.openConnection();
                conexion.connect();
                // this will be useful so that you can show a tipical 0-100% progress bar
                int lenghtOfFile = conexion.getContentLength();

                // download the file
                String prefix = getResources().getString(R.string.MainActivity_tmpDBPrefix);
                SharedPreferences sharedPref = PreferenceManager
                        .getDefaultSharedPreferences(GrandRiverTransitActivity.this);
                String latest_db_name = sharedPref.getString(
                        getResources().getString(R.string.MainActivity_latestDBName), "grt.db");
                InputStream input = new BufferedInputStream(newUrl.openStream());
                OutputStream output = new FileOutputStream(GRT_DB_FOLDER + prefix + latest_db_name);

                byte data[] = new byte[1024];
                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    publishProgress((int) (total * 100 / lenghtOfFile));
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();
            } catch (IOException e) {
                Log.e(DEBUG, "IOException during updating databases!");
            }
            return null;
        }

        @Override
        public void onProgressUpdate(Integer... args) {
            progressDialog.setProgress(args[0]);
            if (args[0] >= 100) {
                progressDialog.dismiss();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            SharedPreferences sharedPref = PreferenceManager
                    .getDefaultSharedPreferences(GrandRiverTransitActivity.this);
            String local_db_name = sharedPref.getString(getResources().getString(R.string.MainActivity_localDBName),
                    "grt.db");
            String latest_db_name = sharedPref.getString(getResources().getString(R.string.MainActivity_latestDBName),
                    "grt.db");

            // remove the old DB, and rename the new DB:
            File localDB = new File(GRT_DB_FOLDER + local_db_name);
            if (localDB.delete()) {
                String prefix = getResources().getString(R.string.MainActivity_tmpDBPrefix);
                File from = new File(GRT_DB_FOLDER + prefix + latest_db_name);
                File to = new File(GRT_DB_FOLDER + latest_db_name);
                from.renameTo(to);

                // update local db version:
                String latestDBVersion = sharedPref.getString(
                        getResources().getString(R.string.MainActivity_latestDBVersion), "unknown");
                Utilities.updateSharedPreferences(getResources().getString(R.string.MainActivity_localDBName),
                        latest_db_name);
                Utilities.updateSharedPreferences(getResources().getString(R.string.MainActivity_localDBVersion),
                        latestDBVersion);
            }
        }
    }

    public class CopyDatabase extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... url) {
            try {
                File dbDir = createDBFile();
                // Open your local db as the input stream
                InputStream myInput = getAssets().open(GRT_DB_NAME);
                // Open the empty db as the output stream
                OutputStream myOutput = new FileOutputStream(dbDir);
                int lengthOfFile = myInput.available();

                if (Environment.getExternalStorageDirectory().canWrite()) {
                    // transfer bytes from the inputfile to the outputfile
                    byte[] buffer = new byte[1024];
                    int length;
                    long total = 0;
                    while ((length = myInput.read(buffer)) > 0) {
                        total += length;
                        publishProgress((int) (total * 100 / lengthOfFile));
                        myOutput.write(buffer, 0, length);
                    }
                    // Close the streams
                    myOutput.flush();
                    myOutput.close();
                    myInput.close();
                }
                Utilities.updateSharedPreferences("local_db_version", "1.0");

                /*
                 * Utilities.init(this); Utilities.initDatabaseAdapter(); databaseAdapter =
                 * Utilities.getNewDatabaseAdapter();
                 * 
                 * fillData(); registerForContextMenu(getListView());
                 * 
                 * checkDatabaseUpdate();
                 */
            } catch (FileNotFoundException e) {
                Log.d(DEBUG, "FileNotFoundException in during copying the database!");
            } catch (IOException e) {
                Log.d(DEBUG, "Failed to copy database from the assets folder to the sdcard!");
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Utilities.initDatabaseAdapter();
            databaseAdapter = Utilities.getNewDatabaseAdapter();
            fillData();
            registerForContextMenu(getListView());

            // DON'T CHECK DATABASE UPDATES FOR NOW! (because my UW csclub host expired...)
            // checkDatabaseUpdate();
        }

        @Override
        public void onProgressUpdate(Integer... args) {
            progressDialog.setProgress(args[0]);
            if (args[0] >= 100) {
                progressDialog.dismiss();
            }
        }
    }

    public class MyXMLHandler extends DefaultHandler {
        Boolean currentElement = false;
        String currentValue = null;

        /*
         * Called when tag starts ( ex:- <name>AndroidPeople</name> -- <name> )
         */
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            currentElement = true;
            if (localName.equals("grt_db_update")) {
                /* Start */
                // sitesList = new SitesList();
                Log.d(DEBUG, "start parsing xml file.");
            } else if (localName.equals("latest_db_file_name")) {
                /* Get attribute value */
                // String attr = attributes.getValue("category");
                // sitesList.setCategory(attr);
            }

        }

        /*
         * Called when tag closing ( ex:- <name>AndroidPeople</name> -- </name> )
         */
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            currentElement = false;
            /* set value */
            if (localName.equalsIgnoreCase(getResources().getString(R.string.MainActivity_latestDBVersion))) {
                Utilities.updateSharedPreferences(localName, currentValue);
            } else if (localName.equalsIgnoreCase(getResources().getString(R.string.MainActivity_latestDBName))) {
                // sitesList.setWebsite(currentValue);
                Utilities.updateSharedPreferences(localName, currentValue);
            }
        }

        /*
         * Called to get tag characters ( ex:- <name>AndroidPeople</name> -- to get AndroidPeople Character )
         */
        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (currentElement) {
                currentValue = new String(ch, start, length);
                currentElement = false;
            }
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.grt_main);
        Utilities.init(GrandRiverTransitActivity.this);
        if (!canOpenDatabase()) {
            // copy database:
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Initializing databases...");
            progressDialog.setIndeterminate(false);
            progressDialog.setMax(100);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setCancelable(false);
            CopyDatabase copyDB = new CopyDatabase();
            copyDB.execute();
            progressDialog.show();
        } else {
            Utilities.init(this);
            Utilities.initDatabaseAdapter();
            databaseAdapter = Utilities.getNewDatabaseAdapter();

            fillData();
            registerForContextMenu(getListView());

            // DON'T CHECK DATABASE UPDATES FOR NOW! (because my UW csclub host expired...)
            // checkDatabaseUpdate();
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        menu.setHeaderTitle("Select An Action");
        inflater.inflate(R.menu.schedule_list_context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
        case R.id.ScheduleList_delete:
            deleteSchedule(info.id);
            return true;
        case R.id.ScheduleList_edit:
            editSchedule(info.id);
            return true;
        default:
            return super.onContextItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_screen_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i;
        switch (item.getItemId()) {
        case R.id.MainScreenMenuItem_newRoute:
            i = new Intent(this, NewSchedule.class);
            startActivityForResult(i, NEW_SCHEDULE);
            return true;
        case R.id.MainScreenMenuItem_fullSchedule:
            return true;
        case R.id.MainScreenMenuItem_settings:
            i = new Intent(getApplicationContext(), AppSettings.class);
            startActivityForResult(i, APP_SETTINGS);
            return true;
        case R.id.MainScreenMenuItem_about:
            i = new Intent(this, About.class);
            startActivity(i);
            return true;
        default:
            return true;
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        String routeID = null, startStopID = null, endStopID = null, startStopName = null, endStopName = null;

        // get routeID:
        Cursor c = databaseAdapter.getRouteInfoFromFavorites(id);
        if (c.moveToFirst()) {
            routeID = c.getString(c.getColumnIndexOrThrow(NewDatabaseAdapter.MY_SCHEDULES_ROUTE_ID));
            startStopID = c.getString(c.getColumnIndexOrThrow(NewDatabaseAdapter.START_STOP_ID));
            startStopName = c.getString(c.getColumnIndexOrThrow(NewDatabaseAdapter.START_STOP_NAME));
            endStopID = c.getString(c.getColumnIndexOrThrow(NewDatabaseAdapter.END_STOP_ID));
            endStopName = c.getString(c.getColumnIndexOrThrow(NewDatabaseAdapter.END_STOP_NAME));
        }

        Intent i = new Intent(this, BusStopTime.class);
        // pass data needed by the BusStopTime activity:
        BusStopTime.putExtra(i, routeID, startStopID, endStopID, startStopName, endStopName);

        startActivity(i);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case APP_SETTINGS:
            // update my schedule list if a new schedule is added:
            if (resultCode == RESULT_OK) {
            }
            break;
        case NEW_SCHEDULE:
            if (resultCode == RESULT_OK) {
                fillData();
            }
            break;
        case FULL_SCHEDULE:
            break;
        case ABOUT:
            break;
        default:
            break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /*
         * if (databaseAdapter != null) { databaseAdapter.close(); }
         */
    }

    private void fillData() {
        Cursor cursor = databaseAdapter.getMyShceuldes();
        startManagingCursor(cursor);

        String[] from = new String[] { NewDatabaseAdapter.MY_SCHEDULES_ROUTE_ID, NewDatabaseAdapter.START_STOP_NAME,
                NewDatabaseAdapter.END_STOP_NAME };
        int[] to = new int[] { R.id.ScheduleListEntry_routeName, R.id.ScheduleListEntry_startStop,
                R.id.ScheduleListEntry_endStop };

        // Now create an array adapter and set it to display using our row
        // Discouraged. Use CursorManager and CursorLoader instead
        SimpleCursorAdapter cursorAdapter = new SimpleCursorAdapter(this, R.layout.schedule_list_entry, cursor, from,
                to);
        setListAdapter(cursorAdapter);
    }

    private void editSchedule(long _id) {
        Cursor c = databaseAdapter.getMyFavoriteScheduleInfo(_id);
        String routeShortName = null, startStopID = null, endStopID = null;
        if (c.moveToFirst()) {
            routeShortName = c.getString(c.getColumnIndexOrThrow(NewDatabaseAdapter.MY_SCHEDULES_ROUTE_SHORT_NAME));
            startStopID = c.getString(c.getColumnIndexOrThrow(NewDatabaseAdapter.START_STOP_ID));
            endStopID = c.getString(c.getColumnIndexOrThrow(NewDatabaseAdapter.END_STOP_ID));
        }
        c.close();

        Intent i = new Intent(this, StopsList.class);
        StopsList.putExtra(i, _id, routeShortName, startStopID, endStopID);

        startActivity(i);
    }

    private void deleteSchedule(long _id) {
        databaseAdapter.deleteMyScheduleEntry(_id);
        Cursor cursor = databaseAdapter.getMyShceuldes();
        String[] from = new String[] { NewDatabaseAdapter.MY_SCHEDULES_ROUTE_ID, NewDatabaseAdapter.START_STOP_NAME,
                NewDatabaseAdapter.END_STOP_NAME };
        int[] to = new int[] { R.id.ScheduleListEntry_routeName, R.id.ScheduleListEntry_startStop,
                R.id.ScheduleListEntry_endStop };

        SimpleCursorAdapter cursorAdapter = new SimpleCursorAdapter(this, R.layout.schedule_list_entry, cursor, from,
                to);
        setListAdapter(cursorAdapter);
    }

    private void checkDatabaseUpdate() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String lastTimeCheck = sharedPref.getString(getResources().getString(R.string.MainActivity_lastTimeCheck),
                "unknown");
        if (!lastTimeCheck.equals(Utilities.getCurrentDate())) {
            try {
                /* Handling XML */
                SAXParserFactory spf = SAXParserFactory.newInstance();
                SAXParser sp = spf.newSAXParser();
                XMLReader xr = sp.getXMLReader();

                /* Send URL to parse XML Tags */
                URL sourceUrl = new URL(DB_UPDATE_URL);

                /* Create handler to handle XML Tags ( extends DefaultHandler ) */
                MyXMLHandler myXMLHandler = new MyXMLHandler();
                xr.setContentHandler(myXMLHandler);
                xr.parse(new InputSource(sourceUrl.openStream()));

                // check db version:
                String localDBVersion = sharedPref.getString(
                        getResources().getString(R.string.MainActivity_localDBVersion), "1.0");
                // String localDBName =
                // sharedPref.getString(getResources().getString(R.string.MainActivity_localDBName), "grt.db");
                String latestDBVersion = sharedPref.getString(
                        getResources().getString(R.string.MainActivity_latestDBVersion), "1.0");
                // String latestDBFileName =
                // sharedPref.getString(getResources().getString(R.string.MainActivity_latestDBFileName), "grt.db");

                // LOCAL_DB_NAME = localDBName;
                // LATEST_DB_NAME = latestDBFileName;

                double localDBVersionDouble = Double.parseDouble(localDBVersion);
                double latestDBVersionDouble = Double.parseDouble(latestDBVersion);
                if (localDBVersionDouble < latestDBVersionDouble) {
                    // download database update:
                    progressDialog = new ProgressDialog(GrandRiverTransitActivity.this);
                    progressDialog.setMessage("Downloading Latest Bus Schedules...");
                    progressDialog.setIndeterminate(false);
                    progressDialog.setMax(100);
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    progressDialog.setCancelable(false);
                    UpdateDatabase downloadFile = new UpdateDatabase();
                    downloadFile.execute(NEW_DB_DIRECTORY
                            + sharedPref.getString(getResources().getString(R.string.MainActivity_latestDBName),
                                    "ERROR"));
                    progressDialog.show();
                }

                // db update checked
                Utilities.updateSharedPreferences(getResources().getString(R.string.MainActivity_lastTimeCheck),
                        Utilities.getCurrentDate());
            } catch (Exception e) {
                System.out.println("XML Pasing Excpetion = " + e);
            }
        }
    }

    private boolean canOpenDatabase() {
        SQLiteDatabase database = null;
        try {
            String dbPath = GRT_DB_FOLDER + GRT_DB_NAME;
            database = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
        } catch (SQLiteException e) {
            Log.d(DEBUG,
                    "SQLiteException in checkDatabase! Need to copy database from the assets folder to the SDCard.");
        }
        if (database != null) {
            database.close();
        }
        return database != null ? true : false;
    }

    private File createDBFile() {
        String sdcard = Environment.getExternalStorageDirectory().getAbsolutePath();
        File dbDir = new File(sdcard + "/data/data/com.kerrywei.grt/databases/");
        dbDir.mkdirs();
        dbDir = new File(sdcard + "/data/data/com.kerrywei.grt/databases/grt.db");
        return dbDir;
    }

}
