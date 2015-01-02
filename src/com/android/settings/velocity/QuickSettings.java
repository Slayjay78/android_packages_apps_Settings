
package com.android.settings.velocity;

import android.os.Bundle;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class QuickSettings extends SettingsPreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.quick_settings_panel);
    }
}
