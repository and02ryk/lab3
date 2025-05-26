package com.example.lab3

import com.example.lab3.model.Note
import com.example.lab3.model.NotesResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class NotesRepository(private val okHttpClient: OkHttpClient) {
    private val notesUrl = "https://mej1g.wiremockapi.cloud/notes"

    suspend fun fetchNotes(): Result<List<Note>> = runCatching {
        withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url(notesUrl)
                .get()
                .build()

            val response = okHttpClient.newCall(request).execute()

            if (!response.isSuccessful) {
                throw IOException("HTTP ${response.code}: ${response.message}")
            }

            val responseBody = response.body?.string()
            if (responseBody.isNullOrEmpty()) {
                throw IOException("Empty response body")
            }

            try {
                Json.decodeFromString<NotesResponse>(responseBody).notes
            } catch (e: Exception) {
                Json.decodeFromString<List<Note>>(responseBody)
            }
        }
    }
}