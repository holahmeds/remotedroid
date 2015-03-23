package com.joshsera;

import android.test.ActivityInstrumentationTestCase2;

/**
 * This is a simple framework for a test of an Application.  See
 * {@link android.test.ApplicationTestCase ApplicationTestCase} for more information on
 * how to write and extend Application tests.
 * <p/>
 * To run this test, you can type:
 * adb shell am instrument -w \
 * -e class com.joshsera.RemoteDroidTest \
 * com.joshsera.tests/android.test.InstrumentationTestRunner
 */
public class RemoteDroidTest extends ActivityInstrumentationTestCase2<RemoteDroid> {

    public RemoteDroidTest() {
        super("com.joshsera", RemoteDroid.class);
    }

}
