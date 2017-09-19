package me.moallemi.hidebgnotification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

/**
 * Created by Reza Moallemi on 9/18/17.
 */

public class AppNotificationListenerService extends NotificationListenerService {

    public static final String FILTER_INTENT = "me.moallemi.hidebgnotification.NOTIFICATION_LISTENER_INTENT";
    private static final String NOTIFICATION_INTENT_KEY = "android.title";
    private NotificationListenerBroadcastReceiver notificationListenerBroadcastReceiver;

    @Override
    public void onCreate() {
        super.onCreate();

        notificationListenerBroadcastReceiver = new NotificationListenerBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(FILTER_INTENT);
        registerReceiver(notificationListenerBroadcastReceiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (notificationListenerBroadcastReceiver != null) {
            unregisterReceiver(notificationListenerBroadcastReceiver);
        }
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (sbn == null)
            return;

        if (sbn.getPackageName().equals("android")) {
            String key = sbn.getNotification().extras.getString(NOTIFICATION_INTENT_KEY);
            if (key == null) {
                return;
            }

            String notificationContent = getString(R.string.notification_content_singular);
            String notificationContentPlural = getString(R.string.notification_content_plural);

            if (key.contains(notificationContent) || key.contains(notificationContentPlural)) {
                snoozeNotification(sbn.getKey(), 9999999L);
            }
        }
    }

    class NotificationListenerBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getStringExtra("action").equals("snooze")) {
                for (StatusBarNotification sbn : getActiveNotifications()) {
                    if (sbn.getPackageName().equals("android")) {
                        String key = sbn.getNotification().extras.getString(NOTIFICATION_INTENT_KEY);
                        if (key == null) {
                            return;
                        }

                        String notificationContent = getString(R.string.notification_content_singular);
                        String notificationContentPlural = getString(R.string.notification_content_plural);

                        if (key.contains(notificationContent) || key.contains(notificationContentPlural)) {
                            snoozeNotification(sbn.getKey(), 9999999L);
                        }
                    }
                }
            }
        }
    }
}
