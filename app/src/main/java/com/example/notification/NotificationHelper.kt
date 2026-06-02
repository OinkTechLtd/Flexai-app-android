package com.example.notification

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import java.util.Calendar

object NotificationHelper {
    const val SLEEP_CHANNEL_ID = "flexai_sleep_notifications"
    const val SITE_CHANNEL_ID = "flexai_site_notifications"
    
    const val EXTRA_NOTIFICATION_TYPE = "notification_type"
    const val TYPE_SLEEP = "sleep"
    const val TYPE_REST = "rest"
    const val TYPE_SITE = "site"

    const val SLEEP_ALARM_ID = 1001
    const val REST_ALARM_ID = 1002

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val sleepChannelName = "Reminders (Sleep & Rest)"
            val sleepChannelDesc = "Alerts reminding you when it's time to sleep or take a break"
            val sleepImportance = NotificationManager.IMPORTANCE_DEFAULT
            val sleepChannel = NotificationChannel(SLEEP_CHANNEL_ID, sleepChannelName, sleepImportance).apply {
                description = sleepChannelDesc
                enableLights(true)
                lightColor = 0xFF00FF9D.toInt() // Cyan/Green highlight
            }

            val siteChannelName = "FLEXAI Updates"
            val siteChannelDesc = "Notifications regarding site content and new AI tools"
            val siteImportance = NotificationManager.IMPORTANCE_DEFAULT
            val siteChannel = NotificationChannel(SITE_CHANNEL_ID, siteChannelName, siteImportance).apply {
                description = siteChannelDesc
                enableLights(true)
                lightColor = 0xFF8F12FD.toInt() // Purple/Gold highlight
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(sleepChannel)
            manager.createNotificationChannel(siteChannel)
        }
    }

    @SuppressLint("MissingPermission")
    fun showNotification(context: Context, type: String, title: String, message: String) {
        createNotificationChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 
            0, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = if (type == TYPE_SITE) SITE_CHANNEL_ID else SLEEP_CHANNEL_ID
        
        // Let's verify we use an icon. We will fall back to standard icons.
        // Android system uses application launcher icon or default white icon.
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_popup_reminder) // safe fallback small icon
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            val notificationId = when (type) {
                TYPE_SLEEP -> 2001
                TYPE_REST -> 2002
                else -> 2003 + (System.currentTimeMillis() % 1000).toInt()
            }
            notificationManager.notify(notificationId, builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun scheduleSleepAlarm(context: Context, hour: Int, minute: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra(EXTRA_NOTIFICATION_TYPE, TYPE_SLEEP)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            SLEEP_ALARM_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    fun cancelSleepAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            SLEEP_ALARM_ID,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    fun schedulePeriodicRestAlarm(context: Context, intervalMinutes: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra(EXTRA_NOTIFICATION_TYPE, TYPE_REST)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REST_ALARM_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = System.currentTimeMillis() + intervalMinutes * 60 * 1000L

        // In iOS-like interval scheduling, we schedule next alarm inside the receiver itself.
        // This is highly compatible and robust for deep doze modes.
        try {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cancelPeriodicRestAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REST_ALARM_ID,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }
}
