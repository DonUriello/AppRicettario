# Ricettario

A personal, ad-free Android app for recipes, meal planning and shopping lists.
The full specification lives in the project brief; this is **phase 1, the local MVP**.

## Build and test

Requirements: JDK 17+, Android SDK with platform 37 (`local.properties` → `sdk.dir=...`).

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

All tests run on the JVM: domain logic as plain JUnit, Room and the Compose UI through Robolectric
(including an end-to-end test of the main flow that also saves screenshots to `app/build/screenshots`).

Debug builds seed five sample recipes on first launch; every build starts with the default staples
(`sale`, `olio extravergine`, `pepe`).

## Layout

```
app/src/main/kotlin/com/example/recipes
├── data/database     Room entities, DAOs (shopping list aggregation query in ShoppingListDao), seeder
├── data/repository   RecipeRepository, MealPlanRepository, ShoppingListRepository
├── domain            parser, aisle guessing, quantity formatting, shopping list grouping (pure Kotlin)
├── ui                theme (colours, fonts, mountain header), components, one package per screen
└── di                Hilt module
```

Fonts: Inter and DM Sans, bundled as static instances in `res/font` (SIL Open Font License, see `licenses/`).
