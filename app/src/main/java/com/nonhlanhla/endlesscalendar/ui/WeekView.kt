package com.nonhlanhla.endlesscalendar.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.nonhlanhla.endlesscalendar.calendar.CalendarMath
import com.nonhlanhla.endlesscalendar.calendar.CalendarViewModel
import com.nonhlanhla.endlesscalendar.calendar.Occurrence
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

private val HOUR_HEIGHT = 56.dp

@Composable
fun WeekScreen(navController: NavController, viewModel: CalendarViewModel) {
    val settings by viewModel.settings.collectAsState()
    val pagerState = rememberPagerState(
        initialPage = CalendarMath.WEEK_CENTER,
        pageCount = { CalendarMath.WEEK_ITEM_COUNT }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Week") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        HorizontalPager(state = pagerState, modifier = Modifier.padding(padding).fillMaxSize()) { page ->
            val weekStart = remember(page, settings.weekStartsMonday) {
                CalendarMath.weekStartForIndex(page, settings.weekStartsMonday)
            }
            WeekPage(weekStart, viewModel) { date -> navController.navigate("day/${date.toEpochDay()}") }
        }
    }
}

@Composable
private fun WeekPage(weekStart: LocalDate, viewModel: CalendarViewModel, onDayClick: (LocalDate) -> Unit) {
    val days = remember(weekStart) { (0..6).map { weekStart.plusDays(it.toLong()) } }
    val rangeStart = remember(weekStart) { CalendarMath.startOfDayMillis(days.first()) }
    val rangeEnd = remember(weekStart) { CalendarMath.endOfDayMillis(days.last()) }
    val occurrences by produceState(initialValue = emptyList<Occurrence>(), weekStart) {
        value = viewModel.occurrencesForRange(rangeStart, rangeEnd)
    }
    val zone = ZoneId.systemDefault()

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            Box(Modifier.width(40.dp))
            days.forEach { date ->
                Column(
                    modifier = Modifier.weight(1f).clickable { onDayClick(date) },
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                ) {
                    Text(date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()), fontSize = 11.sp)
                    Text(
                        date.dayOfMonth.toString(),
                        fontWeight = if (date == LocalDate.now()) FontWeight.Bold else FontWeight.Normal,
                        color = if (date == LocalDate.now()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
        val scrollState = rememberScrollState()
        Row(Modifier.fillMaxSize().verticalScroll(scrollState)) {
            Column(Modifier.width(40.dp)) {
                for (hour in 0..23) {
                    Box(Modifier.height(HOUR_HEIGHT).fillMaxWidth()) {
                        Text(String.format("%02d:00", hour), fontSize = 10.sp, modifier = Modifier.padding(2.dp))
                    }
                }
            }
            days.forEach { date ->
                Box(Modifier.weight(1f).height(HOUR_HEIGHT * 24)) {
                    // hour gridlines
                    Column(Modifier.fillMaxSize()) {
                        for (hour in 0..23) {
                            Box(Modifier.height(HOUR_HEIGHT).fillMaxWidth()) {
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                )
                            }
                        }
                    }
                    val dayEvents = occurrences.filter {
                        Instant.ofEpochMilli(it.startMillis).atZone(zone).toLocalDate() == date
                    }
                    dayEvents.forEach { occ ->
                        val startTime = Instant.ofEpochMilli(occ.startMillis).atZone(zone)
                        val endTime = Instant.ofEpochMilli(occ.endMillis).atZone(zone)
                        val startFraction = startTime.hour + startTime.minute / 60f
                        val durationHours = ((occ.endMillis - occ.startMillis).coerceAtLeast(15 * 60_000L)) / 3_600_000f
                        Box(
                            modifier = Modifier
                                .offset(y = HOUR_HEIGHT * startFraction)
                                .fillMaxWidth()
                                .height(HOUR_HEIGHT * durationHours)
                                .padding(horizontal = 1.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.85f))
                                .clickable { onDayClick(date) }
                        ) {
                            Text(
                                occ.event.title,
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onPrimary,
                                maxLines = 2,
                                modifier = Modifier.padding(2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
