package com.android.settings.psx;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
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
import android.provider.Settings.SettingNotFoundException;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import android.preference.ListPreference;
import android.preference.PreferenceCategory;
import android.database.ContentObserver;
import android.os.Handler;
import com.android.settings.psx.DisplayRotation;
import com.android.internal.view.RotationPolicy;
import android.content.res.Resources;
import java.util.ArrayList;

public class UserInterfaceSettings extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private static final String KEY_POWER_CRT_MODE = "system_power_crt_mode";
    private static final String KEY_LIGHT_OPTIONS = "category_light_options";
    private static final String KEY_NOTIFICATION_PULSE = "notification_pulse";
    private static final String KEY_BATTERY_LIGHT = "battery_light";
    private static final String KEY_ANIMATION_OPTIONS = "category_animation_options";
    private static final String KEY_DISPLAY_ROTATION = "display_rotation";
    private static final String KEY_IMMERSIVE_MODE_STYLE = "immersive_mode_style";
    private static final String KEY_IMMERSIVE_MODE_STATE = "immersive_mode_state";
	private static final String KEY_CLEAR_RECENTS_BUTTON_LOCATION = "clear_recents_button_location";

    private static final String ROTATION_ANGLE_0 = "0";
    private static final String ROTATION_ANGLE_90 = "90";
    private static final String ROTATION_ANGLE_180 = "180";
    private static final String ROTATION_ANGLE_270 = "270";
    private static final String RECENTS_USE_OMNISWITCH = "recents_use_omniswitch";
    private static final String OMNISWITCH_START_SETTINGS = "omniswitch_start_settings";

    // Package name of the omnniswitch app
    public static final String OMNISWITCH_PACKAGE_NAME = "org.omnirom.omniswitch";
    // Intent for launching the omniswitch settings actvity
    public static Intent INTENT_OMNISWITCH_SETTINGS = new Intent(Intent.ACTION_MAIN)
            .setClassName(OMNISWITCH_PACKAGE_NAME, OMNISWITCH_PACKAGE_NAME + ".SettingsActivity");	

    private ListPreference mCrtMode;
    private PreferenceCategory mLightOptions;
    private PreferenceScreen mNotificationPulse;
    private PreferenceScreen mBatteryPulse;
    private PreferenceScreen mDisplayRotationPreference;
    private ListPreference mImmersiveModePref;
    private CheckBoxPreference mImmersiveModeState;
    private ListPreference mClearRecentsLocation;
    private CheckBoxPreference mRecentsUseOmniSwitch;
    private Preference mOmniSwitchSettings;
    private boolean mOmniSwitchInitCalled;
	
    private ContentObserver mAccelerometerRotationObserver = 
            new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            updateDisplayRotationPreferenceDescription();
        }
    };
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.user_interface_settings);
		PreferenceScreen prefSet = getPreferenceScreen();

        mDisplayRotationPreference = (PreferenceScreen) findPreference(KEY_DISPLAY_ROTATION);

        // respect device default configuration
        // true fades while false animates
        boolean electronBeamFadesConfig = getResources().getBoolean(
                com.android.internal.R.bool.config_animateScreenLights);
        PreferenceCategory animationOptions =
            (PreferenceCategory) prefSet.findPreference(KEY_ANIMATION_OPTIONS);
        mCrtMode = (ListPreference) prefSet.findPreference(KEY_POWER_CRT_MODE);
        if (!electronBeamFadesConfig && mCrtMode != null) {
            int crtMode = Settings.System.getInt(getContentResolver(),
                    Settings.System.SYSTEM_POWER_CRT_MODE, 1);
            mCrtMode.setValue(String.valueOf(crtMode));
            mCrtMode.setSummary(mCrtMode.getEntry());
            mCrtMode.setOnPreferenceChangeListener(this);
        } else if (animationOptions != null) {
            prefSet.removePreference(animationOptions);
        }
		
        mLightOptions = (PreferenceCategory) prefSet.findPreference(KEY_LIGHT_OPTIONS);
        mNotificationPulse = (PreferenceScreen) findPreference(KEY_NOTIFICATION_PULSE);
        if (mNotificationPulse != null) {
            if (!getResources().getBoolean(
                com.android.internal.R.bool.config_intrusiveNotificationLed)) {
                mLightOptions.removePreference(mNotificationPulse);
				mNotificationPulse = null;
            } else {
                updateLightPulseDescription();
            }
        }
        mBatteryPulse = (PreferenceScreen) findPreference(KEY_BATTERY_LIGHT);
        if (mBatteryPulse != null) {
            if (!getResources().getBoolean(
                com.android.internal.R.bool.config_showBatteryLedOption)) {
                mLightOptions.removePreference(mBatteryPulse);
				mBatteryPulse = null;
            } else {
                updateBatteryPulseDescription();
            }
        }
        mImmersiveModeState = (CheckBoxPreference) findPreference(KEY_IMMERSIVE_MODE_STATE);
        mImmersiveModeState.setChecked(Settings.System.getInt(getContentResolver(), 
                    Settings.System.GLOBAL_IMMERSIVE_MODE_STATE, 0) == 1);
        mImmersiveModeState.setOnPreferenceChangeListener(this);        
        
        mImmersiveModePref = (ListPreference) prefSet.findPreference(KEY_IMMERSIVE_MODE_STYLE);
        mImmersiveModePref.setOnPreferenceChangeListener(this);
        int immersiveModeValue = Settings.System.getInt(getContentResolver(), Settings.System.GLOBAL_IMMERSIVE_MODE_STYLE, 0);
        mImmersiveModePref.setValue(String.valueOf(immersiveModeValue));
        updateImmersiveModeState(immersiveModeValue);
        updateImmersiveModeSummary(immersiveModeValue);
		
        mClearRecentsLocation = (ListPreference) prefSet.findPreference(KEY_CLEAR_RECENTS_BUTTON_LOCATION);
        mClearRecentsLocation.setOnPreferenceChangeListener(this);
        int LocationValue = Settings.System.getInt(getContentResolver(), Settings.System.CLEAR_ALL_LAYOUT, 0);
        mClearRecentsLocation.setValue(String.valueOf(LocationValue));
        updateClearRecentsLocation(LocationValue);

        mDisplayRotationPreference = (PreferenceScreen) findPreference(KEY_DISPLAY_ROTATION);

        mRecentsUseOmniSwitch = (CheckBoxPreference) prefSet.findPreference(RECENTS_USE_OMNISWITCH);
        try {
            mRecentsUseOmniSwitch.setChecked(Settings.System.getInt(getContentResolver(),
                    Settings.System.RECENTS_USE_OMNISWITCH) == 1);
            mOmniSwitchInitCalled = true;
        } catch(SettingNotFoundException e){
            // if the settings value is unset
        }
        mRecentsUseOmniSwitch.setOnPreferenceChangeListener(this);
        mOmniSwitchSettings = (Preference)
                prefSet.findPreference(OMNISWITCH_START_SETTINGS);
        mOmniSwitchSettings.setEnabled(mRecentsUseOmniSwitch.isChecked());
    }
    
    private void updateImmersiveModeState(int value) {
	if (value >=1) {
	   mImmersiveModeState.setEnabled(true);
        } else {
           mImmersiveModeState.setEnabled(false);	
	}
    }

    private void updateLightPulseDescription() {
        if (Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.NOTIFICATION_LIGHT_PULSE, 0) == 1) {
            mNotificationPulse.setSummary(getString(R.string.enabled));
        } else {
            mNotificationPulse.setSummary(getString(R.string.disabled));
         }
    }

    private void updateBatteryPulseDescription() {
        if (Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.BATTERY_LIGHT_ENABLED, 1) == 1) {
            mBatteryPulse.setSummary(getString(R.string.enabled));
        } else {
            mBatteryPulse.setSummary(getString(R.string.disabled));
        }
     }
	
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mOmniSwitchSettings){
            startActivity(INTENT_OMNISWITCH_SETTINGS);
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        final String key = preference.getKey();
        if (KEY_POWER_CRT_MODE.equals(key)) {
            int value = Integer.parseInt((String) objValue);
            int index = mCrtMode.findIndexOfValue((String) objValue);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.SYSTEM_POWER_CRT_MODE,
                    value);
            mCrtMode.setSummary(mCrtMode.getEntries()[index]);
        } else if (preference == mImmersiveModePref) {
            int immersiveModeValue = Integer.valueOf((String) objValue);
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.GLOBAL_IMMERSIVE_MODE_STYLE, immersiveModeValue);
             updateImmersiveModeSummary(immersiveModeValue);
             updateImmersiveModeState(immersiveModeValue);
        } else if (preference == mClearRecentsLocation) {
            int CrlValue = Integer.valueOf((String) objValue);
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.CLEAR_ALL_LAYOUT, CrlValue);
             updateClearRecentsLocation(CrlValue);
        } else if (preference == mImmersiveModeState) {
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.GLOBAL_IMMERSIVE_MODE_STATE,
                    (Boolean) objValue ? 1 : 0);
        } else if (preference == mRecentsUseOmniSwitch) {
            boolean value = (Boolean) objValue;
            if (value && !isOmniSwitchInstalled()){
                openOmniSwitchNotInstalledWarning();
                return false;
            }

            // if value has never been set before
            if (value && !mOmniSwitchInitCalled){
                openOmniSwitchFirstTimeWarning();
                mOmniSwitchInitCalled = true;
            }

            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.RECENTS_USE_OMNISWITCH, value ? 1 : 0);
            mOmniSwitchSettings.setEnabled(value && isOmniSwitchInstalled());
            mOmniSwitchSettings.setSummary(isOmniSwitchInstalled() ?
                    getResources().getString(R.string.omniswitch_start_settings_summary) :
                    getResources().getString(R.string.omniswitch_not_installed_summary));
        } else {
            return false;
        }

        return true;
    }
	
    @Override
    public void onResume() {
        super.onResume();

        getContentResolver().registerContentObserver(
                Settings.System.getUriFor(Settings.System.ACCELEROMETER_ROTATION), true,
                mAccelerometerRotationObserver);
        updateDisplayRotationPreferenceDescription();
    }	
	
    @Override
    public void onPause() {
        super.onPause();
        getContentResolver().unregisterContentObserver(mAccelerometerRotationObserver);
	}
	
    private void updateDisplayRotationPreferenceDescription() {
        if (mDisplayRotationPreference == null) {
            return;
        }
        PreferenceScreen preference = mDisplayRotationPreference;
        StringBuilder summary = new StringBuilder();
        Boolean rotationEnabled = Settings.System.getInt(getContentResolver(),
                Settings.System.ACCELEROMETER_ROTATION, 0) != 0;
        int mode = Settings.System.getInt(getContentResolver(),
            Settings.System.ACCELEROMETER_ROTATION_ANGLES,
            DisplayRotation.ROTATION_0_MODE|DisplayRotation.ROTATION_90_MODE|DisplayRotation.ROTATION_270_MODE);

        if (!rotationEnabled) {
            summary.append(getString(R.string.display_rotation_disabled));
        } else {
            ArrayList<String> rotationList = new ArrayList<String>();
            String delim = "";
            if ((mode & DisplayRotation.ROTATION_0_MODE) != 0) {
                rotationList.add(ROTATION_ANGLE_0);
            }
            if ((mode & DisplayRotation.ROTATION_90_MODE) != 0) {
                rotationList.add(ROTATION_ANGLE_90);
            }
            if ((mode & DisplayRotation.ROTATION_180_MODE) != 0) {
                rotationList.add(ROTATION_ANGLE_180);
            }
            if ((mode & DisplayRotation.ROTATION_270_MODE) != 0) {
                rotationList.add(ROTATION_ANGLE_270);
            }
            for (int i = 0; i < rotationList.size(); i++) {
                summary.append(delim).append(rotationList.get(i));
                if ((rotationList.size() - i) > 2) {
                    delim = ", ";
                } else {
                    delim = " & ";
                }
            }
            summary.append(" " + getString(R.string.display_rotation_unit));
        }
        preference.setSummary(summary);
    }

    private void updateImmersiveModeSummary(int value) {
        Resources res = getResources();
        if (value == 0) {
            /* expanded desktop deactivated */
            mImmersiveModePref.setSummary(res.getString(R.string.immersive_mode_disabled));
        } else if (value == 1) {
            String statusBarPresent = res.getString(R.string.immersive_mode_summary_status_bar);
            mImmersiveModePref.setSummary(res.getString(R.string.summary_immersive_mode, statusBarPresent));
        } else if (value == 2) {
            String statusBarPresent = res.getString(R.string.immersive_mode_summary_no_status_bar);
            mImmersiveModePref.setSummary(res.getString(R.string.summary_immersive_mode, statusBarPresent));
        }
    }

    private void updateClearRecentsLocation(int value) {
        Resources res = getResources();
        if (value == 1) {
            mClearRecentsLocation.setSummary(res.getString(R.string.crl_top_right));
        } else if (value == 2) {
            mClearRecentsLocation.setSummary(res.getString(R.string.crl_bottom_left));
        } else if (value == 3) {
            mClearRecentsLocation.setSummary(res.getString(R.string.crl_top_left));
		} else {
            mClearRecentsLocation.setSummary(res.getString(R.string.crl_bottom_right));
        }
    }	


    private void openOmniSwitchNotInstalledWarning() {
        new AlertDialog.Builder(getActivity())
                .setTitle(getResources().getString(R.string.omniswitch_not_installed_title))
                .setMessage(getResources().getString(R.string.omniswitch_not_installed_message))
                .setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        }
                }).show();
    }

    private void openOmniSwitchFirstTimeWarning() {
        new AlertDialog.Builder(getActivity())
                .setTitle(getResources().getString(R.string.omniswitch_first_time_title))
                .setMessage(getResources().getString(R.string.omniswitch_first_time_message))
                .setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        }
                }).show();
    }

    private boolean isOmniSwitchInstalled() {
        final PackageManager pm = getPackageManager();
        try {
            pm.getPackageInfo(OMNISWITCH_PACKAGE_NAME, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (NameNotFoundException e) {
            return false;
        }
    }
}
