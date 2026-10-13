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

stringblur {
    // 加密总开关，默认关闭。
    enable = true
    // 可选的盐，让本项目的生成内容与别人不同，修改后所有生成内容都会变化；
    // 它不是密钥，无需保密，可以提交进版本库，默认为空。
    seed = "my-project-seed"

    // 空列表：当前 applicationId/namespace；null：全部 class；
    // 非空列表：在当前范围上追加这些包名前缀。
    encodePackages = listOf("com.example")
    // 要排除的类名或包名前缀。
    whiteList = listOf("BuildConfig", "R", "R2")

    // 可用加密算法，空列表会回退为 Mode.DEFAULT。
    // 只有一个时直接使用；有多个时每个字符串随机选择其中一个。
    modes = listOf(Mode.XOR_SIMD, Mode.FAST_ROT, Mode.XOR_SHIFT)
    // 密文承载方式：STRING、BYTES 或 RANDOM，默认 STRING。
    bytesMode = BytesMode.RANDOM
    // 跳过长度小于该值的字符串，负数按 0 处理，默认 0。
    minLength = 3

    // 是否同时对 debug variant 启用加密，默认 false。
    enableWhenDebug = false
}
```

### Groovy DSL（`build.gradle`）

```groovy
import com.android.string.plugin.mode.BytesMode
import com.android.string.plugin.mode.Mode

stringblur {
    // 加密总开关，默认关闭。
    enable = true

    // 空列表：当前 applicationId/namespace；null：全部 class；
    // 非空列表：在当前范围上追加这些包名前缀。
    encodePackages = ["com.example"]
    // 要排除的类名或包名前缀。
    whiteList = ["BuildConfig", "R", "R2"]

    // 可用加密算法，空列表会回退为 Mode.DEFAULT。
    // 只有一个时直接使用；有多个时每个字符串随机选择其中一个。
    modes = [Mode.XOR_SIMD, Mode.FAST_ROT, Mode.XOR_SHIFT]
    // 密文承载方式：STRING、BYTES 或 RANDOM，默认 STRING。
    bytesMode = BytesMode.RANDOM
    // 跳过长度小于该值的字符串，负数按 0 处理，默认 0。
    minLength = 3

    // 是否同时对 debug variant 启用加密，默认 false。
    enableWhenDebug = false
}
```

## 加密方式

- `Mode.DEFAULT`：对字节做加减变换，兼容性优先的默认算法。
- `Mode.XOR`：基础异或变换。
- `Mode.XOR_SHIFT`：组合 XOR 与字节位移，安全性更强。
- `Mode.XOR_SIMD`：针对中长字符串和性能敏感场景优化的批量 XOR。
- `Mode.FAST_ROT`：快速位旋转，适合高频短字符串。

## 密文承载方式

- `BytesMode.STRING`：将密文保存为字符串常量。
- `BytesMode.BYTES`：将密文保存为 byte array。
- `BytesMode.RANDOM`：每个字符串随机选择 `STRING` 或 `BYTES`。

## 安全说明

StringBlur 是字符串混淆，不是强加密。解密逻辑和所需的材料都随 App 一起发布，任何人分析 APK 都有可能还原字符串。它提高的是读取字符串的成本（dex 里没有明文字符串、每个字符串的 key 各不相同、没有通用的一键解密工具），但挡不住有决心的分析者，也挡不住在运行时对 App 做 hook。

- 不要依赖它保护 API secret、签名密钥这类机密，这类内容应放在服务端，由服务端给 App 下发短期 token。
- `seed` 不是密钥，它只是让你的项目产物与其他项目不同。

## 相关项目

- [AabResGuard](https://github.com/dawnuu/AabResGuard) — Android AAB 资源混淆工具。

## 许可证

本项目采用 Apache License 2.0，详见 [LICENSE](LICENSE)。
