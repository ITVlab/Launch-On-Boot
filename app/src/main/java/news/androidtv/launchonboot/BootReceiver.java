package news.androidtv.launchonboot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.tv.TvContract;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.felkertech.settingsmanager.SettingsManager;

/**
 * Created by Nick on 10/23/2016.
 */

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = BootReceiver.class.getSimpleName();
    private static final boolean DEBUG = true;

    private boolean mScreenOnListener = false;

    @Override
    public void onReceive(Context context, Intent intent) {
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
        if (settingsManager.getBoolean(SettingsManagerConstants.LAUNCH_LIVE_CHANNELS) &&
                context.getResources().getBoolean(R.bool.TIF_SUPPORT) &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Intent i = new Intent(Intent.ACTION_VIEW, TvContract.Channels.CONTENT_URI);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        } else if (!settingsManager.getString(SettingsManagerConstants.LAUNCH_ACTIVITY).isEmpty()) {
            Intent i;
            if (context.getResources().getBoolean(R.bool.IS_TV)) {
                i = context.getPackageManager().getLeanbackLaunchIntentForPackage(
                        settingsManager.getString(SettingsManagerConstants.LAUNCH_ACTIVITY));
            } else {
                i = context.getPackageManager().getLaunchIntentForPackage(
                        settingsManager.getString(SettingsManagerConstants.LAUNCH_ACTIVITY));
            }

            if (i == null) {
                Toast.makeText(context, R.string.null_intent, Toast.LENGTH_SHORT).show();
                return;
            }
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
    }
}