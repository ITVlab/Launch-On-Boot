package news.androidtv.launchonboot;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import news.androidtv.launchonboot.SettingsManager;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static android.view.View.GONE;
import static news.androidtv.launchonboot.SettingsManagerConstants.ONBOARDING;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final boolean DEBUG = false;

    private SettingsManager mSettingsManager;
    private Switch mSwitchEnabled;
    private Switch mSwitchLiveChannels;
    private Switch mSwitchWakeup;
    private Button mButtonSelectApp;
    private TextView mPackageName;
    private List<ResolveInfo> launcherApplications;

    public static class AppListItem{
        public final String name;
        public final String pkgName;
        public final String activityName;
        public final Drawable icon;
        public AppListItem(String name, Drawable icon, String pkgName, String activityName) {
            this.name = name;
            this.icon = icon;
            this.pkgName = pkgName;
            this.activityName = activityName;
        }
        @Override
        public String toString() {
            return name;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    	launcherApplications = getLauncherApps();
        mSettingsManager = new SettingsManager(this);
        if (!mSettingsManager.getBoolean(ONBOARDING)) {
            startActivity(new Intent(this, OnboardingActivity.class));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSwitchLiveChannels = ((Switch) findViewById(R.id.switch_live_channels));
        mSwitchEnabled = ((Switch) findViewById(R.id.switch_enable));
        mSwitchWakeup = ((Switch) findViewById(R.id.switch_wakeup));
        mButtonSelectApp = (Button) findViewById(R.id.button_select_app);
        mPackageName = ((TextView) findViewById(R.id.text_package_name));

        mSwitchEnabled.setChecked(
                mSettingsManager.getBoolean(SettingsManagerConstants.BOOT_APP_ENABLED));
        mSwitchLiveChannels.setChecked(
                mSettingsManager.getBoolean(SettingsManagerConstants.LAUNCH_LIVE_CHANNELS));
        mSwitchWakeup.setChecked(
                mSettingsManager.getBoolean(SettingsManagerConstants.ON_WAKEUP));
        mPackageName
                .setText(_mPackageText());
        updateSelectionView();

        mSwitchEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mSettingsManager.setBoolean(
                        SettingsManagerConstants.BOOT_APP_ENABLED, isChecked);
                updateSelectionView();
            }
        });
        mSwitchLiveChannels.setOnCheckedChangeListener
                (new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        mSettingsManager.setBoolean(
                                SettingsManagerConstants.LAUNCH_LIVE_CHANNELS, isChecked);
                        updateSelectionView();
                    }
                });
        mSwitchWakeup.setOnCheckedChangeListener
                (new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        mSettingsManager.setBoolean(
                                SettingsManagerConstants.ON_WAKEUP, isChecked);
                        updateSelectionView();
                        if (isChecked) {
                            startForegroundService();
                        }
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

        mButtonSelectApp.setOnClickListener(new View.OnClickListener() {
            AppListItem[] appListItems = getAppList();
            @Override
            public void onClick(View v) {
                ListAdapter adapter = new ArrayAdapter<AppListItem>(MainActivity.this,
                        android.R.layout.select_dialog_item,
                        android.R.id.text1,
                        appListItems){
                    public View getView(int position, View convertView, ViewGroup parent) {
                        //Use super class to create the View
                        View v = super.getView(position, convertView, parent);
                        TextView tv = (TextView)v.findViewById(android.R.id.text1);
						tv.setTextSize(18);

                        //Put the image on the TextView
                        Drawable icn = appListItems[position].icon;
                        int dp64 = (int) (64 * getResources().getDisplayMetrics().density);
                        icn.setBounds(0, 0, dp64, dp64);
                        tv.setCompoundDrawables(icn, null, null, null);

                        //Add margin between image and text (support various screen densities)
                        int dp5 = (int) (5 * getResources().getDisplayMetrics().density + 0.5f);
                        int dp10 = (int) (5 * getResources().getDisplayMetrics().density + 0.5f);
                        tv.setCompoundDrawablePadding(dp5);
                        tv.setPadding(dp10, dp10, dp10, dp10);

                        return v;
                    }
                };
                new AlertDialog.Builder(new ContextThemeWrapper(MainActivity.this, android.R.style.Theme_Material_Light_Dialog))
                        .setTitle(R.string.button_select_app)
                        .setAdapter(adapter, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String packageName = getPackageName(launcherApplications.get(which));
                                mSettingsManager.setString(SettingsManagerConstants.LAUNCH_ACTIVITY,
                                        packageName);
                                mPackageName.setText(_mPackageText());
                            }
                        })
                        .show();
            }
        });
        mButtonSelectApp.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                v.setBackgroundColor(hasFocus ? getResources().getColor(R.color.colorAccent) :
                        getResources().getColor(R.color.colorPrimaryDark));
            }
        });
        findViewById(R.id.button_test).setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                v.setBackgroundColor(hasFocus ? getResources().getColor(R.color.colorAccent) :
                        getResources().getColor(R.color.colorPrimaryDark));
            }
        });

        if (DEBUG) {
            Log.d(TAG, launcherApplications.toString());
            getAppNames(launcherApplications);
        }
        registerReceiver(new BootReceiver(), new IntentFilter(Intent.ACTION_SCREEN_ON));

        if (mSettingsManager.getBoolean(SettingsManagerConstants.ON_WAKEUP)) {
            startForegroundService();
        }
    }

    public AppListItem[] getAppList()
    {
        AppListItem[] items = new AppListItem[launcherApplications.size()];
        for (int i = 0; i < launcherApplications.size(); i++) {
            ResolveInfo info = launcherApplications.get(i);
            String pkgName = info.activityInfo.packageName;
            String appName = info.loadLabel(this.getPackageManager()).toString();
            AppListItem current = new AppListItem(appName + "\n" + pkgName, info.loadIcon(this.getPackageManager()), info.activityInfo.packageName, info.activityInfo.name);
            items[i] = current;
        }
        return items;
    }

    private String _mPackageText()
    {
        String pkgName = mSettingsManager.getString(SettingsManagerConstants.LAUNCH_ACTIVITY);
        String appName = "";
        try {
            ApplicationInfo app = this.getPackageManager().getApplicationInfo(pkgName, 0);
            appName = this.getPackageManager().getApplicationLabel(app).toString();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String result = appName + "\n" + pkgName;
        return result;
    }

    public List<ResolveInfo> getLauncherApps() {
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        // Change which category is used based on form factor.
        mainIntent.addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);
        final List<ResolveInfo> installedApplicationstv = getPackageManager().queryIntentActivities(mainIntent, 0);

        mainIntent.removeCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        final List<ResolveInfo> installedApplications = getPackageManager().queryIntentActivities(mainIntent, 0);

        boolean isPresent = false;
        for (int i = 0; i < installedApplicationstv.size(); i++) {
            isPresent = false;
            for (int j = 0; j < installedApplications.size(); j++) {
                if (installedApplicationstv.get(i).activityInfo.packageName.equals(installedApplications.get(j).activityInfo.packageName)) {
                    isPresent = true;
                    break;
                }
            }
            if (!isPresent) {
                installedApplications.add(installedApplicationstv.get(i));
            }
        }

         Collections.sort(installedApplications,
                new Comparator<ResolveInfo>()
                {
                    public int compare(ResolveInfo f1, ResolveInfo f2)
                    {
                        return f1.loadLabel(getPackageManager()).toString().compareToIgnoreCase(f2.loadLabel(getPackageManager()).toString());
                    }
                });

        return installedApplications;
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

    public String getPackageName(ResolveInfo resolveInfo) {
        return resolveInfo.activityInfo.packageName;
    }

    private void updateSelectionView() {
        if (mSwitchEnabled.isChecked()) {
            mSwitchLiveChannels.setEnabled(true);
            findViewById(R.id.button_test).setEnabled(true);
            if (mSwitchLiveChannels.isChecked()) {
                mButtonSelectApp.setVisibility(GONE);
                mPackageName.setVisibility(GONE);
            } else {
                mButtonSelectApp.setVisibility(View.VISIBLE);
                mPackageName.setVisibility(View.VISIBLE);
            }
        } else {
            mButtonSelectApp.setVisibility(GONE);
            mPackageName.setVisibility(GONE);
            mSwitchLiveChannels.setEnabled(false);
            findViewById(R.id.button_test).setEnabled(false);
        }
    }

    private void startForegroundService() {
        // Ideally only starts once :thinking-emoji:
        Intent i = new Intent(MainActivity.this, DreamListenerService.class);
        startService(i);
    }
}
