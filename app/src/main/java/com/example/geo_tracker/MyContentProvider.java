package com.example.geo_tracker;

import android.app.Application;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.geo_tracker.database.path.PathDao;
import com.example.geo_tracker.database.path.PathDatabase;
import com.example.geo_tracker.repository.MarkRepository;
import com.example.geo_tracker.repository.PathRepository;

//https://developer.android.com/guide/topics/providers/content-provider-creating
public class MyContentProvider extends ContentProvider {
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    public static final String CONTENT_AUTHORITY = "com.example.geo_tracker.database.provider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY + "/path_table");

    private static final int PATHS = 1;
    private static final int PATH_WITH_ID = 2;
    @Override
    public boolean onCreate() {
        return true;
    }

    static {
        uriMatcher.addURI("com.example.geo_tracker.provider", "pathtable", PATHS);
        uriMatcher.addURI("com.example.geo_tracker.provider", "pathtable/#", PATH_WITH_ID);
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        final int code = uriMatcher.match(uri);
        if(code == PATHS || code == PATH_WITH_ID) {
            final Context context = getContext();
            if (context == null) {
                return null;
            }
            SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
            queryBuilder.setTables("path_table");
            String query = queryBuilder.buildQuery(projection, selection,
                    null, null, sortOrder, null);
            final Cursor cursor = PathDatabase.getDatabase(context).getOpenHelper()
                    .getWritableDatabase()
                    .query(query, selectionArgs);
            cursor.setNotificationUri(context.getContentResolver(), uri);
            return cursor;
        }
        else {
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }
    public MyContentProvider() {
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (uriMatcher.match(uri)) {
            case PATHS:
                return "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + ".path_table";
            case PATH_WITH_ID:
                return "vnd.android.cursor.item/" + CONTENT_AUTHORITY + ".path_table";
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException("Not yet implemented");
    }




}