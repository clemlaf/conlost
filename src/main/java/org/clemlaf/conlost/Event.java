package org.clemlaf.conlost;

import android.content.ContentValues;
import android.database.Cursor;
import java.util.Date;
import java.text.SimpleDateFormat;

import org.clemlaf.conlost.ConlostContract.Events;

public class Event{
    public long timestamp;
    public long disconnectionInterval;

    public Event() {}

    public Event(Event e){
	timestamp = e.timestamp;
	disconnectionInterval = e.disconnectionInterval;
    }

    public void read(Cursor c) {
	timestamp = c.getLong(c.getColumnIndexOrThrow(Events.TIMESTAMP));
	disconnectionInterval = c.getLong(c.getColumnIndexOrThrow(Events.DISC_INTERVAL));
    }

    public void write(ContentValues val) {
	val.put(Events.TIMESTAMP, timestamp);
	val.put(Events.DISC_INTERVAL, disconnectionInterval);
    }

    public String getDate(){
	Date d = new Date(timestamp);
	SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        return format.format(d);
    }

    public String getInterval(){
	long val = disconnectionInterval;
	long hours = val/3600000; val %= 3600000;
	long minutes = val/60000; val %= 60000;
	long seconds = (val/1000);
	return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    @Override
    public String toString() {
	return "Event [timestamp=" + getDate() + ", disc_interval=" + getInterval() +"]";
    }

}
