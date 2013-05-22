package com.kerrywei.GrandRiverTransit;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.KeyEvent;

public class AppSettings extends PreferenceActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        setResult(RESULT_OK);
        finish();
        return super.onKeyDown(keyCode, event);
    }
    
}


