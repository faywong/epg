package com.marvell.tv.epg.app;

import java.util.ArrayList;
import java.util.HashSet;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import com.marvell.tv.epg.EventDescriptor;

public class EPGStore {
    /** The authority of the EPG provider. */
    public static final String AUTHORITY = "com.marvell.tv.provider.epg";
    private static final String TAG = "EPGStore";
    private static final String DEFAULT_CONTENT_CATEGORY = "全部";
    private static final String MAX_END_TIME_COL_NAME = "max_end_time";

    public static long getLatestEventEndTime(ContentResolver resolver) {
        long endTime = -1;
        if (resolver == null) {
            return endTime;
        }
        String[] selection = new String[] { "MAX(" + EventsColumns.END_TIME + ") AS " + MAX_END_TIME_COL_NAME };
        Cursor cursor = resolver.query(EPGStore.Events.URI, selection, null, null, null);
        try {
            while (cursor.moveToNext()) {
                int columnInx = cursor.getColumnIndex(MAX_END_TIME_COL_NAME);
                endTime = cursor.getLong(columnInx);
                Log.d(TAG, "endTime:" + endTime);
            }
        } finally {
            cursor.close();
        }
        return endTime;
    }

    public static ArrayList<EventDescriptor> getEvents(ContentResolver resolver, String selection, String[] selectionArgs) {
        if (resolver == null) {
            return null;
        }

        Cursor cursor = resolver.query(EPGStore.Events.URI, EPGStore.Events.PROJECTION, selection, selectionArgs, EPGStore.EventsColumns.START_TIME + " ASC");
        if (cursor == null) {
            return null;
        }
        ArrayList<EventDescriptor> events = new ArrayList<EventDescriptor>();
        try {
            while (cursor.moveToNext()) {
                int columnInx = cursor.getColumnIndex(EPGStore.EventsColumns.CHANNEL_NUMBER);
                String channelNum = cursor.getString(columnInx);
                columnInx = cursor.getColumnIndex(EPGStore.EventsColumns.TITLE);
                String title = cursor.getString(columnInx);
                columnInx = cursor.getColumnIndex(EPGStore.EventsColumns.START_TIME);
                long startTime = cursor.getLong(columnInx);
                columnInx = cursor.getColumnIndex(EPGStore.EventsColumns.END_TIME);
                long endTime = cursor.getLong(columnInx);
                columnInx = cursor.getColumnIndex(EPGStore.EventsColumns.DESCRIPTION);
                String desc = cursor.getString(columnInx);
                columnInx = cursor.getColumnIndex(EPGStore.EventsColumns.LONG_DESCRIPTION);
                String longDesc = cursor.getString(columnInx);
                columnInx = cursor.getColumnIndex(EPGStore.EventsColumns.THUMBNAIL_URL);
                String thumbNailUrl = cursor.getString(columnInx);
                columnInx = cursor.getColumnIndex(EPGStore.EventsColumns.DEVICE_ID);
                String deviceID = cursor.getString(columnInx);
                columnInx = cursor.getColumnIndex(EPGStore.EventsColumns.CONTENT_TYPE);
                String contentType = cursor.getString(columnInx);
                columnInx = cursor.getColumnIndex(EPGStore.EventsColumns.STREAM_TYPE);
                String streamType = cursor.getString(columnInx);
                columnInx = cursor.getColumnIndex(EPGStore.EventsColumns.IS_HD);
                boolean isHD = (cursor.getInt(columnInx) == 1);
                columnInx = cursor.getColumnIndex(EPGStore.EventsColumns.IS_FREE);
                boolean isFree = (cursor.getInt(columnInx) == 1);
                events.add(new EventDescriptor(new com.marvell.tv.livetv.Channel(channelNum), title, startTime, endTime, desc, longDesc, thumbNailUrl, deviceID, contentType, streamType, isHD, isFree));
            }
        } finally {
            cursor.close();
        }
        return events;
    }

    public static EventDescriptor getEvent(ContentResolver resolver, String title, String channelNum, String startTime, String endTime) {
        String selectionString = EPGStore.EventsColumns.TITLE + "=? AND " + EPGStore.EventsColumns.CHANNEL_NUMBER + "=? AND " + EPGStore.EventsColumns.START_TIME + "=? AND "  + EPGStore.EventsColumns.END_TIME + "=?";
        String[] selectionArgs = new String[] { title, channelNum, startTime, endTime };
        ArrayList<EventDescriptor> events = EPGStore.getEvents(resolver, selectionString, selectionArgs);
        if (events == null || events.size() == 0) {
            return null;
        } else {
            return events.get(0);
        }
    }

    public static EventDescriptor getEvent(ContentResolver resolver,
            String channelNum, long startTime) {
        // TODO Auto-generated method stub
        String selectionString = EPGStore.EventsColumns.CHANNEL_NUMBER + "=? AND " + EPGStore.EventsColumns.START_TIME + "=?";
        String[] selectionArgs = new String[] { channelNum, Long.toString(startTime) };
        ArrayList<EventDescriptor> events = EPGStore.getEvents(resolver, selectionString, selectionArgs);
        if (events == null || events.size() == 0) {
            return null;
        } else {
            return events.get(0);
        }
    }

