package com.arcseason.app.ui.macros

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.arcseason.app.data.local.entity.UserProfileEntity
import com.arcseason.app.domain.BmrCalculator
import com.arcseason.app.ui.common.ArcCard
import com.arcseason.app.ui.theme.ArcOnSurfaceMuted
import com.arcseason.app.ui.theme.ArcSurface
import com.arcseason.app.ui.theme.ArcWarningAmber
import com.arcseason.app.ui.theme.NeonEmerald

private val SEX_OPTIONS = listOf("MALE" to "Male", "FEMALE" to "Female", "OTHER" to "Other")
private val GOAL_OPTIONS = listOf("FAT_LOSS" to "Fat Loss", "MAINTENANCE" to "Maintain", "MUSCLE_GAIN" to "Muscle Gain")
private val ACTIVITY_OPTIONS = listOf(
    "SEDENTARY" to "Sedentary (little exercise)",
    "LIGHT" to "Light (1-3 days/week)",
    "MODERATE" to "Moderate (3-5 days/week)",
    "ACTIVE" to "Active (6-7 days/week)",
    "VERY_ACTIVE" to "Very active (2x/day or physical job)"
)

/**
 * Full-screen profile setup. [dismissible] = false for first-run onboarding
 * (no profile exists yet, so there's nothing sensible to fall back to);
 * true for the "Edit profile" re-entry point from the Macros screen, which
 * also skips re-showing the guardian note once it's already been
 * acknowledged once.
 */
