package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val type = intent.getStringExtra(NotificationHelper.EXTRA_NOTIFICATION_TYPE) ?: return

        when (type) {
            NotificationHelper.TYPE_SLEEP -> {
                NotificationHelper.showNotification(
                    context,
                    NotificationHelper.TYPE_SLEEP,
                    "FLEXAI: Время сна 🌌",
                    "Настало время отдохнуть и выключить экраны. Глубокий сон зарядит ваш разум для новых открытий!"
                )
                // Schedule same alarm for tomorrow
                val prefs = context.getSharedPreferences("flexai_prefs", Context.MODE_PRIVATE)
                val isSleepEnabled = prefs.getBoolean("sleep_enabled", false)
                if (isSleepEnabled) {
                    val hour = prefs.getInt("sleep_hour", 22)
                    val minute = prefs.getInt("sleep_minute", 0)
                    NotificationHelper.scheduleSleepAlarm(context, hour, minute)
                }
            }
            NotificationHelper.TYPE_REST -> {
                NotificationHelper.showNotification(
                    context,
                    NotificationHelper.TYPE_REST,
                    "FLEXAI: Пора передохнуть 🧘",
                    "Сделайте глубокий вдох и оторвитесь от экрана на 5 минут. Позаботьтесь о вашем теле и глазах!"
                )
                // Schedule next interval
                val prefs = context.getSharedPreferences("flexai_prefs", Context.MODE_PRIVATE)
                val isRestEnabled = prefs.getBoolean("rest_enabled", false)
                if (isRestEnabled) {
                    val interval = prefs.getInt("rest_interval", 60)
                    NotificationHelper.schedulePeriodicRestAlarm(context, interval)
                }
            }
            NotificationHelper.TYPE_SITE -> {
                NotificationHelper.showNotification(
                    context,
                    NotificationHelper.TYPE_SITE,
                    "FLEXAI: Обновление ИИ ⚡",
                    "Добавлены новые нейросетевые инструменты! Посетите flexai-ru.lovable.app и протестируйте их прямо сейчас."
                )
            }
        }
    }
}