    public static ArrayList<FancyChannel> getAllChannels(ContentResolver resolver) {
        if (resolver == null) {
            return null;
        }
        Cursor cursor = resolver.query(EPGStore.ChannelMap.URI, new String[] { ChannelMap.TABLE + "." + ChannelMapColumns.CHANNEL_NUMBER,
            ChannelMapColumns.SERVICE_ID,
            ChannelMapColumns.DISPLAY_CHANNEL_NAME,
            ChannelMapColumns.RESOURCE_URL }, null, null, EPGStore.WatchedChannelsColumns.WATCHED_TIMES + " DESC");
        Log.d(TAG, "check point 2");

        if (cursor == null) {
            return null;
        }
        Log.d(TAG, "check point 3");

        ArrayList<FancyChannel> channels = new ArrayList<FancyChannel>();
        try {
            while (cursor.moveToNext()) {
                int columnInx = cursor.getColumnIndex(EPGStore.ChannelMapColumns.CHANNEL_NUMBER);
                String channelNum = cursor.getString(columnInx);
                columnInx = cursor.getColumnIndex(EPGStore.ChannelMapColumns.DISPLAY_CHANNEL_NAME);
                String displayName = cursor.getString(columnInx);
                columnInx = cursor.getColumnIndex(EPGStore.ChannelMapColumns.SERVICE_ID);
                int serviceID = cursor.getInt(columnInx);
                columnInx = cursor.getColumnIndex(EPGStore.ChannelMapColumns.RESOURCE_URL);
                String resURL = cursor.getString(columnInx);
                channels.add(new FancyChannel(displayName, channelNum, serviceID, resURL));
            }
        } finally {
            cursor.close();
        }
        Log.d(TAG, "check point 4");

        return channels;
    }

    public static boolean insertEvent(ContentResolver resolver, EventDescriptor event) {
        if (resolver == null || event == null) {
            return false;
        }
        // Log.d(TAG, "insertEvent() in");
        ContentValues values = new ContentValues();

        values.put(EPGStore.EventsColumns.CHANNEL_NUMBER, event.getChannel().toDisplayString());
        values.put(EPGStore.EventsColumns.CONTENT_TYPE, event.getContentType());
        values.put(EPGStore.EventsColumns.TITLE, event.getTitle());
        values.put(EPGStore.EventsColumns.DESCRIPTION, event.getDescription());
        values.put(EPGStore.EventsColumns.LONG_DESCRIPTION, event.getLongDescription());
        values.put(EPGStore.EventsColumns.DEVICE_ID, event.getDeviceID());
        values.put(EPGStore.EventsColumns.DURATION, event.getDuration());
        values.put(EPGStore.EventsColumns.START_TIME, event.getStartTime());
        values.put(EPGStore.EventsColumns.END_TIME, event.getEndTime());
        values.put(EPGStore.EventsColumns.IS_FREE, event.isFree() ? 1 : 0);
        values.put(EPGStore.EventsColumns.IS_HD, event.isHD() ? 1 : 0);
        values.put(EPGStore.EventsColumns.STREAM_TYPE, event.getStreamType());
        values.put(EPGStore.EventsColumns.THUMBNAIL_URL, event.getThumbnailURL());
        return null != resolver.insert(EPGStore.Events.URI, values);
    }

    public static boolean insertWatchedEvent(ContentResolver resolver, EventDescriptor event) {
        if (resolver == null || event == null) {
            return false;
        }
        ContentValues values = new ContentValues();

        values.put(EPGStore.WatchedEventsColumns.EVENT_TITLE, event.getTitle());
        values.put(EPGStore.WatchedEventsColumns.CONTENT_TYPE, event.getContentType());
        values.put(EPGStore.WatchedEventsColumns.START_TIME, event.getStartTime());
        values.put(EPGStore.WatchedEventsColumns.END_TIME, event.getEndTime());
        values.put(EPGStore.WatchedEventsColumns.IS_FREE, event.isFree());
        values.put(EPGStore.WatchedEventsColumns.IS_HD, event.isHD());

        return null != resolver.insert(EPGStore.WatchedEvents.URI, values);
    }

    public static boolean logWatchedChannel(ContentResolver resolver, FancyChannel channel) {
        if (resolver == null || channel == null) {
            return false;
        }
        String channelNum = channel.channelNum;
        Log.d(TAG, "logWatchedChannel() in, channel: " + channelNum);
        String selectionString = EPGStore.WatchedChannelsColumns.CHANNEL_NUMBER + "=?";
        String[] selectionArgs = new String[] { channelNum };

        Cursor cursor = resolver.query(EPGStore.WatchedChannels.URI, EPGStore.WatchedChannels.PROJECTION, selectionString, selectionArgs, null);
        ContentValues values = new ContentValues();
        Log.d(TAG, "logWatchedChannel() cursor: " + cursor);
        if (cursor == null) {
            values.put(EPGStore.WatchedChannelsColumns.CHANNEL_NUMBER, channelNum);
            return null != resolver.insert(EPGStore.WatchedChannels.URI, values);
        } else {
            try {
                while (cursor.moveToNext()) {
                    values.put(EPGStore.WatchedChannelsColumns.CHANNEL_NUMBER, channelNum);
                    int columnInx = cursor.getColumnIndex(EPGStore.WatchedChannelsColumns.WATCHED_TIMES);
                    int oldPlayedTimes = cursor.getInt(columnInx);
                    Log.d(TAG, "logWatchedChannel() oldPlayedTimes: " + oldPlayedTimes);
                    values.put(EPGStore.WatchedChannelsColumns.WATCHED_TIMES, oldPlayedTimes + 1);
                    return 1 == resolver.update(EPGStore.WatchedChannels.URI, values, selectionString, selectionArgs);
                }
            } finally {
                cursor.close();
            }
        }
        return false;
    }

