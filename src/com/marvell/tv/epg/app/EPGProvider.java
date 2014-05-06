/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.marvell.tv.epg.app;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Handler;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

/**
 * EPG content provider. See {@link com.marvell.tv.epg.app.EPGStore} for details.
 */
public class EPGProvider extends ContentProvider {
    private static final String TAG = "EPGProvider";
    private Handler mHandler;
    private DatabaseHelper mDBHelper = null;
    private static final String EPG_DATABASE_NAME = "epg.db";
    public static final Uri CONTENT_URI = Uri.parse("content://"
            + EPGStore.AUTHORITY);
    private static final UriMatcher URI_MATCHER = new UriMatcher(
            UriMatcher.NO_MATCH);

    /**
     * URI matcher tags
     */
    private static final int EPG_EVENTS = 1;
    private static final int EPG_EVENTS_SINGLE_ROW = 2;

    private static final int EPG_CHANNEL_MAP = 3;
    private static final int EPG_CHANNEL_MAP_SINGLE_ROW = 4;

    private static final int EPG_FAVORITE_STREAMS = 5;
    private static final int EPG_FAVORITE_STREAMS_SINGLE_ROW = 6;

    private static final int EPG_SCHEDULED_EVENTS = 7;
    private static final int EPG_SCHEDULED_EVENTS_SINGLE_ROW = 8;

    private static final int EPG_WATCHED_EVENTS = 9;
    private static final int EPG_WATCHED_EVENTS_SINGLE_ROW = 10;

    private static final int EPG_WATCHED_CHANNELS = 11;
    private static final int EPG_WATCHED_CHANNELS_SINGLE_ROW = 12;

    private static final int EPG_MULTILINGUAL_MAPS = 13;
    private static final int EPG_MULTILINGUAL_MAPS_SINGLE_ROW = 14;

    private static final int EPG_LOG = 15;

    static {
        URI_MATCHER.addURI(EPGStore.AUTHORITY, EPGStore.Events.URI_PATH,
                EPG_EVENTS);
        URI_MATCHER.addURI(EPGStore.AUTHORITY, EPGStore.Events.URI_PATH + "/#",
                EPG_EVENTS_SINGLE_ROW);

        URI_MATCHER.addURI(EPGStore.AUTHORITY, EPGStore.ChannelMap.URI_PATH,
                EPG_CHANNEL_MAP);
        URI_MATCHER.addURI(EPGStore.AUTHORITY, EPGStore.ChannelMap.URI_PATH
                + "/#", EPG_CHANNEL_MAP_SINGLE_ROW);

        URI_MATCHER.addURI(EPGStore.AUTHORITY,
                EPGStore.FavoriteStreams.URI_PATH, EPG_FAVORITE_STREAMS);
        URI_MATCHER.addURI(EPGStore.AUTHORITY,
                EPGStore.FavoriteStreams.URI_PATH + "/#",
                EPG_FAVORITE_STREAMS_SINGLE_ROW);

        URI_MATCHER.addURI(EPGStore.AUTHORITY,
                EPGStore.ScheduledEvents.URI_PATH, EPG_SCHEDULED_EVENTS);
        URI_MATCHER.addURI(EPGStore.AUTHORITY,
                EPGStore.ScheduledEvents.URI_PATH + "/#",
                EPG_SCHEDULED_EVENTS_SINGLE_ROW);

        URI_MATCHER.addURI(EPGStore.AUTHORITY, EPGStore.WatchedEvents.URI_PATH,
                EPG_WATCHED_EVENTS);
        URI_MATCHER.addURI(EPGStore.AUTHORITY, EPGStore.WatchedEvents.URI_PATH
                + "/#", EPG_WATCHED_EVENTS_SINGLE_ROW);

        URI_MATCHER.addURI(EPGStore.AUTHORITY, EPGStore.WatchedChannels.URI_PATH,
                EPG_WATCHED_CHANNELS);
        URI_MATCHER.addURI(EPGStore.AUTHORITY, EPGStore.WatchedChannels.URI_PATH
                + "/#", EPG_WATCHED_CHANNELS_SINGLE_ROW);

        URI_MATCHER.addURI(EPGStore.AUTHORITY,
                EPGStore.MultilingualMaps.URI_PATH, EPG_MULTILINGUAL_MAPS);
        URI_MATCHER.addURI(EPGStore.AUTHORITY,
                EPGStore.MultilingualMaps.URI_PATH + "/#",
                EPG_MULTILINGUAL_MAPS_SINGLE_ROW);

        URI_MATCHER.addURI(EPGStore.AUTHORITY, EPGStore.EPGLog.URI_PATH, EPG_LOG);
    }

