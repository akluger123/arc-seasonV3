package com.arcseason.app.domain

/**
 * Structured "function calls" the Arc Coach can trigger from a chat message.
 * These mirror the JSON tool-calling schema this project hands to the real
 * OpenAI/Gemini API in a later phase:
 *
 *   updateDailySchedulePreset(presetType, wakeUpTime)
 *   logNutritionEntry(foodName, calories, proteinGrams)
 *   checkRuleStatus()
 *
 * For this phase, [CoachIntentParser] recognizes these from plain keyword
 * matching so the Coach tab is fully interactive without needing a live API
 * key yet. Swapping the parser's internals for a real LLM call later
 * doesn't require touching [CoachAction] or the UI that reads it.
 */
sealed interface CoachAction {
    data class UpdateSchedulePreset(val presetType: String, val wakeUpTime: String) : CoachAction
    data class LogNutritionEntry(val foodName: String, val calories: Int, val proteinGrams: Int) : CoachAction
    data object CheckRuleStatus : CoachAction
    data object None : CoachAction
}

data class CoachReply(val message: String, val action: CoachAction)

object CoachIntentParser {

    private val foodMacroTable = linkedMapOf(
        "cottage cheese" to (120 to 14),
        "smoothie" to (250 to 30),
        "chicken" to (165 to 31),
        "tuna" to (130 to 26),
        "oats" to (300 to 10),
        "toast" to (80 to 3),
        "rice" to (200 to 4),
        "eggs" to (70 to 6),
        "egg" to (70 to 6)
    )

    private val encouragement = listOf(
        "Consistency beats intensity — stacking small wins is the whole game.",
        "One rule at a time. What's the next one you can knock out right now?",
        "You don't need a perfect day, just a done one. Keep going.",
        "Discipline is just remembering what you actually want. You've got this."
    )

    fun parse(userText: String): CoachReply {
        val text = userText.lowercase()

        return when {
            "no school" in text -> CoachReply(
                message = "Got it — switching you to a No School day. I moved your wake-up to 9:00 AM " +
                    "and shifted your meals and workout block to match.",
                action = CoachAction.UpdateSchedulePreset(presetType = "NO_SCHOOL", wakeUpTime = "09:00")
            )

            "wake up at" in text || "waking up at" in text -> {
                val time = extractTime(text)
                if (time != null) {
                    CoachReply(
                        message = "Done — I've set tomorrow's wake-up to $time and shifted your morning blocks to match.",
                        action = CoachAction.UpdateSchedulePreset(presetType = "WEEKEND", wakeUpTime = time)
                    )
                } else {
                    CoachReply(
                        message = "What time are you waking up? Something like \"waking up at 9am\" works.",
                        action = CoachAction.None
                    )
                }
            }

            "ate" in text || "had" in text -> estimateNutrition(text)

            "how am i doing" in text || "rules today" in text || "status" in text -> CoachReply(
                message = "You're 7 of 11 rules done today, and on pace for gym (3/4) and bike (2/4) this week. Keep it up!",
                action = CoachAction.CheckRuleStatus
            )

            else -> CoachReply(message = encouragement.random(), action = CoachAction.None)
        }
    }

    private fun extractTime(text: String): String? {
        val afterAt = text.substringAfter("at", missingDelimiterValue = "")
        if (afterAt.isBlank()) return null
        val match = Regex("""(\d{1,2})(:(\d{2}))?\s*(am|pm)?""").find(afterAt) ?: return null
        var hour = match.groupValues[1].toIntOrNull() ?: return null
        val minute = match.groupValues[3].ifBlank { "00" }
        val meridiem = match.groupValues[4]
        if (meridiem == "pm" && hour < 12) hour += 12
        if (meridiem == "am" && hour == 12) hour = 0
        if (hour !in 0..23) return null
        return "%02d:%s".format(hour, minute)
    }

    private fun estimateNutrition(text: String): CoachReply {
        var totalCalories = 0
        var totalProtein = 0
        val foundFoods = mutableListOf<String>()

        // foodMacroTable is checked longest-key-first (see its declaration order:
        // "eggs" appears before "egg") and each match is stripped from `remaining`
        // so "3 eggs" can't also match and double-count as a plain "egg".
        var remaining = text
        foodMacroTable.forEach { (food, macros) ->
            if (food in remaining) {
                totalCalories += macros.first
                totalProtein += macros.second
                foundFoods.add(food)
                remaining = remaining.replace(food, "")
            }
        }

        return if (foundFoods.isNotEmpty()) {
            CoachReply(
                message = "Logged it: ${foundFoods.joinToString(", ")} — about $totalCalories kcal and ${totalProtein}g protein.",
                action = CoachAction.LogNutritionEntry(foundFoods.joinToString(" + "), totalCalories, totalProtein)
            )
        } else {
            CoachReply(
                message = "Logged a meal — I didn't recognize the specific food, so I used a rough placeholder " +
                    "estimate of 200 kcal / 12g protein. You can correct the numbers in Macros.",
                action = CoachAction.LogNutritionEntry("Meal", 200, 12)
            )
        }
    }
}
