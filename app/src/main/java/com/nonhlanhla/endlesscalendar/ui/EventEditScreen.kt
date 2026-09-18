package com.nonhlanhla.endlesscalendar.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.nonhlanhla.endlesscalendar.calendar.CalendarViewModel
import com.nonhlanhla.endlesscalendar.data.Event
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private enum class RecurFreq(val label: String) { NONE("Does not repeat"), DAILY("Daily"), WEEKLY("Weekly"), MONTHLY("Monthly"), YEARLY("Yearly") }

private val REMINDER_OPTIONS = listOf(0 to "At time of event", 10 to "10 min before", 30 to "30 min before", 60 to "1 hour before", 1440 to "1 day before")
private val COLOR_TAGS = listOf(0xFF1E88E5.toInt(), 0xFFE53935.toInt(), 0xFF43A047.toInt(), 0xFFFB8C00.toInt(), 0xFF8E24AA.toInt(), 0xFF546E7A.toInt())

@Composable
fun EventEditScreen(navController: NavController, viewModel: CalendarViewModel, eventId: Long, initialDateMillis: Long) {
    val settings by viewModel.settings.collectAsState()
    val zone = ZoneId.systemDefault()

    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var allDay by remember { mutableStateOf(false) }
    var startDateTime by remember {
        mutableStateOf(
            if (initialDateMillis > 0) Instant.ofEpochMilli(initialDateMillis).atZone(zone).toLocalDateTime().withHour(9).withMinute(0)
            else LocalDateTime.now().plusHours(1).withMinute(0)
        )
    }
    var endDateTime by remember { mutableStateOf(startDateTime.plusHours(1)) }
    var colorTag by remember { mutableStateOf(COLOR_TAGS[0]) }
    var recurFreq by remember { mutableStateOf(RecurFreq.NONE) }
    var interval by remember { mutableStateOf(1) }
    var reminderSet by remember { mutableStateOf(setOf<Int>()) }
    var existingEvent by remember { mutableStateOf<Event?>(null) }
    var loaded by remember { mutableStateOf(eventId <= 0) }

    LaunchedEffect(eventId) {
        if (eventId > 0) {
            val e = viewModel.getEvent(eventId)
            if (e != null) {
                existingEvent = e
                title = e.title
                notes = e.notes
                location = e.location
                allDay = e.allDay
                startDateTime = Instant.ofEpochMilli(e.startMillis).atZone(zone).toLocalDateTime()
                endDateTime = Instant.ofEpochMilli(e.endMillis).atZone(zone).toLocalDateTime()
                colorTag = if (e.colorTag != 0) e.colorTag else COLOR_TAGS[0]
                reminderSet = e.reminderMinutes.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
                if (e.recurrenceRule.isNotBlank()) {
                    val parts = e.recurrenceRule.split(";").associate {
                        val kv = it.split("=", limit = 2); kv[0] to (kv.getOrNull(1) ?: "")
                    }
                    recurFreq = when (parts["FREQ"]) {
                        "DAILY" -> RecurFreq.DAILY; "WEEKLY" -> RecurFreq.WEEKLY
                        "MONTHLY" -> RecurFreq.MONTHLY; "YEARLY" -> RecurFreq.YEARLY
                        else -> RecurFreq.NONE
                    }
                    interval = parts["INTERVAL"]?.toIntOrNull() ?: 1
                }
            }
        } else {
            reminderSet = setOf(settings.defaultReminderMinutes)
        }
        loaded = true
    }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (eventId > 0) "Edit event" else "New event") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (eventId > 0) {
                        IconButton(onClick = {
                            existingEvent?.let { viewModel.deleteEvent(it) }
                            navController.popBackStack()
                        }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = title, onValueChange = { title = it },
                label = { Text("Title") }, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("All day", modifier = Modifier.weight(1f))
                Switch(checked = allDay, onCheckedChange = {
                    allDay = it
                    if (it) endDateTime = startDateTime.toLocalDate().atTime(23, 59)
                })
            }
            Spacer(Modifier.height(8.dp))

            Text("Starts", style = MaterialTheme.typography.labelLarge)
            Row(modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = { showStartDatePicker = true }) {
                    Text(startDateTime.toLocalDate().format(DateTimeFormatter.ofPattern("EEE, MMM d yyyy")))
                }
                if (!allDay) {
                    TextButton(onClick = { showStartTimePicker = true }) {
                        Text(startDateTime.toLocalTime().format(DateTimeFormatter.ofPattern("h:mm a")))
                    }
                }
            }

            Text("Ends", style = MaterialTheme.typography.labelLarge)
            Row(modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = { showEndDatePicker = true }) {
                    Text(endDateTime.toLocalDate().format(DateTimeFormatter.ofPattern("EEE, MMM d yyyy")))
                }
                if (!allDay) {
                    TextButton(onClick = { showEndTimePicker = true }) {
                        Text(endDateTime.toLocalTime().format(DateTimeFormatter.ofPattern("h:mm a")))
                    }
                }
            }
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = location, onValueChange = { location = it },
                label = { Text("Location") }, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = notes, onValueChange = { notes = it },
                label = { Text("Notes") }, modifier = Modifier.fillMaxWidth(), minLines = 2
            )
            Spacer(Modifier.height(16.dp))

            Text("Repeat", style = MaterialTheme.typography.labelLarge)
            var freqExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = freqExpanded, onExpandedChange = { freqExpanded = it }) {
                OutlinedTextField(
                    value = recurFreq.label, onValueChange = {}, readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = freqExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                DropdownMenu(expanded = freqExpanded, onDismissRequest = { freqExpanded = false }) {
                    RecurFreq.entries.forEach { freq ->
                        DropdownMenuItem(text = { Text(freq.label) }, onClick = { recurFreq = freq; freqExpanded = false })
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            Text("Reminders", style = MaterialTheme.typography.labelLarge)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                REMINDER_OPTIONS.forEach { (minutes, label) ->
                    FilterChip(
                        selected = reminderSet.contains(minutes),
                        onClick = {
                            reminderSet = if (reminderSet.contains(minutes)) reminderSet - minutes else reminderSet + minutes
                        },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
            Spacer(Modifier.height(16.dp))

            Text("Color", style = MaterialTheme.typography.labelLarge)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                COLOR_TAGS.forEach { c ->
                    val isSelected = c == colorTag
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .size(if (isSelected) 32.dp else 28.dp)
                            .background(Color(c), CircleShape)
                            .clickable { colorTag = c }
                    )
                }
            }
            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    val rrule = if (recurFreq == RecurFreq.NONE) "" else "FREQ=${recurFreq.name};INTERVAL=$interval"
                    val event = (existingEvent ?: Event(title = "", startMillis = 0, endMillis = 0)).copy(
                        title = title.ifBlank { "Untitled event" },
                        notes = notes,
                        location = location,
                        allDay = allDay,
                        timeZoneId = zone.id,
                        startMillis = startDateTime.atZone(zone).toInstant().toEpochMilli(),
                        endMillis = endDateTime.atZone(zone).toInstant().toEpochMilli(),
                        colorTag = colorTag,
                        recurrenceRule = rrule,
                        reminderMinutes = reminderSet.joinToString(",")
                    )
                    viewModel.saveEvent(event) { navController.popBackStack() }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = loaded && title.isNotBlank()
            ) {
                Text("Save")
            }
        }
    }

    if (showStartDatePicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = startDateTime.atZone(zone).toInstant().toEpochMilli())
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { millis ->
                        val newDate = Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()
                        val shift = java.time.temporal.ChronoUnit.DAYS.between(startDateTime.toLocalDate(), newDate)
                        startDateTime = startDateTime.plusDays(shift)
                        endDateTime = endDateTime.plusDays(shift)
                    }
                    showStartDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showStartDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = state) }
    }

    if (showEndDatePicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = endDateTime.atZone(zone).toInstant().toEpochMilli())
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { millis ->
                        val newDate = Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()
                        endDateTime = newDate.atTime(endDateTime.toLocalTime())
                    }
                    showEndDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showEndDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = state) }
    }

    if (showStartTimePicker) {
        val state = rememberTimePickerState(initialHour = startDateTime.hour, initialMinute = startDateTime.minute)
        AlertDialog(
            onDismissRequest = { showStartTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val newStart = startDateTime.withHour(state.hour).withMinute(state.minute)
                    val duration = java.time.Duration.between(startDateTime, endDateTime)
                    startDateTime = newStart
                    endDateTime = newStart.plus(if (duration.isNegative) java.time.Duration.ofHours(1) else duration)
                    showStartTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showStartTimePicker = false }) { Text("Cancel") } },
            text = { TimePicker(state = state) }
        )
    }

    if (showEndTimePicker) {
        val state = rememberTimePickerState(initialHour = endDateTime.hour, initialMinute = endDateTime.minute)
        AlertDialog(
            onDismissRequest = { showEndTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    endDateTime = endDateTime.withHour(state.hour).withMinute(state.minute)
                    showEndTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showEndTimePicker = false }) { Text("Cancel") } },
            text = { TimePicker(state = state) }
        )
    }
}
