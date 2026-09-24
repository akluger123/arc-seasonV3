package com.arcseason.app.ui.schedule

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.arcseason.app.data.local.entity.ScheduleBlockEntity
import com.arcseason.app.domain.ScheduleBlockType
import com.arcseason.app.domain.SchedulePresetType
import com.arcseason.app.ui.common.ArcCard
import com.arcseason.app.ui.common.SectionHeader
import com.arcseason.app.ui.common.contentPadding
import com.arcseason.app.ui.theme.ArcBackground
import com.arcseason.app.ui.theme.ArcCarbBlue
import com.arcseason.app.ui.theme.ArcOnSurfaceMuted
import com.arcseason.app.ui.theme.ArcWarningAmber
import com.arcseason.app.ui.theme.NeonEmerald

private fun colorFor(type: ScheduleBlockType): Color = when (type) {
    ScheduleBlockType.WAKE, ScheduleBlockType.WORKOUT, ScheduleBlockType.SLEEP -> NeonEmerald
    ScheduleBlockType.MEAL, ScheduleBlockType.WIND_DOWN -> ArcWarningAmber
    ScheduleBlockType.RIDE, ScheduleBlockType.STUDY -> ArcCarbBlue
    ScheduleBlockType.SCHOOL -> ArcOnSurfaceMuted
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(viewModel: ScheduleViewModel = viewModel()) {
    val selectedPreset by viewModel.selectedPreset.collectAsStateWithLifecycle()
    val blocks by viewModel.blocks.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = ArcBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }, containerColor = NeonEmerald) {
                Icon(Icons.Filled.Add, contentDescription = "Add block", tint = Color.Black)
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding(innerPadding)
        ) {
            item {
                SectionHeader(title = "Schedule", subtitle = "Pick today's mode, then fine-tune the blocks")
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SchedulePresetType.entries.forEach { preset ->
                        FilterChip(
                            selected = preset == selectedPreset,
                            onClick = { viewModel.selectPreset(preset) },
                            label = { Text(preset.displayName) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonEmerald,
                                selectedLabelColor = Color.Black
                            )
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            if (blocks.isEmpty()) {
                item {
                    Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                        ArcCard {
                            Text(
                                "No blocks yet for ${selectedPreset.displayName}. Tap + to add the first one.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = ArcOnSurfaceMuted
                            )
                        }
                    }
                }
            }

            items(blocks, key = { it.id }) { block ->
                Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                    ScheduleBlockRow(block = block, onDelete = { viewModel.deleteBlock(block) })
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    if (showAddDialog) {
        AddBlockDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { type, label, start, end ->
                viewModel.addBlock(type, label, start, end)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun ScheduleBlockRow(block: ScheduleBlockEntity, onDelete: () -> Unit) {
    val type = ScheduleBlockType.entries.find { it.name == block.blockType } ?: ScheduleBlockType.MEAL
    ArcCard {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(colorFor(type))
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(block.label, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${block.startTime} \u2013 ${block.endTime}  \u00b7  ${type.displayName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = ArcOnSurfaceMuted
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete block", tint = ArcOnSurfaceMuted)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddBlockDialog(
    onDismiss: () -> Unit,
    onConfirm: (ScheduleBlockType, String, String, String) -> Unit
) {
    var label by remember { mutableStateOf("") }
    var start by remember { mutableStateOf("") }
    var end by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(ScheduleBlockType.WORKOUT) }
    var typeMenuExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add schedule block") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Label") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                ExposedDropdownMenuBox(
                    expanded = typeMenuExpanded,
                    onExpandedChange = { typeMenuExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedType.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeMenuExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = typeMenuExpanded,
                        onDismissRequest = { typeMenuExpanded = false }
                    ) {
                        ScheduleBlockType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.displayName) },
                                onClick = {
                                    selectedType = type
                                    typeMenuExpanded = false
                                }
                            )
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = start,
                        onValueChange = { start = it },
                        label = { Text("Start (HH:mm)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = end,
                        onValueChange = { end = it },
                        label = { Text("End (HH:mm)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val safeLabel = label.ifBlank { selectedType.displayName }
                    val safeStart = start.ifBlank { "00:00" }
                    val safeEnd = end.ifBlank { "00:30" }
                    onConfirm(selectedType, safeLabel, safeStart, safeEnd)
                }
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Cancel")
            }
        }
    )
}
