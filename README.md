# Ricettario

A personal, ad-free Android app for recipes, meal planning and shopping lists.
The full specification lives in the project brief. Done so far: **phase 1** (local MVP) and **phase 2** (import from a web page).

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

## Import from a web page

"Importa da link" on the recipe list downloads the page, reads its schema.org `Recipe` JSON-LD
(also inside arrays and `@graph`, with `HowToStep` / `HowToSection` instructions) and saves a draft
(`NEEDS_REVIEW`). Ingredients without any quantity are flagged with a review note and must be
confirmed, edited or removed on the review screen before the recipe can be saved as verified.
Sharing a page from the browser ("Condividi → Importa in Ricettario") does the same: the app keeps
the first link in the shared text and starts the import.
Parser tests use saved HTML fixtures in `app/src/test/resources/fixtures`.

## Layout

```
app/src/main/kotlin/com/example/recipes
├── data/database     Room entities, DAOs (shopping list aggregation query in ShoppingListDao), seeder
├── data/repository   RecipeRepository, MealPlanRepository, ShoppingListRepository
├── data/importing    JSON-LD recipe parser, HTML downloader (OkHttp), web importer
├── domain            parser, aisle guessing, quantity formatting, shopping list grouping (pure Kotlin)
├── ui                theme (colours, fonts, mountain header), components, one package per screen
└── di                Hilt module
```

Fonts: Inter and DM Sans, bundled as static instances in `res/font` (SIL Open Font License, see `licenses/`).