    public static boolean logWatchedChannel(ContentResolver resolver, String channelNum) {
        if (resolver == null || channelNum == null) {
            return false;
        }
        Log.d(TAG, "logWatchedChannel() in, channel: " + channelNum);
        String selectionString = EPGStore.WatchedChannelsColumns.CHANNEL_NUMBER + "=?";
        String[] selectionArgs = new String[] { channelNum };

        Cursor cursor = resolver.query(EPGStore.WatchedChannels.URI, EPGStore.WatchedChannels.PROJECTION, selectionString, selectionArgs, null);
        ContentValues values = new ContentValues();
        Log.d(TAG, "logWatchedChannel() cursor: " + cursor + " isAfterLast:" + cursor.isAfterLast() + " " + " isBeforeFirst:" + cursor.isBeforeFirst()
                 + " isFirst:" + cursor.isFirst() + " isLast:" + cursor.isLast());

        if (cursor != null && cursor.moveToNext()) {
            values.put(EPGStore.WatchedChannelsColumns.CHANNEL_NUMBER, channelNum);
            int columnInx = cursor.getColumnIndex(EPGStore.WatchedChannelsColumns.WATCHED_TIMES);
            int oldWatchedTimes = cursor.getInt(columnInx);
            Log.d(TAG, "logWatchedChannel() oldWatchedTimes: " + oldWatchedTimes);
            values.put(EPGStore.WatchedChannelsColumns.WATCHED_TIMES, oldWatchedTimes + 1);
            return 1 == resolver.update(EPGStore.WatchedChannels.URI, values, selectionString, selectionArgs);
        } else {
            values.put(EPGStore.WatchedChannelsColumns.CHANNEL_NUMBER, channelNum);
            return null != resolver.insert(EPGStore.WatchedChannels.URI, values);
        }
    }

    public static boolean insertScheduledEvent(ContentResolver resolver, String channelNum, long startTime, int scheType) {
        if (resolver == null) {
            return false;
        }
        ContentValues values = new ContentValues();

        values.put(EPGStore.ScheduledEventsColumns.CHANNEL_NUMBER, channelNum);
        values.put(EPGStore.ScheduledEventsColumns.SCHE_TYPE, scheType);
        values.put(EPGStore.ScheduledEventsColumns.IS_VALID, 1);
        values.put(EPGStore.ScheduledEventsColumns.CREATED_TIME, System.currentTimeMillis() / 1000);
        values.put(EPGStore.ScheduledEventsColumns.REMINDER_TIME, startTime);

        return null != resolver.insert(EPGStore.ScheduledEvents.URI, values);
    }

    public static ArrayList<ScheduledEvent> getScheduledEvents(ContentResolver resolver, boolean clearInvalid, String selectionString, String[] selectionArgs) {
        if (resolver == null) {
            return null;
        }
        Cursor cursor = resolver.query(EPGStore.ScheduledEvents.URI, EPGStore.ScheduledEvents.PROJECTION, selectionString, selectionArgs, EPGStore.ScheduledEventsColumns.REMINDER_TIME + " ASC");
        if (cursor == null) {
            return null;
        }
        ArrayList<ScheduledEvent> events = new ArrayList<ScheduledEvent>();
        ArrayList<ScheduledEvent> invalidEvents = clearInvalid ? new ArrayList<EPGStore.ScheduledEvent>() : null;
        try {
            while (cursor.moveToNext()) {
                int columnInx = cursor.getColumnIndex(EPGStore.ScheduledEventsColumns.CHANNEL_NUMBER);
                String channelNum = cursor.getString(columnInx);
                columnInx = cursor.getColumnIndex(EPGStore.ScheduledEventsColumns.REMINDER_TIME);
                long startTime = cursor.getInt(columnInx);
                columnInx = cursor.getColumnIndex(EPGStore.ScheduledEventsColumns.SCHE_TYPE);
                int scheType = cursor.getInt(columnInx);
                ScheduledEvent tmpEvent = new ScheduledEvent(channelNum, startTime, scheType);
                if (startTime <= System.currentTimeMillis() / 1000) {
                    if (clearInvalid) {
                        invalidEvents.add(tmpEvent);
                    }
                } else {
                    events.add(tmpEvent);
                }
            }
        } finally {
            cursor.close();
        }

        if (clearInvalid && invalidEvents != null) {
            // clear all invalid events
            for (ScheduledEvent event : invalidEvents) {
                removeScheduledEvent(resolver, event);
            }
        }
        return events;
    }

