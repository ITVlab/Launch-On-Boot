package news.androidtv.launchonboot;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by Nick on 5/11/2017.
 *
 * A foreground service that listens for Screensaver events and responds.
 */
public class DreamListenerService extends Service {
    private static final String TAG = DreamListenerService.class.getSimpleName();

    private static final int ONGOING_NOTIFICATION_ID = 1;

    private BroadcastReceiver dreamHandler = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Redirect intent.
            Log.d(TAG, "Received service event: " + intent.getAction());
            BootReceiver.processEvent(context, intent);
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // Create a foreground service.
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = new Notification.Builder(this)
                .setContentTitle(getText(R.string.app_name))
                .setContentText(getText(R.string.notification_text))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.banner))
                .setContentIntent(pendingIntent)
                .setCategory(Notification.CATEGORY_RECOMMENDATION)
                .setPriority(Notification.PRIORITY_MIN)
                .build();

        startForeground(ONGOING_NOTIFICATION_ID, notification);
        Log.d(TAG, "Deploy notification");

        // Register listeners.
        IntentFilter filter = new IntentFilter(Intent.ACTION_DREAMING_STOPPED);
        registerReceiver(dreamHandler, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Unregister listener.
        unregisterReceiver(dreamHandler);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }
}
