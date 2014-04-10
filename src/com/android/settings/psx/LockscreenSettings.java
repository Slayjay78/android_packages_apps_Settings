package com.android.settings.psx;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;
import android.provider.Settings;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class LockscreenSettings extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    // Omni Additions
    private static final String BATTERY_AROUND_LOCKSCREEN_RING = "battery_around_lockscreen_ring";
    private static final String LOCKSCREEN_MAXIMIZE_WIDGETS = "lockscreen_maximize_widgets";
    private static final String KEY_SEE_TRHOUGH = "see_through";
    private static final String KEY_LOCKSCREEN_QUICK_UNLOCK_CONTROL = "lockscreen_quick_unlock_control"; 
   
    // Omni Additions
    private CheckBoxPreference mLockRingBattery;
    private CheckBoxPreference mMaximizeKeyguardWidgets;
    private CheckBoxPreference mSeeThrough;
    private CheckBoxPreference mLockscreenQuickUnlockControl;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.lockscreen_settings);
        PreferenceScreen root = getPreferenceScreen();

        // Add the additional Omni settings
        mSeeThrough = (CheckBoxPreference) root.findPreference(KEY_SEE_TRHOUGH);
        mSeeThrough.setChecked(Settings.System.getInt(getContentResolver(),
                    Settings.System.LOCKSCREEN_SEE_THROUGH, 0) == 1);
        mLockRingBattery = (CheckBoxPreference) root
                .findPreference(BATTERY_AROUND_LOCKSCREEN_RING);
        if (mLockRingBattery != null) {
            mLockRingBattery.setChecked(Settings.System.getInt(getContentResolver(),
                    Settings.System.BATTERY_AROUND_LOCKSCREEN_RING, 0) == 1);
        }

        mMaximizeKeyguardWidgets = (CheckBoxPreference) root.findPreference(LOCKSCREEN_MAXIMIZE_WIDGETS);
        if (mMaximizeKeyguardWidgets != null) {
            mMaximizeKeyguardWidgets.setChecked(Settings.System.getInt(getContentResolver(),
                    Settings.System.LOCKSCREEN_MAXIMIZE_WIDGETS, 0) == 1);
        }
        
        //Quick Unlock        
        mLockscreenQuickUnlockControl = (CheckBoxPreference) findPreference(KEY_LOCKSCREEN_QUICK_UNLOCK_CONTROL);
        mLockscreenQuickUnlockControl.setChecked(Settings.System.getInt(getContentResolver(),
                    Settings.System.PSX_LOCKSCREEN_QUICK_UNLOCK_CONTROL, 0) == 1);
	
    }

    private boolean isToggled(Preference pref) {
        return ((CheckBoxPreference) pref).isChecked();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {      
        final String key = preference.getKey();
        if (preference == mLockRingBattery) {
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.BATTERY_AROUND_LOCKSCREEN_RING, isToggled(preference) ? 1 : 0);
			return true;
        } else if (preference == mMaximizeKeyguardWidgets) {
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.LOCKSCREEN_MAXIMIZE_WIDGETS, isToggled(preference) ? 1 : 0);
			return true;
        } else if (preference == mSeeThrough) {
            Settings.System.putInt(getContentResolver(), Settings.System.LOCKSCREEN_SEE_THROUGH,
                    mSeeThrough.isChecked() ? 1 : 0);
			return true;
	} else if (preference == mLockscreenQuickUnlockControl) {
            Settings.System.putInt(getContentResolver(), Settings.System.PSX_LOCKSCREEN_QUICK_UNLOCK_CONTROL,
                    mSeeThrough.isChecked() ? 1 : 0);
			return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        // TODO Auto-generated method stub
        return false;
    }
}
