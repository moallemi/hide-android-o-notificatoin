package me.moallemi.hidebgnotification;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.provider.Settings;

import static me.moallemi.hidebgnotification.AppNotificationListenerService.FILTER_INTENT;

/**
 * Created by Reza Moallemi on 9/18/17.
 */

public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {

    private static final String KEY_HIDE_ICON = "hide_launcher_icon";
    private static final String KEY_ACCESS_GRANTED = "notification_permission";

    private SwitchPreference mNotificationPermissionPreference;
    private SwitchPreference mHideAppIconPreferences;

    private SharedPreferences mSharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings);

        mSharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);

        mNotificationPermissionPreference = (SwitchPreference) findPreference(KEY_ACCESS_GRANTED);
        mHideAppIconPreferences = (SwitchPreference) findPreference(KEY_HIDE_ICON);

        mNotificationPermissionPreference.setOnPreferenceChangeListener(this);
        mHideAppIconPreferences.setOnPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        boolean accessGranted = isNotificationAccessGranted();
        if (!accessGranted) {
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putBoolean(KEY_ACCESS_GRANTED, false);
            editor.apply();

            AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
            builder.setMessage(R.string.permission_request_description);
            builder.setCancelable(true);
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
                }
            });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();

        } else {
            if (!mSharedPreferences.getBoolean(KEY_ACCESS_GRANTED, false)) {

                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putBoolean(KEY_ACCESS_GRANTED, true);
                editor.apply();

                Intent intent = new Intent(FILTER_INTENT);
                intent.putExtra("action", "snooze");
                getContext().sendBroadcast(intent);
            }
        }

        mNotificationPermissionPreference.setChecked(accessGranted);
        mNotificationPermissionPreference.setSummary(accessGranted ? getString(R.string.permission_granted) : getString(R.string.permission_not_granted));
        mHideAppIconPreferences.setChecked(mSharedPreferences.getBoolean(KEY_HIDE_ICON, false));
    }

    public boolean isNotificationAccessGranted() {
        ContentResolver contentResolver = getContext().getContentResolver();
        String enabledNotificationListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners");

        return enabledNotificationListeners != null && enabledNotificationListeners.contains(getContext().getPackageName());
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {

        switch (preference.getKey()) {
            case KEY_HIDE_ICON:
                return onHideIconChanged();
            case KEY_ACCESS_GRANTED:
                return onPermissionAccessChanged();
        }

        return false;
    }

    private boolean onPermissionAccessChanged() {
        if (isNotificationAccessGranted()) {
            startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
            builder.setMessage(R.string.permission_request_description);
            builder.setCancelable(true);
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
                }
            });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
        return false;
    }

    private boolean onHideIconChanged() {
        if (!mHideAppIconPreferences.isChecked()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage(R.string.hide_icon_dialog_msg);
            builder.setTitle(R.string.hide_icon_dialog_title);
            builder.setCancelable(true);
            builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    PackageManager pkg = getContext().getPackageManager();
                    pkg.setComponentEnabledSetting(new ComponentName(getContext(), MainActivity.class), PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                            PackageManager.DONT_KILL_APP);

                    mHideAppIconPreferences.setChecked(true);
                    SharedPreferences.Editor editor = mSharedPreferences.edit();
                    editor.putBoolean(KEY_HIDE_ICON, true);
                    editor.apply();
                }
            });

            builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    mHideAppIconPreferences.setChecked(false);
                    SharedPreferences.Editor editor = mSharedPreferences.edit();
                    editor.putBoolean(KEY_HIDE_ICON, true);
                    editor.apply();
                }
            });

            builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    PackageManager pkg = getContext().getPackageManager();
                    pkg.setComponentEnabledSetting(new ComponentName(getContext(), MainActivity.class), PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                            PackageManager.DONT_KILL_APP);

                    mHideAppIconPreferences.setChecked(false);
                    SharedPreferences.Editor editor = mSharedPreferences.edit();
                    editor.putBoolean(KEY_HIDE_ICON, false);
                    editor.apply();
                }
            });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();

        } else {
            PackageManager pkg = getContext().getPackageManager();
            pkg.setComponentEnabledSetting(new ComponentName(getContext(), MainActivity.class), PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);

            mHideAppIconPreferences.setChecked(false);
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putBoolean(KEY_HIDE_ICON, false);
            editor.apply();
        }

        return true;
    }
}