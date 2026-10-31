package com.techingopochtli.workouttracker

import com.techingopochtli.workouttracker.data.SetRow
import java.math.BigDecimal
import java.math.RoundingMode

/** Lógica pura (sin Android) para formatear, validar y resumir series. */
object Format {

    /** 60.0 -> "60", 62.5 -> "62.5", 62.25 -> "62.25". */
    fun weight(kg: Double): String =
        BigDecimal.valueOf(kg).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()

    /** "10 reps" o "10 × 60 kg". */
    fun set(reps: Int, weightKg: Double?): String =
        if (weightKg == null || weightKg == 0.0) "$reps reps" else "$reps × ${weight(weightKg)} kg"

    /** Reps válidas: entero > 0. */
    fun parseReps(input: String): Int? = input.trim().toIntOrNull()?.takeIf { it > 0 }

    /** Peso válido: número >= 0, acepta coma decimal. Vacío o inválido -> null. */
    fun parseWeight(input: String): Double? =
        input.trim().replace(',', '.').toDoubleOrNull()?.takeIf { it >= 0 && !it.isInfinite() }

    /** El campo de peso es opcional: vacío es válido, texto inválido no. */
    fun isWeightInputValid(input: String): Boolean = input.isBlank() || parseWeight(input) != null

    data class ExerciseGroup(
        val exerciseId: Long,
        val name: String,
        val discipline: String,
        val sets: List<SetRow>,
    ) {
        val totalReps: Int get() = sets.sumOf { it.reps }
        val volumeKg: Double get() = sets.sumOf { it.reps * (it.weightKg ?: 0.0) }

        /** "3 series · 30 reps · 1800 kg" (el volumen solo si hay peso). */
        fun summary(): String {
            val parts = mutableListOf("${sets.size} ${if (sets.size == 1) "serie" else "series"}", "$totalReps reps")
            if (volumeKg > 0) parts += "${weight(volumeKg)} kg vol."
            return parts.joinToString(" · ")
        }
    }

    /** Agrupa por ejercicio, en el orden en que se hizo cada ejercicio por primera vez. */
    fun groupByExercise(rows: List<SetRow>): List<ExerciseGroup> =
        rows.groupBy { it.exerciseId }.map { (id, sets) ->
            ExerciseGroup(id, sets.first().exerciseName, sets.first().discipline, sets)
        }
}