    public static boolean insertScheduledEvent(ContentResolver resolver, ScheduledEvent event) {
        if (resolver == null || event == null) {
            return false;
        }
        ContentValues values = new ContentValues();

        values.put(EPGStore.ScheduledEventsColumns.CHANNEL_NUMBER, event.channelNum);
        values.put(EPGStore.ScheduledEventsColumns.SCHE_TYPE, event.scheType);
        values.put(EPGStore.ScheduledEventsColumns.IS_VALID, event.valid ? 1 : 0);
        values.put(EPGStore.ScheduledEventsColumns.CREATED_TIME, System.currentTimeMillis() / 1000);
        values.put(EPGStore.ScheduledEventsColumns.REMINDER_TIME, event.startTime);

        return null != resolver.insert(EPGStore.ScheduledEvents.URI, values);
    }

    public static boolean insertChannel(ContentResolver resolver, FancyChannel channel) {
        if (resolver == null || channel == null) {
            return false;
        }
        // Log.d(TAG, "insertChannel() in");
        ContentValues values = new ContentValues();
        values.put(EPGStore.ChannelMapColumns.CHANNEL_NUMBER, channel.channelNum);
        values.put(EPGStore.ChannelMapColumns.DISPLAY_CHANNEL_NAME, channel.displayName);
        values.put(EPGStore.ChannelMapColumns.SERVICE_ID, channel.serviceID);
        values.put(EPGStore.ChannelMapColumns.RESOURCE_URL, channel.resURL);
        return null != resolver.insert(EPGStore.ChannelMap.URI, values);
    }

    public static String[] getContentTypes(ContentResolver resolver) {
        String[] contentTypes = new String[0];
        if (resolver == null) {
            return contentTypes;
        }
        String column = EPGStore.EventsColumns.CONTENT_TYPE;
        Cursor cursor = resolver.query(EPGStore.Events.URI, new String[] { "DISTINCT " + column }, column + " IS NOT NULL) GROUP BY (" + column, null, null);
        if (cursor == null) {
            return contentTypes;
        }
        HashSet<String> tmpContentTypes = new HashSet<String>();
        try {
            while (cursor.moveToNext()) {
                int columnInx = cursor.getColumnIndex(EPGStore.EventsColumns.CONTENT_TYPE);
                String contentType = cursor.getString(columnInx);
                tmpContentTypes.add(contentType);
            }
        } finally {
            cursor.close();
        }
        int size = tmpContentTypes.size() + 1;
        contentTypes = new String[size];
        int i = 0;
        contentTypes[i++] = DEFAULT_CONTENT_CATEGORY;
        for (String item : tmpContentTypes.toArray(new String[0])) {
            contentTypes[i++] = item;
        }
        return contentTypes;
    }

    public static boolean removeScheduledEvent(ContentResolver resolver, ScheduledEvent event) {
        if (resolver == null || event == null) {
            return false;
        }
        String selectionString = EPGStore.ScheduledEventsColumns.CHANNEL_NUMBER + "=? AND " + EPGStore.ScheduledEventsColumns.REMINDER_TIME + "=? AND " + EPGStore.ScheduledEventsColumns.SCHE_TYPE + "=?";
        String[] selectionArgs = new String[] { event.channelNum, Long.toString(event.startTime), Integer.toString(event.scheType) };

        int num = resolver.delete(EPGStore.ScheduledEvents.URI, selectionString, selectionArgs);
        Log.d(TAG, "deleted " + num + " ScheduledEvent records!");

        return true;
    }

    public static boolean removeScheduledEvent(ContentResolver resolver, String channelNum, long startTime, int scheType) {
        if (resolver == null) {
            return false;
        }
        String selectionString = EPGStore.ScheduledEventsColumns.CHANNEL_NUMBER + "=? AND " + EPGStore.ScheduledEventsColumns.REMINDER_TIME + "=? AND " + EPGStore.ScheduledEventsColumns.SCHE_TYPE + "=?";
        String[] selectionArgs = new String[] { channelNum, Long.toString(startTime), Integer.toString(scheType) };

        int num = resolver.delete(EPGStore.ScheduledEvents.URI, selectionString, selectionArgs);
        Log.d(TAG, "deleted " + num + " ScheduledEvent records!");

        return true;
    }

    public static boolean emptyAllEvents(ContentResolver resolver) {
        if (resolver == null) {
            return false;
        }
        int num = resolver.delete(EPGStore.Events.URI, null, null);
        // Log.d(TAG, "deleted " + num + " event records!");
        return true;
    }

    public static boolean emptyAllLogs(ContentResolver resolver) {
        if (resolver == null) {
            return false;
        }
        int num = resolver.delete(EPGStore.EPGLog.URI, null, null);
        // Log.d(TAG, "deleted " + num + " log records!");
        return true;
    }

    public static boolean emptyAllChannels(ContentResolver resolver) {
        if (resolver == null) {
            return false;
        }
        int num = resolver.delete(EPGStore.ChannelMap.URI, null, null);
        // Log.d(TAG, "deleted " + num + " channel records!");
        return true;
    }


    public static void logToDB(ContentResolver resolver, String msg) {
        if (resolver == null) {
            return;
        }
        ContentValues values = new ContentValues();
        values.put(EPGStore.EPGLogColumns.MSG, msg);
        resolver.insert(EPGStore.EPGLog.URI, values);
    }

