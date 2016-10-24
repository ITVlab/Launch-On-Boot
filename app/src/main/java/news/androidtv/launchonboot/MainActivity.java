package news.androidtv.launchonboot;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.MenuAdapter;
import android.util.Log;
import android.view.ContextThemeWrapper;
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

        findViewById(R.id.button_select_app).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(new ContextThemeWrapper(MainActivity.this, android.R.style.Theme_Material_Dialog))
                        .setTitle("Select an app")
                        .setItems(getAppNames(getLeanbackApps()), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mSettingsManager.setString(SettingsManagerConstants.LAUNCH_ACTIVITY,
                                        getPackageName(getLeanbackApps().get(which)));

                            }
                        })
                        .show();
            }
        });

        if (DEBUG) {
            Log.d(TAG, getLeanbackApps().toString());
            getAppNames(getLeanbackApps());
        }
    }

    public List<ResolveInfo> getLeanbackApps() {
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);
        return getPackageManager().queryIntentActivities(mainIntent, 0);
    }

    public String[] getAppNames(List<ResolveInfo> leanbackApps) {
        String[] appNames = new String[leanbackApps.size()];
        for (int i = 0; i < leanbackApps.size(); i++) {
            ResolveInfo info = leanbackApps.get(i);
            appNames[i] = info.loadLabel(this.getPackageManager()).toString();
            Log.d(TAG, info.loadLabel(this.getPackageManager()).toString());
            Log.d(TAG, info.activityInfo.toString());
            Log.d(TAG, info.activityInfo.name);
        }
        return appNames;
    }

    public String getActivityName(ResolveInfo resolveInfo) {
        return resolveInfo.activityInfo.name;
    }

    public String getPackageName(ResolveInfo resolveInfo) {
        return resolveInfo.activityInfo.packageName;
    }
}
