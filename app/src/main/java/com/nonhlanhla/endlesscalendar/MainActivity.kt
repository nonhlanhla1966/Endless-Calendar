package com.nonhlanhla.endlesscalendar

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.nonhlanhla.endlesscalendar.calendar.CalendarViewModel
import com.nonhlanhla.endlesscalendar.calendar.CalendarViewModelFactory
import com.nonhlanhla.endlesscalendar.ui.EndlessCalendarNavHost
import com.nonhlanhla.endlesscalendar.ui.EndlessCalendarTheme

class MainActivity : ComponentActivity() {

    private val viewModel: CalendarViewModel by viewModels { CalendarViewModelFactory(application) }

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* no-op either way */ }

    private val calendarPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { /* no-op either way */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        calendarPermissionLauncher.launch(
            arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR)
        )

        val startEventId = intent?.getLongExtra("openEventId", -1L)?.takeIf { it > 0 }

        setContent {
            val settings by viewModel.settings.collectAsState()
            EndlessCalendarTheme(themeMode = settings.themeMode, dynamicColor = settings.dynamicColor) {
                EndlessCalendarNavHost(viewModel = viewModel, startEventId = startEventId)
            }
        }
    }
}