    public static class FancyChannel {
        public String displayName;
        public String channelNum; /* logical channel number */
        public int serviceID;
        public String resURL;

        public FancyChannel(String displayName, String channelNum, int serviceID, String resURL) {
            this.displayName = displayName;
            this.channelNum = channelNum;
            this.serviceID = serviceID;
            this.resURL = resURL;
        }

        public FancyChannel(String displayName, String channelNum, int serviceID) {
            this.displayName = displayName;
            this.channelNum = channelNum;
            this.serviceID = serviceID;
            this.resURL = "images/channel/陕西卫视65.jpg";
        }
    }

    public static class ScheduledEvent {
        public String channelNum;
        public long startTime; /* logical channel number */
        public int scheType;
        public boolean valid;

        public ScheduledEvent(String channelNum, long startTime, int scheType) {
            this(channelNum, startTime, scheType, true);
        }

        public ScheduledEvent(String channelNum, long startTime, int scheType, boolean valid) {
            this.channelNum = channelNum;
            this.startTime = startTime;
            this.scheType = scheType;
            this.valid = valid;
        }
    }

    // ------------------------------------------------------------------------
    // Events

    /**
     * Column and URI constants for the <tt>events</tt> table.
     */
    public static final class Events {
        private Events() {}

        /** The URI path for the <tt>events</tt> table. */
        public static final String URI_PATH = "events";
        public static final String TABLE = URI_PATH;

        /** The content URI for the <tt>events</tt> table. */
        public static final Uri URI = Uri.parse("content://" + AUTHORITY)
                .buildUpon()
                .appendPath(URI_PATH)
                .build();

        /**
         * The complete projection for the <tt>events</tt> table,
         * including all of its columns.
         */
        public static final String[] PROJECTION =
                new String[] { EventsColumns.CHANNEL_NUMBER,
                               EventsColumns.TITLE,
                               EventsColumns.START_TIME,
                               EventsColumns.END_TIME,
                               EventsColumns.DURATION,
                               EventsColumns.DESCRIPTION,
                               EventsColumns.LONG_DESCRIPTION,
                               EventsColumns.THUMBNAIL_URL,
                               EventsColumns.DEVICE_ID,
                               EventsColumns.CONTENT_TYPE,
                               EventsColumns.STREAM_TYPE,
                               EventsColumns.IS_HD,
                               EventsColumns.IS_FREE,
                               EventsColumns.CREATE_TIMESTAMP };

        /**
         * the table structure
         */
        public static final String STRUCTURE = "(" +
                "_id INTEGER PRIMARY KEY," +
                EventsColumns.CHANNEL_NUMBER + " TEXT," +
                EventsColumns.TITLE + " TEXT," +
                EventsColumns.START_TIME + " INTEGER," +
                EventsColumns.END_TIME + " INTEGER," +
                EventsColumns.DURATION + " INTEGER," +
                EventsColumns.DESCRIPTION + " TEXT," +
                EventsColumns.LONG_DESCRIPTION + " TEXT," +
                EventsColumns.THUMBNAIL_URL + " TEXT," +
                EventsColumns.DEVICE_ID + " TEXT," +
                EventsColumns.CONTENT_TYPE + " TEXT," +
                EventsColumns.STREAM_TYPE + " TEXT," +
                EventsColumns.IS_HD + " INTEGER," +
                EventsColumns.IS_FREE + " INTEGER," +
                EventsColumns.CREATE_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP" +
            ");";

        public static final String INDEX_CHANNEL_NUMBER = "channel_number_index";
    }

    /**
     * Columns for the <tt>events</tt> table.
     */
    public static final class EventsColumns implements BaseColumns {
        private EventsColumns() {}

        /**
         * The channel name. Maps to the column {@link MediaDevicesContract.StreamsColumns#CHANNEL_NUMBER} in
         * the <tt>streams</tt> table.
         * format: "major.minor" or "logical_channel_num"
         * example = "200.1", "200"
         */
        public static final String CHANNEL_NUMBER = "channel_number";

        /**
         * the title of event entity
         */
        public static final String TITLE = "title";

        /**
         * the start time of event entity in utc seconds
         */
        public static final String START_TIME = "start_time_utc_sec";

        /**
         * the end time of event entity in utc seconds
         */
        public static final String END_TIME = "end_time_utc_sec";

        /**
         * the duration of event entity in seconds
         */
        public static final String DURATION = "duration_sec";

        /**
         * the description of event entity
         */
        public static final String DESCRIPTION = "description";

        /**
         * the detailed description of event entity
         */
        public static final String LONG_DESCRIPTION = "long_description";

        /**
         * the thumbnail URL of event entity
         */
        public static final String THUMBNAIL_URL = "thumbnail_url";

        /**
         * the corresponding device ID of event entity,
         * Maps to the column {@link MediaDevicesContract.DevicesColumns#DEVICE_ID} in
         * the <tt>devices</tt> table.
         */
        public static final String DEVICE_ID = "device_id";

        /**
         * the content type, such as:
         * "Unknown", "Movie", "News", "Show", "Sports", "Children/Youth",
         * "Music", "Arts", "Social", "Education", "Leisure"
         */
        public static final String CONTENT_TYPE = "content_type";

