package com.example.lab3.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.lab3.viewModel.NotesViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    noteId: Int?,
    viewModel: NotesViewModel = viewModel(),
    navController: NavHostController
) {
    val notes by viewModel.notes.collectAsState()
    val note = remember(noteId) { notes.find { it.id == noteId } }

    var isCompleted by remember { mutableStateOf(note?.isCompleted ?: false) }

    LaunchedEffect(note?.isCompleted) {
        isCompleted = note?.isCompleted ?: false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Детали заметки") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController.navigateUp()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (note == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Заметка не найдена")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.fillMaxWidth()
                )

                // Чекбокс выполнения
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        viewModel.toggleNoteCompletion(note.id)
                        isCompleted = !isCompleted
                    }
                ) {
                    Checkbox(
                        checked = isCompleted,
                        onCheckedChange = {
                            viewModel.toggleNoteCompletion(note.id)
                            isCompleted = !isCompleted
                        }
                    )
                    Text(
                        text = if (isCompleted) "Выполнено" else "Не выполнено",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Создано: ${formatDateTime(note.timestamp)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

fun formatDateTime(isoString: String): String {
    return try {
        val instant = Instant.parse(isoString)
        val formatter = DateTimeFormatter
            .ofPattern("dd.MM.yyyy HH:mm")
            .withZone(ZoneId.systemDefault())

        formatter.format(instant)
    } catch (e: Exception) {
        isoString
    }
}