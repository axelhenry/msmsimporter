package com.ahenry.msmsimporter;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by axel on 12/10/15.
 */
public class SettingsActivity extends PreferenceActivity{
//public class SettingsActivity  extends Activity{

    public static final String KEY_PREF_TAR_GZ = "pref_useTarGz";
    public static final String KEY_PREF_DEBUG = "pref_includeDebugFiles";

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

    }
}
