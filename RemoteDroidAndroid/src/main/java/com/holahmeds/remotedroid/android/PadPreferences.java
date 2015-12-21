package com.holahmeds.remotedroid.android;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class PadPreferences extends PreferenceActivity {
    public static final String TAP_TO_CLICK = "tapclick";
    public static final String TAP_TIME = "taptime";
    public static final String SENSITIVITY = "sensitivity";
    public static final String SCROLL_SENSITIVITY = "scrollSensitivity";
    public static final String SCROLL_INVERTED = "scrollInverted";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
