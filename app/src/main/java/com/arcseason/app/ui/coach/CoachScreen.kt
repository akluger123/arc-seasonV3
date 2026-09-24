package com.arcseason.app.ui.coach

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.arcseason.app.domain.CoachAction
import com.arcseason.app.domain.CoachIntentParser
import com.arcseason.app.ui.common.SectionHeader
import com.arcseason.app.ui.theme.ArcBackground
import com.arcseason.app.ui.theme.ArcOnBackground
import com.arcseason.app.ui.theme.ArcOnSurfaceMuted
import com.arcseason.app.ui.theme.ArcSurface
import com.arcseason.app.ui.theme.NeonEmerald
import kotlinx.coroutines.launch

private data class ChatBubble(
    val id: Int,
    val isUser: Boolean,
    val text: String,
    val actionSummary: String? = null
)

private fun summarize(action: CoachAction): String? = when (action) {
    is CoachAction.UpdateSchedulePreset ->
        "\u2699 updateDailySchedulePreset(${action.presetType}, wakeUpTime=${action.wakeUpTime})"
    is CoachAction.LogNutritionEntry ->
        "\u2699 logNutritionEntry(${action.foodName}, ${action.calories} kcal, ${action.proteinGrams} g protein)"
    CoachAction.CheckRuleStatus -> "\u2699 checkRuleStatus()"
    CoachAction.None -> null
}

@Composable
fun CoachScreen() {
    val messages = remember {
        mutableStateListOf(
            ChatBubble(
                id = 0,
                isUser = false,
                text = "Hey — I'm Arc Coach. Try something like \"I don't have school today\" or " +
                    "\"I had 3 eggs and some oats\" and I'll update your schedule or macros for you."
            )
        )
    }
    var input by remember { mutableStateOf("") }
    var nextId by remember { mutableStateOf(1) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    fun send() {
        val text = input.trim()
        if (text.isBlank()) return

        messages.add(ChatBubble(id = nextId++, isUser = true, text = text))
        input = ""

        val reply = CoachIntentParser.parse(text)
        messages.add(
            ChatBubble(
                id = nextId++,
                isUser = false,
                text = reply.message,
                actionSummary = summarize(reply.action)
            )
        )

        scope.launch { listState.animateScrollToItem(messages.lastIndex) }
    }

    Scaffold(
        containerColor = ArcBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            Surface(color = ArcSurface) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Message Arc Coach\u2026") },
                        maxLines = 4
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = { send() }, enabled = input.isNotBlank()) {
                        Icon(Icons.Filled.Send, contentDescription = "Send", tint = NeonEmerald)
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                SectionHeader(
                    title = "Arc Coach",
                    subtitle = "Keyword-driven mock replies for now \u2014 real OpenAI/Gemini wiring lands in a later phase"
                )
            }
            items(messages, key = { it.id }) { bubble ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = if (bubble.isUser) Arrangement.End else Arrangement.Start
                ) {
                    ChatBubbleContent(bubble)
                }
            }
        }
    }
}

@Composable
private fun ChatBubbleContent(bubble: ChatBubble) {
    Column(horizontalAlignment = if (bubble.isUser) Alignment.End else Alignment.Start) {
        Surface(
            color = if (bubble.isUser) NeonEmerald else ArcSurface,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (bubble.isUser) 16.dp else 2.dp,
                bottomEnd = if (bubble.isUser) 2.dp else 16.dp
            ),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                bubble.text,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                color = if (bubble.isUser) Color.Black else ArcOnBackground,
                style = MaterialTheme.typography.bodyLarge
            )
        }
        bubble.actionSummary?.let {
            Spacer(Modifier.height(4.dp))
            Text(it, style = MaterialTheme.typography.bodySmall, color = ArcOnSurfaceMuted)
        }
    }
}
