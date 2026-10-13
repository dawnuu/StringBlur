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
    // One mode is used as-is; with several, one is chosen at random per string.
    modes = listOf(Mode.XOR_SIMD, Mode.FAST_ROT, Mode.XOR_SHIFT)
    // STRING, BYTES, or RANDOM encrypted-data representation; default STRING.
    bytesMode = BytesMode.RANDOM
    // Skip strings shorter than this value; negative values become 0. Default 0.
    minLength = 3

    // Also encrypt debug variants when enabled; default false.
    enableWhenDebug = false
}
```

### Groovy DSL (`build.gradle`)

```groovy
import com.android.string.plugin.mode.BytesMode
import com.android.string.plugin.mode.Mode

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
    // One mode is used as-is; with several, one is chosen at random per string.
    modes = [Mode.XOR_SIMD, Mode.FAST_ROT, Mode.XOR_SHIFT]
    // STRING, BYTES, or RANDOM encrypted-data representation; default STRING.
    bytesMode = BytesMode.RANDOM
    // Skip strings shorter than this value; negative values become 0. Default 0.
    minLength = 3

    // Also encrypt debug variants when enabled; default false.
    enableWhenDebug = false
}
```

## Encryption modes

- `Mode.DEFAULT`: key-based byte addition/subtraction; the compatibility-oriented default.
- `Mode.XOR`: basic key-based XOR transformation.
- `Mode.XOR_SHIFT`: combines XOR and byte shifting for stronger protection.
- `Mode.XOR_SIMD`: batch XOR optimized for medium and long strings and performance-sensitive code.
- `Mode.FAST_ROT`: fast bit rotation, suitable for frequent short strings.

## Encrypted data representation

- `BytesMode.STRING`: stores encrypted data as a string constant.
- `BytesMode.BYTES`: stores encrypted data as a byte array.
- `BytesMode.RANDOM`: randomly chooses `STRING` or `BYTES` for each string.

## Related project

- [AabResGuard](https://github.com/dawnuu/AabResGuard) — Android AAB resource obfuscation tool.

## License

This project is licensed under the Apache License 2.0. See [LICENSE](LICENSE).
