/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings;

import android.os.Bundle;
import android.os.UserHandle;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;

import com.android.internal.util.velocity.DeviceUtils;
import com.android.internal.widget.LockPatternUtils;

import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.R;

import java.util.Locale;

public class StatusBarSettings extends SettingsPreferenceFragment
        implements OnPreferenceChangeListener {

    private static final String PREF_QUICK_PULLDOWN = "quick_pulldown";
    private static final String PREF_BLOCK_ON_SECURE_KEYGUARD = "block_on_secure_keyguard";
    private static final String KEY_STATUS_BAR_CLOCK = "clock_style_pref";

    private PreferenceScreen mClockStyle;
    private ListPreference mQuickPulldown;
    private SwitchPreference mBlockOnSecureKeyguard;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.status_bar_prefs);

        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        mClockStyle = (PreferenceScreen) prefSet.findPreference(KEY_STATUS_BAR_CLOCK);
        updateClockStyleDescription();

        mQuickPulldown = (ListPreference) findPreference(PREF_QUICK_PULLDOWN);
        if (!DeviceUtils.isPhone(getActivity())) {
            prefSet.removePreference(mQuickPulldown);
        } else {
            // Quick Pulldown
            mQuickPulldown.setOnPreferenceChangeListener(this);
            int statusQuickPulldown = Settings.System.getInt(getContentResolver(),
                    Settings.System.STATUS_BAR_QUICK_QS_PULLDOWN, 1);
            mQuickPulldown.setValue(String.valueOf(statusQuickPulldown));
            updateQuickPulldownSummary(statusQuickPulldown);
        }

        final LockPatternUtils lockPatternUtils = new LockPatternUtils(getActivity());
        mBlockOnSecureKeyguard = (SwitchPreference) findPreference(PREF_BLOCK_ON_SECURE_KEYGUARD);
        if (lockPatternUtils.isSecure()) {
            mBlockOnSecureKeyguard.setChecked(Settings.Secure.getInt(getContentResolver(),
                    Settings.Secure.STATUS_BAR_LOCKED_ON_SECURE_KEYGUARD, 1) == 1);
            mBlockOnSecureKeyguard.setOnPreferenceChangeListener(this);
        } else {
            prefSet.removePreference(mBlockOnSecureKeyguard);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateClockStyleDescription();
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == mQuickPulldown) {
            int statusQuickPulldown = Integer.valueOf((String) objValue);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.STATUS_BAR_QUICK_QS_PULLDOWN,
                    statusQuickPulldown);
            updateQuickPulldownSummary(statusQuickPulldown);
            return true;
        } else if (preference == mBlockOnSecureKeyguard) {
            Settings.Secure.putInt(getContentResolver(),
                    Settings.Secure.STATUS_BAR_LOCKED_ON_SECURE_KEYGUARD,
                    (Boolean) objValue ? 1 : 0);
            return true;
        }
        return false;
    }

    private void updateQuickPulldownSummary(int value) {
        Resources res = getResources();

        if (value == 0) {
            // quick pulldown deactivated
            mQuickPulldown.setSummary(res.getString(R.string.quick_pulldown_off));
        } else {
            Locale l = Locale.getDefault();
            boolean isRtl = TextUtils.getLayoutDirectionFromLocale(l) == View.LAYOUT_DIRECTION_RTL;
            String direction = res.getString(value == 2
                    ? (isRtl ? R.string.quick_pulldown_right : R.string.quick_pulldown_left)
                    : (isRtl ? R.string.quick_pulldown_left : R.string.quick_pulldown_right));
            mQuickPulldown.setSummary(res.getString(R.string.summary_quick_pulldown, direction));
        }
    }

    private void updateClockStyleDescription() {
        if (mClockStyle == null) {
            return;
        }
        if (Settings.System.getInt(getContentResolver(),
               Settings.System.STATUS_BAR_CLOCK, 1) == 1) {
            mClockStyle.setSummary(getString(R.string.enabled_string));
        } else {
            mClockStyle.setSummary(getString(R.string.disabled));
        }
    }
}
