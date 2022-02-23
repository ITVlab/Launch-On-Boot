package news.androidtv.launchonboot;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.tv.TvContract;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import news.androidtv.launchonboot.SettingsManager;

/**
 * Created by Nick on 10/23/2016.
 */

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = BootReceiver.class.getSimpleName();
    private static final boolean DEBUG = true;

    private boolean mScreenOnListener = false;
    private Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        if (intent.getAction() != null &&
                intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            startForegroundService();
        }
        processEvent(context, intent);
    }

    public static void processEvent(Context context, Intent intent) {
        if (DEBUG) {
            Log.d(TAG, "Received intent");
            Log.d(TAG, intent.toString());
        }
/*        if (!mScreenOnListener) {
            context.registerReceiver(this, new IntentFilter(Intent.ACTION_SCREEN_ON));
            mScreenOnListener = true;
        }*/

        SettingsManager settingsManager = new SettingsManager(context);
        if (!settingsManager.getBoolean(SettingsManagerConstants.BOOT_APP_ENABLED)) {
            return;
        }
        if (intent.getAction() != null &&
                intent.getAction().equals(Intent.ACTION_USER_PRESENT) &&
                !settingsManager.getBoolean(SettingsManagerConstants.ON_WAKEUP)) {
            return;
        }
        if (intent.getAction() != null &&
                intent.getAction().equals(Intent.ACTION_SCREEN_ON) &&
                !settingsManager.getBoolean(SettingsManagerConstants.ON_WAKEUP)) {
            return;
        }
        if (intent.getAction() != null &&
                intent.getAction().equals(Intent.ACTION_DREAMING_STOPPED) &&
                !settingsManager.getBoolean(SettingsManagerConstants.ON_WAKEUP)) {
            return;
        }
        if (settingsManager.getBoolean(SettingsManagerConstants.LAUNCH_LIVE_CHANNELS) &&
                context.getResources().getBoolean(R.bool.TIF_SUPPORT) &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Intent i = new Intent(Intent.ACTION_VIEW, TvContract.Channels.CONTENT_URI);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        } else if (!settingsManager.getString(SettingsManagerConstants.LAUNCH_ACTIVITY).isEmpty()) {
            Intent i;
            i = context.getPackageManager().getLeanbackLaunchIntentForPackage(
                        settingsManager.getString(SettingsManagerConstants.LAUNCH_ACTIVITY));
            if (i == null) {
                i = context.getPackageManager().getLaunchIntentForPackage(
                        settingsManager.getString(SettingsManagerConstants.LAUNCH_ACTIVITY));
            }

            if (i == null) {
                Toast.makeText(context, R.string.null_intent, Toast.LENGTH_SHORT).show();
                return;
            }
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                long restartTime = 1000*5;
                PendingIntent restartIntent = PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_ONE_SHOT);
                AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                if (Build.VERSION.SDK_INT > 28) {
                    mgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + restartTime, restartIntent);

                } else {
                    context.startActivity(i);
                }
            } catch (ActivityNotFoundException e) {
                Toast.makeText(context, R.string.null_intent, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startForegroundService() {
        Intent i = new Intent(mContext, DreamListenerService.class);
        mContext.startService(i);
    }
}