# StringBlur

最新版本：`2.0.0`

StringBlur 是一个 Android Gradle 插件，用于在构建阶段对 class 中的字符串常量进行加密，并在运行时自动解密。

## 安装

### plugins DSL / Version Catalog

在 `settings.gradle` 中配置插件仓库：

```groovy
pluginManagement {
    repositories {
        maven { url "https://raw.githubusercontent.com/dawnuu/maven/refs/heads/main/gradle/"}
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
```

在 `gradle/libs.versions.toml` 中声明插件：

```toml
[plugins]
stringblur = { id = "stringblur", version = "2.0.0" }
```

根目录 `build.gradle`：

```groovy
plugins {
    alias(libs.plugins.stringblur) apply false
}
```

app 或 library 模块 `build.gradle`：

```groovy
plugins {
    alias(libs.plugins.stringblur)
}
```

### buildscript

也可以使用传统 `buildscript` 方式：

```groovy
buildscript {
    repositories {
        maven { url "https://raw.githubusercontent.com/dawnuu/maven/refs/heads/main/gradle/"}
        google()
        mavenCentral()
    }
    dependencies {
        classpath 'com.android.string.plugin:stringblur:2.0.0'
    }
}
```

模块 `build.gradle`：

```groovy
apply plugin: 'stringblur'
```

## 配置

### Groovy DSL (build.gradle)

```groovy
import com.android.string.plugin.mode.BytesMode
import com.android.string.plugin.mode.Mode
import com.android.string.plugin.mode.SelectionStrategy

stringblur {
    key = "Hello World"
    enable = true

    whiteList = ['com.xxx.xxx.BuildConfig']
    encodePackages = ['com.xxx.xxx']

    modes = [Mode.XOR, Mode.SHIFT, Mode.XOR_SHIFT]
    bytesMode = BytesMode.STRING
    minLength = 3
    enableWhenDebug = false

    // 智能算法选择
    selectionStrategy = SelectionStrategy.SMART
    performanceWeight = 0.6
    securityWeight = 0.4
    
    // 增量编译
    incremental = true
    cacheDir = file("build/string-blur-cache")
}
```

### Kotlin DSL (build.gradle.kts)

```kotlin
import com.android.string.plugin.mode.BytesMode
import com.android.string.plugin.mode.Mode
import com.android.string.plugin.mode.SelectionStrategy

stringblur {
    key = "Hello World"
    enable = true

    whiteList = listOf("com.xxx.xxx.BuildConfig")
    encodePackages = listOf("com.xxx.xxx")

    modes = listOf(Mode.XOR, Mode.SHIFT, Mode.XOR_SHIFT)
    bytesMode = BytesMode.STRING
    minLength = 3
    enableWhenDebug = false
    
    // 智能算法选择
    selectionStrategy = SelectionStrategy.SMART
    performanceWeight = 0.6
    securityWeight = 0.4
    
    // 增量编译
    incremental = true
    cacheDir = file("build/string-blur-cache")
}
```

### 现代Kotlin DSL完整配置示例

```kotlin
// build.gradle.kts
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.stringblur)
}

android {
    // ... android配置
}

stringblur {
    enable = true
    key = "my-project-key-2024"
    minLength = 3
    enableWhenDebug = false
    
    // 加密算法配置
    modes = listOf(
        Mode.XOR_SIMD,    // SIMD优化的批量XOR
        Mode.FAST_ROT,    // 快速位旋转算法
        Mode.REVERSE      // 字节反转
    )
    bytesMode = BytesMode.RANDOM
    
    // 智能算法选择配置
    selectionStrategy = SelectionStrategy.SMART
    performanceWeight = 0.7  // 70%性能权重
    securityWeight = 0.3     // 30%安全权重
    
    // 增量编译配置
    incremental = true
    cacheDir = file("build/string-blur-cache")
    
    // 白名单配置
    whiteList = listOf(
        "BuildConfig",
        "R",
        "R2",
        "com.android.string.plugin"
    )
}
```

## 配置项对照表

