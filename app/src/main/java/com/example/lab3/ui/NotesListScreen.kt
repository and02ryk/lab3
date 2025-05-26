package com.example.lab3.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.lab3.model.Note
import com.example.lab3.viewModel.NotesViewModel
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesListScreen(
    viewModel: NotesViewModel,
    onNoteClick: (Int) -> Unit
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val notes = viewModel.notes.collectAsState().value
    val error = viewModel.error.collectAsState()
    val isRefreshing = viewModel.isRefreshing.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var newNoteTitle by remember { mutableStateOf("") }
    var newNoteContent by remember { mutableStateOf("") }

    val isTitleValid = newNoteTitle.isNotBlank()
    val isFormValid = isTitleValid

    val autoRefreshEnabled by viewModel.autoRefreshEnabled.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Заметки (${notes.size})") },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = "Автообновление",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Switch(
                            checked = autoRefreshEnabled,
                            onCheckedChange = { viewModel.setAutoRefreshEnabled(it) }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    showAddDialog = true
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Добавить заметку"
                )
            }
        }
    ) { padding ->

        LaunchedEffect(error) {
            error.let { message ->
                scope.launch {
                    snackbarHostState.showSnackbar(message.value.toString())
                }
                viewModel.clearError()
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isRefreshing.value && notes.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Загрузка заметок...")
                        }
                    }
                }
                notes.isEmpty() && !isRefreshing.value -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Нет заметок")
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { viewModel.loadNotes() }) {
                                Text("Обновить")
                            }
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(notes) { note ->
                            NoteItem(
                                note = note,
                                onCheckedChange = { viewModel.toggleNoteCompletion(note.id) },
                                onClick = { onNoteClick(note.id) }
                            )
                        }
                    }
                }
            }

            error.value?.let { errorMessage ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.BottomCenter),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = errorMessage,
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Button(
                            onClick = { viewModel.clearError() }
                        ) {
                            Text("OK")
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = showAddDialog,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically()
        ) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Новая заметка") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = newNoteTitle,
                            onValueChange = { newNoteTitle = it },
                            label = { Text("Заголовок*") },
                            isError = !isTitleValid && newNoteTitle.isNotEmpty(),
                            supportingText = {
                                if (!isTitleValid && newNoteTitle.isNotEmpty()) {
                                    Text("Заголовок не может быть пустым")
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = newNoteContent,
                            onValueChange = { newNoteContent = it },
                            label = { Text("Содержание") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (isFormValid) {
                                viewModel.addLocalNote(newNoteTitle, newNoteContent)
                                newNoteTitle = ""
                                newNoteContent = ""
                                showAddDialog = false
                            }
                        },
                        enabled = isFormValid
                    ) {
                        Text("Добавить")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            showAddDialog = false
                        }
                    ) {
                        Text("Отмена")
                    }
                }
            )
        }
    }
}

@Composable
fun NoteItem(
    note: Note,
    onCheckedChange: (Boolean) -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = note.isCompleted,
                onCheckedChange = onCheckedChange
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = note.title,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = note.content,
                )
                Text(
                    text = formatDateTime(note.timestamp),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}