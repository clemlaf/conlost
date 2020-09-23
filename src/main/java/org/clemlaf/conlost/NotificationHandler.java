/*
 * Copyright (C) 2012 Pixmob (http://github.com/pixmob)
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
package org.clemlaf.conlost;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import static org.clemlaf.conlost.MonitorService.TAG;

/**
 * Handle notification action.
 * @author Pixmob
 */
public class NotificationHandler extends BroadcastReceiver {

    private static final String TAG = "Conlost.NotificationHandler";

    @Override
    public void onReceive(Context context, Intent intent) {
	Log.d(TAG, "Was here !");
	final Intent i = new Intent(context, ConlostActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	if (i != null) {
	    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    try {
		context.startActivity(i);
	    } catch(ActivityNotFoundException e) {
		Log.w(TAG, "Activity not found for notification action", e);
	    }
	}
    }
}