    private static final class DatabaseHelper extends SQLiteOpenHelper {
        final Context mContext;
        final String mName;
        boolean mUpgradeAttempted; // Used for upgrade error handling

        public DatabaseHelper(Context context, String name) {
            super(context, name, null, getDatabaseVersion(context));
            mContext = context;
            mName = name;
            setWriteAheadLoggingEnabled(true);
        }

        @Override
        public void onConfigure(SQLiteDatabase db) {
            // TODO Auto-generated method stub
            super.onConfigure(db);
            db.setForeignKeyConstraintsEnabled(true);
        }

        /**
         * Creates database the first time we try to open it.
         */
        @Override
        public void onCreate(final SQLiteDatabase db) {
            updateDatabase(mContext, db, 0, getDatabaseVersion(mContext));
        }

        /**
         * Updates the database format when a new content provider is used with
         * an older database format.
         */
        @Override
        public void onUpgrade(final SQLiteDatabase db, final int oldV,
                final int newV) {
            mUpgradeAttempted = true;
            updateDatabase(mContext, db, oldV, newV);
        }

        @Override
        public synchronized SQLiteDatabase getWritableDatabase() {
            SQLiteDatabase result = null;
            mUpgradeAttempted = false;
            try {
                result = super.getWritableDatabase();
            } catch (Exception e) {
                if (!mUpgradeAttempted) {
                    Log.e(TAG, "failed to open database " + mName, e);
                    return null;
                }
            }

            // If we failed to open the database during an upgrade, delete the
            // file and try again.
            // This will result in the creation of a fresh database, which will
            // be repopulated
            // when the media scanner runs.
            if (result == null && mUpgradeAttempted) {
                mContext.getDatabasePath(mName).delete();
                result = super.getWritableDatabase();
            }
            return result;
        }

        public static int getDatabaseVersion(Context context) {
            try {
                return context.getPackageManager().getPackageInfo(
                        context.getPackageName(), 0).versionCode;
            } catch (NameNotFoundException e) {
                throw new RuntimeException("couldn't get version code for "
                        + context);
            }
        }

        private static final String PLAYLIST_COLUMNS = "_data,name,date_added,date_modified";

        private static void dropTable(SQLiteDatabase db, final String name) {
            if (db != null) {
                db.execSQL("DROP TABLE IF EXISTS " + name);
            }
        }

        private static void dropTrigger(SQLiteDatabase db, final String name) {
            if (db != null) {
                db.execSQL("DROP TRIGGER IF EXISTS " + name);
            }
        }

        private static void dropView(SQLiteDatabase db, final String name) {
            if (db != null) {
                db.execSQL("DROP VIEW IF EXISTS " + name);
            }
        }

        private static void createTable(SQLiteDatabase db, final String name,
                final String structure) {
            if (db != null) {
                db.execSQL("CREATE TABLE IF NOT EXISTS " + name + " "
                        + structure);
            }
        }

        private static void createIndex(SQLiteDatabase db, final String name,
                final String targetTable, final String targetColumn) {
            if (db != null) {
                db.execSQL("CREATE INDEX IF NOT EXISTS " + name + " on "
                        + targetTable + "(" + targetColumn + ");");
            }
        }

