package com.techingopochtli.workouttracker

import com.techingopochtli.workouttracker.data.SetRow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FormatTest {

    private fun row(id: Long, exerciseId: Long, name: String, reps: Int, kg: Double?) =
        SetRow(id, exerciseId, name, "Gym", reps, kg, createdAt = id)

    @Test fun weightDropsUselessDecimals() {
        assertEquals("60", Format.weight(60.0))
        assertEquals("62.5", Format.weight(62.5))
        assertEquals("62.25", Format.weight(62.25))
        assertEquals("1800", Format.weight(1800.0))
    }

    @Test fun setWithAndWithoutWeight() {
        assertEquals("10 × 60 kg", Format.set(10, 60.0))
        assertEquals("12 reps", Format.set(12, null))
        assertEquals("12 reps", Format.set(12, 0.0))
    }

    @Test fun parseReps() {
        assertEquals(8, Format.parseReps(" 8 "))
        assertNull(Format.parseReps("0"))
        assertNull(Format.parseReps(""))
        assertNull(Format.parseReps("abc"))
    }

    @Test fun parseWeightAcceptsCommaAndIsOptional() {
        assertEquals(62.5, Format.parseWeight("62,5")!!, 0.0)
        assertEquals(40.0, Format.parseWeight("40")!!, 0.0)
        assertNull(Format.parseWeight("-5"))
        assertTrue(Format.isWeightInputValid(""))
        assertFalse(Format.isWeightInputValid("6x"))
    }

    @Test fun groupsKeepOrderAndSummarize() {
        val rows = listOf(
            row(1, 10, "Sentadilla", 10, 60.0),
            row(2, 20, "Dominadas", 8, null),
            row(3, 10, "Sentadilla", 8, 70.0),
        )
        val groups = Format.groupByExercise(rows)
        assertEquals(listOf("Sentadilla", "Dominadas"), groups.map { it.name })
        assertEquals(18, groups[0].totalReps)
        assertEquals(1160.0, groups[0].volumeKg, 0.0)
        assertEquals("2 series · 18 reps · 1160 kg vol.", groups[0].summary())
        assertEquals("1 serie · 8 reps", groups[1].summary())
    }
}
