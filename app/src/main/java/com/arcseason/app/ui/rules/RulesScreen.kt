package com.arcseason.app.ui.rules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.arcseason.app.domain.ArcRuleType
import com.arcseason.app.ui.common.ArcBadge
import com.arcseason.app.ui.common.ArcCard
import com.arcseason.app.ui.common.ArcWeeklyRingCard
import com.arcseason.app.ui.common.SectionHeader
import com.arcseason.app.ui.common.contentPadding
import com.arcseason.app.ui.theme.ArcBackground
import com.arcseason.app.ui.theme.ArcOnSurfaceMuted
import com.arcseason.app.ui.theme.NeonEmerald

private fun formatChallengeSeconds(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return if (minutes > 0) "${minutes}m ${seconds}s" else "${seconds}s"
}

@Composable
fun RulesScreen(viewModel: RulesViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val gratitudeDone = uiState.gratitudeToday.any { it.isNotBlank() }
    val winsDone = uiState.winsToday.any { it.isNotBlank() }
    val doneCount = uiState.simpleRules.count { it.isDone } + (if (gratitudeDone) 1 else 0) + (if (winsDone) 1 else 0)
    val totalCount = uiState.simpleRules.size + 2 // + gratitude + wins

    var editingJournal by remember { mutableStateOf<String?>(null) } // "GRATITUDE" | "WINS" | null

    Scaffold(
        containerColor = ArcBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding(innerPadding)
        ) {
            item {
                SectionHeader(
                    title = "Today's Rules",
                    subtitle = "$doneCount / $totalCount daily check-ins done \u00b7 all 11 rules tracked below"
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ArcWeeklyRingCard(label = "Gym", current = uiState.gymWeeklyCount, target = 4)
                    ArcWeeklyRingCard(label = "Bike", current = uiState.cardioWeeklyCount, target = 4)
                }
            }

            item {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ChallengeStatRow(
                        title = ArcRuleType.PLANK.displayName,
                        todayValue = uiState.plankTodaySeconds?.let { "${formatChallengeSeconds(it)} held today" }
                            ?: "Not attempted today \u2014 see Workout tab",
                        bestValue = "Best: ${formatChallengeSeconds(uiState.plankBestSeconds)}"
                    )
                    Spacer(Modifier.height(8.dp))
                    ChallengeStatRow(
                        title = ArcRuleType.PUSHUPS.displayName,
                        todayValue = uiState.pushupTodayReps?.let { "$it reps today" }
                            ?: "Not attempted today \u2014 see Workout tab",
                        bestValue = "Best: ${uiState.pushupBestReps} reps"
                    )
                }
            }

            items(uiState.simpleRules, key = { it.type.name }) { rule ->
                Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                    RuleCheckRow(rule = rule, onToggle = { viewModel.toggleRule(rule.type) })
                }
            }

            item {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    JournalPreviewCard(
                        title = ArcRuleType.GRATITUDE.displayName,
                        entries = uiState.gratitudeToday,
                        streak = uiState.gratitudeStreak,
                        onEdit = { editingJournal = "GRATITUDE" }
                    )
                    JournalPreviewCard(
                        title = ArcRuleType.WINS.displayName,
                        entries = uiState.winsToday,
                        streak = uiState.winsStreak,
                        onEdit = { editingJournal = "WINS" }
                    )
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }

    if (editingJournal == "GRATITUDE") {
        JournalEditDialog(
            title = ArcRuleType.GRATITUDE.displayName,
            initialEntries = uiState.gratitudeToday,
            onDismiss = { editingJournal = null },
            onSave = { entries ->
                viewModel.saveGratitude(entries)
                editingJournal = null
            }
        )
    } else if (editingJournal == "WINS") {
        JournalEditDialog(
            title = ArcRuleType.WINS.displayName,
            initialEntries = uiState.winsToday,
            onDismiss = { editingJournal = null },
            onSave = { entries ->
                viewModel.saveWins(entries)
                editingJournal = null
            }
        )
    }
}

@Composable
private fun RuleCheckRow(rule: SimpleRuleUiState, onToggle: () -> Unit) {
    ArcCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(rule.type.displayName, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(2.dp))
                Text(
                    rule.type.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = ArcOnSurfaceMuted
                )
                if (rule.streak > 0) {
                    Spacer(Modifier.height(6.dp))
                    ArcBadge(text = "${rule.streak}-day streak")
                }
            }
            Spacer(Modifier.width(8.dp))
            Checkbox(
                checked = rule.isDone,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(checkedColor = NeonEmerald)
            )
        }
    }
}

@Composable
private fun JournalPreviewCard(title: String, entries: List<String>, streak: Int, onEdit: () -> Unit) {
    ArcCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                if (streak > 0) {
                    Spacer(Modifier.width(8.dp))
                    ArcBadge(text = "$streak-day streak")
                }
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Filled.Edit, contentDescription = "Edit $title", tint = ArcOnSurfaceMuted)
            }
        }
        Spacer(Modifier.height(8.dp))
        val filled = entries.filter { it.isNotBlank() }
        if (filled.isEmpty()) {
            Text("Not filled in yet today \u2014 tap edit to add 5.", style = MaterialTheme.typography.bodyMedium, color = ArcOnSurfaceMuted)
        } else {
            filled.forEach { entry ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                    Text("\u2022  ", color = NeonEmerald, style = MaterialTheme.typography.bodyMedium)
                    Text(entry, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun ChallengeStatRow(title: String, todayValue: String, bestValue: String) {
    ArcCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(2.dp))
                Text(todayValue, style = MaterialTheme.typography.bodySmall, color = ArcOnSurfaceMuted)
            }
            ArcBadge(text = bestValue)
        }
    }
}

@Composable
private fun JournalEditDialog(
    title: String,
    initialEntries: List<String>,
    onDismiss: () -> Unit,
    onSave: (List<String>) -> Unit
) {
    val fields = remember { List(5) { i -> mutableStateOf(initialEntries.getOrElse(i) { "" }) } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                fields.forEachIndexed { index, field ->
                    OutlinedTextField(
                        value = field.value,
                        onValueChange = { field.value = it },
                        label = { Text("${index + 1}") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(fields.map { it.value }) }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
