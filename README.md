# Arc Season — Phase 3

Builds on Phase 2: the Macros tab now runs on a real BMR/TDEE calculator
with a proper onboarding flow, instead of a hardcoded seeded profile.
Rules, Schedule, and Workout are unchanged from Phase 2. Coach is still
keyword-driven/mock — real OpenAI/Gemini function-calling is Phase 4.

## What changed since Phase 2

```
app/src/main/java/com/arcseason/app/
├── domain/BmrCalculator.kt           NEW — Mifflin-St Jeor + TDEE + macro split
├── data/repository/NutritionRepository.kt   ensureDefaultProfileSeeded() replaced
│                                             with saveProfile(inputs, guardianAcknowledged)
├── ui/macros/
│   ├── MacrosViewModel.kt            gained needsOnboarding + saveProfile()
│   ├── MacrosScreen.kt               shows OnboardingFlow when no profile exists,
│   │                                 plus an "Edit profile" header action
│   └── OnboardingFlow.kt             NEW — 2-step profile setup dialog
└── ui/common/CommonComponents.kt     SectionHeader gained an optional trailing slot
```

## How the calculator works (`domain/BmrCalculator.kt`)

- **BMR** — Mifflin-St Jeor: `10×weight(kg) + 6.25×height(cm) - 5×age + s`,
  where `s` is +5 (male), -161 (female), or their midpoint -78 for "other"
  (guessing wrong in either direction isn't better than splitting it).
- **TDEE** — BMR × an activity multiplier (1.2 sedentary → 1.9 very active).
- **Calorie target** — TDEE - 300 kcal for fat loss, TDEE + 200 for muscle
  gain, TDEE as-is for maintenance — **then hard-floored at BMR** no matter
  what. This floor is applied last, so nothing upstream can override it.
- **Protein** — 1.8 g/kg body weight (the middle of the 1.6-2.2 g/kg range),
  computed from body weight directly rather than as a percentage of the
  calorie target, so a lower calorie target never drags protein down too.
- **Fat / carbs** — fat at 25% of the calorie target, carbs get whatever's
  left, both floored at 0.

## The onboarding flow (`ui/macros/OnboardingFlow.kt`)

A 2-step full-screen dialog:

1. **Inputs** — age, sex, height, weight, activity level, goal.
2. **Targets preview** — shows the computed BMR/TDEE/calorie/macro numbers,
   plus (only the first time, before `guardianAwareOfGoal` is ever set) a
   note suggesting the person loop a parent, guardian, or doctor in before
   starting a deliberate calorie deficit or surplus. It's an optional
   checkbox, not a gate — the app doesn't block anyone from continuing
   either way, it just makes sure the suggestion is seen once.

First launch (`needsOnboarding == true` because no `UserProfileEntity`
exists yet) shows this non-dismissibly, since there's no sensible fallback
to show instead. The Macros tab's new "Edit profile" button reopens the
same flow, pre-filled, dismissible, and without re-showing the guardian
note if it's already been acknowledged.

## Getting an actual .apk

Same as before — no Android SDK or network in this sandbox. Open in Android
Studio and Build → Build APK(s), or push to GitHub and let
`.github/workflows/build.yml` build it.

## Next phase

- **Phase 4:** real OpenAI/Gemini function-calling behind
  `domain/CoachIntent.kt`'s existing `CoachAction` shape, and the
  document/routine text parser.

## Opening the project

Open the `ArcSeason/` folder in Android Studio directly — it regenerates
`gradlew`'s jar on first sync. Requires JDK 17 and compileSdk 35.

