
package org.clemlaf.conlost;
import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.CursorAdapter;
import android.widget.TextView;

import org.clemlaf.conlost.Event;
import org.clemlaf.conlost.ConlostContract.Events;

public class ConlostCursorAdapter extends CursorAdapter
{
    public ConlostCursorAdapter(Context co, Cursor cu, boolean req){
	super(co, cu, req);
    }
    
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
	final Event e = new Event();
	e.read(cursor);
        final TextView tt = (TextView) view.findViewById(R.id.timestamp);
        tt.setText(e.getDate());
        final TextView ti = (TextView) view.findViewById(R.id.interval);
        ti.setText(e.getInterval());
	if(e.disconnectionInterval > 600000)
	    ti.setTextColor(0xffaa0000);
	else if(e.disconnectionInterval > 60000)
	    ti.setTextColor(0xff888800);
	else
	    ti.setTextColor(0xff222222);
    }
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final View view = LayoutInflater.from(context).inflate(R.layout.event, parent, false);
        return view;
    }
}
