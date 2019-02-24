package org.clemlaf.conlost;

import android.os.Build;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.PendingIntent;
import android.app.Service;
import android.app.AlarmManager;
import android.content.Intent;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.CellInfo;
import android.net.Uri;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import java.util.Arrays;
import java.util.List;


/**
 * This foreground service is monitoring phone state. A notification shows which mobile network is the
 * phone is connected to.
 * 
 */
public class MonitorService extends Service
{
    private PendingIntent openUIPendingIntent;
    private TelephonyManager tm;
    private PhoneStateListener phoneMonitor;
    private int telephonyManagerEvents;
    private Boolean lastMobileNetworkConnected;
    private boolean mobileNetworkConnected;
    public static final String ACTION_NOTIFICATION = "conlost.notif";
    public static final String TAG = "CONLOST";
    private static final String INTENT_ALARM_RESTART_SERVICE_DIED = "ALARM_RESTART_SERVICE_DIED";
    public static final String INTENT_UPDATE_NOTIF_ON_LOCKSCREEN = "UPDATE_NOTIF_ON_LOCKSCREEN";


    public void onCreate() {
        super.onCreate();

        tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        
        // This intent is fired when the application notification is clicked.
        openUIPendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_NOTIFICATION), PendingIntent.FLAG_CANCEL_CURRENT);

        // Watch mobile connections.
        phoneMonitor = new PhoneStateListener() {
            @Override
            public void onDataConnectionStateChanged(int state, int networkType) {
                updateService();
            }

            @Override
            public void onServiceStateChanged(ServiceState serviceState) {

                mobileNetworkConnected =
                        (serviceState != null) && (serviceState.getState() == ServiceState.STATE_IN_SERVICE);

                updateService();
            }

            @Override
            public void onCellInfoChanged(List<CellInfo> cellInfo) {
                updateService();
            }

            private void updateService() {
                final int phoneStateUpdated = onPhoneStateUpdated();
                updateNotification(true, phoneStateUpdated == 1);
            }
        };
        telephonyManagerEvents = PhoneStateListener.LISTEN_SERVICE_STATE |
                                 PhoneStateListener.LISTEN_DATA_CONNECTION_STATE;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            telephonyManagerEvents |= PhoneStateListener.LISTEN_CELL_INFO;

        tm.listen(phoneMonitor, telephonyManagerEvents);

        /*if (prefs.getBoolean(SP_KEY_ENABLE_AUTO_RESTART_SERVICE, false) &&
                Arrays.asList(ANDROID_VERSIONS_ALLOWED_TO_AUTO_RESTART_SERVICE).contains(Build.VERSION.RELEASE)) {
            // Kitkat and JellyBean auto-kill service workaround
            // http://stackoverflow.com/a/20735519/1527491
            ensureServiceStaysRunning();
	    }*/

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        tm.listen(phoneMonitor, PhoneStateListener.LISTEN_NONE);

        tm = null;

        // Remove the status bar notification.
        stopForeground(true);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null) //we have intent to handle
        {
            if (intent.getBooleanExtra(INTENT_ALARM_RESTART_SERVICE_DIED, false)) { //intent to check service is alive
                Log.d(TAG, "onStartCommand > after ALARM_RESTART_SERVICE_DIED [ Kitkat START_STICKY bug ]");
                if (isRunning()) {
                    Log.d(TAG, "onStartCommand > Service already running - return immediately... [ Kitkat START_STICKY bug ]");
                    ensureServiceStaysRunning();
                    return START_STICKY;
                }
            }

            if (intent.getBooleanExtra(INTENT_UPDATE_NOTIF_ON_LOCKSCREEN, false)) { //intent to update the notification on lockscreen (hide / show)
                Log.d(TAG, "onStartCommand > update the notification on lockscreen (hide / show)");
                updateNotification(false, false);
            }
        }
        
        // Update with current state.
        onPhoneStateUpdated();
        updateNotification(false, false);

        return START_STICKY;
	}


    /*@Override
    public void onTaskRemoved(Intent rootIntent) {
        if (prefs.getBoolean(SP_KEY_ENABLE_AUTO_RESTART_SERVICE, false) &&
                Arrays.asList(ANDROID_VERSIONS_ALLOWED_TO_AUTO_RESTART_SERVICE).contains(Build.VERSION.RELEASE)) {
            // If task was removed, we should launch the service again.
            if (DEBUG)
                Log.d(TAG, "onTaskRemoved > setting alarm to restart service [ Kitkat START_STICKY bug ]");
            Intent restartService = new Intent(getApplicationContext(),
                    this.getClass());
            restartService.setPackage(getPackageName());
            PendingIntent restartServicePI = PendingIntent.getService(
                    getApplicationContext(), 1, restartService,
                    PendingIntent.FLAG_ONE_SHOT);
            AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
            alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 1000, restartServicePI);
        }

	}*/
    
    private boolean isRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
            if (MonitorService.class.getName().equals(service.service.getClassName()))
                return true;
        return false;
    }
    
    private void ensureServiceStaysRunning() {
        // KitKat appears to have (in some cases) forgotten how to honor START_STICKY
        // and if the service is killed, it doesn't restart.  On an emulator & AOSP device, it restarts...
        // on my CM device, it does not - WTF?  So, we'll make sure it gets back
        // up and running in a minimum of 10 minutes.  We reset our timer on a handler every
        // 2 minutes...but since the handler runs on uptime vs. the alarm which is on realtime,
        // it is entirely possible that the alarm doesn't get reset.  So - we make it a noop,
        // but this will still count against the app as a wakelock when it triggers.  Oh well,
        // it should never cause a device wakeup.  We're also at SDK 19 preferred, so the alarm
        // mgr set algorithm is better on memory consumption which is good.
    	// http://stackoverflow.com/a/20735519/1527491
        if (false)
        {
        		Log.d(TAG, "ensureServiceStaysRunning > setting alarm. [ Kitkat START_STICKY bug ]");
        	// A restart intent - this never changes...        
            final int restartAlarmInterval = 10*60*1000;
            final int resetAlarmTimer = 1*60*1000;
            final Intent restartIntent = new Intent(this, MonitorService.class);
            restartIntent.putExtra(INTENT_ALARM_RESTART_SERVICE_DIED, true);
            final AlarmManager alarmMgr = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
            Handler restartServiceHandler = new Handler()
            {
                @Override
                public void handleMessage(Message msg) {
                    // Create a pending intent
                    PendingIntent pintent = PendingIntent.getService(getApplicationContext(), 0, restartIntent, 0);
                    alarmMgr.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + restartAlarmInterval, pintent);
                    sendEmptyMessageDelayed(0, resetAlarmTimer);
                }            
            };
            restartServiceHandler.sendEmptyMessageDelayed(0, 0); 
        }
    }

    /**
     * Update the status bar notification.
     * @param playSound play notification sound
     * @param phoneStateUpdated if phone state has been updated
     */
	private void updateNotification(boolean playSound, boolean phoneStateUpdated) {
        String tickerText, contentText;
	int smallIcon;
        final int notificationPriority = NotificationCompat.PRIORITY_LOW;
        final PendingIntent contentIntent = openUIPendingIntent;

        if (mobileNetworkConnected) { // Not Free Mobile nor Orange
	     tickerText = getString(R.string.connected);
	     smallIcon = android.R.drawable.checkbox_on_background;
      	} else { // Foreign operator
	    tickerText = getString(R.string.connection_lost);
            smallIcon = android.R.drawable.stat_sys_warning;
	}

        final NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(getApplicationContext());
        nBuilder.setSmallIcon(smallIcon)
                .setContentTitle(tickerText).setTicker(tickerText)
                .setContentIntent(contentIntent) // always set the content intent - exception fired on GB if null
                .setPriority(notificationPriority)
                .setWhen(0);

        if (playSound) {
            Log.d(TAG, "Play notification sound");


            if (phoneStateUpdated || !mobileNetworkConnected) {
		Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                nBuilder.setSound(soundUri);
            }
        }

        startForeground(R.string.stat_connected, nBuilder.build());
    }
    

    /**
     * This method is called when the phone service state is updated.
     * @return -1 : no update ; 0 : minor update ; 1 : major update
     * It is a major update if mobile operator changes or phone connects a network.
     */
	private int onPhoneStateUpdated() {		
        // Prevent duplicated inserts.
        if (lastMobileNetworkConnected != null && 
            lastMobileNetworkConnected.equals(mobileNetworkConnected)){
            return -1;
        }
        
	int ret = 0;
        
        if (lastMobileNetworkConnected != null && lastMobileNetworkConnected != mobileNetworkConnected)
        	ret = 1;
        
        lastMobileNetworkConnected = mobileNetworkConnected;
        
        Log.i(TAG, "Phone state updated: connected=" + mobileNetworkConnected) ;
        return ret;
    }

}
