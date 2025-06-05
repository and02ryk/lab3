package com.example.lab3.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lab3.NotesPrefsHelper
import com.example.lab3.NotesRepository
import com.example.lab3.model.Note
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import kotlin.random.Random

class NotesViewModel(
    private val repository: NotesRepository,
    private val prefsHelper: NotesPrefsHelper
) : ViewModel() {
    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes = _notes.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private val _autoRefreshEnabled = MutableStateFlow(false)
    val autoRefreshEnabled = _autoRefreshEnabled.asStateFlow()

    private var refreshJob: Job? = null

    init {
        val savedNotes = prefsHelper.loadNotes()
        if (savedNotes.isNotEmpty()) {
            _notes.value = savedNotes
        }
        loadNotes()
        startAutoRefresh()
    }

    fun setAutoRefreshEnabled(enabled: Boolean) {
        _autoRefreshEnabled.value = enabled
        if (enabled) {
            startAutoRefresh()
        } else {
            stopAutoRefresh()
        }
    }

    private fun startAutoRefresh() {
        stopAutoRefresh()
        refreshJob = viewModelScope.launch {
            while (autoRefreshEnabled.value) {
                delay(60_000)
                loadNotes()
            }
        }
    }

    private fun stopAutoRefresh() {
        refreshJob?.cancel()
        refreshJob = null
    }

    fun loadNotes() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _error.value = null

            repository.fetchNotes()
                .onSuccess { remoteNotes ->
                    if (remoteNotes.size > _notes.value.size) {
                        _notes.value = remoteNotes
                        prefsHelper.saveNotes(remoteNotes)
                    }
                    _error.value = null
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Unknown error"
                }

            _isRefreshing.value = false
        }
    }


    fun toggleNoteCompletion(noteId: Int) {
        _notes.value = _notes.value.map { note ->
            if (note.id == noteId) note.copy(isCompleted = !note.isCompleted) else note
        }
        prefsHelper.saveNotes(_notes.value)
    }

    fun addLocalNote(title: String, content: String) {
        val newNote = Note(
            id = Random.nextInt(1000, 10000),
            title = title,
            content = content,
            timestamp = Instant.now().toString(),
            isCompleted = false
        )
        _notes.value = _notes.value + newNote
        prefsHelper.saveNotes(_notes.value)
    }

    fun clearError() {
        _error.value = null
    }
}