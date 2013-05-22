package com.kerrywei.GrandRiverTransit;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class NewDatabaseAdapter {
    final String DEBUG = "GRT Assistant DEBUG";
    
    
    
    // MY_SCHEDULES:
    public static final String MY_SCHEDULES = "MySchedules";
    public static final String MY_SCHEDULES_ROWID = "_id";
    public static final String MY_SCHEDULES_ROUTE_ID = "routeID";
    public static final String START_STOP_ID = "startStopID";
    public static final String START_STOP_NAME = "startStopName";
    public static final String END_STOP_ID = "endStopID";
    public static final String END_STOP_NAME = "endStopName";
    public static final String MY_SCHEDULES_ROUTE_SHORT_NAME = "routeShortName";
    
    // TRIPS:
    // block_id,route_id,trip_headsign,service_id,shape_id,trip_id
    public static final String TRIPS = "Trips";
    public static final String TRIPS_ROWID = "_id";
    public static final String BLOCK_ID = "block_id";
    public static final String ROUTE_ID = "route_id";
    public static final String TRIP_HEADSIGN = "trip_headsign";
    public static final String SERVICE_ID = "service_id";
    public static final String SHAPE_ID = "shape_id";
    public static final String TRIP_ID = "trip_id";
    
    // GRT database:
    // stops:
    public static final String STOPS = "stops";
    public static final String STOPS_ROWID = "_id";
    public static final String STOPS_LAT = "stop_lat";
    public static final String STOPS_LON = "stop_lon";
    public static final String STOPS_ID = "stop_id";
    public static final String STOPS_NAME = "stop_name";
    
    // routes:
    public static final String ROUTES = "routes";
    public static final String ROUTES_ROWID = "_id";
    public static final String ROUTES_LONG_NAME = "route_long_name";
    public static final String ROUTES_SHORT_NAME = "route_short_name";
    public static final String ROUTES_ID = "route_id";
    
    // stop_times:
    // trip_id,arrival_time,departure_time,stop_id,stop_sequence,stop_headsign,pickup_type,drop_off_type,shape_dist_traveled
    public static final String STOP_TIMES = "stop_times";
    public static final String STOP_TIMES_TRIP_ID = "trip_id";
    public static final String STOP_TIMES_ARRIVAL_TIME = "arrival_time";
    public static final String STOP_TIMES_DEPARTURE_TIME = "departure_time";
    public static final String STOP_TIMES_STOP_ID = "stop_id";
    public static final String STOP_TIMES_STOP_SEQUENCE = "stop_sequence";
    public static final String STOP_TIMES_STOP_NAME = "stop_name";
    
    // calendar:
    // service_id,start_date,end_date,monday,tuesday,wednesday,thursday,friday,saturday,sunday
    public static final String CALENDAR = "calendar";
    public static final String CALENDAR_START_DATE = "stop_times";
    public static final String CALENDAR_END_DATE = "stop_times";
    public static final String CALENDAR_SERVICE_ID = "service_id";
    
    
    Context context;
    SQLiteDatabase database;
    SQLiteDatabase grtDatabase;
    DatabaseHelper databaseHelper;
    
    public NewDatabaseAdapter(Context _context) {
        context = _context;
    }

    public NewDatabaseAdapter open() throws SQLException {
        databaseHelper = new DatabaseHelper(context, "GRT Assistant", null, 1);
        database = databaseHelper.getWritableDatabase();
        grtDatabase = databaseHelper.getGRTDatabase();
        return this;
    }

    public void close() {
        databaseHelper.close();
    }
    
    public SQLiteDatabase getGRTDatabase() {
        return grtDatabase;
    }
    
    /*
    public Cursor extreme(int[] trips, String routeID, String startStopID, String endStopID, String startStopName, String endStopName) {
        String tableName = "stop_times_" + routeID;
        // format trips:
        String values = "()";
        if (trips.length != 0 && trips != null) {
            values = "(" + trips[0];
            for (int i = 1; i < trips.length; i++) {
                values += ", " + trips[i];
            }
            values += ")";
        }
        
        // construct subTable1:
        String subTable1 = "SELECT distinct _id, trip_id, departure_time, stop_sequence "
                + "FROM " + tableName + " "
                + "WHERE stop_id = " + startStopID + " AND  trip_id IN " + values;
        
        // construct subTable2:
        String subTable2 = "SELECT distinct trip_id, arrival_time, stop_sequence "
                + "FROM " + tableName + " "
                + "WHERE stop_id = " + endStopID + " AND trip_id IN " + values;
        
        
        String sql = "SELECT st1._id AS _id, st1.trip_id, st1.departure_time AS departure_time, st2.arrival_time AS arrival_time FROM "
                + "(" + subTable1 + ") AS st1, "
                + "(" + subTable2 + ") AS st2 "
                + "WHERE st1.trip_id = st2.trip_id AND st1.stop_sequence < st2.stop_sequence "
                + "ORDER BY st1.departure_time ";
        return grtDatabase.rawQuery(sql, null);
    }
    */
    private Cursor getStopIDsBasedOnStopName(String name) {
        String sql = "SELECT stop_id FROM stops WHERE stop_name = '" + name + "'";
        return grtDatabase.rawQuery(sql, null);
    }
    
    public Cursor getBusStopTimes(int[] trips, String routeID, String startStopID, String endStopID, String startStopName, String endStopName) {
        String tableName = "stop_times_" + routeID;
        // format trips:
        String values = "()";
        if (trips.length != 0 && trips != null) {
            values = "(" + trips[0];
            for (int i = 1; i < trips.length; i++) {
                values += ", " + trips[i];
            }
            values += ")";
        }
        
        // get stop IDs of the start stop:
        String startStopIDs = "(";
        Cursor c = getStopIDsBasedOnStopName(startStopName);
        if (c.moveToFirst()) {
            do {
                startStopIDs += c.getString(c.getColumnIndexOrThrow(NewDatabaseAdapter.STOPS_ID)) + ", ";
            } while (c.moveToNext());
        }
        c.close();
        if (startStopIDs.length() >= 2) {
            startStopIDs = startStopIDs.substring(0, startStopIDs.length() - 2);
        }
        // append the closing ")":
        startStopIDs += ")";
        
        
        
        // get stop IDs of the end stop:
        String endStopIDs = "(";
        c = getStopIDsBasedOnStopName(endStopName);
        if (c.moveToFirst()) {
            do {
                endStopIDs += c.getString(c.getColumnIndexOrThrow(NewDatabaseAdapter.STOPS_ID)) + ", ";
            } while (c.moveToNext());
        }
        c.close();
        if (endStopIDs.length() >= 2) {
            endStopIDs = endStopIDs.substring(0, endStopIDs.length() - 2);
        }
        // append the closing ")":
        endStopIDs += ")";
        
        
        
        // construct subTable1:
        String subTable1 = "SELECT distinct _id, trip_id, departure_time, stop_sequence "
                + "FROM " + tableName + " "
                + "WHERE stop_id IN " + startStopIDs + " AND  trip_id IN " + values;
        
        // construct subTable2:
        String subTable2 = "SELECT distinct trip_id, arrival_time, stop_sequence "
                + "FROM " + tableName + " "
                + "WHERE stop_id IN " + endStopIDs + " AND trip_id IN " + values;
        
        
        String sql = "SELECT st1._id AS _id, st1.trip_id, st1.departure_time AS departure_time, st2.arrival_time AS arrival_time FROM "
                + "(" + subTable1 + ") AS st1, "
                + "(" + subTable2 + ") AS st2 "
                + "WHERE st1.trip_id = st2.trip_id AND st1.stop_sequence < st2.stop_sequence "
                + "ORDER BY st1.departure_time ";
        return grtDatabase.rawQuery(sql, null);
    }
    
    
    /*
    public Cursor getGRTTripID(String routeID, String[] serviceIdArr) {
        String sql = "SELECT trip_id FROM trips WHERE route_id = '" + routeID + "' AND (";
        int actualLength = 0;
        for (int i = 0; i < serviceIdArr.length; i++) {
            if (serviceIdArr[i] == null) {
                actualLength = i;
                break;
            }
        }
        String[] args = new String[actualLength];
        
        for (int i = 0; i < actualLength; i++) {
            assert(serviceIdArr[i] != null);
            args[i] = serviceIdArr[i];
            sql += " service_id = ?"  + " OR";
        }
        
        // remove the last "OR" and append ")"
        sql = sql.substring(0, sql.length() - 2);
        // append the closing ")":
        sql += ")";
        
        return grtDatabase.rawQuery(sql, args);
    }
    */
    
    public Cursor getGRTTripID(String routeID, String[] serviceIdArr) {
        String sql = "SELECT trip_id FROM trips WHERE route_id = '" + routeID + "' AND service_id IN (";
        String valueList = "";
        int actualLength = 0;
        for (int i = 0; i < serviceIdArr.length; i++) {
            if (serviceIdArr[i] == null) {
                actualLength = i;
                break;
            }
        }
        
        for (int i = 0; i < actualLength; i++) {
            assert(serviceIdArr[i] != null);
            valueList += "'" + serviceIdArr[i] + "', ";
        }
        
        // remove the last ", " and append ")"
        if (valueList.length() >= 2) {
            valueList = valueList.substring(0, valueList.length() - 2);
        }
        // append the closing ")":
        sql += valueList + ")";
        
        return grtDatabase.rawQuery(sql, null);
    }
    
    public Cursor getGRTServiceID(String date_of_week, int date) {
        String sql = "SELECT * FROM calendar "
                + "WHERE " + date + " >= start_date AND " + date + " <= end_date AND " + date_of_week + " = 1";
        return grtDatabase.rawQuery(sql, null);
    }
    
    public Cursor getGRTRoutesBasedOnStopID(String stopID) {
        String sql = "SELECT DISTINCT _id, route_id, stop_id, route_short_name, route_long_name "
                + "FROM routes_from_stop "
                + "WHERE stop_id = " + stopID + " "
                + "ORDER BY route_short_name ASC";
        return grtDatabase.rawQuery(sql, null);
    }
    
    public Cursor searchGRTRoutes(String search) {
        String sql = "SELECT " + ROUTES_ROWID + ", " + ROUTES_SHORT_NAME + ", " + ROUTES_LONG_NAME + ", " + ROUTES_ID + " "
                + "FROM " + ROUTES + " "
                + "WHERE route_id LIKE '%" + search + "%' OR route_long_name LIKE '%" + search + "%' "
                + "ORDER BY " + ROUTES_SHORT_NAME + " ASC";
        return grtDatabase.rawQuery(sql, null);
    }

    public Cursor getTripHeadsignFromTripsBasedOnRowID(long rowID) {
        String sql = "SELECT trip_headsign FROM trips WHERE _id = " + rowID;
        return grtDatabase.rawQuery(sql, null);
    }
    
    public Cursor getGRTDirections(String routeID) {
        String table = "stop_times_" + routeID;
        String sql = "SELECT DISTINCT trip_headsign FROM trips WHERE route_id = '" + routeID + "'";
        return grtDatabase.rawQuery(sql, null);
    }
    
    public Cursor getGRTRoutes() {
        String sql = "SELECT " + ROUTES_ROWID + ", " + ROUTES_SHORT_NAME + ", " + ROUTES_LONG_NAME + ", " + ROUTES_ID + " "
                + "FROM " + ROUTES + " "
                + "ORDER BY " + ROUTES_SHORT_NAME + " ASC";
        return grtDatabase.rawQuery(sql, null);
    }
    
    public Cursor getGRTStops() {
        String sql = 
                "SELECT " + STOPS_ROWID + ", " + STOPS_ID + ", " + STOPS_LAT + ", " + STOPS_LON + ", " + STOPS_NAME + " "
                + "FROM " + STOPS;
        return grtDatabase.rawQuery(sql, null);
    }
    
    public String getNameOfTheFirstStopOfATrip(int tripID, String routeID) {
        String stopName = null;
        String table = "stop_times_" + routeID;
        String sql = "SELECT stop_name FROM stops WHERE stop_id = "
                + "(SELECT stop_id FROM " + table + " WHERE trip_id = " + tripID + " AND stop_sequence = 1)";
        Cursor c = grtDatabase.rawQuery(sql, null);
        if (c.moveToFirst()) {
            stopName = c.getString(c.getColumnIndexOrThrow(NewDatabaseAdapter.STOPS_NAME));
        }
        c.close();
        return stopName;
    }
    public String getNameOfTheLastStopOfATrip(int tripID, String routeID) {
        String stopName = null;
        String table = "stop_times_" + routeID;
        String sql = "SELECT stop_name FROM stops WHERE stop_id = "
                + "(SELECT stop_id FROM " + table + " WHERE trip_id = " + tripID + " ORDER BY stop_sequence DESC LIMIT 1 OFFSET 0)";
        Cursor c = grtDatabase.rawQuery(sql, null);
        if (c.moveToFirst()) {
            stopName = c.getString(c.getColumnIndexOrThrow(NewDatabaseAdapter.STOPS_NAME));
        }
        c.close();
        return stopName;
    }
    
    public Cursor getLongestStopListOfADirection(String routeID, String headsign) {
        assert(routeID != null);
        assert(headsign != null);
        String table = "stop_times_" + routeID;
        /*
         * String sql = "SELECT DISTINCT t1.trip_id AS trip_id, stopCount FROM "
            + "(SELECT DISTINCT trip_id FROM trips WHERE route_id = '" + routeID + "') AS t1, "
            + "(SELECT DISTINCT trip_id, stop_sequence, COUNT(*) AS stopCount FROM " + table 
                + " WHERE trip_id IN (SELECT DISTINCT trip_id FROM " + table + " WHERE stop_id = " + stopID + ") "
                + "GROUP BY trip_id "
                + "ORDER BY stopCount DESC "
                + "LIMIT 1 OFFSET 0) " + "AS t2 "
            + "WHERE t1.trip_id = t2.trip_id ";
         */
        
        String sql = "SELECT DISTINCT trip_id, stop_sequence, COUNT(*) AS stopCount "
                + "FROM " + table + " "
                + "WHERE trip_id IN (SELECT DISTINCT trip_id FROM trips WHERE route_id = '" + routeID + "' AND trip_headsign = '" + headsign + "') "
                + "GROUP BY trip_id "
                + "ORDER BY stopCount DESC "
                + "LIMIT 1 OFFSET 0 ";
        return grtDatabase.rawQuery(sql, null);
    }
    
    public Cursor getGRTStopList(String routeID, long tripID) {
        assert(routeID != null);
        String tableName = "stop_times_" + routeID;
        String sql = "SELECT " + tableName + "._id, " + tableName + ".trip_id, " + tableName + ".stop_sequence, stops.stop_id, stops.stop_name "
                + "FROM " + tableName + ", stops "
                + "WHERE stops.stop_id = " + tableName + ".stop_id AND " + tableName + ".trip_id = " + tripID + " "
                + "ORDER BY " + tableName + ".stop_sequence ASC ";
        return grtDatabase.rawQuery(sql, null);
    }
    
    /*
    public Cursor getGRTTripIDBasedOnRouteAndStopID(String routeID, String stopID) {
        String table = "stop_times_" + routeID;
        String sql = "select distinct t1.trip_id as trip_id, stop_sequence from "
                + "(select distinct trip_id from trips where route_id = " + routeID + ") as t1, "
                + "(select distinct trip_id, stop_id, stop_sequence from " + table + " where stop_id = " + stopID + ") as t2 "
                + "where t1.trip_id = t2.trip_id "
                + "order by stop_sequence desc "
                + "limit 1 offset 0";
        
        return grtDatabase.rawQuery(sql, null);
    }
    */
    /*
    public Cursor getGRTTripIDBasedOnRouteAndStopID(String routeID, String stopID) {
        String table = "stop_times_" + routeID;
        String sql = "SELECT DISTINCT t1.trip_id AS trip_id, stop_sequence, t2.stop_id AS stop_id, COUNT(*) AS count FROM "
                + "(SELECT DISTINCT trip_id FROM trips WHERE route_id = '" + routeID + "') AS t1, "
                + "(SELECT DISTINCT trip_id, stop_sequence, stop_id FROM " + table + " WHERE trip_id IN (SELECT DISTINCT trip_id FROM " 
                + table + " WHERE stop_id = " + stopID + ") ) AS t2 "
                + "WHERE t1.trip_id = t2.trip_id "
                + "GROUP BY trip_id "
                + "ORDER BY count DESC "
                + "LIMIT 1 OFFSET 0";
        return grtDatabase.rawQuery(sql, null);
    }
    */
    public Cursor getGRTTripIDBasedOnRouteAndStopID(String routeID, String stopID) {
        String table = "stop_times_" + routeID;
        String sql = "SELECT DISTINCT t1.trip_id AS trip_id, stopCount FROM "
                + "(SELECT DISTINCT trip_id FROM trips WHERE route_id = '" + routeID + "') AS t1, "
                + "(SELECT DISTINCT trip_id, stop_sequence, COUNT(*) AS stopCount FROM " + table 
                    + " WHERE trip_id IN (SELECT DISTINCT trip_id FROM " + table + " WHERE stop_id = " + stopID + ") "
                    + "GROUP BY trip_id "
                    + "ORDER BY stopCount DESC "
                    + "LIMIT 1 OFFSET 0) " + "AS t2 "
                + "WHERE t1.trip_id = t2.trip_id ";
                //+ "ORDER BY stopCount DESC "
                //+ "LIMIT 1 OFFSET 0";
        return grtDatabase.rawQuery(sql, null);
    }
    
    public Cursor getGRTRouteIDFromRoutes_From_Stop(long rowID) {
        String sql = "SELECT route_id FROM routes_from_stop WHERE _id = " + rowID;
        return grtDatabase.rawQuery(sql, null);
    }
    
    public Cursor getGRTRouteIDFromRoutesBasedOnRowID(long rowID) {
        String sql = "SELECT route_id FROM routes WHERE _id = " + rowID;
        return grtDatabase.rawQuery(sql, null);
    }
    
    public Cursor getOneGRTTripID(String routeID, long dontStartWithStopID) {
        String sql = null;
        String table = "stop_times_" + routeID;
        if (dontStartWithStopID == -1) {
            /*
             * String sql = "SELECT DISTINCT t1.trip_id AS trip_id, stopCount FROM "
                + "(SELECT DISTINCT trip_id FROM trips WHERE route_id = '" + routeID + "') AS t1, "
                + "(SELECT DISTINCT trip_id, stop_sequence, COUNT(*) AS stopCount FROM " + table 
                    + " WHERE trip_id IN (SELECT DISTINCT trip_id FROM " + table + " WHERE stop_id = " + stopID + ") "
                    + "GROUP BY trip_id "
                    + "ORDER BY stopCount DESC "
                    + "LIMIT 1 OFFSET 0) " + "AS t2 "
                + "WHERE t1.trip_id = t2.trip_id ";
             */
            sql = "SELECT DISTINCT trip_id, stop_sequence, COUNT(*) AS stopCount FROM " + table + " "
                    + "GROUP BY trip_id " 
                    + "ORDER BY stopCount DESC "
                    + "LIMIT 1 OFFSET 0 ";
        } else {
            sql = "SELECT DISTINCT trip_id, stop_id, stop_sequence, COUNT(*) AS stopCount FROM " + table + " "
                    + "WHERE trip_id NOT IN (SELECT trip_id FROM " + table + " WHERE stop_id = " + dontStartWithStopID + " AND stop_sequence = 1) "
                    + "GROUP BY trip_id " 
                    + "ORDER BY stopCount DESC "
                    + "LIMIT 1 OFFSET 0 ";
        }
        return grtDatabase.rawQuery(sql, null);
    }
    
    public Cursor getFirstStopIDOfATrip(String routeID, long tripID) {
        String table = "stop_times_" + routeID;
        String sql = "SELECT stop_id FROM " + table + " "
                + "WHERE trip_id = " + tripID + " "
                + "ORDER BY stop_sequence ASC "
                + "LIMIT 1 OFFSET 0 ";
        return grtDatabase.rawQuery(sql, null);
    }
    
    
    public Cursor getGRTRouteShortName(String routeID) {
        String sql = "select route_short_name from routes where route_id = '" + routeID + "'";
        return grtDatabase.rawQuery(sql, null);
    }
    
    
    public Cursor getRouteInfoFromFavorites(long rowId) {
        String sql = "SELECT * FROM MySchedules WHERE _id = " + rowId;
        return database.rawQuery(sql, null);
    }
    
    public Cursor getMyShceuldes() {
        // sql must NOT be ';' terminated
        String sql = 
                "SELECT " + MY_SCHEDULES_ROWID + ", " + MY_SCHEDULES_ROUTE_ID + ", " + START_STOP_NAME + ", routeShortName, " + END_STOP_NAME 
                + " FROM " + MY_SCHEDULES
                + " ORDER BY routeShortName ASC";
        return database.rawQuery(sql, null);
    }
    
    public Cursor getMyFavoriteScheduleInfo(long _id) {
        String sql = "SELECT * FROM MySchedules WHERE _id = " + _id;
        return database.rawQuery(sql, null);
    }
    
    public void updateMySchedulesEntry(long _id, String routeID, String startStopID, String startStopName, 
            String endStopID, String endStopName, int routeShortName) {
        String sql = "UPDATE MySchedules SET routeID = '" + routeID + "', startStopID = '" + startStopID + "', startStopName = '" + startStopName 
                + "', endStopID = '" + endStopID + "', endStopName = '" + endStopName + "', routeShortName = " + routeShortName 
                + " WHERE _id = " + _id;
        database.execSQL(sql);
    }
    
    public long insertMySchedulesEntry(String routeID, String startStopID, String startStopName, 
            String endStopID, String endStopName, int routeShortName) {
        ContentValues values = createContentValuesForMySchedules(routeID, startStopID, startStopName, endStopID, endStopName, routeShortName);
        long ans = database.insert(MY_SCHEDULES, null, values);
        return ans;
    }
    
    public void deleteMyScheduleEntry(long _id) {
        String sql = "delete from MySchedules where _id = " + _id;
        database.execSQL(sql);
    }
    
    private ContentValues createContentValuesForMySchedules(String routeID, String startStopID, String startStopName, 
            String endStopID, String endStopName, int routeShortName) {
        ContentValues values = new ContentValues();
        values.put(MY_SCHEDULES_ROUTE_ID, routeID);
        values.put(START_STOP_ID, startStopID);
        values.put(START_STOP_NAME, startStopName);
        values.put(END_STOP_ID, endStopID);
        values.put(END_STOP_NAME, endStopName);
        values.put(MY_SCHEDULES_ROUTE_SHORT_NAME, routeShortName);
        return values;
    }
    
    
}


