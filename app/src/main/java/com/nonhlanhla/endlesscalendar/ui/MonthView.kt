@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.nonhlanhla.endlesscalendar.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.nonhlanhla.endlesscalendar.calendar.CalendarMath
import com.nonhlanhla.endlesscalendar.calendar.CalendarViewModel
import com.nonhlanhla.endlesscalendar.data.HolidayProvider
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun MonthScreen(navController: NavController, viewModel: CalendarViewModel) {
    val settings by viewModel.settings.collectAsState()
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = CalendarMath.MONTH_CENTER)
    val today = LocalDate.now()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Endless Calendar") },
                actions = {
                    IconButton(onClick = { navController.navigate("search") }) {
                        Icon(Icons.Filled.Search, contentDescription = "Search")
                    }
                    IconButton(onClick = { navController.navigate("day/${today.toEpochDay()}") }) {
                        Icon(Icons.Filled.Today, contentDescription = "Today")
                    }
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            WeekdayHeader(weekStartsMonday = settings.weekStartsMonday)
            LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                items(count = CalendarMath.MONTH_ITEM_COUNT, key = { it }) { index ->
                    val month = remember(index) { CalendarMath.monthForIndex(index) }
                    MonthGrid(
                        month = month,
                        weekStartsMonday = settings.weekStartsMonday,
                        showHolidays = settings.showHolidays,
                        holidayRegion = settings.holidayRegion,
                        viewModel = viewModel,
                        onDayClick = { date -> navController.navigate("day/${date.toEpochDay()}") }
                    )
                }
            }
        }
    }
}

@Composable
private fun WeekdayHeader(weekStartsMonday: Boolean) {
    val labels = if (weekStartsMonday) listOf("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su")
    else listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa")
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
        labels.forEach {
            Text(it, fontSize = 12.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(4.dp))
        }
    }
}

@Composable
private fun MonthGrid(
    month: YearMonth,
    weekStartsMonday: Boolean,
    showHolidays: Boolean,
    holidayRegion: com.nonhlanhla.endlesscalendar.data.HolidayRegion,
    viewModel: CalendarViewModel,
    onDayClick: (LocalDate) -> Unit
) {
    val gridDays = remember(month, weekStartsMonday) { CalendarMath.gridDaysForMonth(month, weekStartsMonday) }
    val rangeStart = remember(gridDays) { CalendarMath.startOfDayMillis(gridDays.first()) }
    val rangeEnd = remember(gridDays) { CalendarMath.endOfDayMillis(gridDays.last()) }

    val occurrences by produceState(initialValue = emptyList<com.nonhlanhla.endlesscalendar.calendar.Occurrence>(), month) {
        value = viewModel.occurrencesForRange(rangeStart, rangeEnd)
    }
    val eventDaysCount = remember(occurrences) {
        occurrences.groupingBy { java.time.Instant.ofEpochMilli(it.startMillis).atZone(java.time.ZoneId.systemDefault()).toLocalDate() }
            .eachCount()
    }
    val holidays = remember(month, showHolidays, holidayRegion) {
        if (showHolidays) {
            (HolidayProvider.holidaysFor(holidayRegion, month.year) +
                HolidayProvider.holidaysFor(holidayRegion, month.year - 1) +
                HolidayProvider.holidaysFor(holidayRegion, month.year + 1))
                .associateBy { it.date }
        } else emptyMap()
    }

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text(
            text = "${month.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${month.year}",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.padding(start = 12.dp, bottom = 4.dp)
        )
        gridDays.chunked(7).forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { date ->
                    val inMonth = YearMonth.from(date) == month
                    val isToday = date == LocalDate.now()
                    val hasEvents = (eventDaysCount[date] ?: 0) > 0
                    val holiday = holidays[date]
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(2.dp)
                            .background(
                                when {
                                    isToday -> MaterialTheme.colorScheme.primary
                                    else -> Color.Transparent
                                },
                                shape = androidx.compose.foundation.shape.CircleShape
                            )
                            .clickable { onDayClick(date) },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = date.dayOfMonth.toString(),
                                fontSize = 13.sp,
                                color = when {
                                    isToday -> MaterialTheme.colorScheme.onPrimary
                                    !inMonth -> MaterialTheme.colorScheme.outline
                                    holiday != null -> MaterialTheme.colorScheme.error
                                    else -> MaterialTheme.colorScheme.onSurface
                                },
                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                            )
                            if (hasEvents) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 1.dp)
                                        .size(5.dp)
                                        .background(
                                            if (isToday) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                            shape = androidx.compose.foundation.shape.CircleShape
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