@Composable
fun OnboardingFlow(
    existingProfile: UserProfileEntity?,
    dismissible: Boolean,
    onDismiss: () -> Unit,
    onSave: (BmrCalculator.Inputs, guardianAcknowledged: Boolean) -> Unit
) {
    var step by remember { mutableStateOf(1) }
    var ageInput by remember { mutableStateOf(existingProfile?.ageYears?.toString() ?: "15") }
    var sexInput by remember { mutableStateOf(existingProfile?.sex ?: "MALE") }
    var heightInput by remember { mutableStateOf(existingProfile?.heightCm?.toInt()?.toString() ?: "") }
    var weightInput by remember { mutableStateOf(existingProfile?.weightKg?.toInt()?.toString() ?: "") }
    var activityInput by remember { mutableStateOf(existingProfile?.activityLevel ?: "MODERATE") }
    var goalInput by remember { mutableStateOf(existingProfile?.goal ?: "FAT_LOSS") }
    var guardianAcknowledged by remember { mutableStateOf(existingProfile?.guardianAwareOfGoal ?: false) }

    val alreadyAcknowledgedGuardianNote = existingProfile?.guardianAwareOfGoal == true

    Dialog(
        onDismissRequest = { if (dismissible) onDismiss() },
        properties = DialogProperties(dismissOnBackPress = dismissible, dismissOnClickOutside = dismissible)
    ) {
        Surface(color = ArcSurface, shape = RoundedCornerShape(24.dp)) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                if (step == 1) {
                    Text("Let's set your targets", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "This drives your calorie and macro numbers on the Macros tab.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ArcOnSurfaceMuted
                    )
                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = ageInput,
                        onValueChange = { ageInput = it.filter { c -> c.isDigit() } },
                        label = { Text("Age") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(10.dp))

                    Text("Sex", style = MaterialTheme.typography.labelLarge, color = ArcOnSurfaceMuted)
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SEX_OPTIONS.forEach { (value, label) ->
                            FilterChip(
                                selected = sexInput == value,
                                onClick = { sexInput = value },
                                label = { Text(label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NeonEmerald,
                                    selectedLabelColor = androidx.compose.ui.graphics.Color.Black
                                )
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = heightInput,
                            onValueChange = { heightInput = it.filter { c -> c.isDigit() } },
                            label = { Text("Height (cm)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = weightInput,
                            onValueChange = { weightInput = it.filter { c -> c.isDigit() } },
                            label = { Text("Weight (kg)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(Modifier.height(10.dp))

                    Text("Activity level", style = MaterialTheme.typography.labelLarge, color = ArcOnSurfaceMuted)
                    Spacer(Modifier.height(6.dp))
                    ActivityDropdown(selected = activityInput, onSelected = { activityInput = it })
                    Spacer(Modifier.height(10.dp))

                    Text("Goal", style = MaterialTheme.typography.labelLarge, color = ArcOnSurfaceMuted)
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        GOAL_OPTIONS.forEach { (value, label) ->
                            FilterChip(
                                selected = goalInput == value,
                                onClick = { goalInput = value },
                                label = { Text(label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NeonEmerald,
                                    selectedLabelColor = androidx.compose.ui.graphics.Color.Black
                                )
                            )
                        }
                    }
                    Spacer(Modifier.height(20.dp))

                    val canContinue = ageInput.isNotBlank() && heightInput.isNotBlank() && weightInput.isNotBlank()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (dismissible) Arrangement.SpaceBetween else Arrangement.End
                    ) {
                        if (dismissible) {
                            TextButton(onClick = onDismiss) { Text("Cancel") }
                        }
                        Button(
                            onClick = { step = 2 },
                            enabled = canContinue,
                            colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald, contentColor = androidx.compose.ui.graphics.Color.Black)
                        ) { Text("See my targets") }
                    }
                } else {
                    val inputs = BmrCalculator.Inputs(
                        ageYears = ageInput.toIntOrNull() ?: 15,
                        sex = sexInput,
                        heightCm = (heightInput.toIntOrNull() ?: 170).toDouble(),
                        weightKg = (weightInput.toIntOrNull() ?: 65).toDouble(),
                        activityLevel = activityInput,
                        goal = goalInput
                    )
                    val result = BmrCalculator.calculate(inputs)

                    Text("Your targets", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Based on a conservative estimate \u2014 never below your BMR of ${result.bmrKcal} kcal.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ArcOnSurfaceMuted
                    )
                    Spacer(Modifier.height(16.dp))

                    ArcCard {
                        TargetRow("Maintenance (TDEE)", "${result.tdeeKcal} kcal")
                        TargetRow("Daily target", "${result.calorieTargetKcal} kcal")
                        TargetRow("Protein", "${result.proteinTargetGrams} g")
                        TargetRow("Carbs", "${result.carbsTargetGrams} g")
                        TargetRow("Fat", "${result.fatTargetGrams} g")
                    }

                    if (!alreadyAcknowledgedGuardianNote) {
                        Spacer(Modifier.height(16.dp))
                        Surface(
                            color = ArcWarningAmber.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    "Since you're still growing, it's worth looping a parent, guardian, or " +
                                        "doctor in before starting a calorie deficit or surplus. This app won't " +
                                        "stop you either way \u2014 we'd just rather you have someone in your corner.",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(Modifier.height(10.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = guardianAcknowledged,
                                        onCheckedChange = { guardianAcknowledged = it },
                                        colors = CheckboxDefaults.colors(checkedColor = NeonEmerald)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text("I'll talk to someone about this", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        TextButton(onClick = { step = 1 }) { Text("Back") }
                        Button(
                            onClick = {
                                onSave(inputs, guardianAcknowledged || alreadyAcknowledgedGuardianNote)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald, contentColor = androidx.compose.ui.graphics.Color.Black)
                        ) { Text("Save & start tracking") }
                    }
                }
            }
        }
    }
}

@Composable
private fun TargetRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = ArcOnSurfaceMuted)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActivityDropdown(selected: String, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val label = ACTIVITY_OPTIONS.find { it.first == selected }?.second ?: ACTIVITY_OPTIONS[2].second

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = label,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            ACTIVITY_OPTIONS.forEach { (value, displayLabel) ->
                DropdownMenuItem(
                    text = { Text(displayLabel) },
                    onClick = {
                        onSelected(value)
                        expanded = false
                    }
                )
            }
        }
    }
}
