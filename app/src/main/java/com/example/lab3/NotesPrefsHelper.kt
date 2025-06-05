package com.example.lab3

import android.content.Context
import com.example.lab3.model.Note
import com.example.lab3.model.NotesResponse
import com.google.gson.Gson
import androidx.core.content.edit

class NotesPrefsHelper(context: Context) {
    private val sharedPref = context.getSharedPreferences("notes_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveNotes(notes: List<Note>) {
        val json = gson.toJson(NotesResponse(notes))
        sharedPref.edit { putString("notes_json", json) }
    }

    fun loadNotes(): List<Note> {
        val json = sharedPref.getString("notes_json", null)
        return if (json != null) {
            gson.fromJson(json, NotesResponse::class.java).notes
        } else {
            emptyList()
        }
    }
}