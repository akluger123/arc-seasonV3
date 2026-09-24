package com.arcseason.app.ui.workout

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
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import kotlinx.coroutines.delay

private data class PlanDay(
    val day: String,
    val focus: String,
    val exercises: List<String>
)

private val mockWeekPlan = listOf(
    PlanDay("Mon", "Push", listOf("Bench Press 4x8", "Overhead Press 3x10", "Push-ups 3xMax")),
    PlanDay("Tue", "Bike + Core", listOf("Bike Ride 45min", "Plank 5x1min", "Hanging Knee Raise 3x12")),
    PlanDay("Wed", "Pull", listOf("Pull-ups 4x6", "Barbell Row 3x10", "Bicep Curl 3x12")),
    PlanDay("Thu", "Rest / Outside Time", listOf("20 min walk or light bike", "Stretch 10min")),
    PlanDay("Fri", "Legs", listOf("Squat 4x8", "Romanian Deadlift 3x10", "Calf Raise 3x15")),
    PlanDay("Sat", "Bike (Long)", listOf("Bike Ride 60\u201390min", "Plank 5x1min")),
    PlanDay("Sun", "Rest", listOf("Family time, gratitude + wins journal"))
)

private fun formatSeconds(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return if (minutes > 0) "${minutes}m ${seconds}s" else "${seconds}s"
}

@Composable
fun WorkoutScreen(viewModel: WorkoutViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = ArcBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding(innerPadding)
        ) {
            item {
                SectionHeader(title = "Workout", subtitle = "This week's training, plank, and push-up challenge")
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ArcWeeklyRingCard(label = "Gym", current = uiState.gymWeeklyCount, target = 4)
                    ArcWeeklyRingCard(label = "Bike", current = uiState.cardioWeeklyCount, target = 4)
                }
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(onClick = { viewModel.logGymSession() }, modifier = Modifier.weight(1f)) {
                        Text("Log Gym Session")
                    }
                    OutlinedButton(onClick = { viewModel.logCardioSession() }, modifier = Modifier.weight(1f)) {
                        Text("Log Bike Ride")
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            item {
                Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                    PlankTimerCard(
                        todaySeconds = uiState.plankTodaySeconds,
                        bestSeconds = uiState.plankBestSeconds,
                        onSave = { seconds -> viewModel.savePlank(seconds) }
                    )
                }
            }

            item {
                Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                    PushupCounterCard(
                        todayReps = uiState.pushupTodayReps,
                        bestReps = uiState.pushupBestReps,
                        history = uiState.pushupHistory,
                        onSave = { reps -> viewModel.savePushups(reps) }
                    )
                }
            }

            item {
                Text(
                    "This week's plan",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }

            items(mockWeekPlan) { planDay ->
                Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                    PlanDayRow(planDay)
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun PlankTimerCard(todaySeconds: Int?, bestSeconds: Int, onSave: (Int) -> Unit) {
    var elapsedSeconds by remember { mutableIntStateOf(0) }
    var isRunning by remember { mutableStateOf(false) }

    LaunchedEffect(isRunning) {
        while (isRunning) {
            delay(1000)
            elapsedSeconds += 1
        }
    }

    ArcCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(ArcRuleType.PLANK.displayName, style = MaterialTheme.typography.titleMedium)
                Text(ArcRuleType.PLANK.description, style = MaterialTheme.typography.bodySmall, color = ArcOnSurfaceMuted)
            }
            ArcBadge(text = "Best: ${formatSeconds(bestSeconds)}")
        }
        Spacer(Modifier.height(12.dp))
        Text(formatSeconds(elapsedSeconds), style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FilledTonalButton(onClick = { isRunning = !isRunning }) {
                Icon(if (isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text(if (isRunning) "Pause" else "Start")
            }
            OutlinedButton(onClick = {
                isRunning = false
                elapsedSeconds = 0
            }) {
                Icon(Icons.Filled.Refresh, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Reset")
            }
            Button(
                onClick = {
                    isRunning = false
                    onSave(elapsedSeconds)
                    elapsedSeconds = 0
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald, contentColor = androidx.compose.ui.graphics.Color.Black),
                enabled = elapsedSeconds > 0
            ) { Text("Save") }
        }
        todaySeconds?.let {
            Spacer(Modifier.height(8.dp))
            Text("Logged ${formatSeconds(it)} for today", style = MaterialTheme.typography.bodySmall, color = ArcOnSurfaceMuted)
        }
    }
}

@Composable
private fun PushupCounterCard(todayReps: Int?, bestReps: Int, history: List<Int>, onSave: (Int) -> Unit) {
    var reps by remember { mutableIntStateOf(0) }

    ArcCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(ArcRuleType.PUSHUPS.displayName, style = MaterialTheme.typography.titleMedium)
                Text(ArcRuleType.PUSHUPS.description, style = MaterialTheme.typography.bodySmall, color = ArcOnSurfaceMuted)
            }
            ArcBadge(text = "Best: $bestReps reps")
        }
        Spacer(Modifier.height(12.dp))
        Text("$reps reps", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = { if (reps > 0) reps -= 1 }) { Text("-1") }
            OutlinedButton(onClick = { reps += 1 }) { Text("+1") }
            OutlinedButton(onClick = { reps += 5 }) { Text("+5") }
            OutlinedButton(onClick = { reps = 0 }) { Text("Reset") }
        }
        Spacer(Modifier.height(10.dp))
        Button(
            onClick = {
                onSave(reps)
                reps = 0
            },
            colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald, contentColor = androidx.compose.ui.graphics.Color.Black),
            enabled = reps > 0
        ) { Text("Save today's max") }
        todayReps?.let {
            Spacer(Modifier.height(8.dp))
            Text("Logged $it reps for today", style = MaterialTheme.typography.bodySmall, color = ArcOnSurfaceMuted)
        }
        if (history.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(
                "History: ${history.joinToString(", ")}",
                style = MaterialTheme.typography.bodySmall,
                color = ArcOnSurfaceMuted
            )
        }
    }
}

@Composable
private fun PlanDayRow(planDay: PlanDay) {
    var expanded by remember { mutableStateOf(false) }
    ArcCard {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = if (expanded) 8.dp else 0.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ArcBadge(text = planDay.day)
                Spacer(Modifier.width(10.dp))
                Text(planDay.focus, style = MaterialTheme.typography.titleMedium)
            }
            IconButton(onClick = { expanded = !expanded }) {
                Icon(
                    if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = ArcOnSurfaceMuted
                )
            }
        }
        if (expanded) {
            Column {
                planDay.exercises.forEach { exercise ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                        Text("\u2022  ", color = NeonEmerald, style = MaterialTheme.typography.bodyMedium)
                        Text(exercise, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