        /**
         * stream type, such as:
         * "Video", "Audio", "Teletext"
         */
        public static final String STREAM_TYPE = "stream_type";

        /**
         * whether the stream is HD
         */
        public static final String IS_HD = "is_hd";

        /**
         * whether the stream is scrambled
         */
        public static final String IS_FREE = "is_free";

        /**
         * the timestamp of time this record is created,
         * NOTE: The format for CURRENT_TIMESTAMP is "YYYY-MM-DD HH:MM:SS"
         */
        public static final String CREATE_TIMESTAMP = "create_timestamp";
    }

    // ------------------------------------------------------------------------
    // ChannelMap

    /**
     * Column and URI constants for the <tt>channel_map</tt> table.
     */
    public static final class ChannelMap {
        private ChannelMap() {}

        /** The URI path for the <tt>channel_map</tt> table. */
        public static final String URI_PATH = "channel_maps";
        public static final String TABLE = URI_PATH;


        /** The content URI for the <tt>channel_map</tt> table. */
        public static final Uri URI = Uri.parse("content://" + AUTHORITY)
                .buildUpon()
                .appendPath(URI_PATH)
                .build();

        /**
         * The complete projection for the <tt>channel_map</tt> table,
         * including all of its columns.
         */
        public static final String[] PROJECTION =
                new String[] { ChannelMapColumns.CHANNEL_NUMBER,
                               ChannelMapColumns.SERVICE_ID,
                               ChannelMapColumns.DISPLAY_CHANNEL_NAME,
                               ChannelMapColumns.RESOURCE_URL };

        /**
         * the table structure
         */
        public static final String STRUCTURE = "(" +
                "_id INTEGER PRIMARY KEY," +
                ChannelMapColumns.CHANNEL_NUMBER + " TEXT," +
                ChannelMapColumns.SERVICE_ID + " INTEGER," +
                ChannelMapColumns.DISPLAY_CHANNEL_NAME + " TEXT," +
                ChannelMapColumns.RESOURCE_URL + " TEXT" +
            ");";
    }

    /**
     * Columns for the <tt>channel_map</tt> table.
     */
    public static final class ChannelMapColumns implements BaseColumns {
        private ChannelMapColumns() {}

        /**
         * The channel name. Maps to the column {@link MediaDevicesContract.StreamsColumns#CHANNEL_NUMBER} in
         * the <tt>events</tt> table.
         * format: "major.minor" or "logical_channel_num"
         * example = "200.1", "200"
         */
        public static final String CHANNEL_NUMBER = "channel_number";

        /**
         * the corresponding service ID
         */
        public static final String SERVICE_ID = "service_id";

        /**
         * the preferred channel name for display
         */
        public static final String DISPLAY_CHANNEL_NAME = "display_channel_name";

        /**
         * url for channel related resource
         */
        public static final String RESOURCE_URL = "resource_url";
    }

    // ------------------------------------------------------------------------
    // FavoriteStream

    /**
     * Column and URI constants for the <tt>favorite_streams</tt> table.
     */
    public static final class FavoriteStreams {
        private FavoriteStreams() {}

        /** The URI path for the <tt>channel_map</tt> table. */
        public static final String URI_PATH = "favorite_streams";
        public static final String TABLE = URI_PATH;

        /** The content URI for the <tt>favorite_streams</tt> table. */
        public static final Uri URI = Uri.parse("content://" + AUTHORITY)
                .buildUpon()
                .appendPath(URI_PATH)
                .build();

        /**
         * The complete projection for the <tt>favorite_streams</tt> table,
         * including all of its columns.
         */
        public static final String[] PROJECTION =
                new String[] { FavoriteStreamsColumns.STREAM_ID,
                               FavoriteStreamsColumns.CHANNEL_NUMBER,
                               FavoriteStreamsColumns.IS_FAVORITE,
                               FavoriteStreamsColumns.WATCHED_TIMES,
                               FavoriteStreamsColumns.WATCHED_HOURS };

        /**
         * the table structure
         */
        public static final String STRUCTURE = "(" +
                "_id INTEGER PRIMARY KEY," +
                FavoriteStreamsColumns.STREAM_ID + " INTEGER," +
                FavoriteStreamsColumns.CHANNEL_NUMBER + " TEXT," +
                FavoriteStreamsColumns.IS_FAVORITE + " INTEGER," +
                FavoriteStreamsColumns.WATCHED_TIMES + " INTEGER," +
                FavoriteStreamsColumns.WATCHED_HOURS + " REAL" +
            ");";
    }

    /**
     * Columns for the <tt>favorite_streams</tt> table.
     */
    public static final class FavoriteStreamsColumns implements BaseColumns {
        private FavoriteStreamsColumns() {}

        /**
         * The stream id. Maps to the column {@link MediaDevicesContract.StreamsColumns#_ID} in
         * the <tt>streams</tt> table.
         */
        public static final String STREAM_ID = "stream_id";

        /**
         * The channel name. Maps to the column {@link MediaDevicesContract.StreamsColumns#CHANNEL_NUMBER} in
         * the <tt>events</tt> table.
         * format: "major.minor" or "logical_channel_num"
         * example = "200.1", "200"
         */
        public static final String CHANNEL_NUMBER = "channel_number";

