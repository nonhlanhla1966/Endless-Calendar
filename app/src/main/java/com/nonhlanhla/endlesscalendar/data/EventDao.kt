package com.nonhlanhla.endlesscalendar.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: Event): Long

    @Update
    suspend fun update(event: Event)

    @Delete
    suspend fun delete(event: Event)

    @Query("DELETE FROM events WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM events WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): Event?

    @Query("SELECT * FROM events ORDER BY startMillis ASC")
    fun getAllFlow(): Flow<List<Event>>

    @Query("SELECT * FROM events ORDER BY startMillis ASC")
    suspend fun getAllOnce(): List<Event>

    /**
     * Non-recurring events overlapping the range, plus every recurring event whose
     * own definition could plausibly produce an occurrence in range (recurrence
     * expansion itself happens in Kotlin, since SQLite can't evaluate RRULEs).
     */
    @Query(
        """
        SELECT * FROM events
        WHERE (recurrenceRule != '' )
           OR (startMillis <= :rangeEnd AND endMillis >= :rangeStart)
        """
    )
    suspend fun getRelevantForRange(rangeStart: Long, rangeEnd: Long): List<Event>

    @Query(
        """
        SELECT * FROM events
        WHERE title LIKE '%' || :query || '%'
           OR notes LIKE '%' || :query || '%'
           OR location LIKE '%' || :query || '%'
        ORDER BY startMillis ASC
        """
    )
    suspend fun search(query: String): List<Event>
}
