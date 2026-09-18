package com.nonhlanhla.endlesscalendar.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A single stored event. Recurring events are stored once and expanded into
 * occurrences at query time by [com.nonhlanhla.endlesscalendar.calendar.RecurrenceExpander].
 */
@Entity(tableName = "events")
data class Event(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val notes: String = "",
    val location: String = "",
    val startMillis: Long,
    val endMillis: Long,
    val allDay: Boolean = false,
    val timeZoneId: String = "UTC",
    val colorTag: Int = 0,
    /** Simplified RRULE, e.g. "FREQ=WEEKLY;INTERVAL=1;BYDAY=MO,WE;COUNT=10". Empty = not recurring. */
    val recurrenceRule: String = "",
    /** Comma separated minutes-before-start values, e.g. "0,30,1440". */
    val reminderMinutes: String = "",
    /** If set, this event should also be mirrored into the device calendar with this id. */
    val deviceCalendarEventId: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