        /**
         * whether this stream is a favorite of user
         */
        public static final String IS_FAVORITE = "is_favorite";

        /**
         * times the user have watched this stream
         */
        public static final String WATCHED_TIMES = "watched_times";

        /**
         * hours the user have watched this stream
         */
        public static final String WATCHED_HOURS = "watched_hours";
    }

    // ------------------------------------------------------------------------
    // ScheduledEvents

    /**
     * Column and URI constants for the <tt>scheduled_events</tt> table.
     */
    public static final class ScheduledEvents {
        private ScheduledEvents() {}

        /** The URI path for the <tt>scheduled_events</tt> table. */
        public static final String URI_PATH = "scheduled_events";
        public static final String TABLE = URI_PATH;

        public static final int SCHE_TYPE_RECORD = 1;
        public static final int SCHE_TYPE_REMIND = 2;

        /** The content URI for the <tt>scheduled_events</tt> table. */
        public static final Uri URI = Uri.parse("content://" + AUTHORITY)
                .buildUpon()
                .appendPath(URI_PATH)
                .build();

        /**
         * The complete projection for the <tt>scheduled_events</tt> table,
         * including all of its columns.
         */
        public static final String[] PROJECTION =
                new String[] { ScheduledEventsColumns.CHANNEL_NUMBER,
                               ScheduledEventsColumns.SCHE_TYPE,
                               ScheduledEventsColumns.IS_VALID,
                               ScheduledEventsColumns.CREATED_TIME,
                               ScheduledEventsColumns.REMINDER_TIME };

        /**
         * the table structure
         */
        public static final String STRUCTURE = "(" +
                "_id INTEGER PRIMARY KEY," +
                ScheduledEventsColumns.CHANNEL_NUMBER + " TEXT," +
                ScheduledEventsColumns.SCHE_TYPE + " INTEGER," +
                ScheduledEventsColumns.IS_VALID + " INTEGER," +
                ScheduledEventsColumns.CREATED_TIME + " INTEGER," +
                ScheduledEventsColumns.REMINDER_TIME + " INTEGER" +
            ");";
    }

    /**
     * Columns for the <tt>scheduled_events</tt> table.
     */
    public static final class ScheduledEventsColumns implements BaseColumns {
        private ScheduledEventsColumns() {}

        /**
         * The stream id. Maps to the column {@link MediaDevicesContract.StreamsColumns#CHANNEL_NUMBER} in
         * the <tt>steams</tt> table.
         */
        public static final String CHANNEL_NUMBER = "channel_number";

        /**
         * the schedule event type
         * possible value:
         * {@link ScheduledEvents#SCHE_TYPE_RECORD}
         * {@link ScheduledEvents#SCHE_TYPE_REMIND}
         */
        public static final String SCHE_TYPE = "sche_type";

        /**
         * whether this event is valid
         */
        public static final String IS_VALID = "is_valid";

        /**
         * the time to perform record operation
         */
        public static final String CREATED_TIME = "created_time_utc_sec";

        /**
         * the time to remind user
         */
        public static final String REMINDER_TIME = "reminder_time_utc_sec";
    }

    // ------------------------------------------------------------------------
    // WatchedEvents

    /**
     * Column and URI constants for the <tt>watched_events</tt> table.
     */
    public static final class WatchedEvents {
        private WatchedEvents() {}

        /** The URI path for the <tt>watched_events</tt> table. */
        public static final String URI_PATH = "watched_events";
        public static final String TABLE = URI_PATH;

        /** The content URI for the <tt>watched_events</tt> table. */
        public static final Uri URI = Uri.parse("content://" + AUTHORITY)
                .buildUpon()
                .appendPath(URI_PATH)
                .build();

        /**
         * The complete projection for the <tt>watched_events</tt> table,
         * including all of its columns.
         */
        public static final String[] PROJECTION =
                new String[] { WatchedEventsColumns.EVENT_TITLE,
                               WatchedEventsColumns.CONTENT_TYPE,
                               WatchedEventsColumns.START_TIME,
                               WatchedEventsColumns.END_TIME,
                               WatchedEventsColumns.IS_HD,
                               WatchedEventsColumns.IS_FREE };

        /**
         * the table structure
         */
        public static final String STRUCTURE = "(" +
                "_id INTEGER PRIMARY KEY," +
                WatchedEventsColumns.EVENT_TITLE + " TEXT," +
                WatchedEventsColumns.CONTENT_TYPE + " TEXT," +
                WatchedEventsColumns.START_TIME + " INTEGER," +
                WatchedEventsColumns.END_TIME + " INTEGER," +
                WatchedEventsColumns.IS_HD + " INTEGER," +
                WatchedEventsColumns.IS_FREE + " INTEGER" +
            ");";
    }

    /**
     * Columns for the <tt>watched_events</tt> table.
     */
    public static final class WatchedEventsColumns implements BaseColumns {
        private WatchedEventsColumns() {}

        /**
         * The event title. Maps to the column {@link EventsColumns#TITLE} in
         * the <tt>events</tt> table.
         */
        public static final String EVENT_TITLE = "event_title";

        /**
         * The content type. Maps to the column {@link EventsColumns#CONTENT_TYPE} in
         * the <tt>events</tt> table.
         */
        public static final String CONTENT_TYPE = "content_type";

