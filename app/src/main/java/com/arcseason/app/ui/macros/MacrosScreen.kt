package com.arcseason.app.ui.macros

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.arcseason.app.data.local.entity.NutritionLogEntity
import com.arcseason.app.ui.common.ArcBadge
import com.arcseason.app.ui.common.ArcCard
import com.arcseason.app.ui.common.LabeledProgressBar
import com.arcseason.app.ui.common.SectionHeader
import com.arcseason.app.ui.common.contentPadding
import com.arcseason.app.ui.theme.ArcBackground
import com.arcseason.app.ui.theme.ArcCarbBlue
import com.arcseason.app.ui.theme.ArcOnSurfaceMuted
import com.arcseason.app.ui.theme.ArcWarningAmber
import com.arcseason.app.ui.theme.NeonEmerald
import kotlinx.coroutines.launch

private data class QuickFood(
    val label: String,
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fat: Int
)

private val quickFoods = listOf(
    QuickFood("3 Eggs", 210, 18, 2, 15),
    QuickFood("Cottage Cheese", 120, 14, 5, 4),
    QuickFood("Tuna (1 can)", 130, 26, 0, 1),
    QuickFood("Chicken Breast", 165, 31, 0, 4),
    QuickFood("Oats (1 cup)", 300, 10, 54, 6),
    QuickFood("Protein Smoothie", 250, 30, 20, 5)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MacrosScreen(viewModel: MacrosViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showManualDialog by remember { mutableStateOf(false) }
    var showEditProfile by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun logFood(label: String, calories: Int, protein: Int, carbs: Int, fat: Int, isJunk: Boolean, source: String) {
        viewModel.logFood(label, calories, protein, carbs, fat, isJunk, source)
        scope.launch { snackbarHostState.showSnackbar("Logged: $label ($calories kcal)") }
    }

    Scaffold(
        containerColor = ArcBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding(innerPadding)
        ) {
            item {
                SectionHeader(
                    title = "Macros",
                    subtitle = "Conservative cut \u00b7 protein-first, never below BMR",
                    trailing = {
                        if (!uiState.needsOnboarding) {
                            TextButton(onClick = { showEditProfile = true }) { Text("Edit profile") }
                        }
                    }
                )
            }

            item {
                ArcCard(modifier = Modifier.padding(horizontal = 20.dp)) {
                    LabeledProgressBar("Calories", uiState.caloriesConsumed, uiState.caloriesTarget, "kcal", accent = NeonEmerald)
                    Spacer(Modifier.height(14.dp))
                    LabeledProgressBar("Protein", uiState.proteinConsumed, uiState.proteinTarget, "g", accent = NeonEmerald)
                    Spacer(Modifier.height(14.dp))
                    LabeledProgressBar("Carbs", uiState.carbsConsumed, uiState.carbsTarget, "g", accent = ArcCarbBlue)
                    Spacer(Modifier.height(14.dp))
                    LabeledProgressBar("Fat", uiState.fatConsumed, uiState.fatTarget, "g", accent = ArcWarningAmber)
                }
                Spacer(Modifier.height(16.dp))
            }

            item {
                Text(
                    "Quick add",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(Modifier.height(8.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(quickFoods) { food ->
                        AssistChip(
                            onClick = {
                                logFood(food.label, food.calories, food.protein, food.carbs, food.fat, isJunk = false, source = "QUICK_ADD")
                            },
                            label = { Text(food.label) }
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Today's log", style = MaterialTheme.typography.titleMedium)
                    TextButton(onClick = { showManualDialog = true }) { Text("+ Manual entry") }
                }
                Spacer(Modifier.height(4.dp))
            }

            if (uiState.logs.isEmpty()) {
                item {
                    Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                        ArcCard {
                            Text(
                                "Nothing logged yet today \u2014 use a quick-add chip or manual entry.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = ArcOnSurfaceMuted
                            )
                        }
                    }
                }
            }

            items(uiState.logs, key = { it.id }) { food ->
                Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                    LoggedFoodRow(food = food, onDelete = { viewModel.deleteFood(food) })
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }

    if (showManualDialog) {
        ManualFoodDialog(
            onDismiss = { showManualDialog = false },
            onConfirm = { label, cal, protein, carbs, fat, isJunk ->
                logFood(label, cal, protein, carbs, fat, isJunk, source = "MANUAL")
                showManualDialog = false
            }
        )
    }

    if (uiState.needsOnboarding) {
        OnboardingFlow(
            existingProfile = null,
            dismissible = false,
            onDismiss = {},
            onSave = { inputs, guardianAcknowledged -> viewModel.saveProfile(inputs, guardianAcknowledged) }
        )
    } else if (showEditProfile) {
        OnboardingFlow(
            existingProfile = uiState.profile,
            dismissible = true,
            onDismiss = { showEditProfile = false },
            onSave = { inputs, guardianAcknowledged ->
                viewModel.saveProfile(inputs, guardianAcknowledged)
                showEditProfile = false
            }
        )
    }
}

@Composable
private fun LoggedFoodRow(food: NutritionLogEntity, onDelete: () -> Unit) {
    ArcCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(food.foodName, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(2.dp))
                Text(
                    "${food.calories} kcal \u00b7 P ${food.proteinGrams}g \u00b7 C ${food.carbsGrams}g \u00b7 F ${food.fatGrams}g",
                    style = MaterialTheme.typography.bodySmall,
                    color = ArcOnSurfaceMuted
                )
                if (food.isJunkFood) {
                    Spacer(Modifier.height(6.dp))
                    ArcBadge(text = "Treat", accent = ArcWarningAmber)
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Remove entry", tint = ArcOnSurfaceMuted)
            }
        }
    }
}

@Composable
private fun ManualFoodDialog(
    onDismiss: () -> Unit,
    onConfirm: (label: String, calories: Int, protein: Int, carbs: Int, fat: Int, isJunk: Boolean) -> Unit
) {
    var label by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }
    var isJunk by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log a food") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Food") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = calories,
                        onValueChange = { calories = it.filter { c -> c.isDigit() } },
                        label = { Text("kcal") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = protein,
                        onValueChange = { protein = it.filter { c -> c.isDigit() } },
                        label = { Text("Protein g") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = carbs,
                        onValueChange = { carbs = it.filter { c -> c.isDigit() } },
                        label = { Text("Carbs g") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = fat,
                        onValueChange = { fat = it.filter { c -> c.isDigit() } },
                        label = { Text("Fat g") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isJunk, onCheckedChange = { isJunk = it })
                    Spacer(Modifier.width(6.dp))
                    Text("This was more of a treat", style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        label.ifBlank { "Food" },
                        calories.toIntOrNull() ?: 0,
                        protein.toIntOrNull() ?: 0,
                        carbs.toIntOrNull() ?: 0,
                        fat.toIntOrNull() ?: 0,
                        isJunk
                    )
                }
            ) { Text("Log it") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.height(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Cancel")
            }
        }
    )
}
