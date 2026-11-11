# AGENTS.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

Tab Shifter: an IntelliJ Platform plugin (Kotlin) with actions to move editor tabs between splitters, move focus between splitters, and resize/maximize/equalize splitters. Plugin id `socrates.tabshifter`.

## Build

Gradle with the IntelliJ Platform Gradle Plugin 2.x; CI uses JDK 21. Gradle 8.13 fails to configure on JDK 25 ("Type T not present"), so run with `JAVA_HOME` pointing at a JDK 21 if the default JDK is newer.

- `./gradlew buildPlugin` — builds the plugin zip into `build/distributions/` (CI runs `./gradlew check buildPlugin`).
- `./gradlew runIde` — launches a sandbox IDE with the plugin installed (the main way to exercise changes).
- `./gradlew publishPlugin` — publishes using the `PUBLISH_TOKEN` env var (published as hidden).

There is no Gradle test source set: `check` only compiles. The only tests are `plugin-test.groovy`, integration tests meant to be run inside a running IDE via the LivePlugin plugin (it loads `build/libs/tab-shifter.jar` via `add-to-classpath`). `plugin.groovy` similarly re-registers the actions through LivePlugin for live reloading. Both scripts are stale relative to the Kotlin code (they reference the old `tabshifter.valueobjects` package and an `Actions.EvenSplitter` class that no longer exist) and need updating before use.

## Versioning and compatibility

- The target IDE SDK is `ideVersion` in `build.gradle.kts` (IntelliJ IDEA).
- Plugin version and `since-build` are maintained by hand in `resources/META-INF/plugin.xml`. Patching of since/until is deliberately disabled in `build.gradle.kts` so `since-build` can name a major release and `until-build` stays open-ended. A release typically bumps `<version>`, `since-build`, and `ideVersion` together.
- Sources live directly in `src/` (not `src/main/kotlin`) and resources in `resources/`, configured via `sourceSets` in `build.gradle.kts`.

## Architecture

Three layers, all under `src/tabshifter/`:

1. **`Actions.kt`** — thin `AnAction` classes (registered in `plugin.xml` with per-keymap shortcuts) that build a `TabShifter(Ide(...))` per invocation and call one method on it.
2. **`TabShifter.kt`** — IDE-independent logic. It works on a snapshot of the editor layout as a binary tree of `layout.Split` / `layout.Window` (`src/tabshifter/layout/`). Every `Window` has size 1×1, so `updatePositions()` assigns positions on a logical grid (not pixels); neighbor lookup (`findWindowNextTo`) matches shared edges in that grid.
3. **`Ide.kt`** — the only place touching IntelliJ APIs. It builds the layout snapshot by walking the Swing component tree under `editorManager.splitters` (`Splitter` → `IdeSplitter`, `JPanel`/`JBTabs` → `IdeWindow`), and performs mutations (split, open/close file, pin, proportions, tool window hide/restore). `IdeSplitter`/`IdeWindow` subclass the layout types to carry the underlying IntelliJ objects; `Ide` methods downcast to them.

Key non-obvious points:

- **Orientation is inverted** between this plugin and IntelliJ: `Split.Orientation.vertical` means side-by-side panes, which is an IntelliJ `Splitter` with `isVertical == false` (see `IdeSplitter`).
- **Moving a tab must work for non-standard editors** (e.g. agent sessions backed by a custom virtual file system). `Ide.moveFile`/`moveFileToNewSplitter` therefore: use the exact `VirtualFile` instance from the source window's `fileList` (not `VirtualFileManager.findFileByUrl`); set `FileEditorManagerKeys.CLOSING_TO_REOPEN` around close+reopen so editors that release state on `fileClosed` survive; and close the file *before* splitting so a `FORBID_TAB_SPLIT` file is never open in two windows. This mirrors the platform's `MoveEditorToOppositeTabGroupAction`.
- Moving left/up never creates a new splitter; moving right/down does (unless the window has only one tab). The moved file's pinned state is preserved.
- Maximize state is stored per-project in a `WeakHashMap<Splitter, MaximizeState>` user-data key; maximizing also hides all tool windows and restoring brings back the saved tool window layout.
- `EditorWindow_AccessToPanel_Hack` reads `EditorWindow`'s non-public `component` property via Kotlin reflection; this is a likely breakage point on new IDE versions, along with other `UnstableApiUsage` calls in `Ide.kt`.

## Keymaps

Shortcuts are defined in `plugin.xml` separately for `$default` and both macOS keymaps (`Mac OS X`, `Mac OS X 10.5+`, with `replace-all="true"`). Comments there record which combinations were rejected due to clashes. Keep `README.md` and the `plugin.xml` description in sync when changing shortcuts.
