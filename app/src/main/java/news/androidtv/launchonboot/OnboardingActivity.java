package news.androidtv.launchonboot;

import android.app.Activity;
import android.os.Bundle;

import news.androidtv.launchonboot.SettingsManager;

/**
 * Created by Nick on 4/22/2017.
 */

public class OnboardingActivity extends Activity {
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.onboarding);
        // Update the shared preferences
        new SettingsManager(this).setBoolean(SettingsManagerConstants.ONBOARDING, true);

    }
}
