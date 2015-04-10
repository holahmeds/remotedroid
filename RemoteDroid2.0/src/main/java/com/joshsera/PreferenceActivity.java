package com.joshsera;

import android.os.Bundle;

/**
 * Created by ahmed on 07/04/15.
 */
public class PreferenceActivity extends android.preference.PreferenceActivity {
    public static final String TAP_TO_CLICK = "tapclick";
    public static final String TAP_TIME = "taptime";
    public static final String SENSITIVITY = "sensitivity";
    public static final String SCROLL_SENSITIVITY = "scrollSensitivity";
    public static final String SCROLL_INVERTED = "scrollInverted";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
