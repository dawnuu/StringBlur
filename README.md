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
    // Master switch; disabled by default.
    enable = true
    // Optional salt that makes this project's output different; changing it changes all generated output.
    // Not a secret, so it is fine to commit. Default empty.
    seed = "my-project-seed"

    // Empty list: current applicationId/namespace; null: all classes;
    // non-empty list: add these package prefixes to the current scope.
    encodePackages = listOf("com.example")
    // Class names or package prefixes to exclude.
    whiteList = listOf("BuildConfig", "R", "R2")

    // Available algorithms; an empty list falls back to Mode.DEFAULT.
    // One mode is used as-is; with several, one is chosen at random per string.
    modes = listOf(Mode.XOR, Mode.FAST_ROT, Mode.XOR_SHIFT)
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
    // Master switch; disabled by default.
    enable = true

    // Empty list: current applicationId/namespace; null: all classes;
    // non-empty list: add these package prefixes to the current scope.
    encodePackages = ["com.example"]
    // Class names or package prefixes to exclude.
    whiteList = ["BuildConfig", "R", "R2"]

    // Available algorithms; an empty list falls back to Mode.DEFAULT.
    // One mode is used as-is; with several, one is chosen at random per string.
    modes = [Mode.XOR, Mode.FAST_ROT, Mode.XOR_SHIFT]
    // STRING, BYTES, or RANDOM encrypted-data representation; default STRING.
    bytesMode = BytesMode.RANDOM
    // Skip strings shorter than this value; negative values become 0. Default 0.
    minLength = 3

    // Also encrypt debug variants when enabled; default false.
    enableWhenDebug = false
}
```

## String concatenation

Direct boolean, character, integer, and floating-point constant appends to `StringBuilder`/`StringBuffer` are converted to text and passed through encryption before D8 can fold them into new plaintext. Numeric bootstrap constants in concat recipes are handled as text too. Only direct constants with verified stack provenance are rewritten; variables, arithmetic expressions, merged branch values, and lone surrogate characters stay intact. Length and annotation rules still apply; this does not cover all subsequent D8/R8 constant propagation.

`StringConcatFactory.makeConcatWithConstants` invokedynamic calls are rewritten to `StringBuilder`, exposing fixed recipe text and string bootstrap constants to the existing encryption pipeline without `inline` compiler flags. Arbitrary call-site names and recipes mixing Class, MethodType, and numeric constants are supported; dynamic argument types and order are preserved.

`minLength` applies to each extracted literal segment; short segments and `@KeepString` scopes remain plaintext. Calls without fixed text are left intact. Unsupported bootstrap constants such as MethodHandle or ConstantDynamic, or mismatched recipe argument counts, leave the original instruction intact and its recipe text unencrypted. This does not process `strings.xml`.

## Encryption modes

- `Mode.DEFAULT`: byte addition/subtraction; the compatibility-oriented default.
- `Mode.XOR`: basic XOR transformation.
- `Mode.XOR_SHIFT`: combines XOR and byte shifting for stronger protection.
- `Mode.FAST_ROT`: fast bit rotation, suitable for frequent short strings.
- `Mode.XOR_CHAIN`: each byte XORs the key byte and the previous ciphertext byte (initially 0); changing a plaintext byte changes all subsequent ciphertext bytes.
- `Mode.LCG_STREAM`: hashes the entire UTF-8 key into a 32-bit seed with FNV-1a, then XORs each byte with the high byte of an LCG step (`state = state * 1664525 + 1013904223`, modulo 2^32). This avoids directly repeating key bytes; the stream is predictable and has a finite period.

Enable the new modes with `modes = listOf(Mode.XOR_CHAIN, Mode.LCG_STREAM)` (Kotlin) or `modes = [Mode.XOR_CHAIN, Mode.LCG_STREAM]` (Groovy).

## Encrypted data representation

- `BytesMode.STRING`: stores encrypted data as a string constant.
- `BytesMode.BYTES`: stores encrypted data as a byte array.
- `BytesMode.RANDOM`: randomly chooses `STRING` or `BYTES` for each string.

## Security notes

StringBlur is string obfuscation, not strong encryption. The decryption logic and the material it needs ship inside the app, so anyone who analyzes the APK can recover the strings. It raises the effort needed to read them (no plain strings in the dex, a different key per string, no generic one-click decryptor), but it cannot stop a determined analyst, and it cannot stop hooking the app at runtime.

- Do not rely on it to protect secrets such as API secrets or signing keys. Keep those on a server and hand the app short-lived tokens instead.
- `seed` is not a secret. It only makes your project's output differ from other projects'.

## Related project

- [AabResGuard](https://github.com/dawnuu/AabResGuard) — Android AAB resource obfuscation tool.

## License

This project is licensed under the Apache License 2.0. See [LICENSE](LICENSE).