        /**
         * This method takes care of updating all the tables in the database to
         * the current version, creating them if necessary.
         * @param db
         *            Database
         */
        private static void updateDatabase(Context context, SQLiteDatabase db,
                int fromVersion, int toVersion) {

            // sanity checks
            int dbversion = getDatabaseVersion(context);
            if (toVersion != dbversion) {
                Log.e(TAG, "Illegal update request. Got " + toVersion
                        + ", expected " + dbversion);
                throw new IllegalArgumentException();
            } else if (fromVersion > toVersion) {
                Log.e(TAG, "Illegal update request: can't downgrade from "
                        + fromVersion + " to " + toVersion
                        + ". Did you forget to wipe data?");
                throw new IllegalArgumentException();
            }
            long startTime = System.currentTimeMillis();

            if (fromVersion < toVersion) {
                // Drop everything and start over.
                Log.i(TAG, "Upgrading EPG database from version "
                        + fromVersion + " to " + toVersion
                        + ", which will destroy all old data");
                dropTable(db, EPGStore.Events.TABLE);
                dropTable(db, EPGStore.ChannelMap.TABLE);
                dropTable(db, EPGStore.FavoriteStreams.TABLE);
                dropTable(db, EPGStore.ScheduledEvents.TABLE);
                dropTable(db, EPGStore.WatchedEvents.TABLE);
                dropTable(db, EPGStore.WatchedChannels.TABLE);
                dropTable(db, EPGStore.MultilingualMaps.TABLE);
                dropTable(db, EPGStore.EPGLog.TABLE);
            }

            // epg related tables
            createTable(db, EPGStore.Events.TABLE, EPGStore.Events.STRUCTURE);
            createTable(db, EPGStore.ChannelMap.TABLE,
                    EPGStore.ChannelMap.STRUCTURE);
            Log.d(TAG, "faywong table channel_maps created!");
            createTable(db, EPGStore.FavoriteStreams.TABLE,
                    EPGStore.FavoriteStreams.STRUCTURE);
            createTable(db, EPGStore.ScheduledEvents.TABLE,
                    EPGStore.ScheduledEvents.STRUCTURE);
            createTable(db, EPGStore.WatchedEvents.TABLE,
                    EPGStore.WatchedEvents.STRUCTURE);
            createTable(db, EPGStore.WatchedChannels.TABLE,
                    EPGStore.WatchedChannels.STRUCTURE);
            createTable(db, EPGStore.MultilingualMaps.TABLE,
                    EPGStore.MultilingualMaps.STRUCTURE);

            // log table for debug
            createTable(db, EPGStore.EPGLog.TABLE,
                    EPGStore.EPGLog.STRUCTURE);

            createIndex(db, EPGStore.Events.INDEX_CHANNEL_NUMBER,
                    EPGStore.Events.TABLE,
                    EPGStore.EventsColumns.CHANNEL_NUMBER);

/*            // TODO: refine the trigger implement
            db.execSQL("CREATE TRIGGER IF NOT EXISTS epg_update_trigger UPDATE ON "
                    + EPGStore.Events.TABLE
                    + " "
                    + "BEGIN "
                    + "DELETE FROM thumbnails WHERE image_id = old._id;"
                    + "SELECT _DELETE_FILE(old._data);" + "END");*/

            sanityCheck(db, fromVersion);
            long elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000;
            logToDb(db, "Database upgraded from version " + fromVersion
                    + " to " + toVersion + " in " + elapsedSeconds + " seconds");
        }

        /**
         * Write a persistent diagnostic message to the log table.
         */
        static void logToDb(SQLiteDatabase db, String message) {
            db.execSQL(
                    "INSERT INTO log (time,message) VALUES (strftime('%Y-%m-%d %H:%M:%f','now'),?);",
                    new String[] { message });
            // delete all but the last 500 rows
            db.execSQL("DELETE FROM log WHERE rowid IN"
                    + " (SELECT rowid FROM log ORDER BY time DESC LIMIT 500,-1);");
        }

        /**
         * Perform a simple sanity check on the database. Currently this tests
         * whether all the device_id entries in events are unique
         */
        private static void sanityCheck(SQLiteDatabase db, int fromVersion) {
            Cursor c1 = db.query(EPGStore.Events.TABLE,
                    new String[] { "count(*)" }, null, null, null, null, null);
            Cursor c2 = db.query(EPGStore.Events.TABLE,
                    new String[] { "count(distinct "
                            + EPGStore.EventsColumns.DEVICE_ID + ")" }, null,
                    null, null, null, null);
            c1.moveToFirst();
            c2.moveToFirst();
            int num1 = c1.getInt(0);
            int num2 = c2.getInt(0);
            c1.close();
            c2.close();
            if (num1 != num2) {
                Log.e(TAG,
                        "events.device_id column is not unique while upgrading"
                                + " from schema " + fromVersion + " : " + num1
                                + "/" + num2);
                // Delete all audio_meta rows so they will be rebuilt by the
                // media scanner
                db.execSQL("DELETE FROM " + EPGStore.Events.TABLE + ";");
            }
        }
    }

    private SQLiteDatabase getDatabase(boolean writable) {
        if (mDBHelper == null) {
            mDBHelper = new DatabaseHelper(getContext(), EPG_DATABASE_NAME);
        }
        return writable ? mDBHelper.getWritableDatabase() : mDBHelper.getReadableDatabase();
    }