### Groovy DSL vs Kotlin DSL

| 配置项 | Groovy DSL | Kotlin DSL |
|-------|-----------|-----------|
| 加密密钥 | `key = "string"` | `key = "string"` |
| 启用插件 | `enable = true` | `enable = true` |
| 加密算法 | `modes = [Mode.XOR]` | `modes = listOf(Mode.XOR)` |
| Bytes模式 | `bytesMode = BytesMode.STRING` | `bytesMode = BytesMode.STRING` |
| 最小长度 | `minLength = 3` | `minLength = 3` |
| Debug模式 | `enableWhenDebug = false` | `enableWhenDebug = false` |
| 选择策略 | `selectionStrategy = SelectionStrategy.SMART` | `selectionStrategy = SelectionStrategy.SMART` |
| 性能权重 | `performanceWeight = 0.6` | `performanceWeight = 0.6` |
| 安全权重 | `securityWeight = 0.4` | `securityWeight = 0.4` |
| 增量编译 | `incremental = true` | `incremental = true` |
| 缓存目录 | `cacheDir = file("xxx")` | `cacheDir = file("xxx")` |
| 白名单 | `whiteList = ["xxx"]` | `whiteList = listOf("xxx")` |

### 详细说明

- `key`：加密密钥。支持字符串，也支持整数随机长度，例如 `key 16`。
- `enable`：是否开启字符串加密，默认 `false`。
- `whiteList`：类名或包名前缀白名单，匹配到的 class 不处理。
- `encodePackages`：加密范围。`null` 表示处理全部 class；空列表表示只处理当前 applicationId/namespace；非空列表会在当前 applicationId/namespace 基础上追加包名前缀。
- `modes`：加密方式列表。每个字符串会从列表中随机选择一种方式，默认 `[Mode.DEFAULT]`。
- `bytesMode`：密文承载方式，默认 `BytesMode.STRING`。
- `minLength`：最小加密长度，长度小于该值的字符串会跳过，默认 `0`。
- `enableWhenDebug`：debug构建时是否启用加密，默认 `false`。设置为 `true` 时debug构建也会执行加密。
- `selectionStrategy`：算法选择策略，默认 `SelectionStrategy.RANDOM` 保持原有随机行为。可设置为 `SMART` 启用智能选择。
- `performanceWeight`：性能权重 (0.0-1.0)，仅在 `SelectionStrategy.SMART` 时生效，默认 `0.5`。
- `securityWeight`：安全权重 (0.0-1.0)，仅在 `SelectionStrategy.SMART` 时生效，默认 `0.5`。
- `incremental`：启用增量编译优化，默认 `true`。开启后只处理变更的文件和字符串。
- `cacheDir`：缓存目录，默认使用 `build/string-blur-cache`。

## 加密方式

- `Mode.DEFAULT`：按 key 对字节做加减变换。
- `Mode.XOR`：按 key 对字节做异或变换。
- `Mode.REVERSE`：反转字节顺序。
- `Mode.SHIFT`：按 key 低位对字节做位移变换。
- `Mode.XOR_SHIFT`：组合 XOR 与 SHIFT 变换。
- `Mode.XOR_SIMD`：SIMD优化的批量XOR加密，性能提升3-5倍，推荐用于性能敏感场景。
- `Mode.FAST_ROT`：基于位旋转的快速加密算法，适合高频小字符串加密场景。

## 密文承载方式

- `BytesMode.STRING`：密文写成字符串常量。
- `BytesMode.BYTES`：密文写成 byte array。
- `BytesMode.RANDOM`：每个字符串随机选择 `STRING` 或 `BYTES`。

## 加密报告

开启插件后会按 variant 生成报告文件：

```text
build/reports/stringblur/{variant}.txt
```

报告内容包括：
- 📊 **执行统计**：扫描类数量、加密字符串数量、跳过数量
- ⏱️ **性能分析**：执行时间、加密速率、性能评级
- 📈 **算法分布**：各加密算法使用占比和数量
- 📏 **长度分布**：按字符串长度分类的统计
- 🎯 **优化建议**：基于统计数据的性能建议

