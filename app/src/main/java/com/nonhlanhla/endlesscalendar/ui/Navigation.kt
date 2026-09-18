package com.nonhlanhla.endlesscalendar.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nonhlanhla.endlesscalendar.calendar.CalendarViewModel

@Composable
fun EndlessCalendarNavHost(viewModel: CalendarViewModel, startEventId: Long?) {
    val navController = rememberNavController()

    LaunchedEffect(startEventId) {
        if (startEventId != null && startEventId > 0) {
            navController.navigate("eventEdit?eventId=$startEventId&dateMillis=0")
        }
    }

    NavHost(navController = navController, startDestination = "month") {
        composable("month") { MonthScreen(navController, viewModel) }
        composable("week") { WeekScreen(navController, viewModel) }
        composable(
            "day/{epochDay}",
            arguments = listOf(navArgument("epochDay") { type = NavType.LongType })
        ) { backStackEntry ->
            val epochDay = backStackEntry.arguments?.getLong("epochDay") ?: java.time.LocalDate.now().toEpochDay()
            DayAgendaScreen(navController, viewModel, epochDay)
        }
        composable("search") { SearchScreen(navController, viewModel) }
        composable("settings") { SettingsScreen(navController, viewModel) }
        composable(
            "eventEdit?eventId={eventId}&dateMillis={dateMillis}",
            arguments = listOf(
                navArgument("eventId") { type = NavType.LongType; defaultValue = -1L },
                navArgument("dateMillis") { type = NavType.LongType; defaultValue = 0L }
            )
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getLong("eventId") ?: -1L
            val dateMillis = backStackEntry.arguments?.getLong("dateMillis") ?: 0L
            EventEditScreen(navController, viewModel, eventId, dateMillis)
        }
    }
}