    @Override
    public boolean onCreate() {
/*        HandlerThread ht = new HandlerThread("epg thread",
                Process.THREAD_PRIORITY_BACKGROUND);
        ht.start();
        if (mHandler == null) {
            mHandler = new Handler(ht.getLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    if (msg.what > 0) {
                    }
                }
            };
        }*/
        getDatabase(true);
        return true;
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        // TODO Auto-generated method stub
        long rowId = 0;
        Uri newUri = null;
        int match = URI_MATCHER.match(uri);
        SQLiteDatabase db = getDatabase(true);
        if (db == null || match == -1)
            return null;
        switch (match) {
        case EPG_EVENTS:
            rowId = db.insert(EPGStore.Events.TABLE, null, initialValues);
            if (rowId > -1) {
                newUri = ContentUris.withAppendedId(EPGStore.Events.URI, rowId);
            }
            break;
        case EPG_CHANNEL_MAP:
            rowId = db.insert(EPGStore.ChannelMap.TABLE, null, initialValues);
            if (rowId > -1) {
                newUri = ContentUris.withAppendedId(EPGStore.ChannelMap.URI,
                        rowId);
            }
            break;
        case EPG_FAVORITE_STREAMS:
            rowId = db.insert(EPGStore.FavoriteStreams.TABLE, null,
                    initialValues);
            if (rowId > -1) {
                newUri = ContentUris.withAppendedId(
                        EPGStore.FavoriteStreams.URI, rowId);
            }
            break;
        case EPG_SCHEDULED_EVENTS:
            rowId = db.insert(EPGStore.ScheduledEvents.TABLE, null,
                    initialValues);
            if (rowId > -1) {
                newUri = ContentUris.withAppendedId(
                        EPGStore.ScheduledEvents.URI, rowId);
            }
            break;
        case EPG_WATCHED_EVENTS:
            rowId = db
                    .insert(EPGStore.WatchedEvents.TABLE, null, initialValues);
            if (rowId > -1) {
                newUri = ContentUris.withAppendedId(EPGStore.WatchedEvents.URI,
                        rowId);
            }
            break;
        case EPG_WATCHED_CHANNELS:
            rowId = db
                    .insert(EPGStore.WatchedChannels.TABLE, null, initialValues);
            if (rowId > -1) {
                newUri = ContentUris.withAppendedId(EPGStore.WatchedChannels.URI,
                        rowId);
            }
            break;
        case EPG_MULTILINGUAL_MAPS:
            rowId = db.insert(EPGStore.MultilingualMaps.TABLE, null,
                    initialValues);
            if (rowId > -1) {
                newUri = ContentUris.withAppendedId(
                        EPGStore.MultilingualMaps.URI, rowId);
            }
            break;
        case EPG_LOG:
            rowId = db.insert(EPGStore.EPGLog.TABLE, null,
                    initialValues);
            if (rowId > -1) {
                newUri = ContentUris.withAppendedId(
                        EPGStore.EPGLog.URI, rowId);
            }
            break;
        default:
            Log.e(TAG, "unexpected insert operation on URI: " + uri);
            break;
        }
        if (rowId > -1) {
            // Notify any observers of the change in the data set.
            getContext().getContentResolver().notifyChange(newUri, null);
        }
        return newUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // TODO Auto-generated method stub
        int match = URI_MATCHER.match(uri);
        Log.d(TAG, "delete() in, match:" + match);
        SQLiteDatabase db = getDatabase(true);
        if (db == null || match == -1)
            return 0;
        // If this is a row URI, limit the deletion to the specified row.
        switch (match) {
        case EPG_EVENTS_SINGLE_ROW:
        case EPG_CHANNEL_MAP_SINGLE_ROW:
        case EPG_FAVORITE_STREAMS_SINGLE_ROW:
        case EPG_SCHEDULED_EVENTS_SINGLE_ROW:
        case EPG_WATCHED_EVENTS_SINGLE_ROW:
        case EPG_MULTILINGUAL_MAPS_SINGLE_ROW:
            String rowID = uri.getPathSegments().get(1);
            Log.d(TAG, "query on table:" + uri.getPathSegments().get(0)
                    + " row:" + rowID);
            selection = BaseColumns._ID
                    + "="
                    + rowID
                    + (!TextUtils.isEmpty(selection) ? " AND (" + selection
                            + ')' : "");
        default:
            break;
        }

        // To return the number of deleted items you must specify a where
        // clause. To delete all rows and return a value pass in "1".
        if (selection == null)
            selection = "1";

        // Perform the deletion.
        int deleteCount = db.delete(uri.getPathSegments().get(0), selection,
                selectionArgs);

        // Notify any observers of the change in the data set.
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the number of deleted items.
        return deleteCount;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        // TODO Auto-generated method stub
        int match = URI_MATCHER.match(uri);
        Log.d(TAG, "query() in, match:" + match);
        SQLiteDatabase db = getDatabase(false);
        if (db == null || match == -1)
            return null;

        // Replace these with valid SQL statements if necessary.
        String groupBy = null;
        String having = null;
        String targetTable = uri.getPathSegments().get(0);
        // Use an SQLite Query Builder to simplify constructing the
        // database query.
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        // If this is a row query, limit the result set to the passed in row.
        switch (match) {
        case EPG_EVENTS_SINGLE_ROW:
        case EPG_CHANNEL_MAP_SINGLE_ROW:
        case EPG_FAVORITE_STREAMS_SINGLE_ROW:
        case EPG_SCHEDULED_EVENTS_SINGLE_ROW:
        case EPG_WATCHED_EVENTS_SINGLE_ROW:
        case EPG_WATCHED_CHANNELS_SINGLE_ROW:
        case EPG_MULTILINGUAL_MAPS_SINGLE_ROW:
            String rowID = uri.getPathSegments().get(1);
            Log.d(TAG, "query on table:" + targetTable
                    + " row:" + rowID);
            selection = BaseColumns._ID
                    + "="
                    + rowID
                    + (!TextUtils.isEmpty(selection) ? " AND (" + selection
                            + ')' : "");
            queryBuilder.appendWhere(BaseColumns._ID + "=" + rowID);
        case EPG_CHANNEL_MAP:
            Log.d(TAG, "case EPG_CHANNEL_MAP, query on table:" + targetTable);
            String extraTableString = " LEFT JOIN " + EPGStore.WatchedChannels.TABLE + " ON " + targetTable + "."+ EPGStore.ChannelMapColumns.CHANNEL_NUMBER +" = " + EPGStore.WatchedChannels.TABLE + "." + EPGStore.WatchedChannelsColumns.CHANNEL_NUMBER;
            targetTable +=  extraTableString;
            Log.d(TAG, "after adding extraTableString, targetTable:" + targetTable);
        default:
            break;
        }

        // Specify the table on which to perform the query. This can
        // be a specific table or a join as required.
        queryBuilder.setTables(targetTable);

        Log.d(TAG, "step 1");
        // Execute the query.
        Cursor cursor = queryBuilder.query(db, projection, selection,
                selectionArgs, groupBy, having, sortOrder);

        Log.d(TAG, "step 2");

        // Return the result Cursor.
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        // TODO Auto-generated method stub
        int match = URI_MATCHER.match(uri);
        Log.d(TAG, "update() in, match:" + match);
        SQLiteDatabase db = getDatabase(true);
        if (db == null || match == -1)
            return 0;
        switch (match) {
        case EPG_EVENTS_SINGLE_ROW:
        case EPG_CHANNEL_MAP_SINGLE_ROW:
        case EPG_FAVORITE_STREAMS_SINGLE_ROW:
        case EPG_SCHEDULED_EVENTS_SINGLE_ROW:
        case EPG_WATCHED_EVENTS_SINGLE_ROW:
        case EPG_MULTILINGUAL_MAPS_SINGLE_ROW:
            String rowID = uri.getPathSegments().get(1);
            Log.d(TAG, "query on table:" + uri.getPathSegments().get(0)
                    + " row:" + rowID);
            selection = BaseColumns._ID
                    + "="
                    + rowID
                    + (!TextUtils.isEmpty(selection) ? " AND (" + selection
                            + ')' : "");
        default:
            break;
        }
        // Perform the update.
        int updateCount = db.update(uri.getPathSegments().get(0), values,
                selection, selectionArgs);
        // Notify any observers of the change in the data set.
        getContext().getContentResolver().notifyChange(uri, null);
        return updateCount;
    }

    @Override
    public String getType(Uri uri) {
        // TODO Auto-generated method stub
        switch (URI_MATCHER.match(uri)) {
        case EPG_EVENTS_SINGLE_ROW:
        case EPG_CHANNEL_MAP_SINGLE_ROW:
        case EPG_FAVORITE_STREAMS_SINGLE_ROW:
        case EPG_SCHEDULED_EVENTS_SINGLE_ROW:
        case EPG_WATCHED_EVENTS_SINGLE_ROW:
        case EPG_MULTILINGUAL_MAPS_SINGLE_ROW:
            return "vnd.android.cursor.item/vnd.marvell.tv.provider.epg";
        case EPG_EVENTS:
        case EPG_CHANNEL_MAP:
        case EPG_FAVORITE_STREAMS:
        case EPG_SCHEDULED_EVENTS:
        case EPG_WATCHED_EVENTS:
        case EPG_MULTILINGUAL_MAPS:
            return "vnd.android.cursor.dir/vnd.marvell.tv.provider.epg";

        default:
            throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }
}
