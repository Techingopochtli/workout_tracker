package com.techingopochtli.workouttracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM exercises ORDER BY name COLLATE NOCASE")
    fun exercises(): Flow<List<Exercise>>

    @Query("SELECT * FROM exercises WHERE name = :name COLLATE NOCASE LIMIT 1")
    suspend fun findExerciseByName(name: String): Exercise?

    @Insert
    suspend fun insertExercise(exercise: Exercise): Long

    @Insert
    suspend fun insertSet(set: WorkoutSet): Long

    @Query("SELECT * FROM workout_sets WHERE id = :id")
    suspend fun getSet(id: Long): WorkoutSet?

    @Query("DELETE FROM workout_sets WHERE id = :id")
    suspend fun deleteSet(id: Long)

    @Query(
        """
        SELECT s.id AS setId, s.exerciseId AS exerciseId, e.name AS exerciseName,
               e.discipline AS discipline, s.reps AS reps, s.weightKg AS weightKg,
               s.createdAt AS createdAt
        FROM workout_sets s
        JOIN exercises e ON e.id = s.exerciseId
        WHERE s.epochDay = :epochDay
        ORDER BY s.createdAt
        """
    )
    fun setsForDay(epochDay: Long): Flow<List<SetRow>>
}
