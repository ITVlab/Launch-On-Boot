package news.androidtv.launchonboot;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.MenuAdapter;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.felkertech.settingsmanager.SettingsManager;

import java.util.List;

import static android.view.View.GONE;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final boolean DEBUG = true;

    private SettingsManager mSettingsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSettingsManager = new SettingsManager(this);

        ((Switch) findViewById(R.id.switch_live_channels)).setChecked(
                mSettingsManager.getBoolean(SettingsManagerConstants.LAUNCH_LIVE_CHANNELS));
        ((Switch) findViewById(R.id.switch_live_channels)).setOnCheckedChangeListener
                (new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mSettingsManager.setBoolean(
                        SettingsManagerConstants.LAUNCH_LIVE_CHANNELS, isChecked);
            }
        });

        if (!getResources().getBoolean(R.bool.DEBUG_FLAG_TEST_BUTTON)) {
            findViewById(R.id.button_test).setVisibility(GONE);
        }
        findViewById(R.id.button_test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, BootReceiver.class);
                sendBroadcast(i);
            }
        });

        if (DEBUG) {
            Log.d(TAG, getLeanbackApps().toString());
        }
    }

    public List getLeanbackApps() {
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);
        return getPackageManager().queryIntentActivities(mainIntent, 0);
    }

    public String[] getAppNames(List<ResolveInfo> leanbackApps) {
        String[] appNames = new String[leanbackApps.size()];
        for (ResolveInfo info : leanbackApps) {
//            info.
        }
        return null;
    }
}
