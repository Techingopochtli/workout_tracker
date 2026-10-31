package com.techingopochtli.workouttracker.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "exercises")
data class Exercise(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    /** Disciplina libre: "Gym", "Calistenia", etc. */
    val discipline: String,
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "workout_sets",
    foreignKeys = [
        ForeignKey(
            entity = Exercise::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("exerciseId"), Index("epochDay")],
)
data class WorkoutSet(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val exerciseId: Long,
    /** Día del entrenamiento como LocalDate.toEpochDay(). */
    val epochDay: Long,
    val reps: Int,
    /** Peso en kg; null para ejercicios de peso corporal. */
    val weightKg: Double?,
    val createdAt: Long = System.currentTimeMillis(),
)

/** Fila plana para mostrar una serie junto con su ejercicio. */
data class SetRow(
    val setId: Long,
    val exerciseId: Long,
    val exerciseName: String,
    val discipline: String,
    val reps: Int,
    val weightKg: Double?,
    val createdAt: Long,
)
