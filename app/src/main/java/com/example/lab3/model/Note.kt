package com.example.lab3.model

import kotlinx.serialization.Serializable

@Serializable
data class Note(
    val id: Int,
    val title: String,
    val content: String,
    val timestamp: String,
    var isCompleted: Boolean = false
)

@Serializable
data class NotesResponse(
    val notes: List<Note>
)