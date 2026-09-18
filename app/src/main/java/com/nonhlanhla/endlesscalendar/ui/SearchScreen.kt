@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.nonhlanhla.endlesscalendar.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.nonhlanhla.endlesscalendar.calendar.CalendarViewModel
import com.nonhlanhla.endlesscalendar.data.Event
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun SearchScreen(navController: NavController, viewModel: CalendarViewModel) {
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf(listOf<Event>()) }

    LaunchedEffect(query) {
        results = if (query.isBlank()) emptyList() else viewModel.searchEvents(query)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Search") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Search events") },
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            )
            LazyColumn(Modifier.fillMaxSize()) {
                items(results, key = { it.id }) { event ->
                    val dateText = remember(event) {
                        DateTimeFormatter.ofPattern("EEE, MMM d yyyy 'at' h:mm a").format(
                            Instant.ofEpochMilli(event.startMillis).atZone(ZoneId.systemDefault())
                        )
                    }
                    ListItem(
                        headlineContent = { Text(event.title) },
                        supportingContent = { Text(dateText, color = MaterialTheme.colorScheme.outline) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate("eventEdit?eventId=${event.id}&dateMillis=0") }
                            .padding(horizontal = 4.dp)
                    )
                }
            }
        }
    }
}
