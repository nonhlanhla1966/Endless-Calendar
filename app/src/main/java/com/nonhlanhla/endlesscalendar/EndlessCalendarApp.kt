package com.nonhlanhla.endlesscalendar

import android.app.Application
import com.nonhlanhla.endlesscalendar.data.AppDatabase
import com.nonhlanhla.endlesscalendar.data.EventRepository
import com.nonhlanhla.endlesscalendar.data.UserPreferences
import com.nonhlanhla.endlesscalendar.reminder.NotificationHelper

class EndlessCalendarApp : Application() {

    lateinit var repository: EventRepository
        private set
    lateinit var userPreferences: UserPreferences
        private set

    override fun onCreate() {
        super.onCreate()
        val db = AppDatabase.getInstance(this)
        repository = EventRepository(db.eventDao())
        userPreferences = UserPreferences(this)
        NotificationHelper.ensureChannel(this)
    }
}
