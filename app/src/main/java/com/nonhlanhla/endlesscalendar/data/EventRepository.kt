package com.nonhlanhla.endlesscalendar.data

import com.nonhlanhla.endlesscalendar.calendar.Occurrence
import com.nonhlanhla.endlesscalendar.calendar.RecurrenceExpander
import kotlinx.coroutines.flow.Flow

class EventRepository(private val dao: EventDao) {

    val allEvents: Flow<List<Event>> get() = dao.getAllFlow()

    suspend fun upsert(event: Event): Long = dao.insert(event)

    suspend fun update(event: Event) = dao.update(event)

    suspend fun delete(event: Event) = dao.delete(event)

    suspend fun deleteById(id: Long) = dao.deleteById(id)

    suspend fun getById(id: Long): Event? = dao.getById(id)

    suspend fun getAllOnce(): List<Event> = dao.getAllOnce()

    suspend fun occurrencesInRange(rangeStart: Long, rangeEnd: Long): List<Occurrence> {
        val candidates = dao.getRelevantForRange(rangeStart, rangeEnd)
        return RecurrenceExpander.expandAll(candidates, rangeStart, rangeEnd)
    }

    suspend fun search(query: String): List<Event> = dao.search(query)
}
