package org.clemlaf.conlost;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class ConlostContract {
    /**
     * The authority for the content provider.
     */
    public static final String AUTHORITY = "org.clemlaf.conlost";
    
    protected static interface EventsColumns {
        String TIMESTAMP = "timestamp";
        String DISC_INTERVAL = "disconnection";
    }
    
    /**
     * Table for events.
     */
    public static class Events implements BaseColumns, EventsColumns {
        /**
         * The content:// style URI for this table.
         */
        public static final Uri CONTENT_URI = new Uri.Builder()
                .scheme(ContentResolver.SCHEME_CONTENT).authority(AUTHORITY)
                .appendPath("events").build();
        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single
         * entry.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/event";
        /**
         * The MIME type of a {@link #CONTENT_URI} providing a directory of
         * entries.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/event";
    }
}
