package com.nonhlanhla.endlesscalendar.sync

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.provider.CalendarContract
import com.nonhlanhla.endlesscalendar.data.Event

data class DeviceCalendarInfo(val id: Long, val name: String, val accountName: String)

data class DeviceEvent(
    val id: Long,
    val title: String,
    val startMillis: Long,
    val endMillis: Long,
    val calendarId: Long
)

/** Reads and writes the device's own calendars via CalendarContract. Requires READ/WRITE_CALENDAR. */
class DeviceCalendarSync(private val context: Context) {

    fun listCalendars(): List<DeviceCalendarInfo> {
        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
            CalendarContract.Calendars.ACCOUNT_NAME
        )
        val result = mutableListOf<DeviceCalendarInfo>()
        context.contentResolver.query(CalendarContract.Calendars.CONTENT_URI, projection, null, null, null)?.use { c ->
            while (c.moveToNext()) {
                result.add(DeviceCalendarInfo(c.getLong(0), c.getString(1) ?: "", c.getString(2) ?: ""))
            }
        }
        return result
    }

    fun eventsInRange(rangeStart: Long, rangeEnd: Long): List<DeviceEvent> {
        val projection = arrayOf(
            CalendarContract.Instances.EVENT_ID,
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.END,
            CalendarContract.Instances.CALENDAR_ID
        )
        val builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
        ContentUris.appendId(builder, rangeStart)
        ContentUris.appendId(builder, rangeEnd)
        val result = mutableListOf<DeviceEvent>()
        context.contentResolver.query(builder.build(), projection, null, null, null)?.use { c ->
            while (c.moveToNext()) {
                result.add(
                    DeviceEvent(
                        id = c.getLong(0),
                        title = c.getString(1) ?: "",
                        startMillis = c.getLong(2),
                        endMillis = c.getLong(3),
                        calendarId = c.getLong(4)
                    )
                )
            }
        }
        return result
    }

    /** Inserts [event] into the device's [calendarId] calendar; returns the new device event id. */
    fun insertIntoDeviceCalendar(event: Event, calendarId: Long): Long? {
        val values = ContentValues().apply {
            put(CalendarContract.Events.DTSTART, event.startMillis)
            put(CalendarContract.Events.DTEND, event.endMillis)
            put(CalendarContract.Events.TITLE, event.title)
            put(CalendarContract.Events.DESCRIPTION, event.notes)
            put(CalendarContract.Events.EVENT_LOCATION, event.location)
            put(CalendarContract.Events.CALENDAR_ID, calendarId)
            put(CalendarContract.Events.EVENT_TIMEZONE, event.timeZoneId)
            put(CalendarContract.Events.ALL_DAY, if (event.allDay) 1 else 0)
        }
        val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
        return uri?.lastPathSegment?.toLongOrNull()
    }

    fun deleteFromDeviceCalendar(deviceEventId: Long) {
        val uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, deviceEventId)
        context.contentResolver.delete(uri, null, null)
    }
}
