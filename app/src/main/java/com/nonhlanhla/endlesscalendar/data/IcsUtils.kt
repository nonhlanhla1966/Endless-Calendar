package com.nonhlanhla.endlesscalendar.data

import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/** Minimal but real VCALENDAR/VEVENT reader and writer (subset of RFC 5545). */
object IcsUtils {

    private val UTC_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")

    fun export(events: List<Event>): String {
        val sb = StringBuilder()
        sb.append("BEGIN:VCALENDAR\r\n")
        sb.append("VERSION:2.0\r\n")
        sb.append("PRODID:-//EndlessCalendar//EN\r\n")
        for (e in events) {
            sb.append("BEGIN:VEVENT\r\n")
            sb.append("UID:${e.id}-${e.createdAt}@endlesscalendar\r\n")
            sb.append("SUMMARY:${escape(e.title)}\r\n")
            if (e.notes.isNotBlank()) sb.append("DESCRIPTION:${escape(e.notes)}\r\n")
            if (e.location.isNotBlank()) sb.append("LOCATION:${escape(e.location)}\r\n")
            sb.append("DTSTART:${formatUtc(e.startMillis)}\r\n")
            sb.append("DTEND:${formatUtc(e.endMillis)}\r\n")
            if (e.recurrenceRule.isNotBlank()) sb.append("RRULE:${e.recurrenceRule}\r\n")
            sb.append("END:VEVENT\r\n")
        }
        sb.append("END:VCALENDAR\r\n")
        return sb.toString()
    }

    fun import(icsText: String): List<Event> {
        val events = mutableListOf<Event>()
        var title = ""
        var notes = ""
        var location = ""
        var start: Long? = null
        var end: Long? = null
        var rrule = ""
        var inEvent = false

        for (rawLine in icsText.lines()) {
            val line = rawLine.trim()
            when {
                line == "BEGIN:VEVENT" -> {
                    inEvent = true; title = ""; notes = ""; location = ""; start = null; end = null; rrule = ""
                }
                line == "END:VEVENT" -> {
                    inEvent = false
                    val s = start
                    val en = end ?: s
                    if (s != null && en != null) {
                        events.add(
                            Event(
                                title = title.ifBlank { "Untitled" },
                                notes = notes,
                                location = location,
                                startMillis = s,
                                endMillis = en,
                                recurrenceRule = rrule
                            )
                        )
                    }
                }
                inEvent && line.startsWith("SUMMARY:") -> title = unescape(line.removePrefix("SUMMARY:"))
                inEvent && line.startsWith("DESCRIPTION:") -> notes = unescape(line.removePrefix("DESCRIPTION:"))
                inEvent && line.startsWith("LOCATION:") -> location = unescape(line.removePrefix("LOCATION:"))
                inEvent && line.startsWith("DTSTART") -> start = parseIcsDate(line.substringAfter(":"))
                inEvent && line.startsWith("DTEND") -> end = parseIcsDate(line.substringAfter(":"))
                inEvent && line.startsWith("RRULE:") -> rrule = line.removePrefix("RRULE:")
            }
        }
        return events
    }

    private fun formatUtc(millis: Long): String =
        UTC_FORMAT.format(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC))

    private fun parseIcsDate(value: String): Long? = try {
        when {
            value.endsWith("Z") -> Instant.from(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").parse(value)).toEpochMilli()
            value.contains("T") -> Instant.from(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss").withZone(ZoneOffset.UTC).parse(value)).toEpochMilli()
            else -> Instant.from(DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneOffset.UTC).parse(value)).toEpochMilli()
        }
    } catch (t: Throwable) {
        null
    }

    private fun escape(s: String) = s.replace("\\", "\\\\").replace(",", "\\,").replace(";", "\\;").replace("\n", "\\n")
    private fun unescape(s: String) = s.replace("\\n", "\n").replace("\\,", ",").replace("\\;", ";").replace("\\\\", "\\")
}
