# StringBlur

[English](README.md) | [简体中文](README.zh-CN.md)

[![Maven Central](https://img.shields.io/maven-central/v/io.github.dawnuu/stringblur?style=flat-square)](https://central.sonatype.com/artifact/io.github.dawnuu/stringblur)

StringBlur 是一个 Android Gradle 插件，用于在构建阶段对 class 中的字符串常量进行加密，并在运行时自动解密。

## 迁移到 Maven Central

本次迁移将旧版自定义 GitHub Maven 仓库替换为 Maven Central，变更的是仓库和 Maven 发布坐标，源码包保持不变。

| 项目 | 迁移前 | 迁移后 |
| --- | --- | --- |
| 仓库 | 旧版自定义 GitHub Maven 仓库（已移除） | `mavenCentral()` |
| 插件坐标 | `com.android.string.plugin:stringblur:2.1.0` | `io.github.dawnuu:stringblur:1.0.1` |
| 插件 ID | `stringblur` | `io.github.dawnuu.stringblur`（Plugins DSL）；`stringblur`（buildscript） |
| 源码包 | `com.android.string.plugin` | `com.android.string.plugin` |

## 接入

### Plugins DSL / Version Catalog

在 `settings.gradle` 或 `settings.gradle.kts` 中配置插件仓库：

```groovy
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
```

在 `gradle/libs.versions.toml` 中声明插件：

```toml
[versions]
stringblur = "1.0.1"

[plugins]
stringblur = { id = "io.github.dawnuu.stringblur", version.ref = "stringblur" }
```

在根目录和 app/library 模块的构建文件中应用插件：

```kotlin
// 根目录 build.gradle.kts
plugins {
    alias(libs.plugins.stringblur) apply false
}

// app 或 library 模块 build.gradle.kts
plugins {
    alias(libs.plugins.stringblur)
}
```

Groovy DSL 在 `build.gradle` 中使用 `alias(libs.plugins.stringblur)`。

不使用 Version Catalog 时：

```kotlin
plugins {
    id("io.github.dawnuu.stringblur") version "1.0.1"
}
```

## 配置

### Kotlin DSL（`build.gradle.kts`）

```kotlin
import com.android.string.plugin.mode.BytesMode
import com.android.string.plugin.mode.Mode
import com.android.string.plugin.mode.SelectionStrategy

stringblur {
    // 加密密钥：支持字符串，也支持用整数指定随机密钥长度。
    // 省略此行时按以下顺序读取：
    // 1. gradle.properties 中的 stringblur.key，或 -Pstringblur.key=your-key
    // 2. 环境变量 STRINGBLUR_KEY=your-key
    // 3. 项目根目录 local.properties 中的 stringblur.key=your-key
    key = "my-project-key-2024"
    // 加密总开关，默认关闭。
    enable = true

    // 空列表：当前 applicationId/namespace；null：全部 class；
    // 非空列表：在当前范围上追加这些包名前缀。
    encodePackages = listOf("com.example")
    // 要排除的类名或包名前缀。
    whiteList = listOf("BuildConfig", "R", "R2")

    // 可用加密算法，空列表会回退为 Mode.DEFAULT。
    modes = listOf(Mode.XOR_SIMD, Mode.FAST_ROT, Mode.REVERSE)
    // 密文承载方式：STRING、BYTES 或 RANDOM。
    bytesMode = BytesMode.RANDOM
    // 跳过长度小于该值的字符串，负数按 0 处理。
    minLength = 3

    // 是否同时对 debug variant 启用加密。
    enableWhenDebug = false

    // 算法选择：RANDOM、SMART、PERFORMANCE 或 SECURITY。
    selectionStrategy = SelectionStrategy.SMART
    // 仅 SMART 策略使用，建议范围为 0.0–1.0。
    performanceWeight = 0.7
    // 仅 SMART 策略使用，建议范围为 0.0–1.0。
    securityWeight = 0.3
}
```

### Groovy DSL（`build.gradle`）

```groovy
import com.android.string.plugin.mode.BytesMode
import com.android.string.plugin.mode.Mode
import com.android.string.plugin.mode.SelectionStrategy

stringblur {
    // 加密密钥：支持字符串，也支持用整数指定随机密钥长度。
    // 省略此行时按以下顺序读取：
    // 1. gradle.properties 中的 stringblur.key，或 -Pstringblur.key=your-key
    // 2. 环境变量 STRINGBLUR_KEY=your-key
    // 3. 项目根目录 local.properties 中的 stringblur.key=your-key
    key = "my-project-key-2024"
    // 加密总开关，默认关闭。
    enable = true

    // 空列表：当前 applicationId/namespace；null：全部 class；
    // 非空列表：在当前范围上追加这些包名前缀。
    encodePackages = ["com.example"]
    // 要排除的类名或包名前缀。
    whiteList = ["BuildConfig", "R", "R2"]

    // 可用加密算法，空列表会回退为 Mode.DEFAULT。
    modes = [Mode.XOR_SIMD, Mode.FAST_ROT, Mode.REVERSE]
    // 密文承载方式：STRING、BYTES 或 RANDOM。
    bytesMode = BytesMode.RANDOM
    // 跳过长度小于该值的字符串，负数按 0 处理。
    minLength = 3

    // 是否同时对 debug variant 启用加密。
    enableWhenDebug = false

    // 算法选择：RANDOM、SMART、PERFORMANCE 或 SECURITY。
    selectionStrategy = SelectionStrategy.SMART
    // 仅 SMART 策略使用，建议范围为 0.0–1.0。
    performanceWeight = 0.7
    // 仅 SMART 策略使用，建议范围为 0.0–1.0。
    securityWeight = 0.3
}
```

## 加密方式

- `Mode.DEFAULT`：按 key 对字节做加减变换，兼容性优先的默认算法。
- `Mode.XOR`：按 key 做基础异或变换。
- `Mode.REVERSE`：反转字节顺序，通用场景下速度很快。
- `Mode.SHIFT`：按 key 对字节做位移变换。
- `Mode.XOR_SHIFT`：组合 XOR 与 SHIFT，安全性更强。
- `Mode.XOR_SIMD`：针对中长字符串和性能敏感场景优化的批量 XOR。
- `Mode.FAST_ROT`：快速位旋转，适合高频短字符串。

## 密文承载方式

- `BytesMode.STRING`：将密文保存为字符串常量。
- `BytesMode.BYTES`：将密文保存为 byte array。
- `BytesMode.RANDOM`：每个字符串随机选择 `STRING` 或 `BYTES`。

## 智能算法选择

`SelectionStrategy.RANDOM` 为默认策略，保持随机选择算法的行为。需要根据字符串特征或优先级选择算法时，可以使用 `SMART`、`PERFORMANCE` 或 `SECURITY`。

| 策略 | 使用建议 |
| --- | --- |
| `RANDOM` | 默认选择，适合需要保持原有行为的场景。 |
| `SMART` | 推荐大多数项目使用，在字符串特征、性能和安全性之间平衡。 |
| `PERFORMANCE` | 适合性能敏感应用，选择可用算法中速度最快的方式。 |
| `SECURITY` | 适合安全敏感字符串，选择可用算法中安全性最高的方式。 |

`SMART` 默认性能和安全权重均为 `0.5`。如果更重视某一方面，可以调整权重，例如性能 `0.7`、安全性 `0.3`。

| 字符串特征 | 推荐算法 |
| --- | --- |
| 短字符串（1–8 个字符） | `FAST_ROT` |
| 中等长度（9–50 个字符） | `XOR_SIMD` |
| 长字符串（超过 200 个字符） | `REVERSE` |
| 敏感内容 | `XOR_SHIFT` |
| 以数字或二进制数据为主 | `FAST_ROT` 或 `XOR_SIMD` |

## 相关项目

- [AabResGuard](https://github.com/dawnuu/AabResGuard) — Android AAB 资源混淆工具。

## 许可证

本项目采用 Apache License 2.0，详见 [LICENSE](LICENSE)。
