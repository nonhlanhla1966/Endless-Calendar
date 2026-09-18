package com.nonhlanhla.endlesscalendar.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "endless_calendar_prefs")

enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,
    val holidayRegion: HolidayRegion = HolidayRegion.NONE,
    val showHolidays: Boolean = false,
    val defaultReminderMinutes: Int = 30,
    val weekStartsMonday: Boolean = true
)

class UserPreferences(private val context: Context) {

    private object Keys {
        val THEME = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val HOLIDAY_REGION = stringPreferencesKey("holiday_region")
        val SHOW_HOLIDAYS = booleanPreferencesKey("show_holidays")
        val DEFAULT_REMINDER = intPreferencesKey("default_reminder_minutes")
        val WEEK_START_MONDAY = booleanPreferencesKey("week_start_monday")
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            themeMode = prefs[Keys.THEME]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.SYSTEM,
            dynamicColor = prefs[Keys.DYNAMIC_COLOR] ?: true,
            holidayRegion = prefs[Keys.HOLIDAY_REGION]?.let { runCatching { HolidayRegion.valueOf(it) }.getOrNull() } ?: HolidayRegion.NONE,
            showHolidays = prefs[Keys.SHOW_HOLIDAYS] ?: false,
            defaultReminderMinutes = prefs[Keys.DEFAULT_REMINDER] ?: 30,
            weekStartsMonday = prefs[Keys.WEEK_START_MONDAY] ?: true
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) = context.dataStore.edit { it[Keys.THEME] = mode.name }
    suspend fun setDynamicColor(enabled: Boolean) = context.dataStore.edit { it[Keys.DYNAMIC_COLOR] = enabled }
    suspend fun setHolidayRegion(region: HolidayRegion) = context.dataStore.edit { it[Keys.HOLIDAY_REGION] = region.name }
    suspend fun setShowHolidays(show: Boolean) = context.dataStore.edit { it[Keys.SHOW_HOLIDAYS] = show }
    suspend fun setDefaultReminderMinutes(minutes: Int) = context.dataStore.edit { it[Keys.DEFAULT_REMINDER] = minutes }
    suspend fun setWeekStartsMonday(monday: Boolean) = context.dataStore.edit { it[Keys.WEEK_START_MONDAY] = monday }
}
