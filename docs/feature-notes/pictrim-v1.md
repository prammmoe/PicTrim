# PicTrim v1 Implementation Notes

Branch: `no Git metadata available in workspace`

## 2026-08-24 - Offline image compressor and resizer MVP

### Changed
- Replaced the placeholder Compose shell with Home, Preview, Editor, and Result destinations backed by Hilt ViewModels and immutable `StateFlow` state.
- Added Android Photo Picker, local metadata reading, sampled bitmap processing, EXIF orientation/metadata handling, JPG/PNG/WebP output, resize, approximate target size, MediaStore save, and FileProvider sharing.
- Switched the package ID to `com.prammmoe.pictrim`; added core Indonesian resource overrides and unit tests for image-processing rules.
- Removed the remaining `com.example.pictrim` template sources and template tests so all source sets use the production namespace.
- Removed the now-empty legacy source directories so Android Studio no longer presents `com.example.pictrim` under the Android test source set.
- Explicitly targeted the Hilt application-context qualifier at the repository constructor parameter, resolving the Kotlin annotation-target warning.

### Bugs And Fixes
- Bug: `kapt` cannot run with AGP 9 built-in Kotlin.
  Fix: migrated Hilt processing to KSP and upgraded Hilt to 2.59.2 for AGP 9 support.

### Verification
- `./gradlew testDebugUnitTest assembleDebug` and the final `./gradlew testDebugUnitTest` completed successfully.
- Device-dependent Photo Picker, MediaStore, and share flows require emulator/device instrumentation verification.

## 2026-08-24 - Show saved image in gallery

### Changed
- Added a `ViewSavedImageUseCase` and repository intent factory that open the exact saved MediaStore URI with `ACTION_VIEW` and a read grant.
- The Result screen now exposes **Show in gallery** after a successful save; it does not appear before saving.

### Verification
- `./gradlew testDebugUnitTest assembleDebug` completed successfully.

## 2026-08-25 - Batch, crop, and privacy processing

### Changed
- Added multi-image selection (maximum 50), shared batch options, center-crop aspect presets, and an optional metadata-removal mode for both single and batch processing.
- Added WorkManager-backed sequential batch processing with foreground notification, private temporary outputs, persisted job/item manifest, progress polling, and review-before-save with Save All.
- Added the required WorkManager and foreground-service declarations; timestamped outputs now include milliseconds to avoid batch filename collisions.

### Verification
- `./gradlew testDebugUnitTest assembleDebug` completed successfully.
- Multi-image picker grants, foreground notification behavior, and MediaStore batch export still require device/emulator instrumentation checks.

## 2026-08-25 - Batch worker crash prevention

### Bugs And Fixes
- Bug: Starting batch processing could crash on recent Android releases when WorkManager promoted the worker to a foreground service without a declared service type.
  Fix: declared WorkManager's foreground service as `dataSync`, added the matching service permission and foreground info type, and convert enqueue failures into an editor error state.

### Verification
- `./gradlew testDebugUnitTest assembleDebug` completed successfully.

## 2026-08-25 - Batch previews and gallery links

### Changed
- Batch results now render a thumbnail for each processed output.
- Saved MediaStore URIs are persisted per batch item; each saved item exposes **Open gallery** for an exact-image `ACTION_VIEW` deep link.

### Verification
- `./gradlew testDebugUnitTest assembleDebug` completed successfully.

## 2026-08-25 - Batch back navigation

### Changed
- Added a Material top app bar and pop-back arrow to both Batch Processing and Batch Result destinations; Home remains the root without back navigation.

### Verification
- `./gradlew testDebugUnitTest assembleDebug` completed successfully.

## 2026-08-25 - Minimal monochrome UI system and first-launch onboarding

### Changed
- Replaced the dynamic green theme with a fixed monochrome light/dark token system, shared rounded surfaces, action buttons, option cards, and top bars.
- Redesigned Home, preview, editor, result, batch editor, and batch result around the shared Compose components without changing the image-processing contracts.
- Added three programmatic onboarding pages, local Preferences DataStore persistence, and route gating so onboarding is shown only before its first completion.
- Added complete English and Indonesian copy for the new UI and onboarding flow.

### Stayed The Same
- Image picker, compression, resize, metadata removal, WorkManager batch processing, MediaStore export, gallery, and sharing behavior remain unchanged.

### Bugs And Fixes
- Bug: the programmatic lock illustration passed a `Rect` to a Canvas `drawArc` overload that expects separate position and size values.
  Fix: pass explicit `topLeft` and `size` values.

### Verification
- `./gradlew compileDebugKotlin --console=plain` completed successfully.

## 2026-08-25 - Neutral controls and visible toggles

### Changed
- Added shared neutral FilterChip and Slider colors so selected controls use black/grey rather than Material tonal purple.
- Added a shared Switch style with high-contrast grey tracks, borders, and thumbs; applied it to privacy and aspect-ratio controls in both single and batch editors.

### Verification
- `./gradlew testDebugUnitTest assembleDebug --console=plain` completed successfully.

## 2026-08-25 - Simplify Home

### Changed
- Removed the redundant On-device privacy card from Home; the privacy message remains in onboarding.

## 2026-08-25 - PicTrim launcher icon

### Changed
- Replaced the legacy Android launcher artwork at every density with the supplied scissors-and-photo PicTrim icon.
- Updated adaptive launcher icons to use the new icon foreground against a white background.

## 2026-08-25 - Launcher icon revision

### Changed
- Replaced the launcher artwork with the supplied scissors-and-dashed-cut-line icon across all Android densities and the adaptive foreground.

## 2026-08-25 - Metadata explanation

### Changed
- Added an accessible info icon beside Remove metadata in both processing flows. It opens a dialog explaining that GPS, camera, date, and EXIF data are removed while image pixels remain unchanged.

### Verification
- `./gradlew testDebugUnitTest assembleDebug` completed successfully.
