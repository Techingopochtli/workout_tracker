package com.techingopochtli.workouttracker.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.techingopochtli.workouttracker.Format
import com.techingopochtli.workouttracker.data.Exercise
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.util.Locale

private val DEFAULT_DISCIPLINES = listOf("Gym", "Calistenia")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(vm: MainViewModel = viewModel()) {
    val exercises by vm.exercises.collectAsStateWithLifecycle()
    val sets by vm.todaySets.collectAsStateWithLifecycle()
    val groups = remember(sets) { Format.groupByExercise(sets) }

    var selectedId by rememberSaveable { mutableStateOf<Long?>(null) }
    var repsText by rememberSaveable { mutableStateOf("") }
    var weightText by rememberSaveable { mutableStateOf("") }
    var showNewExercise by rememberSaveable { mutableStateOf(false) }

    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val dateLabel = remember(vm.today) {
        vm.today.format(DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", Locale("es")))
            .replaceFirstChar { it.uppercase() }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Hoy · $dateLabel") }) },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                AddSetCard(
                    exercises = exercises,
                    selectedId = selectedId,
                    onSelect = { selectedId = it },
                    onNewExercise = { showNewExercise = true },
                    repsText = repsText,
                    onRepsChange = { repsText = it },
                    weightText = weightText,
                    onWeightChange = { weightText = it },
                    onAdd = {
                        val id = selectedId ?: return@AddSetCard
                        val reps = Format.parseReps(repsText) ?: return@AddSetCard
                        // Se conservan reps y peso para registrar la siguiente serie con un toque.
                        vm.addSet(id, reps, Format.parseWeight(weightText))
                    },
                )
            }

            if (groups.isEmpty()) {
                item {
                    Text(
                        "Aún no hay series hoy.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }

            items(groups, key = { it.exerciseId }) { group ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(group.name, style = MaterialTheme.typography.titleMedium)
                        Text(
                            "${group.discipline} · ${group.summary()}",
                            style = MaterialTheme.typography.bodySmall,
                        )
                        HorizontalDivider(Modifier.padding(vertical = 8.dp))
                        group.sets.forEachIndexed { index, row ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("${index + 1}.  ${Format.set(row.reps, row.weightKg)}")
                                Spacer(Modifier.weight(1f))
                                IconButton(onClick = {
                                    vm.deleteSet(row.setId) { deleted ->
                                        scope.launch {
                                            val result = snackbar.showSnackbar(
                                                message = "Serie borrada",
                                                actionLabel = "Deshacer",
                                                duration = SnackbarDuration.Short,
                                            )
                                            if (result == SnackbarResult.ActionPerformed) vm.restoreSet(deleted)
                                        }
                                    }
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Borrar serie")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showNewExercise) {
        NewExerciseDialog(
            knownDisciplines = (DEFAULT_DISCIPLINES + exercises.map { it.discipline }).distinct(),
            onDismiss = { showNewExercise = false },
            onCreate = { name, discipline ->
                vm.addExercise(name, discipline) { id -> selectedId = id }
                showNewExercise = false
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddSetCard(
    exercises: List<Exercise>,
    selectedId: Long?,
    onSelect: (Long) -> Unit,
    onNewExercise: () -> Unit,
    repsText: String,
    onRepsChange: (String) -> Unit,
    weightText: String,
    onWeightChange: (String) -> Unit,
    onAdd: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = exercises.firstOrNull { it.id == selectedId }
    val repsValid = Format.parseReps(repsText) != null
    val weightValid = Format.isWeightInputValid(weightText)

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = selected?.let { "${it.name} · ${it.discipline}" } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Ejercicio") },
                    placeholder = { Text("Elige o crea uno") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    exercises.forEach { exercise ->
                        DropdownMenuItem(
                            text = { Text("${exercise.name} · ${exercise.discipline}") },
                            onClick = {
                                onSelect(exercise.id)
                                expanded = false
                            },
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("+ Nuevo ejercicio") },
                        onClick = {
                            expanded = false
                            onNewExercise()
                        },
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = repsText,
                    onValueChange = { onRepsChange(it.filter(Char::isDigit).take(4)) },
                    label = { Text("Reps") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = weightText,
                    onValueChange = { onWeightChange(it.take(7)) },
                    label = { Text("Peso kg (opcional)") },
                    singleLine = true,
                    isError = !weightValid,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                )
            }

            Button(
                onClick = onAdd,
                enabled = selected != null && repsValid && weightValid,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Agregar serie")
            }
        }
    }
}

@Composable
private fun NewExerciseDialog(
    knownDisciplines: List<String>,
    onDismiss: () -> Unit,
    onCreate: (name: String, discipline: String) -> Unit,
) {
    var name by rememberSaveable { mutableStateOf("") }
    var discipline by rememberSaveable { mutableStateOf(knownDisciplines.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo ejercicio") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    placeholder = { Text("Ej. Press banca, Dominadas") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = discipline,
                    onValueChange = { discipline = it },
                    label = { Text("Disciplina") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    knownDisciplines.forEach { option ->
                        FilterChip(
                            selected = discipline.equals(option, ignoreCase = true),
                            onClick = { discipline = option },
                            label = { Text(option) },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onCreate(name, discipline) }, enabled = name.isNotBlank()) {
                Text("Crear")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}
