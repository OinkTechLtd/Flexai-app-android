package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.database.SavedPage
import com.example.data.repository.SavedPageRepository
import com.example.notification.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FlexViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = SavedPageRepository(database.savedPageDao())

    val savedPages: StateFlow<List<SavedPage>> = repository.allSavedPages
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    // Shared preferences for reminders
    private val prefs = application.getSharedPreferences("flexai_prefs", Context.MODE_PRIVATE)

    private val _sleepEnabled = MutableStateFlow(prefs.getBoolean("sleep_enabled", false))
    val sleepEnabled: StateFlow<Boolean> = _sleepEnabled.asStateFlow()

    private val _sleepHour = MutableStateFlow(prefs.getInt("sleep_hour", 22))
    val sleepHour: StateFlow<Int> = _sleepHour.asStateFlow()

    private val _sleepMinute = MutableStateFlow(prefs.getInt("sleep_minute", 0))
    val sleepMinute: StateFlow<Int> = _sleepMinute.asStateFlow()

    private val _restEnabled = MutableStateFlow(prefs.getBoolean("rest_enabled", false))
    val restEnabled: StateFlow<Boolean> = _restEnabled.asStateFlow()

    private val _restInterval = MutableStateFlow(prefs.getInt("rest_interval", 60)) // in minutes
    val restInterval: StateFlow<Int> = _restInterval.asStateFlow()

    private val connectivityManager =
        application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            _isOnline.value = true
        }

        override fun onLost(network: Network) {
            _isOnline.value = false
        }
    }

    init {
        // Initial online check
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        _isOnline.value = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

        // Register callback for network changes
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, networkCallback)

        // Pre-create notification channels on start
        NotificationHelper.createNotificationChannels(application)
    }

    override fun onCleared() {
        super.onCleared()
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun savePage(url: String, title: String, htmlContent: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isSaving.value = true
            try {
                val correctedTitle = title.trim().ifEmpty { "Saved Page" }
                val newPage = SavedPage(
                    url = url,
                    title = correctedTitle,
                    htmlContent = htmlContent
                )
                repository.insert(newPage)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun deletePage(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteById(id)
        }
    }

    fun toggleSleepReminder(enabled: Boolean, hour: Int = _sleepHour.value, minute: Int = _sleepMinute.value) {
        _sleepEnabled.value = enabled
        _sleepHour.value = hour
        _sleepMinute.value = minute

        prefs.edit().apply {
            putBoolean("sleep_enabled", enabled)
            putInt("sleep_hour", hour)
            putInt("sleep_minute", minute)
            apply()
        }

        val context = getApplication<Application>()
        if (enabled) {
            NotificationHelper.scheduleSleepAlarm(context, hour, minute)
        } else {
            NotificationHelper.cancelSleepAlarm(context)
        }
    }

    fun toggleRestReminder(enabled: Boolean, intervalMinutes: Int = _restInterval.value) {
        _restEnabled.value = enabled
        _restInterval.value = intervalMinutes

        prefs.edit().apply {
            putBoolean("rest_enabled", enabled)
            putInt("rest_interval", intervalMinutes)
            apply()
        }

        val context = getApplication<Application>()
        if (enabled) {
            NotificationHelper.schedulePeriodicRestAlarm(context, intervalMinutes)
        } else {
            NotificationHelper.cancelPeriodicRestAlarm(context)
        }
    }

    fun triggerSimulatedNotification(type: String) {
        val context = getApplication<Application>()
        when (type) {
            "sleep" -> {
                NotificationHelper.showNotification(
                    context,
                    NotificationHelper.TYPE_SLEEP,
                    "FLEXAI: Время сна 🌌",
                    "Напоминание: Время восстановить силы. Закройте вкладки, расслабьтесь и погрузитесь в сон."
                )
            }
            "rest" -> {
                NotificationHelper.showNotification(
                    context,
                    NotificationHelper.TYPE_REST,
                    "FLEXAI: Минутка здоровья 🧘",
                    "Вы провели много времени перед экраном. Встаньте, пройдитесь по комнате и разомните шею."
                )
            }
            "site" -> {
                NotificationHelper.showNotification(
                    context,
                    NotificationHelper.TYPE_SITE,
                    "FLEXAI: Новые наработки ⚡",
                    "На flexai-ru.lovable.app опубликованы новые инструменты автоматизации ИИ. Попробуйте их!"
                )
            }
        }
    }
}