        /**
         * start time of event. Maps to the column {@link EventsColumns#START_TIME} in
         * the <tt>events</tt> table.
         */
        public static final String START_TIME = "start_time_utc_sec";

        /**
         * end time of event. Maps to the column {@link EventsColumns#END_TIME} in
         * the <tt>events</tt> table.
         */
        public static final String END_TIME = "end_time_utc_sec";

        /**
         * whether stream is HD. Maps to the column {@link EventsColumns#IS_HD} in
         * the <tt>events</tt> table.
         */
        public static final String IS_HD = "is_hd";

        /**
         * whether stream is scrambled. Maps to the column {@link EventsColumns#IS_FREE} in
         * the <tt>events</tt> table.
         */
        public static final String IS_FREE = "is_free";
    }
 // ------------------------------------------------------------------------
    // WatchedEvents

    /**
     * Column and URI constants for the <tt>WatchedChannels</tt> table.
     */
    public static final class WatchedChannels {
        private WatchedChannels() {}

        /** The URI path for the <tt>watched_channels</tt> table. */
        public static final String URI_PATH = "watched_channels";
        public static final String TABLE = URI_PATH;

        /** The content URI for the <tt>watched_channels</tt> table. */
        public static final Uri URI = Uri.parse("content://" + AUTHORITY)
                .buildUpon()
                .appendPath(URI_PATH)
                .build();

        /**
         * The complete projection for the <tt>watched_channels</tt> table,
         * including all of its columns.
         */
        public static final String[] PROJECTION =
                new String[] { WatchedChannelsColumns.CHANNEL_NUMBER,
                               WatchedChannelsColumns.WATCHED_TIMES
                             };

        /**
         * the table structure
         */
        public static final String STRUCTURE = "(" +
                "_id INTEGER PRIMARY KEY," +
                WatchedChannelsColumns.CHANNEL_NUMBER + " TEXT," +
                WatchedChannelsColumns.WATCHED_TIMES + " INTEGER DEFAULT 1" +
            ");";
    }

    /**
     * Columns for the <tt>watched_channels</tt> table.
     */
    public static final class WatchedChannelsColumns implements BaseColumns {
        private WatchedChannelsColumns() {}

        /**
         * The channel name. Maps to the column {@link ChannelMapColumns#CHANNEL_NUMBER} in
         * the <tt>channel_map</tt> table.
         */
        public static final String CHANNEL_NUMBER = ChannelMapColumns.CHANNEL_NUMBER;

        /**
         * times been played back
         */
        public static final String WATCHED_TIMES = "watched_times";
    }

    // ------------------------------------------------------------------------
    // MultilingualMaps(TBD)

    /**
     * Column and URI constants for the <tt>multilingual_maps</tt> table.
     */
    public static final class MultilingualMaps {
        private MultilingualMaps() {}

        /** The URI path for the <tt>multilingual_maps</tt> table. */
        public static final String URI_PATH = "multilingual_maps";
        public static final String TABLE = URI_PATH;

        /** The content URI for the <tt>multilingual_maps</tt> table. */
        public static final Uri URI = Uri.parse("content://" + AUTHORITY)
                .buildUpon()
                .appendPath(URI_PATH)
                .build();

        /**
         * The complete projection for the <tt>multilingual_maps</tt> table,
         * including all of its columns.
         */
        public static final String[] PROJECTION =
                new String[] { MultilingualMapsColumns.LANG };
        /**
         * the table structure
         */
        public static final String STRUCTURE = "(" +
                "_id INTEGER PRIMARY KEY," +
                MultilingualMapsColumns.LANG + " TEXT" +
            ");";
    }

    /**
     * Columns for the <tt>multilingual_maps</tt> table.
     */
    public static final class MultilingualMapsColumns implements BaseColumns {
        private MultilingualMapsColumns() {}

        /**
         * the language
         */
        public static final String LANG = "lang";
    }

 // ------------------------------------------------------------------------
    // Log

    /**
     * Column and URI constants for the <tt>log</tt> table.
     */
    public static final class EPGLog {
        private EPGLog() {}

        /** The URI path for the <tt>log</tt> table. */
        public static final String URI_PATH = "log";
        public static final String TABLE = URI_PATH;

        /** The content URI for the <tt>log</tt> table. */
        public static final Uri URI = Uri.parse("content://" + AUTHORITY)
                .buildUpon()
                .appendPath(URI_PATH)
                .build();

        /**
         * The complete projection for the <tt>log</tt> table,
         * including all of its columns.
         */
        public static final String[] PROJECTION =
                new String[] { EPGLogColumns.TIME,
                               EPGLogColumns.MSG };
        /**
         * the table structure
         */
        public static final String STRUCTURE = "(" +
                "_id INTEGER PRIMARY KEY," +
                EPGLogColumns.TIME + " DATETIME DEFAULT CURRENT_TIMESTAMP," +
                EPGLogColumns.MSG + " TEXT" +
            ");";
    }

    /**
     * Columns for the <tt>multilingual_maps</tt> table.
     */
    public static final class EPGLogColumns implements BaseColumns {
        private EPGLogColumns() {}

        /**
         * the Time of log item
         */
        private static final String TIME = "time";
        public static final String MSG = "message";
    }

}
