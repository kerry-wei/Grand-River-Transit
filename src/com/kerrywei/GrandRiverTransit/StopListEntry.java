package com.kerrywei.GrandRiverTransit;

public class StopListEntry {
    String stopID;
    String stopName;
    boolean startStop;
    boolean endStop;
    
    public StopListEntry(String stopID, String stopName) {
        this.stopID = stopID;
        this.stopName = stopName;
        startStop = false;
        endStop = false;
    }
    
    public String getStopID() {
        return stopID;
    }
    
    public String getStopName() {
        return stopName;
    }
    
    public void setStopID(String newID) {
        stopID = newID;
    }
    
    public void setStopName(String newName) {
        stopName = newName;
    }
    
    public boolean isStartStop() {
        return startStop;
    }
    
    public boolean isEndStop() {
        return endStop;
    }
    
    public void setStartStop(boolean start) {
        this.startStop = start;
    }
    
    public void setEndStop(boolean end) {
        this.endStop = end;
    }
}

