package com.techingopochtli.workouttracker.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.techingopochtli.workouttracker.data.AppDatabase
import com.techingopochtli.workouttracker.data.Exercise
import com.techingopochtli.workouttracker.data.SetRow
import com.techingopochtli.workouttracker.data.WorkoutSet
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDatabase.get(app).dao()

    val today: LocalDate = LocalDate.now()

    val exercises: StateFlow<List<Exercise>> = dao.exercises()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val todaySets: StateFlow<List<SetRow>> = dao.setsForDay(today.toEpochDay())
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Crea el ejercicio (o reutiliza uno con el mismo nombre) y devuelve su id. */
    fun addExercise(name: String, discipline: String, onDone: (Long) -> Unit) {
        val cleanName = name.trim()
        val cleanDiscipline = discipline.trim().ifEmpty { "Otro" }
        if (cleanName.isEmpty()) return
        viewModelScope.launch {
            val existing = dao.findExerciseByName(cleanName)
            val id = existing?.id ?: dao.insertExercise(Exercise(name = cleanName, discipline = cleanDiscipline))
            onDone(id)
        }
    }

    fun addSet(exerciseId: Long, reps: Int, weightKg: Double?) {
        viewModelScope.launch {
            dao.insertSet(
                WorkoutSet(
                    exerciseId = exerciseId,
                    epochDay = today.toEpochDay(),
                    reps = reps,
                    weightKg = weightKg,
                )
            )
        }
    }

    /** Borra la serie y entrega una copia para poder deshacer. */
    fun deleteSet(setId: Long, onDeleted: (WorkoutSet) -> Unit) {
        viewModelScope.launch {
            val set = dao.getSet(setId) ?: return@launch
            dao.deleteSet(setId)
            onDeleted(set)
        }
    }

    fun restoreSet(set: WorkoutSet) {
        viewModelScope.launch { dao.insertSet(set) }
    }
}
