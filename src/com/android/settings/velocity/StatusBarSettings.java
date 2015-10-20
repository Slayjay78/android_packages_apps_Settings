package com.android.settings.velocity;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;

import com.android.internal.logging.MetricsLogger;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class StatusBarSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String QUICK_PULLDOWN = "quick_pulldown";

    private ListPreference mQuickPulldown;
		
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings_statusbar);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        mQuickPulldown = (ListPreference) prefSet.findPreference(QUICK_PULLDOWN);
        mQuickPulldown.setOnPreferenceChangeListener(this);
        int quickPulldownValue = Settings.System.getIntForUser(resolver,
                Settings.System.QS_QUICK_PULLDOWN, 0, UserHandle.USER_CURRENT);
        mQuickPulldown.setValue(String.valueOf(quickPulldownValue));
        mQuickPulldown.setSummary(mQuickPulldown.getEntry());
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getContentResolver();
        if (preference == mQuickPulldown) {
            int quickPulldownValue = Integer.valueOf((String) newValue);
            int index = mQuickPulldown.findIndexOfValue((String) newValue);
            Settings.System.putIntForUser(resolver, Settings.System.QS_QUICK_PULLDOWN,
                    quickPulldownValue, UserHandle.USER_CURRENT);
            mQuickPulldown.setSummary(mQuickPulldown.getEntries()[index]);
            return true;
        }
        return false;
    }

    @Override
    protected int getMetricsCategory() {
        return MetricsLogger.DISPLAY;
    }
}
