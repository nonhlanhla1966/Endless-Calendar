package com.nonhlanhla.endlesscalendar.widget

import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.nonhlanhla.endlesscalendar.R
import com.nonhlanhla.endlesscalendar.calendar.CalendarMath
import com.nonhlanhla.endlesscalendar.calendar.Occurrence
import com.nonhlanhla.endlesscalendar.calendar.RecurrenceExpander
import com.nonhlanhla.endlesscalendar.data.AppDatabase
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Locale

class AgendaWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory = AgendaRemoteViewsFactory(this)
}

private class AgendaRemoteViewsFactory(private val context: android.content.Context) : RemoteViewsService.RemoteViewsFactory {

    private var occurrences: List<Occurrence> = emptyList()

    override fun onCreate() {}

    override fun onDataSetChanged() {
        val today = LocalDate.now()
        val rangeStart = CalendarMath.startOfDayMillis(today)
        val rangeEnd = CalendarMath.endOfDayMillis(today)
        occurrences = runBlocking {
            val dao = AppDatabase.getInstance(context).eventDao()
            val candidates = dao.getRelevantForRange(rangeStart, rangeEnd)
            RecurrenceExpander.expandAll(candidates, rangeStart, rangeEnd)
        }
    }

    override fun onDestroy() {}

    override fun getCount(): Int = occurrences.size

    override fun getViewAt(position: Int): RemoteViews {
        val occ = occurrences[position]
        val views = RemoteViews(context.packageName, R.layout.agenda_widget_item)
        val timeText = if (occ.event.allDay) "All day" else
            SimpleDateFormat("h:mm a", Locale.getDefault()).format(occ.startMillis)
        views.setTextViewText(R.id.item_time, timeText)
        views.setTextViewText(R.id.item_title, occ.event.title)
        return views
    }

    override fun getLoadingView(): RemoteViews? = null
    override fun getViewTypeCount(): Int = 1
    override fun getItemId(position: Int): Long = occurrences[position].event.id
    override fun hasStableIds(): Boolean = true
}