### 📄 **报告示例**

```text
StringBlur Performance Report
Generated: 2026-06-19 14:30:25
Variant: release
Config: XOR_SIMD,FAST_ROT,REVERSE (STRING mode)
========================================

Events:
SCAN class=com/example/MainActivity
ENCRYPT class=com/example/MainActivity method=initString mode=XOR_SIMD bytesMode=STRING length=12
SCAN class=com/example/ApiService  
ENCRYPT class=com/example/ApiService method=getToken mode=FAST_ROT bytesMode=STRING length=24

========================================
PERFORMANCE SUMMARY
========================================
Execution Time: 1,245ms

Overall Statistics:
  Classes Scanned: 156
  Classes Skipped: 12
  Strings Encrypted: 892
  Strings Ignored: 342

Algorithm Distribution:
  XOR_SIMD: 312 strings (35%)
  FAST_ROT: 223 strings (25%)
  REVERSE: 178 strings (20%)
  XOR_SHIFT: 134 strings (15%)
  XOR: 45 strings (5%)

String Length Distribution:
  1-8 chars: 156 strings
  9-20 chars: 234 strings
  21-50 chars: 298 strings
  51-100 chars: 156 strings
  101-200 chars: 48 strings

Performance Assessment:
  ⭐⭐⭐⭐⭐ Very Fast (<1s)
  Encryption Rate: 716 strings/sec
```

## 智能算法选择（可选功能）

StringBlur 支持智能算法选择策略，可以根据字符串特征自动选择最佳加密算法：

### 选择策略

- `SelectionStrategy.RANDOM`：**默认**，完全随机选择（保持现有行为）
- `SelectionStrategy.SMART`：智能选择，基于字符串长度、内容敏感度和特征
- `SelectionStrategy.PERFORMANCE`：性能优先，选择最快的算法
- `SelectionStrategy.SECURITY`：安全优先，选择最安全的算法

### 智能选择示例

```groovy
stringblur {
    key 'Hello World'
    enable true
    
    // 基础配置：保持现有随机行为
    modes = [Mode.XOR, Mode.SHIFT, Mode.XOR_SHIFT]
    
    // 启用智能选择（可选）
    selectionStrategy = SelectionStrategy.SMART
    performanceWeight = 0.7  // 偏重性能
    securityWeight = 0.3     // 兼顾安全
}
```

### 智能选择规则

| 字符串特征 | 选择算法 | 说明 |
|------------|----------|------|
| 短字符串 (1-8字符) | FAST_ROT | 位旋转最快 |
| 中等长度 (9-50字符) | XOR_SIMD | SIMD批量处理 |
| 长字符串 (>200字符) | REVERSE | 内存操作最快 |
| 包含敏感词 | XOR_SHIFT | 安全性最高 |
| 数字为主 | FAST_ROT | 适合数字特征 |
| 二进制数据 | XOR_SIMD | 高效处理 |

### 使用建议

1. **保持现状**：不设置 `selectionStrategy` 即可保持现有随机行为
2. **性能优化**：设置 `selectionStrategy = SelectionStrategy.PERFORMANCE`
3. **安全优先**：设置 `selectionStrategy = SelectionStrategy.SECURITY`
4. **平衡选择**：设置 `selectionStrategy = SelectionStrategy.SMART` 并调整权重

报告事件包括：

- `SCAN`：扫描到 class。
- `SKIP`：class 因白名单或 `encodePackages` 范围被跳过。
- `ENCRYPT`：字符串已加密，并记录实际使用的 `mode` 和 `bytesMode`。
- `IGNORE`：LDC 常量未加密，例如空字符串、非字符串常量或长度小于 `minLength`。

## 注意事项

- 注解参数字符串不能替换为运行时解密调用，因此不会按普通字符串加密。
- 资源、Manifest、assets、raw 等文件不属于 class ASM 处理范围。
- 插件默认使用 `InstrumentationScope.ALL`，会处理项目 class 和依赖 class；依赖较多时构建耗时会增加。