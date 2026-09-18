package com.nonhlanhla.endlesscalendar.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.nonhlanhla.endlesscalendar.EndlessCalendarApp
import com.nonhlanhla.endlesscalendar.calendar.CalendarViewModel
import com.nonhlanhla.endlesscalendar.data.HolidayRegion
import com.nonhlanhla.endlesscalendar.data.IcsUtils
import com.nonhlanhla.endlesscalendar.data.ThemeMode
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(navController: NavController, viewModel: CalendarViewModel) {
    val settings by viewModel.settings.collectAsState()
    val context = LocalContext.current
    val app = context.applicationContext as EndlessCalendarApp
    val scope = rememberCoroutineScope()

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/calendar")) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val events = viewModel.repository.getAllOnce()
                val text = IcsUtils.export(events)
                context.contentResolver.openOutputStream(uri)?.use { it.write(text.toByteArray()) }
            }
        }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.readText() ?: return@launch
                val imported = IcsUtils.import(text)
                imported.forEach { viewModel.repository.upsert(it) }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .fillMaxWidth()
        ) {
            Text("Appearance", style = MaterialTheme.typography.titleMedium)
            var themeExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = themeExpanded, onExpandedChange = { themeExpanded = it }) {
                OutlinedTextField(
                    value = settings.themeMode.name.lowercase().replaceFirstChar { it.uppercase() },
                    onValueChange = {}, readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = themeExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                DropdownMenu(expanded = themeExpanded, onDismissRequest = { themeExpanded = false }) {
                    ThemeMode.entries.forEach { mode ->
                        DropdownMenuItem(text = { Text(mode.name) }, onClick = {
                            scope.launch { app.userPreferences.setThemeMode(mode) }
                            themeExpanded = false
                        })
                    }
                }
            }
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Dynamic color (Material You)", modifier = Modifier.weight(1f))
                Switch(checked = settings.dynamicColor, onCheckedChange = {
                    scope.launch { app.userPreferences.setDynamicColor(it) }
                })
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Week starts on Monday", modifier = Modifier.weight(1f))
                Switch(checked = settings.weekStartsMonday, onCheckedChange = {
                    scope.launch { app.userPreferences.setWeekStartsMonday(it) }
                })
            }

            HorizontalDivider(Modifier.padding(vertical = 16.dp))

            Text("Holidays", style = MaterialTheme.typography.titleMedium)
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Show holidays on calendar", modifier = Modifier.weight(1f))
                Switch(checked = settings.showHolidays, onCheckedChange = {
                    scope.launch { app.userPreferences.setShowHolidays(it) }
                })
            }
            var regionExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = regionExpanded, onExpandedChange = { regionExpanded = it }) {
                OutlinedTextField(
                    value = settings.holidayRegion.name.replace("_", " "),
                    onValueChange = {}, readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = regionExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                DropdownMenu(expanded = regionExpanded, onDismissRequest = { regionExpanded = false }) {
                    HolidayRegion.entries.forEach { region ->
                        DropdownMenuItem(text = { Text(region.name.replace("_", " ")) }, onClick = {
                            scope.launch { app.userPreferences.setHolidayRegion(region) }
                            regionExpanded = false
                        })
                    }
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 16.dp))

            Text("Reminders", style = MaterialTheme.typography.titleMedium)
            Text(
                "Default reminder for new events: ${settings.defaultReminderMinutes} min before",
                modifier = Modifier.padding(top = 8.dp)
            )
            Row(Modifier.fillMaxWidth()) {
                listOf(0, 10, 30, 60, 1440).forEach { minutes ->
                    Button(
                        onClick = { scope.launch { app.userPreferences.setDefaultReminderMinutes(minutes) } },
                        modifier = Modifier.padding(end = 6.dp, top = 6.dp)
                    ) { Text("${minutes}m") }
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 16.dp))

            Text("Backup", style = MaterialTheme.typography.titleMedium)
            Row(Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Button(onClick = { exportLauncher.launch("endless-calendar-backup.ics") }, modifier = Modifier.padding(end = 8.dp)) {
                    Text("Export .ics")
                }
                Button(onClick = { importLauncher.launch(arrayOf("text/calendar", "*/*")) }) {
                    Text("Import .ics")
                }
            }
        }
    }
}
