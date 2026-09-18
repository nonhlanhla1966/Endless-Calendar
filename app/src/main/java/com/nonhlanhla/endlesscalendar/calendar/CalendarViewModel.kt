package com.nonhlanhla.endlesscalendar.calendar

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nonhlanhla.endlesscalendar.EndlessCalendarApp
import com.nonhlanhla.endlesscalendar.data.AppSettings
import com.nonhlanhla.endlesscalendar.data.Event
import com.nonhlanhla.endlesscalendar.data.EventRepository
import com.nonhlanhla.endlesscalendar.data.UserPreferences
import com.nonhlanhla.endlesscalendar.reminder.ReminderScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class CalendarViewModel(application: Application) : AndroidViewModel(application) {

    private val app: EndlessCalendarApp get() = getApplication()
    val repository: EventRepository get() = app.repository
    private val userPreferences: UserPreferences get() = app.userPreferences

    val settings: StateFlow<AppSettings> = userPreferences.settings.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings()
    )

    var todayAnchor: LocalDate = LocalDate.now()
        private set

    suspend fun occurrencesForRange(rangeStart: Long, rangeEnd: Long) =
        repository.occurrencesInRange(rangeStart, rangeEnd)

    suspend fun searchEvents(query: String) = repository.search(query)

    suspend fun getEvent(id: Long): Event? = repository.getById(id)

    fun saveEvent(event: Event, onSaved: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = if (event.id == 0L) repository.upsert(event) else {
                repository.update(event); event.id
            }
            val saved = event.copy(id = id)
            if (saved.reminderMinutes.isNotBlank()) {
                ReminderScheduler.scheduleForEvent(app, saved)
            } else {
                ReminderScheduler.cancelForEvent(app, id)
            }
            onSaved(id)
        }
    }

    fun deleteEvent(event: Event) {
        viewModelScope.launch {
            repository.delete(event)
            ReminderScheduler.cancelForEvent(app, event.id)
        }
    }
}

class CalendarViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return CalendarViewModel(application) as T
    }
}
