@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.nonhlanhla.endlesscalendar.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.nonhlanhla.endlesscalendar.calendar.CalendarMath
import com.nonhlanhla.endlesscalendar.calendar.CalendarViewModel
import com.nonhlanhla.endlesscalendar.calendar.Occurrence
import com.nonhlanhla.endlesscalendar.data.HolidayProvider
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun DayAgendaScreen(navController: NavController, viewModel: CalendarViewModel, startEpochDay: Long) {
    val settings by viewModel.settings.collectAsState()
    val startDate = remember(startEpochDay) { LocalDate.ofEpochDay(startEpochDay) }
    val startIndex = remember(startDate) { CalendarMath.indexForDay(startDate) }
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = startIndex)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agenda") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                val date = CalendarMath.dayForIndex(listState.firstVisibleItemIndex)
                navController.navigate("eventEdit?eventId=-1&dateMillis=${CalendarMath.startOfDayMillis(date)}")
            }) {
                Icon(Icons.Filled.Add, contentDescription = "Add event")
            }
        }
    ) { padding ->
        LazyColumn(state = listState, modifier = Modifier.padding(padding).fillMaxSize()) {
            items(count = CalendarMath.DAY_ITEM_COUNT, key = { it }) { index ->
                val date = remember(index) { CalendarMath.dayForIndex(index) }
                DayCard(date, settings.showHolidays, settings.holidayRegion, viewModel) { eventId ->
                    navController.navigate("eventEdit?eventId=$eventId&dateMillis=0")
                }
            }
        }
    }
}

@Composable
private fun DayCard(
    date: LocalDate,
    showHolidays: Boolean,
    holidayRegion: com.nonhlanhla.endlesscalendar.data.HolidayRegion,
    viewModel: CalendarViewModel,
    onEventClick: (Long) -> Unit
) {
    val rangeStart = remember(date) { CalendarMath.startOfDayMillis(date) }
    val rangeEnd = remember(date) { CalendarMath.endOfDayMillis(date) }
    val occurrences by produceState(initialValue = emptyList<Occurrence>(), date) {
        value = viewModel.occurrencesForRange(rangeStart, rangeEnd)
    }
    val holiday = remember(date, showHolidays, holidayRegion) {
        if (showHolidays) HolidayProvider.holidaysFor(holidayRegion, date.year).firstOrNull { it.date == date } else null
    }
    val isToday = date == LocalDate.now()

    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        CircleShape
                    ),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text(
                    date.dayOfMonth.toString(),
                    color = if (isToday) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.padding(6.dp))
            Column {
                Text(
                    date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault()) +
                        ", " + date.month.getDisplayName(TextStyle.FULL, Locale.getDefault()) + " " + date.year,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
                holiday?.let {
                    Text(it.name, fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                }
            }
        }

        if (occurrences.isEmpty()) {
            Text(
                "No events",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(start = 46.dp, top = 4.dp)
            )
        } else {
            occurrences.sortedBy { it.startMillis }.forEach { occ ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 46.dp, top = 4.dp)
                        .clickable { onEventClick(occ.event.id) },
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Box(
                        Modifier
                            .size(8.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                    )
                    Spacer(Modifier.padding(4.dp))
                    Column {
                        val timeText = if (occ.event.allDay) "All day" else
                            DateTimeFormatter.ofPattern("h:mm a").format(
                                java.time.Instant.ofEpochMilli(occ.startMillis).atZone(java.time.ZoneId.systemDefault())
                            )
                        Text(occ.event.title, fontSize = 13.sp)
                        Text(timeText, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
        }
    }
}
