# StringBlur

[English](README.md) | [简体中文](README.zh-CN.md)

[![Maven Central](https://img.shields.io/maven-central/v/io.github.dawnuu/stringblur?style=flat-square)](https://central.sonatype.com/artifact/io.github.dawnuu/stringblur)

StringBlur is an Android Gradle plugin that encrypts string constants in class files during the build and decrypts them automatically at runtime.

## Migration to Maven Central

This migration replaces the previous custom GitHub Maven repository with Maven Central. It changes the repository and publication coordinates; the source package remains unchanged.

| Item | Before migration | After migration |
| --- | --- | --- |
| Repository | Previous custom GitHub Maven repository (removed) | `mavenCentral()` |
| Plugin coordinate | `com.android.string.plugin:stringblur:2.1.0` | `io.github.dawnuu:stringblur:1.0.1` |
| Plugin IDs | `stringblur` | `io.github.dawnuu.stringblur` (Plugins DSL); `stringblur` (buildscript) |
| Source packages | `com.android.string.plugin` | `com.android.string.plugin` |

## Installation

### Plugins DSL / Version Catalog

Configure the plugin repository in `settings.gradle` or `settings.gradle.kts`:

```groovy
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
```

Declare the plugin with a version catalog:

```toml
# gradle/libs.versions.toml
[versions]
stringblur = "1.0.1"

[plugins]
stringblur = { id = "io.github.dawnuu.stringblur", version.ref = "stringblur" }
```

Apply it in the root and application/library module build files:

```kotlin
// Root build.gradle.kts
plugins {
    alias(libs.plugins.stringblur) apply false
}

// Application or library module build.gradle.kts
plugins {
    alias(libs.plugins.stringblur)
}
```

The equivalent Groovy DSL uses `alias(libs.plugins.stringblur)` in `build.gradle`.

Without a version catalog:

```kotlin
plugins {
    id("io.github.dawnuu.stringblur") version "1.0.1"
}
```

## Configuration

### Kotlin DSL (`build.gradle.kts`)

```kotlin
import com.android.string.plugin.mode.BytesMode
import com.android.string.plugin.mode.Mode
import com.android.string.plugin.mode.SelectionStrategy

stringblur {
    // Encryption key. Use a string, or an integer for a random key length.
    // Omit this line to resolve the key in this order:
    // 1. gradle.properties or -Pstringblur.key=your-key
    // 2. Environment variable STRINGBLUR_KEY=your-key
    // 3. Root local.properties: stringblur.key=your-key
    key = "my-project-key-2024"
    // Master switch; disabled by default.
    enable = true

    // Empty list: current applicationId/namespace; null: all classes;
    // non-empty list: add these package prefixes to the current scope.
    encodePackages = listOf("com.example")
    // Class names or package prefixes to exclude.
    whiteList = listOf("BuildConfig", "R", "R2")

    // Available algorithms; an empty list falls back to Mode.DEFAULT.
    modes = listOf(Mode.XOR_SIMD, Mode.FAST_ROT, Mode.REVERSE)
    // STRING, BYTES, or RANDOM encrypted-data representation.
    bytesMode = BytesMode.RANDOM
    // Skip strings shorter than this value; negative values become 0.
    minLength = 3

    // Also encrypt debug variants when enabled.
    enableWhenDebug = false

    // RANDOM, SMART, PERFORMANCE, or SECURITY algorithm selection.
    selectionStrategy = SelectionStrategy.SMART
    // Used by SMART; recommended range is 0.0–1.0.
    performanceWeight = 0.7
    // Used by SMART; recommended range is 0.0–1.0.
    securityWeight = 0.3
}
```

### Groovy DSL (`build.gradle`)

```groovy
import com.android.string.plugin.mode.BytesMode
import com.android.string.plugin.mode.Mode
import com.android.string.plugin.mode.SelectionStrategy

stringblur {
    // Encryption key. Use a string, or an integer for a random key length.
    // Omit this line to resolve the key in this order:
    // 1. gradle.properties or -Pstringblur.key=your-key
    // 2. Environment variable STRINGBLUR_KEY=your-key
    // 3. Root local.properties: stringblur.key=your-key
    key = "my-project-key-2024"
    // Master switch; disabled by default.
    enable = true

    // Empty list: current applicationId/namespace; null: all classes;
    // non-empty list: add these package prefixes to the current scope.
    encodePackages = ["com.example"]
    // Class names or package prefixes to exclude.
    whiteList = ["BuildConfig", "R", "R2"]

    // Available algorithms; an empty list falls back to Mode.DEFAULT.
    modes = [Mode.XOR_SIMD, Mode.FAST_ROT, Mode.REVERSE]
    // STRING, BYTES, or RANDOM encrypted-data representation.
    bytesMode = BytesMode.RANDOM
    // Skip strings shorter than this value; negative values become 0.
    minLength = 3

    // Also encrypt debug variants when enabled.
    enableWhenDebug = false

    // RANDOM, SMART, PERFORMANCE, or SECURITY algorithm selection.
    selectionStrategy = SelectionStrategy.SMART
    // Used by SMART; recommended range is 0.0–1.0.
    performanceWeight = 0.7
    // Used by SMART; recommended range is 0.0–1.0.
    securityWeight = 0.3
}
```

## Encryption modes

- `Mode.DEFAULT`: key-based byte addition/subtraction; the compatibility-oriented default.
- `Mode.XOR`: basic key-based XOR transformation.
- `Mode.REVERSE`: reverses byte order; very fast for general-purpose use.
- `Mode.SHIFT`: key-based byte shifting.
- `Mode.XOR_SHIFT`: combines XOR and SHIFT for stronger protection.
- `Mode.XOR_SIMD`: batch XOR optimized for medium and long strings and performance-sensitive code.
- `Mode.FAST_ROT`: fast bit rotation, suitable for frequent short strings.

## Encrypted data representation

- `BytesMode.STRING`: stores encrypted data as a string constant.
- `BytesMode.BYTES`: stores encrypted data as a byte array.
- `BytesMode.RANDOM`: randomly chooses `STRING` or `BYTES` for each string.

## Smart mode selection

`SelectionStrategy.RANDOM` is the default and preserves random mode selection. Use `SMART`, `PERFORMANCE`, or `SECURITY` when the mode should follow content characteristics or a priority.

| Strategy | Recommendation |
| --- | --- |
| `RANDOM` | Default choice when compatibility and behavior preservation matter. |
| `SMART` | Recommended for most projects; balances string characteristics, performance, and security. |
| `PERFORMANCE` | Use for performance-sensitive applications; selects the fastest available mode. |
| `SECURITY` | Use for security-sensitive strings; selects the strongest available mode. |

For `SMART`, the default weight is `0.5` for both performance and security. Adjust the weights when one priority matters more, for example `0.7` performance and `0.3` security.

| String characteristic | Preferred mode |
| --- | --- |
| Short strings (1–8 characters) | `FAST_ROT` |
| Medium strings (9–50 characters) | `XOR_SIMD` |
| Long strings (over 200 characters) | `REVERSE` |
| Sensitive content | `XOR_SHIFT` |
| Mostly numeric or binary data | `FAST_ROT` or `XOR_SIMD` |

## Related project

- [AabResGuard](https://github.com/dawnuu/AabResGuard) — Android AAB resource obfuscation tool.

## License

This project is licensed under the Apache License 2.0. See [LICENSE](LICENSE).
