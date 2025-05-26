package com.example.lab3

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.lab3.ui.NoteDetailScreen
import com.example.lab3.ui.NotesListScreen
import com.example.lab3.viewModel.NotesViewModel
import org.koin.compose.getKoin

@Composable
fun NotesApp() {
    val navController = rememberNavController()
    val notesRepository: NotesRepository = getKoin().get()
    val viewModel: NotesViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return NotesViewModel(notesRepository) as T
            }
        }
    )

    NavHost(
        navController = navController,
        startDestination = "notesList"
    ) {
        composable("notesList") {
            NotesListScreen(
                viewModel = viewModel,
                onNoteClick = { noteId ->
                    navController.navigate("noteDetail/$noteId")
                }
            )
        }
        composable("noteDetail/{noteId}") { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId")?.toIntOrNull()
            NoteDetailScreen(
                viewModel = viewModel,
                noteId = noteId,
                navController = navController
            )
        }
    }
}