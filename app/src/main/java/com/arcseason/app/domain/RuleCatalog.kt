package com.arcseason.app.domain

/**
 * The 11 non-negotiable Arc Rules, as a single source of truth the Phase 2
 * UI, Room rows (`DailyRuleLogEntity.ruleType`), and the Arc Coach's tool
 * definitions all key off of.
 *
 * `NO_JUNK` is labeled "Mindful Eating" rather than "No Junk" and tracked
 * as an ordinary daily check-in (see ArcRuleEntities.kt) rather than a
 * pass/fail gate on the day — see the note there for why.
 */
enum class ArcRuleType(
    val displayName: String,
    val description: String,
    val trackingKind: RuleTrackingKind
) {
    WATER(
        "Water Only",
        "Water as your main drink today.",
        RuleTrackingKind.NUMERIC_OR_BOOLEAN
    ),
    NO_JUNK(
        "Mindful Eating",
        "Keep ultra-processed / junk food to a minimum today.",
        RuleTrackingKind.BOOLEAN
    ),
    SLEEP(
        "8h Sleep",
        "Aim for 8 hours of sleep.",
        RuleTrackingKind.LINKED_TO_SLEEP_LOG
    ),
    GYM(
        "Gym x4/week",
        "Strength train 4 times this week.",
        RuleTrackingKind.WEEKLY_COUNT
    ),
    CARDIO(
        "Bike x4/week",
        "Cardio / bike ride 4 times this week.",
        RuleTrackingKind.WEEKLY_COUNT
    ),
    NO_PHONE_MEALS(
        "Phone-Free Meals",
        "No phone during meals.",
        RuleTrackingKind.TIMER
    ),
    OUTSIDE_TIME(
        "20 Min Outside",
        "Spend at least 20 minutes outside.",
        RuleTrackingKind.TIMER
    ),
    GRATITUDE(
        "Gratitude x5",
        "Write down 5 things you're grateful for.",
        RuleTrackingKind.JOURNAL_5
    ),
    WINS(
        "Wins x5",
        "Track 5 wins from today.",
        RuleTrackingKind.JOURNAL_5
    ),
    PLANK(
        "1-Min Plank x5/week",
        "Hold a plank for 1 minute, 5 days a week.",
        RuleTrackingKind.CHALLENGE_TIMER
    ),
    PUSHUPS(
        "Push-Up Max",
        "Do as many push-ups as you can, and beat your best.",
        RuleTrackingKind.CHALLENGE_REPS
    )
}

enum class RuleTrackingKind {
    NUMERIC_OR_BOOLEAN,
    BOOLEAN,
    LINKED_TO_SLEEP_LOG,
    WEEKLY_COUNT,
    TIMER,
    JOURNAL_5,
    CHALLENGE_TIMER,
    CHALLENGE_REPS
}
