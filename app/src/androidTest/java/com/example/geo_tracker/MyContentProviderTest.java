package com.example.geo_tracker;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class MyContentProviderTest {

    @Test
    public void testPathsWithUri() {
        ContentResolver contentResolver = InstrumentationRegistry.getInstrumentation().getContext().getContentResolver();
        Uri pathsUri = Uri.parse("content://com.example.geo_tracker.database.provider/path_table");
        Cursor cursor = contentResolver.query(pathsUri, null, null, null, null);

        assertNotNull(cursor);
        if (cursor != null) {
            cursor.close();
        }
    }

    @Test
    public void testPathsWithIdUri() {
        ContentResolver contentResolver = InstrumentationRegistry.getInstrumentation().getContext().getContentResolver();
        Uri pathWithIdUri = Uri.parse("content://com.example.geo_tracker.database.provider/path_table/2");
        Cursor cursor = contentResolver.query(pathWithIdUri, null, null, null, null);
        assertNotNull(cursor);
        if (cursor != null) {
            cursor.close();
        }
    }

}
