package org.garywzh.quumiibox.ui;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.umeng.analytics.MobclickAgent;

import org.garywzh.quumiibox.R;
import org.garywzh.quumiibox.common.UserState;

public class SettingsActivity extends AppCompatActivity {
    private final PrefsFragment mFragment = new PrefsFragment();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getFragmentManager().beginTransaction().replace(android.R.id.content, mFragment).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mFragment.onActivityResult(requestCode, resultCode, data);
    }

    public static class PrefsFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener {

        private static final String PREF_KEY_CATEGORY_USER = "user";
        private static final String PREF_KEY_LOGIN = "login";
        private static final String PREF_KEY_LOGOUT = "logout";
        private static final int REQ_LOGIN = 0;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.pref_general);
            init();
        }

        private void init() {
            final PreferenceCategory user = (PreferenceCategory) findPreference(PREF_KEY_CATEGORY_USER);
            final Preference loginPref = findPreference(PREF_KEY_LOGIN);
            final Preference logoutPref = findPreference(PREF_KEY_LOGOUT);

            if (UserState.getInstance().isLoggedIn()) {
                user.removePreference(loginPref);
                logoutPref.setOnPreferenceClickListener(this);
            } else {
                user.removePreference(logoutPref);
                loginPref.setOnPreferenceClickListener(this);
            }
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            switch (requestCode) {
                case REQ_LOGIN:
                    if (resultCode == RESULT_OK) {
                        getActivity().recreate();
                    }
                    break;
            }
            super.onActivityResult(requestCode, resultCode, data);
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            switch (preference.getKey()) {
                case PREF_KEY_LOGIN:
                    startActivityForResult(new Intent(getActivity(), LoginActivity.class), 0);
                    return true;
                case PREF_KEY_LOGOUT:
                    UserState.getInstance().logout();
                    getActivity().recreate();
                    return true;
            }
            return false;
        }
    }
}